package com.softwaremill.jox.json;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;

/**
 * Splits a byte flow into NDJSON records decoded as strict UTF-8.
 *
 * <p>Records are delimited by LF only; a CR before the LF stays in the record. One UTF-8 BOM is
 * removed from the first record. A final record without LF is emitted. The size limit is checked
 * against each record's encoded bytes, excluding the LF, and applies to blank records too.
 * Malformed UTF-8 or an oversized record fails with {@link IllegalArgumentException}.
 */
final class NdjsonFraming {

    private static final byte[] UTF_8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
    private static final byte[] NO_BYTES = {};

    private NdjsonFraming() {}

    static Flow<String> lines(ByteFlow bytes, int maxRecordBytes) {
        return Flows.usingEmit(
                output -> {
                    // usingEmit runs single-threaded, so one decoder serves all records
                    var decoder =
                            StandardCharsets.UTF_8
                                    .newDecoder()
                                    .onMalformedInput(CodingErrorAction.REPORT)
                                    .onUnmappableCharacter(CodingErrorAction.REPORT);
                    var framer = new Framer(maxRecordBytes);
                    FlowEmit<Record> emitRecord = record -> output.apply(decode(decoder, record));

                    bytes.runToEmit(chunk -> framer.emitRecords(chunk, emitRecord));
                    framer.finish(emitRecord);
                });
    }

    private static String decode(CharsetDecoder decoder, Record record) {
        var bomLength = record.first() && startsWithBom(record) ? UTF_8_BOM.length : 0;
        var buffer =
                ByteBuffer.wrap(
                        record.bytes(), record.offset() + bomLength, record.length() - bomLength);
        try {
            return decoder.decode(buffer).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("NDJSON input contains malformed UTF-8", e);
        }
    }

    private static boolean startsWithBom(Record record) {
        return record.length() >= UTF_8_BOM.length
                && Arrays.equals(
                        record.bytes(),
                        record.offset(),
                        record.offset() + UTF_8_BOM.length,
                        UTF_8_BOM,
                        0,
                        UTF_8_BOM.length);
    }

    /** A view of a record's encoded bytes, valid only until the record is emitted. */
    private record Record(byte[] bytes, int offset, int length, boolean first) {}

    private static final class Framer {
        private final int maxRecordBytes;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean firstRecord = true;

        private Framer(int maxRecordBytes) {
            this.maxRecordBytes = maxRecordBytes;
        }

        private void emitRecords(ByteChunk chunk, FlowEmit<Record> output) throws Exception {
            for (var array : chunk.getArrays()) {
                int recordStart = 0;
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == '\n') {
                        output.apply(completeRecord(array, recordStart, i - recordStart));
                        recordStart = i + 1;
                    }
                }
                append(array, recordStart, array.length - recordStart);
            }
        }

        private void finish(FlowEmit<Record> output) throws Exception {
            if (buffer.size() > 0) {
                output.apply(completeRecord(NO_BYTES, 0, 0));
            }
        }

        private void append(byte[] bytes, int offset, int length) {
            requireWithinLimit(length);
            buffer.write(bytes, offset, length);
        }

        // A record contained in one array is emitted as a view of that array; only records spanning
        // arrays are copied through the buffer.
        private Record completeRecord(byte[] tail, int offset, int length) {
            requireWithinLimit(length);
            Record record;
            if (buffer.size() == 0) {
                record = new Record(tail, offset, length, firstRecord);
            } else {
                buffer.write(tail, offset, length);
                var bytes = buffer.toByteArray();
                buffer.reset();
                record = new Record(bytes, 0, bytes.length, firstRecord);
            }
            firstRecord = false;
            return record;
        }

        private void requireWithinLimit(int length) {
            if ((long) buffer.size() + length > maxRecordBytes) {
                throw new IllegalArgumentException(
                        "NDJSON record exceeds the configured maximum of "
                                + maxRecordBytes
                                + " bytes");
            }
        }
    }
}

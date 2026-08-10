package com.softwaremill.jox.json;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;

final class NdjsonFraming {

    private static final byte[] UTF_8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

    private NdjsonFraming() {}

    static Flow<String> lines(ByteFlow bytes, int maxRecordBytes) {
        return Flows.usingEmit(
                output -> {
                    var framer = new Framer(maxRecordBytes);
                    FlowEmit<Record> emitRecord = record -> output.apply(decode(record));

                    bytes.runToEmit(chunk -> framer.emitRecords(chunk, emitRecord));

                    var finalRecord = framer.finish();
                    if (finalRecord.isPresent()) {
                        emitRecord.apply(finalRecord.get());
                    }
                });
    }

    private static String decode(Record record) {
        var bytes = record.bytes();
        var offset = record.first() && startsWithBom(bytes) ? UTF_8_BOM.length : 0;
        var decoder =
                StandardCharsets.UTF_8
                        .newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("NDJSON input contains malformed UTF-8", e);
        }
    }

    private static boolean startsWithBom(byte[] bytes) {
        if (bytes.length < UTF_8_BOM.length) {
            return false;
        }
        for (int i = 0; i < UTF_8_BOM.length; i++) {
            if (bytes[i] != UTF_8_BOM[i]) {
                return false;
            }
        }
        return true;
    }

    private record Record(byte[] bytes, boolean first) {}

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
                        append(array, recordStart, i - recordStart);
                        output.apply(completeRecord());
                        recordStart = i + 1;
                    }
                }
                append(array, recordStart, array.length - recordStart);
            }
        }

        private Optional<Record> finish() {
            return buffer.size() == 0 ? Optional.empty() : Optional.of(completeRecord());
        }

        private void append(byte[] bytes, int offset, int length) {
            if ((long) buffer.size() + length > maxRecordBytes) {
                throw new IllegalArgumentException(
                        "NDJSON record exceeds the configured maximum of "
                                + maxRecordBytes
                                + " bytes");
            }
            buffer.write(bytes, offset, length);
        }

        private Record completeRecord() {
            var record = new Record(buffer.toByteArray(), firstRecord);
            buffer.reset();
            firstRecord = false;
            return record;
        }
    }
}

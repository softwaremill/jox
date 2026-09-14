package com.softwaremill.jox.json;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;

/**
 * Splits a byte flow into NDJSON records.
 *
 * <p>Records are delimited by LF only; a CR before the LF stays in the record. A UTF-8 BOM is
 * removed from the first record. A final record without LF is emitted. The size limit is checked
 * against each record's bytes, excluding the LF, and applies to blank records too; an oversized
 * record fails with {@link IllegalArgumentException}.
 */
final class NdjsonFraming {

    private static final byte[] UTF_8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

    private NdjsonFraming() {}

    /**
     * A view of one record's bytes. Valid only until the next record is emitted, so it must be
     * consumed synchronously.
     */
    record RecordBytes(byte[] bytes, int offset, int length) {

        /** True if the record contains only spaces, tabs and CR. */
        boolean isBlank() {
            for (int i = offset; i < offset + length; i++) {
                if (bytes[i] != ' ' && bytes[i] != '\t' && bytes[i] != '\r') {
                    return false;
                }
            }
            return true;
        }

        private RecordBytes withoutBom() {
            var bom = UTF_8_BOM.length;
            if (length >= bom && Arrays.equals(bytes, offset, offset + bom, UTF_8_BOM, 0, bom)) {
                return new RecordBytes(bytes, offset + bom, length - bom);
            }
            return this;
        }
    }

    static Flow<RecordBytes> records(ByteFlow bytes, int maxRecordBytes) {
        return Flows.usingEmit(
                emitRecord -> {
                    var framer = new Framer(maxRecordBytes);
                    bytes.runToEmit(chunk -> framer.split(chunk, emitRecord));
                    framer.emitBuffered(emitRecord);
                });
    }

    private static final class Framer {
        private final int maxRecordBytes;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean firstRecord = true;

        private Framer(int maxRecordBytes) {
            this.maxRecordBytes = maxRecordBytes;
        }

        private void split(ByteChunk chunk, FlowEmit<RecordBytes> emitRecord) throws Exception {
            for (var array : chunk.getArrays()) {
                int recordStart = 0;
                for (int i = 0; i < array.length; i++) {
                    if (array[i] == '\n') {
                        emitRecord.apply(completeRecord(array, recordStart, i - recordStart));
                        recordStart = i + 1;
                    }
                }
                append(array, recordStart, array.length - recordStart);
            }
        }

        private void emitBuffered(FlowEmit<RecordBytes> emitRecord) throws Exception {
            if (buffer.size() > 0) {
                emitRecord.apply(bufferedRecord());
            }
        }

        private void append(byte[] bytes, int offset, int length) {
            requireRecordWithinLimit(length);
            buffer.write(bytes, offset, length);
        }

        // a record within one array is a view of it; only records spanning arrays are copied
        private RecordBytes completeRecord(byte[] lastSegment, int offset, int length) {
            requireRecordWithinLimit(length);
            if (buffer.size() == 0) {
                return nextRecord(lastSegment, offset, length);
            }
            buffer.write(lastSegment, offset, length);
            return bufferedRecord();
        }

        private RecordBytes bufferedRecord() {
            var bytes = buffer.toByteArray();
            buffer.reset();
            return nextRecord(bytes, 0, bytes.length);
        }

        private RecordBytes nextRecord(byte[] bytes, int offset, int length) {
            var record = new RecordBytes(bytes, offset, length);
            if (firstRecord) {
                firstRecord = false;
                return record.withoutBom();
            }
            return record;
        }

        private void requireRecordWithinLimit(int moreBytes) {
            if ((long) buffer.size() + moreBytes > maxRecordBytes) {
                throw new IllegalArgumentException(
                        "NDJSON record exceeds the configured maximum of "
                                + maxRecordBytes
                                + " bytes");
            }
        }
    }
}

package com.softwaremill.jox.json;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;

final class NdjsonFraming {

    private static final byte[] UTF_8_BOM = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};

    private NdjsonFraming() {}

    static Flow<String> lines(ByteFlow bytes, int maxRecordBytes) {
        return bytes.mapWithResource(
                        () -> new State(maxRecordBytes), NdjsonFraming::finish, State::records)
                .mapConcat(records -> records)
                .map(NdjsonFraming::decode);
    }

    private static Optional<Iterable<Record>> finish(State state) {
        return state.finish().map(record -> List.of(record));
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

    private static final class State {
        private final int maxRecordBytes;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean firstRecord = true;

        private State(int maxRecordBytes) {
            this.maxRecordBytes = maxRecordBytes;
        }

        private Iterable<Record> records(ByteChunk chunk) {
            return () ->
                    new Iterator<>() {
                        private final List<byte[]> arrays = chunk.getArrays();
                        private int arrayIndex;
                        private int offset;
                        private Record next;

                        @Override
                        public boolean hasNext() {
                            if (next != null) {
                                return true;
                            }

                            while (arrayIndex < arrays.size()) {
                                var array = arrays.get(arrayIndex);
                                for (int i = offset; i < array.length; i++) {
                                    if (array[i] == '\n') {
                                        append(array, offset, i - offset);
                                        next = completeRecord();
                                        offset = i + 1;
                                        if (offset == array.length) {
                                            arrayIndex++;
                                            offset = 0;
                                        }
                                        return true;
                                    }
                                }

                                append(array, offset, array.length - offset);
                                arrayIndex++;
                                offset = 0;
                            }
                            return false;
                        }

                        @Override
                        public Record next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            var result = next;
                            next = null;
                            return result;
                        }
                    };
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

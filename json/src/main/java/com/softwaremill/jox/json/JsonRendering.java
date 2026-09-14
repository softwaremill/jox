package com.softwaremill.jox.json;

import java.io.ByteArrayOutputStream;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.Flows;

import tools.jackson.databind.ObjectWriter;

final class JsonRendering {

    private static final ByteChunk ARRAY_START = ByteChunk.fromArray(new byte[] {'['});
    private static final ByteChunk ARRAY_END = ByteChunk.fromArray(new byte[] {']'});
    private static final ByteChunk COMMA = ByteChunk.fromArray(new byte[] {','});

    private JsonRendering() {}

    // the JSON and its LF go into one byte array, as sinks write one array at a time: halves the
    // writes of unbuffered sinks
    static <T> ByteFlow renderNdjson(Flow<T> values, ObjectWriter writer) {
        return Flows.<ByteChunk>usingEmit(
                        emit -> {
                            // per run, as the same flow may run concurrently
                            var line = new LineBuffer();
                            values.runToEmit(
                                    value -> {
                                        line.reset();
                                        writer.writeValue(line, requireNonNullValue(value));
                                        line.requireNoLineBreaks();
                                        line.write('\n');
                                        emit.apply(ByteChunk.fromArray(line.toByteArray()));
                                    });
                        })
                .toByteFlow();
    }

    static <T> ByteFlow renderArray(Flow<T> values, ObjectWriter writer) {
        return values.map(
                        value ->
                                ByteChunk.fromArray(
                                        writer.writeValueAsBytes(requireNonNullValue(value))))
                .intersperse(ARRAY_START, COMMA, ARRAY_END)
                .toByteFlow();
    }

    private static <T> T requireNonNullValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Java null cannot be rendered because Jox flows do not support null values");
        }
        return value;
    }

    private static final class LineBuffer extends ByteArrayOutputStream {

        private void requireNoLineBreaks() {
            for (int i = 0; i < count; i++) {
                if (buf[i] == '\r' || buf[i] == '\n') {
                    throw new IllegalArgumentException(
                            "ObjectWriter output contains a raw line break and cannot be rendered"
                                    + " as NDJSON");
                }
            }
        }
    }
}

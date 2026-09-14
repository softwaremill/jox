package com.softwaremill.jox.json;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;

import tools.jackson.databind.ObjectWriter;

final class JsonRendering {

    private static final ByteChunk ARRAY_START = ByteChunk.fromArray(new byte[] {'['});
    private static final ByteChunk ARRAY_END = ByteChunk.fromArray(new byte[] {']'});
    private static final ByteChunk COMMA = ByteChunk.fromArray(new byte[] {','});
    private static final ByteChunk NEW_LINE = ByteChunk.fromArray(new byte[] {'\n'});

    private JsonRendering() {}

    static <T> ByteFlow renderNdjson(Flow<T> values, ObjectWriter writer) {
        return values.map(
                        value -> {
                            var json = writer.writeValueAsBytes(requireNonNullValue(value));
                            requireNoLineBreaks(json);
                            return ByteChunk.fromArray(json).concat(NEW_LINE);
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

    private static void requireNoLineBreaks(byte[] json) {
        for (byte b : json) {
            if (b == '\r' || b == '\n') {
                throw new IllegalArgumentException(
                        "ObjectWriter output contains a raw line break and cannot be rendered as"
                                + " NDJSON");
            }
        }
    }
}

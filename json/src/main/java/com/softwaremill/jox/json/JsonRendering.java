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
        return values.map(writer::writeValueAsBytes)
                .tap(JsonRendering::requireNoLineBreaks)
                .map(json -> ByteChunk.fromArray(json).concat(NEW_LINE))
                .toByteFlow();
    }

    static <T> ByteFlow renderArray(Flow<T> values, ObjectWriter writer) {
        return values.map(value -> ByteChunk.fromArray(writer.writeValueAsBytes(value)))
                .intersperse(ARRAY_START, COMMA, ARRAY_END)
                .toByteFlow();
    }

    private static void requireNoLineBreaks(byte[] json) {
        for (byte value : json) {
            if (value == '\r' || value == '\n') {
                throw new IllegalArgumentException(
                        "ObjectWriter output contains a raw line break and cannot be rendered as"
                                + " NDJSON");
            }
        }
    }
}

package com.softwaremill.jox.json;

import com.softwaremill.jox.flows.ByteChunk;
import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.Flows;

import tools.jackson.databind.ObjectWriter;

final class JsonRendering {

    private static final ByteChunk ARRAY_START = ByteChunk.fromArray(new byte[] {'['});
    private static final ByteChunk ARRAY_END = ByteChunk.fromArray(new byte[] {']'});
    private static final ByteChunk COMMA = ByteChunk.fromArray(new byte[] {','});
    private static final ByteChunk NEW_LINE = ByteChunk.fromArray(new byte[] {'\n'});

    private JsonRendering() {}

    static <T> ByteFlow renderNdjson(Flow<T> values, ObjectWriter writer) {
        return Flows.<ByteChunk>usingEmit(
                        emit ->
                                values.runForeach(
                                        value -> {
                                            byte[] json = writer.writeValueAsBytes(value);
                                            rejectLineBreaks(json);
                                            emit.apply(ByteChunk.fromArray(json).concat(NEW_LINE));
                                        }))
                .toByteFlow();
    }

    static <T> ByteFlow renderArray(Flow<T> values, ObjectWriter writer) {
        return Flows.<ByteChunk>usingEmit(
                        emit -> {
                            emit.apply(ARRAY_START);
                            boolean[] first = {true};
                            values.runForeach(
                                    value -> {
                                        ByteChunk json =
                                                ByteChunk.fromArray(
                                                        writer.writeValueAsBytes(value));
                                        emit.apply(first[0] ? json : COMMA.concat(json));
                                        first[0] = false;
                                    });
                            emit.apply(ARRAY_END);
                        })
                .toByteFlow();
    }

    private static void rejectLineBreaks(byte[] json) {
        for (byte value : json) {
            if (value == '\r' || value == '\n') {
                throw new IllegalArgumentException(
                        "ObjectWriter output contains a raw line break and cannot be rendered as"
                                + " NDJSON");
            }
        }
    }
}

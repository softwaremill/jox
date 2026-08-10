package com.softwaremill.jox.json;

import static com.softwaremill.jox.structured.Scopes.supervised;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.Flows;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;

final class JsonParsing {

    private JsonParsing() {}

    static <T> Flow<T> parseNdjson(ByteFlow bytes, ObjectReader reader) {
        var singleValueReader = reader.with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return bytes.linesUtf8()
                .filter(line -> !line.isBlank())
                .map(line -> requireNonNullValue(singleValueReader.<T>readValue(line)));
    }

    static <T> Flow<T> parseArray(ByteFlow bytes, ObjectReader reader) {
        var elementReader = reader.without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return Flows.usingEmit(
                emit ->
                        supervised(
                                scope -> {
                                    try (var inputStream = bytes.runToInputStream(scope);
                                            JsonParser parser =
                                                    elementReader.createParser(inputStream)) {
                                        requireToken(
                                                parser.nextToken(),
                                                JsonToken.START_ARRAY,
                                                "Expected one top-level JSON array");

                                        JsonToken token;
                                        while ((token = parser.nextToken())
                                                != JsonToken.END_ARRAY) {
                                            if (token == null) {
                                                throw new IllegalArgumentException(
                                                        "Unexpected end of input while parsing the"
                                                                + " top-level JSON array");
                                            }
                                            emit.apply(
                                                    requireNonNullValue(
                                                            elementReader.<T>readValue(parser)));
                                        }

                                        if (parser.nextToken() != null) {
                                            throw new IllegalArgumentException(
                                                    "Unexpected content after the top-level JSON"
                                                            + " array");
                                        }
                                    }
                                    return null;
                                }));
    }

    private static <T> T requireNonNullValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "JSON null cannot be emitted because Jox flows do not support null values");
        }
        return value;
    }

    private static void requireToken(JsonToken actual, JsonToken expected, String message) {
        if (actual != expected) {
            throw new IllegalArgumentException(message);
        }
    }
}

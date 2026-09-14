package com.softwaremill.jox.json;

import static com.softwaremill.jox.structured.Scopes.supervised;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.flows.FlowEmit;
import com.softwaremill.jox.flows.Flows;
import com.softwaremill.jox.structured.Scope;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;

final class JsonParsing {

    private JsonParsing() {}

    static <T> Flow<T> parseNdjson(
            ByteFlow bytes, ObjectReader reader, NdjsonReadSettings settings) {
        // one record is one complete value, so anything after it is an error
        var recordReader = reader.with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return NdjsonFraming.records(bytes, settings.maxRecordBytes())
                .filter(record -> !record.isBlank())
                .map(
                        record ->
                                requireNonNullValue(
                                        recordReader.<T>readValue(
                                                record.bytes(), record.offset(), record.length())));
    }

    static <T> Flow<T> parseArray(ByteFlow bytes, ObjectReader reader) {
        // the rest of the array follows each element, so trailing tokens are expected
        var elementReader = reader.without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return Flows.usingEmit(
                emit ->
                        supervised(
                                scope -> {
                                    emitArrayElements(bytes, elementReader, scope, emit);
                                    return null;
                                }));
    }

    private static <T> void emitArrayElements(
            ByteFlow bytes, ObjectReader elementReader, Scope scope, FlowEmit<T> emit)
            throws Exception {
        try (var inputStream = bytes.runToInputStream(scope);
                JsonParser parser = elementReader.createParser(inputStream)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("Expected one top-level JSON array");
            }
            // inside the array, a premature end of input makes nextToken() throw rather than
            // return null
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                emit.apply(requireNonNullValue(elementReader.<T>readValue(parser)));
            }
            if (parser.nextToken() != null) {
                throw new IllegalArgumentException(
                        "Unexpected content after the top-level JSON array");
            }
        }
    }

    private static <T> T requireNonNullValue(T value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "JSON null cannot be emitted because Jox flows do not support null values");
        }
        return value;
    }
}

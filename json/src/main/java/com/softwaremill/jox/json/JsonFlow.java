package com.softwaremill.jox.json;

import java.util.Objects;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

/**
 * Creates flows which parse or render newline-delimited JSON (NDJSON) and top-level JSON arrays.
 *
 * <p>All transformations are lazy and preserve the backpressure and cancellation behavior of the
 * supplied flow. Values are parsed or rendered one at a time.
 */
public final class JsonFlow {

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    private JsonFlow() {}

    /**
     * Parses newline-delimited JSON using a default {@link ObjectMapper}. Empty and whitespace-only
     * lines are ignored. Both LF and CRLF line endings are accepted, as is a final record without a
     * line ending.
     *
     * @param bytes the UTF-8 encoded NDJSON
     * @param valueType the type of each parsed value
     * @param <T> the type of parsed values
     * @return a flow emitting one value for each non-blank input line
     */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, Class<T> valueType) {
        return parseNdjson(bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Parses newline-delimited JSON using a default {@link ObjectMapper}. Empty and whitespace-only
     * lines are ignored. Both LF and CRLF line endings are accepted, as is a final record without a
     * line ending.
     *
     * @param bytes the UTF-8 encoded NDJSON
     * @param valueType the generic type of each parsed value
     * @param <T> the type of parsed values
     * @return a flow emitting one value for each non-blank input line
     */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, TypeReference<T> valueType) {
        return parseNdjson(bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Parses newline-delimited JSON using the supplied Jackson reader. Empty and whitespace-only
     * lines are ignored. Both LF and CRLF line endings are accepted, as is a final record without a
     * line ending. Each non-blank line must contain exactly one JSON value.
     *
     * @param bytes the UTF-8 encoded NDJSON
     * @param reader the reader used to deserialize each value
     * @param <T> the type of parsed values
     * @return a flow emitting one value for each non-blank input line
     */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, ObjectReader reader) {
        return JsonParsing.parseNdjson(
                Objects.requireNonNull(bytes, "bytes"), Objects.requireNonNull(reader, "reader"));
    }

    /**
     * Parses a top-level JSON array using a default {@link ObjectMapper}. The returned flow emits
     * each array element as soon as it is available. Input that is not one complete top-level
     * array, including input containing trailing JSON, fails the flow.
     *
     * @param bytes the UTF-8 encoded JSON array
     * @param valueType the type of each array element
     * @param <T> the type of parsed values
     * @return a flow emitting the array elements
     */
    public static <T> Flow<T> parseArray(ByteFlow bytes, Class<T> valueType) {
        return parseArray(bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Parses a top-level JSON array using a default {@link ObjectMapper}. The returned flow emits
     * each array element as soon as it is available. Input that is not one complete top-level
     * array, including input containing trailing JSON, fails the flow.
     *
     * @param bytes the UTF-8 encoded JSON array
     * @param valueType the generic type of each array element
     * @param <T> the type of parsed values
     * @return a flow emitting the array elements
     */
    public static <T> Flow<T> parseArray(ByteFlow bytes, TypeReference<T> valueType) {
        return parseArray(bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Parses a top-level JSON array using the supplied Jackson reader. The returned flow emits each
     * array element as soon as it is available. Input that is not one complete top-level array,
     * including input containing trailing JSON, fails the flow.
     *
     * @param bytes the UTF-8 encoded JSON array
     * @param reader the reader used to deserialize each array element
     * @param <T> the type of parsed values
     * @return a flow emitting the array elements
     */
    public static <T> Flow<T> parseArray(ByteFlow bytes, ObjectReader reader) {
        return JsonParsing.parseArray(
                Objects.requireNonNull(bytes, "bytes"), Objects.requireNonNull(reader, "reader"));
    }

    /**
     * Renders values as newline-delimited JSON using a default {@link ObjectMapper}. Every value is
     * followed by an LF byte, including the final value.
     *
     * @param values the values to render
     * @param valueType the type of each value
     * @param <T> the type of rendered values
     * @return a flow emitting UTF-8 encoded NDJSON
     */
    public static <T> ByteFlow renderNdjson(Flow<T> values, Class<T> valueType) {
        return renderNdjson(values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as newline-delimited JSON using a default {@link ObjectMapper}. Every value is
     * followed by an LF byte, including the final value.
     *
     * @param values the values to render
     * @param valueType the generic type of each value
     * @param <T> the type of rendered values
     * @return a flow emitting UTF-8 encoded NDJSON
     */
    public static <T> ByteFlow renderNdjson(Flow<T> values, TypeReference<T> valueType) {
        return renderNdjson(values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as newline-delimited JSON using the supplied Jackson writer. Every value is
     * followed by an LF byte, including the final value. Writer output containing a raw CR or LF
     * byte is rejected, as it would produce invalid NDJSON records.
     *
     * @param values the values to render
     * @param writer the writer used to serialize each value
     * @param <T> the type of rendered values
     * @return a flow emitting UTF-8 encoded NDJSON
     */
    public static <T> ByteFlow renderNdjson(Flow<T> values, ObjectWriter writer) {
        return JsonRendering.renderNdjson(
                Objects.requireNonNull(values, "values"), Objects.requireNonNull(writer, "writer"));
    }

    /**
     * Renders values as one JSON array using a default {@link ObjectMapper}. Elements are
     * serialized one at a time. An empty input flow produces {@code []}.
     *
     * @param values the values to render
     * @param valueType the type of each array element
     * @param <T> the type of rendered values
     * @return a flow emitting one UTF-8 encoded JSON array
     */
    public static <T> ByteFlow renderArray(Flow<T> values, Class<T> valueType) {
        return renderArray(values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as one JSON array using a default {@link ObjectMapper}. Elements are
     * serialized one at a time. An empty input flow produces {@code []}.
     *
     * @param values the values to render
     * @param valueType the generic type of each array element
     * @param <T> the type of rendered values
     * @return a flow emitting one UTF-8 encoded JSON array
     */
    public static <T> ByteFlow renderArray(Flow<T> values, TypeReference<T> valueType) {
        return renderArray(values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as one JSON array using the supplied Jackson writer. Elements are serialized
     * one at a time. An empty input flow produces {@code []}.
     *
     * @param values the values to render
     * @param writer the writer used to serialize each array element
     * @param <T> the type of rendered values
     * @return a flow emitting one UTF-8 encoded JSON array
     */
    public static <T> ByteFlow renderArray(Flow<T> values, ObjectWriter writer) {
        return JsonRendering.renderArray(
                Objects.requireNonNull(values, "values"), Objects.requireNonNull(writer, "writer"));
    }
}

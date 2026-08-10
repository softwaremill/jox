package com.softwaremill.jox.json;

import java.util.Objects;

import com.softwaremill.jox.flows.Flow;
import com.softwaremill.jox.flows.Flow.ByteFlow;
import com.softwaremill.jox.structured.JoxScopeExecutionException;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;

/**
 * Creates flows which parse or render newline-delimited JSON (NDJSON) and top-level JSON arrays.
 *
 * <p>All transformations are lazy and preserve the backpressure and cancellation behavior of the
 * supplied flow. Values are parsed or rendered one at a time. Parsing fails when Jackson
 * deserializes an NDJSON record or array element as {@code null}, and rendering fails on a raw Java
 * {@code null}, as Jox flows do not support null elements. Use Jackson's tree model to represent a
 * JSON {@code null} as a non-null node.
 */
public final class JsonFlow {

    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    private JsonFlow() {}

    /** Parses NDJSON using the default mapper and settings. */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, Class<T> valueType) {
        return parseNdjson(bytes, valueType, JsonReadSettings.defaults());
    }

    /** Parses NDJSON using the default mapper and the supplied settings. */
    public static <T> Flow<T> parseNdjson(
            ByteFlow bytes, Class<T> valueType, JsonReadSettings settings) {
        return parseNdjson(
                bytes,
                DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")),
                settings);
    }

    /** Parses generic NDJSON values using the default mapper and settings. */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, TypeReference<T> valueType) {
        return parseNdjson(bytes, valueType, JsonReadSettings.defaults());
    }

    /** Parses generic NDJSON values using the default mapper and the supplied settings. */
    public static <T> Flow<T> parseNdjson(
            ByteFlow bytes, TypeReference<T> valueType, JsonReadSettings settings) {
        return parseNdjson(
                bytes,
                DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")),
                settings);
    }

    /** Parses NDJSON using the supplied reader and default settings. */
    public static <T> Flow<T> parseNdjson(ByteFlow bytes, ObjectReader reader) {
        return parseNdjson(bytes, reader, JsonReadSettings.defaults());
    }

    /**
     * Parses UTF-8 NDJSON using the supplied reader and framing settings. Blank lines are ignored;
     * LF, CRLF, a final unterminated record and one initial byte-order mark are accepted. {@link
     * tools.jackson.databind.DeserializationFeature#FAIL_ON_TRAILING_TOKENS} is enabled regardless
     * of the reader's configuration. The caller must ensure that {@code T} matches the type
     * configured on the reader.
     *
     * @param bytes the UTF-8 encoded NDJSON
     * @param reader the reader used to deserialize each value
     * @param settings the NDJSON framing settings
     * @param <T> the type of parsed values
     * @return a flow emitting one value for each non-blank input line
     */
    public static <T> Flow<T> parseNdjson(
            ByteFlow bytes, ObjectReader reader, JsonReadSettings settings) {
        return JsonParsing.parseNdjson(
                Objects.requireNonNull(bytes, "bytes"),
                Objects.requireNonNull(reader, "reader"),
                Objects.requireNonNull(settings, "settings"));
    }

    /** Parses a top-level JSON array using the default mapper. */
    public static <T> Flow<T> parseArray(ByteFlow bytes, Class<T> valueType) {
        return parseArray(
                bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /** Parses generic elements from a top-level JSON array using the default mapper. */
    public static <T> Flow<T> parseArray(ByteFlow bytes, TypeReference<T> valueType) {
        return parseArray(
                bytes, DEFAULT_MAPPER.readerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Incrementally parses exactly one top-level JSON array using the supplied reader. Successful
     * completion waits for end-of-input to reject trailing content. {@link
     * tools.jackson.databind.DeserializationFeature#FAIL_ON_TRAILING_TOKENS} is disabled while
     * reading individual elements, regardless of the reader's configuration. The caller must ensure
     * that {@code T} matches the type configured on the reader. Failures are wrapped in {@link
     * JoxScopeExecutionException}.
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

    /** Renders values as NDJSON using the default mapper. */
    public static <T> ByteFlow renderNdjson(Flow<T> values, Class<T> valueType) {
        return renderNdjson(
                values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /** Renders generic values as NDJSON using the default mapper. */
    public static <T> ByteFlow renderNdjson(Flow<T> values, TypeReference<T> valueType) {
        return renderNdjson(
                values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as UTF-8 NDJSON using the supplied writer. Every value, including the final
     * one, is followed by LF. Writer output containing raw CR or LF is rejected.
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

    /** Renders values as one JSON array using the default mapper. */
    public static <T> ByteFlow renderArray(Flow<T> values, Class<T> valueType) {
        return renderArray(
                values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /** Renders generic values as one JSON array using the default mapper. */
    public static <T> ByteFlow renderArray(Flow<T> values, TypeReference<T> valueType) {
        return renderArray(
                values, DEFAULT_MAPPER.writerFor(Objects.requireNonNull(valueType, "valueType")));
    }

    /**
     * Renders values as one UTF-8 JSON array using the supplied writer. An empty flow produces
     * {@code []}; a failed flow can leave an incomplete array.
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

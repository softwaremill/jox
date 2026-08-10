package com.softwaremill.jox.json;

/**
 * Settings used when parsing NDJSON flows.
 *
 * @param maxNdjsonRecordBytes maximum UTF-8 encoded size of one NDJSON record, excluding the LF
 *     delimiter; must be positive
 */
public record JsonReadSettings(int maxNdjsonRecordBytes) {

    private static final int DEFAULT_MAX_NDJSON_RECORD_BYTES = 32 * 1024 * 1024;

    /**
     * @throws IllegalArgumentException if {@code maxNdjsonRecordBytes} is not positive
     */
    public JsonReadSettings {
        if (maxNdjsonRecordBytes <= 0) {
            throw new IllegalArgumentException("maxNdjsonRecordBytes must be greater than zero");
        }
    }

    /** Returns settings with a 32 MiB maximum encoded NDJSON record size. */
    public static JsonReadSettings defaults() {
        return new JsonReadSettings(DEFAULT_MAX_NDJSON_RECORD_BYTES);
    }

    /**
     * Returns a copy with the given maximum UTF-8 encoded NDJSON record size.
     *
     * @throws IllegalArgumentException if {@code newMaxNdjsonRecordBytes} is not positive
     */
    public JsonReadSettings maxNdjsonRecordBytes(int newMaxNdjsonRecordBytes) {
        return new JsonReadSettings(newMaxNdjsonRecordBytes);
    }
}

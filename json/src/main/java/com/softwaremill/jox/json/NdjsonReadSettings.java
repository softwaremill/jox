package com.softwaremill.jox.json;

/**
 * Settings used when parsing NDJSON flows.
 *
 * @param maxRecordBytes maximum UTF-8 encoded size of one NDJSON record, excluding the LF
 *     delimiter; applies to blank lines as well; must be positive
 */
public record NdjsonReadSettings(int maxRecordBytes) {

    private static final int DEFAULT_MAX_RECORD_BYTES = 32 * 1024 * 1024;

    public NdjsonReadSettings {
        if (maxRecordBytes <= 0) {
            throw new IllegalArgumentException("maxRecordBytes must be greater than zero");
        }
    }

    /** Returns settings with a 32 MiB maximum encoded NDJSON record size. */
    public static NdjsonReadSettings defaults() {
        return new NdjsonReadSettings(DEFAULT_MAX_RECORD_BYTES);
    }
}

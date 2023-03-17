package com.arangodb.util;

/**
 * Wrapper for raw data, current implementations are:
 * - {@link RawBytes}
 * - {@link RawJson}
 */
public interface RawData<T> {
    static RawJson of(final String value) {
        return RawJson.of(value);
    }

    static RawBytes of(final byte[] value) {
        return RawBytes.of(value);
    }

    T get();
}

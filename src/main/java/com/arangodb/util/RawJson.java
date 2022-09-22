package com.arangodb.util;

import com.arangodb.internal.serde.InternalSerde;

import java.util.Objects;

/**
 * Helper class used to encapsulate raw JSON string.
 * It can be used:
 * - in serialization to append a raw JSON node
 * - in deserialization as target wrapper type for the raw JSON string
 * <p>
 * The driver's {@link InternalSerde} supports serializing and deserializing to and from
 * {@code RawJson}.
 */
public final class RawJson implements RawData {
    private final String value;

    private RawJson(final String value) {
        this.value = value;
    }

    public static RawJson of(final String value) {
        return new RawJson(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawJson rawJson = (RawJson) o;
        return Objects.equals(getValue(), rawJson.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}

package com.arangodb.util;

import com.arangodb.internal.serde.InternalSerde;

import java.util.Arrays;

/**
 * Helper class used to encapsulate raw value serialized as byte array.
 * It can be used:
 * - in serialization to append an already serialized raw value as is
 * - in deserialization as target wrapper type for the raw value
 * <p>
 * No validation is performed, the user is responsible for providing a valid byte array for the used content type.
 * <p>
 * The raw value byte array can represent either:
 * - a valid VPack
 * - a valid JSON UTF-8 encoded string
 * <p>
 * The driver's {@link InternalSerde} supports serializing and deserializing to and from
 * {@code RawBytes}.
 */
public final class RawBytes implements RawData {
    private final byte[] value;

    private RawBytes(final byte[] value) {
        this.value = value;
    }

    public static RawBytes of(final byte[] value) {
        return new RawBytes(value);
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawBytes rawBytes = (RawBytes) o;
        return Arrays.equals(getValue(), rawBytes.getValue());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getValue());
    }
}

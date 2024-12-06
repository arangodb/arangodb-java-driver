package com.arangodb.internal.serde;

import com.fasterxml.jackson.core.SerializableString;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class RawUserDataValue implements SerializableString {
    private final byte[] data;

    RawUserDataValue(byte[] data) {
        this.data = data;
    }

    @Override
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int charLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public char[] asQuotedChars() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] asUnquotedUTF8() {
        return data;
    }

    @Override
    public byte[] asQuotedUTF8() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int appendQuotedUTF8(byte[] buffer, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int appendQuoted(char[] buffer, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int appendUnquotedUTF8(byte[] buffer, int offset) {
        final int length = data.length;
        if ((offset + length) > buffer.length) {
            return -1;
        }
        System.arraycopy(data, 0, buffer, offset, length);
        return length;
    }

    @Override
    public int appendUnquoted(char[] buffer, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeQuotedUTF8(OutputStream out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeUnquotedUTF8(OutputStream out) throws IOException {
        final int length = data.length;
        out.write(data, 0, length);
        return length;
    }

    @Override
    public int putQuotedUTF8(ByteBuffer buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int putUnquotedUTF8(ByteBuffer buffer) {
        final int length = data.length;
        if (length > buffer.remaining()) {
            return -1;
        }
        buffer.put(data, 0, length);
        return length;
    }
}

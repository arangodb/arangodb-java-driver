package com.arangodb.http.compression;

import io.vertx.core.buffer.Buffer;

class NoopEncoder implements Encoder {
    @Override
    public Buffer encode(byte[] data) {
        return Buffer.buffer(data);
    }

    @Override
    public String getFormat() {
        return null;
    }
}

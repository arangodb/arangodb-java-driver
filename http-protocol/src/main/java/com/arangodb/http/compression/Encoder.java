package com.arangodb.http.compression;

import com.arangodb.Compression;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.vertx.core.buffer.Buffer;

public interface Encoder {
    Buffer encode(byte[] data);

    String getFormat();

    static Encoder of(Compression compression, int level) {
        if (level < 0 || level > 9) {
            throw new IllegalArgumentException("compression level: " + level + " (expected: 0-9)");
        }

        switch (compression) {
            case GZIP:
                return new ZlibEncoder(ZlibWrapper.GZIP, level, "gzip");
            case DEFLATE:
                return new ZlibEncoder(ZlibWrapper.ZLIB, level, "deflate");
            case NONE:
                return new NoopEncoder();
            default:
                throw new IllegalArgumentException("Unsupported compression: " + compression);
        }
    }
}

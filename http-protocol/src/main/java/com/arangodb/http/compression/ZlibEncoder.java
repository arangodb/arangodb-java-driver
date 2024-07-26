package com.arangodb.http.compression;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.vertx.core.buffer.Buffer;

class ZlibEncoder implements Encoder {
    private final ZlibWrapper wrapper;
    private final int level;
    private final String format;

    ZlibEncoder(ZlibWrapper wrapper, int level, String format) {
        this.wrapper = wrapper;
        this.level = level;
        this.format = format;
    }

    @Override
    public Buffer encode(byte[] data) {
        JdkZlibEncoder encoder = new JdkZlibEncoder(wrapper, level);
        ByteBuf bb = encoder.encode(data);
        Buffer out = Buffer.buffer(bb);
        encoder.close();
        return out;
    }

    @Override
    public String getFormat() {
        return format;
    }
}

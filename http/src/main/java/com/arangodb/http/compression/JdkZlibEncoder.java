/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.arangodb.http.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.compression.CompressionException;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.util.internal.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * Compresses a {@link ByteBuf} using the deflate algorithm.
 */
class JdkZlibEncoder {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JdkZlibEncoder.class);

    /**
     * Maximum initial size for temporary heap buffers used for the compressed output. Buffer may still grow beyond
     * this if necessary.
     */
    private static final int MAX_INITIAL_OUTPUT_BUFFER_SIZE;
    /**
     * Max size for temporary heap buffers used to copy input data to heap.
     */
    private static final int MAX_INPUT_BUFFER_SIZE;
    private static final ByteBuf EMPTY_BUF;

    private final ZlibWrapper wrapper;
    private final Deflater deflater;

    /*
     * GZIP support
     */
    private final CRC32 crc = new CRC32();
    private static final byte[] gzipHeader = {0x1f, (byte) 0x8b, Deflater.DEFLATED, 0, 0, 0, 0, 0, 0, 0};

    static {
        MAX_INITIAL_OUTPUT_BUFFER_SIZE = SystemPropertyUtil.getInt(
                "io.netty.jdkzlib.encoder.maxInitialOutputBufferSize",
                65536);
        MAX_INPUT_BUFFER_SIZE = SystemPropertyUtil.getInt(
                "io.netty.jdkzlib.encoder.maxInputBufferSize",
                65536);

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.jdkzlib.encoder.maxInitialOutputBufferSize={}", MAX_INITIAL_OUTPUT_BUFFER_SIZE);
            logger.debug("-Dio.netty.jdkzlib.encoder.maxInputBufferSize={}", MAX_INPUT_BUFFER_SIZE);
        }

        EMPTY_BUF = allocateByteBuf(0);
    }

    private static ByteBuf allocateByteBuf(int len) {
        return ByteBufAllocator.DEFAULT.heapBuffer(len);
    }

    private static ByteBuf allocateByteBuf() {
        return ByteBufAllocator.DEFAULT.heapBuffer();
    }


    /**
     * Creates a new zlib encoder with the specified {@code compressionLevel}
     * and the specified wrapper.
     *
     * @param compressionLevel {@code 1} yields the fastest compression and {@code 9} yields the
     *                         best compression.  {@code 0} means no compression.  The default
     *                         compression level is {@code 6}.
     * @throws CompressionException if failed to initialize zlib
     */
    JdkZlibEncoder(ZlibWrapper wrapper, int compressionLevel) {
        ObjectUtil.checkInRange(compressionLevel, 0, 9, "compressionLevel");
        ObjectUtil.checkNotNull(wrapper, "wrapper");

        if (wrapper == ZlibWrapper.ZLIB_OR_NONE) {
            throw new IllegalArgumentException(
                    "wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not " +
                            "allowed for compression.");
        }

        this.wrapper = wrapper;
        deflater = new Deflater(compressionLevel, wrapper != ZlibWrapper.ZLIB);
    }

    ByteBuf encode(byte[] in) {
        if (in.length == 0) {
            return EMPTY_BUF;
        }

        ByteBuf out = allocateBuffer(in.length);
        encodeSome(in, out);
        finishEncode(out);
        return out;
    }

    private void encodeSome(byte[] in, ByteBuf out) {
        if (wrapper == ZlibWrapper.GZIP) {
            out.writeBytes(gzipHeader);
        }
        if (wrapper == ZlibWrapper.GZIP) {
            crc.update(in, 0, in.length);
        }

        deflater.setInput(in);
        for (; ; ) {
            deflate(out);
            if (!out.isWritable()) {
                out.ensureWritable(out.writerIndex());
            } else if (deflater.needsInput()) {
                break;
            }
        }
    }

    private ByteBuf allocateBuffer(int length) {
        int sizeEstimate = (int) Math.ceil(length * 1.001) + 12;
        switch (wrapper) {
            case GZIP:
                sizeEstimate += gzipHeader.length;
                break;
            case ZLIB:
                sizeEstimate += 2; // first two magic bytes
                break;
            default:
                throw new IllegalArgumentException();
        }
        // sizeEstimate might overflow if close to 2G
        if (sizeEstimate < 0 || sizeEstimate > MAX_INITIAL_OUTPUT_BUFFER_SIZE) {
            // can always expand later
            return allocateByteBuf(MAX_INITIAL_OUTPUT_BUFFER_SIZE);
        }
        return allocateByteBuf(sizeEstimate);
    }

    private void finishEncode(ByteBuf out) {
        ByteBuf footer = allocateByteBuf();
        deflater.finish();
        while (!deflater.finished()) {
            deflate(footer);
        }
        if (wrapper == ZlibWrapper.GZIP) {
            int crcValue = (int) crc.getValue();
            int uncBytes = deflater.getTotalIn();
            footer.writeByte(crcValue);
            footer.writeByte(crcValue >>> 8);
            footer.writeByte(crcValue >>> 16);
            footer.writeByte(crcValue >>> 24);
            footer.writeByte(uncBytes);
            footer.writeByte(uncBytes >>> 8);
            footer.writeByte(uncBytes >>> 16);
            footer.writeByte(uncBytes >>> 24);
        }
        out.writeBytes(footer);
        deflater.reset();
        crc.reset();
    }

    private void deflate(ByteBuf out) {
        int numBytes;
        do {
            int writerIndex = out.writerIndex();
            numBytes = deflater.deflate(
                    out.array(), out.arrayOffset() + writerIndex, out.writableBytes(), Deflater.SYNC_FLUSH);
            out.writerIndex(writerIndex + numBytes);
        } while (numBytes > 0);
    }

    void close() {
        deflater.reset();
        deflater.end();
    }
}

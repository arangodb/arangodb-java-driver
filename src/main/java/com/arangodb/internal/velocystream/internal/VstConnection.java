/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.internal.velocystream.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.velocypack.VPackSlice;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static com.arangodb.internal.ArangoDefaults.*;
import static io.netty.buffer.Unpooled.*;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class VstConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
    private static final byte[] PROTOCOL_HEADER = "VST/1.0\r\n\r\n".getBytes();
    private static final int DEFAULT_INITIAL_CAPACITY = 256;

    protected final MessageStore messageStore;

    protected final Integer timeout;
    private final Long ttl;
    private final Boolean useSsl;
    private final SSLContext sslContext;

    private final HostDescription host;

    private final HashMap<Long, Long> sendTimestamps = new HashMap<>();

    private final ConnectionProvider connectionProvider;

    private final String connectionName;
    private final ArangoTcpClient arangoTcpClient;

    private volatile ChunkStore chunkStore;

    protected VstConnection(final HostDescription host, final Integer timeout, final Long ttl, final Boolean useSsl,
                            final SSLContext sslContext, final MessageStore messageStore) {
        super();
        this.host = host;
        this.timeout = timeout;
        this.ttl = ttl;
        this.useSsl = useSsl;
        this.sslContext = sslContext;
        this.messageStore = messageStore;

        this.connectionProvider = initConnectionProvider();

        connectionName = "conenction_" + System.currentTimeMillis() + "_" + Math.random();
        LOGGER.debug("Connection " + connectionName + " created");

        arangoTcpClient = new ArangoTcpClient();
        chunkStore = new ChunkStore(messageStore);
    }

    private ByteBuf createBuffer() {
        return createBuffer(DEFAULT_INITIAL_CAPACITY);
    }

    private ByteBuf createBuffer(int initialCapacity) {
        return PooledByteBufAllocator.DEFAULT.directBuffer(initialCapacity);
    }

    private ConnectionProvider initConnectionProvider() {
        return ConnectionProvider.fixed(
                "tcp",
                1,
                DEFAULT_POOL_ACQUIRE_TIMEOUT,
                ttl != null ? Duration.ofMillis(ttl) : null);
    }

    public boolean isOpen() {
        return arangoTcpClient.isActive();
    }

    public synchronized void open() throws IOException {
        if (isOpen()) {
            return;
        }
        new Thread(arangoTcpClient::connect).start();
        // wait for connection
        arangoTcpClient.getConnectedFuture().join();
    }

    @Override
    public synchronized void close() {
        arangoTcpClient.disconnect();
    }

    protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks) throws ArangoDBException {
        for (final Chunk chunk : chunks) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Send chunk %s:%s from message %s", chunk.getChunk(),
                        chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
                sendTimestamps.put(chunk.getMessageId(), System.currentTimeMillis());
            }

            ByteBuf out = writeChunkHead(chunk);

            final int contentOffset = chunk.getContentOffset();
            final int contentLength = chunk.getContentLength();
            final VPackSlice head = message.getHead();
            final int headLength = head.getByteSize();
            int headBytes = Math.min(contentLength, headLength - contentOffset);
            if (contentOffset < headLength) {
                out.writeBytes(head.getBuffer(), contentOffset, headBytes);
            }
            final VPackSlice body = message.getBody();
            if (body != null) {
                out.writeBytes(body.getBuffer(), contentOffset + headBytes - headLength, contentLength - headBytes);
            }
            arangoTcpClient.send(out);
        }
    }

    private synchronized ByteBuf writeChunkHead(final Chunk chunk) {
        final long messageLength = chunk.getMessageLength();
        final int headLength = messageLength > -1L ? CHUNK_MAX_HEADER_SIZE
                : CHUNK_MIN_HEADER_SIZE;
        final int length = chunk.getContentLength() + headLength;
        final ByteBuf buffer = createBuffer(headLength);

        buffer.writeIntLE(length);
        buffer.writeIntLE(chunk.getChunkX());
        buffer.writeLongLE(chunk.getMessageId());
        if (messageLength > -1L) {
            buffer.writeLongLE(messageLength);
        }
        return buffer;
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    private class ArangoTcpClient {
        private volatile NettyOutbound outbound;
        private volatile reactor.netty.Connection connection;
        private TcpClient tcpClient;
        private volatile Chunk chunk;
        private volatile ByteBuf chunkHeaderBuffer = createBuffer();
        private volatile ByteBuf chunkContentBuffer = createBuffer();
        private volatile CompletableFuture<Void> connectedFuture = new CompletableFuture<>();

        private void setConnection(reactor.netty.Connection connection) {
            this.connection = connection;
        }

        void send(ByteBuf buf) {
            outbound
                    .send(Mono.just(buf))
                    // FIXME: catch errors
                    .then().subscribe();
        }

        ArangoTcpClient() {
            tcpClient = TcpClient.create(connectionProvider)
                    .host("127.0.0.1")
                    .port(8529)
                    .doOnDisconnected(c -> messageStore.clear(new IOException("Connection closed!")))
                    .handle((i, o) -> {
                        outbound = o;
                        i.receive()
                                .doOnNext(x -> {
                                    try {
                                        while (x.readableBytes() > 0) {
                                            handleByteBuf(x);
                                        }
                                    } catch (IOException e) {
                                        messageStore.clear(e);
                                        close();
                                    }
                                })
                                .subscribe();
                        return Mono.never();
                    })
                    .doOnConnected(c -> {
                        setConnection(c);
                        connectedFuture.complete(null);
                        send(wrappedBuffer(PROTOCOL_HEADER));
                    });
        }

        private void readBytes(ByteBuf bbIn, ByteBuf out, int len) {
            int bytesToRead = Integer.min(len, bbIn.readableBytes());
            out.ensureWritable(bytesToRead);
            bbIn.readBytes(out, bytesToRead);
        }

        private void handleByteBuf(ByteBuf bbIn) throws IOException {
            // new chunk
            if (chunk == null) {
                int missingBytes = CHUNK_MIN_HEADER_SIZE - chunkHeaderBuffer.readableBytes();
                readBytes(bbIn, chunkHeaderBuffer, missingBytes);
                if (chunkHeaderBuffer.readableBytes() >= CHUNK_MIN_HEADER_SIZE) {
                    readHeader(bbIn);
                }
            }

            if (chunk != null) {
                int missingContentBytes = chunk.getContentLength() - chunkContentBuffer.readableBytes();
                readBytes(bbIn, chunkContentBuffer, missingContentBytes);

                // chunkContent completely received
                if (chunkContentBuffer.readableBytes() == chunk.getContentLength()) {
                    readContent();
                }
            }
        }

        private void readHeader(ByteBuf bbIn) {
            final int chunkLength = chunkHeaderBuffer.getIntLE(0);
            final int chunkX = chunkHeaderBuffer.getIntLE(INTEGER_BYTES);
            final long messageId = chunkHeaderBuffer.getLongLE(2 * INTEGER_BYTES);
            final long messageLength;
            final int contentLength;
            if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
                readBytes(bbIn, chunkHeaderBuffer, LONG_BYTES);
                if (chunkHeaderBuffer.readableBytes() < CHUNK_MAX_HEADER_SIZE)
                    return;
                messageLength = chunkHeaderBuffer.getLongLE(CHUNK_MIN_HEADER_SIZE);
                contentLength = chunkLength - CHUNK_MAX_HEADER_SIZE;
            } else {
                messageLength = -1L;
                contentLength = chunkLength - CHUNK_MIN_HEADER_SIZE;
            }
            chunk = new Chunk(messageId, chunkX, messageLength, 0, contentLength);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(), chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
                LOGGER.debug("Responsetime for Message " + chunk.getMessageId() + " is " + (sendTimestamps.get(chunk.getMessageId()) - System.currentTimeMillis()));
            }
        }

        private void readContent() throws IOException {
            byte[] buf = new byte[chunkContentBuffer.readableBytes()];
            chunkContentBuffer.readBytes(buf);
            chunkStore.storeChunk(chunk, buf);

            chunkHeaderBuffer.clear();
            chunkContentBuffer.clear();

            chunk = null;
        }

        void connect() {
            tcpClient
                    .connectNow()
                    .onDispose()
                    .block();
        }

        void disconnect() {
            connection.dispose();
        }

        boolean isActive() {
            return connection != null && connection.channel().isActive();
        }

        CompletableFuture<Void> getConnectedFuture() {
            return connectedFuture;
        }
    }
}

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
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.velocypack.VPackSlice;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static com.arangodb.internal.ArangoDefaults.LONG_BYTES;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class VstConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
    private static final byte[] PROTOCOL_HEADER = "VST/1.0\r\n\r\n".getBytes();

    protected final MessageStore messageStore;

    protected final Integer timeout;
    private final Long ttl;
    private final Boolean useSsl;
    private final SSLContext sslContext;

    private volatile InputStream inputStream;
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

            writeChunkHead(chunk);

            final int contentOffset = chunk.getContentOffset();
            final int contentLength = chunk.getContentLength();
            final VPackSlice head = message.getHead();
            final int headLength = head.getByteSize();
            int written = 0;
            if (contentOffset < headLength) {
                written = Math.min(contentLength, headLength - contentOffset);
                arangoTcpClient.send(Arrays.copyOfRange(head.getBuffer(), contentOffset, contentOffset + written));
            }
            if (written < contentLength) {
                final VPackSlice body = message.getBody();
                arangoTcpClient.send(Arrays.copyOfRange(body.getBuffer(), contentOffset + written - headLength, contentOffset + contentLength - headLength));
            }
        }
    }

    private synchronized void writeChunkHead(final Chunk chunk) {
        final long messageLength = chunk.getMessageLength();
        final int headLength = messageLength > -1L ? ArangoDefaults.CHUNK_MAX_HEADER_SIZE
                : ArangoDefaults.CHUNK_MIN_HEADER_SIZE;
        final int length = chunk.getContentLength() + headLength;
        final ByteBuffer buffer = ByteBuffer.allocate(headLength).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(length);
        buffer.putInt(chunk.getChunkX());
        buffer.putLong(chunk.getMessageId());
        if (messageLength > -1L) {
            buffer.putLong(messageLength);
        }
        arangoTcpClient.send(buffer.array());
    }

    private ByteBuffer readBytes(final int len) throws IOException {
        final byte[] buf = new byte[len];
        inputStream.read(buf, 0, len);
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    private class ArangoTcpClient {
        private volatile NettyOutbound outbound;
        private volatile reactor.netty.Connection connection;
        private TcpClient tcpClient;
        private volatile Chunk chunk;
        // FIXME: replace with faster data structures
        private volatile ByteArrayOutputStream chunkHeaderBuffer = new ByteArrayOutputStream();
        private volatile ByteArrayOutputStream chunkContentBuffer = new ByteArrayOutputStream();
        private volatile CompletableFuture<Void> connectedFuture = new CompletableFuture<>();

        private void setConnection(reactor.netty.Connection connection) {
            this.connection = connection;
        }

        void send(byte[] bytes) {
            outbound
                    .sendByteArray(Mono.just(bytes))
                    .then()
                    // FIXME: catch errors
                    .subscribe();
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
                        send(PROTOCOL_HEADER);
                    });
        }

        private void fillBytes(ByteBuf bbIn, ByteArrayOutputStream out, int len) {
            int missingBytes = len - out.size();
            int bytesToRead = Integer.min(missingBytes, bbIn.readableBytes());

            if (bytesToRead > 0) {
                try {
                    bbIn.readBytes(out, bytesToRead);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        private void handleByteBuf(ByteBuf bbIn) throws IOException {
            // new chunk
            if (chunk == null) {
                fillBytes(bbIn, chunkHeaderBuffer, ArangoDefaults.CHUNK_MIN_HEADER_SIZE);
                if (chunkHeaderBuffer.size() == ArangoDefaults.CHUNK_MIN_HEADER_SIZE) {
                    ByteBuffer bbHeader = ByteBuffer.wrap(chunkHeaderBuffer.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);

                    final int chunkLength = bbHeader.getInt();
                    final int chunkX = bbHeader.getInt();
                    final long messageId = bbHeader.getLong();
                    final long messageLength;
                    final int contentLength;
                    if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
                        ByteArrayOutputStream lengthBuffer = new ByteArrayOutputStream();
                        fillBytes(bbIn, lengthBuffer, LONG_BYTES);
                        if (lengthBuffer.size() < ArangoDefaults.LONG_BYTES)
                            return;
                        messageLength = ByteBuffer.wrap(lengthBuffer.toByteArray()).order(ByteOrder.LITTLE_ENDIAN).getLong();
                        contentLength = chunkLength - ArangoDefaults.CHUNK_MAX_HEADER_SIZE;
                    } else {
                        messageLength = -1L;
                        contentLength = chunkLength - ArangoDefaults.CHUNK_MIN_HEADER_SIZE;
                    }
                    chunk = new Chunk(messageId, chunkX, messageLength, 0, contentLength);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(), chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
                        LOGGER.debug("Responsetime for Message " + chunk.getMessageId() + " is " + (sendTimestamps.get(chunk.getMessageId()) - System.currentTimeMillis()));
                    }

                }
            }

            if (chunk != null) {
                int missingContentBytes = chunk.getContentLength() - chunkContentBuffer.size();
                int contentBytesToRead = Integer.min(missingContentBytes, bbIn.readableBytes());

                if (contentBytesToRead > 0) {
                    bbIn.readBytes(chunkContentBuffer, contentBytesToRead);
                }

                // chunkContent completely received
                if (chunkContentBuffer.size() == chunk.getContentLength()) {
                    chunkStore.storeChunk(chunk, chunkContentBuffer.toByteArray());
                    chunk = null;
                    chunkContentBuffer = new ByteArrayOutputStream();
                    chunkHeaderBuffer = new ByteArrayOutputStream();
                }
            }
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

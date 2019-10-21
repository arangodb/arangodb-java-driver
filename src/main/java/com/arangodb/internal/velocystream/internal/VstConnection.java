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
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.arangodb.internal.ArangoDefaults.HEADER_SIZE;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static reactor.netty.resources.ConnectionProvider.DEFAULT_POOL_ACQUIRE_TIMEOUT;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class VstConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
    private static final byte[] PROTOCOL_HEADER = "VST/1.1\r\n\r\n".getBytes();

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

        connectionName = "connection_" + System.currentTimeMillis() + "_" + Math.random();
        LOGGER.debug("Connection " + connectionName + " created");

        arangoTcpClient = new ArangoTcpClient();
        chunkStore = new ChunkStore(messageStore);
    }

    private ConnectionProvider initConnectionProvider() {
        return ConnectionProvider.fixed(
                "tcp",
                1,
                getAcquireTimeout(),
                ttl != null ? Duration.ofMillis(ttl) : null);
    }

    private long getAcquireTimeout() {
        return timeout != null && timeout >= 0 ? timeout : DEFAULT_POOL_ACQUIRE_TIMEOUT;
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
        try {
            arangoTcpClient.getConnectedFuture().get();
        } catch (InterruptedException e) {
            close();
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    @Override
    public synchronized void close() {
        arangoTcpClient.disconnect();
    }

    protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks) throws ArangoDBException {
        final ByteBuf messageBuffer = IOUtils.createBuffer();
        messageBuffer.writeBytes(message.getHead().getBuffer(), 0, message.getHead().getByteSize());
        final VPackSlice body = message.getBody();
        if (body != null) {
            messageBuffer.writeBytes(body.getBuffer(), 0, message.getBody().getByteSize());
        }

        final ByteBuf out = IOUtils.createBuffer();

        for (final Chunk chunk : chunks) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Send chunk %s:%s from message %s", chunk.getChunk(),
                        chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
                sendTimestamps.put(chunk.getMessageId(), System.currentTimeMillis());
            }

            final int length = chunk.getContentLength() + HEADER_SIZE;

            out.writeIntLE(length);
            out.writeIntLE(chunk.getChunkX());
            out.writeLongLE(chunk.getMessageId());
            out.writeLongLE(chunk.getMessageLength());

            final int contentOffset = chunk.getContentOffset();
            final int contentLength = chunk.getContentLength();

            out.writeBytes(messageBuffer, contentOffset, contentLength);
        }

        messageBuffer.release();
        arangoTcpClient.send(out);
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    private class ArangoTcpClient {
        private volatile reactor.netty.Connection connection;
        private TcpClient tcpClient;
        private volatile Chunk chunk;
        private volatile ByteBuf chunkHeaderBuffer = IOUtils.createBuffer();
        private volatile ByteBuf chunkContentBuffer = IOUtils.createBuffer();
        private volatile CompletableFuture<Void> connectedFuture = new CompletableFuture<>();

        private void setConnection(reactor.netty.Connection connection) {
            this.connection = connection;
        }

        void send(ByteBuf buf) {
            connection.outbound()
                    .send(Mono.just(buf))
                    .then()
                    .doOnError(this::handleError)
                    .subscribe();
        }

        private TcpClient applyConnectionTimeout(TcpClient tcpClient) {
            return timeout != null && timeout >= 0 ? tcpClient.option(CONNECT_TIMEOUT_MILLIS, timeout) : tcpClient;
        }

        private TcpClient applySslContext(TcpClient httpClient) {
            if (Boolean.TRUE == useSsl && sslContext != null) {
                //noinspection deprecation
                return httpClient.secure(spec -> spec.sslContext(new JdkSslContext(sslContext, true, ClientAuth.NONE)));
            } else {
                return httpClient;
            }
        }

        ArangoTcpClient() {
            tcpClient = applySslContext(applyConnectionTimeout(TcpClient.create(connectionProvider)))
                    .host(host.getHost())
                    .port(host.getPort())
                    .doOnDisconnected(c -> finalize(new IOException("Connection closed!")))
                    .handle((i, o) -> {
                        i.receive()
                                .doOnNext(x -> {
                                    while (x.readableBytes() > 0) {
                                        handleByteBuf(x);
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

        private void handleError(final Throwable t) {
            t.printStackTrace();
            finalize(t);
            disconnect();
        }

        private void finalize(final Throwable t) {
            connectedFuture.completeExceptionally(t);
            connectedFuture = new CompletableFuture<>();
            messageStore.clear(t);
        }

        private void readBytes(ByteBuf bbIn, ByteBuf out, int len) {
            int bytesToRead = Integer.min(len, bbIn.readableBytes());
            out.ensureWritable(bytesToRead);
            bbIn.readBytes(out, bytesToRead);
        }

        private void handleByteBuf(ByteBuf bbIn) {
            // new chunk
            if (chunk == null) {
                int missingHeaderBytes = HEADER_SIZE - chunkHeaderBuffer.readableBytes();
                readBytes(bbIn, chunkHeaderBuffer, missingHeaderBytes);
                if (chunkHeaderBuffer.readableBytes() == HEADER_SIZE) {
                    readHeader();
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

        private void readHeader() {
            final int chunkLength = chunkHeaderBuffer.readIntLE();
            final int chunkX = chunkHeaderBuffer.readIntLE();

            final long messageId = chunkHeaderBuffer.readLongLE();
            final long messageLength = chunkHeaderBuffer.readLongLE();
            final int contentLength = chunkLength - HEADER_SIZE;

            chunk = new Chunk(messageId, chunkX, messageLength, 0, contentLength);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(), chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
                LOGGER.debug("Responsetime for Message " + chunk.getMessageId() + " is " + (sendTimestamps.get(chunk.getMessageId()) - System.currentTimeMillis()));
            }
        }

        private void readContent() {
            chunkStore.storeChunk(chunk, chunkContentBuffer);
            chunkHeaderBuffer.clear();
            chunkContentBuffer.clear();
            chunk = null;
        }

        void connect() {
            tcpClient
                    .connect()
                    .doOnError(this::handleError)
                    .subscribe();
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

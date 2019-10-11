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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Mark Vollmary
 */
public abstract class VstConnection implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
    private static final byte[] PROTOCOL_HEADER = "VST/1.0\r\n\r\n".getBytes();

    protected final MessageStore messageStore;

    protected final Integer timeout;
    private final Long ttl;
    private final Boolean useSsl;
    private final SSLContext sslContext;

    private volatile ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private volatile InputStream inputStream;
    private final HostDescription host;

    private final HashMap<Long, Long> sendTimestamps = new HashMap<>();

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

        connectionName = "conenction_" + System.currentTimeMillis() + "_" + Math.random();
        LOGGER.debug("Connection " + connectionName + " created");

        arangoTcpClient = new ArangoTcpClient();
        chunkStore = new ChunkStore(messageStore);
    }

    public boolean isOpen() {
        return arangoTcpClient.isActive();
    }

    public synchronized void open() throws IOException {
        if (isOpen()) {
            return;
        }
        new Thread(arangoTcpClient::connect).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        messageStore.clear();
        arangoTcpClient.disconnect();
    }

    protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks) throws ArangoDBException {
        for (final Chunk chunk : chunks) {
            try {
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
                    outputStream.write(head.getBuffer(), contentOffset, written);
                }
                if (written < contentLength) {
                    final VPackSlice body = message.getBody();
                    outputStream.write(body.getBuffer(), contentOffset + written - headLength, contentLength - written);
                }
                arangoTcpClient.send(outputStream.toByteArray());
            } catch (final IOException e) {
                LOGGER.error("Error on Connection " + connectionName);
                throw new ArangoDBException(e);
            }
        }
    }

    private synchronized void writeChunkHead(final Chunk chunk) throws IOException {
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
        outputStream.write(buffer.array());
    }

    protected Chunk readChunk() throws IOException {
        final ByteBuffer chunkHeadBuffer = readBytes(ArangoDefaults.CHUNK_MIN_HEADER_SIZE);
        final int length = chunkHeadBuffer.getInt();
        final int chunkX = chunkHeadBuffer.getInt();
        final long messageId = chunkHeadBuffer.getLong();
        final long messageLength;
        final int contentLength;
        if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
            messageLength = readBytes(ArangoDefaults.LONG_BYTES).getLong();
            contentLength = length - ArangoDefaults.CHUNK_MAX_HEADER_SIZE;
        } else {
            messageLength = -1L;
            contentLength = length - ArangoDefaults.CHUNK_MIN_HEADER_SIZE;
        }
        final Chunk chunk = new Chunk(messageId, chunkX, messageLength, 0, contentLength);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(), chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
            LOGGER.debug("Responsetime for Message " + chunk.getMessageId() + " is " + (sendTimestamps.get(chunk.getMessageId()) - System.currentTimeMillis()));
        }

        return chunk;
    }

    private ByteBuffer readBytes(final int len) throws IOException {
        final byte[] buf = new byte[len];
        readBytesIntoBuffer(buf, 0, len);
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
    }

    protected void readBytesIntoBuffer(final byte[] buf, final int off, final int len) throws IOException {
        for (int readed = 0; readed < len; ) {
            final int read = inputStream.read(buf, off + readed, len - readed);
            if (read == -1) {
                throw new IOException("Reached the end of the stream.");
            } else {
                readed += read;
            }
        }
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    class ArangoTcpClient {
        private volatile NettyOutbound outbound;
        private reactor.netty.Connection connection;
        private TcpClient tcpClient;

        void setConnection(reactor.netty.Connection connection) {
            this.connection = connection;
        }

        void send(byte[] bytes) {
            outputStream = new ByteArrayOutputStream();
            outbound
                    .sendByteArray(Mono.just(bytes))
                    .then()
                    .subscribe();
        }

        ArangoTcpClient() {
            tcpClient = TcpClient.create()
                    .host("127.0.0.1")
                    .port(8529)
                    .doOnDisconnected(c -> {
                        System.out.println("messageStore.isEmpty(): " + messageStore.isEmpty());
                    })
                    .handle((i, o) -> {
                        outbound = o;
                        i.receive()
                                .asInputStream()
                                .doOnNext(is -> {
                                    inputStream = is;
                                    Chunk chunk = null;
                                    try {
                                        chunk = readChunk();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("rcv: " + chunk.getMessageId());
                                    try {
                                        chunkStore.storeChunk(chunk, is);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                })
                                .doOnError(it -> LOGGER.error(it.getMessage(), it))
                                .subscribe();
                        return Mono.never();
                    })
                    .doOnConnected(connection1 -> {
                        send(PROTOCOL_HEADER);
                        setConnection(connection1);
                    });
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
    }
}

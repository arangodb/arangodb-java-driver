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

package com.arangodb.vst.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionPool;
import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 */
public abstract class VstConnection<T> implements Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static final byte[] PROTOCOL_HEADER = "VST/1.0\r\n\r\n".getBytes();
    protected final MessageStore messageStore = new MessageStore();
    protected final Integer timeout;
    private final AtomicLong keepAliveId = new AtomicLong();
    private final Long ttl;
    private final Integer keepAliveInterval;
    private final Boolean useSsl;
    private final SSLContext sslContext;
    private final HostDescription host;
    private final Map<Long, Long> sendTimestamps = new ConcurrentHashMap<>();
    private final String connectionName;
    private final ConnectionPool pool;
    private final byte[] keepAliveRequest = new VPackBuilder()
            .add(ValueType.ARRAY)
            .add(1)
            .add(1)
            .add("_system")
            .add(1)
            .add("/_admin/server/availability")
            .add(ValueType.OBJECT)
            .close()
            .add(ValueType.OBJECT)
            .close()
            .close()
            .slice()
            .toByteArray();
    private ExecutorService executor;
    private ScheduledExecutorService keepAliveScheduler;
    private int keepAliveFailCounter = 0;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    protected VstConnection(final ArangoConfig config, final HostDescription host, final ConnectionPool pool) {
        super();
        timeout = config.getTimeout();
        ttl = config.getConnectionTtl();
        keepAliveInterval = config.getKeepAliveInterval();
        useSsl = config.getUseSsl();
        sslContext = config.getSslContext();
        this.host = host;
        this.pool = pool;

        connectionName = "connection_" + System.currentTimeMillis() + "_" + Math.random();
        LOGGER.debug("[" + connectionName + "]: Connection created");
    }

    protected T sendKeepAlive() {
        long id = keepAliveId.decrementAndGet();
        Message message = new Message(id, keepAliveRequest, null);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]: Send keepalive probe (id=%s, head=%s, body=%s)", connectionName,
                    message.getId(), message.getHead(),
                    message.getBody() != null ? message.getBody() : "{}"));
        }
        return write(message, Collections.singleton(new Chunk(
                id, 0, 1, -1,
                0, keepAliveRequest.length
        )));
    }

    public abstract T write(final Message message, final Collection<Chunk> chunks);

    protected abstract void doKeepAlive();

    private void keepAlive() {
        try {
            doKeepAlive();
            keepAliveFailCounter = 0;
        } catch (Exception e) {
            LOGGER.error("Got exception while performing keepAlive request:", e);
            keepAliveFailCounter++;
            if (keepAliveFailCounter >= 3) {
                LOGGER.error("KeepAlive request failed consecutively for 3 times, closing connection now...");
                messageStore.clear(new IOException("Connection unresponsive!"));
                close();
            }
        }
    }

    public boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void open() throws IOException {
        if (isOpen()) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]: Open connection to %s", connectionName, host));
        }
        if (Boolean.TRUE.equals(useSsl)) {
            socket = sslContext.getSocketFactory().createSocket();
        } else {
            socket = SocketFactory.getDefault().createSocket();
        }
        socket.connect(new InetSocketAddress(host.getHost(), host.getPort()), timeout);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]: Connected to %s", connectionName, socket));
        }

        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = socket.getInputStream();

        if (Boolean.TRUE.equals(useSsl)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s]: Start Handshake on %s", connectionName, socket));
            }
            ((SSLSocket) socket).startHandshake();
        }
        sendProtocolHeader();

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("adb-vst-" + THREAD_COUNT.getAndIncrement());
            return t;
        });
        executor.submit((Callable<Void>) () -> {
            LOGGER.debug("[" + connectionName + "]: Start Callable");

            final long openTime = new Date().getTime();
            final Long ttlTime = ttl != null && ttl > 0 ? openTime + ttl : null;
            final ChunkStore chunkStore = new ChunkStore(messageStore);
            while (true) {
                if (ttlTime != null && new Date().getTime() > ttlTime && messageStore.isEmpty()) {
                    close();
                    break;
                }
                if (!isOpen()) {
                    messageStore.clear(new IOException("The socket is closed."));
                    close();
                    break;
                }
                try {
                    final Chunk chunk = readChunk();
                    final ByteBuffer chunkBuffer = chunkStore.storeChunk(chunk);
                    if (chunkBuffer != null) {
                        final byte[] buf = new byte[chunk.getContentLength()];
                        readBytesIntoBuffer(buf, 0, buf.length);
                        chunkBuffer.put(buf);
                        chunkStore.checkCompleteness(chunk.getMessageId());
                    }
                } catch (final Exception e) {
                    messageStore.clear(e);
                    close();
                    break;
                }
            }

            LOGGER.debug("[" + connectionName + "]: Stop Callable");

            return null;
        });

        if (keepAliveInterval != null) {
            keepAliveScheduler = Executors.newScheduledThreadPool(1);
            keepAliveScheduler.scheduleAtFixedRate(this::keepAlive, 0, keepAliveInterval, TimeUnit.SECONDS);
        }

    }

    @Override
    public synchronized void close() {
        if (keepAliveScheduler != null) {
            keepAliveScheduler.shutdown();
        }
        messageStore.clear(new IOException("Connection closed"));
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        if (socket != null && !socket.isClosed()) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]: Close connection %s", connectionName, socket));
                }
                socket.close();
            } catch (final IOException e) {
                throw ArangoDBException.of(e);
            }
        }
    }

    @Override
    public void release() {
        pool.release(this);
    }

    private synchronized void sendProtocolHeader() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]: Send velocystream protocol header to %s", connectionName, socket));
        }
        outputStream.write(PROTOCOL_HEADER);
        outputStream.flush();
    }

    protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks) {
        for (final Chunk chunk : chunks) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s]: Send chunk %s:%s from message %s", connectionName,
                            chunk.getChunk(),
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
                outputStream.flush();
            } catch (final IOException e) {
                LOGGER.error("Error on Connection " + connectionName);
                throw ArangoDBException.of(e);
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
            LOGGER.debug(String.format("[%s]: Received chunk %s:%s from message %s", connectionName, chunk.getChunk()
                    , chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
            LOGGER.debug("[" + connectionName + "]: Responsetime for Message " + chunk.getMessageId() + " is " + (System.currentTimeMillis() - sendTimestamps.get(chunk.getMessageId())));
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

    @Override
    public void setJwt(String jwt) {
        // no-op: VST connections send jwt token only at initialization time
    }
}

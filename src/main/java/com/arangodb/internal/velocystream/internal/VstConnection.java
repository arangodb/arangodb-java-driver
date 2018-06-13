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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.Host;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 *
 */
public abstract class VstConnection implements Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(VstConnection.class);
	private static final byte[] PROTOCOL_HEADER = "VST/1.0\r\n\r\n".getBytes();

	private ExecutorService executor;
	protected final MessageStore messageStore;

	private final HostHandler hostHandler;
	private final Integer timeout;
	private final Long ttl;
	private final Boolean useSsl;
	private final SSLContext sslContext;

	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;

	private Host host;

	protected VstConnection(final HostHandler hostHandler, final Integer timeout, final Long ttl, final Boolean useSsl,
		final SSLContext sslContext, final MessageStore messageStore) {
		super();
		this.hostHandler = hostHandler;
		this.timeout = timeout;
		this.ttl = ttl;
		this.useSsl = useSsl;
		this.sslContext = sslContext;
		this.messageStore = messageStore;
	}

	@Override
	public Host getHost() {
		if (host == null) {
			host = hostHandler.get();
		}
		return host;
	}

	public boolean isOpen() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public synchronized void open() throws IOException {
		if (isOpen()) {
			return;
		}
		host = hostHandler.get();
		while (true) {
			if (host == null) {
				hostHandler.reset();
				throw new ArangoDBException("Was not able to connect to any host");
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Open connection to %s", host));
			}
			try {
				if (useSsl != null && useSsl) {
					if (sslContext != null) {
						socket = sslContext.getSocketFactory().createSocket();
					} else {
						socket = SSLSocketFactory.getDefault().createSocket();
					}
				} else {
					socket = SocketFactory.getDefault().createSocket();
				}
				socket.connect(new InetSocketAddress(host.getHost(), host.getPort()),
					timeout != null ? timeout : ArangoDBConstants.DEFAULT_TIMEOUT);
				socket.setKeepAlive(true);
				socket.setTcpNoDelay(true);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Connected to %s", socket));
				}

				outputStream = new BufferedOutputStream(socket.getOutputStream());
				inputStream = socket.getInputStream();

				if (useSsl != null && useSsl) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(String.format("Start Handshake on %s", socket));
					}
					((SSLSocket) socket).startHandshake();
				}
				hostHandler.success();
				break;
			} catch (final IOException e) {
				hostHandler.fail();
				final Host failedHost = host;
				host = hostHandler.get();
				if (host != null) {
					LOGGER.warn(String.format("Could not connect to %s or SSL Handshake failed. Try connecting to %s",
						failedHost, host));
				} else {
					throw e;
				}
			}
		}
		sendProtocolHeader();
		executor = Executors.newSingleThreadExecutor();
		executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final long openTime = new Date().getTime();
				final Long ttlTime = ttl != null ? openTime + ttl : null;
				final ChunkStore chunkStore = new ChunkStore(messageStore);
				while (true) {
					if (ttlTime != null && new Date().getTime() > ttlTime && messageStore.isEmpty()) {
						close();
						hostHandler.reset();
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
				return null;
			}
		});
		hostHandler.opened();
	}

	@Override
	public synchronized void close() {
		messageStore.clear();
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
		if (socket != null && !socket.isClosed()) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Close connection %s", socket));
				}
				socket.close();
			} catch (final IOException e) {
				throw new ArangoDBException(e);
			}
		}
	}

	@Override
	public synchronized void closeOnError() {
		hostHandler.fail();
		close();
	}

	private synchronized void sendProtocolHeader() throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Send velocystream protocol header to %s", socket));
		}
		outputStream.write(PROTOCOL_HEADER);
		outputStream.flush();
	}

	protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks)
			throws ArangoDBException {
		for (final Chunk chunk : chunks) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Send chunk %s:%s from message %s", chunk.getChunk(),
						chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
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
				throw new ArangoDBException(e);
			}
		}
	}

	private synchronized void writeChunkHead(final Chunk chunk) throws IOException {
		final long messageLength = chunk.getMessageLength();
		final int headLength = messageLength > -1L ? ArangoDBConstants.CHUNK_MAX_HEADER_SIZE
				: ArangoDBConstants.CHUNK_MIN_HEADER_SIZE;
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
		final ByteBuffer chunkHeadBuffer = readBytes(ArangoDBConstants.CHUNK_MIN_HEADER_SIZE);
		final int length = chunkHeadBuffer.getInt();
		final int chunkX = chunkHeadBuffer.getInt();
		final long messageId = chunkHeadBuffer.getLong();
		final long messageLength;
		final int contentLength;
		if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
			messageLength = readBytes(ArangoDBConstants.LONG_BYTES).getLong();
			contentLength = length - ArangoDBConstants.CHUNK_MAX_HEADER_SIZE;
		} else {
			messageLength = -1L;
			contentLength = length - ArangoDBConstants.CHUNK_MIN_HEADER_SIZE;
		}
		final Chunk chunk = new Chunk(messageId, chunkX, messageLength, 0, contentLength);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(),
				chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
		}
		return chunk;
	}

	private ByteBuffer readBytes(final int len) throws IOException {
		final byte[] buf = new byte[len];
		readBytesIntoBuffer(buf, 0, len);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
	}

	protected void readBytesIntoBuffer(final byte[] buf, final int off, final int len) throws IOException {
		for (int readed = 0; readed < len;) {
			final int read = inputStream.read(buf, off + readed, len - readed);
			if (read == -1) {
				throw new IOException("Reached the end of the stream.");
			} else {
				readed += read;
			}
		}
	}

}

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

package com.arangodb.internal.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Optional;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

	protected final Optional<String> host;
	protected final Optional<Integer> port;
	protected final Optional<Integer> timeout;
	protected final Optional<Boolean> useSsl;
	protected final Optional<SSLContext> sslContext;

	protected Socket socket;
	protected OutputStream outputStream;
	protected InputStream inputStream;

	protected Connection(final String host, final Integer port, final Integer timeout, final Boolean useSsl,
		final SSLContext sslContext) {
		super();
		this.host = Optional.ofNullable(host);
		this.port = Optional.ofNullable(port);
		this.timeout = Optional.ofNullable(timeout);
		this.useSsl = Optional.ofNullable(useSsl);
		this.sslContext = Optional.ofNullable(sslContext);
	}

	public synchronized boolean isOpen() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public synchronized void open() throws IOException {
		if (isOpen()) {
			return;
		}
		if (useSsl.orElse(false)) {
			if (sslContext.isPresent()) {
				socket = sslContext.get().getSocketFactory().createSocket();
			} else {
				socket = SSLSocketFactory.getDefault().createSocket();
			}
		} else {
			socket = SocketFactory.getDefault().createSocket();
		}
		final String host = this.host.orElse(ArangoDBConstants.DEFAULT_HOST);
		final Integer port = this.port.orElse(ArangoDBConstants.DEFAULT_PORT);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Open connection to addr=%s,port=%s", host, port));
		}
		socket.connect(new InetSocketAddress(host, port), timeout.orElse(ArangoDBConstants.DEFAULT_TIMEOUT));
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Connected to %s", socket));
		}

		outputStream = new BufferedOutputStream(socket.getOutputStream());
		inputStream = socket.getInputStream();

		if (useSsl.orElse(false)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Start Handshake on %s", socket));
			}
			((SSLSocket) socket).startHandshake();
		}
	}

	public synchronized void close() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Close connection %s", socket));
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (final IOException e) {
				throw new ArangoDBException(e);
			}
		}
	}

	protected synchronized void writeIntern(final Message message, final Collection<Chunk> chunks) {
		chunks.stream().forEach(chunk -> {
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
					final VPackSlice body = message.getBody().get();
					outputStream.write(body.getBuffer(), contentOffset + written - headLength, contentLength - written);
				}
				outputStream.flush();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void writeChunkHead(final Chunk chunk) throws IOException {
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
			messageLength = readBytes(Long.BYTES).getLong();
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

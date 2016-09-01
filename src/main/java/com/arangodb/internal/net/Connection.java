package com.arangodb.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Optional;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.velocystream.Chunk;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

	protected Optional<String> host = Optional.empty();
	protected Optional<Integer> port = Optional.empty();
	protected Optional<Integer> timeout = Optional.empty();

	protected Socket socket;
	protected OutputStream outputStream;
	protected InputStream inputStream;

	protected Connection(final String host, final Integer port, final Integer timeout) {
		super();
		this.host = Optional.of(host);
		this.port = Optional.of(port);
		this.timeout = Optional.ofNullable(timeout);
	}

	public synchronized boolean isOpen() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public synchronized void open() throws IOException {
		if (isOpen()) {
			return;
		}
		socket = SocketFactory.getDefault().createSocket();
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

		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();
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

	protected synchronized void writeIntern(final long messageId, final Collection<Chunk> chunks) {
		chunks.stream().forEach(chunk -> {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(String.format("Send chunk %s:%s from message %s", chunk.getChunk(),
						chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
				}
				outputStream.write(chunk.toByteBuffer().array());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	protected Chunk read() throws IOException, BufferUnderflowException {
		final Chunk chunk = readChunkHead();
		final byte[] content = new byte[chunk.getContentLength()];
		chunk.setContent(content);
		readBytes(content.length).get(content);
		return chunk;
	}

	protected Chunk readChunkHead() throws IOException {
		final int length = readBytes(4).getInt();
		final int chunkX = readBytes(4).getInt();
		final long messageId = readBytes(8).getLong();
		final long messageLength;
		final int contentLength;
		if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
			messageLength = readBytes(8).getLong();
			contentLength = length - ArangoDBConstants.CHUNK_MAX_HEADER_SIZE;
		} else {
			messageLength = -1L;
			contentLength = length - ArangoDBConstants.CHUNK_MIN_HEADER_SIZE;
		}
		final Chunk chunk = new Chunk(messageId, chunkX, contentLength, length, messageLength);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(),
				chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
		}
		return chunk;
	}

	private ByteBuffer readBytes(final int len) throws IOException {
		return ByteBuffer.wrap(readBytesIntoBuffer(len)).order(ByteOrder.LITTLE_ENDIAN);
	}

	protected byte[] readBytesIntoBuffer(final int len) throws IOException {
		final byte[] buf = new byte[len];
		for (int readed = 0; readed < len;) {
			final int read = inputStream.read(buf, readed, len - readed);
			if (read == -1) {
				throw new IOException("Reached the end of the stream.");
			} else {
				readed += read;
			}
		}
		return buf;
	}

}

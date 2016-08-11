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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.ChunkStore;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.internal.net.velocystream.MessageStore;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

	private final MessageStore messageStore;
	private Optional<String> host = Optional.empty();
	private Optional<Integer> port = Optional.empty();
	private Optional<Integer> timeout = Optional.empty();
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private ExecutorService executor;

	public static class Builder {
		private final MessageStore messageStore;
		private String host;
		private Integer port;
		private Integer timeout;

		public Builder(final MessageStore messageStore) {
			super();
			this.messageStore = messageStore;
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final int port) {
			this.port = port;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public Connection build() {
			return new Connection(this);
		}
	}

	private Connection(final Builder builder) {
		super();
		this.messageStore = builder.messageStore;
		this.host = Optional.of(builder.host);
		this.port = Optional.of(builder.port);
		this.timeout = Optional.ofNullable(builder.timeout);
	}

	public void open() throws IOException {
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
		executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			final ChunkStore chunkStore = new ChunkStore(messageStore);
			while (true) {
				if (!isOpen()) {
					messageStore.clear(new IOException("The socket is closed."));
					close();
					break;
				}
				try {
					chunkStore.storeChunk(read());
				} catch (final Exception e) {
					messageStore.clear(e);
					close();
					break;
				}
			}
		});
	}

	public boolean isOpen() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public void close() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Close connection %s", socket));
		}
		messageStore.clear();
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (final IOException e) {
				throw new ArangoDBException(e);
			}
		}
	}

	public void write(final long messageId, final Collection<Chunk> chunks, final CompletableFuture<Message> future) {
		messageStore.storeMessage(messageId, future);
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

	private Chunk read() throws IOException, BufferUnderflowException {
		final int length = readBytes(4).getInt();
		final int chunkX = readBytes(4).getInt();
		final long messageId = readBytes(8).getLong();
		final long messageLength;
		final byte[] content;
		if ((1 == (chunkX & 0x1)) && ((chunkX >> 1) > 1)) {
			messageLength = readBytes(8).getLong();
			content = new byte[length - Chunk.CHUNK_MIN_HEADER_SIZE - Long.BYTES];
		} else {
			messageLength = -1L;
			content = new byte[length - Chunk.CHUNK_MIN_HEADER_SIZE];
		}
		readBytes(content.length).get(content);
		final Chunk chunk = new Chunk(messageId, chunkX, content, length, messageLength);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Received chunk %s:%s from message %s", chunk.getChunk(),
				chunk.isFirstChunk() ? 1 : 0, chunk.getMessageId()));
		}
		return chunk;
	}

	private ByteBuffer readBytes(final int len) throws IOException {
		final byte[] buf = new byte[len];
		for (int readed = 0; readed < len;) {
			final int read = inputStream.read(buf, readed, len - readed);
			if (read == -1) {
				throw new IOException("Reached the end of the stream.");
			} else {
				readed += read;
			}
		}
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
	}

}

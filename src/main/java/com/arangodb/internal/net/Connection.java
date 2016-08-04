package com.arangodb.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.ChunkStore;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.internal.net.velocystream.MessageStore;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Connection {

	private final MessageStore messageStore;
	private final String host;
	private final int port;
	private final int timeout;
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private ExecutorService executor;

	public static class Builder {
		private final MessageStore messageStore;
		private String host;
		private int port;
		private int timeout;

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

		public Builder timeout(final int timeout) {
			this.timeout = timeout;
			return this;
		}

		public Connection build() {
			return new Connection(messageStore, host, port, timeout);
		}
	}

	private Connection(final MessageStore messageStore, final String host, final int port, final int timeout) {
		super();
		this.messageStore = messageStore;
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}

	public void connect() throws IOException {
		if (isOpen()) {
			return;
		}
		socket = SocketFactory.getDefault().createSocket();
		socket.connect(new InetSocketAddress(host, port), timeout);
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);

		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();
		executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> {
			final ChunkStore chunkStore = new ChunkStore((messageId, chunks) -> {
				messageStore.consume(new Message(messageId, chunks));
			});
			while (true) {
				if (!isOpen()) {
					// TODO
					// exception = new IOException();
					disconnect();
					break;
				}
				try {
					chunkStore.storeChunk(read());
				} catch (final IOException e) {
					// TODO
					// exception = e;
					disconnect();
					break;
				}
			}
		});
	}

	public boolean isOpen() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public void disconnect() {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (final IOException e) {
				// TODO
			}
		}
		messageStore.clear();
	}

	public void write(final long messageId, final Collection<Chunk> chunks, final CompletableFuture<Message> future) {
		messageStore.storeMessage(messageId, future);
		chunks.stream().forEach(chunk -> {
			try {
				outputStream.write(chunk.toByteBuffer().array());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private Chunk read() throws IOException {
		final ByteBuffer head = readBytes(4);
		final int len = head.getInt();
		return new Chunk.Builder().length(len).data(readBytes(len - 4)).build();
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
		return ByteBuffer.wrap(buf);
	}

}

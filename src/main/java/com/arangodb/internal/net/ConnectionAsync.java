package com.arangodb.internal.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.ChunkStore;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.internal.net.velocystream.MessageStore;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ConnectionAsync extends Connection {

	private ExecutorService executor;
	private final MessageStore messageStore;

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

		public ConnectionAsync build() {
			return new ConnectionAsync(host, port, timeout, messageStore);
		}
	}

	private ConnectionAsync(final String host, final Integer port, final Integer timeout,
		final MessageStore messageStore) {
		super(host, port, timeout);
		this.messageStore = messageStore;
	}

	@Override
	public synchronized void open() throws IOException {
		if (isOpen()) {
			return;
		}
		super.open();
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
		});
	}

	@Override
	public synchronized void close() {
		super.close();
		messageStore.clear();
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
	}

	public synchronized CompletableFuture<Message> write(final Message message, final Collection<Chunk> chunks) {
		final CompletableFuture<Message> future = new CompletableFuture<>();
		messageStore.storeMessage(message.getId(), future);
		super.writeIntern(message, chunks);
		return future;
	}

}

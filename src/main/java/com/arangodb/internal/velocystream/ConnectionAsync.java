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

package com.arangodb.internal.velocystream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

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
		private Boolean useSsl;
		private SSLContext sslContext;

		public Builder(final MessageStore messageStore) {
			super();
			this.messageStore = messageStore;
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final Integer port) {
			this.port = port;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder useSsl(final Boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		public Builder sslContext(final SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		public ConnectionAsync build() {
			return new ConnectionAsync(host, port, timeout, useSsl, sslContext, messageStore);
		}
	}

	private ConnectionAsync(final String host, final Integer port, final Integer timeout, final Boolean useSsl,
		final SSLContext sslContext, final MessageStore messageStore) {
		super(host, port, timeout, useSsl, sslContext);
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
		messageStore.clear();
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
		}
		super.close();
	}

	public synchronized CompletableFuture<Message> write(final Message message, final Collection<Chunk> chunks) {
		final CompletableFuture<Message> future = new CompletableFuture<>();
		messageStore.storeMessage(message.getId(), future);
		super.writeIntern(message, chunks);
		return future;
	}

}

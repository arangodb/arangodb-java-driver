package com.arangodb.internal.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.internal.net.velocystream.MessageStore;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Communication {

	private final AtomicLong mId = new AtomicLong(0L);
	private final VPack vpack;
	private final Connection connection;
	private final MessageStore messageStore;

	public static class Builder {
		private final VPack vpack;
		private String host;
		private Integer port;
		private Integer timeout;

		public Builder(final VPack vpack) {
			super();
			this.vpack = vpack;
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

		public Communication build() {
			return new Communication(this);
		}
	}

	private Communication(final Builder builder) {
		messageStore = new MessageStore();
		vpack = builder.vpack;
		connection = new Connection.Builder(messageStore).host(builder.host).port(builder.port).timeout(builder.timeout)
				.build();
	}

	private void reconnect() {
		if (!connection.isOpen()) {
			try {
				connection.connect();
			} catch (final IOException e) {
				throw new ArangoDBException(e);
			}
		}
	}

	public void disconnect() {
		connection.disconnect();
	}

	public CompletableFuture<Response> execute(final Request request) {
		reconnect();
		final CompletableFuture<Response> rfuture = new CompletableFuture<>();
		try {
			final long id = mId.incrementAndGet();
			final VPackSlice body = request.getBody().isPresent() ? vpack.serialize(request.getBody().get()) : null;
			final Message message = new Message(id, vpack.serialize(request), body);
			send(message).whenComplete((m, ex) -> {
				if (m != null) {
					try {
						final Response response = vpack.deserialize(m.getHead(), Response.class);
						if (m.getBody().isPresent()) {
							response.setBody(m.getBody().get());
						}
						// TODO if responseCode == error throw ex
						rfuture.complete(response);
					} catch (final VPackParserException e) {
						rfuture.completeExceptionally(e);
					}
				} else if (ex != null) {
					rfuture.completeExceptionally(ex);
				} else {
					rfuture.cancel(true);
				}
			});
		} catch (final IOException | VPackException e) {
			rfuture.completeExceptionally(e);
		}
		return rfuture;
	}

	private CompletableFuture<Message> send(final Message message) throws IOException {
		final CompletableFuture<Message> future = new CompletableFuture<>();
		connection.write(message.getId(), buildChunks(message), future);
		return future;
	}

	private Collection<Chunk> buildChunks(final Message message) throws IOException {
		final Collection<Chunk> chunks = new ArrayList<>();
		final Collection<VPackSlice> vpacks = new ArrayList<>();
		vpacks.add(message.getHead());
		final Optional<VPackSlice> body = message.getBody();
		if (body.isPresent()) {
			vpacks.add(body.get());
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int size = 0;
		for (final VPackSlice vpack : vpacks) {
			final int byteSize = vpack.getByteSize();
			out.write(vpack.getVpack(), vpack.getStart(), byteSize);
			size += byteSize;
		}
		out.close();
		final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		final int n = size / Chunk.MAX_CHUNK_CONTENT_SIZE;
		final int numberOfChunks = (size % Chunk.MAX_CHUNK_CONTENT_SIZE != 0) ? (n + 1) : n;
		for (int pos = 0, i = 0; size > 0; pos += Chunk.MAX_CHUNK_CONTENT_SIZE, i++) {
			final int len = Math.min(Chunk.MAX_CHUNK_CONTENT_SIZE, size);
			final byte[] buffer = new byte[len];
			in.read(buffer, pos, len);
			size -= len;
			final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, buffer, len + Chunk.CHUNK_HEADER_SIZE);
			chunks.add(chunk);
		}
		in.close();
		return chunks;
	}

}

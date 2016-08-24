package com.arangodb.internal.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);

	private static final AtomicLong mId = new AtomicLong(0L);
	private final VPack vpack;
	private final Connection connection;
	private final MessageStore messageStore;
	private final CollectionCache collectionCache;

	public static class Builder {
		private String host;
		private Integer port;
		private Integer timeout;

		public Builder() {
			super();
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

		public Communication build(final VPack vpack, final CollectionCache collectionCache) {
			return new Communication(host, port, timeout, vpack, collectionCache);
		}
	}

	private Communication(final String host, final Integer port, final Integer timeout, final VPack vpack,
		final CollectionCache collectionCache) {
		messageStore = new MessageStore();
		this.vpack = vpack;
		this.collectionCache = collectionCache;
		connection = new Connection.Builder(messageStore).host(host).port(port).timeout(timeout).build();
	}

	private void connect() {
		if (!connection.isOpen()) {
			try {
				connection.open();
			} catch (final IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw new ArangoDBException(e);
			}
		}
	}

	public void disconnect() {
		connection.close();
	}

	public CompletableFuture<Response> execute(final Request request) {
		connect();
		final CompletableFuture<Response> rfuture = new CompletableFuture<>();
		try {
			final long id = mId.incrementAndGet();
			final VPackSlice body = request.getBody().isPresent() ? request.getBody().get() : null;
			final Message message = new Message(id, vpack.serialize(request), body);
			send(message).whenComplete((m, ex) -> {
				if (m != null) {
					try {
						collectionCache.setDb(request.getDatabase());
						final Response response = vpack.deserialize(m.getHead(), Response.class);
						if (m.getBody().isPresent()) {
							response.setBody(m.getBody().get());
						}
						if (response.getResponseCode() >= 300) {
							if (response.getBody().isPresent()) {
								final ErrorEntity errorEntity = vpack.deserialize(response.getBody().get(),
									ErrorEntity.class);
								final String errorMessage = String.format("Response: %s, Error: %s - %s",
									errorEntity.getCode(), errorEntity.getErrorNum(), errorEntity.getErrorMessage());
								rfuture.completeExceptionally(new ArangoDBException(errorMessage));
							} else {
								rfuture.completeExceptionally(new ArangoDBException(
										String.format("Response Code: %s", response.getResponseCode())));
							}
						} else {
							rfuture.complete(response);
						}
					} catch (final VPackParserException e) {
						LOGGER.error(e.getMessage(), e);
						rfuture.completeExceptionally(e);
					}
				} else if (ex != null) {
					LOGGER.error(ex.getMessage(), ex);
					rfuture.completeExceptionally(ex);
				} else {
					rfuture.cancel(true);
				}
			});
		} catch (final IOException | VPackException e) {
			LOGGER.error(e.getMessage(), e);
			rfuture.completeExceptionally(e);
		}
		return rfuture;
	}

	private CompletableFuture<Message> send(final Message message) throws IOException {
		final CompletableFuture<Message> future = new CompletableFuture<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Send Message (id=%s, head=%s, body=%s)", message.getId(), message.getHead(),
				message.getBody().isPresent() ? message.getBody().get() : "{}"));
		}
		connection.write(message.getId(), buildChunks(message), future);
		return future;
	}

	private Collection<Chunk> buildChunks(final Message message) throws IOException {
		final Collection<Chunk> chunks = new ArrayList<>();

		final VPackSlice head = message.getHead();
		final int headByteSize = head.getByteSize();
		int size = headByteSize;
		final Optional<VPackSlice> body = message.getBody();
		if (body.isPresent()) {
			size += body.get().getByteSize();
		}
		final ByteBuffer byteBuffer = ByteBuffer.allocate(size);
		byteBuffer.put(head.getVpack(), head.getStart(), headByteSize);
		if (body.isPresent()) {
			byteBuffer.put(body.get().getVpack(), body.get().getStart(), body.get().getByteSize());
		}
		byteBuffer.rewind();

		final int n = size / ArangoDBConstants.CHUNK_BODY_SIZE;
		final int numberOfChunks = (size % ArangoDBConstants.CHUNK_BODY_SIZE != 0) ? (n + 1) : n;
		for (int i = 0; size > 0; i++) {
			final int len = Math.min(ArangoDBConstants.CHUNK_BODY_SIZE, size);
			final byte[] buffer = new byte[len];
			byteBuffer.get(buffer);
			final Chunk chunk;
			if (i == 0 && numberOfChunks > 1) {
				chunk = new Chunk(message.getId(), i, numberOfChunks, buffer,
						len + ArangoDBConstants.CHUNK_MAX_HEADER_SIZE, size);
			} else {
				chunk = new Chunk(message.getId(), i, numberOfChunks, buffer,
						len + ArangoDBConstants.CHUNK_MIN_HEADER_SIZE, -1L);
			}
			size -= len;
			chunks.add(chunk);
		}
		return chunks;
	}

}

package com.arangodb.internal.net.velocystream;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class MessageStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageStore.class);

	private final Map<Long, CompletableFuture<Message>> data;

	public MessageStore() {
		super();
		data = new ConcurrentHashMap<>();
	}

	public void storeMessage(final long messageId, final CompletableFuture<Message> future) {
		data.put(messageId, future);
	}

	public void consume(final Message message) {
		final CompletableFuture<Message> future = data.remove(message.getId());
		if (future != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Received Message (id=%s, head=%s, body=%s)", message.getId(),
					message.getHead(), message.getBody().isPresent() ? message.getBody().get() : "{}"));
			}
			future.complete(message);
		}
	}

	public void cancel(final long messageId) {
		final CompletableFuture<Message> future = data.remove(messageId);
		if (future != null) {
			LOGGER.error(String.format("Cancel Message unexpected (id=%s).", messageId));
			future.cancel(true);
		}
	}

	public void clear(final Exception e) {
		if (!data.isEmpty()) {
			LOGGER.error(e.getMessage(), e);
		}
		data.entrySet().stream().forEach(entry -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Exceptionally complete Message (id=%s).", entry.getKey()));
			}
			entry.getValue().completeExceptionally(e);
		});
		data.clear();
	}

	public void clear() {
		data.entrySet().stream().forEach(entry -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Cancel Message (id=%s).", entry.getKey()));
			}
			entry.getValue().cancel(true);
		});
		data.clear();
	}

}

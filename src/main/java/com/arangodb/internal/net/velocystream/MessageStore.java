package com.arangodb.internal.net.velocystream;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class MessageStore {

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
		future.complete(message);
	}

	public void clear(final Exception e) {
		data.values().stream().forEach(future -> future.completeExceptionally(e));
		data.clear();
	}

	public void clear() {
		data.values().stream().forEach(future -> future.cancel(true));
		data.clear();
	}

}

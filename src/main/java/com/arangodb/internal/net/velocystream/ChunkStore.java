package com.arangodb.internal.net.velocystream;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ChunkStore {

	private final MessageStore messageStore;
	private final Map<Long, Collection<Chunk>> data;

	public ChunkStore(final MessageStore messageStore) {
		super();
		this.messageStore = messageStore;
		data = new HashMap<>();
	}

	public void storeChunk(final Chunk chunk) throws BufferUnderflowException, IndexOutOfBoundsException {
		final long messageId = chunk.getMessageId();
		Collection<Chunk> chunks = data.get(messageId);
		if (chunks == null) {
			chunks = new ArrayList<>();
			data.put(messageId, chunks);
		}
		chunks.add(chunk);
		checkCompleteness(messageId, chunks);
	}

	private void checkCompleteness(final long messageId, final Collection<Chunk> chunks)
			throws BufferUnderflowException, IndexOutOfBoundsException {
		final Optional<Chunk> first = chunks.stream().findFirst();
		if (first.isPresent()) {
			final int numChunks = first.get().getChunk();
			if (numChunks == chunks.size()) {
				messageStore.consume(new Message(messageId, chunks));
				data.remove(messageId);
			}
		}
	}

}

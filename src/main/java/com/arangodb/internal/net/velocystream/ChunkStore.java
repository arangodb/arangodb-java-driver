package com.arangodb.internal.net.velocystream;

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

	public static interface MessageCompleteListener {
		void complete(long messageId, Collection<Chunk> chunks);
	}

	private final Map<Long, Collection<Chunk>> data;
	private final MessageCompleteListener messageCompleteListener;

	public ChunkStore(final MessageCompleteListener messageCompleteListener) {
		super();
		this.messageCompleteListener = messageCompleteListener;
		data = new HashMap<>();
	}

	public void storeChunk(final Chunk chunk) {
		final long messageId = chunk.getMessageId();
		Collection<Chunk> chunks = data.get(messageId);
		if (chunks == null) {
			chunks = new ArrayList<>();
			data.put(messageId, chunks);
		}
		chunks.add(chunk);
		checkCompleteness(messageId, chunks);
	}

	private void checkCompleteness(final long messageId, final Collection<Chunk> chunks) {
		final Optional<Chunk> first = chunks.stream().findFirst();
		if (first.isPresent()) {
			final int numChunks = first.get().getChunk();
			if (numChunks == chunks.size()) {
				messageCompleteListener.complete(messageId, chunks);
				data.remove(messageId);
			}
		}
	}

}

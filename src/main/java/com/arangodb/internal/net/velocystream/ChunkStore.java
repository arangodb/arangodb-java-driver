package com.arangodb.internal.net.velocystream;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ChunkStore {

	private final MessageStore messageStore;
	private final Map<Long, ByteBuffer> data;

	public ChunkStore(final MessageStore messageStore) {
		super();
		this.messageStore = messageStore;
		data = new HashMap<>();
	}

	public void storeChunk(final Chunk chunk) throws BufferUnderflowException, IndexOutOfBoundsException {
		final long messageId = chunk.getMessageId();
		ByteBuffer chunkBuffer = data.get(messageId);
		if (chunkBuffer == null) {
			if (!chunk.isFirstChunk()) {
				messageStore.cancel(messageId);
				return;
			}
			final int length = (int) (chunk.getMessageLength() > 0 ? chunk.getMessageLength()
					: chunk.getContentLength());
			chunkBuffer = ByteBuffer.allocate(length);
			data.put(messageId, chunkBuffer);
		}
//		chunkBuffer.put(chunk.getContent());
		// TODO 
		checkCompleteness(messageId, chunkBuffer);
	}

	private void checkCompleteness(final long messageId, final ByteBuffer chunkBuffer)
			throws BufferUnderflowException, IndexOutOfBoundsException {
		if (chunkBuffer.position() == chunkBuffer.limit()) {
			messageStore.consume(new Message(messageId, chunkBuffer.array()));
			data.remove(messageId);
		}
	}

}

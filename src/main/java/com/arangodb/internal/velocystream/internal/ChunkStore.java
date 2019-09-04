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

package com.arangodb.internal.velocystream.internal;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
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

	public ByteBuffer storeChunk(final Chunk chunk) throws BufferUnderflowException, IndexOutOfBoundsException {
		final long messageId = chunk.getMessageId();
		ByteBuffer chunkBuffer = data.get(messageId);
		if (chunkBuffer == null) {
			if (!chunk.isFirstChunk()) {
				messageStore.cancel(messageId);
				return null;
			}
			final int length = (int) (chunk.getMessageLength() > 0 ? chunk.getMessageLength()
					: chunk.getContentLength());
			chunkBuffer = ByteBuffer.allocate(length);
			data.put(messageId, chunkBuffer);
		}
		return chunkBuffer;
	}

	public void checkCompleteness(final long messageId) {
		checkCompleteness(messageId, data.get(messageId));
	}

	private void checkCompleteness(final long messageId, final ByteBuffer chunkBuffer)
			throws BufferUnderflowException, IndexOutOfBoundsException {
		if (chunkBuffer.position() == chunkBuffer.limit()) {
			messageStore.consume(new Message(messageId, chunkBuffer.array()));
			data.remove(messageId);
		}
	}

}

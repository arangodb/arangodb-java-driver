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

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ChunkStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkStore.class);

    private final MessageStore messageStore;
    private final Map<Long, ByteBuf> data;

    public ChunkStore(final MessageStore messageStore) {
        super();
        this.messageStore = messageStore;
        data = new HashMap<>();
    }

    public void storeChunk(final Chunk chunk, final ByteBuf inBuf) throws BufferUnderflowException, IndexOutOfBoundsException {
        final long messageId = chunk.getMessageId();
        ByteBuf chunkBuffer = data.get(messageId);
        if (chunkBuffer == null) {
            if (!chunk.isFirstChunk()) {
                messageStore.cancel(messageId);
                return;
            }
            final int length = chunk.getChunk() > 1 ? (int) chunk.getMessageLength() : chunk.getContentLength();
            chunkBuffer = IOUtils.createBuffer(length, length);
            data.put(messageId, chunkBuffer);
        }

        chunkBuffer.writeBytes(inBuf);
        checkCompleteness(messageId, chunkBuffer);
    }

    private void checkCompleteness(final long messageId, final ByteBuf chunkBuffer)
            throws BufferUnderflowException, IndexOutOfBoundsException {
        if (chunkBuffer.readableBytes() == chunkBuffer.capacity()) {
            byte[] bytes = new byte[chunkBuffer.readableBytes()];
            chunkBuffer.readBytes(bytes);
            chunkBuffer.release();
            Message message = new Message(messageId, bytes);
            LOGGER.debug("consuming message:\n\t{}\n\t{}\n\t{}", message.getId(), message.getHead(), message.getBody());
            messageStore.consume(message);
            data.remove(messageId);
        }
    }

}

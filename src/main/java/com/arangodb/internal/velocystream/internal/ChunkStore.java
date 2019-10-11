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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class ChunkStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkStore.class);

    private final MessageStore messageStore;
    private final Map<Long, ByteBuffer> data;

    public ChunkStore(final MessageStore messageStore) {
        super();
        this.messageStore = messageStore;
        data = new HashMap<>();
    }

    public void storeChunk(final Chunk chunk, final InputStream inputStream) throws BufferUnderflowException, IndexOutOfBoundsException, IOException {
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

        final byte[] buf = new byte[inputStream.available()];
        readBytesIntoBuffer(inputStream, buf);
        chunkBuffer.put(buf);
        checkCompleteness(messageId, data.get(messageId));
    }

    private void readBytesIntoBuffer(final InputStream inputStream, final byte[] buf) throws IOException {
        final int read = inputStream.read(buf, 0, buf.length);
        if (read == -1) {
            throw new IOException("Reached the end of the stream.");
        }
    }

    private void checkCompleteness(final long messageId, final ByteBuffer chunkBuffer)
            throws BufferUnderflowException, IndexOutOfBoundsException {
        if (chunkBuffer.position() == chunkBuffer.limit()) {
            Message message = new Message(messageId, chunkBuffer.array());
            LOGGER.info("consuming message:\n\t{}\n\t{}\n\t{}", message.getId(), message.getHead(), message.getBody());
            messageStore.consume(message);
            data.remove(messageId);
        }
    }

}

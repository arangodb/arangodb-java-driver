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

import com.arangodb.ArangoDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;

/**
 * @author Mark Vollmary
 */
public class MessageStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageStore.class);

    private final Map<Long, FutureTask<Message>> task;
    private final Map<Long, Message> response;
    private final Map<Long, Exception> error;

    public MessageStore() {
        super();
        task = new ConcurrentHashMap<>();
        response = new ConcurrentHashMap<>();
        error = new ConcurrentHashMap<>();
    }

    public void storeMessage(final long messageId, final FutureTask<Message> future) {
        task.put(messageId, future);
    }

    public void consume(final Message message) {
        final FutureTask<Message> future = task.remove(message.getId());
        if (future != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Received Message (id=%s, head=%s, body=%s)", message.getId(),
                        message.getHead(), message.getBody() != null ? message.getBody() : "{}"));
            }
            response.put(message.getId(), message);
            future.run();
        }
    }

    public Message get(final long messageId) throws ArangoDBException {
        final Message result = response.remove(messageId);
        if (result == null) {
            final Exception e = error.remove(messageId);
            if (e != null) {
                throw new ArangoDBException(e);
            }
        }
        return result;
    }

    public void cancel(final long messageId) {
        final FutureTask<Message> future = task.remove(messageId);
        if (future != null) {
            LOGGER.error(String.format("Cancel Message unexpected (id=%s).", messageId));
            future.cancel(true);
        }
    }

    public void clear(final Exception e) {
        if (!task.isEmpty()) {
            LOGGER.error(e.getMessage(), e);
        }
        for (final Entry<Long, FutureTask<Message>> entry : task.entrySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Exceptionally complete Message (id=%s).", entry.getKey()));
            }
            error.put(entry.getKey(), e);
            entry.getValue().run();
        }
        task.clear();
    }

    public void clear() {
        for (final Entry<Long, FutureTask<Message>> entry : task.entrySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Cancel Message (id=%s).", entry.getKey()));
            }
            entry.getValue().cancel(true);
        }
        task.clear();
    }

    public boolean isEmpty() {
        return task.isEmpty();
    }
}

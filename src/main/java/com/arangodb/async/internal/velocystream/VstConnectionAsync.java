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

package com.arangodb.async.internal.velocystream;

import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.velocystream.internal.Chunk;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.MessageStore;
import com.arangodb.internal.velocystream.internal.VstConnection;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

/**
 * @author Mark Vollmary
 */
public class VstConnectionAsync extends VstConnection {

    private VstConnectionAsync(final HostDescription host, final Integer timeout, final Long ttl, final Boolean useSsl,
                               final SSLContext sslContext, final MessageStore messageStore) {
        super(host, timeout, ttl, useSsl, sslContext, messageStore);
    }

    public synchronized CompletableFuture<Message> write(final Message message, final Collection<Chunk> chunks) {
        final CompletableFuture<Message> future = new CompletableFuture<>();
        final FutureTask<Message> task = new FutureTask<>(() -> {
            try {
                future.complete(messageStore.get(message.getId()));
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
            return null;
        });
        messageStore.storeMessage(message.getId(), task);
        super.writeIntern(message, chunks);
        return future;
    }

    public static class Builder {

        private MessageStore messageStore;
        private HostDescription host;
        private Integer timeout;
        private Long ttl;
        private Boolean useSsl;
        private SSLContext sslContext;

        public Builder() {
            super();
        }

        public Builder messageStore(final MessageStore messageStore) {
            this.messageStore = messageStore;
            return this;
        }

        public Builder host(final HostDescription host) {
            this.host = host;
            return this;
        }

        public Builder timeout(final Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder ttl(final Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder useSsl(final Boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder sslContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public VstConnectionAsync build() {
            return new VstConnectionAsync(host, timeout, ttl, useSsl, sslContext, messageStore);
        }
    }

}

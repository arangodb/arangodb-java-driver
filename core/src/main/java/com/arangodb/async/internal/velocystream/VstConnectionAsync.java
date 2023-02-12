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

import com.arangodb.async.internal.utils.CompletableFutureUtils;
import com.arangodb.config.HostDescription;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.velocystream.internal.Chunk;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.VstConnection;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Mark Vollmary
 */
public class VstConnectionAsync extends VstConnection<CompletableFuture<Message>> {

    private VstConnectionAsync(final ArangoConfig config, final HostDescription host) {
        super(config, host);
    }

    @Override
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
        if (timeout == null || timeout == 0L) {
            return future;
        } else {
            return CompletableFutureUtils.orTimeout(future, timeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void doKeepAlive() {
        sendKeepAlive().join();
    }

    public static class Builder extends VstConnection.Builder<Builder> {
        public VstConnectionAsync build() {
            return new VstConnectionAsync(config, host);
        }
    }

}

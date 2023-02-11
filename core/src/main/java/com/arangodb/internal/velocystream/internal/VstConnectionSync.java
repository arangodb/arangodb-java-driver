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
import com.arangodb.config.HostDescription;
import com.arangodb.internal.config.ArangoConfig;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Mark Vollmary
 */
public class VstConnectionSync extends VstConnection<Message> {

    private VstConnectionSync(final ArangoConfig config, final HostDescription host) {
        super(config, host);
    }

    @Override
    public Message write(final Message message, final Collection<Chunk> chunks) {
        final FutureTask<Message> task = new FutureTask<>(() -> messageStore.get(message.getId()));
        messageStore.storeMessage(message.getId(), task);
        super.writeIntern(message, chunks);
        try {
            return timeout == null || timeout == 0L ? task.get() : task.get(timeout, TimeUnit.MILLISECONDS);
        } catch (final ExecutionException e) {
            throw ArangoDBException.wrap(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArangoDBException(e);
        } catch (final Exception e) {
            throw ArangoDBException.wrap(e);
        }
    }

    @Override
    protected void doKeepAlive() {
        sendKeepAlive();
    }

    public static class Builder {

        private ArangoConfig config;
        private HostDescription host;

        public Builder config(final ArangoConfig config) {
            this.config = config;
            return this;
        }

        public Builder host(final HostDescription host) {
            this.host = host;
            return this;
        }

        public VstConnectionSync build() {
            return new VstConnectionSync(config, host);
        }
    }

}

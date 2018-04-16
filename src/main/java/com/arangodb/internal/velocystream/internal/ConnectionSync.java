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

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.net.ssl.SSLContext;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.HostHandler;

/**
 * @author Mark Vollmary
 *
 */
public class ConnectionSync extends VstConnection {

	public static class Builder {

		private final MessageStore messageStore;
		private HostHandler hostHandler;
		private Integer timeout;
		private Long ttl;
		private Boolean useSsl;
		private SSLContext sslContext;

		public Builder(final MessageStore messageStore) {
			super();
			this.messageStore = messageStore;
		}

		public Builder hostHandler(final HostHandler hostHandler) {
			this.hostHandler = hostHandler;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
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

		public Builder ttl(final Long ttl) {
			this.ttl = ttl;
			return this;
		}

		public ConnectionSync build() {
			return new ConnectionSync(hostHandler, timeout, ttl, useSsl, sslContext, messageStore);
		}
	}

	private ConnectionSync(final HostHandler hostHandler, final Integer timeout, final Long ttl, final Boolean useSsl,
		final SSLContext sslContext, final MessageStore messageStore) {
		super(hostHandler, timeout, ttl, useSsl, sslContext, messageStore);
	}

	public Message write(final Message message, final Collection<Chunk> chunks) throws ArangoDBException {
		final FutureTask<Message> task = new FutureTask<Message>(new Callable<Message>() {
			@Override
			public Message call() throws Exception {
				return messageStore.get(message.getId());
			}
		});
		messageStore.storeMessage(message.getId(), task);
		super.writeIntern(message, chunks);
		try {
			return task.get();
		} catch (final InterruptedException e) {
			throw new ArangoDBException(e);
		} catch (final ExecutionException e) {
			throw new ArangoDBException(e);
		} catch (final CancellationException e) {
			throw new ArangoDBException(e);
		}
	}

}

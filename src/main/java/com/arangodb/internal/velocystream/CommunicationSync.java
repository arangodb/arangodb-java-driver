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

package com.arangodb.internal.velocystream;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CommunicationSync extends Communication<Response, ConnectionSync> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationSync.class);

	public static class Builder {
		private String host;
		private Integer port;
		private Integer timeout;
		private String user;
		private String password;
		private Boolean useSsl;
		private SSLContext sslContext;
		private Integer chunksize;

		public Builder() {
			super();
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final Integer port) {
			this.port = port;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder user(final String user) {
			this.user = user;
			return this;
		}

		public Builder password(final String password) {
			this.password = password;
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

		public Builder chunksize(final Integer chunksize) {
			this.chunksize = chunksize;
			return this;
		}

		public Communication<Response, ConnectionSync> build(final VPack vpack, final CollectionCache collectionCache) {
			return new CommunicationSync(host, port, timeout, user, password, useSsl, sslContext, vpack,
					collectionCache, chunksize);
		}
	}

	protected CommunicationSync(final String host, final Integer port, final Integer timeout, final String user,
		final String password, final Boolean useSsl, final SSLContext sslContext, final VPack vpack,
		final CollectionCache collectionCache, final Integer chunksize) {
		super(host, port, timeout, user, password, useSsl, sslContext, vpack, collectionCache, chunksize,
				new ConnectionSync.Builder(new MessageStore()).host(host).port(port).timeout(timeout).useSsl(useSsl)
						.sslContext(sslContext).build());
	}

	@Override
	public Response execute(final Request request) throws ArangoDBException {
		connect(connection);
		try {
			final Message requestMessage = createMessage(request);
			final Message responseMessage = send(requestMessage);
			collectionCache.setDb(request.getDatabase());
			final Response response = createResponse(responseMessage);
			checkError(response);
			return response;
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}

	}

	private Message send(final Message message) throws ArangoDBException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Send Message (id=%s, head=%s, body=%s)", message.getId(), message.getHead(),
				message.getBody() != null ? message.getBody() : "{}"));
		}
		return connection.write(message, buildChunks(message));
	}

	@Override
	protected void authenticate() {
		final Response response = execute(
			new AuthenticationRequest(user, password != null ? password : "", ArangoDBConstants.ENCRYPTION_PLAIN));
		checkError(response);
	}

}

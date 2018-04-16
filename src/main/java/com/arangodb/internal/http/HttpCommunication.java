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

package com.arangodb.internal.http;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.Host;
import com.arangodb.internal.net.ArangoDBRedirectException;
import com.arangodb.internal.net.ConnectionPool;
import com.arangodb.internal.net.DelHostHandler;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class HttpCommunication {

	public static class Builder {

		private final HostHandler hostHandler;
		private final Protocol protocol;
		private Integer timeout;
		private Long connectionTtl;
		private String user;
		private String password;
		private Boolean useSsl;
		private SSLContext sslContext;
		private Integer maxConnections;

		public Builder(final HostHandler hostHandler, final Protocol protocol) {
			super();
			this.hostHandler = hostHandler;
			this.protocol = protocol;
		}

		public Builder(final Builder builder) {
			this(builder.hostHandler, builder.protocol);
			timeout(builder.timeout).user(builder.user).password(builder.password).useSsl(builder.useSsl)
					.sslContext(builder.sslContext).maxConnections(builder.maxConnections);
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

		public Builder maxConnections(final Integer maxConnections) {
			this.maxConnections = maxConnections;
			return this;
		}

		public Builder connectionTtl(final Long connectionTtl) {
			this.connectionTtl = connectionTtl;
			return this;
		}

		public HttpCommunication build(final ArangoSerialization util) {
			return new HttpCommunication(timeout, user, password, useSsl, sslContext, util, hostHandler, maxConnections,
					protocol, connectionTtl);
		}
	}

	private final ConnectionPool<HttpConnection> connectionPool;

	private HttpCommunication(final Integer timeout, final String user, final String password, final Boolean useSsl,
		final SSLContext sslContext, final ArangoSerialization util, final HostHandler hostHandler,
		final Integer maxConnections, final Protocol contentType, final Long connectionTtl) {
		super();
		connectionPool = new ConnectionPool<HttpConnection>(
				maxConnections != null ? Math.max(1, maxConnections) : ArangoDBConstants.MAX_CONNECTIONS_HTTP_DEFAULT) {
			@Override
			public HttpConnection createConnection(final Host host) {
				return new HttpConnection(timeout, user, password, useSsl, sslContext, util,
						new DelHostHandler(hostHandler, host), contentType, connectionTtl);
			}
		};
	}

	public void disconnect() throws IOException {
		connectionPool.disconnect();
	}

	public Response execute(final Request request, final HostHandle hostHandle) throws ArangoDBException, IOException {
		final HttpConnection connection = connectionPool.connection(hostHandle);
		try {
			return execute(request, connection);
		} catch (final ArangoDBException e) {
			if (e instanceof ArangoDBRedirectException) {
				final String location = ArangoDBRedirectException.class.cast(e).getLocation();
				final Host host = HostUtils.createFromLocation(location);
				connectionPool.closeConnectionOnError(connection);
				return execute(request, new HostHandle().setHost(host));
			} else {
				throw e;
			}
		}
	}

	protected Response execute(final Request request, final HttpConnection connection)
			throws ArangoDBException, IOException {
		return connection.execute(request);
	}

}

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

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;

import com.arangodb.internal.Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.AccessType;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class HttpCommunication extends Communication<Response> implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);

	public static class Builder {

		private final HostHandler hostHandler;

		public Builder(final HostHandler hostHandler) {
			this.hostHandler = hostHandler;
		}

		public Builder(final Builder builder) {
			this(builder.hostHandler);
		}

		public HttpCommunication build(final ArangoSerialization util) {
			return new HttpCommunication(hostHandler);
		}
	}

	private HttpCommunication(final HostHandler hostHandler) {
		super(hostHandler);
	}

	@Override
	public void close() throws IOException {
		hostHandler.close();
	}

	@Override
	public Response execute(final Request request, final HostHandle hostHandle) {
		final AccessType accessType = RequestUtils.determineAccessType(request);
		Host host = hostHandler.get(hostHandle, accessType);
		try {
			while (true) {
				try {
					final HttpConnection connection = (HttpConnection) host.connection();
					final Response response = connection.execute(request);
					hostHandler.success();
					hostHandler.confirm();
					return response;
				} catch (final SocketException se) {
					hostHandler.fail();
					if (hostHandle != null && hostHandle.getHost() != null) {
						hostHandle.setHost(null);
					}
					final Host failedHost = host;
					host = hostHandler.get(hostHandle, accessType);
					if (host != null) {
						LOGGER.warn(String.format("Could not connect to %s. Try connecting to %s",
							failedHost.getDescription(), host.getDescription()));
					} else {
						throw se;
					}
				}
			}
		} catch (ArangoDBException e) {
			return handleArangoDBException(e, request);
		} catch (IOException e) {
			throw new ArangoDBException(e);
		}
	}

}

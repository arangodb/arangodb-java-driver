/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import javax.net.ssl.SSLContext;

import com.arangodb.Protocol;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.util.ArangoSerialization;

/**
 * @author Mark Vollmary
 *
 */
public class HttpConnectionFactory implements ConnectionFactory {

	private final HttpConnection.Builder builder;

	public HttpConnectionFactory(final Integer timeout, final String user, final String password, final Boolean useSsl,
		final SSLContext sslContext, final ArangoSerialization util, final Protocol protocol,
		final Long connectionTtl, String httpCookieSpec) {
		super();
		builder = new HttpConnection.Builder().timeout(timeout).user(user).password(password).useSsl(useSsl)
				.sslContext(sslContext).serializationUtil(util).contentType(protocol).ttl(connectionTtl).httpCookieSpec(httpCookieSpec);

	}

	@Override
	public Connection create(final HostDescription host) {
		return builder.host(host).build();
	}

}

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

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDefaults;
import com.arangodb.internal.net.*;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.internal.util.ResponseUtils;
import com.arangodb.internal.velocystream.internal.Chunk;
import com.arangodb.internal.velocystream.internal.Message;
import com.arangodb.internal.velocystream.internal.VstConnection;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Mark Vollmary
 *
 */
public abstract class VstCommunication<R, C extends VstConnection> implements Closeable {

	protected static final String ENCRYPTION_PLAIN = "plain";
	private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunication.class);

	protected static final AtomicLong mId = new AtomicLong(0L);
	protected final ArangoSerialization util;

	protected final String user;
	protected final String password;

	protected final Integer chunksize;
	private final HostHandler hostHandler;

	protected VstCommunication(final Integer timeout, final String user, final String password, final Boolean useSsl,
		final SSLContext sslContext, final ArangoSerialization util, final Integer chunksize,
		final HostHandler hostHandler) {
		this.user = user;
		this.password = password;
		this.util = util;
		this.hostHandler = hostHandler;
		this.chunksize = chunksize != null ? chunksize : ArangoDefaults.CHUNK_DEFAULT_CONTENT_SIZE;
	}

	@SuppressWarnings("unchecked")
	protected synchronized C connect(final HostHandle hostHandle, final AccessType accessType) {
		Host host = hostHandler.get(hostHandle, accessType);
		while (true) {
			if (host == null) {
				hostHandler.reset();
				throw new ArangoDBException("Was not able to connect to any host");
			}
			final C connection = (C) host.connection();
			if (connection.isOpen()) {
				return connection;
			} else {
				try {
					connection.open();
					hostHandler.success();
					if (user != null) {
						authenticate(connection);
					}
					hostHandler.confirm();
					return connection;
				} catch (final IOException e) {
					hostHandler.fail();
					if (hostHandle != null && hostHandle.getHost() != null) {
						hostHandle.setHost(null);
					}
					final Host failedHost = host;
					host = hostHandler.get(hostHandle, accessType);
					if (host != null) {
						LOGGER.warn(
							String.format("Could not connect to %s or SSL Handshake failed. Try connecting to %s",
								failedHost.getDescription(), host.getDescription()));
					} else {
						LOGGER.error(e.getMessage(), e);
						throw new ArangoDBException(e);
					}
				}
			}
		}
	}

	protected abstract void authenticate(final C connection);

	@Override
	public void close() throws IOException {
		hostHandler.close();
	}

	public R execute(final Request request, final HostHandle hostHandle) throws ArangoDBException {
		try {
			final C connection = connect(hostHandle, RequestUtils.determineAccessType(request));
			return execute(request, connection);
		} catch (final ArangoDBException e) {
			if (e instanceof ArangoDBRedirectException) {
                final String location = ((ArangoDBRedirectException) e).getLocation();
				final HostDescription redirectHost = HostUtils.createFromLocation(location);
				hostHandler.closeCurrentOnError();
				hostHandler.fail();
				return execute(request, new HostHandle().setHost(redirectHost));
			} else {
				throw e;
			}
		}
	}

	protected abstract R execute(final Request request, C connection) throws ArangoDBException;

	protected void checkError(final Response response) throws ArangoDBException {
		ResponseUtils.checkError(util, response);
	}

	protected Response createResponse(final Message message) throws VPackParserException {
		final Response response = util.deserialize(message.getHead(), Response.class);
		if (message.getBody() != null) {
			response.setBody(message.getBody());
		}
		return response;
	}

	protected Message createMessage(final Request request) throws VPackParserException {
		final long id = mId.incrementAndGet();
		return new Message(id, util.serialize(request), request.getBody());
	}

	protected Collection<Chunk> buildChunks(final Message message) {
        final Collection<Chunk> chunks = new ArrayList<>();
		final VPackSlice head = message.getHead();
		int size = head.getByteSize();
		final VPackSlice body = message.getBody();
		if (body != null) {
			size += body.getByteSize();
		}
		final int n = size / chunksize;
		final int numberOfChunks = (size % chunksize != 0) ? (n + 1) : n;
		int off = 0;
		for (int i = 0; size > 0; i++) {
			final int len = Math.min(chunksize, size);
			final long messageLength = (i == 0 && numberOfChunks > 1) ? size : -1L;
			final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, messageLength, off, len);
			size -= len;
			off += len;
			chunks.add(chunk);
		}
		return chunks;
	}

}

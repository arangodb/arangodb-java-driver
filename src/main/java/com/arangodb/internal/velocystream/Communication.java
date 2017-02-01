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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class Communication<R, C extends Connection> {

	private static final int ERROR_STATUS = 300;

	private static final Logger LOGGER = LoggerFactory.getLogger(Communication.class);

	protected static final AtomicLong mId = new AtomicLong(0L);
	protected final VPack vpack;
	protected final C connection;
	protected final CollectionCache collectionCache;

	protected final String user;
	protected final String password;

	protected final Integer chunksize;

	protected Communication(final String host, final Integer port, final Integer timeout, final String user,
		final String password, final Boolean useSsl, final SSLContext sslContext, final VPack vpack,
		final CollectionCache collectionCache, final Integer chunksize, final C connection) {
		this.user = user;
		this.password = password;
		this.vpack = vpack;
		this.collectionCache = collectionCache;
		this.connection = connection;
		this.chunksize = chunksize != null ? chunksize : ArangoDBConstants.CHUNK_DEFAULT_CONTENT_SIZE;
	}

	protected void connect(final Connection connection) {
		if (!connection.isOpen()) {
			try {
				connection.open();
				if (user != null) {
					authenticate();
				}
			} catch (final IOException e) {
				LOGGER.error(e.getMessage(), e);
				throw new ArangoDBException(e);
			}
		}
	}

	protected abstract void authenticate();

	public void disconnect() {
		disconnect(connection);
	}

	public void disconnect(final Connection connection) {
		connection.close();
	}

	public abstract R execute(final Request request) throws ArangoDBException;

	protected void checkError(final Response response) throws ArangoDBException {
		try {
			if (response.getResponseCode() >= ERROR_STATUS) {
				if (response.getBody() != null) {
					final ErrorEntity errorEntity = vpack.deserialize(response.getBody(), ErrorEntity.class);
					throw new ArangoDBException(errorEntity);
				} else {
					throw new ArangoDBException(String.format("Response Code: %s", response.getResponseCode()));
				}
			}
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

	protected Response createResponse(final Message messsage) throws VPackParserException {
		final Response response = vpack.deserialize(messsage.getHead(), Response.class);
		if (messsage.getBody() != null) {
			response.setBody(messsage.getBody());
		}
		return response;
	}

	protected Message createMessage(final Request request) throws VPackParserException {
		final long id = mId.incrementAndGet();
		return new Message(id, vpack.serialize(request), request.getBody());
	}

	protected Collection<Chunk> buildChunks(final Message message) {
		final Collection<Chunk> chunks = new ArrayList<Chunk>();
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

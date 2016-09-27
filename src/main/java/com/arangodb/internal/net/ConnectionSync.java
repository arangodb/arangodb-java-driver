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

package com.arangodb.internal.net;

import java.io.IOException;
import java.util.Collection;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ConnectionSync extends Connection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSync.class);

	public static class Builder {

		private String host;
		private Integer port;
		private Integer timeout;
		private Boolean useSsl;
		private SSLContext sslContext;

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

		public Builder useSsl(final Boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		public Builder sslContext(final SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		public ConnectionSync build() {
			return new ConnectionSync(host, port, timeout, useSsl, sslContext);
		}
	}

	private ConnectionSync(final String host, final Integer port, final Integer timeout, final Boolean useSsl,
		final SSLContext sslContext) {
		super(host, port, timeout, useSsl, sslContext);
	}

	public synchronized Message write(final Message message, final Collection<Chunk> chunks) throws ArangoDBException {
		super.writeIntern(message, chunks);
		byte[] chunkBuffer = null;
		int off = 0;
		while (chunkBuffer == null || off < chunkBuffer.length) {
			if (!isOpen()) {
				close();
				throw new ArangoDBException(new IOException("The socket is closed."));
			}
			try {
				final Chunk chunk = readChunk();
				final int contentLength = chunk.getContentLength();
				if (chunkBuffer == null) {
					if (!chunk.isFirstChunk()) {
						throw new ArangoDBException("Wrong Chunk recieved! Expected first Chunk.");
					}
					final int length = (int) (chunk.getMessageLength() > 0 ? chunk.getMessageLength() : contentLength);
					chunkBuffer = new byte[length];
				}
				readBytesIntoBuffer(chunkBuffer, off, contentLength);
				off += contentLength;
			} catch (final Exception e) {
				close();
				throw new ArangoDBException(e);
			}
		}
		final Message responseMessage = new Message(message.getId(), chunkBuffer);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Received Message (id=%s, head=%s, body=%s)", responseMessage.getId(),
				responseMessage.getHead(),
				responseMessage.getBody().isPresent() ? responseMessage.getBody().get() : "{}"));
		}
		return responseMessage;
	}

}

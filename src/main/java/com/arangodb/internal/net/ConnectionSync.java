package com.arangodb.internal.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ConnectionSync extends Connection {

	public static class Builder {

		private String host;
		private Integer port;
		private Integer timeout;

		public Builder() {
			super();
		}

		public Builder host(final String host) {
			this.host = host;
			return this;
		}

		public Builder port(final int port) {
			this.port = port;
			return this;
		}

		public Builder timeout(final Integer timeout) {
			this.timeout = timeout;
			return this;
		}

		public ConnectionSync build() {
			return new ConnectionSync(host, port, timeout);
		}
	}

	private ConnectionSync(final String host, final Integer port, final Integer timeout) {
		super(host, port, timeout);
	}

	public synchronized Message write(final long messageId, final Collection<Chunk> chunks) throws ArangoDBException {
		super.writeIntern(messageId, chunks);
		ByteBuffer chunkBuffer = null;
		while (chunkBuffer == null || chunkBuffer.position() < chunkBuffer.limit()) {
			if (!isOpen()) {
				close();
				throw new ArangoDBException(new IOException("The socket is closed."));
			}
			try {
				final Chunk chunk = read();
				if (chunkBuffer == null) {
					if (!chunk.isFirstChunk()) {
						throw new ArangoDBException("Wrong Chunk recieved! Expected first Chunk.");
					}
					final int length = (int) (chunk.getMessageLength() > 0 ? chunk.getMessageLength()
							: chunk.getContent().length);
					chunkBuffer = ByteBuffer.allocate(length);
				}
				chunkBuffer.put(chunk.getContent());
			} catch (final Exception e) {
				close();
				throw new ArangoDBException(e);
			}
		}
		return new Message(messageId, chunkBuffer);
	}

}

package com.arangodb.internal.net.velocystream;

import java.nio.ByteBuffer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Chunk {

	private final int length;
	private final long messageId;
	private final int chunkX;
	private final byte[] content;

	public static class Builder {
		private int length;
		private ByteBuffer data;

		public Builder length(final int length) {
			this.length = length;
			return this;
		}

		public Builder data(final ByteBuffer data) {
			this.data = data;
			return this;
		}

		public Chunk build() {
			return new Chunk(length, data);
		}
	}

	private Chunk(final int length, final ByteBuffer data) {
		this.length = length;
		chunkX = data.getInt();
		messageId = data.getLong();
		content = new byte[length - 4 - 4 - 8];
		data.get(content);
	}

	public int getLength() {
		return length;
	}

	public long getMessageId() {
		return messageId;
	}

	public boolean isFirstChunk() {
		return 1 == (chunkX & 0x1);
	}

	public int getChunk() {
		return chunkX >> 1;
	}

	public byte[] getContent() {
		return content;
	}

	public ByteBuffer toByteBuffer() {
		return null;
	}

}

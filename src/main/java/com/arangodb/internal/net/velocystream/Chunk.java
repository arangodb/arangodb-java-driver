package com.arangodb.internal.net.velocystream;

import java.nio.ByteBuffer;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Chunk {

	public static final int MAX_CHUNK_CONTENT_SIZE = 1024;

	private final int length;
	private final long messageId;
	private final int chunkX;
	private final byte[] content;

	public Chunk(final int length, final ByteBuffer data) {
		this.length = length;
		chunkX = data.getInt();
		messageId = data.getLong();
		content = new byte[length - 4 - 4 - 8];
		data.get(content);
	}

	public Chunk(final long messageId, final int chunk, final int numberOfChunks, final byte[] content,
		final int length) {
		this.messageId = messageId;
		this.content = content;
		this.length = length;
		chunkX = chunk >> 1;// TODO
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
		// TODO
		return null;
	}

}

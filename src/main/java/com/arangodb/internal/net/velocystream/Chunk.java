package com.arangodb.internal.net.velocystream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Chunk {

	public static final int MAX_CHUNK_BODY_SIZE = 50;
	public static final int CHUNK_MIN_HEADER_SIZE = Integer.BYTES + Integer.BYTES + Long.BYTES;

	private final int length;
	private final long messageId;
	private final long messageLength;
	private final int chunkX;
	private final byte[] content;

	public Chunk(final long messageId, final int chunkX, final byte[] content, final int length,
		final long messageLength) {
		this.messageId = messageId;
		this.chunkX = chunkX;
		this.content = content;
		this.length = length;
		this.messageLength = messageLength;

	}

	public Chunk(final long messageId, final int chunkIndex, final int numberOfChunks, final byte[] content,
		final int length, final long messageLength) {
		this.messageId = messageId;
		this.content = content;
		this.length = length;
		this.messageLength = messageLength;
		if (numberOfChunks == 1) {
			chunkX = 3;// last byte: 0000 0011
		} else if (chunkIndex == 0) {
			chunkX = (numberOfChunks << 1) + 1;
		} else {
			chunkX = chunkIndex << 1;
		}
	}

	public int getLength() {
		return length;
	}

	public long getMessageId() {
		return messageId;
	}

	public long getMessageLength() {
		return messageLength;
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
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(length);
		buffer.putInt(chunkX);
		buffer.putLong(messageId);
		if (messageLength > -1L) {
			buffer.putLong(messageLength);
		}
		buffer.put(content);
		return buffer;
	}

}

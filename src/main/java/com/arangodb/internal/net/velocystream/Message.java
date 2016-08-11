package com.arangodb.internal.net.velocystream;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Message {

	private final long id;
	private final VPackSlice head;
	private final Optional<VPackSlice> body;

	public Message(final long id, final ByteBuffer chunkBuffer)
			throws BufferUnderflowException, IndexOutOfBoundsException {
		super();
		this.id = id;
		final byte[] array = chunkBuffer.array();
		head = new VPackSlice(array);
		final int headSize = head.getByteSize();
		if (array.length > headSize) {
			body = Optional.of(new VPackSlice(array, headSize));
		} else {
			body = Optional.empty();
		}
	}

	public Message(final long id, final VPackSlice head, final VPackSlice body) {
		super();
		this.id = id;
		this.head = head;
		this.body = Optional.ofNullable(body);
	}

	public long getId() {
		return id;
	}

	public VPackSlice getHead() {
		return head;
	}

	public Optional<VPackSlice> getBody() {
		return body;
	}

}

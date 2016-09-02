package com.arangodb.internal.net.velocystream;

import java.nio.BufferUnderflowException;
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

	public Message(final long id, final byte[] chunkBuffer) throws BufferUnderflowException, IndexOutOfBoundsException {
		super();
		this.id = id;
		head = new VPackSlice(chunkBuffer);
		final int headSize = head.getByteSize();
		if (chunkBuffer.length > headSize) {
			body = Optional.of(new VPackSlice(chunkBuffer, headSize));
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

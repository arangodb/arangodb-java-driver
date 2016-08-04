package com.arangodb.internal.net.velocystream;

import java.nio.ByteBuffer;
import java.util.Collection;
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

	public Message(final long id, final Collection<Chunk> chunks) {
		super();
		this.id = id;
		final int capacity = chunks.stream().mapToInt(chunk -> chunk.getContent().length).sum();
		final ByteBuffer buffer = ByteBuffer.allocate(capacity);
		chunks.stream().forEach(chunk -> buffer.put(chunk.getContent()));

		final byte[] array = buffer.array();
		final VPackSlice slice = new VPackSlice(array);

		final byte[] headBuf = new byte[slice.getByteSize()];
		buffer.get(headBuf, 0, headBuf.length);
		head = new VPackSlice(headBuf);

		if (array.length > headBuf.length) {
			final byte[] bodyBuf = new byte[array.length - headBuf.length];
			buffer.get(bodyBuf, headBuf.length, bodyBuf.length);
			body = Optional.of(new VPackSlice(bodyBuf));
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

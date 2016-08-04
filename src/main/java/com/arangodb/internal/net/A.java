package com.arangodb.internal.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.arangodb.internal.net.velocystream.Chunk;
import com.arangodb.internal.net.velocystream.Message;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class A {

	private static final int MAX_CHUNK_CONTENT_SIZE = 1024;

	public void machIrgendwas(final Message message) throws IOException {

		final Collection<Chunk> chunks = new ArrayList<>();
		final Collection<VPackSlice> vpacks = message.getVpacks();

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		int size = 0;
		for (final VPackSlice vpack : vpacks) {
			final int byteSize = vpack.getByteSize();
			out.write(vpack.getVpack(), vpack.getStart(), byteSize);
			size += byteSize;
		}
		out.close();
		final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		final int n = size / MAX_CHUNK_CONTENT_SIZE;
		final int numberOfChunks = (size % MAX_CHUNK_CONTENT_SIZE != 0) ? (n + 1) : n;
		final byte[] buffer = new byte[MAX_CHUNK_CONTENT_SIZE];
		for (int pos = 0, i = 0; size > 0; pos += MAX_CHUNK_CONTENT_SIZE, i++) {
			final int len = Math.min(MAX_CHUNK_CONTENT_SIZE, size);
			in.read(buffer, pos, len);
			size -= len;
			final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, buffer, len);
			chunks.add(chunk);
		}
		in.close();
	}

}

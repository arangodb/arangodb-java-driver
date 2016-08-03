package com.arangodb.velocypack;

import java.util.Iterator;

import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public abstract class SliceIterator implements Iterator<VPackSlice> {

	protected final VPackSlice slice;
	protected final long size;
	protected long position;
	protected long current;

	protected SliceIterator(final VPackSlice slice) throws VPackValueTypeException {
		super();
		this.slice = slice;
		size = slice.getLength();
		position = 0;
	}

	@Override
	public boolean hasNext() {
		return position < size;
	}

	protected VPackSlice getCurrent() {
		return new VPackSlice(slice.getVpack(), (int) current);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

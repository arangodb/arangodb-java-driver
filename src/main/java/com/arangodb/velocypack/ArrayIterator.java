package com.arangodb.velocypack;

import java.util.NoSuchElementException;

import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArrayIterator extends SliceIterator<VPackSlice> {

	public ArrayIterator(final VPackSlice slice) throws VPackValueTypeException {
		super(slice);
		if (!slice.isArray()) {
			throw new VPackValueTypeException(ValueType.ARRAY);
		}
	}

	@Override
	public VPackSlice next() {
		final VPackSlice next;
		if (hasNext()) {
			next = slice.get((int) position++);
		} else {
			throw new NoSuchElementException();
		}
		return next;
	}

}

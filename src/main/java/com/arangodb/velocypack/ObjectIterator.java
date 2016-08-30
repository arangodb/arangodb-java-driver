package com.arangodb.velocypack;

import java.util.NoSuchElementException;

import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ObjectIterator extends SliceIterator {

	/**
	 * @param slice
	 * @throws VPackValueTypeException
	 */
	public ObjectIterator(final VPackSlice slice) throws VPackValueTypeException {
		super(slice);
		if (!slice.isObject()) {
			throw new VPackValueTypeException(ValueType.OBJECT);
		}
		if (size > 0) {
			final byte head = slice.head();
			if (head == 0x14) {
				current = slice.keyAt(0).getStart();
			} else {
				current = slice.getStart() + slice.findDataOffset();
			}
		}
	}

	@Override
	public VPackSlice next() {
		if (position++ > 0) {
			if (position <= size && current != 0) {
				// skip over key
				current += getCurrent().getByteSize();
				// skip over value
				current += getCurrent().getByteSize();
			} else {
				throw new NoSuchElementException();
			}
		}
		return getCurrent();
	}

}

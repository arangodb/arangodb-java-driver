package com.arangodb.velocypack;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.arangodb.velocypack.exception.VPackKeyTypeException;
import com.arangodb.velocypack.exception.VPackNeedAttributeTranslatorException;
import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ObjectIterator extends SliceIterator<Entry<String, VPackSlice>> {

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
	public Entry<String, VPackSlice> next() {
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
		final VPackSlice currentField = getCurrent();
		return new Entry<String, VPackSlice>() {
			@Override
			public VPackSlice setValue(final VPackSlice value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public VPackSlice getValue() {
				return new VPackSlice(currentField.getVpack(), currentField.getStart() + currentField.getByteSize());
			}

			@Override
			public String getKey() {
				try {
					return currentField.makeKey().getAsString();
				} catch (VPackKeyTypeException | VPackNeedAttributeTranslatorException e) {
					throw new NoSuchElementException();
				}
			}
		};
	}

}

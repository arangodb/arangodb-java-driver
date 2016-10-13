/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

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
				return new VPackSlice(currentField.getBuffer(), currentField.getStart() + currentField.getByteSize());
			}

			@Override
			public String getKey() {
				try {
					return currentField.makeKey().getAsString();
				} catch (final VPackKeyTypeException e) {
					throw new NoSuchElementException();
				} catch (final VPackNeedAttributeTranslatorException e) {
					throw new NoSuchElementException();
				}
			}
		};
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

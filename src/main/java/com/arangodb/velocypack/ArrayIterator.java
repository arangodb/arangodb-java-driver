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

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

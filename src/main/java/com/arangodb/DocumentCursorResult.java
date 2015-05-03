/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DocumentEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class DocumentCursorResult<T, S extends DocumentEntity<T>> extends CursorResult<S> {

	public DocumentCursorResult(String database, InternalCursorDriver cursorDriver, CursorEntity<S> entity, Class<?>... clazz) {
		super(database, cursorDriver, entity, clazz);
	}

	/**
	 * Returns an iterator over a entity set of elements of type T.
	 * 
	 * @return an Iterator.
	 */
	public Iterator<T> entityIterator() {
		return new EntityIterator();
	}

	/**
	 * Returns the entities of DocumentEntity objects as a list
	 * 
	 * @return list of DocumentEntity objects
	 */
	public List<T> asEntityList() {
		List<T> result = new ArrayList<T>();
		Iterator<S> iterator = iterator();

		while (iterator.hasNext()) {
			T e = iterator.next().getEntity();
			if (e != null) {
				result.add(e);
			}
		}

		return result;
	}

	/**
	 * internal entity iterator
	 */
	public class EntityIterator implements Iterator<T> {

		private Iterator<S> iterator;

		public EntityIterator() {
			iterator = iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public T next() {
			return iterator.next().getEntity();
		}

		@Override
		public void remove() {
			iterator.remove();
		}

	}
}

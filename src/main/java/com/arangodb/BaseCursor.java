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
import java.util.NoSuchElementException;

import com.arangodb.entity.BaseCursorEntity;
import com.arangodb.entity.DocumentEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class BaseCursor<T, S extends DocumentEntity<T>> implements Iterable<S> {

	private String database;
	private transient InternalCursorDocumentDriver cursorDriver;
	private transient Class<S> classDocumentEntity;
	private transient Class<T> clazz;
	private transient BaseCursorEntity<T, S> entity;
	private transient int pos;
	private int count;

	public BaseCursor(String database, InternalCursorDocumentDriver cursorDriver, BaseCursorEntity<T, S> entity,
		Class<S> classDocumentEntity, Class<T> clazz) {
		this.database = database;
		this.cursorDriver = cursorDriver;
		this.classDocumentEntity = classDocumentEntity;
		this.clazz = clazz;
		this.entity = entity;
		this.count = entity == null ? 0 : entity.getCount();
		this.pos = 0;
	}

	@Override
	public Iterator<S> iterator() {
		return new DocumentEntityIterator();
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
	 * Returns the DocumentEntity objects as a list
	 * 
	 * @return list of DocumentEntity objects
	 */
	public List<S> asList() {
		List<S> result = new ArrayList<S>();
		Iterator<S> iterator = iterator();

		while (iterator.hasNext()) {
			result.add(iterator.next());
		}

		return result;
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
	 * Close cursor (removes cursor from database)
	 * 
	 * @throws ArangoException
	 */
	public void close() throws ArangoException {
		long cursorId = entity.getCursorId();
		cursorDriver.finishQuery(database, cursorId);
	}

	/**
	 * Get total number of results (if requested)
	 * 
	 * @return total number of results
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Return a single instance that matches the query, or null if the query
	 * returns no results.
	 * 
	 * Throws NonUniqueResultException (RuntimeException) if there is more than
	 * one matching result
	 * 
	 * @return the single result or null
	 */
	public S getUniqueResult() {
		return entity.getUniqueResult();
	}

	/**
	 * read more values
	 * 
	 * @throws ArangoException
	 */
	private void updateEntity() throws ArangoException {
		long cursorId = entity.getCursorId();
		this.entity = cursorDriver.continueBaseCursorEntityQuery(database, cursorId, classDocumentEntity, clazz);
		this.pos = 0;
	}

	/**
	 * internal DocumentEntity iterator
	 */
	public class DocumentEntityIterator implements Iterator<S> {

		@Override
		public boolean hasNext() {
			if (entity == null) {
				return false;
			}
			if (pos < entity.size()) {
				return true;
			}
			if (entity.hasMore()) {
				return true;
			}
			return false;
		}

		@Override
		public S next() {
			if (hasNext()) {
				if (pos >= entity.size()) {
					try {
						updateEntity();
					} catch (ArangoException e) {
						throw new IllegalStateException(e);
					}
				}
				return entity.get(pos++);
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove is not supported");
		}

	}

	/**
	 * internal DocumentEntity iterator
	 */
	public class EntityIterator implements Iterator<T> {

		@Override
		public boolean hasNext() {
			if (entity == null) {
				return false;
			}
			if (pos < entity.size()) {
				return true;
			}
			if (entity.hasMore()) {
				return true;
			}
			return false;
		}

		@Override
		public T next() {
			if (hasNext()) {
				if (pos >= entity.size()) {
					try {
						updateEntity();
					} catch (ArangoException e) {
						throw new IllegalStateException(e);
					}
				}
				return entity.get(pos++).getEntity();
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove is not supported");
		}

	}

}

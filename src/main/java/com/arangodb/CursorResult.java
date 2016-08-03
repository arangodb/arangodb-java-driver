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
import java.util.Map;
import java.util.NoSuchElementException;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.WarningEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CursorResult<T> implements Iterable<T> {

	private String database;
	private InternalCursorDriver cursorDriver;
	private Class<?>[] clazz;
	private CursorEntity<T> entity;
	private int pos;
	private int count;
	private CursorIterator iter;

	public CursorResult(String database, InternalCursorDriver cursorDriver, CursorEntity<T> entity, Class<?>... clazz) {
		this.database = database;
		this.cursorDriver = cursorDriver;
		this.clazz = clazz;
		this.entity = entity;
		this.count = entity == null ? 0 : entity.getCount();
		this.pos = 0;
		this.iter = null;
	}

	@Override
	public Iterator<T> iterator() {
		if (iter == null) {
			iter = new CursorIterator();
		}

		return iter;
	}

	/**
	 * Returns the objects as a list
	 * 
	 * @return list of DocumentEntity objects
	 */
	public List<T> asList() {
		List<T> result = new ArrayList<T>();
		Iterator<T> iterator = iterator();

		while (iterator.hasNext()) {
			result.add(iterator.next());
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
	 * Get total number of results for queries with LIMIT clause
	 * 
	 * @return total number of results
	 */
	public int getFullCount() {
		return entity.getFullCount();
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
	public T getUniqueResult() {
		return entity.getUniqueResult();
	}

	/**
	 * read more values
	 * 
	 * @throws ArangoException
	 */
	private void updateEntity() throws ArangoException {
		long cursorId = entity.getCursorId();
		this.entity = cursorDriver.continueQuery(database, cursorId, this.clazz);
		this.pos = 0;
	}

	/**
	 * Returns the CursorEntity object
	 * 
	 * @return CursorEntity
	 */
	public CursorEntity<T> getEntity() {
		return entity;
	}

	/**
	 * internal iterator
	 */
	public class CursorIterator implements Iterator<T> {

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
				return entity.get(pos++);
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove is not supported!");
		}

	}

	/**
	 * Returns true if the cursor can load more data from the database
	 * 
	 * @return true, if the cursor can load more data from the database
	 */
	public boolean hasMore() {
		return entity.hasMore();
	}

	/**
	 * Returns the cursor identifier
	 * 
	 * @return the cursor identifier
	 */
	public Long getCursorId() {
		return entity.getCursorId();
	}

	/**
	 * a boolean flag indicating whether the query result was served from the
	 * query cache or not. If the query result is served from the query cache,
	 * the extra return attribute will not contain any stats sub-attribute and
	 * no profile sub-attribute. (since ArangoDB 2.7)
	 * 
	 * @return true, if the result is cached
	 * 
	 */
	public boolean isCached() {
		return entity.isCached();
	}

	/**
	 * Returns true, if there are AQL warnings
	 * 
	 * @return true, if there are AQL warnings
	 */
	public boolean hasWarning() {
		return entity.hasWarnings();
	}

	/**
	 * Returns a list of AQL warnings (code and message)
	 * 
	 * @return list of AQL warnings
	 */
	public List<WarningEntity> getWarnings() {
		return entity.getWarnings();
	}

	/**
	 * A list of extra stats returned by the query
	 * 
	 * @return query stats
	 */
	public Map<String, Object> getExtra() {
		return entity.getExtra();
	}

}

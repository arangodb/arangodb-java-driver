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

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.WarningEntity;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CursorRawResult implements Iterable<String> {

	private String database;
	private InternalCursorDriver cursorDriver;
	private CursorEntity<JsonObject> entity;
	private int pos;
	private int count;
	private CursorIterator iter;

	public CursorRawResult(String database, InternalCursorDriver cursorDriver, CursorEntity<JsonObject> entity,
		Class<?>... clazz) {
		this.database = database;
		this.cursorDriver = cursorDriver;
		this.entity = entity;
		this.count = entity == null ? 0 : entity.getCount();
		this.pos = 0;
		this.iter = null;
	}

	@Override
	public Iterator<String> iterator() {
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
	public List<String> asList() {
		List<String> result = new ArrayList<String>();
		Iterator<String> iterator = iterator();

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
	public String getUniqueResult() {
		return entity.getUniqueResult().toString();
	}

	/**
	 * read more values
	 * 
	 * @throws ArangoException
	 */
	private void updateEntity() throws ArangoException {
		long cursorId = entity.getCursorId();
		this.entity = cursorDriver.continueQuery(database, cursorId, JsonObject.class);
		this.pos = 0;
	}

	/**
	 * internal iterator
	 */
	public class CursorIterator implements Iterator<String> {

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
		public String next() {
			if (hasNext()) {
				if (pos >= entity.size()) {
					try {
						updateEntity();
					} catch (ArangoException e) {
						throw new IllegalStateException(e);
					}
				}
				return entity.get(pos++).toString();
			}
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove is not supported!");
		}

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

}

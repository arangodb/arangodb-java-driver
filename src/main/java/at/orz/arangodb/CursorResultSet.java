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

package at.orz.arangodb;

import java.util.Iterator;
import java.util.NoSuchElementException;

import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.impl.InternalCursorDriverImpl;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class CursorResultSet<T> implements Iterable<T> {

	private String database;
	private transient InternalCursorDriverImpl cursorDriver;
	private transient Class<?>[] clazz;
	private transient CursorEntity<T> entity;
	private transient int pos;
	private int totalCount;
	private transient Iterator<T> itr;
	
	public CursorResultSet(String database, InternalCursorDriverImpl cursorDriver, CursorEntity<T> entity, Class<?> ...clazz) {
		this.database = database;
		this.cursorDriver = cursorDriver;
		this.clazz = clazz;
		this.entity = entity;
		this.totalCount = entity == null ? 0 : entity.getCount();
		this.pos = 0;
		this.itr = new CursorIterator();
	}

	public Iterator<T> iterator() {
		return new CursorIterator();
	}
	
	public boolean hasNext() {
		return itr.hasNext();
	}
	
	public T next() {
		return itr.next();
	}
	
	public void close() throws ArangoException {
		long cursorId = entity.getCursorId();
		cursorDriver.finishQuery(database, cursorId);
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	private void updateEntity() throws ArangoException {
		long cursorId = entity.getCursorId();
		this.entity = cursorDriver.continueQuery(database, cursorId, this.clazz);
		this.pos = 0;
	}
	
	public class CursorIterator implements Iterator<T> {

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

		public void remove() {
			throw new UnsupportedOperationException("remove does not support!!");
		}
		
	}
	
}

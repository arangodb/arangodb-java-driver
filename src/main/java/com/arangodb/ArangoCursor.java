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

package com.arangodb;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.CursorEntity.Extras;
import com.arangodb.entity.CursorEntity.Stats;
import com.arangodb.entity.CursorEntity.Warning;
import com.arangodb.internal.ArangoCursorExecute;
import com.arangodb.internal.ArangoCursorIterator;
import com.arangodb.internal.InternalArangoDatabase;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoCursor<T> implements Iterator<T>, Closeable {

	private final Class<T> type;
	protected final ArangoCursorIterator<T> iterator;
	private final String id;
	private final Integer count;
	private final Extras extra;
	private final boolean cached;
	private final ArangoCursorExecute execute;

	public ArangoCursor(final InternalArangoDatabase<?, ?, ?> db, final ArangoCursorExecute execute,
		final Class<T> type, final CursorEntity result) {
		super();
		this.execute = execute;
		this.type = type;
		count = result.getCount();
		extra = result.getExtra();
		cached = result.getCached().booleanValue();
		iterator = new ArangoCursorIterator<T>(this, execute, db, result);
		id = result.getId();
	}

	public String getId() {
		return id;
	}

	public Class<T> getType() {
		return type;
	}

	public Integer getCount() {
		return count;
	}

	public Stats getStats() {
		return extra != null ? extra.getStats() : null;
	}

	public Collection<Warning> getWarnings() {
		return extra != null ? extra.getWarnings() : null;
	}

	public boolean isCached() {
		return cached;
	}

	@Override
	public void close() throws IOException {
		execute.close(id);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	public List<T> asListRemaining() {
		final List<T> remaining = new ArrayList<T>();
		while (hasNext()) {
			remaining.add(next());
		}
		return remaining;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

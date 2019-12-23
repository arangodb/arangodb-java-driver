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

package com.arangodb.internal.cursor;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoIterator;
import com.arangodb.Consumer;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.CursorEntity.Extras;
import com.arangodb.entity.CursorEntity.Stats;
import com.arangodb.entity.CursorEntity.Warning;
import com.arangodb.internal.ArangoCursorExecute;
import com.arangodb.internal.InternalArangoDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mark Vollmary
 */
public class ArangoCursorImpl<T> extends AbstractArangoIterable<T> implements ArangoCursor<T> {

    private final Class<T> type;
    protected final ArangoCursorIterator<T> iterator;
    private final String id;
    private final ArangoCursorExecute execute;

    public ArangoCursorImpl(final InternalArangoDatabase<?, ?> db, final ArangoCursorExecute execute,
                            final Class<T> type, final CursorEntity result) {
        super();
        this.execute = execute;
        this.type = type;
        iterator = createIterator(this, db, execute, result);
        id = result.getId();
    }

    protected ArangoCursorIterator<T> createIterator(
            final ArangoCursor<T> cursor,
            final InternalArangoDatabase<?, ?> db,
            final ArangoCursorExecute execute,
            final CursorEntity result) {
        return new ArangoCursorIterator<>(cursor, execute, db, result);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Integer getCount() {
        return iterator.getResult().getCount();
    }

    @Override
    public Stats getStats() {
        final Extras extra = iterator.getResult().getExtra();
        return extra != null ? extra.getStats() : null;
    }

    @Override
    public Collection<Warning> getWarnings() {
        final Extras extra = iterator.getResult().getExtra();
        return extra != null ? extra.getWarnings() : null;
    }

    @Override
    public boolean isCached() {
        final Boolean cached = iterator.getResult().getCached();
        return Boolean.TRUE == cached;
    }

    @Override
    public void close() {
        if (id != null && hasNext()) {
            execute.close(id, iterator.getResult().getMeta());
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public List<T> asListRemaining() {
        final List<T> remaining = new ArrayList<>();
        while (hasNext()) {
            remaining.add(next());
        }
        return remaining;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArangoIterator<T> iterator() {
        return iterator;
    }

    @Override
    public void foreach(final Consumer<? super T> action) {
        while (hasNext()) {
            action.accept(next());
        }
    }

}

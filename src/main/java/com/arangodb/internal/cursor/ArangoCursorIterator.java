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
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoCursorExecute;
import com.arangodb.internal.InternalArangoDatabase;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @param <T>
 * @author Mark Vollmary
 */
public class ArangoCursorIterator<T> implements ArangoIterator<T> {

    private CursorEntity result;
    private Iterator<JsonNode> arrayIterator;

    private final ArangoCursor<T> cursor;
    private final InternalArangoDatabase<?, ?> db;
    private final ArangoCursorExecute execute;

    protected ArangoCursorIterator(final ArangoCursor<T> cursor, final ArangoCursorExecute execute,
                                   final InternalArangoDatabase<?, ?> db, final CursorEntity result) {
        super();
        this.cursor = cursor;
        this.execute = execute;
        this.db = db;
        this.result = result;
        arrayIterator = result.getResult().iterator();
    }

    public CursorEntity getResult() {
        return result;
    }

    @Override
    public boolean hasNext() {
        return arrayIterator.hasNext() || result.getHasMore();
    }

    @Override
    public T next() {
        if (!arrayIterator.hasNext() && result.getHasMore()) {
            result = execute.next(cursor.getId(), result.getMeta());
            arrayIterator = result.getResult().iterator();
        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return deserialize(db.getInternalSerialization().serialize(arrayIterator.next()), cursor.getType());
    }

    protected <R> R deserialize(final byte[] result, final Class<R> type) {
        return db.getUserSerialization().deserialize(result, type);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

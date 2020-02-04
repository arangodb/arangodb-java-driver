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
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.velocypack.VPackSlice;

import java.util.NoSuchElementException;

/**
 * @param <T>
 * @author Mark Vollmary
 */
public class ArangoCursorIterator<T> implements ArangoIterator<T> {

    private CursorEntity result;
    private int pos;

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
        pos = 0;
    }

    public CursorEntity getResult() {
        return result;
    }

    @Override
    public boolean hasNext() {
        return pos < result.getResult().size() || result.getHasMore();
    }

    @Override
    public T next() {
        if (pos >= result.getResult().size() && result.getHasMore()) {
            result = execute.next(cursor.getId(), result.getMeta());
            pos = 0;
        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return deserialize(result.getResult().get(pos++), cursor.getType());
    }

    protected <R> R deserialize(final VPackSlice result, final Class<R> type) {
        return db.util(Serializer.CUSTOM).deserialize(result, type);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

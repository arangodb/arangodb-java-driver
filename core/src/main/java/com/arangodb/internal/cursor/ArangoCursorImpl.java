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
import com.arangodb.entity.CursorStats;
import com.arangodb.entity.CursorWarning;
import com.arangodb.internal.ArangoCursorExecute;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Mark Vollmary
 */
public class ArangoCursorImpl<T> implements ArangoCursor<T> {

    protected final ArangoCursorIterator<T> iterator;
    private final Class<T> type;
    private final String id;
    private final ArangoCursorExecute<T> execute;
    private final boolean pontentialDirtyRead;
    private final boolean allowRetry;

    public ArangoCursorImpl(final ArangoCursorExecute<T> execute,
                            final Class<T> type, final CursorEntity<T> result) {
        super();
        this.execute = execute;
        this.type = type;
        id = result.getId();
        pontentialDirtyRead = result.isPotentialDirtyRead();
        iterator = new ArangoCursorIterator<>(id, execute, result);
        this.allowRetry = result.getNextBatchId() != null;
    }

    @Override
    public void close() {
        if (getId() != null && (allowRetry || iterator.result.getHasMore())) {
            getExecute().close(getId());
        }
    }

    @Override
    public T next() {
        return iterator.next();
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
        return iterator.result.getCount();
    }

    @Override
    public CursorStats getStats() {
        final CursorEntity.Extras extra = iterator.result.getExtra();
        return extra != null ? extra.getStats() : null;
    }

    @Override
    public Collection<CursorWarning> getWarnings() {
        final CursorEntity.Extras extra = iterator.result.getExtra();
        return extra != null ? extra.getWarnings() : null;
    }

    @Override
    public boolean isCached() {
        final Boolean cached = iterator.result.getCached();
        return Boolean.TRUE.equals(cached);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public boolean isPotentialDirtyRead() {
        return pontentialDirtyRead;
    }

    @Override
    public ArangoIterator<T> iterator() {
        return iterator;
    }

    @Override
    public String getNextBatchId() {
        return iterator.result.getNextBatchId();
    }

    protected ArangoCursorExecute<T> getExecute() {
        return execute;
    }

    protected static class ArangoCursorIterator<T> implements ArangoIterator<T> {
        private final String cursorId;
        private final ArangoCursorExecute<T> execute;
        private CursorEntity<T> result;
        private Iterator<T> arrayIterator;

        protected ArangoCursorIterator(final String cursorId, final ArangoCursorExecute<T> execute,
                                       final CursorEntity<T> result) {
            this.cursorId = cursorId;
            this.execute = execute;
            this.result = result;
            arrayIterator = result.getResult().iterator();
        }

        @Override
        public boolean hasNext() {
            return arrayIterator.hasNext() || result.getHasMore();
        }

        @Override
        public T next() {
            if (!arrayIterator.hasNext() && Boolean.TRUE.equals(result.getHasMore())) {
                result = execute.next(cursorId, result.getNextBatchId());
                arrayIterator = result.getResult().iterator();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return arrayIterator.next();
        }
    }

}


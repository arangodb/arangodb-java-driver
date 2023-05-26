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

import com.arangodb.entity.CursorStats;
import com.arangodb.entity.CursorWarning;
import com.arangodb.model.AqlQueryOptions;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Mark Vollmary
 */
public interface ArangoCursor<T> extends ArangoIterable<T>, ArangoIterator<T>, Closeable {

    /**
     * @return id of temporary cursor created on the server
     */
    String getId();

    /**
     * @return the type of the result elements
     */
    Class<T> getType();

    /**
     * @return the total number of result documents available (only available if the query was executed with the count
     * attribute set)
     */
    Integer getCount();

    /**
     * @return extra information about the query result. For data-modification queries, the stats will contain the
     * number of modified documents and the number of documents that could not be modified due to an error (if
     * ignoreErrors query option is specified)
     */
    CursorStats getStats();

    /**
     * @return warnings which the query could have been produced
     */
    Collection<CursorWarning> getWarnings();

    /**
     * @return indicating whether the query result was served from the query cache or not
     */
    boolean isCached();

    /**
     * @return the remaining results as a {@code List}
     */
    List<T> asListRemaining();

    /**
     * @return true if the result is a potential dirty read
     * @since ArangoDB 3.10
     */
    boolean isPotentialDirtyRead();

    /**
     * @return The ID of the batch after the current one. The first batch has an ID of 1 and the value is incremented by
     * 1 with every batch. Only set if the allowRetry query option is enabled.
     * @since ArangoDB 3.11
     */
    String getNextBatchId();

    /**
     * Returns the next element in the iteration.
     * <p/>
     * If the cursor allows retries (see {@link AqlQueryOptions#allowRetry(Boolean)}), then it is safe to retry invoking
     * this method in case of I/O exceptions (which are actually thrown as {@link com.arangodb.ArangoDBException} with
     * cause {@link java.io.IOException}).
     * <p/>
     * If the cursor does not allow retries (default), then it is not safe to retry invoking this method in case of I/O
     * exceptions, since the request to fetch the next batch is not idempotent (i.e. the cursor may advance multiple
     * times on the server).
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    T next();
}

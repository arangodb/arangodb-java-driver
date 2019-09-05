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

package com.arangodb.async.internal;

import com.arangodb.async.ArangoCursorAsync;
import com.arangodb.entity.CursorEntity;
import com.arangodb.internal.ArangoCursorExecute;
import com.arangodb.internal.InternalArangoDatabase;
import com.arangodb.internal.cursor.ArangoCursorImpl;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Mark Vollmary
 */
public class ArangoCursorAsyncImpl<T> extends ArangoCursorImpl<T> implements ArangoCursorAsync<T> {

    ArangoCursorAsyncImpl(final InternalArangoDatabase<?, ?> db, final ArangoCursorExecute execute,
                          final Class<T> type, final CursorEntity result) {
        super(db, execute, type, result);
    }

    @Override
    public Stream<T> streamRemaining() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }
}

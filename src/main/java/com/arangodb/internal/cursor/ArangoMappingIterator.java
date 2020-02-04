/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.ArangoIterator;
import com.arangodb.Function;

/**
 * @author Mark Vollmary
 */
public class ArangoMappingIterator<R, T> implements ArangoIterator<T> {

    private final ArangoIterator<R> iterator;
    private final Function<? super R, ? extends T> mapper;

    public ArangoMappingIterator(final ArangoIterator<R> iterator, final Function<? super R, ? extends T> mapper) {
        super();
        this.iterator = iterator;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return mapper.apply(iterator.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

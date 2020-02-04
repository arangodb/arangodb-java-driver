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
import com.arangodb.Predicate;

import java.util.NoSuchElementException;

/**
 * @author Mark Vollmary
 */
public class ArangoFilterIterator<T> implements ArangoIterator<T> {

    private final ArangoIterator<T> iterator;
    private final Predicate<? super T> predicate;
    private T next;

    protected ArangoFilterIterator(final ArangoIterator<T> iterator, final Predicate<? super T> predicate) {
        super();
        this.iterator = iterator;
        this.predicate = predicate;
        next = null;
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        while (iterator.hasNext()) {
            next = iterator.next();
            if (predicate.test(next)) {
                return true;
            }
        }
        next = null;
        return false;
    }

    @Override
    public T next() {
        if (next == null && !hasNext()) {
            throw new NoSuchElementException();
        }
        final T tmp = next;
        next = null;
        return tmp;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

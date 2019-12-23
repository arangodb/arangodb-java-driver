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

import com.arangodb.ArangoIterable;
import com.arangodb.ArangoIterator;
import com.arangodb.Consumer;
import com.arangodb.Predicate;

/**
 * @author Mark Vollmary
 */
public class ArangoFilterIterable<T> extends AbstractArangoIterable<T> implements ArangoIterable<T> {

    private final ArangoIterable<T> iterable;
    private final Predicate<? super T> predicate;

    protected ArangoFilterIterable(final ArangoIterable<T> iterable, final Predicate<? super T> predicate) {
        super();
        this.iterable = iterable;
        this.predicate = predicate;
    }

    @Override
    public ArangoIterator<T> iterator() {
        return new ArangoFilterIterator<>(iterable.iterator(), predicate);
    }

    @Override
    public void foreach(final Consumer<? super T> action) {
        for (final T t : iterable) {
            if (predicate.test(t)) {
                action.accept(t);
            }
        }
    }

}

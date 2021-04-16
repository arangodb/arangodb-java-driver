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

package com.arangodb;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * @author Mark Vollmary
 */
public interface ArangoIterable<T> extends Iterable<T> {

    @Override
    ArangoIterator<T> iterator();

    Stream<T> stream();

    /**
     * Performs the given action for each element of the {@code ArangoIterable}
     *
     * @param action
     *         a action to perform on the elements
     * @deprecated Use {@link #forEach(java.util.function.Consumer)} instead.
     */
    @Deprecated
    void foreach(Consumer<? super T> action);

    /**
     * Returns a {@code ArangoIterable} consisting of the results of applying the given function to the elements of this
     * {@code ArangoIterable}.
     *
     * @param mapper
     *         a function to apply to each element
     * @return the new {@code ArangoIterable}
     *
     * @deprecated Use {@link #stream()} and {@link Stream#map(java.util.function.Function)} instead.
     */
    @Deprecated
    <R> ArangoIterable<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Returns a {@code ArangoIterable} consisting of the elements of this {@code ArangoIterable} that match the given
     * predicate.
     *
     * @param predicate
     *         a predicate to apply to each element to determine if it should be included
     * @return the new {@code ArangoIterable}
     *
     * @deprecated Use {@link #stream()} and {@link Stream#filter(java.util.function.Predicate)} instead.
     */
    @Deprecated
    ArangoIterable<T> filter(Predicate<? super T> predicate);

    /**
     * Returns the first element or {@code null} if no element exists.
     *
     * @return first element or {@code null}
     * @deprecated Use {@link #stream()} and {@link Stream#findFirst()} instead.
     */
    @Deprecated
    T first();

    /**
     * Returns the count of elements of this {@code ArangoIterable}.
     *
     * @return the count of elements
     * @deprecated Use {@link #stream()} and {@link Stream#count()} instead.
     */
    @Deprecated
    long count();

    /**
     * Returns whether any elements of this {@code ArangoIterable} match the provided predicate.
     *
     * @param predicate a predicate to apply to elements of this {@code ArangoIterable}
     * @return {@code true} if any elements of the {@code ArangoIterable} match the provided predicate, otherwise
     * {@code false}
     * @deprecated Use {@link #stream()} and {@link Stream#anyMatch(java.util.function.Predicate)} instead.
     */
    @Deprecated
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * Returns whether all elements of this {@code ArangoIterable} match the provided predicate.
     *
     * @param predicate a predicate to apply to elements of this {@code ArangoIterable}
     * @return {@code true} if all elements of the {@code ArangoIterable} match the provided predicate, otherwise
     * {@code false}
     * @deprecated Use {@link #stream()} and {@link Stream#allMatch(java.util.function.Predicate)} instead.
     */
    @Deprecated
    boolean allMatch(Predicate<? super T> predicate);

    /**
     * Returns whether no elements of this {@code ArangoIterable} match the provided predicate.
     *
     * @param predicate a predicate to apply to elements of this {@code ArangoIterable}
     * @return {@code true} if no elements of the {@code ArangoIterable} match the provided predicate, otherwise
     * {@code false}
     * @deprecated Use {@link #stream()} and {@link Stream#noneMatch(java.util.function.Predicate)} instead.
     */
    @Deprecated
    boolean noneMatch(Predicate<? super T> predicate);

    /**
     * Iterates over all elements of this {@code ArangoIterable} and adds each to the given target.
     *
     * @param target the collection to insert into
     * @return the filled target
     * @deprecated Use {@link #stream()} and {@link Stream#collect} instead.
     */
    @Deprecated
    <R extends Collection<? super T>> R collectInto(R target);

}

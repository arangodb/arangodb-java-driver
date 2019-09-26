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
import com.arangodb.Function;
import com.arangodb.Predicate;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Mark Vollmary
 *
 */
public abstract class AbstractArangoIterable<T> implements ArangoIterable<T> {

	@Override
	public <R> ArangoIterable<R> map(final Function<? super T, ? extends R> mapper) {
		return new ArangoMappingIterable<>(this, mapper);
	}

	@Override
	public ArangoIterable<T> filter(final Predicate<? super T> predicate) {
		return new ArangoFilterIterable<>(this, predicate);
	}

	@Override
	public T first() {
		final ArangoIterator<T> iterator = iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

	@Override
	public long count() {
		long count = 0L;
		for (final Iterator<T> iterator = iterator(); iterator.hasNext(); iterator.next()) {
			count++;
		}
		return count;
	}

	@Override
	public boolean anyMatch(final Predicate<? super T> predicate) {
		boolean match = false;
		for (final T t : this) {
			if (predicate.test(t)) {
				match = true;
				break;
			}
		}
		return match;
	}

	@Override
	public boolean allMatch(final Predicate<? super T> predicate) {
		boolean match = false;
		for (final T t : this) {
			match = predicate.test(t);
			if (!match) {
				break;
			}
		}
		return match;
	}

	@Override
	public boolean noneMatch(final Predicate<? super T> predicate) {
		boolean match = false;
		for (final T t : this) {
			match = !predicate.test(t);
			if (!match) {
				break;
			}
		}
		return match;
	}

	@Override
	public <R extends Collection<? super T>> R collectInto(final R target) {
		for (final T t : this) {
			target.add(t);
		}
		return target;
	}

}

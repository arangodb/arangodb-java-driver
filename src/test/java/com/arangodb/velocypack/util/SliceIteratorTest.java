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

package com.arangodb.velocypack.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.arangodb.velocypack.ArrayIterator;
import com.arangodb.velocypack.ObjectIterator;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class SliceIteratorTest {

	@Test
	public void objectIterator() {
		// {"a":1, "b":16}
		final VPackSlice slice = new VPackSlice(
				new byte[] { 0x14, 0x0a, 0x41, 0x61, 0x31, 0x41, 0x62, 0x28, 0x10, 0x02 });
		{
			final ObjectIterator iterator = new ObjectIterator(slice);
			for (final String s : new String[] { "a", "b" }) {
				final Entry<String, VPackSlice> next = iterator.next();
				assertThat(next.getKey(), is(s));
			}
		}
		{
			final ObjectIterator iterator = new ObjectIterator(slice);
			for (final int i : new int[] { 1, 16 }) {
				final Entry<String, VPackSlice> next = iterator.next();
				assertThat(next.getValue().getAsInt(), is(i));
			}
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void objectIteratorNoNext() {
		// {"a":1, "b":16}
		final VPackSlice slice = new VPackSlice(
				new byte[] { 0x14, 0x0a, 0x41, 0x61, 0x31, 0x41, 0x62, 0x28, 0x10, 0x02 });
		final ObjectIterator iterator = new ObjectIterator(slice);

		for (final String s : new String[] { "a", "b" }) {
			final Entry<String, VPackSlice> next = iterator.next();
			assertThat(next.getKey(), is(s));
		}
		iterator.next();// no more elements
	}

	@Test(expected = VPackValueTypeException.class)
	public void objectIteratorWithArrayFail() {
		final VPackSlice slice = new VPackSlice(new byte[] { 0x13, 0x06, 0x31, 0x28, 0x10, 0x02 });
		new ObjectIterator(slice);
	}

	@Test
	public void emptyObjectIterator() {
		final VPackSlice slice = new VPackSlice(new byte[] { 0x0a });
		final ObjectIterator iterator = new ObjectIterator(slice);
		assertThat(iterator.hasNext(), is(false));
	}

	@Test
	public void arrayIterator() {
		// { 1, 16 }
		final VPackSlice slice = new VPackSlice(new byte[] { 0x13, 0x06, 0x31, 0x28, 0x10, 0x02 });
		final ArrayIterator iterator = new ArrayIterator(slice);

		for (final int i : new int[] { 1, 16 }) {
			final VPackSlice next = iterator.next();
			assertThat(next.getAsInt(), is(i));
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void arrayIteratorNoNext() {
		// { 1, 16 }
		final VPackSlice slice = new VPackSlice(new byte[] { 0x13, 0x06, 0x31, 0x28, 0x10, 0x02 });
		final ArrayIterator iterator = new ArrayIterator(slice);

		for (final int i : new int[] { 1, 16 }) {
			final VPackSlice next = iterator.next();
			assertThat(next.getAsInt(), is(i));
		}
		iterator.next();// no more elements
	}

	@Test(expected = VPackValueTypeException.class)
	public void arrayIteratorWithObjectFail() {
		final VPackSlice slice = new VPackSlice(
				new byte[] { 0x14, 0x0a, 0x41, 0x61, 0x31, 0x41, 0x62, 0x28, 0x10, 0x02 });
		new ArrayIterator(slice);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void removeFail() {
		final VPackSlice slice = new VPackSlice(new byte[] { 0x13, 0x06, 0x31, 0x28, 0x10, 0x02 });
		final ArrayIterator iterator = new ArrayIterator(slice);
		iterator.remove();
	}
}

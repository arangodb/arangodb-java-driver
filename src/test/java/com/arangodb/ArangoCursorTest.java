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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoCursorTest extends BaseTest {

	public ArangoCursorTest(final Builder builder) {
		super(builder);
	}

	@Test
	public void first() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final VPackSlice first = cursor.first();
		assertThat(first, is(not(nullValue())));
		assertThat(first.isInteger(), is(true));
		assertThat(first.getAsLong(), is(0L));
	}

	@Test
	public void mapFilterCount() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).count();
		assertThat(count, is(50L));
	}

	@Test
	public void mapMapFilterCount() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(e -> e.getAsLong()).map(e -> e * 10).filter(e -> e < 500).count();
		assertThat(count, is(50L));
	}

	@Test
	public void mapMapFilterFilterCount() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(e -> e.getAsLong()).map(e -> e * 10).filter(e -> e < 500).filter(e -> e < 250)
				.count();
		assertThat(count, is(25L));
	}

	@Test
	public void mapFilterNext() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).iterator().next();
		assertThat(count, is(0L));
	}

	@Test
	public void mapFilterFirst() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).first();
		assertThat(count, is(0L));
	}

	@Test
	public void mapFilterCollectIntoList() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final List<Long> target = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).collectInto(new ArrayList<>());
		assertThat(target, is(not(nullValue())));
		assertThat(target.size(), is(50));
	}

	@Test
	public void mapFilterCollectIntoSet() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final Set<Long> target = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).collectInto(new HashSet<>());
		assertThat(target, is(not(nullValue())));
		assertThat(target.size(), is(50));
	}

	@Test
	public void foreach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.foreach(e -> {
			assertThat(e.getAsLong(), is(i.getAndIncrement()));
		});
	}

	@Test
	public void mapForeach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.map(e -> e.getAsLong()).foreach(e -> {
			assertThat(e, is(i.getAndIncrement()));
		});
	}

	@Test
	public void mapFilterForeach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.map(e -> e.getAsLong()).filter(e -> e < 50).foreach(e -> {
			assertThat(e, is(i.getAndIncrement()));
		});
	}

	@Test
	public void anyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.anyMatch(e -> e.getAsLong() == 50L);
		assertThat(match, is(true));
	}

	@Test
	public void mapAnyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).anyMatch(e -> e == 50L);
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterAnyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).anyMatch(e -> e == 25L);
		assertThat(match, is(true));
	}

	@Test
	public void noneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.noneMatch(e -> e.getAsLong() == 100L);
		assertThat(match, is(true));
	}

	@Test
	public void mapNoneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).noneMatch(e -> e == 100L);
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterNoneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).noneMatch(e -> e == 50L);
		assertThat(match, is(true));
	}

	@Test
	public void allMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.allMatch(e -> e.getAsLong() < 100L);
		assertThat(match, is(true));
	}

	@Test
	public void mapAllMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).allMatch(e -> e < 100L);
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterAllMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(e -> e.getAsLong()).filter(e -> e < 50).allMatch(e -> e < 50L);
		assertThat(match, is(true));
	}
}

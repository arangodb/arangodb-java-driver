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
		final long count = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).count();
		assertThat(count, is(50L));
	}

	@Test
	public void mapMapFilterCount() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).map(new Function<Long, Long>() {
			@Override
			public Long apply(final Long t) {
				return t * 10;
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 500;
			}
		}).count();
		assertThat(count, is(50L));
	}

	@Test
	public void mapMapFilterFilterCount() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).map(new Function<Long, Long>() {
			@Override
			public Long apply(final Long t) {
				return t * 10;
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 500;
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 250;
			}
		}).count();
		assertThat(count, is(25L));
	}

	@Test
	public void mapFilterNext() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).iterator().next();
		assertThat(count, is(0L));
	}

	@Test
	public void mapFilterFirst() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final long count = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).first();
		assertThat(count, is(0L));
	}

	@Test
	public void mapFilterCollectIntoList() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final List<Long> target = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).collectInto(new ArrayList<Long>());
		assertThat(target, is(not(nullValue())));
		assertThat(target.size(), is(50));
	}

	@Test
	public void mapFilterCollectIntoSet() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final Set<Long> target = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).collectInto(new HashSet<Long>());
		assertThat(target, is(not(nullValue())));
		assertThat(target.size(), is(50));
	}

	@Test
	public void foreach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.foreach(new Consumer<VPackSlice>() {
			@Override
			public void accept(final VPackSlice t) {
				assertThat(t.getAsLong(), is(i.getAndIncrement()));
			}
		});
	}

	@Test
	public void mapForeach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).foreach(new Consumer<Long>() {
			@Override
			public void accept(final Long t) {
				assertThat(t, is(i.getAndIncrement()));
			}
		});
	}

	@Test
	public void mapFilterForeach() {
		final AtomicLong i = new AtomicLong(0L);
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).foreach(new Consumer<Long>() {
			@Override
			public void accept(final Long t) {
				assertThat(t, is(i.getAndIncrement()));
			}
		});
	}

	@Test
	public void anyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.anyMatch(new Predicate<VPackSlice>() {
			@Override
			public boolean test(final VPackSlice t) {
				return t.getAsLong() == 50L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapAnyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).anyMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t == 50L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterAnyMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).anyMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t == 25L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void noneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.noneMatch(new Predicate<VPackSlice>() {
			@Override
			public boolean test(final VPackSlice t) {
				return t.getAsLong() == 100L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapNoneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).noneMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t == 100L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterNoneMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).noneMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t == 50L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void allMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.allMatch(new Predicate<VPackSlice>() {
			@Override
			public boolean test(final VPackSlice t) {
				return t.getAsLong() < 100L;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapAllMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).allMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 100;
			}
		});
		assertThat(match, is(true));
	}

	@Test
	public void mapFilterAllMatch() {
		final ArangoCursor<VPackSlice> cursor = db.query("FOR i IN 0..99 RETURN i", VPackSlice.class);
		final boolean match = cursor.map(new Function<VPackSlice, Long>() {
			@Override
			public Long apply(final VPackSlice t) {
				return t.getAsLong();
			}
		}).filter(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		}).allMatch(new Predicate<Long>() {
			@Override
			public boolean test(final Long t) {
				return t < 50;
			}
		});
		assertThat(match, is(true));
	}
}

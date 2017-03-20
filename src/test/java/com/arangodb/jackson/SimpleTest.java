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

package com.arangodb.jackson;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class SimpleTest {

	private static final String TEST_STRING = "hello world";
	private static final int TEST_INT = 69;

	public static class TestEntity {
		private String value1;
		private int value2;

		public TestEntity(final String value1, final int value2) {
			super();
			this.value1 = value1;
			this.value2 = value2;
		}

		public TestEntity() {
			super();
		}

		public String getValue1() {
			return value1;
		}

		public void setValue1(final String value1) {
			this.value1 = value1;
		}

		public int getValue2() {
			return value2;
		}

		public void setValue2(final int value2) {
			this.value2 = value2;
		}

	}

	@Test
	public void arangoVPackSerialization() throws IOException {
		final VelocyJack arango = new VelocyJack();

		final VPackSlice slice = arango.serialize(new TestEntity(TEST_STRING, TEST_INT));
		assertThat(slice, is(notNullValue()));
		assertThat(slice.isObject(), is(true));
		assertThat(slice.size(), is(2));
		assertThat(slice.get("value1").isString(), is(true));
		assertThat(slice.get("value1").getAsString(), is(TEST_STRING));
		assertThat(slice.get("value2").isInteger(), is(true));
		assertThat(slice.get("value2").getAsInt(), is(TEST_INT));

		final TestEntity entity = arango.deserialize(slice, TestEntity.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.getValue1(), is(TEST_STRING));
		assertThat(entity.getValue2(), is(TEST_INT));
	}
}

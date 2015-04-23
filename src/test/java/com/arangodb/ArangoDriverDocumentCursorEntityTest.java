/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseCursorEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author mrbatista
 * @author a-brandt
 * 
 */
public class ArangoDriverDocumentCursorEntityTest extends BaseTest {

	public ArangoDriverDocumentCursorEntityTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private static final String COLLECTION_NAME = "unit_test_query_test";

	@Before
	public void before() throws ArangoException {

		// create test collection
		try {
			driver.createCollection(COLLECTION_NAME);
		} catch (ArangoException e) {
		}
		driver.truncateCollection(COLLECTION_NAME);

		// create some test data
		for (int i = 0; i < 100; i++) {
			TestComplexEntity01 value = new TestComplexEntity01("user_" + (i % 10), "desc" + (i % 10), i);
			driver.createDocument(COLLECTION_NAME, value, null, null);
		}

	}

	@Test
	public void test_validateQuery() throws ArangoException {
		BaseCursorEntity<?, ?> entity = driver
				.validateDocumentQuery("FOR t IN unit_test_cursor FILTER t.name == @name && t.age >= @age RETURN t");

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getBindVars().size(), is(2));
		assertFalse(entity.getBindVars().indexOf("name") == -1);
		assertFalse(entity.getBindVars().indexOf("age") == -1);
	}

	@Test
	public void test_validateQuery_400_1() throws ArangoException {

		// syntax error, unexpected assignment near '= @name@'

		try {
			driver.validateDocumentQuery("FOR t IN unit_test_cursor FILTER t.name = @name@");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1501));
		}

	}

	@Test
	public void test_executeQuery() throws ArangoException {
		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// 全件とれる範囲
		{
			BaseCursorEntity<?, ?> result = driver.executeCursorDocumentEntityQuery(query, bindVars,
				DocumentEntity.class, TestComplexEntity01.class, true, 20, false);
			assertThat(result.size(), is(10));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(false));
		}

	}

	@Test
	public void test_executeQuery_2() throws ArangoException {
		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// ちまちまとる範囲
		long cursorId;
		{
			BaseCursorEntity<?, ?> result = driver.executeCursorDocumentEntityQuery(query, bindVars,
				DocumentEntity.class, TestComplexEntity01.class, true, 3, false);
			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
			assertThat(result.getCursorId(), is(not(-1L)));
			assertThat(result.getCursorId(), is(not(0L)));

			cursorId = result.getCursorId();
		}

		// 次のRoundTrip
		{
			BaseCursorEntity<?, ?> result = driver.continueBaseCursorEntityQuery(cursorId, DocumentEntity.class,
				TestComplexEntity01.class);

			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
		}

		// 次のRoundTrip
		{
			BaseCursorEntity<?, ?> result = driver.continueBaseCursorEntityQuery(cursorId, DocumentEntity.class,
				TestComplexEntity01.class);
			assertThat(result.size(), is(3));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(true));
		}

		// 次のRoundTrip
		{
			BaseCursorEntity<?, ?> result = driver.continueBaseCursorEntityQuery(cursorId, DocumentEntity.class,
				TestComplexEntity01.class);
			assertThat(result.size(), is(1));
			assertThat(result.getCount(), is(10));
			assertThat(result.hasMore(), is(false));
		}

		// 削除
		{
			driver.finishCursorDocumentEntityQuery(cursorId);
		}

	}

	@Test
	public void test_executeQueryFullCount() throws ArangoException {
		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age LIMIT 2 RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 10).get();

		// 全件とれる範囲
		{
			BaseCursorEntity<?, ?> result = driver.executeCursorDocumentEntityQuery(query, bindVars,
				DocumentEntity.class, TestComplexEntity01.class, true, 1, true);
			assertThat(result.size(), is(1));
			assertThat(result.getCount(), is(2));
			assertThat(result.getFullCount(), is(90));
			assertThat(result.hasMore(), is(true));
		}

	}

	@Test
	public void test_executeQueryUniqueResult() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age LIMIT 2 RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 10).get();

		// 全件とれる範囲
		{
			BaseCursorEntity<?, ?> result = driver.executeCursorDocumentEntityQuery(query, bindVars,
				DocumentEntity.class, TestComplexEntity01.class, true, 0, false);
			assertThat(result.size(), is(2));
			assertThat(result.getCount(), is(2));
			String msg = "";
			try {
				result.getUniqueResult();
			} catch (NonUniqueResultException e) {
				msg = e.getMessage();
			}
			assertThat(msg, startsWith("Query did not return a unique result:"));
		}

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@";
		query = "FOR t IN unit_test_query_test FILTER t.age == @age LIMIT 2 RETURN t";
		{
			BaseCursorEntity<TestComplexEntity01, DocumentEntity<TestComplexEntity01>> result = driver
					.executeCursorDocumentEntityQuery(query, bindVars, DocumentEntity.class, TestComplexEntity01.class,
						true, 0, false);
			assertThat(result.size(), is(1));
			assertThat(result.getCount(), is(1));
			DocumentEntity<TestComplexEntity01> uniqueResult = result.getUniqueResult();
			assertThat(uniqueResult.getEntity().getAge(), is(10));
		}
	}
}

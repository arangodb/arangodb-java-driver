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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverCursorResultSetTest extends BaseTest {

	@Before
	public void setup() throws ArangoException {

		// Collectionを作る
		final String collectionName = "unit_test_query_test";
		try {
			driver.createCollection(collectionName);
		} catch (final ArangoException e) {
		}
		driver.truncateCollection(collectionName);

		// テストデータを作る
		for (int i = 0; i < 100; i++) {
			final TestComplexEntity01 value = new TestComplexEntity01("user_" + (i % 10), "desc" + (i % 10), i);
			driver.createDocument(collectionName, value, null);
		}

	}

	@Test
	public void test1() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// 全件とれる範囲
		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(20);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		for (final TestComplexEntity01 obj : rs.asEntityList()) {
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test2() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(10);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		for (final TestComplexEntity01 obj : rs.asEntityList()) {
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test3() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(5);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		for (final TestComplexEntity01 obj : rs.asEntityList()) {
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test4() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(3);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		for (final TestComplexEntity01 obj : rs.asEntityList()) {
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test5() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(1);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		for (final TestComplexEntity01 obj : rs.asEntityList()) {
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	/**
	 * Iterableを使わないバージョン。
	 * 
	 * @throws ArangoException
	 */
	@Test
	public void test6() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(2);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		final Iterator<DocumentEntity<TestComplexEntity01>> iterator = rs.iterator();
		while (iterator.hasNext()) {
			final TestComplexEntity01 obj = iterator.next().getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		rs.close();
		assertThat(count, is(10));

	}

	/**
	 * Iterableを使わないバージョン。 途中で終了。
	 * 
	 * @throws ArangoException
	 */
	@Test
	public void test7() throws ArangoException {

		// String query = "SELECT t FROM unit_test_query_test t WHERE t.age >=
		// @age@ order by t.age";
		final String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		final Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions();
		aqlQueryOptions.setBatchSize(2);
		aqlQueryOptions.setCount(true);
		final DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
			TestComplexEntity01.class);

		int count = 0;
		final Iterator<DocumentEntity<TestComplexEntity01>> iterator = rs.iterator();
		while (iterator.hasNext()) {
			final TestComplexEntity01 obj = iterator.next().getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
			if (count == 5) {
				rs.close();
				break;
			}
		}
		assertThat(count, is(5));

	}

}

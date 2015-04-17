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
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class ArangoDriverDocumentCursorTest extends BaseTest {

	public ArangoDriverDocumentCursorTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private static final String COLLECTION_NAME = "unit_test_query_test";

	@Before
	public void setup() throws ArangoException {

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
	public void test1_WithIterator() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// 全件とれる範囲
		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 20);

		int count = 0;
		Iterator<DocumentEntity<TestComplexEntity01>> iterator = rs.iterator();

		while (iterator.hasNext()) {
			DocumentEntity<TestComplexEntity01> next = iterator.next();
			TestComplexEntity01 obj = next.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));
	}

	@Test
	public void test1_WithList() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// 全件とれる範囲
		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 20);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test2() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 10);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test3() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 5);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test4_withIterator() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 3);

		int count = 0;
		Iterator<DocumentEntity<TestComplexEntity01>> iterator = rs.iterator();

		while (iterator.hasNext()) {
			DocumentEntity<TestComplexEntity01> next = iterator.next();
			TestComplexEntity01 obj = next.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test4_withList() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 3);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test5() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 1);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
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
	public void test6_TotalNumber() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// get only two results but calculate the total number of results
		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 2);

		// test total number of results
		assertThat(rs.getCount(), is(10));

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
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
	public void test7_closeCursor() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 2);

		int count = 0;
		Iterator<DocumentEntity<TestComplexEntity01>> iterator = rs.iterator();

		while (iterator.hasNext()) {
			DocumentEntity<TestComplexEntity01> next = iterator.next();
			TestComplexEntity01 obj = next.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
			if (count == 5) {
				rs.close();
				break;
			}
		}
		assertThat(count, is(5));

	}

	@Test
	public void test7_EntityCursor() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> documentCursor = driver.executeDocumentQuery(query, bindVars,
			TestComplexEntity01.class, true, 1);

		Iterator<TestComplexEntity01> entityIterator = documentCursor.entityIterator();

		int count = 0;
		while (entityIterator.hasNext()) {
			TestComplexEntity01 obj = entityIterator.next();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

}

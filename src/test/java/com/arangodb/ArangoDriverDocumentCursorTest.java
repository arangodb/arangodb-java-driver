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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 *
 */
public class ArangoDriverDocumentCursorTest extends BaseTest {

	private static Logger logger = LoggerFactory.getLogger(ArangoDriverDocumentCursorTest.class);

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

	private AqlQueryOptions getAqlQueryOptions(Boolean count, Integer batchSize, Boolean fullCount) {
		return new AqlQueryOptions().setCount(count).setBatchSize(batchSize).setFullCount(fullCount);
	}

	@Test
	public void test1_WithIterator() throws ArangoException {

		// String query =
		// "SELECT t FROM unit_test_query_test t WHERE t.age >= @age@ order by
		// t.age";
		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// 全件とれる範囲
		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 20, null), TestComplexEntity01.class);

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

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 20, null), TestComplexEntity01.class);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test2_BatchSize10() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 10, null), TestComplexEntity01.class);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test3_BatchSize5() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 5, null), TestComplexEntity01.class);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test_withCache() throws ArangoException {
		if (isMinimumVersion(VERSION_2_7)) {
			QueryCachePropertiesEntity properties = new QueryCachePropertiesEntity();
			properties.setMode("on");
			driver.setQueryCacheProperties(properties);

			String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
			Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

			AqlQueryOptions aqlQueryOptions = getAqlQueryOptions(true, 5, null);
			aqlQueryOptions.setCache(true);

			DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions,
				TestComplexEntity01.class);

			rs = driver.executeDocumentQuery(query, bindVars, aqlQueryOptions, TestComplexEntity01.class);

			assertThat(rs.isCached(), is(true));
		}
	}

	@Test
	public void test4_withIterator() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 3, null), TestComplexEntity01.class);

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

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 3, null), TestComplexEntity01.class);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test5_BatchSize1_asList() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 1, null), TestComplexEntity01.class);

		int count = 0;
		for (DocumentEntity<TestComplexEntity01> documentEntity : rs.asList()) {
			TestComplexEntity01 obj = documentEntity.getEntity();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test6_getCount() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		// get only two results but calculate the total number of results
		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 2, null), TestComplexEntity01.class);

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

	@Test
	public void test7_closeCursor() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> rs = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 2, null), TestComplexEntity01.class);

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

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		DocumentCursor<TestComplexEntity01> documentCursor = driver.executeDocumentQuery(query, bindVars,
			getAqlQueryOptions(true, 1, null), TestComplexEntity01.class);

		Iterator<TestComplexEntity01> entityIterator = documentCursor.entityIterator();

		int count = 0;
		while (entityIterator.hasNext()) {
			TestComplexEntity01 obj = entityIterator.next();
			assertThat(obj.getAge(), is(90 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test8_CursorResult_as_Map() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN t";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		@SuppressWarnings("rawtypes")
		CursorResult<Map> cursor = driver.executeAqlQuery(query, bindVars, getAqlQueryOptions(true, 1, false),
			Map.class);

		@SuppressWarnings("rawtypes")
		Iterator<Map> iterator = cursor.iterator();

		int count = 0;
		while (iterator.hasNext()) {
			Map<?, ?> obj = iterator.next();
			Double i = (Double) obj.get("age");
			assertThat(i, is(90.0 + count));
			count++;
		}
		assertThat(count, is(10));

	}

	@Test
	public void test8_CursorResult_as_List() throws ArangoException {

		String query = "FOR t IN unit_test_query_test FILTER t.age >= @age SORT t.age RETURN [t._key, t.user]";
		Map<String, Object> bindVars = new MapBuilder().put("age", 90).get();

		@SuppressWarnings("rawtypes")
		CursorResult<List> cursor = driver.executeAqlQuery(query, bindVars, getAqlQueryOptions(true, 1, false),
			List.class);

		@SuppressWarnings("rawtypes")
		Iterator<List> iterator = cursor.iterator();

		int count = 0;
		while (iterator.hasNext()) {
			List<?> list = iterator.next();
			String user = "user_" + count;
			String get1 = list.get(1).toString();
			assertThat(get1, is(user));

			for (Object obj : list) {
				logger.debug("value " + obj);
			}
			count++;
		}
		assertThat(count, is(10));

	}

}

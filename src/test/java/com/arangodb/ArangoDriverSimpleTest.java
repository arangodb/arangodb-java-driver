/*
 * Copyright (C) 2012,2013 tamtam180
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ResultSetUtils;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 */
public class ArangoDriverSimpleTest extends BaseTest {

	// TODO: 404 test each example.

	private static Logger logger = LoggerFactory.getLogger(ArangoDriverSimpleTest.class);

	private static final String COLLECTION_NAME = "unit_test_simple_test";
	private static final String COLLECTION_NAME_400 = "unit_test_simple_test_400";

	@Before
	public void setup() throws ArangoException {

		// create test collection
		try {
			driver.deleteCollection(COLLECTION_NAME);
		} catch (final ArangoException e) {
		}
		try {
			driver.createCollection(COLLECTION_NAME);
		} catch (final ArangoException e) {
		}

		// add some test data
		for (int i = 0; i < 100; i++) {
			final TestComplexEntity01 value = new TestComplexEntity01("user_" + (i % 10), "desc" + (i % 10), i);
			driver.createDocument(COLLECTION_NAME, value, null);
		}

		// delete second test collection
		try {
			driver.deleteCollection(COLLECTION_NAME_400);
		} catch (final ArangoException e) {
		}

	}

	@Test
	public void test_simple_all() throws ArangoException {

		final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleAllDocuments(COLLECTION_NAME, 0,
			0, TestComplexEntity01.class);

		final Iterator<DocumentEntity<TestComplexEntity01>> iterator = documentCursor.iterator();

		int count = 0;
		while (iterator.hasNext()) {
			final TestComplexEntity01 entity = iterator.next().getEntity();
			count++;
			assertThat(entity, is(notNullValue()));
		}

		assertThat(count, is(100));
	}

	@Test
	public void test_example_by() throws ArangoException {

		final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleByExampleDocuments(
			COLLECTION_NAME, new MapBuilder().put("user", "user_6").get(), 0, 0, TestComplexEntity01.class);

		final Iterator<DocumentEntity<TestComplexEntity01>> iterator = documentCursor.iterator();

		int count = 0;
		while (iterator.hasNext()) {
			final TestComplexEntity01 entity = iterator.next().getEntity();
			count++;
			assertThat(entity.getUser(), is("user_6"));
		}
		assertThat(count, is(10));
	}

	@Test
	public void test_first_example() throws ArangoException {

		final ScalarExampleEntity<TestComplexEntity01> entity = driver.executeSimpleFirstExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_5").put("desc", "desc5").get(), TestComplexEntity01.class);

		final DocumentEntity<TestComplexEntity01> doc = entity.getDocument();

		assertThat(entity.getStatusCode(), is(200));
		assertThat(doc.getDocumentRevision(), is(not(0L)));
		assertThat(doc.getDocumentHandle(), is(COLLECTION_NAME + "/" + doc.getDocumentKey()));
		assertThat(doc.getDocumentKey(), is(notNullValue()));
		assertThat(doc.getEntity(), is(notNullValue()));
		assertThat(doc.getEntity().getUser(), is("user_5"));
		assertThat(doc.getEntity().getDesc(), is("desc5"));

	}

	@Test
	public void test_any() throws ArangoException {

		final ScalarExampleEntity<TestComplexEntity01> entity = driver.executeSimpleAny(COLLECTION_NAME,
			TestComplexEntity01.class);

		for (int i = 0; i < 30; i++) {
			final DocumentEntity<TestComplexEntity01> doc = entity.getDocument();

			assertThat(entity.getStatusCode(), is(200));
			assertThat(doc.getDocumentRevision(), is(not(0L)));
			assertThat(doc.getDocumentHandle(), is(COLLECTION_NAME + "/" + doc.getDocumentKey()));
			assertThat(doc.getDocumentKey(), is(notNullValue()));
			assertThat(doc.getEntity(), is(notNullValue()));
			assertThat(doc.getEntity().getUser(), is(notNullValue()));
			assertThat(doc.getEntity().getDesc(), is(notNullValue()));
			assertThat(doc.getEntity().getAge(), is(notNullValue()));
		}
	}

	@Test
	public void test_range_no_skiplist() throws ArangoException {

		// no suitable index known
		try {
			driver.executeSimpleRangeWithDocuments(COLLECTION_NAME, "age", 5, 30, null, 0, 0,
				TestComplexEntity01.class);
			fail("request should fail");
		} catch (final ArangoException e) {
			assertThat(e.getErrorNumber(), is(1209));
			assertThat(e.getCode(), is(404));
		}

	}

	@Test
	public void test_range() throws ArangoException {

		// create skip-list
		driver.createIndex(COLLECTION_NAME, IndexType.SKIPLIST, false, "age");

		{
			final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleRangeWithDocuments(
				COLLECTION_NAME, "age", 5, 30, null, 0, 0, TestComplexEntity01.class);
			final Iterator<DocumentEntity<TestComplexEntity01>> iterator = documentCursor.iterator();

			int count = 0;
			while (iterator.hasNext()) {
				final TestComplexEntity01 entity = iterator.next().getEntity();
				count++;
				assertThat(entity, is(notNullValue()));
			}

			assertThat(count, is(25));
		}

		{
			final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleRangeWithDocuments(
				COLLECTION_NAME, "age", 5, 30, true, 0, 0, TestComplexEntity01.class);
			final Iterator<DocumentEntity<TestComplexEntity01>> iterator = documentCursor.iterator();

			int count = 0;
			while (iterator.hasNext()) {
				final TestComplexEntity01 entity = iterator.next().getEntity();
				count++;
				assertThat(entity, is(notNullValue()));
			}

			assertThat(count, is(26));
		}

	}

	@Test
	public void test_remove_by_example() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleRemoveByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), null, null);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(10));
		assertThat(entity.getDeleted(), is(10));
		assertThat(entity.getReplaced(), is(0));
		assertThat(entity.getUpdated(), is(0));

	}

	@Test
	public void test_remove_by_example_with_limit() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleRemoveByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), null, 5);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(5));
		assertThat(entity.getDeleted(), is(5));
		assertThat(entity.getReplaced(), is(0));
		assertThat(entity.getUpdated(), is(0));

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void test_replace_by_example() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleReplaceByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").get(), null, null);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(10));
		assertThat(entity.getDeleted(), is(0));
		assertThat(entity.getReplaced(), is(10));
		assertThat(entity.getUpdated(), is(0));

		// Get Replaced Document
		final DocumentCursor<Map> documentCursor = driver.executeSimpleByExampleDocuments(COLLECTION_NAME,
			new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);

		final List<DocumentEntity<Map>> list = documentCursor.asList();

		assertThat(list.size(), is(10));
		for (final DocumentEntity<Map> docuemntEntity : list) {
			final Map<?, ?> map = docuemntEntity.getEntity();
			assertThat(map.size(), is(4)); // _id, _rev, _key and "abc"
			assertThat((String) map.get("abc"), is("xxx"));
		}

	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Test
	public void test_replace_by_example_with_limit() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleReplaceByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").get(), null, 3);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(3));
		assertThat(entity.getDeleted(), is(0));
		assertThat(entity.getReplaced(), is(3));
		assertThat(entity.getUpdated(), is(0));

		// Get Replaced Document
		final CursorResultSet<Map> rs = driver.executeSimpleByExampleWithResusltSet(COLLECTION_NAME,
			new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
		final List<Map> list = ResultSetUtils.toList(rs);

		assertThat(list.size(), is(3));
		for (final Map<String, ?> map : list) {
			assertThat(map.size(), is(4)); // _id, _rev, _key and "abc"
			assertThat((String) map.get("abc"), is("xxx"));
		}

	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Test
	public void test_update_by_example() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").put("age", 999).get(),
			null, null, null);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(10));
		assertThat(entity.getDeleted(), is(0));
		assertThat(entity.getReplaced(), is(0));
		assertThat(entity.getUpdated(), is(10));

		// Get Replaced Document
		final CursorResultSet<Map> rs = driver.executeSimpleByExampleWithResusltSet(COLLECTION_NAME,
			new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
		final List<Map> list = ResultSetUtils.toList(rs);

		assertThat(list.size(), is(10));
		for (final Map<String, ?> map : list) {
			assertThat(map.size(), is(7)); // _id, _rev, _key and "user",
			// "desc", "age", "abc"
			assertThat((String) map.get("user"), is("user_3"));
			assertThat((String) map.get("desc"), is("desc3"));
			assertThat(((Number) map.get("age")).intValue(), is(999));
			assertThat((String) map.get("abc"), is("xxx"));
		}

	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Test
	public void test_update_by_example_with_limit() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").put("age", 999).get(),
			null, null, 3);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(3));
		assertThat(entity.getDeleted(), is(0));
		assertThat(entity.getReplaced(), is(0));
		assertThat(entity.getUpdated(), is(3));

		// Get Replaced Document
		final CursorResultSet<Map> rs = driver.executeSimpleByExampleWithResusltSet(COLLECTION_NAME,
			new MapBuilder().put("age", 999).get(), 0, 0, Map.class);
		final List<Map> list = ResultSetUtils.toList(rs);

		assertThat(list.size(), is(3));
		for (final Map<String, ?> map : list) {
			assertThat(map.size(), is(7)); // _id, _rev, _key and "user",
			// "desc", "age", "abc"
			assertThat((String) map.get("user"), is("user_3"));
			assertThat((String) map.get("desc"), is("desc3"));
			assertThat(((Number) map.get("age")).intValue(), is(999));
			assertThat((String) map.get("abc"), is("xxx"));
		}

	}

	@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
	@Test
	public void test_update_by_example_with_keepnull() throws ArangoException {

		final SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(COLLECTION_NAME,
			new MapBuilder().put("user", "user_3").get(),
			new MapBuilder(false).put("abc", "xxx").put("age", 999).put("user", null).get(), false, null, null);

		assertThat(entity.getCode(), is(200));
		assertThat(entity.getCount(), is(10));
		assertThat(entity.getDeleted(), is(0));
		assertThat(entity.getReplaced(), is(0));
		assertThat(entity.getUpdated(), is(10));

		// Get Replaced Document
		final CursorResultSet<Map> rs = driver.executeSimpleByExampleWithResusltSet(COLLECTION_NAME,
			new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
		final List<Map> list = ResultSetUtils.toList(rs);

		assertThat(list.size(), is(10));
		for (final Map<String, ?> map : list) {
			assertThat(map.size(), is(6)); // _id, _rev, _key and "desc", "age",
			// "abc"
			assertThat(map.get("user"), is(nullValue()));
			assertThat((String) map.get("desc"), is("desc3"));
			assertThat(((Number) map.get("age")).intValue(), is(999));
			assertThat((String) map.get("abc"), is("xxx"));
		}

	}

	// TODO fulltext Japanese Text

	@Test
	public void test_fulltext() throws ArangoException {

		// MEMO: INDEX作成前のドキュメントはヒットしない・・。仕様？

		// create fulltext index
		driver.createFulltextIndex(COLLECTION_NAME, 2, "desc");

		driver.createDocument(COLLECTION_NAME, new TestComplexEntity01("xxx1", "this text contains a word", 10), null);
		driver.createDocument(COLLECTION_NAME, new TestComplexEntity01("xxx2", "this text also contains a word", 10),
			null);

		final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleFulltextWithDocuments(
			COLLECTION_NAME, "desc", "word", 0, 0, null, TestComplexEntity01.class);
		final List<DocumentEntity<TestComplexEntity01>> list = documentCursor.asList();

		assertThat(list.size(), is(2));
		assertThat(list.get(0).getEntity().getUser(), is("xxx1"));
		assertThat(list.get(0).getEntity().getDesc(), is("this text contains a word"));
		assertThat(list.get(0).getEntity().getAge(), is(10));

		assertThat(list.get(1).getEntity().getUser(), is("xxx2"));
		assertThat(list.get(1).getEntity().getDesc(), is("this text also contains a word"));
		assertThat(list.get(1).getEntity().getAge(), is(10));

	}

	@Test
	public void test_fulltext_with_doc() throws ArangoException {

		// MEMO: INDEX作成前のドキュメントはヒットしない・・。仕様？

		// create fulltext index
		driver.createFulltextIndex(COLLECTION_NAME, 2, "desc");

		driver.createDocument(COLLECTION_NAME, new TestComplexEntity01("xxx1", "this text contains a word", 10), null);
		driver.createDocument(COLLECTION_NAME, new TestComplexEntity01("xxx2", "this text also contains a word", 10),
			null);

		final DocumentCursor<TestComplexEntity01> documentCursor = driver.executeSimpleFulltextWithDocuments(
			COLLECTION_NAME, "desc", "word", 0, 0, null, TestComplexEntity01.class);
		final List<DocumentEntity<TestComplexEntity01>> list = documentCursor.asList();

		assertThat(list.size(), is(2));
		assertThat(list.get(0).getDocumentHandle(), startsWith(COLLECTION_NAME));
		assertThat(list.get(0).getDocumentKey(), is(notNullValue()));
		assertThat(list.get(0).getDocumentRevision(), is(not(0L)));
		assertThat(list.get(0).getEntity().getUser(), is("xxx1"));
		assertThat(list.get(0).getEntity().getDesc(), is("this text contains a word"));
		assertThat(list.get(0).getEntity().getAge(), is(10));

		assertThat(list.get(1).getDocumentHandle(), startsWith(COLLECTION_NAME));
		assertThat(list.get(1).getDocumentKey(), is(notNullValue()));
		assertThat(list.get(1).getDocumentRevision(), is(not(0L)));
		assertThat(list.get(1).getEntity().getUser(), is("xxx2"));
		assertThat(list.get(1).getEntity().getDesc(), is("this text also contains a word"));
		assertThat(list.get(1).getEntity().getAge(), is(10));

	}

	@Test
	public void test_geo() throws ArangoException, IOException {

		// Load Station data
		final List<Station> stations = TestUtils.readStations();
		logger.debug(stations.toString());

	}

}

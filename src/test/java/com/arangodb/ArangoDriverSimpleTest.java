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
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResultSet;
import com.arangodb.ErrorNums;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.DocumentResultEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ResultSetUtils;
import com.arangodb.util.TestUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverSimpleTest extends BaseTest {

  // TODO: 404 test each example.

  public ArangoDriverSimpleTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  private String collectionName = "unit_test_simple_test";
  private String collectionName400 = "unit_test_simple_test_400";

  @Before
  public void setup() throws ArangoException {

    // index破棄のために一度削除する
    try {
      driver.deleteCollection(collectionName);
    } catch (ArangoException e) {
    }
    // Collectionを作る
    try {
      driver.createCollection(collectionName);
    } catch (ArangoException e) {
    }
    driver.truncateCollection(collectionName);

    // テストデータを作る
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user_" + (i % 10), "desc" + (i % 10), i);
      driver.createDocument(collectionName, value, null, null);
    }

    // 存在しないコレクション
    try {
      driver.deleteCollection(collectionName400);
    } catch (ArangoException e) {
    }

  }

  @Test
  public void test_simple_all() throws ArangoException {

    CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleAllWithResultSet(collectionName, 0, 0,
        TestComplexEntity01.class);
    int count = 0;
    while (rs.hasNext()) {
      TestComplexEntity01 entity = rs.next();
      count++;

      assertThat(entity, is(notNullValue()));
    }
    rs.close();

    assertThat(count, is(100));

  }

  @Test
  public void test_simple_all_with_doc() throws ArangoException {

    CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.executeSimpleAllWithDocumentResultSet(
        collectionName, 0, 0, TestComplexEntity01.class);
    int count = 0;
    int ageCount = 0;
    while (rs.hasNext()) {
      DocumentEntity<TestComplexEntity01> doc = rs.next();
      count++;

      assertThat(doc, is(notNullValue()));
      assertThat(doc.getDocumentHandle(), startsWith(collectionName));
      assertThat(doc.getDocumentKey(), is(notNullValue()));
      assertThat(doc.getDocumentRevision(), is(not(0L)));

      if (doc.getEntity().getAge() != 0) {
        ageCount++;
      }
    }
    rs.close();

    assertThat(count, is(100));
    assertThat(ageCount, is(99));

  }

  @Test
  public void test_example_by() throws ArangoException {

    CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("user", "user_6").get(), 0, 0, TestComplexEntity01.class);
    int count = 0;
    while (rs.hasNext()) {
      TestComplexEntity01 entity = rs.next();
      count++;

      assertThat(entity.getUser(), is("user_6"));
    }
    rs.close();

    assertThat(count, is(10));

  }

  @Test
  public void test_example_by_with_doc() throws ArangoException {

    CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.executeSimpleByExampleWithDocumentResusltSet(
        collectionName, new MapBuilder().put("user", "user_6").get(), 0, 0, TestComplexEntity01.class);
    int count = 0;
    while (rs.hasNext()) {
      DocumentEntity<TestComplexEntity01> doc = rs.next();
      count++;

      assertThat(doc.getDocumentHandle(), startsWith(collectionName));
      assertThat(doc.getDocumentKey(), is(notNullValue()));
      assertThat(doc.getDocumentRevision(), is(not(0L)));

      assertThat(doc.getEntity().getUser(), is("user_6"));
    }
    rs.close();

    assertThat(count, is(10));

  }

  @Test
  public void test_first_example() throws ArangoException {

    ScalarExampleEntity<TestComplexEntity01> entity = driver.executeSimpleFirstExample(collectionName,
        new MapBuilder().put("user", "user_5").put("desc", "desc5").get(), TestComplexEntity01.class);

    DocumentEntity<TestComplexEntity01> doc = entity.getDocument();

    assertThat(entity.getStatusCode(), is(200));
    assertThat(doc.getDocumentRevision(), is(not(0L)));
    assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
    assertThat(doc.getDocumentKey(), is(notNullValue()));
    assertThat(doc.getEntity(), is(notNullValue()));
    assertThat(doc.getEntity().getUser(), is("user_5"));
    assertThat(doc.getEntity().getDesc(), is("desc5"));

  }

  @Test
  public void test_any() throws ArangoException {

    ScalarExampleEntity<TestComplexEntity01> entity = driver.executeSimpleAny(collectionName,
        TestComplexEntity01.class);

    for (int i = 0; i < 30; i++) {
      DocumentEntity<TestComplexEntity01> doc = entity.getDocument();

      assertThat(entity.getStatusCode(), is(200));
      assertThat(doc.getDocumentRevision(), is(not(0L)));
      assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
      assertThat(doc.getDocumentKey(), is(notNullValue()));
      assertThat(doc.getEntity(), is(notNullValue()));
      assertThat(doc.getEntity().getUser(), is(notNullValue()));
      assertThat(doc.getEntity().getDesc(), is(notNullValue()));
      assertThat(doc.getEntity().getAge(), is(notNullValue()));
    }
  }

  @Test
  public void test_range_no_skiplist() throws ArangoException {

    // skip listが無いのでエラーになる
    try {
      CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleRangeWithResultSet(collectionName, "age", 5,
          30, null, 0, 0, TestComplexEntity01.class);
      fail("例外が発生しないとだめ");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(500));
      assertThat(e.getCode(), is(500));
    }

  }

  @Test
  public void test_range() throws ArangoException {

    // create skip-list
    driver.createIndex(collectionName, IndexType.SKIPLIST, false, "age");

    {
      CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleRangeWithResultSet(collectionName, "age", 5,
          30, null, 0, 0, TestComplexEntity01.class);

      int count = 0;
      while (rs.hasNext()) {
        TestComplexEntity01 entity = rs.next();
        count++;
        assertThat(entity, is(notNullValue()));
      }
      rs.close();
      assertThat(count, is(25));
    }

    {
      CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleRangeWithResultSet(collectionName, "age", 5,
          30, true, 0, 0, TestComplexEntity01.class);

      int count = 0;
      while (rs.hasNext()) {
        TestComplexEntity01 entity = rs.next();
        count++;
        assertThat(entity, is(notNullValue()));
      }
      rs.close();
      assertThat(count, is(26));
    }

  }

  @Test
  public void test_range_with_doc() throws ArangoException {

    // create skip-list
    driver.createIndex(collectionName, IndexType.SKIPLIST, false, "age");

    {
      CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.executeSimpleRangeWithDocumentResultSet(
          collectionName, "age", 5, 30, null, 0, 0, TestComplexEntity01.class);

      int count = 0;
      while (rs.hasNext()) {
        DocumentEntity<TestComplexEntity01> doc = rs.next();
        count++;
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getDocumentHandle(), startsWith(collectionName));
        assertThat(doc.getDocumentKey(), is(notNullValue()));
        assertThat(doc.getDocumentRevision(), is(not(0L)));
        assertThat(doc.getEntity(), is(notNullValue()));
        assertThat(doc.getEntity().getAge(), is(not(0)));
      }
      rs.close();
      assertThat(count, is(25));
    }

    {
      CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.executeSimpleRangeWithDocumentResultSet(
          collectionName, "age", 5, 30, true, 0, 0, TestComplexEntity01.class);

      int count = 0;
      while (rs.hasNext()) {
        DocumentEntity<TestComplexEntity01> doc = rs.next();
        count++;
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getDocumentHandle(), startsWith(collectionName));
        assertThat(doc.getDocumentKey(), is(notNullValue()));
        assertThat(doc.getDocumentRevision(), is(not(0L)));
        assertThat(doc.getEntity(), is(notNullValue()));
        assertThat(doc.getEntity().getAge(), is(not(0)));
      }
      rs.close();
      assertThat(count, is(26));
    }

  }

  @Test
  public void test_remove_by_example() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleRemoveByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), null, null);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(10));
    assertThat(entity.getDeleted(), is(10));
    assertThat(entity.getReplaced(), is(0));
    assertThat(entity.getUpdated(), is(0));

  }

  @Test
  public void test_remove_by_example_with_limit() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleRemoveByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), null, 5);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(5));
    assertThat(entity.getDeleted(), is(5));
    assertThat(entity.getReplaced(), is(0));
    assertThat(entity.getUpdated(), is(0));

  }

  @Test
  public void test_replace_by_example() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleReplaceByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").get(), null, null);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(10));
    assertThat(entity.getDeleted(), is(0));
    assertThat(entity.getReplaced(), is(10));
    assertThat(entity.getUpdated(), is(0));

    // Get Replaced Document
    CursorResultSet<Map<String, Object>> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
    List<Map<String, Object>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(10));
    for (Map<String, ?> map : list) {
      assertThat(map.size(), is(4)); // _id, _rev, _key and "abc"
      assertThat((String) map.get("abc"), is("xxx"));
    }

  }

  @Test
  public void test_replace_by_example_with_limit() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleReplaceByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").get(), null, 3);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(3));
    assertThat(entity.getDeleted(), is(0));
    assertThat(entity.getReplaced(), is(3));
    assertThat(entity.getUpdated(), is(0));

    // Get Replaced Document
    CursorResultSet<Map<String, Object>> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
    List<Map<String, Object>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(3));
    for (Map<String, ?> map : list) {
      assertThat(map.size(), is(4)); // _id, _rev, _key and "abc"
      assertThat((String) map.get("abc"), is("xxx"));
    }

  }

  @Test
  public void test_update_by_example() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").put("age", 999).get(),
        null, null, null);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(10));
    assertThat(entity.getDeleted(), is(0));
    assertThat(entity.getReplaced(), is(0));
    assertThat(entity.getUpdated(), is(10));

    // Get Replaced Document
    CursorResultSet<Map<String, Object>> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
    List<Map<String, Object>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(10));
    for (Map<String, ?> map : list) {
      assertThat(map.size(), is(7)); // _id, _rev, _key and "user",
                      // "desc", "age", "abc"
      assertThat((String) map.get("user"), is("user_3"));
      assertThat((String) map.get("desc"), is("desc3"));
      assertThat(((Number) map.get("age")).intValue(), is(999));
      assertThat((String) map.get("abc"), is("xxx"));
    }

  }

  @Test
  public void test_update_by_example_with_limit() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), new MapBuilder().put("abc", "xxx").put("age", 999).get(),
        null, null, 3);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(3));
    assertThat(entity.getDeleted(), is(0));
    assertThat(entity.getReplaced(), is(0));
    assertThat(entity.getUpdated(), is(3));

    // Get Replaced Document
    CursorResultSet<Map<String, Object>> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("age", 999).get(), 0, 0, Map.class);
    List<Map<String, Object>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(3));
    for (Map<String, ?> map : list) {
      assertThat(map.size(), is(7)); // _id, _rev, _key and "user",
                      // "desc", "age", "abc"
      assertThat((String) map.get("user"), is("user_3"));
      assertThat((String) map.get("desc"), is("desc3"));
      assertThat(((Number) map.get("age")).intValue(), is(999));
      assertThat((String) map.get("abc"), is("xxx"));
    }

  }

  @Test
  public void test_update_by_example_with_keepnull() throws ArangoException {

    SimpleByResultEntity entity = driver.executeSimpleUpdateByExample(collectionName,
        new MapBuilder().put("user", "user_3").get(), new MapBuilder(false).put("abc", "xxx").put("age", 999)
            .put("user", null).get(), false, null, null);

    assertThat(entity.getCode(), is(200));
    assertThat(entity.getCount(), is(10));
    assertThat(entity.getDeleted(), is(0));
    assertThat(entity.getReplaced(), is(0));
    assertThat(entity.getUpdated(), is(10));

    // Get Replaced Document
    CursorResultSet<Map<String, Object>> rs = driver.executeSimpleByExampleWithResusltSet(collectionName,
        new MapBuilder().put("abc", "xxx").get(), 0, 0, Map.class);
    List<Map<String, Object>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(10));
    for (Map<String, ?> map : list) {
      assertThat(map.size(), is(6)); // _id, _rev, _key and "desc", "age",
                      // "abc"
      assertThat((String) map.get("user"), is(nullValue()));
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
    driver.createFulltextIndex(collectionName, 2, "desc");

    driver.createDocument(collectionName, new TestComplexEntity01("xxx1", "this text contains a word", 10), null,
        null);
    driver.createDocument(collectionName, new TestComplexEntity01("xxx2", "this text also contains a word", 10),
        null, null);

    CursorResultSet<TestComplexEntity01> rs = driver.executeSimpleFulltextWithResultSet(collectionName, "desc",
        "word", 0, 0, null, TestComplexEntity01.class);
    List<TestComplexEntity01> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(2));
    assertThat(list.get(0).getUser(), is("xxx1"));
    assertThat(list.get(0).getDesc(), is("this text contains a word"));
    assertThat(list.get(0).getAge(), is(10));

    assertThat(list.get(1).getUser(), is("xxx2"));
    assertThat(list.get(1).getDesc(), is("this text also contains a word"));
    assertThat(list.get(1).getAge(), is(10));

  }

  @Test
  public void test_fulltext_with_doc() throws ArangoException {

    // MEMO: INDEX作成前のドキュメントはヒットしない・・。仕様？

    // create fulltext index
    driver.createFulltextIndex(collectionName, 2, "desc");

    driver.createDocument(collectionName, new TestComplexEntity01("xxx1", "this text contains a word", 10), null,
        null);
    driver.createDocument(collectionName, new TestComplexEntity01("xxx2", "this text also contains a word", 10),
        null, null);

    CursorResultSet<DocumentEntity<TestComplexEntity01>> rs = driver.executeSimpleFulltextWithDocumentResultSet(
        collectionName, "desc", "word", 0, 0, null, TestComplexEntity01.class);
    List<DocumentEntity<TestComplexEntity01>> list = ResultSetUtils.toList(rs);

    assertThat(list.size(), is(2));
    assertThat(list.get(0).getDocumentHandle(), startsWith(collectionName));
    assertThat(list.get(0).getDocumentKey(), is(notNullValue()));
    assertThat(list.get(0).getDocumentRevision(), is(not(0L)));
    assertThat(list.get(0).getEntity().getUser(), is("xxx1"));
    assertThat(list.get(0).getEntity().getDesc(), is("this text contains a word"));
    assertThat(list.get(0).getEntity().getAge(), is(10));

    assertThat(list.get(1).getDocumentHandle(), startsWith(collectionName));
    assertThat(list.get(1).getDocumentKey(), is(notNullValue()));
    assertThat(list.get(1).getDocumentRevision(), is(not(0L)));
    assertThat(list.get(1).getEntity().getUser(), is("xxx2"));
    assertThat(list.get(1).getEntity().getDesc(), is("this text also contains a word"));
    assertThat(list.get(1).getEntity().getAge(), is(10));

  }

  @Test
  public void test_geo() throws ArangoException, IOException {

    // Load Station data
    List<Station> stations = TestUtils.readStations();
    System.out.println(stations);

  }

  @Test
  public void test_first() throws ArangoException {

    // server returns object-type
    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleFirst(collectionName, null,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(1));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(0));
    assertThat(obj.getEntity().getUser(), is("user_0"));
    assertThat(obj.getEntity().getDesc(), is("desc0"));

  }

  @Test
  public void test_first_count1() throws ArangoException {

    // count = null と count = 1はサーバが返してくるresultの戻りの型が違う
    // server returns array-type
    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleFirst(collectionName, 1,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(1));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(0));
    assertThat(obj.getEntity().getUser(), is("user_0"));
    assertThat(obj.getEntity().getDesc(), is("desc0"));

  }

  @Test
  public void test_first_count5() throws ArangoException {

    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleFirst(collectionName, 5,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(5));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(0));
    assertThat(obj.getEntity().getUser(), is("user_0"));
    assertThat(obj.getEntity().getDesc(), is("desc0"));

    DocumentEntity<TestComplexEntity01> obj4 = entity.getResult().get(4);
    assertThat(obj4.getDocumentHandle(), is(notNullValue()));
    assertThat(obj4.getDocumentRevision(), is(not(0L)));
    assertThat(obj4.getDocumentKey(), is(notNullValue()));

    assertThat(obj4.getEntity().getAge(), is(4));
    assertThat(obj4.getEntity().getUser(), is("user_4"));
    assertThat(obj4.getEntity().getDesc(), is("desc4"));

  }

  @Test
  public void test_first_400() throws ArangoException {
    try {
      driver.executeSimpleFirst(collectionName400, 1, TestComplexEntity01.class);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(400));
      assertThat(e.getErrorNumber(), is(ErrorNums.ERROR_TYPE_ERROR));
    }

  }

  @Test
  public void test_last() throws ArangoException {

    // server returns object-type
    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleLast(collectionName, null,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(1));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(99));
    assertThat(obj.getEntity().getUser(), is("user_9"));
    assertThat(obj.getEntity().getDesc(), is("desc9"));

  }

  @Test
  public void test_last_count1() throws ArangoException {

    // count = null と count = 1はサーバが返してくるresultの戻りの型が違う
    // server returns array-type
    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleLast(collectionName, 1,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(1));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(99));
    assertThat(obj.getEntity().getUser(), is("user_9"));
    assertThat(obj.getEntity().getDesc(), is("desc9"));

  }

  @Test
  public void test_last_count5() throws ArangoException {

    DocumentResultEntity<TestComplexEntity01> entity = driver.executeSimpleLast(collectionName, 5,
        TestComplexEntity01.class);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getResult().size(), is(5));

    DocumentEntity<TestComplexEntity01> obj = entity.getOne();
    assertThat(obj.getDocumentHandle(), is(notNullValue()));
    assertThat(obj.getDocumentRevision(), is(not(0L)));
    assertThat(obj.getDocumentKey(), is(notNullValue()));

    assertThat(obj.getEntity().getAge(), is(99));
    assertThat(obj.getEntity().getUser(), is("user_9"));
    assertThat(obj.getEntity().getDesc(), is("desc9"));

    DocumentEntity<TestComplexEntity01> obj4 = entity.getResult().get(4);
    assertThat(obj4.getDocumentHandle(), is(notNullValue()));
    assertThat(obj4.getDocumentRevision(), is(not(0L)));
    assertThat(obj4.getDocumentKey(), is(notNullValue()));

    assertThat(obj4.getEntity().getAge(), is(95));
    assertThat(obj4.getEntity().getUser(), is("user_5"));
    assertThat(obj4.getEntity().getDesc(), is("desc5"));

  }

  @Test
  public void test_last_400() throws ArangoException {
    try {
      driver.executeSimpleLast(collectionName400, 1, TestComplexEntity01.class);
      fail();
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(400));
      assertThat(e.getErrorNumber(), is(ErrorNums.ERROR_TYPE_ERROR));
    }

  }

}

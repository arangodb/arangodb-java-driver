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

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionKeyOption;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionStatus;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.CollectionsEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.util.MapBuilder;

/**
 * UnitTest for REST API "collections"
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 * 
 */
public class ArangoDriverCollectionTest extends BaseTest {

  public ArangoDriverCollectionTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  private static Logger logger = LoggerFactory.getLogger(ArangoDriverCollectionTest.class);

  final String collectionName = "unit_test_arango_001"; // 通常ケースで使うコレクション名
  final String collectionName2 = "unit_test_arango_002";
  final String collectionName404 = "unit_test_arango_404"; // 存在しないコレクション名

  @Before
  public void before() throws ArangoException {

    logger.debug("----------");

    // 事前に消しておく
    for (String col : new String[] { collectionName, collectionName2, collectionName404 }) {
      try {
        driver.deleteCollection(col);
      } catch (ArangoException e) {
      }
    }

    logger.debug("--");

  }

  @After
  public void after() {
    logger.debug("----------");
  }

  /**
   * system test
   * 
   * @throws ArangoException
   */
  @Test
  public void test_create_document_collection() throws ArangoException {

    // DocumentCollection
    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1, is(notNullValue()));
    assertThat(res1.getCode(), is(200));
    assertThat(res1.getId(), is(notNullValue()));
    assertThat(res1.getName(), is(collectionName));
    assertThat(res1.getWaitForSync(), is(false));
    assertThat(res1.getIsVolatile(), is(false));
    assertThat(res1.getIsSystem(), is(false));
    assertThat(res1.getStatus(), is(CollectionStatus.LOADED));
    assertThat(res1.getType(), is(CollectionType.DOCUMENT));

  }

  /**
   * system test
   * 
   * @throws ArangoException
   */
  @Test
  // @Parameters
      public
      void test_create_edge_collection() throws ArangoException {
    CollectionOptions collectionOptions = new CollectionOptions();
    collectionOptions.setType(CollectionType.EDGE);
    CollectionEntity res2 = driver.createCollection(collectionName, collectionOptions);
    assertThat(res2, is(notNullValue()));
    assertThat(res2.getCode(), is(200));
    assertThat(res2.getId(), is(notNullValue()));
    assertThat(res2.getName(), is(collectionName));
    assertThat(res2.getWaitForSync(), is(false));
    assertThat(res2.getIsVolatile(), is(false));
    assertThat(res2.getIsSystem(), is(false));
    assertThat(res2.getStatus(), is(CollectionStatus.LOADED));
    assertThat(res2.getType(), is(CollectionType.EDGE));

  }

  /**
   * Test for InMemory Document Collection.
   * 
   * @throws ArangoException
   */
  @Test
  public void test_create_inmemory_document_collection() throws ArangoException {

    CollectionOptions collectionOptions = new CollectionOptions();
    collectionOptions.setIsVolatile(true);
    CollectionEntity res = driver.createCollection(collectionName, collectionOptions);
    assertThat(res, is(notNullValue()));
    assertThat(res.getCode(), is(200));
    assertThat(res.getId(), is(not(0L)));
    assertThat(res.getName(), is(collectionName));
    assertThat(res.getWaitForSync(), is(false));
    assertThat(res.getIsVolatile(), is(true));
    assertThat(res.getIsSystem(), is(false));
    assertThat(res.getStatus(), is(CollectionStatus.LOADED));
    assertThat(res.getType(), is(CollectionType.DOCUMENT));

  }

  @Test
  public void test_create_with_options() throws ArangoException {

    CollectionKeyOption keyOptions = new CollectionKeyOption();
    keyOptions.setType("autoincrement");
    keyOptions.setAllowUserKeys(true);
    keyOptions.setIncrement(10);
    keyOptions.setOffset(200);

    CollectionOptions collectionOptions = new CollectionOptions();
    collectionOptions.setKeyOptions(keyOptions);
    CollectionEntity res = driver.createCollection(collectionName, collectionOptions);
    assertThat(res, is(notNullValue()));
    assertThat(res.getCode(), is(200));
    assertThat(res.getId(), is(not(0L)));
    assertThat(res.getName(), is(collectionName));
    assertThat(res.getWaitForSync(), is(false));
    assertThat(res.getIsVolatile(), is(false));
    assertThat(res.getIsSystem(), is(false));
    assertThat(res.getStatus(), is(CollectionStatus.LOADED));
    assertThat(res.getType(), is(CollectionType.DOCUMENT));

    // 現状では戻り値でとれないので別のAPIで確認する
    // // TODO 現状では戻ってこないことを確認する
    assertThat(res.getKeyOptions(), is(nullValue()));

    // 別のAPIで確認する
    CollectionEntity ent = driver.getCollectionProperties(collectionName);
    CollectionKeyOption opt = ent.getKeyOptions();
    assertThat(opt.isAllowUserKeys(), is(true));
    assertThat(opt.getType(), is("autoincrement"));
    assertThat(opt.getIncrement(), is(10L));
    assertThat(opt.getOffset(), is(200L));

  }

  @Test
  public void test_create_no_compact() throws ArangoException {

    // DocumentCollection
    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1, is(notNullValue()));
    assertThat(res1.getCode(), is(200));
    assertThat(res1.getId(), is(notNullValue()));
    assertThat(res1.getName(), is(collectionName));
    assertThat(res1.getWaitForSync(), is(false));
    assertThat(res1.getIsVolatile(), is(false));
    assertThat(res1.getIsSystem(), is(false));
    assertThat(res1.getStatus(), is(CollectionStatus.LOADED));
    assertThat(res1.getType(), is(CollectionType.DOCUMENT));

    CollectionEntity prop = driver.getCollectionProperties(collectionName);
    assertThat(prop.getCode(), is(200));
    assertThat(prop.getId(), is(res1.getId()));
    assertThat(prop.getDoCompact(), is(true));

  }

  /**
   * 既に存在する場合の挙動確認。
   * 
   * @throws ArangoException
   */
  @Test
  public void test_create_dup() throws ArangoException {

    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1, is(notNullValue()));
    assertThat(res1.getCode(), is(200));
    assertThat(res1.getId(), is(not(0L)));
    assertThat(res1.getName(), is(collectionName));
    assertThat(res1.getWaitForSync(), is(false));
    assertThat(res1.getIsVolatile(), is(false));
    assertThat(res1.getIsSystem(), is(false));
    assertThat(res1.getStatus(), is(CollectionStatus.LOADED));

    {
      try {
        CollectionEntity res = driver.createCollection(collectionName);
        fail("ここに来てはダメー！");
      } catch (ArangoException e) {
        assertThat(e.getCode(), is(409));
        assertThat(e.getErrorNumber(), is(1207));
      }
    }

  }

  @Test
  public void test_getCollection_01() throws ArangoException {

    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    long collectionId = res1.getId();

    // IDで取得
    CollectionEntity entity1 = driver.getCollection(collectionId);
    // 名前で取得
    CollectionEntity entity2 = driver.getCollection(collectionName);
    assertThat(entity1.getId(), is(collectionId));
    assertThat(entity2.getId(), is(collectionId));
    assertThat(entity1.getName(), is(collectionName));
    assertThat(entity2.getName(), is(collectionName));

    // Type確認
    assertThat(entity1.getType(), is(CollectionType.DOCUMENT));
    assertThat(entity2.getType(), is(CollectionType.DOCUMENT));

  }

  /**
   * 存在しないコレクションを取得する場合
   * 
   * @throws ArangoException
   */
  @Test
  public void test_getCollection_404() throws ArangoException {

    try {
      driver.getCollection(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getCollectionProperties_01() throws ArangoException {

    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    CollectionEntity collection = driver.getCollectionProperties(collectionName);
    assertThat(collection.getCode(), is(200));
    assertThat(collection.getId(), is(res1.getId()));
    assertThat(collection.getName(), is(collectionName));
    assertThat(collection.getWaitForSync(), is(Boolean.FALSE));
    assertThat(collection.getJournalSize(), is(32L * 1024 * 1024)); // 32MB
    assertThat(collection.getIsSystem(), is(false));
    assertThat(collection.getIsVolatile(), is(false));

    assertThat(collection.getStatus(), is(CollectionStatus.LOADED)); // 3
    assertThat(collection.getType(), is(CollectionType.DOCUMENT)); // 2
    assertThat(collection.getDoCompact(), is(true));

    assertThat(collection.getKeyOptions().getType(), is("traditional"));
    assertThat(collection.getKeyOptions().isAllowUserKeys(), is(true));

    // Countがないこと
    // Revisionがないこと
    assertThat(collection.getCount(), is(0L));
    assertThat(collection.getRevision(), is(0L));

  }

  /**
   * 存在しないコレクションを指定した場合。
   * 
   * @throws ArangoException
   */
  @Test
  public void test_getCollectionProperties_404() throws ArangoException {

    try {
      driver.getCollectionProperties(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getCollectionRevision() throws ArangoException {

    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    // Get Revision
    CollectionEntity collection = driver.getCollectionRevision(collectionName);
    assertThat(collection.getRevision(), is(0L));

    // Create Document
    driver.createDocument(collectionName, new TestComplexEntity01("test_user1", "test user 1", 20), false, true);

    // Get Revision again
    collection = driver.getCollectionRevision(collectionName);
    // Check to updated revision
    long rev2 = collection.getRevision();
    assertThat(rev2, is(not(0L)));

    // Create Document
    driver.createDocument(collectionName, new TestComplexEntity01("test_user2", "test user 2", 21), false, true);

    // Get Revision again
    collection = driver.getCollectionRevision(collectionName);
    // Check to updated revision
    long rev3 = collection.getRevision();
    assertThat(rev3, greaterThan(rev2));

  }

  @Test
  public void test_getCollectionRevision_404() throws ArangoException {

    try {
      driver.getCollectionRevision(collectionName404);
      fail("Because did not raise Exception.");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getCollectionCount_01() throws ArangoException {

    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    CollectionEntity collection = driver.getCollectionCount(collectionName);
    assertThat(collection.getCode(), is(200));
    assertThat(collection.getId(), is(res1.getId()));
    assertThat(collection.getName(), is(collectionName));
    assertThat(collection.getWaitForSync(), is(Boolean.FALSE));
    assertThat(collection.getJournalSize(), is(32L * 1024 * 1024)); // 32MB
    assertThat(collection.getCount(), is(0L)); // 何も入っていないのでゼロ
    // TODO type, status

    // 100個ほどドキュメントを入れてみる
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("test_user" + i, "tes user:" + i, 20 + i);
      driver.createDocument(collectionName, value, false, true);
    }

    // もっかいアクセスして10になっているか確認する
    collection = driver.getCollectionCount(collectionName);
    assertThat(collection.getCount(), is(100L));

  }

  /**
   * 存在しないコレクションを指定した場合。
   * 
   * @throws ArangoException
   */
  @Test
  public void test_getCollectionCount_404() throws ArangoException {

    try {
      driver.getCollectionCount(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getCollectionFigures_01() throws ArangoException {

    // コレクションを作る
    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    // 100個ほどドキュメントを入れてみる
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("test_user" + i, "test user:" + i, 20 + i);
      DocumentEntity<TestComplexEntity01> entity = driver.createDocument(collectionName, value, false, true);
      // 1個消す
      if (i == 50) {
        driver.deleteDocument(entity.getDocumentHandle(), null, null);
      }
    }

    CollectionEntity collection = driver.getCollectionFigures(collectionName);
    assertThat(collection.getCode(), is(200));
    assertThat(collection.getId(), is(res1.getId()));
    assertThat(collection.getName(), is(collectionName));
    assertThat(collection.getWaitForSync(), is(Boolean.FALSE));
    assertThat(collection.getJournalSize(), is(32L * 1024 * 1024)); // 32MB
    assertThat(collection.getCount(), is(99L));
    assertThat(collection.getType(), is(CollectionType.DOCUMENT));
    assertThat(collection.getStatus(), is(CollectionStatus.LOADED));

    // It is not possible to check the numbers
    // assertThat(collection.getFigures().getAliveCount(), is(99L));
    // assertThat(collection.getFigures().getAliveSize(), is(not(0L)));
    // assertThat(collection.getFigures().getDeadCount(), is(1L));
    // assertThat(collection.getFigures().getDeadSize(), is(not(0L)));
    // assertThat(collection.getFigures().getJournalsCount(), is(1L));
    // assertThat(collection.getFigures().getJournalsFileSize(),
    // is(not(0L)));
    // assertThat(collection.getFigures().getShapefilesCount(), is(1L));
    // assertThat(collection.getFigures().getShapefilesFileSize(),
    // is(not(0L)));
    // assertThat(collection.getFigures().getShapesCount(), is(not(0L)));
    // assertThat(collection.getFigures().getAttributesCount(),
    // is(not(0L)));

    assertThat(collection.getFigures().getIndexesCount(), is(1L));
    assertThat(collection.getFigures().getIndexesSize(), is(not(0L)));
    // assertThat(collection.getFigures().getLastTick(), is(1L));
    assertThat(collection.getFigures().getUncollectedLogfileEntries(), is(not(0L)));

  }

  /**
   * 存在しないコレクションを指定した場合。
   * 
   * @throws ArangoException
   */
  @Test
  public void test_getCollectionFigures_404() throws ArangoException {

    try {
      driver.getCollectionFigures(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getCollectionChecksum() throws ArangoException {

    // create collection.
    CollectionEntity res1 = driver.createCollection(collectionName);
    assertThat(res1.getCode(), is(200));

    // checksum
    CollectionEntity entity = driver.getCollectionChecksum(collectionName, true, true);
    assertThat(entity.getCode(), is(200));
    assertThat(entity.isError(), is(false));
    assertThat(entity.getRevision(), is(0L)); // レスポンスは戻ってきてるんだけど、0なのでどうチェックしたものか・・
    assertThat(entity.getChecksum(), is(0L)); // 同上

  }

  @Test
  public void test_getCollections() throws ArangoException {

    for (int i = 0; i < 10; i++) {
      try {
        driver.createCollection("unit_test_arango_" + (1000 + i));
      } catch (ArangoException e) {
      }
    }

    CollectionsEntity collections = driver.getCollections();
    assertThat(collections.getCode(), is(200));
    Map<String, CollectionEntity> map = collections.getNames();
    for (int i = 0; i < 10; i++) {
      String collectionName = "unit_test_arango_" + (1000 + i);
      CollectionEntity collection = map.get(collectionName);
      // id, name, status
      assertThat(collection, is(notNullValue()));
      assertThat(collection.getId(), is(not(0L)));
      assertThat(collection.getName(), is(collectionName));
    }

    // with exluceSystem parameter.
    int allColCount = collections.getCollections().size();
    int sysExcludeCount = driver.getCollections(true).getCollections().size();
    assertThat(allColCount > sysExcludeCount, is(true));

  }

  @Test
  public void test_load_unload() throws ArangoException {

    // create
    CollectionEntity collection = driver.createCollection(collectionName);
    assertThat(collection, is(notNullValue()));
    assertThat(collection.getCode(), is(200));

    // add document, for count parameter test.
    driver.createDocument(collectionName, new MapBuilder("hoge", "fuga").put("fuga", "piyoko").get(), false, null);

    CollectionEntity collection1 = driver.unloadCollection(collectionName);
    assertThat(collection1, is(notNullValue()));
    assertThat(collection1.getCode(), is(200));
    assertThat(collection1.getCount(), is(0L));

    assertThat(
      collection1.getStatus(),
      anyOf(is(CollectionStatus.UNLOADED), is(CollectionStatus.IN_THE_PROCESS_OF_BEING_UNLOADED)));

    CollectionEntity collection2 = driver.loadCollection(collectionName);
    assertThat(collection2, is(notNullValue()));
    assertThat(collection2.getCode(), is(200));
    assertThat(collection2.getStatus(), is(CollectionStatus.LOADED));
    assertThat(collection2.getCount(), is(1L));

    // unload again, for count parameter test.
    collection1 = driver.unloadCollection(collectionName);
    assertThat(collection1, is(notNullValue()));
    assertThat(collection1.getCode(), is(200));
    assertThat(collection1.getCount(), is(0L));

    // with count parameter
    CollectionEntity col3 = driver.loadCollection(collectionName, false);
    assertThat(col3, is(notNullValue()));
    assertThat(col3.getCode(), is(200));
    assertThat(col3.getStatus(), is(CollectionStatus.LOADED));
    assertThat(col3.getCount(), is(0L)); // Not contains count in response.

  }

  @Test
  public void test_load_404() throws ArangoException {

    try {
      driver.loadCollection(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_unload_404() throws ArangoException {

    try {
      driver.unloadCollection(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_truncate() throws ArangoException {

    CollectionEntity collection = driver.createCollection(collectionName);
    assertThat(collection, is(notNullValue()));
    assertThat(collection.getCode(), is(200));

    // 100個ほどドキュメントを入れてみる
    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("test_user" + i, "test user:" + i, 20 + i);
      assertThat(driver.createDocument(collectionName, value, false, true).getStatusCode(), is(201));
    }
    // 100個入ったよね？
    assertThat(driver.getCollectionCount(collectionName).getCount(), is(100L));

    // 抹殺じゃー！
    CollectionEntity collection2 = driver.truncateCollection(collectionName);
    assertThat(collection2, is(notNullValue()));
    assertThat(collection2.getCode(), is(200));

    // 0件になってるか確認
    assertThat(driver.getCollectionCount(collectionName).getCount(), is(0L));

  }

  @Test
  public void test_truncate_404() throws ArangoException {

    try {
      driver.unloadCollection(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_setCollectionProperties() throws ArangoException {

    CollectionEntity collection = driver.createCollection(collectionName);
    assertThat(collection, is(notNullValue()));
    assertThat(collection.getCode(), is(200));
    assertThat(collection.getWaitForSync(), is(Boolean.FALSE));

    // Change waitForSync: false -> true
    CollectionEntity col = driver.setCollectionProperties(collectionName, true, null);
    assertThat(col.getCode(), is(200));
    assertThat(col.getWaitForSync(), is(Boolean.TRUE));

    // Change journalSize: default -> 1234567Byte
    col = driver.setCollectionProperties(collectionName, null, 1234567L);
    assertThat(col.getCode(), is(200));
    assertThat(col.getJournalSize(), is(1234567L));

  }

  @Test
  public void test_setCollectionProperties_404() throws ArangoException {

    try {
      driver.setCollectionProperties(collectionName404, true, null);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_delete() throws ArangoException {

    // コレクションを適当に10個作る
    TreeSet<String> collectionNames = new TreeSet<String>();
    for (int i = 0; i < 10; i++) {
      try {
        CollectionEntity col = driver.createCollection("unit_test_arango_" + (1000 + i));
        long collectionId = col.getId();
        if (i == 5) {
          // 1個だけ消す
          CollectionEntity res = driver.deleteCollection(collectionId);
          assertThat(res.getCode(), is(200));
          assertThat(res.getId(), is(collectionId));
        } else {
          collectionNames.add(col.getName());
        }
      } catch (ArangoException e) {
      }
    }

    // 残りの9個は残っていること
    Map<String, CollectionEntity> collections = driver.getCollections().getNames();
    for (String name : collectionNames) {
      assertThat(collections.containsKey(name), is(true));
    }

  }

  @Test
  public void test_delete_404() throws ArangoException {

    try {
      driver.deleteCollection(collectionName404);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_rename_404() throws ArangoException {

    CollectionEntity collection = driver.createCollection(collectionName);
    assertThat(collection.getCode(), is(200));

    try {
      driver.renameCollection(collectionName404, collectionName);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(404));
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  /**
   * Rename先が既に存在する場合
   * 
   * @throws ArangoException
   */
  @Test
  public void test_rename_dup() throws ArangoException {

    CollectionEntity collection1 = driver.createCollection(collectionName);
    assertThat(collection1.getCode(), is(200));

    CollectionEntity collection2 = driver.createCollection(collectionName2);
    assertThat(collection2.getCode(), is(200));

    try {
      driver.renameCollection(collectionName, collectionName2);
      fail("ここに来てはダメー！");
    } catch (ArangoException e) {
      assertThat(e.getCode(), is(409));
      assertThat(e.getErrorNumber(), is(1207));
    }

  }
}

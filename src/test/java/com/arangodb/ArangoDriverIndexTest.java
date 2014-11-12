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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ErrorNums;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoDriverIndexTest extends BaseTest {

  public ArangoDriverIndexTest(ArangoConfigure configure, ArangoDriver driver) {
    super(configure, driver);
  }

  private static Logger logger = LoggerFactory.getLogger(ArangoDriverCollectionTest.class);

  final String collectionName = "unit_test_arango_index"; //
  final String collectionName404 = "unit_test_arango_404"; // 存在しないコレクション名

  CollectionEntity col1;

  @Before
  public void before() throws ArangoException {

    logger.debug("----------");

    // 事前に消しておく
    try {
      driver.deleteCollection(collectionName);
    } catch (ArangoException e) {
    }
    try {
      driver.deleteCollection(collectionName404);
    } catch (ArangoException e) {
    }

    // 1は作る
    col1 = driver.createCollection(collectionName);

    logger.debug("--");

  }

  @After
  public void after() {
    logger.debug("----------");
  }

  @Test
  public void test_create_index() throws ArangoException {

    {
      IndexEntity entity = driver.createIndex(collectionName, IndexType.GEO, false, "a");

      assertThat(entity, is(notNullValue()));
      assertThat(entity.getCode(), is(201));
      assertThat(entity.isError(), is(false));
      assertThat(entity.isNewlyCreated(), is(true));
      assertThat(entity.isGeoJson(), is(false));
      assertThat(entity.getId(), is(notNullValue()));
      assertThat(entity.getType(), is(IndexType.GEO));
    }

    // 重複して作成する
    {
      IndexEntity entity = driver.createIndex(collectionName, IndexType.GEO, false, "a");

      assertThat(entity, is(notNullValue()));
      assertThat(entity.getCode(), is(200));
      assertThat(entity.isError(), is(false));
      assertThat(entity.isNewlyCreated(), is(false));
      assertThat(entity.isGeoJson(), is(false));
      assertThat(entity.getId(), is(notNullValue()));
      assertThat(entity.getType(), is(IndexType.GEO));
    }

  }

  @Test
  public void test_create_index_404() throws ArangoException {

    try {
      driver.createIndex(collectionName404, IndexType.GEO, false, "a");
      fail("We expect an Exception here");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(1203)); // FIXME MagicNumber
    }

  }

  @Test
  public void test_create_geo_index_unique() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.GEO, true, "a", "b");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.GEO));

  }

  @Test
  public void test_create_geo_index_over_columnnum() throws ArangoException {

    // GeoIndexは2つまで。だけど3つを指定した場合のエラー確認

    try {
      IndexEntity entity = driver.createIndex(collectionName, IndexType.GEO, true, "a", "b", "c");
      fail("We expect an Exception here");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(ErrorNums.ERROR_BAD_PARAMETER));
    }

  }

  @Test
  public void test_create_hash_index() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.HASH, false, "a", "b", "c", "d", "e", "f",
        "g");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.HASH));

  }

  @Test
  public void test_create_hash_index_404() throws ArangoException {

    try {
      IndexEntity entity = driver.createIndex(collectionName404, IndexType.HASH, false, "a", "b", "c", "d", "e",
          "f", "g");
      fail("We expect an Exception here");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(ErrorNums.ERROR_ARANGO_COLLECTION_NOT_FOUND));
    }

  }

  @Test
  public void test_create_hash_index_unique() throws ArangoException {

    IndexEntity entity = driver
        .createIndex(collectionName, IndexType.HASH, true, "a", "b", "c", "d", "e", "f", "g");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.HASH));

  }

  @Test
  public void test_create_skiplist_index() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.SKIPLIST, false, "a", "b", "c", "d", "e",
        "f", "g");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.SKIPLIST));

  }

  @Test
  public void test_create_skiplist_index_unique() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.SKIPLIST, true, "a", "b", "c", "d", "e", "f",
        "g");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.SKIPLIST));

  }

  @Test
  public void test_create_hash_index_with_document() throws ArangoException {

    for (int i = 0; i < 100; i++) {
      TestComplexEntity01 value = new TestComplexEntity01("user_" + i, "", i);

      assertThat(driver.createDocument(collectionName, value, false, false), is(notNullValue()));
    }

    IndexEntity entity = driver.createIndex(collectionName, IndexType.HASH, true, "name", "age");

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.HASH));

  }

  @Test
  public void test_create_fulltext_index() throws ArangoException {

    // create test data 100 count.
    for (int i = 0; i < 100; i++) {
      String desc = i % 2 == 0 ? "寿司" : "天ぷら";
      TestComplexEntity01 value = new TestComplexEntity01("user_" + i, desc, i);
      assertThat(driver.createDocument(collectionName, value, false, false), is(notNullValue()));
    }

    // create fulltext index
    IndexEntity index = driver.createFulltextIndex(collectionName, 1, "desc");

    // {"id":"unit_test_arango_index/6420761720","unique":false,"type":"fulltext","minLength":1,"fields":["desc"],"isNewlyCreated":true,"error":false,"code":201}
    assertThat(index.getCode(), is(201));
    assertThat(index.isError(), is(false));
    assertThat(index.getId(), is(not(nullValue())));
    assertThat(index.isUnique(), is(false));
    assertThat(index.getType(), is(IndexType.FULLTEXT));
    assertThat(index.getMinLength(), is(1));
    assertThat(index.getFields(), is(Arrays.asList("desc")));
    assertThat(index.isNewlyCreated(), is(true));

  }

  @Test
  public void test_delete_index() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.HASH, true, "name", "age");
    assertThat(entity, is(notNullValue()));
    assertThat(entity.getId(), is(notNullValue()));

    String id = entity.getId();

    IndexEntity entity2 = driver.deleteIndex(id);

    assertThat(entity2, is(notNullValue()));
    assertThat(entity2.getCode(), is(200));
    assertThat(entity2.isError(), is(false));
    assertThat(entity2.getId(), is(id));

  }

  @Test
  public void test_delete_index_pk() throws ArangoException {

    // PKは削除できない
    try {
      IndexEntity entity2 = driver.deleteIndex(collectionName + "/0");
      fail("例外が飛ばないといけない");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(1212));
    }

  }

  @Test
  public void test_delete_index_404_1() throws ArangoException {

    // コレクションは存在するが、存在しないインデックスを削除しようとした

    try {
      IndexEntity entity2 = driver.deleteIndex(collectionName + "/1");
      fail("例外が飛ばないといけない");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(1212));
    }

  }

  /**
   * ユニークインデックスの列が重複した場合。 TODO: あとで
   * 
   * @throws ArangoException
   */
  @Test
  @Ignore
  public void test_create_hash_index_dup_unique() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.HASH, true, "user", "age");

    assertThat(driver.createDocument(collectionName, new TestComplexEntity01("寿司天ぷら", "", 18), false, false),
        is(notNullValue()));
    assertThat(driver.createDocument(collectionName, new TestComplexEntity01("寿司天ぷら", "", 18), false, false),
        is(notNullValue()));

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.isGeoJson(), is(false));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.HASH));

  }

  @Test
  public void test_create_cap_index() throws ArangoException {

    IndexEntity entity = driver.createCappedIndex(collectionName, 10);

    assertThat(entity, is(notNullValue()));
    assertThat(entity.getCode(), is(201));
    assertThat(entity.isError(), is(false));
    assertThat(entity.isNewlyCreated(), is(true));
    assertThat(entity.getSize(), is(10));
    assertThat(entity.getId(), is(notNullValue()));
    assertThat(entity.getType(), is(IndexType.CAP));

    // 確認 ピンポイントで取得
    IndexEntity entity2 = driver.getIndex(entity.getId());
    assertThat(entity2.getCode(), is(200));
    assertThat(entity2.isError(), is(false));
    assertThat(entity2.isNewlyCreated(), is(false));
    assertThat(entity2.getSize(), is(10));
    assertThat(entity2.getId(), is(entity.getId()));
    assertThat(entity2.getType(), is(IndexType.CAP));

    // 確認 インデックス一覧を取得
    IndexesEntity indexes = driver.getIndexes(collectionName);
    assertThat(indexes.getCode(), is(200));
    assertThat(indexes.isError(), is(false));
    assertThat(indexes.getIndexes().size(), is(2));

    String pkHandle = col1.getName() + "/0";
    IndexEntity pk = indexes.getIdentifiers().get(pkHandle);
    assertThat(pk.getType(), is(IndexType.PRIMARY));
    assertThat(pk.getFields().size(), is(1));
    assertThat(pk.getFields().get(0), is("_key"));

    IndexEntity idx1 = indexes.getIdentifiers().get(entity.getId());
    assertThat(idx1.getType(), is(IndexType.CAP));
    assertThat(idx1.getFields(), is(nullValue()));
    assertThat(idx1.getSize(), is(10));

  }

  @Test
  public void test_create_cap_index_404() throws ArangoException {

    try {
      IndexEntity entity = driver.createCappedIndex(collectionName404, 10);
      fail("例外が飛ばないといけない");
    } catch (ArangoException e) {
      assertThat(e.getErrorNumber(), is(1203));
    }

  }

  @Test
  public void test_getIndexes() throws ArangoException {

    IndexEntity entity = driver.createIndex(collectionName, IndexType.HASH, true, "name", "age");
    assertThat(entity, is(notNullValue()));

    IndexesEntity indexes = driver.getIndexes(collectionName);

    assertThat(indexes, is(notNullValue()));

    assertThat(indexes.getIndexes().size(), is(2));
    assertThat(indexes.getIndexes().get(0).getType(), is(IndexType.PRIMARY));
    assertThat(indexes.getIndexes().get(0).getFields().size(), is(1));
    assertThat(indexes.getIndexes().get(0).getFields().get(0), is("_key"));
    assertThat(indexes.getIndexes().get(1).getType(), is(IndexType.HASH));
    assertThat(indexes.getIndexes().get(1).getFields().size(), is(2));
    assertThat(indexes.getIndexes().get(1).getFields().get(0), is("name"));
    assertThat(indexes.getIndexes().get(1).getFields().get(1), is("age"));

    String id1 = indexes.getIndexes().get(0).getId();
    String id2 = indexes.getIndexes().get(1).getId();

    assertThat(indexes.getIdentifiers().size(), is(2));
    assertThat(indexes.getIdentifiers().get(id1).getType(), is(IndexType.PRIMARY));
    assertThat(indexes.getIdentifiers().get(id1).getFields().size(), is(1));
    assertThat(indexes.getIdentifiers().get(id1).getFields().get(0), is("_key"));
    assertThat(indexes.getIdentifiers().get(id2).getType(), is(IndexType.HASH));
    assertThat(indexes.getIdentifiers().get(id2).getFields().size(), is(2));
    assertThat(indexes.getIdentifiers().get(id2).getFields().get(0), is("name"));
    assertThat(indexes.getIdentifiers().get(id2).getFields().get(1), is("age"));

  }

}

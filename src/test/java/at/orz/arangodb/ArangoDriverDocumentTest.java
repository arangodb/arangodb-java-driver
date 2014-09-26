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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.DocumentEntity;
/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverDocumentTest extends BaseTest {

	public ArangoDriverDocumentTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}

	private static Logger logger = LoggerFactory.getLogger(ArangoDriverCollectionTest.class);
	
	final String collectionName = "unit_test_arango_001"; // 通常ケースで使うコレクション名
	final String collectionName2 = "unit_test_arango_002";
	final String collectionName404 = "unit_test_arango_404"; // 存在しないコレクション名
	
	CollectionEntity col1;
	CollectionEntity col2;
	
	@Before
	public void before() throws ArangoException {
		
		logger.debug("----------");
		
		// 事前に消しておく
		for (String col: new String[]{collectionName, collectionName2, collectionName404}) {
			try {
				driver.deleteCollection(col);
			} catch (ArangoException e) {}
		}

		// 1と2は作る
		col1 = driver.createCollection(collectionName, false, null, null, null, null, null);
		col2 = driver.createCollection(collectionName2, true, null, null, null, null, null);
		
		logger.debug("--");
		
	}
	
	@After
	public void after() {
		logger.debug("----------");
	}

	@Test
	public void test_create_normal() throws ArangoException {
		
		// 適当にドキュメントを作る
		TestComplexEntity01 value = new TestComplexEntity01("user-" + 9999, "説明:" + 9999, 9999);
		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, null, false);
		
		assertThat(doc.getDocumentKey(), is(notNullValue()));
		assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
		assertThat(doc.getDocumentRevision(), is(not(0L)));
		
	}
	
	@Test
	public void test_create_normal100() throws ArangoException {
		
		// 適当にドキュメントを作る
		for (int i = 0; i < 100; i++) {
			TestComplexEntity01 value = new TestComplexEntity01("user-" + i, "説明:" + i, i);
			driver.createDocument(collectionName, value, null, false);
		}
		
		// 100個格納できていることを確認する
		assertThat(driver.getCollectionCount(collectionName).getCount(), is(100L));
		
	}
	
	@Test
	public void test_create_sameobject() throws ArangoException {
		// 適当にドキュメントを作る
		for (int i = 0; i < 100; i++) {
			TestComplexEntity01 value = new TestComplexEntity01("user", "説明:", 10);
			driver.createDocument(collectionName, value, null, true);
		}
		
		// 100個格納できていることを確認する
		assertThat(driver.getCollectionCount(collectionName).getCount(), is(100L));
	}
	
	/**
	 * 存在しないコレクションに追加しようとするテスト
	 * @throws ArangoException
	 */
	@Test
	public void test_create_404() throws ArangoException {
		
		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		try {
			driver.createDocument(collectionName404, value, false, true);
			fail("例外が発生しないといけないの");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}
		
	}
	
	/**
	 * コレクションがない場合、コレクションを勝手に作ってくれることを確認。
	 * @throws ArangoException
	 */
	@Test
	public void test_create_404_insert() throws ArangoException {

		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		// 存在しないコレクションに追加しようとする
		DocumentEntity<TestComplexEntity01> res = driver.createDocument(collectionName404, value, true, true);
		assertThat(res, is(notNullValue()));
		
		// コレクションができている
		CollectionEntity col3 = driver.getCollection(collectionName404);
		assertThat(col3, is(notNullValue()));
		
		assertThat(res.getDocumentHandle().startsWith(collectionName404 + "/"), is(true));
		assertThat(res.getDocumentRevision(), is(not(0L)));
		assertThat(res.getDocumentKey(), is(notNullValue()));
		
	}
	
	@Test
	public void test_replace() throws ArangoException {
		
		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		
		// Create Document
		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, true, false);
		assertThat(doc, is(notNullValue()));
		
		// Update
		value.setUser(null);
		value.setDesc("UpdatedDescription");
		value.setAge(15);
		DocumentEntity<TestComplexEntity01> doc2 = driver.replaceDocument(doc.getDocumentHandle(), value, null, null, null);
		assertThat(doc2.getStatusCode(), is(202));
		
		// Get
		DocumentEntity<TestComplexEntity01> doc3 = driver.getDocument(doc2.getDocumentHandle(), TestComplexEntity01.class);
		assertThat(doc3.getStatusCode(), is(200));
		assertThat(doc3.getEntity(), is(notNullValue()));
		assertThat(doc3.getEntity().getUser(), is(nullValue()));
		assertThat(doc3.getEntity().getDesc(), is("UpdatedDescription"));
		assertThat(doc3.getEntity().getAge(), is(15));
		
	}
	
	@Test
	public void test_replace_404() throws ArangoException {
		
		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		// 存在しないコレクションに追加しようとする
		try {
			driver.replaceDocument(collectionName404, 1, value, null, null, null);
			fail("例外が発生しないといけないの");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
		}
		
	}

	@Test
	public void test_replace_404_2() throws ArangoException {
		
		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		// 存在するコレクションだが、ドキュメントが存在しない
		try {
			driver.replaceDocument(collectionName, 1, value, null, null, null);
			fail("例外が発生しないといけないの");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1202));
		}
		
	}

	@Test
	public void test_partial_update() throws ArangoException {
		
		TestComplexEntity01 value = new TestComplexEntity01("test-user", "テスト☆ユーザー", 22);
		
		// Create Document
		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, true, false);
		assertThat(doc, is(notNullValue()));
		
		// PartialUpdate
		value.setUser(null);
		value.setDesc("UpdatedDescription");
		value.setAge(15);
		DocumentEntity<TestComplexEntity01> doc2 = driver.updateDocument(doc.getDocumentHandle(), value, null, null, null, null);
		assertThat(doc2.getStatusCode(), is(202));
		
		// Get
		DocumentEntity<TestComplexEntity01> doc3 = driver.getDocument(doc2.getDocumentHandle(), TestComplexEntity01.class);
		assertThat(doc3.getStatusCode(), is(200));
		assertThat(doc3.getEntity(), is(notNullValue()));
		assertThat(doc3.getEntity().getUser(), is("test-user")); // not update
		assertThat(doc3.getEntity().getDesc(), is("UpdatedDescription"));
		assertThat(doc3.getEntity().getAge(), is(15));
		
		// keepNull = false (NULLを渡すと削除)
		DocumentEntity<TestComplexEntity01> doc4 = driver.updateDocument(doc.getDocumentHandle(), value, null, null, null, false);
		assertThat(doc4.getStatusCode(), is(202));
		
		// Get
		DocumentEntity<TestComplexEntity01> doc5 = driver.getDocument(doc2.getDocumentHandle(), TestComplexEntity01.class);
		assertThat(doc5.getStatusCode(), is(200));
		assertThat(doc5.getEntity(), is(notNullValue()));
		assertThat(doc5.getEntity().getUser(), is(nullValue())); // update
		assertThat(doc5.getEntity().getDesc(), is("UpdatedDescription"));
		assertThat(doc5.getEntity().getAge(), is(15));
		
	}

	@Test
	public void test_getDocuments() throws ArangoException {
		
		// create document
		DocumentEntity<TestComplexEntity01> doc1 = driver.createDocument(collectionName, new TestComplexEntity01("test-user1", "test-user1-desc", 21), true, false);
		DocumentEntity<TestComplexEntity01> doc2 = driver.createDocument(collectionName, new TestComplexEntity01("test-user2", "test-user2-desc", 22), true, false);
		DocumentEntity<TestComplexEntity01> doc3 = driver.createDocument(collectionName, new TestComplexEntity01("test-user3", "test-user3-desc", 23), true, false);
		assertThat(doc1, is(notNullValue()));
		assertThat(doc2, is(notNullValue()));
		assertThat(doc3, is(notNullValue()));

		//
		Set<String> tree = new TreeSet<String>(Arrays.asList(
				"/_api/document/" + doc1.getDocumentHandle(),
				"/_api/document/" + doc2.getDocumentHandle(),
				"/_api/document/" + doc3.getDocumentHandle()
				));
		
		// get documents
		Set<String> docIds = new TreeSet<String>(driver.getDocuments(collectionName));
		assertThat(docIds.size(), is(3));
		
		assertThat(docIds, is(tree));
		
	}

	@Test
	public void test_getDocuments_handle() throws ArangoException {

		// create document
		DocumentEntity<TestComplexEntity01> doc1 = driver.createDocument(collectionName, new TestComplexEntity01("test-user1", "test-user1-desc", 21), true, false);
		DocumentEntity<TestComplexEntity01> doc2 = driver.createDocument(collectionName, new TestComplexEntity01("test-user2", "test-user2-desc", 22), true, false);
		DocumentEntity<TestComplexEntity01> doc3 = driver.createDocument(collectionName, new TestComplexEntity01("test-user3", "test-user3-desc", 23), true, false);
		assertThat(doc1, is(notNullValue()));
		assertThat(doc2, is(notNullValue()));
		assertThat(doc3, is(notNullValue()));

		//
		Set<String> tree = new TreeSet<String>(Arrays.asList(
				doc1.getDocumentHandle(),
				doc2.getDocumentHandle(),
				doc3.getDocumentHandle()
				));
		
		// get documents
		Set<String> docIds = new TreeSet<String>(driver.getDocuments(collectionName, true));
		assertThat(docIds.size(), is(3));
		
		assertThat(docIds, is(tree));

	}

	@Test
	public void test_get_document() throws ArangoException {
		
		// create document
		TestComplexEntity01 value = new TestComplexEntity01("user-" + 9999, "説明:" + 9999, 9999);
		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, null, false);
		
		assertThat(doc.getDocumentKey(), is(notNullValue()));
		assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
		assertThat(doc.getDocumentRevision(), is(not(0L)));
		
		// Get
		DocumentEntity<TestComplexEntity01> retVal = driver.getDocument(doc.getDocumentHandle(), TestComplexEntity01.class);
		assertThat(retVal.getDocumentHandle(), is(doc.getDocumentHandle()));
		assertThat(retVal.getDocumentRevision(), is(doc.getDocumentRevision()));
		assertThat(retVal.getDocumentKey(), is(doc.getDocumentKey()));
		
		assertThat(retVal.getEntity(), instanceOf(TestComplexEntity01.class));
		assertThat(retVal.getEntity().getUser(), is("user-9999"));
		assertThat(retVal.getEntity().getDesc(), is("説明:9999"));
		assertThat(retVal.getEntity().getAge(), is(9999));
		
	}

	@Test
	public void test_get_document_collection_not_found() throws ArangoException {
		
		// Get
		try {
			driver.getDocument(collectionName404, 1L, TestComplexEntity01.class);
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1203));
			// collection not found
		}
		
	}

	@Test
	public void test_get_document_doc_not_found() throws ArangoException {
		
		// Get
		try {
			driver.getDocument(collectionName, 1L, TestComplexEntity01.class);
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1202));
			// document not found
		}
		
	}

	// TODO: If-None-Match, If-Matchヘッダを付けても挙動が変わらない。arango-1.4.0
	
//	@Test
//	public void test_get_document_none_match_eq() throws ArangoException {
//		
//		// create document
//		TestComplexEntity01 value = new TestComplexEntity01("user-" + 9999, "説明:" + 9999, 9999);
//		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, null, false);
//		
//		assertThat(doc.getDocumentKey(), is(notNullValue()));
//		assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
//		assertThat(doc.getDocumentRevision(), is(not(0L)));
//		
//		// Get
//		DocumentEntity<TestComplexEntity01> retVal = driver.getDocument(doc.getDocumentHandle(), TestComplexEntity01.class, doc.getDocumentRevision(), null);
//		assertThat(retVal.getDocumentHandle(), is(doc.getDocumentHandle()));
//		assertThat(retVal.getDocumentRevision(), is(doc.getDocumentRevision()));
//		assertThat(retVal.getDocumentKey(), is(doc.getDocumentKey()));
//		
//		assertThat(retVal.getEntity(), instanceOf(TestComplexEntity01.class));
//		assertThat(retVal.getEntity().getUser(), is("user-9999"));
//		assertThat(retVal.getEntity().getDesc(), is("説明:9999"));
//		assertThat(retVal.getEntity().getAge(), is(9999));
//		
//	}
//
//	@Test
//	public void test_get_document_none_match_ne() throws ArangoException {
//		
//		// create document
//		TestComplexEntity01 value = new TestComplexEntity01("user-" + 9999, "説明:" + 9999, 9999);
//		DocumentEntity<TestComplexEntity01> doc = driver.createDocument(collectionName, value, null, false);
//		
//		assertThat(doc.getDocumentKey(), is(notNullValue()));
//		assertThat(doc.getDocumentHandle(), is(collectionName + "/" + doc.getDocumentKey()));
//		assertThat(doc.getDocumentRevision(), is(not(0L)));
//		
//		// Get
//		DocumentEntity<TestComplexEntity01> retVal = driver.getDocument(doc.getDocumentHandle(), TestComplexEntity01.class, doc.getDocumentRevision() + 1, null);
//		assertThat(retVal.getDocumentHandle(), is(doc.getDocumentHandle()));
//		assertThat(retVal.getDocumentRevision(), is(doc.getDocumentRevision()));
//		assertThat(retVal.getDocumentKey(), is(doc.getDocumentKey()));
//
//		assertThat(retVal.getEntity(), instanceOf(TestComplexEntity01.class));
//		assertThat(retVal.getEntity().getUser(), is("user-9999"));
//		assertThat(retVal.getEntity().getDesc(), is("説明:9999"));
//		assertThat(retVal.getEntity().getAge(), is(9999));
//
//	}

	
	/**
	 * Mapで取得した時に特別なキー(_id, _rev, _key)はEntityに入ってこないこと
	 * TODO: アノテーションで設定できるようにしようかな。。
	 * @throws ArangoException
	 */
	@Test
	public void test_get_as_map() throws ArangoException {
		
	}
	
	@Test
	public void test_checkDocument() throws ArangoException {

		DocumentEntity<String> doc = driver.createDocument(collectionName, new TestComplexEntity02(1, 2, 3), null, null);

		Long etag = driver.checkDocument(doc.getDocumentHandle());
		assertThat(etag, is(doc.getDocumentRevision()));
		
	}

	@Test
	public void test_check_document_doc_not_found() throws ArangoException {

		DocumentEntity<String> doc = driver.createDocument(collectionName, new TestComplexEntity02(1, 2, 3), null, null);

		try {
			driver.checkDocument(collectionName, 1);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(0));
			assertThat(e.getErrorNumber(), is(404));
		}
		
	}

	@Test
	public void test_delete() throws ArangoException {
		
		DocumentEntity<String> doc = driver.createDocument(collectionName, new TestComplexEntity02(1, 2, 3));

		driver.deleteDocument(doc.getDocumentHandle());
		
	}

	@Test
	public void test_delete_doc_not_found() throws ArangoException {
		
		try {
			driver.deleteDocument(collectionName, 1);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
		}
		
		
	}

}

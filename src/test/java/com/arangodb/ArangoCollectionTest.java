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

package com.arangodb;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionRevisionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoCollectionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_collection_test";

	@Before
	public void setup() {
		db.createCollection(COLLECTION_NAME, null);
	}

	@After
	public void teardown() {
		db.collection(COLLECTION_NAME).drop();
	}

	@Test
	public void insertDocument() {
		final DocumentCreateEntity<BaseDocument> doc = db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(),
			null);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew(), is(nullValue()));
		assertThat(doc.getId(), is(COLLECTION_NAME + "/" + doc.getKey()));
	}

	@Test
	public void insertDocumentAsync() throws InterruptedException, ExecutionException {
		final CompletableFuture<DocumentCreateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
				.insertDocumentAsync(new BaseDocument(), null);
		assertThat(f, is(notNullValue()));
		f.whenComplete((doc, ex) -> {
			assertThat(ex, is(nullValue()));
			assertThat(doc.getId(), is(notNullValue()));
			assertThat(doc.getKey(), is(notNullValue()));
			assertThat(doc.getRev(), is(notNullValue()));
			assertThat(doc.getNew(), is(nullValue()));
		});
		f.get();
	}

	@Test
	public void insertDocumentReturnNew() {
		final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
		final DocumentCreateEntity<BaseDocument> doc = db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(),
			options);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew(), is(notNullValue()));
	}

	@Test
	public void insertDocumentWaitForSync() {
		final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
		final DocumentCreateEntity<BaseDocument> doc = db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(),
			options);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
		assertThat(doc.getNew(), is(nullValue()));
	}

	@Test
	public void insertDocumentAsJson() {
		final DocumentCreateEntity<String> doc = db.collection(COLLECTION_NAME)
				.insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getId(), is(notNullValue()));
		assertThat(doc.getKey(), is(notNullValue()));
		assertThat(doc.getRev(), is(notNullValue()));
	}

	@Test
	public void getDocument() {
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void getDocumentIfMatch() {
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, options);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void getDocumentIfMatchFail() {
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, options);
		assertThat(document, is(nullValue()));
	}

	@Test
	public void getDocumentIfNoneMatch() {
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, options);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
	}

	@Test
	public void getDocumentIfNoneMatchFail() {
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), null);
		assertThat(createResult.getKey(), is(notNullValue()));
		final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, options);
		assertThat(document, is(nullValue()));
	}

	@Test
	public void getDocumentAsJson() {
		db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null);
		final String readResult = db.collection(COLLECTION_NAME).getDocument("docRaw", String.class, null);
		assertThat(readResult.contains("\"_key\":\"docRaw\""), is(true));
		assertThat(readResult.contains("\"_id\":\"db_collection_test\\/docRaw\""), is(true));
	}

	@Test
	public void getDocumentNotFound() {
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class);
		assertThat(document, is(nullValue()));
	}

	@Test(expected = ArangoDBException.class)
	public void getDocumentWrongKey() {
		db.collection(COLLECTION_NAME).getDocument("no/no", BaseDocument.class);
	}

	@Test
	public void getDocumentAsyncNotFound() {
		try {
			final BaseDocument document = db.collection(COLLECTION_NAME).getDocumentAsync("no", BaseDocument.class)
					.get();
			assertThat(document, is(nullValue()));
		} catch (InterruptedException | ExecutionException e) {
			fail();
		}
	}

	@Test
	public void updateDocument() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, null);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getNew(), is(nullValue()));
		assertThat(updateResult.getOld(), is(nullValue()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
		assertThat(readResult.getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
		assertThat(readResult.getRevision(), is(updateResult.getRev()));
		assertThat(readResult.getProperties().keySet(), hasItem("c"));
	}

	@Test
	public void updateDocumentIfMatch() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
		assertThat(readResult.getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
		assertThat(readResult.getRevision(), is(updateResult.getRev()));
		assertThat(readResult.getProperties().keySet(), hasItem("c"));
	}

	@Test
	public void updateDocumentIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		doc.addAttribute("c", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		doc.updateAttribute("c", null);
		try {
			final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
			db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void updateDocumentReturnNew() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));
		assertThat(updateResult.getNew(), is(notNullValue()));
		assertThat(updateResult.getNew().getKey(), is(createResult.getKey()));
		assertThat(updateResult.getNew().getRevision(), is(not(createResult.getRev())));
		assertThat(updateResult.getNew().getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(updateResult.getNew().getAttribute("a")), is("test1"));
		assertThat(updateResult.getNew().getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(updateResult.getNew().getAttribute("b")), is("test"));
	}

	@Test
	public void updateDocumentReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.addAttribute("b", "test");
		final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));
		assertThat(updateResult.getOld(), is(notNullValue()));
		assertThat(updateResult.getOld().getKey(), is(createResult.getKey()));
		assertThat(updateResult.getOld().getRevision(), is(createResult.getRev()));
		assertThat(updateResult.getOld().getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(updateResult.getOld().getAttribute("a")), is("test"));
		assertThat(updateResult.getOld().getProperties().keySet(), not(hasItem("b")));
	}

	@Test
	public void updateDocumentKeepNullTrue() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getProperties().keySet(), hasItem("a"));
	}

	@Test
	public void updateDocumentKeepNullFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", null);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getRevision(), is(notNullValue()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateDocumentMergeObjectsTrue() {
		final BaseDocument doc = new BaseDocument();
		final Map<String, String> a = new HashMap<>();
		a.put("a", "test");
		doc.addAttribute("a", a);
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		a.clear();
		a.put("b", "test");
		doc.updateAttribute("a", a);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		final Object aResult = readResult.getAttribute("a");
		assertThat(aResult, instanceOf(Map.class));
		final Map<String, String> aMap = (Map<String, String>) aResult;
		assertThat(aMap.keySet(), hasItem("a"));
		assertThat(aMap.keySet(), hasItem("b"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateDocumentMergeObjectsFalse() {
		final BaseDocument doc = new BaseDocument();
		final Map<String, String> a = new HashMap<>();
		a.put("a", "test");
		doc.addAttribute("a", a);
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		a.clear();
		a.put("b", "test");
		doc.updateAttribute("a", a);
		final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, options);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getId(), is(createResult.getId()));
		assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
		assertThat(updateResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		final Object aResult = readResult.getAttribute("a");
		assertThat(aResult, instanceOf(Map.class));
		final Map<String, String> aMap = (Map<String, String>) aResult;
		assertThat(aMap.keySet(), not(hasItem("a")));
		assertThat(aMap.keySet(), hasItem("b"));
	}

	@Test
	public void updateDocumentIgnoreRevsFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.updateAttribute("a", "test1");
		doc.setRevision("no");
		try {
			final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
			db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replaceDocument() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentUpdateEntity<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, null);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getNew(), is(nullValue()));
		assertThat(replaceResult.getOld(), is(nullValue()));
		assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getRevision(), is(replaceResult.getRev()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
		assertThat(readResult.getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
	}

	@Test
	public void replaceDocumentIfMatch() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
		final DocumentUpdateEntity<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getRevision(), is(replaceResult.getRev()));
		assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
		assertThat(readResult.getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
	}

	@Test
	public void replaceDocumentIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		try {
			final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
			db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replaceDocumentIgnoreRevsFalse() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		doc.setRevision("no");
		try {
			final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
			db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void replaceDocumentReturnNew() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
		final DocumentUpdateEntity<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
		assertThat(replaceResult.getNew(), is(notNullValue()));
		assertThat(replaceResult.getNew().getKey(), is(createResult.getKey()));
		assertThat(replaceResult.getNew().getRevision(), is(not(createResult.getRev())));
		assertThat(replaceResult.getNew().getProperties().keySet(), not(hasItem("a")));
		assertThat(replaceResult.getNew().getAttribute("b"), is(notNullValue()));
		assertThat(String.valueOf(replaceResult.getNew().getAttribute("b")), is("test"));
	}

	@Test
	public void replaceDocumentReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		doc.getProperties().clear();
		doc.addAttribute("b", "test");
		final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
		final DocumentUpdateEntity<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, options);
		assertThat(replaceResult, is(notNullValue()));
		assertThat(replaceResult.getId(), is(createResult.getId()));
		assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
		assertThat(replaceResult.getOld(), is(notNullValue()));
		assertThat(replaceResult.getOld().getKey(), is(createResult.getKey()));
		assertThat(replaceResult.getOld().getRevision(), is(createResult.getRev()));
		assertThat(replaceResult.getOld().getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(replaceResult.getOld().getAttribute("a")), is("test"));
		assertThat(replaceResult.getOld().getProperties().keySet(), not(hasItem("b")));
	}

	@Test
	public void deleteDocument() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, null);
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(document, is(nullValue()));
	}

	@Test
	public void deleteDocumentReturnOld() {
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("a", "test");
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
		final DocumentDeleteEntity<BaseDocument> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocument(createResult.getKey(), BaseDocument.class, options);
		assertThat(deleteResult.getOld(), is(notNullValue()));
		assertThat(deleteResult.getOld(), instanceOf(BaseDocument.class));
		assertThat(deleteResult.getOld().getAttribute("a"), is(notNullValue()));
		assertThat(String.valueOf(deleteResult.getOld().getAttribute("a")), is("test"));
	}

	@Test
	public void deleteDocumentIfMatch() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
		db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options);
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class, null);
		assertThat(document, is(nullValue()));
	}

	@Test
	public void deleteDocumentIfMatchFail() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
		try {
			db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createHashIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createHashIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson(), is(nullValue()));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSelectivityEstimate(), is(1));
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.hash));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createGeoIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getGeoJson(), is(false));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getType(), is(IndexType.geo1));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createGeo2Index() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createGeoIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(false));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson(), is(nullValue()));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getType(), is(IndexType.geo2));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createSkiplistIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createSkiplistIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson(), is(nullValue()));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.skiplist));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createPersistentIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createPersistentIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getGeoJson(), is(nullValue()));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.persistent));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createFulltextIndex() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).createFulltextIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getGeoJson(), is(nullValue()));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getType(), is(IndexType.fulltext));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void getIndexes() {
		final Collection<String> fields = new ArrayList<>();
		fields.add("a");
		db.collection(COLLECTION_NAME).createHashIndex(fields, null);
		final Collection<IndexEntity> indexes = db.collection(COLLECTION_NAME).getIndexes();
		assertThat(indexes, is(notNullValue()));
		assertThat(indexes.size(), is(2));
		for (final IndexEntity i : indexes) {
			assertThat(i.getType(), anyOf(is(IndexType.primary), is(IndexType.hash)));
			if (i.getType() == IndexType.hash) {
				assertThat(i.getFields().size(), is(1));
				assertThat(i.getFields(), hasItem("a"));
			}
		}
	}

	@Test
	public void truncate() {
		final BaseDocument doc = new BaseDocument();
		db.collection(COLLECTION_NAME).insertDocument(doc, null);
		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(doc.getKey(), BaseDocument.class,
			null);
		assertThat(readResult.getKey(), is(doc.getKey()));
		final CollectionEntity truncateResult = db.collection(COLLECTION_NAME).truncate();
		assertThat(truncateResult, is(notNullValue()));
		assertThat(truncateResult.getId(), is(notNullValue()));
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(doc.getKey(), BaseDocument.class,
			null);
		assertThat(document, is(nullValue()));
	}

	@Test
	public void getCount() {
		final CollectionPropertiesEntity countEmpty = db.collection(COLLECTION_NAME).count();
		assertThat(countEmpty, is(notNullValue()));
		assertThat(countEmpty.getCount(), is(0L));
		db.collection(COLLECTION_NAME).insertDocument("{}", null);
		final CollectionPropertiesEntity count = db.collection(COLLECTION_NAME).count();
		assertThat(count.getCount(), is(1L));
	}

	@Test
	public void documentExists() {
		final Boolean existsNot = db.collection(COLLECTION_NAME).documentExists("no", null);
		assertThat(existsNot, is(false));
		db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null);
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", null);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsAsync() throws Exception {
		final CompletableFuture<Boolean> existsNot = db.collection(COLLECTION_NAME).documentExistsAsync("no", null);
		existsNot.thenAccept(result -> {
			assertThat(result, is(false));
		});
		existsNot.get();
		db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null);
		final CompletableFuture<Boolean> exists = db.collection(COLLECTION_NAME).documentExistsAsync("abc", null);
		exists.thenAccept(result -> {
			assertThat(result, is(true));
		});
	}

	@Test
	public void documentExistsIfMatch() {
		final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
				.insertDocument("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsIfMatchFail() {
		db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(false));
	}

	@Test
	public void documentExistsIfNoneMatch() {
		db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(true));
	}

	@Test
	public void documentExistsIfNoneMatchFail() {
		final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
				.insertDocument("{\"_key\":\"abc\"}", null);
		final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
		final Boolean exists = db.collection(COLLECTION_NAME).documentExists("abc", options);
		assertThat(exists, is(false));
	}

	@Test
	public void insertDocuments() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(3));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(0));
	}

	@Test
	public void insertDocumentsOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(1));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(0));
	}

	@Test
	public void insertDocumentsEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values, null);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(0));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(0));
	}

	@Test
	public void insertDocumentsReturnNew() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values, options);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(3));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(0));
		for (final DocumentCreateEntity<BaseDocument> doc : docs.getDocuments()) {
			assertThat(doc.getNew(), is(notNullValue()));
			final BaseDocument baseDocument = doc.getNew();
			assertThat(baseDocument.getKey(), is(notNullValue()));
		}

	}

	@Test
	public void insertDocumentsFail() {
		final Collection<BaseDocument> values = new ArrayList<>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(2));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(1));
		assertThat(docs.getErrors().iterator().next().getErrorNum(), is(1210));
	}

	@Test
	public void deleteDocuments() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("2");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		keys.add("2");
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(2));
		for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
			assertThat(i.getKey(), anyOf(is("1"), is("2")));
		}
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(1));
		for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
			assertThat(i.getKey(), is("1"));
		}
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<>();
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(0));
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsNotExisting() {
		final Collection<BaseDocument> values = new ArrayList<>();
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<>();
		keys.add("1");
		keys.add("2");
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(0));
		assertThat(deleteResult.getErrors().size(), is(2));
	}

	@Test
	public void updateDocuments() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("2");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		for (final BaseDocument i : values) {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		}
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(2));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void updateDocumentsOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		final BaseDocument first = values.iterator().next();
		first.addAttribute("a", "test");
		updatedValues.add(first);
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(1));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void updateDocumentsEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(values, null);
		assertThat(updateResult.getDocuments().size(), is(0));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void updateDocumentsWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			values.add(new BaseDocument("1"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		for (final BaseDocument i : values) {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		}
		updatedValues.add(new BaseDocument());
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(1));
		assertThat(updateResult.getErrors().size(), is(1));
	}

	@Test
	public void replaceDocuments() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			values.add(new BaseDocument("1"));
			values.add(new BaseDocument("2"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		for (final BaseDocument i : values) {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		}
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.replaceDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(2));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void replaceDocumentsOne() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		final BaseDocument first = values.iterator().next();
		first.addAttribute("a", "test");
		updatedValues.add(first);
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(1));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void replaceDocumentsEmpty() {
		final Collection<BaseDocument> values = new ArrayList<>();
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(values, null);
		assertThat(updateResult.getDocuments().size(), is(0));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void replaceDocumentsWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<>();
		{
			values.add(new BaseDocument("1"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<>();
		for (final BaseDocument i : values) {
			i.addAttribute("a", "test");
			updatedValues.add(i);
		}
		updatedValues.add(new BaseDocument());
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues, null);
		assertThat(updateResult.getDocuments().size(), is(1));
		assertThat(updateResult.getErrors().size(), is(1));
	}

	@Test
	public void load() {
		final CollectionEntity result = db.collection(COLLECTION_NAME).load();
		assertThat(result.getName(), is(COLLECTION_NAME));
	}

	@Test
	public void unload() {
		final CollectionEntity result = db.collection(COLLECTION_NAME).unload();
		assertThat(result.getName(), is(COLLECTION_NAME));
	}

	@Test
	public void getInfo() {
		final CollectionEntity result = db.collection(COLLECTION_NAME).getInfo();
		assertThat(result.getName(), is(COLLECTION_NAME));
	}

	@Test
	public void getPropeties() {
		final CollectionPropertiesEntity result = db.collection(COLLECTION_NAME).getProperties();
		assertThat(result.getName(), is(COLLECTION_NAME));
		assertThat(result.getCount(), is(nullValue()));
	}

	@Test
	public void changeProperties() {
		final CollectionPropertiesEntity properties = db.collection(COLLECTION_NAME).getProperties();
		assertThat(properties.getWaitForSync(), is(notNullValue()));
		final CollectionPropertiesOptions options = new CollectionPropertiesOptions();
		options.waitForSync(!properties.getWaitForSync());
		options.journalSize(2000000L);
		final CollectionPropertiesEntity changedProperties = db.collection(COLLECTION_NAME).changeProperties(options);
		assertThat(changedProperties.getWaitForSync(), is(notNullValue()));
		assertThat(changedProperties.getWaitForSync(), is(not(properties.getWaitForSync())));
		assertThat(changedProperties.getJournalSize(), is(options.getJournalSize()));
	}

	@Test
	public void rename() {
		try {
			final CollectionEntity result = db.collection(COLLECTION_NAME).rename(COLLECTION_NAME + "1");
			assertThat(result, is(notNullValue()));
			assertThat(result.getName(), is(COLLECTION_NAME + "1"));
			final CollectionEntity info = db.collection(COLLECTION_NAME + "1").getInfo();
			assertThat(info.getName(), is(COLLECTION_NAME + "1"));
			try {
				db.collection(COLLECTION_NAME).getInfo();
				fail();
			} catch (final ArangoDBException e) {
			}
		} finally {
			db.collection(COLLECTION_NAME + "1").rename(COLLECTION_NAME);
		}
	}

	@Test
	public void getRevision() {
		final CollectionRevisionEntity result = db.collection(COLLECTION_NAME).getRevision();
		assertThat(result, is(notNullValue()));
		assertThat(result.getName(), is(COLLECTION_NAME));
		assertThat(result.getRevision(), is(notNullValue()));
	}

}

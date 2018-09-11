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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionRevisionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentImportEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ServerRole;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentImportOptions;
import com.arangodb.model.DocumentImportOptions.OnDuplicate;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 *
 */
@RunWith(Parameterized.class)
public class ArangoCollectionTest extends BaseTest {

	private static final String COLLECTION_NAME = "db_collection_test";
	private static final String EDGE_COLLECTION_NAME = "db_edge_collection_test";

	public ArangoCollectionTest(final Builder builder) {
		super(builder);
		db.createCollection(COLLECTION_NAME, null);
	}

	@After
	public void teardown() {
		db.collection(COLLECTION_NAME).truncate();
	}

	@Test
	public void create() {
		try {
			final CollectionEntity result = db.collection(COLLECTION_NAME + "_1").create();
			assertThat(result, is(notNullValue()));
			assertThat(result.getId(), is(notNullValue()));
		} finally {
			db.collection(COLLECTION_NAME + "_1").drop();
		}
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
	public void insertDocumentUpdateRev() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		assertThat(doc.getRevision(), is(createResult.getRev()));
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
	public void insertDocumentOverwriteReturnOld() {
		if (!requireVersion(3, 4)) {
			return;
		}
		final BaseDocument doc = new BaseDocument();
		doc.addAttribute("value", "a");
		final DocumentCreateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME).insertDocument(doc);
		doc.addAttribute("value", "b");
		final DocumentCreateEntity<BaseDocument> repsert = db.collection(COLLECTION_NAME).insertDocument(doc,
			new DocumentCreateOptions().overwrite(true).returnOld(true).returnNew(true));
		assertThat(repsert, is(notNullValue()));
		assertThat(repsert.getRev(), is(not(meta.getRev())));
		assertThat(repsert.getOld().getAttribute("value").toString(), is("a"));
		assertThat(repsert.getNew().getAttribute("value").toString(), is("b"));
		assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(1L));
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
	public void insertDocumentSilent() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final DocumentCreateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument(), new DocumentCreateOptions().silent(true));
		assertThat(meta, is(notNullValue()));
		assertThat(meta.getId(), is(nullValue()));
		assertThat(meta.getKey(), is(nullValue()));
		assertThat(meta.getRev(), is(nullValue()));
	}

	@Test
	public void insertDocumentSilentDontTouchInstance() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final BaseDocument doc = new BaseDocument();
		final String key = "testkey";
		doc.setKey(key);
		final DocumentCreateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME).insertDocument(doc,
			new DocumentCreateOptions().silent(true));
		assertThat(meta, is(notNullValue()));
		assertThat(meta.getKey(), is(nullValue()));
		assertThat(doc.getKey(), is(key));
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

	@Test
	public void getDocumentNotFoundOptionsDefault() {
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class,
			new DocumentReadOptions());
		assertThat(document, is(nullValue()));
	}

	@Test
	public void getDocumentNotFoundOptionsNull() {
		final BaseDocument document = db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class, null);
		assertThat(document, is(nullValue()));
	}

	@Test(expected = ArangoDBException.class)
	public void getDocumentNotFoundThrowException() {
		db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class,
			new DocumentReadOptions().catchException(false));
	}

	@Test(expected = ArangoDBException.class)
	public void getDocumentWrongKey() {
		db.collection(COLLECTION_NAME).getDocument("no/no", BaseDocument.class);
	}

	public void getDocumentDirtyRead() {
		final BaseDocument doc = new BaseDocument();
		db.collection(COLLECTION_NAME).insertDocument(doc);
		final VPackSlice document = db.collection(COLLECTION_NAME).getDocument(doc.getKey(), VPackSlice.class,
			new DocumentReadOptions().allowDirtyRead(true));
		assertThat(document, is(notNullValue()));
	}

	@Test
	public void getDocuments() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("3"));
		db.collection(COLLECTION_NAME).insertDocuments(values);
		final MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME)
				.getDocuments(Arrays.asList("1", "2", "3"), BaseDocument.class);
		assertThat(documents, is(notNullValue()));
		assertThat(documents.getDocuments().size(), is(3));
		for (final BaseDocument document : documents.getDocuments()) {
			assertThat(document.getId(),
				isOneOf(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3"));
		}
	}

	@Test
	public void getDocumentsDirtyRead() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("3"));
		db.collection(COLLECTION_NAME).insertDocuments(values);
		final MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME).getDocuments(
			Arrays.asList("1", "2", "3"), BaseDocument.class, new DocumentReadOptions().allowDirtyRead(true));
		assertThat(documents, is(notNullValue()));
		assertThat(documents.getDocuments().size(), is(3));
		for (final BaseDocument document : documents.getDocuments()) {
			assertThat(document.getId(),
				isOneOf(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3"));
		}
	}

	@Test
	public void getDocumentsNotFound() {
		final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
				.getDocuments(Collections.singleton("no"), BaseDocument.class);
		assertThat(readResult, is(notNullValue()));
		assertThat(readResult.getDocuments().size(), is(0));
		assertThat(readResult.getErrors().size(), is(1));
	}

	@Test
	public void getDocumentsWrongKey() {
		final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
				.getDocuments(Collections.singleton("no/no"), BaseDocument.class);
		assertThat(readResult, is(notNullValue()));
		assertThat(readResult.getDocuments().size(), is(0));
		assertThat(readResult.getErrors().size(), is(1));
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
	public void updateDocumentUpdateRev() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		assertThat(doc.getRevision(), is(createResult.getRev()));
		final DocumentUpdateEntity<BaseDocument> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), doc, null);
		assertThat(doc.getRevision(), is(updateResult.getRev()));
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

	private static class TestUpdateEntity {
		@SuppressWarnings("unused")
		private String a, b;
	}

	@Test
	public void updateDocumentSerializeNullTrue() {
		final TestUpdateEntity doc = new TestUpdateEntity();
		doc.a = "foo";
		doc.b = "foo";
		final DocumentCreateEntity<TestUpdateEntity> createResult = db.collection(COLLECTION_NAME).insertDocument(doc);
		final TestUpdateEntity patchDoc = new TestUpdateEntity();
		patchDoc.a = "bar";
		final DocumentUpdateEntity<TestUpdateEntity> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), patchDoc);
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getKey(), is(createResult.getKey()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getProperties().keySet(), hasItem("a"));
		assertThat(readResult.getAttribute("a").toString(), is("bar"));
	}

	@Test
	public void updateDocumentSerializeNullFalse() {
		final TestUpdateEntity doc = new TestUpdateEntity();
		doc.a = "foo";
		doc.b = "foo";
		final DocumentCreateEntity<TestUpdateEntity> createResult = db.collection(COLLECTION_NAME).insertDocument(doc);
		final TestUpdateEntity patchDoc = new TestUpdateEntity();
		patchDoc.a = "bar";
		final DocumentUpdateEntity<TestUpdateEntity> updateResult = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), patchDoc, new DocumentUpdateOptions().serializeNull(false));
		assertThat(updateResult, is(notNullValue()));
		assertThat(updateResult.getKey(), is(createResult.getKey()));

		final BaseDocument readResult = db.collection(COLLECTION_NAME).getDocument(createResult.getKey(),
			BaseDocument.class);
		assertThat(readResult.getKey(), is(createResult.getKey()));
		assertThat(readResult.getProperties().keySet(), hasItems("a", "b"));
		assertThat(readResult.getAttribute("a").toString(), is("bar"));
		assertThat(readResult.getAttribute("b").toString(), is("foo"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void updateDocumentMergeObjectsTrue() {
		final BaseDocument doc = new BaseDocument();
		final Map<String, String> a = new HashMap<String, String>();
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
		final Map<String, String> a = new HashMap<String, String>();
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
	public void updateDocumentSilent() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument());
		final DocumentUpdateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME)
				.updateDocument(createResult.getKey(), new BaseDocument(), new DocumentUpdateOptions().silent(true));
		assertThat(meta, is(notNullValue()));
		assertThat(meta.getId(), is(nullValue()));
		assertThat(meta.getKey(), is(nullValue()));
		assertThat(meta.getRev(), is(nullValue()));
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
	public void replaceDocumentUpdateRev() {
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc,
			null);
		assertThat(doc.getRevision(), is(createResult.getRev()));
		final DocumentUpdateEntity<BaseDocument> replaceResult = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, null);
		assertThat(doc.getRevision(), is(replaceResult.getRev()));
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
	public void replaceDocumentSilent() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument());
		final DocumentUpdateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), new BaseDocument(), new DocumentReplaceOptions().silent(true));
		assertThat(meta, is(notNullValue()));
		assertThat(meta.getId(), is(nullValue()));
		assertThat(meta.getKey(), is(nullValue()));
		assertThat(meta.getRev(), is(nullValue()));
	}

	@Test
	public void replaceDocumentSilentDontTouchInstance() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final BaseDocument doc = new BaseDocument();
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc);
		final String revision = doc.getRevision();
		assertThat(revision, is(notNullValue()));
		final DocumentUpdateEntity<BaseDocument> meta = db.collection(COLLECTION_NAME)
				.replaceDocument(createResult.getKey(), doc, new DocumentReplaceOptions().silent(true));
		assertThat(meta.getRev(), is(nullValue()));
		assertThat(doc.getRevision(), is(revision));
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
	public void deleteDocumentSilent() {
		if (!requireVersion(3, 3)) {
			return;
		}
		final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
				.insertDocument(new BaseDocument());
		final DocumentDeleteEntity<BaseDocument> meta = db.collection(COLLECTION_NAME)
				.deleteDocument(createResult.getKey(), BaseDocument.class, new DocumentDeleteOptions().silent(true));
		assertThat(meta, is(notNullValue()));
		assertThat(meta.getId(), is(nullValue()));
		assertThat(meta.getKey(), is(nullValue()));
		assertThat(meta.getRev(), is(nullValue()));
	}

	@Test
	public void getIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
		final IndexEntity readResult = db.collection(COLLECTION_NAME).getIndex(createResult.getId());
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getType(), is(createResult.getType()));
	}

	@Test
	public void getIndexByKey() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
		final IndexEntity readResult = db.collection(COLLECTION_NAME).getIndex(createResult.getId().split("/")[1]);
		assertThat(readResult.getId(), is(createResult.getId()));
		assertThat(readResult.getType(), is(createResult.getType()));
	}

	@Test
	public void deleteIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
		final String id = db.collection(COLLECTION_NAME).deleteIndex(createResult.getId());
		assertThat(id, is(createResult.getId()));
		try {
			db.getIndex(id);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void deleteIndexByKey() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
		final String id = db.collection(COLLECTION_NAME).deleteIndex(createResult.getId().split("/")[1]);
		assertThat(id, is(createResult.getId()));
		try {
			db.getIndex(id);
			fail();
		} catch (final ArangoDBException e) {
		}
	}

	@Test
	public void createHashIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		if (arangoDB.getRole() == ServerRole.SINGLE) {
			assertThat(indexResult.getSelectivityEstimate(), is(1));
		}
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.hash));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createGeoIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getUnique(), is(false));
		if (requireVersion(3, 4)) {
			assertThat(indexResult.getType(), is(IndexType.geo));
		} else {
			assertThat(indexResult.getType(), is(IndexType.geo1));
		}
	}

	@Test
	public void createGeo2Index() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getUnique(), is(false));
		if (requireVersion(3, 4)) {
			assertThat(indexResult.getType(), is(IndexType.geo));
		} else {
			assertThat(indexResult.getType(), is(IndexType.geo2));
		}
	}

	@Test
	public void createSkiplistIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureSkiplistIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.skiplist));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createPersistentIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		fields.add("b");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getFields(), hasItem("b"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getMinLength(), is(nullValue()));
		assertThat(indexResult.getSparse(), is(false));
		assertThat(indexResult.getType(), is(IndexType.persistent));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void createFulltextIndex() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureFulltextIndex(fields, null);
		assertThat(indexResult, is(notNullValue()));
		assertThat(indexResult.getConstraint(), is(nullValue()));
		assertThat(indexResult.getFields(), hasItem("a"));
		assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
		assertThat(indexResult.getIsNewlyCreated(), is(true));
		assertThat(indexResult.getSparse(), is(true));
		assertThat(indexResult.getType(), is(IndexType.fulltext));
		assertThat(indexResult.getUnique(), is(false));
	}

	@Test
	public void getIndexes() {
		final Collection<String> fields = new ArrayList<String>();
		fields.add("a");
		db.collection(COLLECTION_NAME).ensureHashIndex(fields, null);
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
	public void getEdgeIndex() {
		try {
			db.createCollection(EDGE_COLLECTION_NAME, new CollectionCreateOptions().type(CollectionType.EDGES));
			final Collection<IndexEntity> indexes = db.collection(EDGE_COLLECTION_NAME).getIndexes();
			assertThat(indexes, is(notNullValue()));
			assertThat(indexes.size(), is(2));
			for (final IndexEntity i : indexes) {
				assertThat(i.getType(), anyOf(is(IndexType.primary), is(IndexType.edge)));
			}
		} finally {
			try {
				db.collection(EDGE_COLLECTION_NAME).drop();
			} catch (final ArangoDBException e) {
			}
		}
	}

	@Test
	public void exists() {
		assertThat(db.collection(COLLECTION_NAME).exists(), is(true));
		assertThat(db.collection(COLLECTION_NAME + "no").exists(), is(false));
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

	@Test(expected = ArangoDBException.class)
	public void documentExistsThrowExcpetion() {
		db.collection(COLLECTION_NAME).documentExists("no", new DocumentExistsOptions().catchException(false));
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
	public void insertDocumentsOverwrite() {
		if (!requireVersion(3, 4)) {
			return;
		}
		final BaseDocument doc1 = new BaseDocument();
		doc1.addAttribute("value", "a");
		final DocumentCreateEntity<BaseDocument> meta1 = db.collection(COLLECTION_NAME).insertDocument(doc1);
		final BaseDocument doc2 = new BaseDocument();
		doc2.addAttribute("value", "a");
		final DocumentCreateEntity<BaseDocument> meta2 = db.collection(COLLECTION_NAME).insertDocument(doc2);

		doc1.addAttribute("value", "b");
		doc2.addAttribute("value", "b");

		final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> repsert = db.collection(COLLECTION_NAME)
				.insertDocuments(Arrays.asList(doc1, doc2),
					new DocumentCreateOptions().overwrite(true).returnOld(true).returnNew(true));
		assertThat(repsert, is(notNullValue()));
		for (final DocumentCreateEntity<BaseDocument> documentCreateEntity : repsert.getDocuments()) {
			assertThat(documentCreateEntity.getRev(), is(not(meta1.getRev())));
			assertThat(documentCreateEntity.getRev(), is(not(meta2.getRev())));
			assertThat(documentCreateEntity.getOld().getAttribute("value").toString(), is("a"));
			assertThat(documentCreateEntity.getNew().getAttribute("value").toString(), is("b"));
		}
		assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(2L));
	}

	@Test
	public void insertDocumentsJson() {
		final Collection<String> values = new ArrayList<String>();
		values.add("{}");
		values.add("{}");
		values.add("{}");
		final MultiDocumentEntity<DocumentCreateEntity<String>> docs = db.collection(COLLECTION_NAME)
				.insertDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getDocuments(), is(notNullValue()));
		assertThat(docs.getDocuments().size(), is(3));
		assertThat(docs.getErrors(), is(notNullValue()));
		assertThat(docs.getErrors().size(), is(0));
	}

	@Test
	public void insertDocumentsOne() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
	public void importDocuments() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(values.size()));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonList() {
		final Collection<String> values = new ArrayList<String>();
		values.add("{}");
		values.add("{}");
		values.add("{}");
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(values.size()));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsDuplicateDefaultError() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsDuplicateError() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.error));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsDuplicateIgnore() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(1));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsDuplicateReplace() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(1));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsDuplicateUpdate() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.update));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(1));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsCompleteFail() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		try {
			db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true));
			fail();
		} catch (final ArangoDBException e) {
			assertThat(e.getMessage(), containsString("1210"));
		}
	}

	@Test
	public void importDocumentsDetails() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument("1"));
		values.add(new BaseDocument("2"));
		values.add(new BaseDocument("2"));
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().details(true));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails().size(), is(1));
		assertThat(docs.getDetails().iterator().next(), containsString("unique constraint violated"));
	}

	@Test
	public void importDocumentsOverwriteFalse() {
		final ArangoCollection collection = db.collection(COLLECTION_NAME);
		collection.insertDocument(new BaseDocument());
		assertThat(collection.count().getCount(), is(1L));

		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		collection.importDocuments(values, new DocumentImportOptions().overwrite(false));
		assertThat(collection.count().getCount(), is(3L));
	}

	@Test
	public void importDocumentsOverwriteTrue() {
		final ArangoCollection collection = db.collection(COLLECTION_NAME);
		collection.insertDocument(new BaseDocument());
		assertThat(collection.count().getCount(), is(1L));

		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		values.add(new BaseDocument());
		values.add(new BaseDocument());
		collection.importDocuments(values, new DocumentImportOptions().overwrite(true));
		assertThat(collection.count().getCount(), is(2L));
	}

	@Test
	public void importDocumentsFromToPrefix() {
		db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES));
		final ArangoCollection collection = db.collection(COLLECTION_NAME + "_edge");
		try {
			final Collection<BaseEdgeDocument> values = new ArrayList<BaseEdgeDocument>();
			final String[] keys = { "1", "2" };
			for (int i = 0; i < keys.length; i++) {
				values.add(new BaseEdgeDocument(keys[i], "from", "to"));
			}
			assertThat(values.size(), is(keys.length));

			final DocumentImportEntity importResult = collection.importDocuments(values,
				new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
			assertThat(importResult, is(notNullValue()));
			assertThat(importResult.getCreated(), is(values.size()));
			for (int i = 0; i < keys.length; i++) {
				final BaseEdgeDocument doc = collection.getDocument(keys[i], BaseEdgeDocument.class);
				assertThat(doc, is(notNullValue()));
				assertThat(doc.getFrom(), is("foo/from"));
				assertThat(doc.getTo(), is("bar/to"));
			}
		} finally {
			collection.drop();
		}
	}

	@Test
	public void importDocumentsJson() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonDuplicateDefaultError() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values);
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonDuplicateError() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.error));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonDuplicateIgnore() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(1));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonDuplicateReplace() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(1));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonDuplicateUpdate() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().onDuplicate(OnDuplicate.update));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(0));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(1));
		assertThat(docs.getDetails(), is(empty()));
	}

	@Test
	public void importDocumentsJsonCompleteFail() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		try {
			db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true));
			fail();
		} catch (final ArangoDBException e) {
			assertThat(e.getMessage(), containsString("1210"));
		}
	}

	@Test
	public void importDocumentsJsonDetails() {
		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
		final DocumentImportEntity docs = db.collection(COLLECTION_NAME).importDocuments(values,
			new DocumentImportOptions().details(true));
		assertThat(docs, is(notNullValue()));
		assertThat(docs.getCreated(), is(2));
		assertThat(docs.getEmpty(), is(0));
		assertThat(docs.getErrors(), is(1));
		assertThat(docs.getIgnored(), is(0));
		assertThat(docs.getUpdated(), is(0));
		assertThat(docs.getDetails().size(), is(1));
		assertThat(docs.getDetails().iterator().next(), containsString("unique constraint violated"));
	}

	@Test
	public void importDocumentsJsonOverwriteFalse() {
		final ArangoCollection collection = db.collection(COLLECTION_NAME);
		collection.insertDocument(new BaseDocument());
		assertThat(collection.count().getCount(), is(1L));

		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
		collection.importDocuments(values, new DocumentImportOptions().overwrite(false));
		assertThat(collection.count().getCount(), is(3L));
	}

	@Test
	public void importDocumentsJsonOverwriteTrue() {
		final ArangoCollection collection = db.collection(COLLECTION_NAME);
		collection.insertDocument(new BaseDocument());
		assertThat(collection.count().getCount(), is(1L));

		final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
		collection.importDocuments(values, new DocumentImportOptions().overwrite(true));
		assertThat(collection.count().getCount(), is(2L));
	}

	@Test
	public void importDocumentsJsonFromToPrefix() {
		db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES));
		final ArangoCollection collection = db.collection(COLLECTION_NAME + "_edge");
		try {
			final String[] keys = { "1", "2" };
			final String values = "[{\"_key\":\"1\",\"_from\":\"from\",\"_to\":\"to\"},{\"_key\":\"2\",\"_from\":\"from\",\"_to\":\"to\"}]";

			final DocumentImportEntity importResult = collection.importDocuments(values,
				new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
			assertThat(importResult, is(notNullValue()));
			assertThat(importResult.getCreated(), is(2));
			for (int i = 0; i < keys.length; i++) {
				final BaseEdgeDocument doc = collection.getDocument(keys[i], BaseEdgeDocument.class);
				assertThat(doc, is(notNullValue()));
				assertThat(doc.getFrom(), is("foo/from"));
				assertThat(doc.getTo(), is("bar/to"));
			}
		} finally {
			collection.drop();
		}
	}

	@Test
	public void deleteDocumentsByKey() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final Collection<String> keys = new ArrayList<String>();
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
	public void deleteDocumentsByDocuments() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(values, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(2));
		for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
			assertThat(i.getKey(), anyOf(is("1"), is("2")));
		}
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsByKeyOne() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<String>();
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
	public void deleteDocumentsByDocumentOne() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(values, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(1));
		for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
			assertThat(i.getKey(), is("1"));
		}
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsEmpty() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<String>();
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(0));
		assertThat(deleteResult.getErrors().size(), is(0));
	}

	@Test
	public void deleteDocumentsByKeyNotExisting() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<String> keys = new ArrayList<String>();
		keys.add("1");
		keys.add("2");
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(keys, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(0));
		assertThat(deleteResult.getErrors().size(), is(2));
	}

	@Test
	public void deleteDocumentsByDocumentsNotExisting() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = db.collection(COLLECTION_NAME)
				.deleteDocuments(values, null, null);
		assertThat(deleteResult, is(notNullValue()));
		assertThat(deleteResult.getDocuments().size(), is(0));
		assertThat(deleteResult.getErrors().size(), is(2));
	}

	@Test
	public void updateDocuments() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(values, null);
		assertThat(updateResult.getDocuments().size(), is(0));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void updateDocumentsWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			values.add(new BaseDocument("1"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
	public void updateDocumentsJson() {
		final Collection<String> values = new ArrayList<String>();
		values.add("{\"_key\":\"1\"}");
		values.add("{\"_key\":\"2\"}");
		db.collection(COLLECTION_NAME).insertDocuments(values);

		final Collection<String> updatedValues = new ArrayList<String>();
		updatedValues.add("{\"_key\":\"1\", \"foo\":\"bar\"}");
		updatedValues.add("{\"_key\":\"2\", \"foo\":\"bar\"}");
		final MultiDocumentEntity<DocumentUpdateEntity<String>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(updatedValues);
		assertThat(updateResult.getDocuments().size(), is(2));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void replaceDocuments() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			values.add(new BaseDocument("1"));
			values.add(new BaseDocument("2"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			final BaseDocument e = new BaseDocument();
			e.setKey("1");
			values.add(e);
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = db.collection(COLLECTION_NAME)
				.updateDocuments(values, null);
		assertThat(updateResult.getDocuments().size(), is(0));
		assertThat(updateResult.getErrors().size(), is(0));
	}

	@Test
	public void replaceDocumentsWithoutKey() {
		final Collection<BaseDocument> values = new ArrayList<BaseDocument>();
		{
			values.add(new BaseDocument("1"));
		}
		db.collection(COLLECTION_NAME).insertDocuments(values, null);
		final Collection<BaseDocument> updatedValues = new ArrayList<BaseDocument>();
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
	public void replaceDocumentsJson() {
		final Collection<String> values = new ArrayList<String>();
		values.add("{\"_key\":\"1\"}");
		values.add("{\"_key\":\"2\"}");
		db.collection(COLLECTION_NAME).insertDocuments(values);

		final Collection<String> updatedValues = new ArrayList<String>();
		updatedValues.add("{\"_key\":\"1\", \"foo\":\"bar\"}");
		updatedValues.add("{\"_key\":\"2\", \"foo\":\"bar\"}");
		final MultiDocumentEntity<DocumentUpdateEntity<String>> updateResult = db.collection(COLLECTION_NAME)
				.replaceDocuments(updatedValues);
		assertThat(updateResult.getDocuments().size(), is(2));
		assertThat(updateResult.getErrors().size(), is(0));
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
		final String collection = COLLECTION_NAME + "_prop";
		try {
			db.createCollection(collection);
			final CollectionPropertiesEntity properties = db.collection(collection).getProperties();
			assertThat(properties.getWaitForSync(), is(notNullValue()));
			final CollectionPropertiesOptions options = new CollectionPropertiesOptions();
			options.waitForSync(!properties.getWaitForSync());
			options.journalSize(2000000L);
			final CollectionPropertiesEntity changedProperties = db.collection(collection).changeProperties(options);
			assertThat(changedProperties.getWaitForSync(), is(notNullValue()));
			assertThat(changedProperties.getWaitForSync(), is(not(properties.getWaitForSync())));
		} finally {
			db.collection(collection).drop();
		}
	}

	@Test
	public void rename() {
		if (arangoDB.getRole() != ServerRole.SINGLE) {
			return;
		}
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
	public void renameDontBreaksCollectionHandler() {
		if (arangoDB.getRole() != ServerRole.SINGLE) {
			return;
		}
		try {
			final ArangoCollection collection = db.collection(COLLECTION_NAME);
			collection.rename(COLLECTION_NAME + "1");
			assertThat(collection.getInfo(), is(notNullValue()));
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

	@Test
	public void keyWithSpecialCharacter() {
		final String key = "myKey_-:.@()+,=;$!*'%";
		db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));
		final BaseDocument doc = db.collection(COLLECTION_NAME).getDocument(key, BaseDocument.class);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getKey(), is(key));
	}

	@Test
	public void alreadyUrlEncodedkey() {
		final String key = "http%3A%2F%2Fexample.com%2F";
		db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));
		final BaseDocument doc = db.collection(COLLECTION_NAME).getDocument(key, BaseDocument.class);
		assertThat(doc, is(notNullValue()));
		assertThat(doc.getKey(), is(key));
	}

	@Test
	public void grantAccessRW() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW);
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test
	public void grantAccessRO() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RO);
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test
	public void grantAccessNONE() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE);
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void grantAccessUserNotFound() {
		db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW);
	}

	@Test
	public void revokeAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE);
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void revokeAccessUserNotFound() {
		db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE);
	}

	@Test
	public void resetAccess() {
		try {
			arangoDB.createUser("user1", "1234", null);
			db.collection(COLLECTION_NAME).resetAccess("user1");
		} finally {
			arangoDB.deleteUser("user1");
		}
	}

	@Test(expected = ArangoDBException.class)
	public void resetAccessUserNotFound() {
		db.collection(COLLECTION_NAME).resetAccess("user1");
	}

	@Test
	public void getPermissions() {
		assertThat(Permissions.RW, is(db.collection(COLLECTION_NAME).getPermissions("root")));
	}

}

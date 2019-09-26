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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import com.arangodb.model.DocumentImportOptions.OnDuplicate;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoCollectionTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_collection_test";

    public ArangoCollectionTest() throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        if (!collection.exists().get()) {
            collection.create().get();
        }
    }

    @BeforeClass
    public static void setup() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
    }

    @After
    public void teardown() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    public void create() throws InterruptedException, ExecutionException {
        final CollectionEntity result = db.collection(COLLECTION_NAME + "_1").create().get();
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notNullValue()));
        db.collection(COLLECTION_NAME + "_1").drop().get();
    }

    @Test
    public void insertDocument() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null)
                .whenComplete((doc, ex) -> {
                    assertThat(ex, is(nullValue()));
                    assertThat(doc.getId(), is(notNullValue()));
                    assertThat(doc.getKey(), is(notNullValue()));
                    assertThat(doc.getRev(), is(notNullValue()));
                    assertThat(doc.getNew(), is(nullValue()));
                    assertThat(doc.getId(), is(COLLECTION_NAME + "/" + doc.getKey()));
                })
                .get();
    }

    @Test
    public void insertDocumentReturnNew() throws InterruptedException, ExecutionException {
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), options)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.getId(), is(notNullValue()));
                    assertThat(doc.getKey(), is(notNullValue()));
                    assertThat(doc.getRev(), is(notNullValue()));
                    assertThat(doc.getNew(), is(notNullValue()));
                })
                .get();
    }

    @Test
    public void insertDocumentWaitForSync() throws InterruptedException, ExecutionException {
        final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), options)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.getId(), is(notNullValue()));
                    assertThat(doc.getKey(), is(notNullValue()));
                    assertThat(doc.getRev(), is(notNullValue()));
                    assertThat(doc.getNew(), is(nullValue()));
                })
                .get();
    }

    @Test
    public void insertDocumentAsJson() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null)
                .whenComplete((doc, ex) -> {
                    assertThat(doc, is(notNullValue()));
                    assertThat(doc.getId(), is(notNullValue()));
                    assertThat(doc.getKey(), is(notNullValue()));
                    assertThat(doc.getRev(), is(notNullValue()));
                })
                .get();
    }

    @Test
    public void getDocument() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey(), is(notNullValue()));
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey(), is(createResult.getKey()));
                    assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
                })
                .get();
    }

    @Test
    public void getDocumentIfMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey(), is(createResult.getKey()));
                    assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
                })
                .get();
    }

    @Test
    public void getDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((doc, ex) -> assertThat(doc, is(nullValue())))
                .get();
    }

    @Test
    public void getDocumentIfNoneMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey(), is(createResult.getKey()));
                    assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
                })
                .get();
    }

    @Test
    public void getDocumentIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((doc, ex) -> assertThat(doc, is(nullValue())))
                .get();
    }

    @Test
    public void getDocumentAsJson() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null).get();
        db.collection(COLLECTION_NAME).getDocument("docRaw", String.class, null)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.contains("\"_key\":\"docRaw\""), is(true));
                    assertThat(readResult.contains("\"_id\":\"db_collection_test\\/docRaw\""), is(true));
                })
                .get();
    }

    @Test
    public void getDocumentNotFound() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class)
                .whenComplete((doc, ex) -> assertThat(doc, is(nullValue())))
                .get();
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWrongKey() {
        db.collection(COLLECTION_NAME).getDocument("no/no", BaseDocument.class);
    }

    @Test
    public void getDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        db.collection(COLLECTION_NAME).insertDocuments(values).get();
        final MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME)
                .getDocuments(Arrays.asList("1", "2", "3"), BaseDocument.class).get();
        assertThat(documents, is(notNullValue()));
        assertThat(documents.getDocuments().size(), is(3));
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId(),
                    isOneOf(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3"));
        }
    }

    @Test
    public void getDocumentsNotFound() throws InterruptedException, ExecutionException {
        final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
                .getDocuments(Collections.singleton("no"), BaseDocument.class).get();
        assertThat(readResult, is(notNullValue()));
        assertThat(readResult.getDocuments().size(), is(0));
        assertThat(readResult.getErrors().size(), is(1));
    }

    @Test
    public void getDocumentsWrongKey() throws InterruptedException, ExecutionException {
        final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
                .getDocuments(Collections.singleton("no/no"), BaseDocument.class).get();
        assertThat(readResult, is(notNullValue()));
        assertThat(readResult.getDocuments().size(), is(0));
        assertThat(readResult.getErrors().size(), is(1));
    }

    @Test
    public void updateDocument() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);

        final CompletableFuture<DocumentUpdateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
                .updateDocument(createResult.getKey(), doc, null);
        f.whenComplete((updateResult, ex) -> {
            assertThat(updateResult, is(notNullValue()));
            assertThat(updateResult.getId(), is(createResult.getId()));
            assertThat(updateResult.getNew(), is(nullValue()));
            assertThat(updateResult.getOld(), is(nullValue()));
            assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
            assertThat(updateResult.getOldRev(), is(createResult.getRev()));
        }).get();
        final DocumentUpdateEntity<BaseDocument> updateResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateDocumentIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
        final CompletableFuture<DocumentUpdateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(f, is(notNullValue()));
        f.whenComplete((updateResult, ex) -> {
            assertThat(updateResult, is(notNullValue()));
            assertThat(updateResult.getId(), is(createResult.getId()));
            assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
            assertThat(updateResult.getOldRev(), is(createResult.getRev()));
        }).get();
        final DocumentUpdateEntity<BaseDocument> updateResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        try {
            final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
            db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void updateDocumentReturnNew() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
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
                })
                .get();
    }

    @Test
    public void updateDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult, is(notNullValue()));
                    assertThat(updateResult.getId(), is(createResult.getId()));
                    assertThat(updateResult.getOldRev(), is(createResult.getRev()));
                    assertThat(updateResult.getOld(), is(notNullValue()));
                    assertThat(updateResult.getOld().getKey(), is(createResult.getKey()));
                    assertThat(updateResult.getOld().getRevision(), is(createResult.getRev()));
                    assertThat(updateResult.getOld().getAttribute("a"), is(notNullValue()));
                    assertThat(String.valueOf(updateResult.getOld().getAttribute("a")), is("test"));
                    assertThat(updateResult.getOld().getProperties().keySet(), not(hasItem("b")));
                })
                .get();
    }

    @Test
    public void updateDocumentKeepNullTrue() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult, is(notNullValue()));
                    assertThat(updateResult.getId(), is(createResult.getId()));
                    assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
                    assertThat(updateResult.getOldRev(), is(createResult.getRev()));
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateDocumentKeepNullFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult, is(notNullValue()));
                    assertThat(updateResult.getId(), is(createResult.getId()));
                    assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
                    assertThat(updateResult.getOldRev(), is(createResult.getRev()));
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getRevision(), is(notNullValue()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateDocumentMergeObjectsTrue() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult, is(notNullValue()));
                    assertThat(updateResult.getId(), is(createResult.getId()));
                    assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
                    assertThat(updateResult.getOldRev(), is(createResult.getRev()));
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult, instanceOf(Map.class));
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet(), hasItem("a"));
        assertThat(aMap.keySet(), hasItem("b"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateDocumentMergeObjectsFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult, is(notNullValue()));
                    assertThat(updateResult.getId(), is(createResult.getId()));
                    assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
                    assertThat(updateResult.getOldRev(), is(createResult.getRev()));
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult, instanceOf(Map.class));
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet(), not(hasItem("a")));
        assertThat(aMap.keySet(), hasItem("b"));
    }

    @Test
    public void updateDocumentIgnoreRevsFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.setRevision("no");
        try {
            final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
            db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void replaceDocument() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final CompletableFuture<DocumentUpdateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
                .replaceDocument(createResult.getKey(), doc, null);
        f.whenComplete((replaceResult, ex) -> {
            assertThat(replaceResult, is(notNullValue()));
            assertThat(replaceResult.getId(), is(createResult.getId()));
            assertThat(replaceResult.getNew(), is(nullValue()));
            assertThat(replaceResult.getOld(), is(nullValue()));
            assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
            assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
        }).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceDocumentIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
        final CompletableFuture<DocumentUpdateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
                .replaceDocument(createResult.getKey(), doc, options);
        f.whenComplete((replaceResult, ex) -> {
            assertThat(replaceResult, is(notNullValue()));
            assertThat(replaceResult.getId(), is(createResult.getId()));
            assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
            assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
        }).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        try {
            final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
            db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void replaceDocumentIgnoreRevsFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        doc.setRevision("no");
        try {
            final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
            db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void replaceDocumentReturnNew() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
        db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options)
                .whenComplete((replaceResult, ex) -> {
                    assertThat(replaceResult, is(notNullValue()));
                    assertThat(replaceResult.getId(), is(createResult.getId()));
                    assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
                    assertThat(replaceResult.getNew(), is(notNullValue()));
                    assertThat(replaceResult.getNew().getKey(), is(createResult.getKey()));
                    assertThat(replaceResult.getNew().getRevision(), is(not(createResult.getRev())));
                    assertThat(replaceResult.getNew().getProperties().keySet(), not(hasItem("a")));
                    assertThat(replaceResult.getNew().getAttribute("b"), is(notNullValue()));
                    assertThat(String.valueOf(replaceResult.getNew().getAttribute("b")), is("test"));
                })
                .get();
    }

    @Test
    public void replaceDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
        db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options)
                .whenComplete((replaceResult, ex) -> {
                    assertThat(replaceResult, is(notNullValue()));
                    assertThat(replaceResult.getId(), is(createResult.getId()));
                    assertThat(replaceResult.getOldRev(), is(createResult.getRev()));
                    assertThat(replaceResult.getOld(), is(notNullValue()));
                    assertThat(replaceResult.getOld().getKey(), is(createResult.getKey()));
                    assertThat(replaceResult.getOld().getRevision(), is(createResult.getRev()));
                    assertThat(replaceResult.getOld().getAttribute("a"), is(notNullValue()));
                    assertThat(String.valueOf(replaceResult.getOld().getAttribute("a")), is("test"));
                    assertThat(replaceResult.getOld().getProperties().keySet(), not(hasItem("b")));
                })
                .get();
    }

    @Test
    public void deleteDocument() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, null).get();
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((document, ex) -> assertThat(document, is(nullValue())))
                .get();
    }

    @Test
    public void deleteDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult.getOld(), is(notNullValue()));
                    assertThat(deleteResult.getOld(), instanceOf(BaseDocument.class));
                    assertThat(deleteResult.getOld().getAttribute("a"), is(notNullValue()));
                    assertThat(String.valueOf(deleteResult.getOld().getAttribute("a")), is("test"));
                })
                .get();
    }

    @Test
    public void deleteDocumentIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options).get();
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((document, ex) -> assertThat(document, is(nullValue())))
                .get();
    }

    @Test
    public void deleteDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
        try {
            db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
    }

    @Test
    public void getIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndex(createResult.getId())
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId(), is(createResult.getId()));
                    assertThat(readResult.getType(), is(createResult.getType()));
                })
                .get();
    }

    @Test
    public void getIndexByKey() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndex(createResult.getId().split("/")[1])
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId(), is(createResult.getId()));
                    assertThat(readResult.getType(), is(createResult.getType()));
                })
                .get();
    }

    @Test
    public void deleteIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("deleteIndexField");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId()).get();
        db.collection(COLLECTION_NAME).deleteIndex(createResult.getId())
                .whenComplete((id, ex) -> {
                    assertThat(id, is(createResult.getId()));
                    try {
                        db.getIndex(id).get();
                        fail();
                    } catch (final InterruptedException exception) {
                        exception.printStackTrace();
                        fail();
                    } catch (final ExecutionException exception) {
                        assertThat(exception.getCause(), instanceOf(ArangoDBException.class));
                    }
                })
                .get();
    }

    @Test
    public void deleteIndexByKey() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("deleteIndexByKeyField");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId()).get();
        db.collection(COLLECTION_NAME).deleteIndex(createResult.getId().split("/")[1])
                .whenComplete((id, ex) -> {
                    assertThat(id, is(createResult.getId()));
                    try {
                        db.getIndex(id).get();
                        fail();
                    } catch (final InterruptedException exception) {
                        exception.printStackTrace();
                        fail();
                    } catch (final ExecutionException exception) {
                        assertThat(exception.getCause(), instanceOf(ArangoDBException.class));
                    }
                })
                .get();
    }

    @Test
    public void createHashIndex() throws InterruptedException, ExecutionException {
        final boolean singleServer = isSingleServer();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureHashIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult, is(notNullValue()));
                    assertThat(indexResult.getConstraint(), is(nullValue()));
                    assertThat(indexResult.getFields(), hasItem("a"));
                    assertThat(indexResult.getFields(), hasItem("b"));
                    assertThat(indexResult.getGeoJson(), is(nullValue()));
                    assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
                    assertThat(indexResult.getIsNewlyCreated(), is(true));
                    assertThat(indexResult.getMinLength(), is(nullValue()));
                    if (singleServer) {
                        assertThat(indexResult.getSelectivityEstimate(), is(1.0));
                    }
                    assertThat(indexResult.getSparse(), is(false));
                    assertThat(indexResult.getType(), is(IndexType.hash));
                    assertThat(indexResult.getUnique(), is(false));
                })
                .get();
    }

    @Test
    public void createHashIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final HashIndexOptions options = new HashIndexOptions();
        options.name("myHashIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, options).get();
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem("a"));
        assertThat(indexResult.getFields(), hasItem("b"));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        if (isSingleServer()) {
            assertThat(indexResult.getSelectivityEstimate(), is(1.));
        }
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.hash));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is("myHashIndex"));
    }

    @Test
    public void createGeoIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult, is(notNullValue()));
                    assertThat(indexResult.getFields(), hasItem("a"));
                    assertThat(indexResult.getGeoJson(), is(false));
                    assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
                    assertThat(indexResult.getIsNewlyCreated(), is(true));
                    assertThat(indexResult.getMinLength(), is(nullValue()));
                    assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
                    assertThat(indexResult.getSparse(), is(true));
                    assertThat(indexResult.getType(), anyOf(is(IndexType.geo), is(IndexType.geo1)));
                    assertThat(indexResult.getUnique(), is(false));
                })
                .get();
    }

    @Test
    public void createGeoIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name("myGeoIndex1");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, options).get();
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem("a"));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(true));
        assertThat(indexResult.getUnique(), is(false));
        if (isAtLeastVersion(3, 4)) {
            assertThat(indexResult.getType(), is(IndexType.geo));
        } else {
            assertThat(indexResult.getType(), is(IndexType.geo1));
        }
        assertThat(indexResult.getName(), is("myGeoIndex1"));
    }

    @Test
    public void createGeo2Index() throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null).whenComplete((indexResult, ex) -> {
            assertThat(indexResult, is(notNullValue()));
            assertThat(indexResult.getFields(), hasItem("a"));
            assertThat(indexResult.getFields(), hasItem("b"));
            assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
            assertThat(indexResult.getIsNewlyCreated(), is(true));
            assertThat(indexResult.getMinLength(), is(nullValue()));
            assertThat(indexResult.getSparse(), is(true));
            assertThat(indexResult.getUnique(), is(false));
            try {
                if (isAtLeastVersion(3, 4)) {
                    assertThat(indexResult.getType(), is(IndexType.geo));
                } else {
                    assertThat(indexResult.getType(), is(IndexType.geo2));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).get();
    }

    @Test
    public void createGeo2IndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name("myGeoIndex2");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, options).get();
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem("a"));
        assertThat(indexResult.getFields(), hasItem("b"));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(true));
        assertThat(indexResult.getUnique(), is(false));
        if (isAtLeastVersion(3, 4)) {
            assertThat(indexResult.getType(), is(IndexType.geo));
        } else {
            assertThat(indexResult.getType(), is(IndexType.geo2));
        }
        assertThat(indexResult.getName(), is("myGeoIndex2"));
    }

    @Test
    public void createSkiplistIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureSkiplistIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult, is(notNullValue()));
                    assertThat(indexResult.getConstraint(), is(nullValue()));
                    assertThat(indexResult.getFields(), hasItem("a"));
                    assertThat(indexResult.getFields(), hasItem("b"));
                    assertThat(indexResult.getGeoJson(), is(nullValue()));
                    assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
                    assertThat(indexResult.getIsNewlyCreated(), is(true));
                    assertThat(indexResult.getMinLength(), is(nullValue()));
                    assertThat(indexResult.getSparse(), is(false));
                    assertThat(indexResult.getType(), is(IndexType.skiplist));
                    assertThat(indexResult.getUnique(), is(false));
                })
                .get();
    }

    @Test
    public void createSkiplistIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final SkiplistIndexOptions options = new SkiplistIndexOptions();
        options.name("mySkiplistIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureSkiplistIndex(fields, options).get();
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
        assertThat(indexResult.getName(), is("mySkiplistIndex"));
    }

    @Test
    public void createPersistentIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult, is(notNullValue()));
                    assertThat(indexResult.getConstraint(), is(nullValue()));
                    assertThat(indexResult.getFields(), hasItem("a"));
                    assertThat(indexResult.getFields(), hasItem("b"));
                    assertThat(indexResult.getGeoJson(), is(nullValue()));
                    assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
                    assertThat(indexResult.getIsNewlyCreated(), is(true));
                    assertThat(indexResult.getMinLength(), is(nullValue()));
                    assertThat(indexResult.getSparse(), is(false));
                    assertThat(indexResult.getType(), is(IndexType.persistent));
                    assertThat(indexResult.getUnique(), is(false));
                })
                .get();
    }

    @Test
    public void createPersistentIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name("myPersistentIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, options).get();
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
        assertThat(indexResult.getName(), is("myPersistentIndex"));
    }

    @Test
    public void createFulltextIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureFulltextIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
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
                })
                .get();
    }

    @Test
    public void createFulltextIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final FulltextIndexOptions options = new FulltextIndexOptions();
        options.name("myFulltextIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureFulltextIndex(fields, options).get();
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem("a"));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getSparse(), is(true));
        assertThat(indexResult.getType(), is(IndexType.fulltext));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is("myFulltextIndex"));
    }

    @Test
    public void createTtlIndexWithoutOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        try {
            db.collection(COLLECTION_NAME).ensureTtlIndex(fields, null).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            assertThat(e.getCause().getMessage(), containsString("expireAfter attribute must be a number"));
            assertThat(((ArangoDBException) e.getCause()).getResponseCode(), is(400));
            assertThat(((ArangoDBException) e.getCause()).getErrorNum(), is(10));
        }
    }

    @Test
    public void createTtlIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");

        final TtlIndexOptions options = new TtlIndexOptions();
        options.name("myTtlIndex");
        options.expireAfter(3600);

        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureTtlIndex(fields, options).get();
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem("a"));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getType(), is(IndexType.ttl));
        assertThat(indexResult.getExpireAfter(), is(3600));
        assertThat(indexResult.getName(), is("myTtlIndex"));
    }

    @Test
    public void getIndexes() throws InterruptedException, ExecutionException {
        final int initialIndexCount = db.collection(COLLECTION_NAME).getIndexes().get().size();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndexes()
                .whenComplete((indexes, ex) -> {
                    assertThat(indexes, is(notNullValue()));
                    assertThat(indexes.size(), is(initialIndexCount + 1));
                    for (final IndexEntity i : indexes) {
                        if (i.getType() == IndexType.hash) {
                            assertThat(i.getFields().size(), is(1));
                            assertThat(i.getFields(), hasItem("a"));
                        }
                    }
                })
                .get();
    }

    @Test
    public void exists() throws InterruptedException, ExecutionException {
        assertThat(db.collection(COLLECTION_NAME).exists().get(), is(true));
        assertThat(db.collection(COLLECTION_NAME + "no").exists().get(), is(false));
    }

    @Test
    public void truncate() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        db.collection(COLLECTION_NAME).insertDocument(doc, null).get();
        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(doc.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey(), is(doc.getKey()));
        db.collection(COLLECTION_NAME).truncate()
                .whenComplete((truncateResult, ex) -> {
                    assertThat(truncateResult, is(notNullValue()));
                    assertThat(truncateResult.getId(), is(notNullValue()));
                })
                .get();
        final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(doc.getKey(), BaseDocument.class, null)
                .get();
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getCount() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).count()
                .whenComplete((countEmpty, ex) -> {
                    assertThat(countEmpty, is(notNullValue()));
                    assertThat(countEmpty.getCount(), is(0L));
                })
                .get();

        db.collection(COLLECTION_NAME).insertDocument("{}", null).get();

        db.collection(COLLECTION_NAME).count()
                .whenComplete((count, ex) -> assertThat(count.getCount(), is(1L)))
                .get();
    }

    @Test
    public void documentExists() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).documentExists("no", null)
                .whenComplete((existsNot, ex) -> assertThat(existsNot, is(false)))
                .get();

        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();

        db.collection(COLLECTION_NAME).documentExists("abc", null)
                .whenComplete((exists, ex) -> assertThat(exists, is(true)))
                .get();
    }

    @Test
    public void documentExistsIfMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists, is(true)))
                .get();
    }

    @Test
    public void documentExistsIfMatchFail() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists, is(false)))
                .get();
    }

    @Test
    public void documentExistsIfNoneMatch() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists, is(true)))
                .get();
    }

    @Test
    public void documentExistsIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists, is(false)))
                .get();
    }

    @Test
    public void insertDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getDocuments(), is(notNullValue()));
                    assertThat(docs.getDocuments().size(), is(3));
                    assertThat(docs.getErrors(), is(notNullValue()));
                    assertThat(docs.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void insertDocumentsOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getDocuments(), is(notNullValue()));
                    assertThat(docs.getDocuments().size(), is(1));
                    assertThat(docs.getErrors(), is(notNullValue()));
                    assertThat(docs.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void insertDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getDocuments(), is(notNullValue()));
                    assertThat(docs.getDocuments().size(), is(0));
                    assertThat(docs.getErrors(), is(notNullValue()));
                    assertThat(docs.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void insertDocumentsReturnNew() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        db.collection(COLLECTION_NAME).insertDocuments(values, options)
                .whenComplete((docs, ex) -> {
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
                })
                .get();
    }

    @Test
    public void insertDocumentsFail() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).insertDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getDocuments(), is(notNullValue()));
                    assertThat(docs.getDocuments().size(), is(2));
                    assertThat(docs.getErrors(), is(notNullValue()));
                    assertThat(docs.getErrors().size(), is(1));
                    assertThat(docs.getErrors().iterator().next().getErrorNum(), is(1210));
                })
                .get();
    }

    @Test
    public void importDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(values.size()));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsDuplicateDefaultError() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsDuplicateError() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsDuplicateIgnore() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(1));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsDuplicateReplace() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(1));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsDuplicateUpdate() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(1));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsCompleteFail() throws InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        try {
            db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true)).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            assertThat(((ArangoDBException) e.getCause()).getErrorNum(), is(1210));
        }
    }

    @Test
    public void importDocumentsDetails() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().details(true))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails().size(), is(1));
                    assertThat(docs.getDetails().iterator().next(), containsString("unique constraint violated"));
                })
                .get();
    }

    @Test
    public void importDocumentsOverwriteFalse() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount(), is(1L));

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount(), is(3L));
    }

    @Test
    public void importDocumentsOverwriteTrue() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount(), is(1L));

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount(), is(2L));
    }

    @Test
    public void importDocumentsFromToPrefix() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES)).get();
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME + "_edge");
        try {
            final Collection<BaseEdgeDocument> values = new ArrayList<>();
            final String[] keys = {"1", "2"};
            for (String s : keys) {
                values.add(new BaseEdgeDocument(s, "from", "to"));
            }
            assertThat(values.size(), is(keys.length));

            final DocumentImportEntity importResult = collection
                    .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar")).get();
            assertThat(importResult, is(notNullValue()));
            assertThat(importResult.getCreated(), is(values.size()));
            for (String key : keys) {
                BaseEdgeDocument doc;
                doc = collection.getDocument(key, BaseEdgeDocument.class).get();
                assertThat(doc, is(notNullValue()));
                assertThat(doc.getFrom(), is("foo/from"));
                assertThat(doc.getTo(), is("bar/to"));
            }
        } finally {
            collection.drop().get();
        }
    }

    @Test
    public void importDocumentsJson() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonDuplicateDefaultError() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonDuplicateError() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonDuplicateIgnore() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(1));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonDuplicateReplace() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(1));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonDuplicateUpdate() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(0));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(1));
                    assertThat(docs.getDetails(), is(empty()));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonCompleteFail() throws InterruptedException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        try {
            db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true)).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
            assertThat(((ArangoDBException) e.getCause()).getErrorNum(), is(1210));
        }
    }

    @Test
    public void importDocumentsJsonDetails() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().details(true))
                .whenComplete((docs, ex) -> {
                    assertThat(docs, is(notNullValue()));
                    assertThat(docs.getCreated(), is(2));
                    assertThat(docs.getEmpty(), is(0));
                    assertThat(docs.getErrors(), is(1));
                    assertThat(docs.getIgnored(), is(0));
                    assertThat(docs.getUpdated(), is(0));
                    assertThat(docs.getDetails().size(), is(1));
                    assertThat(docs.getDetails().iterator().next(), containsString("unique constraint violated"));
                })
                .get();
    }

    @Test
    public void importDocumentsJsonOverwriteFalse() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount(), is(1L));

        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount(), is(3L));
    }

    @Test
    public void importDocumentsJsonOverwriteTrue() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount(), is(1L));

        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount(), is(2L));
    }

    @Test
    public void importDocumentsJsonFromToPrefix() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES)).get();
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME + "_edge");
        try {
            final String[] keys = {"1", "2"};
            final String values = "[{\"_key\":\"1\",\"_from\":\"from\",\"_to\":\"to\"},{\"_key\":\"2\",\"_from\":\"from\",\"_to\":\"to\"}]";

            final DocumentImportEntity importResult = collection
                    .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar")).get();
            assertThat(importResult, is(notNullValue()));
            assertThat(importResult.getCreated(), is(2));
            for (String key : keys) {
                BaseEdgeDocument doc;
                doc = collection.getDocument(key, BaseEdgeDocument.class).get();
                assertThat(doc, is(notNullValue()));
                assertThat(doc.getFrom(), is("foo/from"));
                assertThat(doc.getTo(), is("bar/to"));
            }
        } finally {
            collection.drop().get();
        }
    }

    @Test
    public void deleteDocumentsByKey() throws InterruptedException, ExecutionException {
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
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(2));
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey(), anyOf(is("1"), is("2")));
                    }
                    assertThat(deleteResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void deleteDocumentsByDocuments() throws InterruptedException, ExecutionException {
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
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        db.collection(COLLECTION_NAME).deleteDocuments(values, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(2));
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey(), anyOf(is("1"), is("2")));
                    }
                    assertThat(deleteResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void deleteDocumentsByKeyOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(1));
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey(), is("1"));
                    }
                    assertThat(deleteResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void deleteDocumentsByDocumentOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        db.collection(COLLECTION_NAME).deleteDocuments(values, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(1));
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey(), is("1"));
                    }
                    assertThat(deleteResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void deleteDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(0));
                    assertThat(deleteResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void deleteDocumentsByKeyNotExisting() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(0));
                    assertThat(deleteResult.getErrors().size(), is(2));
                })
                .get();
    }

    @Test
    public void deleteDocumentsByDocumentsNotExisting() throws InterruptedException, ExecutionException {
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
        db.collection(COLLECTION_NAME).deleteDocuments(values, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult, is(notNullValue()));
                    assertThat(deleteResult.getDocuments().size(), is(0));
                    assertThat(deleteResult.getErrors().size(), is(2));
                })
                .get();
    }

    @Test
    public void updateDocuments() throws InterruptedException, ExecutionException {
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
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        db.collection(COLLECTION_NAME).updateDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(2));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void updateDocumentsOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        db.collection(COLLECTION_NAME).updateDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(1));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void updateDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).updateDocuments(values, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(0));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void updateDocumentsWithoutKey() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        db.collection(COLLECTION_NAME).updateDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(1));
                    assertThat(updateResult.getErrors().size(), is(1));
                })
                .get();
    }

    @Test
    public void replaceDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
            values.add(new BaseDocument("2"));
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        db.collection(COLLECTION_NAME).replaceDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(2));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void replaceDocumentsOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        db.collection(COLLECTION_NAME).updateDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(1));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void replaceDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).updateDocuments(values, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(0));
                    assertThat(updateResult.getErrors().size(), is(0));
                })
                .get();
    }

    @Test
    public void replaceDocumentsWithoutKey() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        db.collection(COLLECTION_NAME).updateDocuments(updatedValues, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size(), is(1));
                    assertThat(updateResult.getErrors().size(), is(1));
                })
                .get();
    }

    @Test
    public void load() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).load()
                .whenComplete((result, ex) -> assertThat(result.getName(), is(COLLECTION_NAME)))
                .get();
    }

    @Test
    public void unload() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).unload()
                .whenComplete((result, ex) -> assertThat(result.getName(), is(COLLECTION_NAME)))
                .get();
    }

    @Test
    public void getInfo() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getInfo()
                .whenComplete((result, ex) -> assertThat(result.getName(), is(COLLECTION_NAME)))
                .get();
    }

    @Test
    public void getPropeties() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getProperties()
                .whenComplete((result, ex) -> {
                    assertThat(result.getName(), is(COLLECTION_NAME));
                    assertThat(result.getCount(), is(nullValue()));
                })
                .get();
    }

    @Test
    public void changeProperties() throws InterruptedException, ExecutionException {
        final String collection = COLLECTION_NAME + "_prop";
        try {
            db.createCollection(collection).get();
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
            assertThat(properties.getWaitForSync(), is(notNullValue()));
            final CollectionPropertiesOptions options = new CollectionPropertiesOptions();
            options.waitForSync(!properties.getWaitForSync());
            options.journalSize(2000000L);
            db.collection(collection).changeProperties(options)
                    .whenComplete((changedProperties, ex) -> {
                        assertThat(changedProperties.getWaitForSync(), is(notNullValue()));
                        assertThat(changedProperties.getWaitForSync(), is(not(properties.getWaitForSync())));
                    })
                    .get();
        } finally {
            db.collection(collection).drop().get();
        }
    }

    @Test
    public void rename() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        db.collection(COLLECTION_NAME).rename(COLLECTION_NAME + "1")
                .whenComplete((result, ex) -> {
                    assertThat(result, is(notNullValue()));
                    assertThat(result.getName(), is(COLLECTION_NAME + "1"));
                })
                .get();
        final CollectionEntity info = db.collection(COLLECTION_NAME + "1").getInfo().get();
        assertThat(info.getName(), is(COLLECTION_NAME + "1"));
        try {
            db.collection(COLLECTION_NAME).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause(), instanceOf(ArangoDBException.class));
        }
        db.collection(COLLECTION_NAME + "1").rename(COLLECTION_NAME).get();
    }

    @Test
    public void responsibleShard() throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 5));
        ShardEntity shard = db.collection(COLLECTION_NAME).getResponsibleShard(new BaseDocument("testKey")).get();
        assertThat(shard, is(notNullValue()));
        assertThat(shard.getShardId(), is(notNullValue()));
    }

    @Test
    public void getRevision() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getRevision()
                .whenComplete((result, ex) -> {
                    assertThat(result, is(notNullValue()));
                    assertThat(result.getName(), is(COLLECTION_NAME));
                    assertThat(result.getRevision(), is(notNullValue()));
                })
                .get();
    }

    @Test
    public void grantAccessRW() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void grantAccessRO() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RO).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    public void grantAccessNONE() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void grantAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW).get();
    }

    @Test
    public void revokeAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void revokeAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get();
    }

    @Test
    public void resetAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).resetAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test(expected = ExecutionException.class)
    public void resetAccessUserNotFound() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).resetAccess("user1").get();
    }

    @Test
    public void getPermissions() throws InterruptedException, ExecutionException {
        assertThat(Permissions.RW, is(db.collection(COLLECTION_NAME).getPermissions("root").get()));
    }
}

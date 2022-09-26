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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoCollectionTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_collection_test";

    ArangoCollectionTest() throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        if (!collection.exists().get()) {
            collection.create().get();
        }
    }

    @BeforeAll
    static void setup() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME, null).get();
    }

    @AfterEach
    void teardown() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void create() throws InterruptedException, ExecutionException {
        final CollectionEntity result = db.collection(COLLECTION_NAME + "_1").create().get();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        db.collection(COLLECTION_NAME + "_1").drop().get();
    }

    @Test
    void insertDocument() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null)
                .whenComplete((doc, ex) -> {
                    assertThat(ex).isNull();
                    assertThat(doc.getId()).isNotNull();
                    assertThat(doc.getKey()).isNotNull();
                    assertThat(doc.getRev()).isNotNull();
                    assertThat(doc.getNew()).isNull();
                    assertThat(doc.getId()).isEqualTo(COLLECTION_NAME + "/" + doc.getKey());
                })
                .get();
    }

    @Test
    void insertDocumentReturnNew() throws InterruptedException, ExecutionException {
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), options)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.getId()).isNotNull();
                    assertThat(doc.getKey()).isNotNull();
                    assertThat(doc.getRev()).isNotNull();
                    assertThat(doc.getNew()).isNotNull();
                })
                .get();
    }

    @Test
    void insertDocumentWaitForSync() throws InterruptedException, ExecutionException {
        final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), options)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.getId()).isNotNull();
                    assertThat(doc.getKey()).isNotNull();
                    assertThat(doc.getRev()).isNotNull();
                    assertThat(doc.getNew()).isNull();
                })
                .get();
    }

    @Test
    void insertDocumentAsJson() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null)
                .whenComplete((doc, ex) -> {
                    assertThat(doc).isNotNull();
                    assertThat(doc.getId()).isNotNull();
                    assertThat(doc.getKey()).isNotNull();
                    assertThat(doc.getRev()).isNotNull();
                })
                .get();
    }

    @Test
    void getDocument() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
                    assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
                })
                .get();
    }

    @Test
    void getDocumentIfMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
                    assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
                })
                .get();
    }

    @Test
    void getDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((doc, ex) -> assertThat(doc).isNull())
                .get();
    }

    @Test
    void getDocumentIfNoneMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
                    assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
                })
                .get();
    }

    @Test
    void getDocumentIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((doc, ex) -> assertThat(doc).isNull())
                .get();
    }

    @Test
    void getDocumentAsJson() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null).get();
        db.collection(COLLECTION_NAME).getDocument("docRaw", String.class, null)
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.contains("\"_key\":\"docRaw\"")).isEqualTo(true);
                    assertThat(readResult.contains("\"_id\":\"db_collection_test/docRaw\"")).isEqualTo(true);
                })
                .get();
    }

    @Test
    void getDocumentNotFound() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getDocument("no", BaseDocument.class)
                .whenComplete((doc, ex) -> assertThat(doc).isNull())
                .get();
    }

    @Test
    void getDocumentWrongKey() {
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).getDocument("no/no", BaseDocument.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @Test
    void getDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        db.collection(COLLECTION_NAME).insertDocuments(values).get();
        final MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME)
                .getDocuments(Arrays.asList("1", "2", "3"), BaseDocument.class).get();
        assertThat(documents).isNotNull();
        assertThat(documents.getDocuments()).hasSize(3);
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId()).isIn(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3");
        }
    }

    @Test
    void getDocumentsNotFound() throws InterruptedException, ExecutionException {
        final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
                .getDocuments(Collections.singleton("no"), BaseDocument.class).get();
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @Test
    void getDocumentsWrongKey() throws InterruptedException, ExecutionException {
        final MultiDocumentEntity<BaseDocument> readResult = db.collection(COLLECTION_NAME)
                .getDocuments(Collections.singleton("no/no"), BaseDocument.class).get();
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @Test
    void updateDocument() throws InterruptedException, ExecutionException {
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
            assertThat(updateResult).isNotNull();
            assertThat(updateResult.getId()).isEqualTo(createResult.getId());
            assertThat(updateResult.getNew()).isNull();
            assertThat(updateResult.getOld()).isNull();
            assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
            assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
        }).get();
        final DocumentUpdateEntity<BaseDocument> updateResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @Test
    void updateDocumentWithDifferentReturnType() throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        final String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("a", "test");
        collection.insertDocument(doc).get();

        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(key, Collections.singletonMap("b", "test"), new DocumentUpdateOptions().returnNew(true), BaseDocument.class).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(key);
        BaseDocument updated = updateResult.getNew();
        assertThat(updated).isNotNull();
        assertThat(updated.getAttribute("a")).isEqualTo("test");
        assertThat(updated.getAttribute("b")).isEqualTo("test");
    }

    @Test
    void updateDocumentIfMatch() throws InterruptedException, ExecutionException {
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
        assertThat(f).isNotNull();
        f.whenComplete((updateResult, ex) -> {
            assertThat(updateResult).isNotNull();
            assertThat(updateResult.getId()).isEqualTo(createResult.getId());
            assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
            assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
        }).get();
        final DocumentUpdateEntity<BaseDocument> updateResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @Test
    void updateDocumentIfMatchFail() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void updateDocumentReturnNew() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                    assertThat(updateResult.getNew()).isNotNull();
                    assertThat(updateResult.getNew().getKey()).isEqualTo(createResult.getKey());
                    assertThat(updateResult.getNew().getRevision()).isNotEqualTo(createResult.getRev());
                    assertThat(updateResult.getNew().getAttribute("a")).isNotNull();
                    assertThat(String.valueOf(updateResult.getNew().getAttribute("a"))).isEqualTo("test1");
                    assertThat(updateResult.getNew().getAttribute("b")).isNotNull();
                    assertThat(String.valueOf(updateResult.getNew().getAttribute("b"))).isEqualTo("test");
                })
                .get();
    }

    @Test
    void updateDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                    assertThat(updateResult.getOld()).isNotNull();
                    assertThat(updateResult.getOld().getKey()).isEqualTo(createResult.getKey());
                    assertThat(updateResult.getOld().getRevision()).isEqualTo(createResult.getRev());
                    assertThat(updateResult.getOld().getAttribute("a")).isNotNull();
                    assertThat(String.valueOf(updateResult.getOld().getAttribute("a"))).isEqualTo("test");
                    assertThat(updateResult.getOld().getProperties().keySet()).doesNotContain("b");
                })
                .get();
    }

    @Test
    void updateDocumentKeepNullTrue() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @Test
    void updateDocumentKeepNullFalse() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
        db.collection(COLLECTION_NAME).updateDocument(createResult.getKey(), doc, options)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateDocumentMergeObjectsTrue() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap).containsKey("a");
        assertThat(aMap).containsKey("b");
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateDocumentMergeObjectsFalse() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult).isNotNull();
                    assertThat(updateResult.getId()).isEqualTo(createResult.getId());
                    assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
                    assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
                })
                .get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet()).doesNotContain("a");
        assertThat(aMap).containsKey("b");
    }

    @Test
    void updateDocumentIgnoreRevsFalse() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void replaceDocument() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final CompletableFuture<DocumentUpdateEntity<BaseDocument>> f = db.collection(COLLECTION_NAME)
                .replaceDocument(createResult.getKey(), doc, null);
        f.whenComplete((replaceResult, ex) -> {
            assertThat(replaceResult).isNotNull();
            assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
            assertThat(replaceResult.getNew()).isNull();
            assertThat(replaceResult.getOld()).isNull();
            assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
            assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
        }).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @Test
    void replaceDocumentIfMatch() throws InterruptedException, ExecutionException {
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
            assertThat(replaceResult).isNotNull();
            assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
            assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
            assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
        }).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = f.get();

        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @Test
    void replaceDocumentIfMatchFail() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void replaceDocumentIgnoreRevsFalse() throws InterruptedException, ExecutionException {
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
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void replaceDocumentReturnNew() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
        db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options)
                .whenComplete((replaceResult, ex) -> {
                    assertThat(replaceResult).isNotNull();
                    assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
                    assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
                    assertThat(replaceResult.getNew()).isNotNull();
                    assertThat(replaceResult.getNew().getKey()).isEqualTo(createResult.getKey());
                    assertThat(replaceResult.getNew().getRevision()).isNotEqualTo(createResult.getRev());
                    assertThat(replaceResult.getNew().getProperties().keySet()).doesNotContain("a");
                    assertThat(replaceResult.getNew().getAttribute("b")).isNotNull();
                    assertThat(String.valueOf(replaceResult.getNew().getAttribute("b"))).isEqualTo("test");
                })
                .get();
    }

    @Test
    void replaceDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
        db.collection(COLLECTION_NAME).replaceDocument(createResult.getKey(), doc, options)
                .whenComplete((replaceResult, ex) -> {
                    assertThat(replaceResult).isNotNull();
                    assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
                    assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
                    assertThat(replaceResult.getOld()).isNotNull();
                    assertThat(replaceResult.getOld().getKey()).isEqualTo(createResult.getKey());
                    assertThat(replaceResult.getOld().getRevision()).isEqualTo(createResult.getRev());
                    assertThat(replaceResult.getOld().getAttribute("a")).isNotNull();
                    assertThat(String.valueOf(replaceResult.getOld().getAttribute("a"))).isEqualTo("test");
                    assertThat(replaceResult.getOld().getProperties().keySet()).doesNotContain("b");
                })
                .get();
    }

    @Test
    void deleteDocument() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, null).get();
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((document, ex) -> assertThat(document).isNull())
                .get();
    }

    @Test
    void deleteDocumentReturnOld() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), BaseDocument.class, options)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult.getOld()).isNotNull();
                    assertThat(deleteResult.getOld()).isInstanceOf(BaseDocument.class);
                    assertThat(deleteResult.getOld().getAttribute("a")).isNotNull();
                    assertThat(String.valueOf(deleteResult.getOld().getAttribute("a"))).isEqualTo("test");
                })
                .get();
    }

    @Test
    void deleteDocumentIfMatch() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options).get();
        db.collection(COLLECTION_NAME).getDocument(createResult.getKey(), BaseDocument.class, null)
                .whenComplete((document, ex) -> assertThat(document).isNull())
                .get();
    }

    @Test
    void deleteDocumentIfMatchFail() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = db.collection(COLLECTION_NAME).insertDocument(doc, null)
                .get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
        try {
            db.collection(COLLECTION_NAME).deleteDocument(createResult.getKey(), null, options).get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndex(createResult.getId())
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId()).isEqualTo(createResult.getId());
                    assertThat(readResult.getType()).isEqualTo(createResult.getType());
                })
                .get();
    }

    @Test
    void getIndexByKey() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndex(createResult.getId().split("/")[1])
                .whenComplete((readResult, ex) -> {
                    assertThat(readResult.getId()).isEqualTo(createResult.getId());
                    assertThat(readResult.getType()).isEqualTo(createResult.getType());
                })
                .get();
    }

    @Test
    void deleteIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("deleteIndexField");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId()).get();
        db.collection(COLLECTION_NAME).deleteIndex(createResult.getId())
                .whenComplete((id, ex) -> {
                    assertThat(id).isEqualTo(createResult.getId());
                    try {
                        db.getIndex(id).get();
                        fail();
                    } catch (final InterruptedException exception) {
                        exception.printStackTrace();
                        fail();
                    } catch (final ExecutionException exception) {
                        assertThat(exception.getCause()).isInstanceOf(ArangoDBException.class);
                    }
                })
                .get();
    }

    @Test
    void deleteIndexByKey() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("deleteIndexByKeyField");
        final IndexEntity createResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.getIndex(createResult.getId()).get();
        db.collection(COLLECTION_NAME).deleteIndex(createResult.getId().split("/")[1])
                .whenComplete((id, ex) -> {
                    assertThat(id).isEqualTo(createResult.getId());
                    try {
                        db.getIndex(id).get();
                        fail();
                    } catch (final InterruptedException exception) {
                        exception.printStackTrace();
                        fail();
                    } catch (final ExecutionException exception) {
                        assertThat(exception.getCause()).isInstanceOf(ArangoDBException.class);
                    }
                })
                .get();
    }

    @Test
    void createHashIndex() throws InterruptedException, ExecutionException {
        final boolean singleServer = isSingleServer();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureHashIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult).isNotNull();
                    assertThat(indexResult.getConstraint()).isNull();
                    assertThat(indexResult.getFields()).contains("a");
                    assertThat(indexResult.getFields()).contains("b");
                    assertThat(indexResult.getGeoJson()).isNull();
                    assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
                    assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
                    assertThat(indexResult.getMinLength()).isNull();
                    if (singleServer) {
                        assertThat(indexResult.getSelectivityEstimate()).isEqualTo(1.0);
                    }
                    assertThat(indexResult.getSparse()).isEqualTo(false);
                    assertThat(indexResult.getType()).isEqualTo(IndexType.hash);
                    assertThat(indexResult.getUnique()).isEqualTo(false);
                })
                .get();
    }

    @Test
    void createHashIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final HashIndexOptions options = new HashIndexOptions();
        options.name("myHashIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureHashIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        if (isSingleServer()) {
            assertThat(indexResult.getSelectivityEstimate()).isEqualTo(1.);
        }
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.hash);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo("myHashIndex");
    }

    @Test
    void createGeoIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult).isNotNull();
                    assertThat(indexResult.getFields()).contains("a");
                    assertThat(indexResult.getGeoJson()).isEqualTo(false);
                    assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
                    assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
                    assertThat(indexResult.getMinLength()).isNull();
                    assertThat(indexResult.getSelectivityEstimate()).isNull();
                    assertThat(indexResult.getSparse()).isEqualTo(true);
                    assertThat(indexResult.getType()).isIn(IndexType.geo, IndexType.geo1);
                    assertThat(indexResult.getUnique()).isEqualTo(false);
                })
                .get();
    }

    @Test
    void createGeoIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name("myGeoIndex1");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getUnique()).isFalse();
        if (isAtLeastVersion(3, 4)) {
            assertThat(indexResult.getType()).isEqualTo(IndexType.geo);
        } else {
            assertThat(indexResult.getType()).isEqualTo(IndexType.geo1);
        }
        assertThat(indexResult.getName()).isEqualTo("myGeoIndex1");
    }

    @Test
    void createGeo2Index() throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureGeoIndex(fields, null).whenComplete((indexResult, ex) -> {
            assertThat(indexResult).isNotNull();
            assertThat(indexResult.getFields()).contains("a");
            assertThat(indexResult.getFields()).contains("b");
            assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
            assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
            assertThat(indexResult.getMinLength()).isNull();
            assertThat(indexResult.getSparse()).isEqualTo(true);
            assertThat(indexResult.getUnique()).isEqualTo(false);
            try {
                if (isAtLeastVersion(3, 4)) {
                    assertThat(indexResult.getType()).isEqualTo(IndexType.geo);
                } else {
                    assertThat(indexResult.getType()).isEqualTo(IndexType.geo2);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
            }
        }).get();
    }

    @Test
    void createGeo2IndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name("myGeoIndex2");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureGeoIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getUnique()).isFalse();
        if (isAtLeastVersion(3, 4)) {
            assertThat(indexResult.getType()).isEqualTo(IndexType.geo);
        } else {
            assertThat(indexResult.getType()).isEqualTo(IndexType.geo2);
        }
        assertThat(indexResult.getName()).isEqualTo("myGeoIndex2");
    }

    @Test
    void createSkiplistIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensureSkiplistIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult).isNotNull();
                    assertThat(indexResult.getConstraint()).isNull();
                    assertThat(indexResult.getFields()).contains("a");
                    assertThat(indexResult.getFields()).contains("b");
                    assertThat(indexResult.getGeoJson()).isNull();
                    assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
                    assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
                    assertThat(indexResult.getMinLength()).isNull();
                    assertThat(indexResult.getSparse()).isEqualTo(false);
                    assertThat(indexResult.getType()).isEqualTo(IndexType.skiplist);
                    assertThat(indexResult.getUnique()).isEqualTo(false);
                })
                .get();
    }

    @Test
    void createSkiplistIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final SkiplistIndexOptions options = new SkiplistIndexOptions();
        options.name("mySkiplistIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureSkiplistIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.skiplist);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo("mySkiplistIndex");
    }

    @Test
    void createPersistentIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult).isNotNull();
                    assertThat(indexResult.getConstraint()).isNull();
                    assertThat(indexResult.getFields()).contains("a");
                    assertThat(indexResult.getFields()).contains("b");
                    assertThat(indexResult.getGeoJson()).isNull();
                    assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
                    assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
                    assertThat(indexResult.getMinLength()).isNull();
                    assertThat(indexResult.getSparse()).isEqualTo(false);
                    assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
                    assertThat(indexResult.getUnique()).isEqualTo(false);
                    assertThat(indexResult.getDeduplicate()).isTrue();
                })
                .get();
    }

    @Test
    void createPersistentIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name("myPersistentIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo("myPersistentIndex");
    }

    @Test
    void indexDeduplicate() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(true);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isTrue();
    }

    @Test
    void indexDeduplicateFalse() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(false);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isFalse();
    }

    @Test
    void createFulltextIndex() throws InterruptedException, ExecutionException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureFulltextIndex(fields, null)
                .whenComplete((indexResult, ex) -> {
                    assertThat(indexResult).isNotNull();
                    assertThat(indexResult.getConstraint()).isNull();
                    assertThat(indexResult.getFields()).contains("a");
                    assertThat(indexResult.getGeoJson()).isNull();
                    assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
                    assertThat(indexResult.getIsNewlyCreated()).isEqualTo(true);
                    assertThat(indexResult.getSelectivityEstimate()).isNull();
                    assertThat(indexResult.getSparse()).isEqualTo(true);
                    assertThat(indexResult.getType()).isEqualTo(IndexType.fulltext);
                    assertThat(indexResult.getUnique()).isEqualTo(false);
                })
                .get();
    }

    @Test
    void createFulltextIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final FulltextIndexOptions options = new FulltextIndexOptions();
        options.name("myFulltextIndex");

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureFulltextIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.fulltext);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo("myFulltextIndex");
    }

    @Test
    void createTtlIndexWithoutOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        try {
            db.collection(COLLECTION_NAME).ensureTtlIndex(fields, null).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(e.getCause().getMessage()).contains("expireAfter attribute must be a number");
            assertThat(((ArangoDBException) e.getCause()).getResponseCode()).isEqualTo(400);
            assertThat(((ArangoDBException) e.getCause()).getErrorNum()).isEqualTo(10);
        }
    }

    @Test
    void createTtlIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");

        final TtlIndexOptions options = new TtlIndexOptions();
        options.name("myTtlIndex");
        options.expireAfter(3600);

        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureTtlIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.ttl);
        assertThat(indexResult.getExpireAfter()).isEqualTo(3600);
        assertThat(indexResult.getName()).isEqualTo("myTtlIndex");
    }

    @Test
    void createZKDIndex() throws InterruptedException, ExecutionException {
        assumeTrue(isAtLeastVersion(3, 9));
        db.collection(COLLECTION_NAME).truncate().get();

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureZKDIndex(fields, null).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getGeoJson()).isNull();
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.zkd);
        assertThat(indexResult.getUnique()).isFalse();
        db.collection(COLLECTION_NAME).deleteIndex(indexResult.getId()).get();
    }

    @Test
    void createZKDIndexWithOptions() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 9));
        db.collection(COLLECTION_NAME).truncate().get();

        final ZKDIndexOptions options = new ZKDIndexOptions()
                .name("myZKDIndex")
                .fieldValueTypes(ZKDIndexOptions.FieldValueTypes.DOUBLE);

        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        fields.add("b");
        final IndexEntity indexResult = db.collection(COLLECTION_NAME).ensureZKDIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains("a");
        assertThat(indexResult.getFields()).contains("b");
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.zkd);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo("myZKDIndex");
        db.collection(COLLECTION_NAME).deleteIndex(indexResult.getId()).get();
    }

    @Test
    void getIndexes() throws InterruptedException, ExecutionException {
        final int initialIndexCount = db.collection(COLLECTION_NAME).getIndexes().get().size();
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        db.collection(COLLECTION_NAME).ensureHashIndex(fields, null).get();
        db.collection(COLLECTION_NAME).getIndexes()
                .whenComplete((indexes, ex) -> {
                    assertThat(indexes).isNotNull();
                    assertThat(indexes.size()).isEqualTo(initialIndexCount + 1);
                    for (final IndexEntity i : indexes) {
                        if (i.getType() == IndexType.hash) {
                            assertThat(i.getFields().size()).isEqualTo(1);
                            assertThat(i.getFields()).contains("a");
                        }
                    }
                })
                .get();
    }

    @Test
    void exists() throws InterruptedException, ExecutionException {
        assertThat(db.collection(COLLECTION_NAME).exists().get()).isTrue();
        assertThat(db.collection(COLLECTION_NAME + "no").exists().get()).isFalse();
    }

    @Test
    void truncate() throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument();
        db.collection(COLLECTION_NAME).insertDocument(doc, null).get();
        final BaseDocument readResult = db.collection(COLLECTION_NAME)
                .getDocument(doc.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(doc.getKey());
        db.collection(COLLECTION_NAME).truncate()
                .whenComplete((truncateResult, ex) -> {
                    assertThat(truncateResult).isNotNull();
                    assertThat(truncateResult.getId()).isNotNull();
                })
                .get();
        final BaseDocument document = db.collection(COLLECTION_NAME).getDocument(doc.getKey(), BaseDocument.class, null)
                .get();
        assertThat(document).isNull();
    }

    @Test
    void getCount() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).count()
                .whenComplete((countEmpty, ex) -> {
                    assertThat(countEmpty).isNotNull();
                    assertThat(countEmpty.getCount()).isEqualTo(0L);
                })
                .get();

        db.collection(COLLECTION_NAME).insertDocument("{}", null).get();

        db.collection(COLLECTION_NAME).count()
                .whenComplete((count, ex) -> assertThat(count.getCount()).isEqualTo(1L))
                .get();
    }

    @Test
    void documentExists() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).documentExists("no", null)
                .whenComplete((existsNot, ex) -> assertThat(existsNot).isEqualTo(false))
                .get();

        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();

        db.collection(COLLECTION_NAME).documentExists("abc", null)
                .whenComplete((exists, ex) -> assertThat(exists).isEqualTo(true))
                .get();
    }

    @Test
    void documentExistsIfMatch() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists).isEqualTo(true))
                .get();
    }

    @Test
    void documentExistsIfMatchFail() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists).isEqualTo(false))
                .get();
    }

    @Test
    void documentExistsIfNoneMatch() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists).isEqualTo(true))
                .get();
    }

    @Test
    void documentExistsIfNoneMatchFail() throws InterruptedException, ExecutionException {
        final DocumentCreateEntity<String> createResult = db.collection(COLLECTION_NAME)
                .insertDocument("{\"_key\":\"abc\"}", null).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
        db.collection(COLLECTION_NAME).documentExists("abc", options)
                .whenComplete((exists, ex) -> assertThat(exists).isEqualTo(false))
                .get();
    }

    @Test
    void insertDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getDocuments()).isNotNull();
                    assertThat(docs.getDocuments().size()).isEqualTo(3);
                    assertThat(docs.getErrors()).isNotNull();
                    assertThat(docs.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void insertDocumentsOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getDocuments()).isNotNull();
                    assertThat(docs.getDocuments().size()).isEqualTo(1);
                    assertThat(docs.getErrors()).isNotNull();
                    assertThat(docs.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void insertDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getDocuments()).isNotNull();
                    assertThat(docs.getDocuments().size()).isEqualTo(0);
                    assertThat(docs.getErrors()).isNotNull();
                    assertThat(docs.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void insertDocumentsReturnNew() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        db.collection(COLLECTION_NAME).insertDocuments(values, options)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getDocuments()).isNotNull();
                    assertThat(docs.getDocuments().size()).isEqualTo(3);
                    assertThat(docs.getErrors()).isNotNull();
                    assertThat(docs.getErrors().size()).isEqualTo(0);
                    for (final DocumentCreateEntity<BaseDocument> doc : docs.getDocuments()) {
                        assertThat(doc.getNew()).isNotNull();
                        final BaseDocument baseDocument = doc.getNew();
                        assertThat(baseDocument.getKey()).isNotNull();
                    }
                })
                .get();
    }

    @Test
    void insertDocumentsFail() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).insertDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getDocuments()).isNotNull();
                    assertThat(docs.getDocuments().size()).isEqualTo(2);
                    assertThat(docs.getErrors()).isNotNull();
                    assertThat(docs.getErrors().size()).isEqualTo(1);
                    assertThat(docs.getErrors().iterator().next().getErrorNum()).isEqualTo(1210);
                })
                .get();
    }

    @Test
    void importDocuments() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(values.size());
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsDuplicateDefaultError() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsDuplicateError() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsDuplicateIgnore() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(1);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsDuplicateReplace() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(1);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsDuplicateUpdate() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(1);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsCompleteFail() throws InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        try {
            db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true)).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getErrorNum()).isEqualTo(1210);
        }
    }

    @Test
    void importDocumentsDetails() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("2"));
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().details(true))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails().size()).isEqualTo(1);
                    assertThat(docs.getDetails().iterator().next()).contains("unique constraint violated");
                })
                .get();
    }

    @Test
    void importDocumentsOverwriteFalse() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount()).isEqualTo(1L);

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(3L);
    }

    @Test
    void importDocumentsOverwriteTrue() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount()).isEqualTo(1L);

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(2L);
    }

    @Test
    void importDocumentsFromToPrefix() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES)).get();
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME + "_edge");
        try {
            final Collection<BaseEdgeDocument> values = new ArrayList<>();
            final String[] keys = {"1", "2"};
            for (String s : keys) {
                values.add(new BaseEdgeDocument(s, "from", "to"));
            }
            assertThat(values).hasSize(keys.length);

            final DocumentImportEntity importResult = collection
                    .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar")).get();
            assertThat(importResult).isNotNull();
            assertThat(importResult.getCreated()).isEqualTo(values.size());
            for (String key : keys) {
                BaseEdgeDocument doc;
                doc = collection.getDocument(key, BaseEdgeDocument.class).get();
                assertThat(doc).isNotNull();
                assertThat(doc.getFrom()).isEqualTo("foo/from");
                assertThat(doc.getTo()).isEqualTo("bar/to");
            }
        } finally {
            collection.drop().get();
        }
    }

    @Test
    void importDocumentsJson() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonDuplicateDefaultError() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values)
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonDuplicateError() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonDuplicateIgnore() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(1);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonDuplicateReplace() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(1);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonDuplicateUpdate() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(0);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(1);
                    assertThat(docs.getDetails()).isEmpty();
                })
                .get();
    }

    @Test
    void importDocumentsJsonCompleteFail() throws InterruptedException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        try {
            db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().complete(true)).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
            assertThat(((ArangoDBException) e.getCause()).getErrorNum()).isEqualTo(1210);
        }
    }

    @Test
    void importDocumentsJsonDetails() throws InterruptedException, ExecutionException {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        db.collection(COLLECTION_NAME).importDocuments(values, new DocumentImportOptions().details(true))
                .whenComplete((docs, ex) -> {
                    assertThat(docs).isNotNull();
                    assertThat(docs.getCreated()).isEqualTo(2);
                    assertThat(docs.getEmpty()).isEqualTo(0);
                    assertThat(docs.getErrors()).isEqualTo(1);
                    assertThat(docs.getIgnored()).isEqualTo(0);
                    assertThat(docs.getUpdated()).isEqualTo(0);
                    assertThat(docs.getDetails().size()).isEqualTo(1);
                    assertThat(docs.getDetails().iterator().next()).contains("unique constraint violated");
                })
                .get();
    }

    @Test
    void importDocumentsJsonOverwriteFalse() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount()).isEqualTo(1L);

        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(3L);
    }

    @Test
    void importDocumentsJsonOverwriteTrue() throws InterruptedException, ExecutionException {
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument()).get();
        assertThat(collection.count().get().getCount()).isEqualTo(1L);

        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"}]";
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(2L);
    }

    @Test
    void importDocumentsJsonFromToPrefix() throws InterruptedException, ExecutionException {
        db.createCollection(COLLECTION_NAME + "_edge", new CollectionCreateOptions().type(CollectionType.EDGES)).get();
        final ArangoCollectionAsync collection = db.collection(COLLECTION_NAME + "_edge");
        try {
            final String[] keys = {"1", "2"};
            final String values = "[{\"_key\":\"1\",\"_from\":\"from\",\"_to\":\"to\"},{\"_key\":\"2\",\"_from\":\"from\",\"_to\":\"to\"}]";

            final DocumentImportEntity importResult = collection
                    .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar")).get();
            assertThat(importResult).isNotNull();
            assertThat(importResult.getCreated()).isEqualTo(2);
            for (String key : keys) {
                BaseEdgeDocument doc;
                doc = collection.getDocument(key, BaseEdgeDocument.class).get();
                assertThat(doc).isNotNull();
                assertThat(doc.getFrom()).isEqualTo("foo/from");
                assertThat(doc.getTo()).isEqualTo("bar/to");
            }
        } finally {
            collection.drop().get();
        }
    }

    @Test
    void deleteDocumentsByKey() throws InterruptedException, ExecutionException {
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
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(2);
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey()).isIn("1", "2");
                    }
                    assertThat(deleteResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void deleteDocumentsByDocuments() throws InterruptedException, ExecutionException {
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
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(2);
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey()).isIn("1", "2");
                    }
                    assertThat(deleteResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void deleteDocumentsByKeyOne() throws InterruptedException, ExecutionException {
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
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(1);
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey()).isEqualTo("1");
                    }
                    assertThat(deleteResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void deleteDocumentsByDocumentOne() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        db.collection(COLLECTION_NAME).deleteDocuments(values, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(1);
                    for (final DocumentDeleteEntity<Object> i : deleteResult.getDocuments()) {
                        assertThat(i.getKey()).isEqualTo("1");
                    }
                    assertThat(deleteResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void deleteDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(0);
                    assertThat(deleteResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void deleteDocumentsByKeyNotExisting() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).insertDocuments(values, null).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        db.collection(COLLECTION_NAME).deleteDocuments(keys, null, null)
                .whenComplete((deleteResult, ex) -> {
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(0);
                    assertThat(deleteResult.getErrors().size()).isEqualTo(2);
                })
                .get();
    }

    @Test
    void deleteDocumentsByDocumentsNotExisting() throws InterruptedException, ExecutionException {
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
                    assertThat(deleteResult).isNotNull();
                    assertThat(deleteResult.getDocuments().size()).isEqualTo(0);
                    assertThat(deleteResult.getErrors().size()).isEqualTo(2);
                })
                .get();
    }

    @Test
    void updateDocuments() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(2);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void updateDocumentsWithDifferentReturnType() throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        List<String> keys = IntStream.range(0, 3).mapToObj(it -> "key-" + UUID.randomUUID()).collect(Collectors.toList());
        List<BaseDocument> docs = keys.stream()
                .map(BaseDocument::new)
                .peek(it -> it.addAttribute("a", "test"))
                .collect(Collectors.toList());

        collection.insertDocuments(docs).get();

        List<Map<String, Object>> modifiedDocs = docs.stream()
                .peek(it -> it.addAttribute("b", "test"))
                .map(it -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("_key", it.getKey());
                    map.put("a", it.getAttribute("a"));
                    map.put("b", it.getAttribute("b"));
                    return map;
                })
                .collect(Collectors.toList());

        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(modifiedDocs, new DocumentUpdateOptions().returnNew(true), BaseDocument.class).get();
        assertThat(updateResult.getDocuments()).hasSize(3);
        assertThat(updateResult.getErrors()).isEmpty();
        assertThat(updateResult.getDocuments().stream().map(DocumentUpdateEntity::getNew)
                .allMatch(it -> it.getAttribute("a").equals("test") && it.getAttribute("b").equals("test")))
                .isTrue();
    }

    @Test
    void updateDocumentsOne() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(1);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void updateDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).updateDocuments(values, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size()).isEqualTo(0);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void updateDocumentsWithoutKey() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(1);
                    assertThat(updateResult.getErrors().size()).isEqualTo(1);
                })
                .get();
    }

    @Test
    void replaceDocuments() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(2);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void replaceDocumentsOne() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(1);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void replaceDocumentsEmpty() throws InterruptedException, ExecutionException {
        final Collection<BaseDocument> values = new ArrayList<>();
        db.collection(COLLECTION_NAME).updateDocuments(values, null)
                .whenComplete((updateResult, ex) -> {
                    assertThat(updateResult.getDocuments().size()).isEqualTo(0);
                    assertThat(updateResult.getErrors().size()).isEqualTo(0);
                })
                .get();
    }

    @Test
    void replaceDocumentsWithoutKey() throws InterruptedException, ExecutionException {
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
                    assertThat(updateResult.getDocuments().size()).isEqualTo(1);
                    assertThat(updateResult.getErrors().size()).isEqualTo(1);
                })
                .get();
    }

    @Test
    void load() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).load()
                .whenComplete((result, ex) -> assertThat(result.getName()).isEqualTo(COLLECTION_NAME))
                .get();
    }

    @Test
    void unload() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).unload()
                .whenComplete((result, ex) -> assertThat(result.getName()).isEqualTo(COLLECTION_NAME))
                .get();
    }

    @Test
    void getInfo() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getInfo()
                .whenComplete((result, ex) -> assertThat(result.getName()).isEqualTo(COLLECTION_NAME))
                .get();
    }

    @Test
    void getPropeties() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getProperties()
                .whenComplete((result, ex) -> {
                    assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
                    assertThat(result.getCount()).isNull();
                })
                .get();
    }

    @Test
    void changeProperties() throws InterruptedException, ExecutionException {
        final String collection = COLLECTION_NAME + "_prop";
        try {
            db.createCollection(collection).get();
            final CollectionPropertiesEntity properties = db.collection(collection).getProperties().get();
            assertThat(properties.getWaitForSync()).isNotNull();
            final CollectionPropertiesOptions options = new CollectionPropertiesOptions();
            options.waitForSync(!properties.getWaitForSync());
            options.journalSize(2000000L);
            db.collection(collection).changeProperties(options)
                    .whenComplete((changedProperties, ex) -> {
                        assertThat(changedProperties.getWaitForSync()).isNotNull();
                        assertThat(changedProperties.getWaitForSync()).isNotEqualTo(properties.getWaitForSync());
                    })
                    .get();
        } finally {
            db.collection(collection).drop().get();
        }
    }

    @Test
    void rename() throws InterruptedException, ExecutionException {
        assumeTrue(isSingleServer());
        db.collection(COLLECTION_NAME).rename(COLLECTION_NAME + "1")
                .whenComplete((result, ex) -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getName()).isEqualTo(COLLECTION_NAME + "1");
                })
                .get();
        final CollectionEntity info = db.collection(COLLECTION_NAME + "1").getInfo().get();
        assertThat(info.getName()).isEqualTo(COLLECTION_NAME + "1");
        try {
            db.collection(COLLECTION_NAME).getInfo().get();
            fail();
        } catch (final ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
        db.collection(COLLECTION_NAME + "1").rename(COLLECTION_NAME).get();
    }

    @Test
    void responsibleShard() throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 5));
        ShardEntity shard = db.collection(COLLECTION_NAME).getResponsibleShard(new BaseDocument("testKey")).get();
        assertThat(shard).isNotNull();
        assertThat(shard.getShardId()).isNotNull();
    }

    @Test
    void getRevision() throws InterruptedException, ExecutionException {
        db.collection(COLLECTION_NAME).getRevision()
                .whenComplete((result, ex) -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
                    assertThat(result.getRevision()).isNotNull();
                })
                .get();
    }

    @Test
    void grantAccessRW() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessRO() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RO).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessNONE() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void grantAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.RW).get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void revokeAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void revokeAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).grantAccess("user1", Permissions.NONE).get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void resetAccess() throws InterruptedException, ExecutionException {
        try {
            arangoDB.createUser("user1", "1234", null).get();
            db.collection(COLLECTION_NAME).resetAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @Test
    void resetAccessUserNotFound() {
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).resetAccess("user1").get());
        assertThat(thrown).isInstanceOf(ExecutionException.class);
    }

    @Test
    void getPermissions() throws InterruptedException, ExecutionException {
        assertThat(db.collection(COLLECTION_NAME).getPermissions("root").get()).isEqualTo(Permissions.RW);
    }
}

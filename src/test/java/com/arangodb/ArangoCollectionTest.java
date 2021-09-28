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

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionRevisionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.DocumentImportEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.ShardEntity;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.CollectionSchema;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentImportOptions;
import com.arangodb.model.DocumentImportOptions.OnDuplicate;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.OverwriteMode;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.arangodb.model.TtlIndexOptions;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Mark Vollmary
 */
@RunWith(Parameterized.class)
public class ArangoCollectionTest extends BaseTest {

    private static final String COLLECTION_NAME = "ArangoCollectionTest_collection";
    private static final String EDGE_COLLECTION_NAME = "ArangoCollectionTest_edge_collection";

    private final ArangoCollection collection;
    private final ArangoCollection edgeCollection;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLLECTION_NAME);
        BaseTest.initEdgeCollections(EDGE_COLLECTION_NAME);
    }

    public ArangoCollectionTest(final ArangoDB arangoDB) {
        super(arangoDB);
        collection = db.collection(COLLECTION_NAME);
        edgeCollection = db.collection(EDGE_COLLECTION_NAME);
    }

    @Test
    public void insertDocument() {
        final DocumentCreateEntity<BaseDocument> doc = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(notNullValue()));
        assertThat(doc.getKey(), is(notNullValue()));
        assertThat(doc.getRev(), is(notNullValue()));
        assertThat(doc.getNew(), is(nullValue()));
        assertThat(doc.getId(), is(COLLECTION_NAME + "/" + doc.getKey()));
    }

    // FIXME: v7
    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void insertDocumentWithArrayWithNullValues() {
        List<String> arr = Arrays.asList("a", null);
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("arr", arr);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection
                .insertDocument(doc, new DocumentCreateOptions().returnNew(true));
        assertThat(insertedDoc, is(notNullValue()));
        assertThat(insertedDoc.getId(), is(notNullValue()));
        assertThat(insertedDoc.getKey(), is(notNullValue()));
        assertThat(insertedDoc.getRev(), is(notNullValue()));
        assertThat(insertedDoc.getId(), is(COLLECTION_NAME + "/" + insertedDoc.getKey()));
        //noinspection unchecked
        assertThat(((List<String>) insertedDoc.getNew().getAttribute("arr")), contains("a", null));
    }

    // FIXME: v7
    @Test
    @Ignore
    public void insertDocumentWithNullValues() {
        BaseDocument doc = new BaseDocument();
        doc.addAttribute("null", null);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection
                .insertDocument(doc, new DocumentCreateOptions().returnNew(true));
        assertThat(insertedDoc, is(notNullValue()));
        assertThat(insertedDoc.getId(), is(notNullValue()));
        assertThat(insertedDoc.getKey(), is(notNullValue()));
        assertThat(insertedDoc.getRev(), is(notNullValue()));
        assertThat(insertedDoc.getId(), is(COLLECTION_NAME + "/" + insertedDoc.getKey()));
        assertThat(insertedDoc.getNew().getProperties().containsKey("null"), is(true));
    }

    @Test
    public void insertDocumentUpdateRev() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
    }

    @Test
    public void insertDocumentReturnNew() {
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        final DocumentCreateEntity<BaseDocument> doc = collection
                .insertDocument(new BaseDocument(), options);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(notNullValue()));
        assertThat(doc.getKey(), is(notNullValue()));
        assertThat(doc.getRev(), is(notNullValue()));
        assertThat(doc.getNew(), is(notNullValue()));
    }

    @Test
    public void insertDocumentOverwriteReturnOld() {
        assumeTrue(isAtLeastVersion(3, 4));
        Long initialCount = collection.count().getCount();

        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("value", "a");
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        doc.addAttribute("value", "b");
        final DocumentCreateEntity<BaseDocument> repsert = collection
                .insertDocument(doc, new DocumentCreateOptions().overwrite(true).returnOld(true).returnNew(true));

        assertThat(repsert, is(notNullValue()));
        assertThat(repsert.getRev(), is(not(meta.getRev())));
        assertThat(repsert.getOld().getAttribute("value").toString(), is("a"));
        assertThat(repsert.getNew().getAttribute("value").toString(), is("b"));
        assertThat(collection.count().getCount(), is(initialCount + 1L));
    }

    @Test
    public void insertDocumentOverwriteModeIgnore() {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID().toString();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> insertIgnore = collection
                .insertDocument(doc2, new DocumentCreateOptions().overwriteMode(OverwriteMode.ignore));

        assertThat(insertIgnore, is(notNullValue()));
        assertThat(insertIgnore.getRev(), is(meta.getRev()));
    }

    @Test
    public void insertDocumentOverwriteModeConflict() {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID().toString();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        try {
            collection.insertDocument(doc2, new DocumentCreateOptions().overwriteMode(OverwriteMode.conflict));
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(409));
            assertThat(e.getErrorNum(), is(1210));
        }
    }

    @Test
    public void insertDocumentOverwriteModeReplace() {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID().toString();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> repsert = collection
                .insertDocument(doc2, new DocumentCreateOptions().overwriteMode(OverwriteMode.replace).returnNew(true));

        assertThat(repsert, is(notNullValue()));
        assertThat(repsert.getRev(), is(not(meta.getRev())));
        assertThat(repsert.getNew().getProperties().containsKey("foo"), is(false));
        assertThat(repsert.getNew().getAttribute("bar").toString(), is("b"));
    }

    @Test
    public void insertDocumentOverwriteModeUpdate() {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        doc.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> updated = collection
                .insertDocument(doc, new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true));

        assertThat(updated, is(notNullValue()));
        assertThat(updated.getRev(), is(not(meta.getRev())));
        assertThat(updated.getNew().getAttribute("foo").toString(), is("a"));
        assertThat(updated.getNew().getAttribute("bar").toString(), is("b"));
    }

    @Test
    public void insertDocumentOverwriteModeUpdateMergeObjectsFalse() {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument();
        Map<String, String> fieldA = Collections.singletonMap("a", "a");
        doc.addAttribute("foo", fieldA);
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc);

        Map<String, String> fieldB = Collections.singletonMap("b", "b");
        doc.addAttribute("foo", fieldB);
        final DocumentCreateEntity<BaseDocument> updated = collection
                .insertDocument(doc, new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.update)
                        .mergeObjects(false)
                        .returnNew(true));

        assertThat(updated, is(notNullValue()));
        assertThat(updated.getRev(), is(not(meta.getRev())));
        assertThat(updated.getNew().getAttribute("foo"), is(fieldB));
    }

    @Test
    public void insertDocumentWaitForSync() {
        final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
        final DocumentCreateEntity<BaseDocument> doc = collection
                .insertDocument(new BaseDocument(), options);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(notNullValue()));
        assertThat(doc.getKey(), is(notNullValue()));
        assertThat(doc.getRev(), is(notNullValue()));
        assertThat(doc.getNew(), is(nullValue()));
    }

    @Test
    public void insertDocumentAsJson() {
        final DocumentCreateEntity<String> doc = collection
                .insertDocument("{\"_key\":\"docRaw\",\"a\":\"test\"}", null);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getId(), is(notNullValue()));
        assertThat(doc.getKey(), is(notNullValue()));
        assertThat(doc.getRev(), is(notNullValue()));
    }

    @Test
    public void insertDocumentSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> meta = collection
                .insertDocument(new BaseDocument(), new DocumentCreateOptions().silent(true));
        assertThat(meta, is(notNullValue()));
        assertThat(meta.getId(), is(nullValue()));
        assertThat(meta.getKey(), is(nullValue()));
        assertThat(meta.getRev(), is(nullValue()));
    }

    @Test
    public void insertDocumentSilentDontTouchInstance() {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument();
        final String key = "testkey";
        doc.setKey(key);
        final DocumentCreateEntity<BaseDocument> meta = collection
                .insertDocument(doc, new DocumentCreateOptions().silent(true));
        assertThat(meta, is(notNullValue()));
        assertThat(meta.getKey(), is(nullValue()));
        assertThat(doc.getKey(), is(key));
    }

    @Test
    public void insertDocumentsSilent() {
        assumeTrue(isSingleServer());
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> info = collection
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().silent(true));
        assertThat(info, is(notNullValue()));
        assertThat(info.getDocuments().isEmpty(), is(true));
        assertThat(info.getDocumentsAndErrors().isEmpty(), is(true));
        assertThat(info.getErrors().isEmpty(), is(true));
    }

    @Test
    public void getDocument() {
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey(), is(notNullValue()));
        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
    }

    @Test
    public void getDocumentIfMatch() {
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
    }

    @Test
    public void getDocumentIfMatchFail() {
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        final BaseDocument document = collection
                .getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getDocumentIfNoneMatch() {
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getId(), is(COLLECTION_NAME + "/" + createResult.getKey()));
    }

    @Test
    public void getDocumentIfNoneMatchFail() {
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey(), is(notNullValue()));
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
        final BaseDocument document = collection
                .getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getDocumentAsJson() {
        String key = rnd();
        collection.insertDocument("{\"_key\":\"" + key + "\",\"a\":\"test\"}", null);
        final String readResult = collection.getDocument(key, String.class, null);
        assertThat(readResult, containsString("\"_key\":\"" + key + "\""));
        assertThat(readResult, containsString("\"_id\":\"" + COLLECTION_NAME + "/" + key + "\""));
    }

    @Test
    public void getDocumentNotFound() {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class);
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getDocumentNotFoundOptionsDefault() {
        final BaseDocument document = collection
                .getDocument("no", BaseDocument.class, new DocumentReadOptions());
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getDocumentNotFoundOptionsNull() {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class, null);
        assertThat(document, is(nullValue()));
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentNotFoundThrowException() {
        collection
                .getDocument("no", BaseDocument.class, new DocumentReadOptions().catchException(false));
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWrongKey() {
        collection.getDocument("no/no", BaseDocument.class);
    }

    @Test
    public void getDocumentDirtyRead() throws InterruptedException {
        final BaseDocument doc = new BaseDocument();
        collection.insertDocument(doc, new DocumentCreateOptions());
        Thread.sleep(2000);
        final VPackSlice document = collection
                .getDocument(doc.getKey(), VPackSlice.class, new DocumentReadOptions().allowDirtyRead(true));
        assertThat(document, is(notNullValue()));
    }

    @Test
    public void getDocuments() {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values);
        final MultiDocumentEntity<BaseDocument> documents = collection
                .getDocuments(Arrays.asList("1", "2", "3"), BaseDocument.class);
        assertThat(documents, is(notNullValue()));
        assertThat(documents.getDocuments().size(), is(3));
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId(),
                    isOneOf(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3"));
        }
    }

    @Test
    public void getDocumentsWithCustomShardingKey() {
        ArangoCollection collection = db.collection("customShardingKeyCollection");
        if (collection.exists())
            collection.drop();

        collection.create(new CollectionCreateOptions()
                .shardKeys("customField")
                .numberOfShards(10)
        );

        List<BaseDocument> values = IntStream.range(0, 10)
                .mapToObj(String::valueOf).map(key -> new BaseDocument())
                .peek(it -> it.addAttribute("customField", rnd()))
                .collect(Collectors.toList());

        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> inserted = collection.insertDocuments(values);
        List<String> insertedKeys = inserted.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        final Collection<BaseDocument> documents = collection
                .getDocuments(insertedKeys, BaseDocument.class).getDocuments();

        assertThat(documents.size(), is(10));
    }

    @Test
    public void getDocumentsDirtyRead() {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values);
        final MultiDocumentEntity<BaseDocument> documents = collection
                .getDocuments(Arrays.asList("1", "2", "3"), BaseDocument.class,
                        new DocumentReadOptions().allowDirtyRead(true));
        assertThat(documents, is(notNullValue()));
        assertThat(documents.getDocuments().size(), is(3));
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId(),
                    isOneOf(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2", COLLECTION_NAME + "/" + "3"));
        }
    }

    @Test
    public void getDocumentsNotFound() {
        final MultiDocumentEntity<BaseDocument> readResult = collection
                .getDocuments(Collections.singleton("no"), BaseDocument.class);
        assertThat(readResult, is(notNullValue()));
        assertThat(readResult.getDocuments().size(), is(0));
        assertThat(readResult.getErrors().size(), is(1));
    }

    @Test
    public void getDocumentsWrongKey() {
        final MultiDocumentEntity<BaseDocument> readResult = collection
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
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, null);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getNew(), is(nullValue()));
        assertThat(updateResult.getOld(), is(nullValue()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test
    public void updateDocumentWithDifferentReturnType() {
        final String key = "key-" + UUID.randomUUID().toString();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("a", "test");
        collection.insertDocument(doc);

        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(key, Collections.singletonMap("b", "test"), new DocumentUpdateOptions().returnNew(true), BaseDocument.class);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getKey(), is(key));
        BaseDocument updated = updateResult.getNew();
        assertThat(updated, is(notNullValue()));
        assertThat(updated.getAttribute("a"), is("test"));
        assertThat(updated.getAttribute("b"), is("test"));
    }

    @Test
    public void updateDocumentUpdateRev() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(updateResult.getRev()));
    }

    @Test
    public void updateDocumentIfMatch() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("a")), is("test1"));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
        assertThat(readResult.getRevision(), is(updateResult.getRev()));
        assertThat(readResult.getProperties().keySet(), hasItem("c"));
    }

    @Test(expected = ArangoDBException.class)
    public void updateDocumentIfMatchFail() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
        collection.updateDocument(createResult.getKey(), doc, options);
    }

    @Test
    public void updateDocumentReturnNew() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
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
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
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
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
    }

    @Test
    public void updateDocumentKeepNullFalse() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
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
        final DocumentCreateEntity<TestUpdateEntity> createResult = collection.insertDocument(doc);
        final TestUpdateEntity patchDoc = new TestUpdateEntity();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<TestUpdateEntity> updateResult = collection
                .updateDocument(createResult.getKey(), patchDoc);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getKey(), is(createResult.getKey()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet(), hasItem("a"));
        assertThat(readResult.getAttribute("a").toString(), is("bar"));
    }

    @Test
    public void updateDocumentSerializeNullFalse() {
        final TestUpdateEntity doc = new TestUpdateEntity();
        doc.a = "foo";
        doc.b = "foo";
        final DocumentCreateEntity<TestUpdateEntity> createResult = collection.insertDocument(doc);
        final TestUpdateEntity patchDoc = new TestUpdateEntity();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<TestUpdateEntity> updateResult = collection
                .updateDocument(createResult.getKey(), patchDoc, new DocumentUpdateOptions().serializeNull(false));
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getKey(), is(createResult.getKey()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getProperties().keySet(), hasItems("a", "b"));
        assertThat(readResult.getAttribute("a").toString(), is("bar"));
        assertThat(readResult.getAttribute("b").toString(), is("foo"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateDocumentMergeObjectsTrue() {
        final BaseDocument doc = new BaseDocument();
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
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
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection
                .updateDocument(createResult.getKey(), doc, options);
        assertThat(updateResult, is(notNullValue()));
        assertThat(updateResult.getId(), is(createResult.getId()));
        assertThat(updateResult.getRev(), is(not(updateResult.getOldRev())));
        assertThat(updateResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult, instanceOf(Map.class));
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet(), not(hasItem("a")));
        assertThat(aMap.keySet(), hasItem("b"));
    }

    @Test(expected = ArangoDBException.class)
    public void updateDocumentIgnoreRevsFalse() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.setRevision("no");

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
        collection.updateDocument(createResult.getKey(), doc, options);
    }

    @Test
    public void updateDocumentSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final DocumentUpdateEntity<BaseDocument> meta = collection
                .updateDocument(createResult.getKey(), new BaseDocument(), new DocumentUpdateOptions().silent(true));
        assertThat(meta, is(notNullValue()));
        assertThat(meta.getId(), is(nullValue()));
        assertThat(meta.getKey(), is(nullValue()));
        assertThat(meta.getRev(), is(nullValue()));
    }

    @Test
    public void updateDocumentsSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info = collection
                .updateDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentUpdateOptions().silent(true));
        assertThat(info, is(notNullValue()));
        assertThat(info.getDocuments().isEmpty(), is(true));
        assertThat(info.getDocumentsAndErrors().isEmpty(), is(true));
        assertThat(info.getErrors().isEmpty(), is(true));
    }

    @Test
    public void updateNonExistingDocument() {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");

        try {
            collection.updateDocument(doc.getKey(), doc, null);
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
            assertThat(e.getErrorNum(), is(1202));
        }
    }

    @Test
    public void updateDocumentPreconditionFailed() {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);

        doc.updateAttribute("foo", "b");
        collection.updateDocument(doc.getKey(), doc, null);

        doc.updateAttribute("foo", "c");
        try {
            collection.updateDocument(doc.getKey(), doc, new DocumentUpdateOptions().ifMatch(createResult.getRev()));
            fail();
        } catch (ArangoDBException e) {
            assertThat(e.getResponseCode(), is(412));
            assertThat(e.getErrorNum(), is(1200));
        }
        BaseDocument readDocument = collection.getDocument(doc.getKey(), BaseDocument.class);
        assertThat(readDocument.getAttribute("foo"), is("b"));
    }

    @Test
    public void replaceDocument() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection
                .replaceDocument(createResult.getKey(), doc, null);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getNew(), is(nullValue()));
        assertThat(replaceResult.getOld(), is(nullValue()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test
    public void replaceDocumentUpdateRev() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        assertThat(doc.getRevision(), is(createResult.getRev()));
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection
                .replaceDocument(createResult.getKey(), doc, null);
        assertThat(doc.getRevision(), is(replaceResult.getRev()));
    }

    @Test
    public void replaceDocumentIfMatch() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection
                .replaceDocument(createResult.getKey(), doc, options);
        assertThat(replaceResult, is(notNullValue()));
        assertThat(replaceResult.getId(), is(createResult.getId()));
        assertThat(replaceResult.getRev(), is(not(replaceResult.getOldRev())));
        assertThat(replaceResult.getOldRev(), is(createResult.getRev()));

        final BaseDocument readResult = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(createResult.getKey()));
        assertThat(readResult.getRevision(), is(replaceResult.getRev()));
        assertThat(readResult.getProperties().keySet(), not(hasItem("a")));
        assertThat(readResult.getAttribute("b"), is(notNullValue()));
        assertThat(String.valueOf(readResult.getAttribute("b")), is("test"));
    }

    @Test(expected = ArangoDBException.class)
    public void replaceDocumentIfMatchFail() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
        collection.replaceDocument(createResult.getKey(), doc, options);
    }

    @Test(expected = ArangoDBException.class)
    public void replaceDocumentIgnoreRevsFalse() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        doc.setRevision("no");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
        collection.replaceDocument(createResult.getKey(), doc, options);
    }

    @Test
    public void replaceDocumentReturnNew() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection
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
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        doc.getProperties().clear();
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection
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
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final DocumentUpdateEntity<BaseDocument> meta = collection
                .replaceDocument(createResult.getKey(), new BaseDocument(), new DocumentReplaceOptions().silent(true));
        assertThat(meta, is(notNullValue()));
        assertThat(meta.getId(), is(nullValue()));
        assertThat(meta.getKey(), is(nullValue()));
        assertThat(meta.getRev(), is(nullValue()));
    }

    @Test
    public void replaceDocumentSilentDontTouchInstance() {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc);
        final String revision = doc.getRevision();
        assertThat(revision, is(notNullValue()));
        final DocumentUpdateEntity<BaseDocument> meta = collection
                .replaceDocument(createResult.getKey(), doc, new DocumentReplaceOptions().silent(true));
        assertThat(meta.getRev(), is(nullValue()));
        assertThat(doc.getRevision(), is(revision));
    }

    @Test
    public void replaceDocumentsSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info = collection
                .replaceDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentReplaceOptions().silent(true));
        assertThat(info, is(notNullValue()));
        assertThat(info.getDocuments().isEmpty(), is(true));
        assertThat(info.getDocumentsAndErrors().isEmpty(), is(true));
        assertThat(info.getErrors().isEmpty(), is(true));
    }

    @Test
    public void deleteDocument() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        collection.deleteDocument(createResult.getKey(), null, null);
        final BaseDocument document = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(document, is(nullValue()));
    }

    @Test
    public void deleteDocumentReturnOld() {
        final BaseDocument doc = new BaseDocument();
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
        final DocumentDeleteEntity<BaseDocument> deleteResult = collection
                .deleteDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(deleteResult.getOld(), is(notNullValue()));
        assertThat(deleteResult.getOld(), instanceOf(BaseDocument.class));
        assertThat(deleteResult.getOld().getAttribute("a"), is(notNullValue()));
        assertThat(String.valueOf(deleteResult.getOld().getAttribute("a")), is("test"));
    }

    @Test
    public void deleteDocumentIfMatch() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
        collection.deleteDocument(createResult.getKey(), null, options);
        final BaseDocument document = collection
                .getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(document, is(nullValue()));
    }

    @Test(expected = ArangoDBException.class)
    public void deleteDocumentIfMatchFail() {
        final BaseDocument doc = new BaseDocument();
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(doc, null);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
        collection.deleteDocument(createResult.getKey(), null, options);
    }

    @Test
    public void deleteDocumentSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final DocumentDeleteEntity<BaseDocument> meta = collection
                .deleteDocument(createResult.getKey(), BaseDocument.class, new DocumentDeleteOptions().silent(true));
        assertThat(meta, is(notNullValue()));
        assertThat(meta.getId(), is(nullValue()));
        assertThat(meta.getKey(), is(nullValue()));
        assertThat(meta.getRev(), is(nullValue()));
    }

    @Test
    public void deleteDocumentsSilent() {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> createResult = collection
                .insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentDeleteEntity<BaseDocument>> info = collection
                .deleteDocuments(Collections.singletonList(createResult.getKey()), BaseDocument.class,
                        new DocumentDeleteOptions().silent(true));
        assertThat(info, is(notNullValue()));
        assertThat(info.getDocuments().isEmpty(), is(true));
        assertThat(info.getDocumentsAndErrors().isEmpty(), is(true));
        assertThat(info.getErrors().isEmpty(), is(true));
    }

    @Test
    public void getIndex() {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensureHashIndex(fields, null);
        final IndexEntity readResult = collection.getIndex(createResult.getId());
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getType(), is(createResult.getType()));
    }

    @Test
    public void getIndexByKey() {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensureHashIndex(fields, null);
        final IndexEntity readResult = collection.getIndex(createResult.getId().split("/")[1]);
        assertThat(readResult.getId(), is(createResult.getId()));
        assertThat(readResult.getType(), is(createResult.getType()));
    }

    @Test(expected = ArangoDBException.class)
    public void deleteIndex() {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensureHashIndex(fields, null);
        final String id = collection.deleteIndex(createResult.getId());
        assertThat(id, is(createResult.getId()));
        db.getIndex(id);
    }

    @Test(expected = ArangoDBException.class)
    public void deleteIndexByKey() {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensureHashIndex(fields, null);
        final String id = collection.deleteIndex(createResult.getId().split("/")[1]);
        assertThat(id, is(createResult.getId()));
        db.getIndex(id);
    }

    @Test
    public void createHashIndex() {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensureHashIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        if (isSingleServer()) {
            assertThat(indexResult.getSelectivityEstimate(), is(greaterThan(0.0)));
        }
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.hash));
        assertThat(indexResult.getUnique(), is(false));
    }

    @Test
    public void createHashIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "hashIndex-" + rnd();
        final HashIndexOptions options = new HashIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureHashIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        if (isSingleServer()) {
            assertThat(indexResult.getSelectivityEstimate(), is(greaterThan(0.0)));
        }
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.hash));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void createGeoIndex() {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
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
    }

    @Test
    public void createGeoIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
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
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void createGeo2Index() {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
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
    }

    @Test
    public void createGeo2IndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
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
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void createSkiplistIndex() {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensureSkiplistIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.skiplist));
        assertThat(indexResult.getUnique(), is(false));
    }

    @Test
    public void createSkiplistIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "skiplistIndex-" + rnd();
        final SkiplistIndexOptions options = new SkiplistIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensureSkiplistIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.skiplist));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void createPersistentIndex() {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.persistent));
        assertThat(indexResult.getUnique(), is(false));
    }

    @Test
    public void createPersistentIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getFields(), hasItem(f2));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getMinLength(), is(nullValue()));
        assertThat(indexResult.getSparse(), is(false));
        assertThat(indexResult.getType(), is(IndexType.persistent));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void indexEstimates() {
        assumeTrue(isAtLeastVersion(3, 8));
        assumeTrue(isSingleServer());

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.estimates(true);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getEstimates(), is(true));
        assertThat(indexResult.getSelectivityEstimate(), is(notNullValue()));
    }

    @Test
    public void indexEstimatesFalse() {
        assumeTrue(isAtLeastVersion(3, 8));
        assumeTrue(isSingleServer());

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.estimates(false);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getEstimates(), is(false));
        assertThat(indexResult.getSelectivityEstimate(), is(nullValue()));
    }

    @Test
    public void createFulltextIndex() {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, null);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getSparse(), is(true));
        assertThat(indexResult.getType(), is(IndexType.fulltext));
        assertThat(indexResult.getUnique(), is(false));
    }

    @Test
    public void createFulltextIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "fulltextIndex-" + rnd();
        final FulltextIndexOptions options = new FulltextIndexOptions();
        options.name(name);

        String f = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getConstraint(), is(nullValue()));
        assertThat(indexResult.getFields(), hasItem(f));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getSparse(), is(true));
        assertThat(indexResult.getType(), is(IndexType.fulltext));
        assertThat(indexResult.getUnique(), is(false));
        assertThat(indexResult.getName(), is(name));
    }

    @Test
    public void createTtlIndexWithoutOptions() {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        try {
            collection.ensureTtlIndex(fields, null);
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(400));
            assertThat(e.getErrorNum(), is(10));
            assertThat(e.getMessage(), containsString("expireAfter attribute must be a number"));
        }
    }

    @Test
    public void createTtlIndexWithOptions() {
        assumeTrue(isAtLeastVersion(3, 5));

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);

        String name = "ttlIndex-" + rnd();
        final TtlIndexOptions options = new TtlIndexOptions();
        options.name(name);
        options.expireAfter(3600);

        final IndexEntity indexResult = collection.ensureTtlIndex(fields, options);
        assertThat(indexResult, is(notNullValue()));
        assertThat(indexResult.getFields(), hasItem(f1));
        assertThat(indexResult.getId(), startsWith(COLLECTION_NAME));
        assertThat(indexResult.getIsNewlyCreated(), is(true));
        assertThat(indexResult.getType(), is(IndexType.ttl));
        assertThat(indexResult.getExpireAfter(), is(3600));
        assertThat(indexResult.getName(), is(name));

        // revert changes
        collection.deleteIndex(indexResult.getId());
    }

    @Test
    public void getIndexes() {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        collection.ensureHashIndex(fields, null);
        long matchingIndexes = collection.getIndexes().stream()
                .filter(i -> i.getType() == IndexType.hash)
                .filter(i -> i.getFields().contains(f1))
                .count();
        assertThat(matchingIndexes, is(1L));
    }

    @Test
    public void getEdgeIndex() {
        Collection<IndexEntity> indexes = edgeCollection.getIndexes();
        long primaryIndexes = indexes.stream().filter(i -> i.getType() == IndexType.primary).count();
        long edgeIndexes = indexes.stream().filter(i -> i.getType() == IndexType.primary).count();
        assertThat(primaryIndexes, is(1L));
        assertThat(edgeIndexes, is(1L));
    }

    @Test
    public void exists() {
        assertThat(collection.exists(), is(true));
        assertThat(db.collection(COLLECTION_NAME + "no").exists(), is(false));
    }

    @Test
    public void truncate() {
        final BaseDocument doc = new BaseDocument();
        collection.insertDocument(doc, null);
        final BaseDocument readResult = collection
                .getDocument(doc.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey(), is(doc.getKey()));
        final CollectionEntity truncateResult = collection.truncate();
        assertThat(truncateResult, is(notNullValue()));
        assertThat(truncateResult.getId(), is(notNullValue()));
        final BaseDocument document = collection
                .getDocument(doc.getKey(), BaseDocument.class, null);
        assertThat(document, is(nullValue()));
    }

    @Test
    public void getCount() {
        Long initialCount = collection.count().getCount();
        collection.insertDocument("{}", null);
        final CollectionPropertiesEntity count = collection.count();
        assertThat(count.getCount(), is(initialCount + 1L));
    }

    @Test
    public void documentExists() throws JsonProcessingException {
        final Boolean existsNot = collection.documentExists(rnd(), null);
        assertThat(existsNot, is(false));

        String key = rnd();
        collection.insertDocument(mapper.writeValueAsString(Collections.singletonMap("_key", key)), null);
        final Boolean exists = collection.documentExists(key, null);
        assertThat(exists, is(true));
    }

    @Test(expected = ArangoDBException.class)
    public void documentExistsThrowExcpetion() {
        collection.documentExists("no", new DocumentExistsOptions().catchException(false));
    }

    @Test
    public void documentExistsIfMatch() throws JsonProcessingException {
        String key = rnd();
        final DocumentCreateEntity<String> createResult = collection.insertDocument(mapper.writeValueAsString(Collections.singletonMap("_key", key)), null);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists, is(true));
    }

    @Test
    public void documentExistsIfMatchFail() throws JsonProcessingException {
        String key = rnd();
        collection.insertDocument(mapper.writeValueAsString(Collections.singletonMap("_key", key)), null);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists, is(false));
    }

    @Test
    public void documentExistsIfNoneMatch() throws JsonProcessingException {
        String key = rnd();
        collection.insertDocument(mapper.writeValueAsString(Collections.singletonMap("_key", key)), null);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists, is(true));
    }

    @Test
    public void documentExistsIfNoneMatchFail() throws JsonProcessingException {
        String key = rnd();
        final DocumentCreateEntity<String> createResult = collection.insertDocument(mapper.writeValueAsString(Collections.singletonMap("_key", key)), null);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists, is(false));
    }

    @Test
    public void insertDocuments() {
        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(),
                new BaseDocument(),
                new BaseDocument()
        );

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection
                .insertDocuments(values, null);
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getDocuments(), is(notNullValue()));
        assertThat(docs.getDocuments().size(), is(3));
        assertThat(docs.getErrors(), is(notNullValue()));
        assertThat(docs.getErrors().size(), is(0));
    }

    @Test
    public void insertDocumentsOverwrite() {
        assumeTrue(isAtLeastVersion(3, 4));

        final BaseDocument doc1 = new BaseDocument();
        doc1.addAttribute("value", "a");
        final DocumentCreateEntity<BaseDocument> meta1 = collection.insertDocument(doc1);

        final BaseDocument doc2 = new BaseDocument();
        doc2.addAttribute("value", "a");
        final DocumentCreateEntity<BaseDocument> meta2 = collection.insertDocument(doc2);

        doc1.addAttribute("value", "b");
        doc2.addAttribute("value", "b");

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> repsert = collection
                .insertDocuments(Arrays.asList(doc1, doc2),
                        new DocumentCreateOptions().overwrite(true).returnOld(true).returnNew(true));
        assertThat(repsert, is(notNullValue()));
        assertThat(repsert.getDocuments().size(), is(2));
        assertThat(repsert.getErrors().size(), is(0));
        for (final DocumentCreateEntity<BaseDocument> documentCreateEntity : repsert.getDocuments()) {
            assertThat(documentCreateEntity.getRev(), is(not(meta1.getRev())));
            assertThat(documentCreateEntity.getRev(), is(not(meta2.getRev())));
            assertThat(documentCreateEntity.getOld().getAttribute("value").toString(), is("a"));
            assertThat(documentCreateEntity.getNew().getAttribute("value").toString(), is("b"));
        }
    }

    @Test
    public void insertDocumentsOverwriteModeUpdate() {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc1 = new BaseDocument();
        doc1.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta1 = collection.insertDocument(doc1);

        final BaseDocument doc2 = new BaseDocument();
        doc2.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> meta2 = collection.insertDocument(doc2);

        doc1.addAttribute("bar", "b");
        doc2.addAttribute("bar", "b");

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> repsert = collection
                .insertDocuments(Arrays.asList(doc1, doc2),
                        new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true));
        assertThat(repsert, is(notNullValue()));
        assertThat(repsert.getDocuments().size(), is(2));
        assertThat(repsert.getErrors().size(), is(0));
        for (final DocumentCreateEntity<BaseDocument> documentCreateEntity : repsert.getDocuments()) {
            assertThat(documentCreateEntity.getRev(), is(not(meta1.getRev())));
            assertThat(documentCreateEntity.getRev(), is(not(meta2.getRev())));
            assertThat(documentCreateEntity.getNew().getAttribute("foo").toString(), is("a"));
            assertThat(documentCreateEntity.getNew().getAttribute("bar").toString(), is("b"));
        }
    }

    @Test
    public void insertDocumentsJson() {
        final Collection<String> values = new ArrayList<>();
        values.add("{}");
        values.add("{}");
        values.add("{}");
        final MultiDocumentEntity<DocumentCreateEntity<String>> docs = collection
                .insertDocuments(values);
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
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection
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
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection
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
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection
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
        String k1 = rnd();
        String k2 = rnd();
        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection
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
        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(),
                new BaseDocument(),
                new BaseDocument()
        );

        final DocumentImportEntity docs = collection.importDocuments(values);
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
        final Collection<String> values = Arrays.asList(
                "{}",
                "{}",
                "{}"
        );

        final DocumentImportEntity docs = collection.importDocuments(values);
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection.importDocuments(values);
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error));
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update));
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
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        try {
            collection.importDocuments(values, new DocumentImportOptions().complete(true));
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorNum(), is(1210));
        }
    }

    @Test
    public void importDocumentsDetails() {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(k1),
                new BaseDocument(k2),
                new BaseDocument(k2)
        );

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().details(true));
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
        collection.insertDocument(new BaseDocument());
        Long initialCount = collection.count().getCount();

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false));
        assertThat(collection.count().getCount(), is(initialCount + 2L));
    }

    @Test
    public void importDocumentsOverwriteTrue() {
        collection.insertDocument(new BaseDocument());

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true));
        assertThat(collection.count().getCount(), is(2L));
    }

    @Test
    public void importDocumentsFromToPrefix() {
        final Collection<BaseEdgeDocument> values = new ArrayList<>();
        final String[] keys = {
                rnd(),
                rnd()
        };
        for (String s : keys) {
            values.add(new BaseEdgeDocument(s, "from", "to"));
        }
        assertThat(values.size(), is(keys.length));

        final DocumentImportEntity importResult = edgeCollection
                .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
        assertThat(importResult, is(notNullValue()));
        assertThat(importResult.getCreated(), is(values.size()));
        for (String key : keys) {
            final BaseEdgeDocument doc = edgeCollection.getDocument(key, BaseEdgeDocument.class);
            assertThat(doc, is(notNullValue()));
            assertThat(doc.getFrom(), is("foo/from"));
            assertThat(doc.getTo(), is("bar/to"));
        }
    }

    @Test
    public void importDocumentsJson() throws JsonProcessingException {
        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())
        ));

        final DocumentImportEntity docs = collection.importDocuments(values);
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getCreated(), is(2));
        assertThat(docs.getEmpty(), is(0));
        assertThat(docs.getErrors(), is(0));
        assertThat(docs.getIgnored(), is(0));
        assertThat(docs.getUpdated(), is(0));
        assertThat(docs.getDetails(), is(empty()));
    }

    @Test
    public void importDocumentsJsonDuplicateDefaultError() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));

        final DocumentImportEntity docs = collection.importDocuments(values);
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getCreated(), is(2));
        assertThat(docs.getEmpty(), is(0));
        assertThat(docs.getErrors(), is(1));
        assertThat(docs.getIgnored(), is(0));
        assertThat(docs.getUpdated(), is(0));
        assertThat(docs.getDetails(), is(empty()));
    }

    @Test
    public void importDocumentsJsonDuplicateError() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.error));
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getCreated(), is(2));
        assertThat(docs.getEmpty(), is(0));
        assertThat(docs.getErrors(), is(1));
        assertThat(docs.getIgnored(), is(0));
        assertThat(docs.getUpdated(), is(0));
        assertThat(docs.getDetails(), is(empty()));
    }

    @Test
    public void importDocumentsJsonDuplicateIgnore() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));
        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getCreated(), is(2));
        assertThat(docs.getEmpty(), is(0));
        assertThat(docs.getErrors(), is(0));
        assertThat(docs.getIgnored(), is(1));
        assertThat(docs.getUpdated(), is(0));
        assertThat(docs.getDetails(), is(empty()));
    }

    @Test
    public void importDocumentsJsonDuplicateReplace() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
        assertThat(docs, is(notNullValue()));
        assertThat(docs.getCreated(), is(2));
        assertThat(docs.getEmpty(), is(0));
        assertThat(docs.getErrors(), is(0));
        assertThat(docs.getIgnored(), is(0));
        assertThat(docs.getUpdated(), is(1));
        assertThat(docs.getDetails(), is(empty()));
    }

    @Test
    public void importDocumentsJsonDuplicateUpdate() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().onDuplicate(OnDuplicate.update));
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
            collection.importDocuments(values, new DocumentImportOptions().complete(true));
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getErrorNum(), is(1210));
        }
    }

    @Test
    public void importDocumentsJsonDetails() throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2),
                Collections.singletonMap("_key", k2)
        ));

        final DocumentImportEntity docs = collection
                .importDocuments(values, new DocumentImportOptions().details(true));
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
    public void importDocumentsJsonOverwriteFalse() throws JsonProcessingException {
        collection.insertDocument(new BaseDocument());
        Long initialCount = collection.count().getCount();

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())
        ));
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false));
        assertThat(collection.count().getCount(), is(initialCount + 2L));
    }

    @Test
    public void importDocumentsJsonOverwriteTrue() throws JsonProcessingException {
        collection.insertDocument(new BaseDocument());

        final String values = mapper.writeValueAsString(Arrays.asList(
                Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())
        ));
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true));
        assertThat(collection.count().getCount(), is(2L));
    }

    @Test
    public void importDocumentsJsonFromToPrefix() throws JsonProcessingException {
        String k1 = UUID.randomUUID().toString();
        String k2 = UUID.randomUUID().toString();

        final String[] keys = {k1, k2};

        final String values = mapper.writeValueAsString(Arrays.asList(
                new MapBuilder()
                        .put("_key", k1)
                        .put("_from", "from")
                        .put("_to", "to")
                        .get(),
                new MapBuilder()
                        .put("_key", k2)
                        .put("_from", "from")
                        .put("_to", "to")
                        .get()
        ));

        final DocumentImportEntity importResult = edgeCollection
                .importDocuments(values, new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
        assertThat(importResult, is(notNullValue()));
        assertThat(importResult.getCreated(), is(2));
        for (String key : keys) {
            final BaseEdgeDocument doc = edgeCollection.getDocument(key, BaseEdgeDocument.class);
            assertThat(doc, is(notNullValue()));
            assertThat(doc.getFrom(), is("foo/from"));
            assertThat(doc.getTo(), is("bar/to"));
        }
    }

    @Test
    public void deleteDocumentsByKey() {
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
        collection.insertDocuments(values, null);
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
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
        collection.insertDocuments(values, null);
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
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
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values, null);
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
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
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values, null);
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
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
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values, null);
        final Collection<String> keys = new ArrayList<>();
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
                .deleteDocuments(keys, null, null);
        assertThat(deleteResult, is(notNullValue()));
        assertThat(deleteResult.getDocuments().size(), is(0));
        assertThat(deleteResult.getErrors().size(), is(0));
    }

    @Test
    public void deleteDocumentsByKeyNotExisting() {
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values, null);
        final Collection<String> keys = Arrays.asList(
                rnd(),
                rnd()
        );

        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
                .deleteDocuments(keys, null, null);
        assertThat(deleteResult, is(notNullValue()));
        assertThat(deleteResult.getDocuments().size(), is(0));
        assertThat(deleteResult.getErrors().size(), is(2));
    }

    @Test
    public void deleteDocumentsByDocumentsNotExisting() {
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
        final MultiDocumentEntity<DocumentDeleteEntity<Object>> deleteResult = collection
                .deleteDocuments(values, null, null);
        assertThat(deleteResult, is(notNullValue()));
        assertThat(deleteResult.getDocuments().size(), is(0));
        assertThat(deleteResult.getErrors().size(), is(2));
    }

    @Test
    public void updateDocuments() {
        final Collection<BaseDocument> values = Arrays.asList(
                new BaseDocument(rnd()),
                new BaseDocument(rnd())
        );
        collection.insertDocuments(values, null);
        values.forEach(it -> it.addAttribute("a", "test"));

        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(values, null);
        assertThat(updateResult.getDocuments().size(), is(2));
        assertThat(updateResult.getErrors().size(), is(0));
    }

    @Test
    public void updateDocumentsWithDifferentReturnType() {
        List<String> keys = IntStream.range(0, 3).mapToObj(it -> "key-" + UUID.randomUUID().toString()).collect(Collectors.toList());
        List<BaseDocument> docs = keys.stream()
                .map(BaseDocument::new)
                .peek(it -> it.addAttribute("a", "test"))
                .collect(Collectors.toList());

        collection.insertDocuments(docs);

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
                .updateDocuments(modifiedDocs, new DocumentUpdateOptions().returnNew(true), BaseDocument.class);
        assertThat(updateResult.getDocuments().size(), is(3));
        assertThat(updateResult.getErrors().size(), is(0));
        assertThat(updateResult.getDocuments().stream().map(DocumentUpdateEntity::getNew)
                        .allMatch(it -> it.getAttribute("a").equals("test") && it.getAttribute("b").equals("test")),
                is(true));
    }

    @Test
    public void updateDocumentsOne() {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument();
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values, null);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(updatedValues, null);
        assertThat(updateResult.getDocuments().size(), is(1));
        assertThat(updateResult.getErrors().size(), is(0));
    }

    @Test
    public void updateDocumentsEmpty() {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
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
        collection.insertDocuments(values, null);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(updatedValues, null);
        assertThat(updateResult.getDocuments().size(), is(1));
        assertThat(updateResult.getErrors().size(), is(1));
    }

    @Test
    public void updateDocumentsJson() {
        final Collection<String> values = new ArrayList<>();
        values.add("{\"_key\":\"1\"}");
        values.add("{\"_key\":\"2\"}");
        collection.insertDocuments(values);

        final Collection<String> updatedValues = new ArrayList<>();
        updatedValues.add("{\"_key\":\"1\", \"foo\":\"bar\"}");
        updatedValues.add("{\"_key\":\"2\", \"foo\":\"bar\"}");
        final MultiDocumentEntity<DocumentUpdateEntity<String>> updateResult = collection
                .updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments().size(), is(2));
        assertThat(updateResult.getErrors().size(), is(0));
    }

    @Test
    public void replaceDocuments() {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
            values.add(new BaseDocument("2"));
        }
        collection.insertDocuments(values, null);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
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
        collection.insertDocuments(values, null);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(updatedValues, null);
        assertThat(updateResult.getDocuments().size(), is(1));
        assertThat(updateResult.getErrors().size(), is(0));
    }

    @Test
    public void replaceDocumentsEmpty() {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
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
        collection.insertDocuments(values, null);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection
                .updateDocuments(updatedValues, null);
        assertThat(updateResult.getDocuments().size(), is(1));
        assertThat(updateResult.getErrors().size(), is(1));
    }

    @Test
    public void replaceDocumentsJson() {
        final Collection<String> values = new ArrayList<>();
        values.add("{\"_key\":\"1\"}");
        values.add("{\"_key\":\"2\"}");
        collection.insertDocuments(values);

        final Collection<String> updatedValues = new ArrayList<>();
        updatedValues.add("{\"_key\":\"1\", \"foo\":\"bar\"}");
        updatedValues.add("{\"_key\":\"2\", \"foo\":\"bar\"}");
        final MultiDocumentEntity<DocumentUpdateEntity<String>> updateResult = collection
                .replaceDocuments(updatedValues);
        assertThat(updateResult.getDocuments().size(), is(2));
        assertThat(updateResult.getErrors().size(), is(0));
    }

    @Test
    public void load() {
        final CollectionEntity result = collection.load();
        assertThat(result.getName(), is(COLLECTION_NAME));
    }

    @Test
    public void unload() {
        final CollectionEntity result = collection.unload();
        assertThat(result.getName(), is(COLLECTION_NAME));
    }

    @Test
    public void getInfo() {
        final CollectionEntity result = collection.getInfo();
        assertThat(result.getName(), is(COLLECTION_NAME));
    }

    @Test
    public void getPropeties() {
        final CollectionPropertiesEntity result = collection.getProperties();
        assertThat(result.getName(), is(COLLECTION_NAME));
        assertThat(result.getCount(), is(nullValue()));
    }

    @Test
    public void changeProperties() {
        final CollectionPropertiesEntity properties = collection.getProperties();
        assertThat(properties.getWaitForSync(), is(notNullValue()));
        if (isAtLeastVersion(3, 7)) {
            assertThat(properties.getSchema(), is(nullValue()));
        }

        String schemaRule = ("{  " +
                "           \"properties\": {" +
                "               \"number\": {" +
                "                   \"type\": \"number\"" +
                "               }" +
                "           }" +
                "       }")
                .replaceAll("\\s", "");
        String schemaMessage = "The document has problems!";

        CollectionPropertiesOptions updatedOptions = new CollectionPropertiesOptions()
                .waitForSync(!properties.getWaitForSync())
                .schema(new CollectionSchema()
                        .setLevel(CollectionSchema.Level.NEW)
                        .setMessage(schemaMessage)
                        .setRule(schemaRule)
                );

        final CollectionPropertiesEntity changedProperties = collection.changeProperties(updatedOptions);
        assertThat(changedProperties.getWaitForSync(), is(notNullValue()));
        assertThat(changedProperties.getWaitForSync(), is(!properties.getWaitForSync()));
        if (isAtLeastVersion(3, 7)) {
            assertThat(changedProperties.getSchema(), is(notNullValue()));
            assertThat(changedProperties.getSchema().getLevel(), is(CollectionSchema.Level.NEW));
            assertThat(changedProperties.getSchema().getMessage(), is(schemaMessage));
            assertThat(changedProperties.getSchema().getRule(), is(schemaRule));
        }

        // revert changes
        CollectionPropertiesEntity revertedProperties = collection.changeProperties(new CollectionPropertiesOptions()
                .waitForSync(properties.getWaitForSync())
                .schema(CollectionSchema.NULL_SCHEMA)
        );
        if (isAtLeastVersion(3, 7)) {
            assertThat(revertedProperties.getSchema(), is(nullValue()));
        }

    }

    @Test
    public void rename() {
        assumeTrue(isSingleServer());
        final CollectionEntity result = collection.rename(COLLECTION_NAME + "1");
        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is(COLLECTION_NAME + "1"));
        final CollectionEntity info = db.collection(COLLECTION_NAME + "1").getInfo();
        assertThat(info.getName(), is(COLLECTION_NAME + "1"));
        try {
            db.collection(COLLECTION_NAME).getInfo();
            fail();
        } catch (final ArangoDBException e) {
            assertThat(e.getResponseCode(), is(404));
        }
        db.collection(COLLECTION_NAME + "1").rename(COLLECTION_NAME);
    }

    @Test
    public void responsibleShard() {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 5));
        ShardEntity shard = collection.getResponsibleShard(new BaseDocument("testKey"));
        assertThat(shard, is(notNullValue()));
        assertThat(shard.getShardId(), is(notNullValue()));
    }

    @Test
    public void renameDontBreaksCollectionHandler() {
        assumeTrue(isSingleServer());
        collection.rename(COLLECTION_NAME + "1");
        assertThat(collection.getInfo(), is(notNullValue()));
        db.collection(COLLECTION_NAME + "1").rename(COLLECTION_NAME);
    }

    @Test
    public void getRevision() {
        final CollectionRevisionEntity result = collection.getRevision();
        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is(COLLECTION_NAME));
        assertThat(result.getRevision(), is(notNullValue()));
    }

    @Test
    public void keyWithSpecialCharacter() {
        final String key = "myKey_-:.@()+,=;$!*'%";
        collection.insertDocument(new BaseDocument(key));
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getKey(), is(key));
    }

    @Test
    public void alreadyUrlEncodedkey() {
        final String key = "http%3A%2F%2Fexample.com%2F";
        collection.insertDocument(new BaseDocument(key));
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getKey(), is(key));
    }

    @Test
    public void grantAccessRW() {
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.RW);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void grantAccessRO() {
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.RO);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test
    public void grantAccessNONE() {
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.NONE);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void grantAccessUserNotFound() {
        collection.grantAccess("user1", Permissions.RW);
    }

    @Test
    public void revokeAccess() {
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.NONE);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void revokeAccessUserNotFound() {
        collection.grantAccess("user1", Permissions.NONE);
    }

    @Test
    public void resetAccess() {
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.resetAccess("user1");
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @Test(expected = ArangoDBException.class)
    public void resetAccessUserNotFound() {
        collection.resetAccess("user1");
    }

    @Test
    public void getPermissions() {
        assertThat(Permissions.RW, is(collection.getPermissions("root")));
    }

}

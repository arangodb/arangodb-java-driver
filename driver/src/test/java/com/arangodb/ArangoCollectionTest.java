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

import com.arangodb.entity.*;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;
import com.arangodb.model.DocumentImportOptions.OnDuplicate;
import com.arangodb.serde.jackson.Id;
import com.arangodb.serde.jackson.JacksonSerde;
import com.arangodb.serde.jackson.Key;
import com.arangodb.serde.jackson.Rev;
import com.arangodb.util.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoCollectionTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "ArangoCollectionTest_collection";
    private static final String EDGE_COLLECTION_NAME = "ArangoCollectionTest_edge_collection";

    private final ObjectMapper mapper = new ObjectMapper();

    private static Stream<Arguments> cols() {
        return dbsStream()
                .map(mapNamedPayload(db -> db.collection(COLLECTION_NAME)))
                .map(Arguments::of);
    }

    private static Stream<Arguments> edges() {
        return dbsStream()
                .map(mapNamedPayload(db -> db.collection(EDGE_COLLECTION_NAME)))
                .map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
        initEdgeCollections(EDGE_COLLECTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocument(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), null);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
        assertThat(doc.getId()).isEqualTo(COLLECTION_NAME + "/" + doc.getKey());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentWithArrayWithNullValues(ArangoCollection collection) {
        List<String> arr = Arrays.asList("a", null);
        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("arr", arr);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection.insertDocument(doc,
                new DocumentCreateOptions().returnNew(true));
        assertThat(insertedDoc).isNotNull();
        assertThat(insertedDoc.getId()).isNotNull();
        assertThat(insertedDoc.getKey()).isNotNull();
        assertThat(insertedDoc.getRev()).isNotNull();
        assertThat(insertedDoc.getId()).isEqualTo(COLLECTION_NAME + "/" + insertedDoc.getKey());
        //noinspection unchecked
        assertThat((List<String>) insertedDoc.getNew().getAttribute("arr")).containsAll(Arrays.asList("a", null));
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentWithNullValues(ArangoCollection collection) {
        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("null", null);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection.insertDocument(doc,
                new DocumentCreateOptions().returnNew(true));
        assertThat(insertedDoc).isNotNull();
        assertThat(insertedDoc.getId()).isNotNull();
        assertThat(insertedDoc.getKey()).isNotNull();
        assertThat(insertedDoc.getRev()).isNotNull();
        assertThat(insertedDoc.getId()).isEqualTo(COLLECTION_NAME + "/" + insertedDoc.getKey());
        assertThat(insertedDoc.getNew().getProperties()).containsKey("null");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentUpdateRev(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentReturnNew(ArangoCollection collection) {
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentWithTypeOverwriteModeReplace(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(collection.getSerde().getUserSerde() instanceof JacksonSerde, "polymorphic deserialization support" +
                " required");

        String key = UUID.randomUUID().toString();
        Dog dog = new Dog(key, "Teddy");
        Cat cat = new Cat(key, "Luna");

        final DocumentCreateOptions options = new DocumentCreateOptions()
                .returnNew(true)
                .returnOld(true)
                .overwriteMode(OverwriteMode.replace);
        collection.insertDocument(dog, options);
        final DocumentCreateEntity<Animal> doc = collection.insertDocument(cat, options, Animal.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull().isEqualTo(key);
        assertThat(doc.getRev()).isNotNull();

        assertThat(doc.getOld())
                .isNotNull()
                .isInstanceOf(Dog.class);
        assertThat(doc.getOld().getKey()).isEqualTo(key);
        assertThat(doc.getOld().getName()).isEqualTo("Teddy");

        assertThat(doc.getNew())
                .isNotNull()
                .isInstanceOf(Cat.class);
        assertThat(doc.getNew().getKey()).isEqualTo(key);
        assertThat(doc.getNew().getName()).isEqualTo("Luna");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeIgnore(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> insertIgnore = collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.ignore));

        assertThat(insertIgnore).isNotNull();
        assertThat(insertIgnore.getRev()).isEqualTo(meta.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeConflict(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        Throwable thrown = catchThrowable(() -> collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.conflict)));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(409);
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeReplace(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc);

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> repsert = collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.replace).returnNew(true));

        assertThat(repsert).isNotNull();
        assertThat(repsert.getRev()).isNotEqualTo(meta.getRev());
        assertThat(repsert.getNew().getProperties().containsKey("foo")).isFalse();
        assertThat(repsert.getNew().getAttribute("bar")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeUpdate(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc);

        doc.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> updated = collection.insertDocument(doc,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true));

        assertThat(updated).isNotNull();
        assertThat(updated.getRev()).isNotEqualTo(meta.getRev());
        assertThat(updated.getNew().getAttribute("foo")).isEqualTo("a");
        assertThat(updated.getNew().getAttribute("bar")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeUpdateMergeObjectsFalse(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        Map<String, String> fieldA = Collections.singletonMap("a", "a");
        doc.addAttribute("foo", fieldA);
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc);

        Map<String, String> fieldB = Collections.singletonMap("b", "b");
        doc.addAttribute("foo", fieldB);
        final DocumentCreateEntity<BaseDocument> updated = collection.insertDocument(doc,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.update).mergeObjects(false).returnNew(true));

        assertThat(updated).isNotNull();
        assertThat(updated.getRev()).isNotEqualTo(meta.getRev());
        assertThat(updated.getNew().getAttribute("foo")).isEqualTo(fieldB);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeUpdateKeepNullTrue(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "bar");
        collection.insertDocument(doc);

        doc.updateAttribute("foo", null);
        final BaseDocument updated = collection.insertDocument(doc, new DocumentCreateOptions()
                .overwriteMode(OverwriteMode.update)
                .keepNull(true)
                .returnNew(true)).getNew();

        assertThat(updated.getProperties()).containsEntry("foo", null);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentOverwriteModeUpdateKeepNullFalse(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "bar");
        collection.insertDocument(doc);

        doc.updateAttribute("foo", null);
        final BaseDocument updated = collection.insertDocument(doc, new DocumentCreateOptions()
                .overwriteMode(OverwriteMode.update)
                .keepNull(false)
                .returnNew(true)).getNew();

        assertThat(updated.getProperties()).doesNotContainKey("foo");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentWaitForSync(ArangoCollection collection) {
        final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentRefillIndexCaches(ArangoCollection collection) {
        final DocumentCreateOptions options = new DocumentCreateOptions().refillIndexCaches(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentAsJson(ArangoCollection collection) {
        String key = "doc-" + UUID.randomUUID();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\",\"a\":\"test\"}");
        final DocumentCreateEntity<?> doc = collection.insertDocument(rawJson);
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo(collection.name() + "/" + key);
        assertThat(doc.getKey()).isEqualTo(key);
        assertThat(doc.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentAsBytes(ArangoCollection collection) {
        String key = "doc-" + UUID.randomUUID();
        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("a", "test");
        byte[] bytes = collection.getSerde().serializeUserData(doc);
        RawBytes rawJson = RawBytes.of(bytes);
        final DocumentCreateEntity<RawBytes> createEntity = collection.insertDocument(rawJson,
                new DocumentCreateOptions().returnNew(true));
        assertThat(createEntity).isNotNull();
        assertThat(createEntity.getId()).isEqualTo(collection.name() + "/" + key);
        assertThat(createEntity.getKey()).isEqualTo(key);
        assertThat(createEntity.getRev()).isNotNull();
        assertThat(createEntity.getNew()).isNotNull().isInstanceOf(RawBytes.class);
        Map<String, Object> newDoc = collection.getSerde().deserializeUserData(createEntity.getNew().get(),
                Map.class);
        assertThat(newDoc).containsAllEntriesOf(doc);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(new BaseDocument(),
                new DocumentCreateOptions().silent(true));
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentSilentDontTouchInstance(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final String key = "testkey-" + UUID.randomUUID();
        doc.setKey(key);
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc,
                new DocumentCreateOptions().silent(true));
        assertThat(meta).isNotNull();
        assertThat(meta.getKey()).isNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> info =
                collection.insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().silent(true), BaseDocument.class);
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsWithErrors(ArangoCollection collection) {
        // BTS-615
        assumeTrue(isAtLeastVersion(3, 11));

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> res =
                collection.insertDocuments(Arrays.asList(
                                new BaseDocument(),
                                new BaseDocument("<<illegalId>>"),
                                new BaseDocument()
                        ),
                        new DocumentCreateOptions(), BaseDocument.class);
        assertThat(res).isNotNull();
        assertThat(res.getDocuments()).hasSize(2);
        assertThat(res.getErrors()).hasSize(1);
        assertThat(res.getDocumentsAndErrors()).hasSize(3);
        assertThat(res.getDocumentsAndErrors().get(0)).isSameAs(res.getDocuments().get(0));
        assertThat(res.getDocumentsAndErrors().get(1)).isSameAs(res.getErrors().get(0));
        assertThat(res.getDocumentsAndErrors().get(2)).isSameAs(res.getDocuments().get(1));
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsRefillIndexCaches(ArangoCollection collection) {
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> info =
                collection.insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().refillIndexCaches(true), BaseDocument.class);
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocument(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey()).isNotNull();
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentIfMatch(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentIfMatchFail(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentIfNoneMatch(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentIfNoneMatchFail(ArangoCollection collection) {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null);
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, options);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentAsJson(ArangoCollection collection) {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\",\"a\":\"test\"}");
        collection.insertDocument(rawJson);
        final RawJson readResult = collection.getDocument(key, RawJson.class);
        assertThat(readResult.get()).contains("\"_key\":\"" + key + "\"").contains("\"_id\":\"" + COLLECTION_NAME + "/" + key + "\"");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentNotFound(ArangoCollection collection) {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentNotFoundOptionsDefault(ArangoCollection collection) {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class, new DocumentReadOptions());
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentNotFoundOptionsNull(ArangoCollection collection) {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class, null);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentWrongKey(ArangoCollection collection) {
        Throwable thrown = catchThrowable(() -> collection.getDocument("no/no", BaseDocument.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentDirtyRead(ArangoCollection collection) throws InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        collection.insertDocument(doc, new DocumentCreateOptions());
        Thread.sleep(2000);
        final RawJson document = collection.getDocument(doc.getKey(), RawJson.class,
                new DocumentReadOptions().allowDirtyRead(true));
        assertThat(document).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values);
        final MultiDocumentEntity<BaseDocument> documents = collection.getDocuments(Arrays.asList("1", "2", "3"),
                BaseDocument.class);
        assertThat(documents).isNotNull();
        assertThat(documents.getDocuments()).hasSize(3);
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId()).isIn(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2",
                    COLLECTION_NAME + "/" + "3");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentsWithCustomShardingKey(ArangoCollection c) {
        ArangoCollection collection = c.db().collection("customShardingKeyCollection");
        if (collection.exists()) collection.drop();

        collection.create(new CollectionCreateOptions().shardKeys("customField").numberOfShards(10));

        List<BaseDocument> values =
                IntStream.range(0, 10).mapToObj(String::valueOf).map(key -> new BaseDocument()).peek(it -> it.addAttribute(
                        "customField", rnd())).collect(Collectors.toList());

        MultiDocumentEntity<DocumentCreateEntity<Void>> inserted = collection.insertDocuments(values);
        List<String> insertedKeys =
                inserted.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        final Collection<BaseDocument> documents =
                collection.getDocuments(insertedKeys, BaseDocument.class).getDocuments();

        assertThat(documents).hasSize(10);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentsDirtyRead(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values);
        final MultiDocumentEntity<BaseDocument> documents = collection.getDocuments(Arrays.asList("1", "2", "3"),
                BaseDocument.class, new DocumentReadOptions().allowDirtyRead(true));
        assertThat(documents).isNotNull();
        if (isAtLeastVersion(3, 10)) {
            assertThat(documents.isPotentialDirtyRead()).isTrue();
        }
        assertThat(documents.getDocuments()).hasSize(3);
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId()).isIn(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2",
                    COLLECTION_NAME + "/" + "3");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentsNotFound(ArangoCollection collection) {
        final MultiDocumentEntity<BaseDocument> readResult = collection.getDocuments(Collections.singleton("no"),
                BaseDocument.class);
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getDocumentsWrongKey(ArangoCollection collection) {
        final MultiDocumentEntity<BaseDocument> readResult = collection.getDocuments(Collections.singleton("no/no"),
                BaseDocument.class);
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocument(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                null);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getNew()).isNull();
        assertThat(updateResult.getOld()).isNull();
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentWithDifferentReturnType(ArangoCollection collection) {
        final String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("a", "test");
        collection.insertDocument(doc);

        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(key,
                Collections.singletonMap("b", "test"), new DocumentUpdateOptions().returnNew(true), BaseDocument.class);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(key);
        BaseDocument updated = updateResult.getNew();
        assertThat(updated).isNotNull();
        assertThat(updated.getAttribute("a")).isEqualTo("test");
        assertThat(updated.getAttribute("b")).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentUpdateRev(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.addAttribute("foo", "bar");
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                null);
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentIfMatch(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentIfMatchFail(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.updateDocument(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentReturnNew(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
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
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentReturnOld(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());
        assertThat(updateResult.getOld()).isNotNull();
        assertThat(updateResult.getOld().getKey()).isEqualTo(createResult.getKey());
        assertThat(updateResult.getOld().getRevision()).isEqualTo(createResult.getRev());
        assertThat(updateResult.getOld().getAttribute("a")).isNotNull();
        assertThat(String.valueOf(updateResult.getOld().getAttribute("a"))).isEqualTo("test");
        assertThat(updateResult.getOld().getProperties().keySet()).doesNotContain("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentKeepNullTrue(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentKeepNullFalse(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentSerializeNullTrue(ArangoCollection collection) {
        final TestUpdateEntity doc = new TestUpdateEntity();
        doc.a = "foo";
        doc.b = "foo";
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        final TestUpdateEntity patchDoc = new TestUpdateEntity();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<?> updateResult = collection.updateDocument(createResult.getKey(), patchDoc);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(createResult.getKey());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKey("a");
        assertThat(readResult.getAttribute("a")).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentSerializeNullFalse(ArangoCollection collection) {
        final TestUpdateEntitySerializeNullFalse doc = new TestUpdateEntitySerializeNullFalse();
        doc.a = "foo";
        doc.b = "foo";
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        final TestUpdateEntitySerializeNullFalse patchDoc = new TestUpdateEntitySerializeNullFalse();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<?> updateResult = collection.updateDocument(createResult.getKey(), patchDoc);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(createResult.getKey());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKeys("a", "b");
        assertThat(readResult.getAttribute("a")).isEqualTo("bar");
        assertThat(readResult.getAttribute("b")).isEqualTo("foo");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentMergeObjectsTrue(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap).containsKeys("a", "b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentMergeObjectsFalse(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options);
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet()).doesNotContain("a");
        assertThat(aMap).containsKey("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentIgnoreRevsFalse(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.updateAttribute("a", "test1");
        doc.setRevision("no");

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
        Throwable thrown = catchThrowable(() -> collection.updateDocument(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final DocumentUpdateEntity<BaseDocument> meta = collection.updateDocument(createResult.getKey(),
                new BaseDocument(), new DocumentUpdateOptions().silent(true));
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.updateDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentUpdateOptions().silent(true), BaseDocument.class);
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateNonExistingDocument(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");

        Throwable thrown = catchThrowable(() -> collection.updateDocument(doc.getKey(), doc, null));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1202);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentPreconditionFailed(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);

        doc.updateAttribute("foo", "b");
        collection.updateDocument(doc.getKey(), doc, null);

        doc.updateAttribute("foo", "c");
        Throwable thrown = catchThrowable(() -> collection.updateDocument(doc.getKey(), doc,
                new DocumentUpdateOptions().ifMatch(createResult.getRev())));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(412);
        assertThat(e.getErrorNum()).isEqualTo(1200);
        BaseDocument readDocument = collection.getDocument(doc.getKey(), BaseDocument.class);
        assertThat(readDocument.getAttribute("foo")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentRefillIndexCaches(ArangoCollection collection) {
        BaseDocument doc = new BaseDocument();
        DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        doc.addAttribute("foo", "bar");
        DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(),
                doc, new DocumentUpdateOptions().refillIndexCaches(true));
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsRefillIndexCaches(ArangoCollection collection) {
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.updateDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentUpdateOptions().refillIndexCaches(true), BaseDocument.class);
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocument(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, null);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getNew()).isNull();
        assertThat(replaceResult.getOld()).isNull();
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentUpdateRev(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, null);
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentIfMatch(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentIfMatchFail(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.replaceDocument(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);

    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentIgnoreRevsFalse(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        doc.setRevision("no");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
        Throwable thrown = catchThrowable(() -> collection.replaceDocument(createResult.getKey(), doc, options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentReturnNew(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
        assertThat(replaceResult.getNew()).isNotNull();
        assertThat(replaceResult.getNew().getKey()).isEqualTo(createResult.getKey());
        assertThat(replaceResult.getNew().getRevision()).isNotEqualTo(createResult.getRev());
        assertThat(replaceResult.getNew().getProperties().keySet()).doesNotContain("a");
        assertThat(replaceResult.getNew().getAttribute("b")).isNotNull();
        assertThat(String.valueOf(replaceResult.getNew().getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentReturnOld(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options);
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());
        assertThat(replaceResult.getOld()).isNotNull();
        assertThat(replaceResult.getOld().getKey()).isEqualTo(createResult.getKey());
        assertThat(replaceResult.getOld().getRevision()).isEqualTo(createResult.getRev());
        assertThat(replaceResult.getOld().getAttribute("a")).isNotNull();
        assertThat(String.valueOf(replaceResult.getOld().getAttribute("a"))).isEqualTo("test");
        assertThat(replaceResult.getOld().getProperties().keySet()).doesNotContain("b");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final DocumentUpdateEntity<BaseDocument> meta = collection.replaceDocument(createResult.getKey(),
                new BaseDocument(), new DocumentReplaceOptions().silent(true));
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentSilentDontTouchInstance(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        final DocumentUpdateEntity<BaseDocument> meta = collection.replaceDocument(createResult.getKey(), doc,
                new DocumentReplaceOptions().silent(true));
        assertThat(meta.getRev()).isNull();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.replaceDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentReplaceOptions().silent(true), BaseDocument.class);
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentRefillIndexCaches(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(), doc,
                new DocumentReplaceOptions().refillIndexCaches(true));
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsRefillIndexCaches(ArangoCollection collection) {
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.replaceDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentReplaceOptions().refillIndexCaches(true), BaseDocument.class);
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocument(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        collection.deleteDocument(createResult.getKey());
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentReturnOld(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
        final DocumentDeleteEntity<BaseDocument> deleteResult = collection.deleteDocument(createResult.getKey(),
                options, BaseDocument.class);
        assertThat(deleteResult.getOld()).isNotNull();
        assertThat(deleteResult.getOld()).isInstanceOf(BaseDocument.class);
        assertThat(deleteResult.getOld().getAttribute("a")).isNotNull();
        assertThat(String.valueOf(deleteResult.getOld().getAttribute("a"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentIfMatch(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
        collection.deleteDocument(createResult.getKey(), options);
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, null);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentIfMatchFail(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc);
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.deleteDocument(createResult.getKey(), options));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final DocumentDeleteEntity<BaseDocument> meta = collection.deleteDocument(createResult.getKey(),
                new DocumentDeleteOptions().silent(true), BaseDocument.class);
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsSilent(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentDeleteEntity<BaseDocument>> info = collection.deleteDocuments(
                Collections.singletonList(createResult.getKey()),
                new DocumentDeleteOptions().silent(true),
                BaseDocument.class);
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentRefillIndexCaches(ArangoCollection collection) {
        DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        DocumentDeleteEntity<?> deleteResult = collection.deleteDocument(createResult.getKey(),
                new DocumentDeleteOptions().refillIndexCaches(true));
        assertThat(deleteResult.getRev())
                .isNotNull()
                .isEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsRefillIndexCaches(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument());
        final MultiDocumentEntity<DocumentDeleteEntity<BaseDocument>> info = collection.deleteDocuments(
                Collections.singletonList(createResult.getKey()),
                new DocumentDeleteOptions().refillIndexCaches(true),
                BaseDocument.class);
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getIndex(ArangoCollection collection) {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null);
        final IndexEntity readResult = collection.getIndex(createResult.getId());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getIndexByKey(ArangoCollection collection) {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null);
        final IndexEntity readResult = collection.getIndex(createResult.getId().split("/")[1]);
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteIndex(ArangoCollection collection) {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null);
        final String id = collection.deleteIndex(createResult.getId());
        assertThat(id).isEqualTo(createResult.getId());
        Throwable thrown = catchThrowable(() -> collection.db().getIndex(id));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteIndexByKey(ArangoCollection collection) {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null);
        final String id = collection.deleteIndex(createResult.getId().split("/")[1]);
        assertThat(id).isEqualTo(createResult.getId());
        Throwable thrown = catchThrowable(() -> collection.db().getIndex(id));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createGeoIndex(ArangoCollection collection) {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
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
        if (isAtLeastVersion(3, 10)) {
            assertThat(indexResult.getLegacyPolygons()).isFalse();
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createGeoIndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
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
        assertThat(indexResult.getName()).isEqualTo(name);
        if (isAtLeastVersion(3, 10)) {
            assertThat(indexResult.getLegacyPolygons()).isFalse();
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createGeoIndexLegacyPolygons(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);
        options.legacyPolygons(true);

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
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
        assertThat(indexResult.getName()).isEqualTo(name);
        if (isAtLeastVersion(3, 10)) {
            assertThat(indexResult.getLegacyPolygons()).isTrue();
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createGeo2Index(ArangoCollection collection) {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
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
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createGeo2IndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
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
        assertThat(indexResult.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createPersistentIndex(ArangoCollection collection) {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, null);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getDeduplicate()).isTrue();
        if (isAtLeastVersion(3, 10)) {
            assertThat(indexResult.getCacheEnabled()).isFalse();
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createPersistentIndexCacheEnabled(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, new PersistentIndexOptions().cacheEnabled(true));
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getDeduplicate()).isTrue();
        assertThat(indexResult.getCacheEnabled()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createPersistentIndexStoredValues(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 10));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, new PersistentIndexOptions().storedValues("v1", "v2"));
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getDeduplicate()).isTrue();
        assertThat(indexResult.getCacheEnabled()).isFalse();
        assertThat(indexResult.getStoredValues())
                .hasSize(2)
                .contains("v1", "v2");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createPersistentIndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getSparse()).isFalse();
        assertThat(indexResult.getType()).isEqualTo(IndexType.persistent);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createZKDIndex(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 9));
        collection.truncate();
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureZKDIndex(fields, null);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.zkd);
        assertThat(indexResult.getUnique()).isFalse();
        collection.deleteIndex(indexResult.getId());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createZKDIndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 9));
        collection.truncate();

        String name = "ZKDIndex-" + rnd();
        final ZKDIndexOptions options =
                new ZKDIndexOptions().name(name).fieldValueTypes(ZKDIndexOptions.FieldValueTypes.DOUBLE);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensureZKDIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getFields()).contains(f2);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.zkd);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo(name);
        collection.deleteIndex(indexResult.getId());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void indexEstimates(ArangoCollection collection) {
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
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getEstimates()).isTrue();
        assertThat(indexResult.getSelectivityEstimate()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void indexEstimatesFalse(ArangoCollection collection) {
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
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getEstimates()).isFalse();
        assertThat(indexResult.getSelectivityEstimate()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void indexDeduplicate(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(true);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void indexDeduplicateFalse(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(false);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createFulltextIndex(ArangoCollection collection) {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, null);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.fulltext);
        assertThat(indexResult.getUnique()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createFulltextIndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "fulltextIndex-" + rnd();
        final FulltextIndexOptions options = new FulltextIndexOptions();
        options.name(name);

        String f = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.fulltext);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createTtlIndexWithoutOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");

        Throwable thrown = catchThrowable(() -> collection.ensureTtlIndex(fields, null));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(10);
        assertThat(e.getMessage()).contains("expireAfter attribute must be a number");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void createTtlIndexWithOptions(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 5));

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);

        String name = "ttlIndex-" + rnd();
        final TtlIndexOptions options = new TtlIndexOptions();
        options.name(name);
        options.expireAfter(3600);

        final IndexEntity indexResult = collection.ensureTtlIndex(fields, options);
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.ttl);
        assertThat(indexResult.getExpireAfter()).isEqualTo(3600);
        assertThat(indexResult.getName()).isEqualTo(name);

        // revert changes
        collection.deleteIndex(indexResult.getId());
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getIndexes(ArangoCollection collection) {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        collection.ensurePersistentIndex(fields, null);
        long matchingIndexes =
                collection.getIndexes().stream().filter(i -> i.getType() == IndexType.persistent).filter(i -> i.getFields().contains(f1)).count();
        assertThat(matchingIndexes).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("edges")
    void getEdgeIndex(ArangoCollection edgeCollection) {
        Collection<IndexEntity> indexes = edgeCollection.getIndexes();
        long primaryIndexes = indexes.stream().filter(i -> i.getType() == IndexType.primary).count();
        long edgeIndexes = indexes.stream().filter(i -> i.getType() == IndexType.primary).count();
        assertThat(primaryIndexes).isEqualTo(1L);
        assertThat(edgeIndexes).isEqualTo(1L);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void exists(ArangoCollection collection) {
        assertThat(collection.exists()).isTrue();
        assertThat(collection.db().collection(COLLECTION_NAME + "no").exists()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void truncate(ArangoCollection collection) {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        collection.insertDocument(doc, null);
        final BaseDocument readResult = collection.getDocument(doc.getKey(), BaseDocument.class, null);
        assertThat(readResult.getKey()).isEqualTo(doc.getKey());
        final CollectionEntity truncateResult = collection.truncate();
        assertThat(truncateResult).isNotNull();
        assertThat(truncateResult.getId()).isNotNull();
        final BaseDocument document = collection.getDocument(doc.getKey(), BaseDocument.class, null);
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getCount(ArangoCollection collection) {
        Long initialCount = collection.count().getCount();
        collection.insertDocument(RawJson.of("{}"));
        final CollectionPropertiesEntity count = collection.count();
        assertThat(count.getCount()).isEqualTo(initialCount + 1L);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void documentExists(ArangoCollection collection) {
        final Boolean existsNot = collection.documentExists(rnd(), null);
        assertThat(existsNot).isFalse();

        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson);
        final Boolean exists = collection.documentExists(key, null);
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void documentExistsIfMatch(ArangoCollection collection) {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        final DocumentCreateEntity<?> createResult = collection.insertDocument(rawJson);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void documentExistsIfMatchFail(ArangoCollection collection) {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void documentExistsIfNoneMatch(ArangoCollection collection) {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void documentExistsIfNoneMatchFail(ArangoCollection collection) {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        final DocumentCreateEntity<?> createResult = collection.insertDocument(rawJson);
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options);
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(), new BaseDocument(),
                new BaseDocument());

        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsOverwriteModeUpdate(ArangoCollection collection) {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc1 = new BaseDocument(UUID.randomUUID().toString());
        doc1.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta1 = collection.insertDocument(doc1);

        final BaseDocument doc2 = new BaseDocument(UUID.randomUUID().toString());
        doc2.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta2 = collection.insertDocument(doc2);

        doc1.addAttribute("bar", "b");
        doc2.addAttribute("bar", "b");

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> repsert =
                collection.insertDocuments(Arrays.asList(doc1, doc2),
                        new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true), BaseDocument.class);
        assertThat(repsert).isNotNull();
        assertThat(repsert.getDocuments()).hasSize(2);
        assertThat(repsert.getErrors()).isEmpty();
        for (final DocumentCreateEntity<BaseDocument> documentCreateEntity : repsert.getDocuments()) {
            assertThat(documentCreateEntity.getRev()).isNotEqualTo(meta1.getRev());
            assertThat(documentCreateEntity.getRev()).isNotEqualTo(meta2.getRev());
            assertThat(documentCreateEntity.getNew().getAttribute("foo")).isEqualTo("a");
            assertThat(documentCreateEntity.getNew().getAttribute("bar")).isEqualTo("b");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsJson(ArangoCollection collection) {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{}"));
        values.add(RawJson.of("{}"));
        values.add(RawJson.of("{}"));
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsRawData(ArangoCollection collection) {
        final RawData values = RawJson.of("[{},{},{}]");
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsRawDataReturnNew(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"aaa\":33},{\"aaa\":33},{\"aaa\":33}]");
        final MultiDocumentEntity<DocumentCreateEntity<RawData>> docs =
                collection.insertDocuments(values, new DocumentCreateOptions().returnNew(true));
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();

        for (final DocumentCreateEntity<RawData> doc : docs.getDocuments()) {
            RawData d = doc.getNew();
            assertThat(d)
                    .isNotNull()
                    .isInstanceOf(RawJson.class);

            JsonNode jn = SerdeUtils.INSTANCE.parseJson(((RawJson) d).get());
            assertThat(jn.has("aaa")).isTrue();
            assertThat(jn.get("aaa").intValue()).isEqualTo(33);
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsOne(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(1);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsEmpty(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).isEmpty();
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsReturnNew(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection.insertDocuments(values,
                options, BaseDocument.class);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
        for (final DocumentCreateEntity<BaseDocument> doc : docs.getDocuments()) {
            assertThat(doc.getNew()).isNotNull();
            final BaseDocument baseDocument = doc.getNew();
            assertThat(baseDocument.getKey()).isNotNull();
        }

    }

    @ParameterizedTest
    @MethodSource("cols")
    void insertDocumentsFail(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final MultiDocumentEntity<?> docs = collection.insertDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(2);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).hasSize(1);
        assertThat(docs.getErrors().iterator().next().getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(), new BaseDocument(),
                new BaseDocument());

        final DocumentImportEntity docs = collection.importDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(values.size());
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonList(ArangoCollection collection) {
        final Collection<RawJson> values = Arrays.asList(
                RawJson.of("{}"),
                RawJson.of("{}"),
                RawJson.of("{}")
        );

        final DocumentImportEntity docs = collection.importDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(values.size());
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDuplicateDefaultError(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values);
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDuplicateError(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.error));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDuplicateIgnore(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isEqualTo(1);
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDuplicateReplace(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDuplicateUpdate(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.update));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsCompleteFail(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        Throwable thrown = catchThrowable(() -> collection.importDocuments(values,
                new DocumentImportOptions().complete(true)));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsDetails(ArangoCollection collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values, new DocumentImportOptions().details(true));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).hasSize(1);
        assertThat(docs.getDetails().iterator().next()).contains("unique constraint violated");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsOverwriteFalse(ArangoCollection collection) {
        collection.insertDocument(new BaseDocument());
        Long initialCount = collection.count().getCount();

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false));
        assertThat(collection.count().getCount()).isEqualTo(initialCount + 2L);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsOverwriteTrue(ArangoCollection collection) {
        collection.insertDocument(new BaseDocument());

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true));
        assertThat(collection.count().getCount()).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("edges")
    void importDocumentsFromToPrefix(ArangoCollection edgeCollection) {
        final Collection<BaseEdgeDocument> values = new ArrayList<>();
        final String[] keys = {rnd(), rnd()};
        for (String s : keys) {
            values.add(new BaseEdgeDocument(s, "from", "to"));
        }
        assertThat(values).hasSize(keys.length);

        final DocumentImportEntity importResult = edgeCollection.importDocuments(values,
                new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
        assertThat(importResult).isNotNull();
        assertThat(importResult.getCreated()).isEqualTo(values.size());
        for (String key : keys) {
            final BaseEdgeDocument doc = edgeCollection.getDocument(key, BaseEdgeDocument.class);
            assertThat(doc).isNotNull();
            assertThat(doc.getFrom()).isEqualTo("foo/from");
            assertThat(doc.getTo()).isEqualTo("bar/to");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJson(ArangoCollection collection) throws JsonProcessingException {
        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDuplicateDefaultError(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDuplicateError(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.error));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDuplicateIgnore(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));
        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.ignore));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isEqualTo(1);
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDuplicateReplace(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.replace));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDuplicateUpdate(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.update));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonCompleteFail(ArangoCollection collection) {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        Throwable thrown = catchThrowable(() -> collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().complete(true)));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonDetails(ArangoCollection collection) throws JsonProcessingException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().details(true));
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).hasSize(1);
        assertThat(docs.getDetails().iterator().next()).contains("unique constraint violated");
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonOverwriteFalse(ArangoCollection collection) throws JsonProcessingException {
        collection.insertDocument(new BaseDocument());
        Long initialCount = collection.count().getCount();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));
        collection.importDocuments(RawJson.of(values), new DocumentImportOptions().overwrite(false));
        assertThat(collection.count().getCount()).isEqualTo(initialCount + 2L);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void importDocumentsJsonOverwriteTrue(ArangoCollection collection) throws JsonProcessingException {
        collection.insertDocument(new BaseDocument());

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));
        collection.importDocuments(RawJson.of(values), new DocumentImportOptions().overwrite(true));
        assertThat(collection.count().getCount()).isEqualTo(2L);
    }

    @ParameterizedTest
    @MethodSource("edges")
    void importDocumentsJsonFromToPrefix(ArangoCollection edgeCollection) throws JsonProcessingException {
        String k1 = UUID.randomUUID().toString();
        String k2 = UUID.randomUUID().toString();

        final String[] keys = {k1, k2};

        final String values = mapper.writeValueAsString(Arrays.asList(new MapBuilder().put("_key", k1).put("_from",
                "from").put("_to", "to").get(), new MapBuilder().put("_key", k2).put("_from", "from").put("_to", "to").get()));

        final DocumentImportEntity importResult = edgeCollection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().fromPrefix("foo").toPrefix("bar"));
        assertThat(importResult).isNotNull();
        assertThat(importResult.getCreated()).isEqualTo(2);
        for (String key : keys) {
            final BaseEdgeDocument doc = edgeCollection.getDocument(key, BaseEdgeDocument.class);
            assertThat(doc).isNotNull();
            assertThat(doc.getFrom()).isEqualTo("foo/from");
            assertThat(doc.getTo()).isEqualTo("bar/to");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByKey(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("2");
            values.add(e);
        }
        collection.insertDocuments(values);
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(keys);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(2);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isIn("1", "2");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsRawDataByKeyReturnOld(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"_key\":\"1\"},{\"_key\":\"2\"}]");
        collection.insertDocuments(values);
        final RawData keys = RawJson.of("[\"1\",\"2\"]");
        MultiDocumentEntity<DocumentDeleteEntity<RawData>> deleteResult = collection.deleteDocuments(keys,
                new DocumentDeleteOptions().returnOld(true));
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(2);
        for (final DocumentDeleteEntity<RawData> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isIn("1", "2");
            assertThat(i.getOld()).isNotNull().isInstanceOf(RawJson.class);
            JsonNode jn = SerdeUtils.INSTANCE.parseJson(((RawJson) i.getOld()).get());
            assertThat(jn.get("_key").asText()).isEqualTo(i.getKey());
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("2");
            values.add(e);
        }
        collection.insertDocuments(values);
        MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(values);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(2);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isIn("1", "2");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByKeyOne(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values);
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(keys);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(1);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isEqualTo("1");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByDocumentOne(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values);
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(values);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(1);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isEqualTo("1");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsEmpty(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values);
        final Collection<String> keys = new ArrayList<>();
        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(keys);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByKeyNotExisting(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values);
        final Collection<String> keys = Arrays.asList(rnd(), rnd());

        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(keys);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void deleteDocumentsByDocumentsNotExisting(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("2");
            values.add(e);
        }
        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(values);
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(rnd()), new BaseDocument(rnd()));
        collection.insertDocuments(values);
        values.forEach(it -> it.addAttribute("a", "test"));

        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsWithDifferentReturnType(ArangoCollection collection) {
        List<String> keys =
                IntStream.range(0, 3).mapToObj(it -> "key-" + UUID.randomUUID()).collect(Collectors.toList());
        List<BaseDocument> docs =
                keys.stream().map(BaseDocument::new).peek(it -> it.addAttribute("a", "test")).collect(Collectors.toList());

        collection.insertDocuments(docs);

        List<Map<String, Object>> modifiedDocs = docs.stream().peek(it -> it.addAttribute("b", "test")).map(it -> {
            Map<String, Object> map = new HashMap<>();
            map.put("_key", it.getKey());
            map.put("a", it.getAttribute("a"));
            map.put("b", it.getAttribute("b"));
            return map;
        }).collect(Collectors.toList());

        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult =
                collection.updateDocuments(modifiedDocs, new DocumentUpdateOptions().returnNew(true), BaseDocument.class);
        assertThat(updateResult.getDocuments()).hasSize(3);
        assertThat(updateResult.getErrors()).isEmpty();
        assertThat(updateResult.getDocuments().stream()).map(DocumentUpdateEntity::getNew).allMatch(it -> it.getAttribute("a").equals("test")).allMatch(it -> it.getAttribute("b").equals("test"));
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsOne(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsEmpty(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values);
        assertThat(updateResult.getDocuments()).isEmpty();
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsWithoutKey(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
        }
        collection.insertDocuments(values);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsJson(ArangoCollection collection) {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{\"_key\":\"1\"}"));
        values.add(RawJson.of("{\"_key\":\"2\"}"));
        collection.insertDocuments(values);

        final Collection<RawJson> updatedValues = new ArrayList<>();
        updatedValues.add(RawJson.of("{\"_key\":\"1\", \"foo\":\"bar\"}"));
        updatedValues.add(RawJson.of("{\"_key\":\"2\", \"foo\":\"bar\"}"));
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsRawData(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void updateDocumentsRawDataReturnNew(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateResult =
                collection.updateDocuments(updatedValues, new DocumentUpdateOptions().returnNew(true));
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
        for (DocumentUpdateEntity<RawData> doc : updateResult.getDocuments()) {
            RawData d = doc.getNew();
            assertThat(d)
                    .isNotNull()
                    .isInstanceOf(RawJson.class);

            JsonNode jn = SerdeUtils.INSTANCE.parseJson(((RawJson) d).get());
            assertThat(jn.has("foo")).isTrue();
            assertThat(jn.get("foo").textValue()).isEqualTo("bar");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocuments(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
            values.add(new BaseDocument("2"));
        }
        collection.insertDocuments(values);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsOne(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsEmpty(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values);
        assertThat(updateResult.getDocuments()).isEmpty();
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsWithoutKey(ArangoCollection collection) {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
        }
        collection.insertDocuments(values);
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsJson(ArangoCollection collection) {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{\"_key\":\"1\"}"));
        values.add(RawJson.of("{\"_key\":\"2\"}"));
        collection.insertDocuments(values);

        final Collection<RawJson> updatedValues = new ArrayList<>();
        updatedValues.add(RawJson.of("{\"_key\":\"1\", \"foo\":\"bar\"}"));
        updatedValues.add(RawJson.of("{\"_key\":\"2\", \"foo\":\"bar\"}"));
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsRawData(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues);
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void replaceDocumentsRawDataReturnNew(ArangoCollection collection) {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateResult =
                collection.replaceDocuments(updatedValues, new DocumentReplaceOptions().returnNew(true));
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
        for (DocumentUpdateEntity<RawData> doc : updateResult.getDocuments()) {
            RawData d = doc.getNew();
            assertThat(d)
                    .isNotNull()
                    .isInstanceOf(RawJson.class);

            JsonNode jn = SerdeUtils.INSTANCE.parseJson(((RawJson) d).get());
            assertThat(jn.has("foo")).isTrue();
            assertThat(jn.get("foo").textValue()).isEqualTo("bar");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getInfo(ArangoCollection collection) {
        final CollectionEntity result = collection.getInfo();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getPropeties(ArangoCollection collection) {
        final CollectionPropertiesEntity result = collection.getProperties();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
        assertThat(result.getCount()).isNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void changeProperties(ArangoCollection collection) {
        final CollectionPropertiesEntity properties = collection.getProperties();
        assertThat(properties.getWaitForSync()).isNotNull();
        if (isAtLeastVersion(3, 7)) {
            assertThat(properties.getSchema()).isNull();
        }

        String schemaRule = ("{  " + "           \"properties\": {" + "               \"number\": {" + "             " +
                "      \"type\": \"number\"" + "               }" + "           }" + "       }").replaceAll("\\s", "");
        String schemaMessage = "The document has problems!";

        CollectionPropertiesOptions updatedOptions =
                new CollectionPropertiesOptions().waitForSync(!properties.getWaitForSync()).schema(new CollectionSchema().setLevel(CollectionSchema.Level.NEW).setMessage(schemaMessage).setRule(schemaRule));

        final CollectionPropertiesEntity changedProperties = collection.changeProperties(updatedOptions);
        assertThat(changedProperties.getWaitForSync()).isNotNull();
        assertThat(changedProperties.getWaitForSync()).isEqualTo(!properties.getWaitForSync());
        if (isAtLeastVersion(3, 7)) {
            assertThat(changedProperties.getSchema()).isNotNull();
            assertThat(changedProperties.getSchema().getLevel()).isEqualTo(CollectionSchema.Level.NEW);
            assertThat(changedProperties.getSchema().getMessage()).isEqualTo(schemaMessage);
            assertThat(changedProperties.getSchema().getRule()).isEqualTo(schemaRule);
        }

        // revert changes
        CollectionPropertiesEntity revertedProperties = collection.changeProperties(new CollectionPropertiesOptions()
                .waitForSync(properties.getWaitForSync()).schema(new CollectionSchema()));
        if (isAtLeastVersion(3, 7)) {
            assertThat(revertedProperties.getSchema()).isNull();
        }

    }

    @ParameterizedTest
    @MethodSource("cols")
    void rename(ArangoCollection collection) {
        assumeTrue(isSingleServer());
        ArangoDatabase db = collection.db();

        if (!db.collection("c1").exists()) {
            db.collection("c1").create();
        }

        if (db.collection("c2").exists()) {
            db.collection("c2").drop();
        }

        final CollectionEntity result = db.collection("c1").rename("c2");
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("c2");

        final CollectionEntity info = db.collection("c2").getInfo();
        assertThat(info.getName()).isEqualTo("c2");

        Throwable thrown = catchThrowable(() -> db.collection("c1").getInfo());
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void responsibleShard(ArangoCollection collection) {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 5));
        ShardEntity shard = collection.getResponsibleShard(new BaseDocument("testKey"));
        assertThat(shard).isNotNull();
        assertThat(shard.getShardId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getRevision(ArangoCollection collection) {
        final CollectionRevisionEntity result = collection.getRevision();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
        assertThat(result.getRevision()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("cols")
    void keyWithSpecialCharacter(ArangoCollection collection) {
        final String key = "myKey_-:.@()+,=;$!*'%-" + UUID.randomUUID();
        collection.insertDocument(new BaseDocument(key));
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void alreadyUrlEncodedkey(ArangoCollection collection) {
        final String key = "http%3A%2F%2Fexample.com%2F-" + UUID.randomUUID();
        collection.insertDocument(new BaseDocument(key));
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class);
        assertThat(doc).isNotNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void grantAccessRW(ArangoCollection collection) {
        ArangoDB arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.RW);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void grantAccessRO(ArangoCollection collection) {
        ArangoDB arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.RO);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void grantAccessNONE(ArangoCollection collection) {
        ArangoDB arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.NONE);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void grantAccessUserNotFound(ArangoCollection collection) {
        Throwable thrown = catchThrowable(() -> collection.grantAccess("user1", Permissions.RW));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void revokeAccess(ArangoCollection collection) {
        ArangoDB arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.grantAccess("user1", Permissions.NONE);
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void revokeAccessUserNotFound(ArangoCollection collection) {
        Throwable thrown = catchThrowable(() -> collection.grantAccess("user1", Permissions.NONE));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void resetAccess(ArangoCollection collection) {
        ArangoDB arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null);
            collection.resetAccess("user1");
        } finally {
            arangoDB.deleteUser("user1");
        }
    }

    @ParameterizedTest
    @MethodSource("cols")
    void resetAccessUserNotFound(ArangoCollection collection) {
        Throwable thrown = catchThrowable(() -> collection.resetAccess("user1"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void getPermissions(ArangoCollection collection) {
        assertThat(collection.getPermissions("root")).isEqualTo(Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("cols")
    void annotationsInParamsAndMethods(ArangoCollection collection) {
        assumeTrue(collection.getSerde().getUserSerde() instanceof JacksonSerde, "JacksonSerde only");
        AnnotatedEntity entity = new AnnotatedEntity(UUID.randomUUID().toString());
        AnnotatedEntity doc = collection.insertDocument(entity, new DocumentCreateOptions().returnNew(true)).getNew();
        assertThat(doc).isNotNull();
        assertThat(doc.getKey()).isEqualTo(entity.getKey());
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
    public interface Animal {
        String getKey();

        String getName();
    }

    public static class Dog implements Animal {

        @Key
        private String key;
        private String name;

        public Dog() {
        }

        public Dog(String key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Cat implements Animal {
        @Key
        private String key;
        private String name;

        public Cat() {
        }

        public Cat(String key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TestUpdateEntity {
        private String a, b;

        public String getA() {
            return a;
        }

        public String getB() {
            return b;
        }
    }

    public static class TestUpdateEntitySerializeNullFalse {
        private String a, b;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getA() {
            return a;
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getB() {
            return b;
        }
    }

    public static class AnnotatedEntity {

        private final String key;
        private String id;
        private String rev;

        public AnnotatedEntity(@Key String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getId() {
            return id;
        }

        @Id
        public void setId(String id) {
            this.id = id;
        }

        public String getRev() {
            return rev;
        }

        @Rev
        public void setRev(String rev) {
            this.rev = rev;
        }
    }

}

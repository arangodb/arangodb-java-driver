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
import com.arangodb.internal.serde.SerdeContextImpl;
import com.arangodb.internal.serde.SerdeUtils;
import com.arangodb.model.*;
import com.arangodb.model.DocumentImportOptions.OnDuplicate;
import com.arangodb.serde.SerdeContext;
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
import java.util.concurrent.ExecutionException;
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
class ArangoCollectionAsyncTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "ArangoCollectionTest_collection";
    private static final String EDGE_COLLECTION_NAME = "ArangoCollectionTest_edge_collection";

    private final ObjectMapper mapper = new ObjectMapper();

    private static Stream<Arguments> asyncCols() {
        return asyncDbsStream().map(mapNamedPayload(db -> db.collection(COLLECTION_NAME))).map(Arguments::of);
    }

    private static Stream<Arguments> edges() {
        return dbsStream().map(mapNamedPayload(db -> db.collection(EDGE_COLLECTION_NAME))).map(Arguments::of);
    }

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
        initEdgeCollections(EDGE_COLLECTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocument(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
        assertThat(doc.getId()).isEqualTo(COLLECTION_NAME + "/" + doc.getKey());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentWithArrayWithNullValues(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        List<String> arr = Arrays.asList("a", null);
        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("arr", arr);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection.insertDocument(doc,
                new DocumentCreateOptions().returnNew(true)).get();
        assertThat(insertedDoc).isNotNull();
        assertThat(insertedDoc.getId()).isNotNull();
        assertThat(insertedDoc.getKey()).isNotNull();
        assertThat(insertedDoc.getRev()).isNotNull();
        assertThat(insertedDoc.getId()).isEqualTo(COLLECTION_NAME + "/" + insertedDoc.getKey());
        //noinspection unchecked
        assertThat((List<String>) insertedDoc.getNew().getAttribute("arr")).containsAll(Arrays.asList("a", null));
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentWithNullValues(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("null", null);

        final DocumentCreateEntity<BaseDocument> insertedDoc = collection.insertDocument(doc,
                new DocumentCreateOptions().returnNew(true)).get();
        assertThat(insertedDoc).isNotNull();
        assertThat(insertedDoc.getId()).isNotNull();
        assertThat(insertedDoc.getKey()).isNotNull();
        assertThat(insertedDoc.getRev()).isNotNull();
        assertThat(insertedDoc.getId()).isEqualTo(COLLECTION_NAME + "/" + insertedDoc.getKey());
        assertThat(insertedDoc.getNew().getProperties()).containsKey("null");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentUpdateRev(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentWithTypeOverwriteModeReplace(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        collection.insertDocument(dog, options).get();
        final DocumentCreateEntity<Animal> doc = collection.insertDocument(cat, options, Animal.class).get();
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
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeIgnore(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc).get();

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> insertIgnore = collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.ignore)).get();

        assertThat(insertIgnore).isNotNull();
        assertThat(insertIgnore.getRev()).isEqualTo(meta.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeConflict(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        collection.insertDocument(doc).get();

        final BaseDocument doc2 = new BaseDocument(key);
        Throwable thrown = catchThrowable(() -> collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.conflict)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(409);
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeReplace(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc).get();

        final BaseDocument doc2 = new BaseDocument(key);
        doc2.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> repsert = collection.insertDocument(doc2,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.replace).returnNew(true)).get();

        assertThat(repsert).isNotNull();
        assertThat(repsert.getRev()).isNotEqualTo(meta.getRev());
        assertThat(repsert.getNew().getProperties().containsKey("foo")).isFalse();
        assertThat(repsert.getNew().getAttribute("bar")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdate(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc).get();

        doc.addAttribute("bar", "b");
        final DocumentCreateEntity<BaseDocument> updated = collection.insertDocument(doc,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true)).get();

        assertThat(updated).isNotNull();
        assertThat(updated.getRev()).isNotEqualTo(meta.getRev());
        assertThat(updated.getNew().getAttribute("foo")).isEqualTo("a");
        assertThat(updated.getNew().getAttribute("bar")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdateMergeObjectsFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        Map<String, String> fieldA = Collections.singletonMap("a", "a");
        doc.addAttribute("foo", fieldA);
        final DocumentCreateEntity<?> meta = collection.insertDocument(doc).get();

        Map<String, String> fieldB = Collections.singletonMap("b", "b");
        doc.addAttribute("foo", fieldB);
        final DocumentCreateEntity<BaseDocument> updated = collection.insertDocument(doc,
                new DocumentCreateOptions().overwriteMode(OverwriteMode.update).mergeObjects(false).returnNew(true)).get();

        assertThat(updated).isNotNull();
        assertThat(updated.getRev()).isNotEqualTo(meta.getRev());
        assertThat(updated.getNew().getAttribute("foo")).isEqualTo(fieldB);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdateKeepNullTrue(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "bar");
        collection.insertDocument(doc).get();

        doc.updateAttribute("foo", null);
        final BaseDocument updated = collection.insertDocument(doc, new DocumentCreateOptions()
                .overwriteMode(OverwriteMode.update)
                .keepNull(true)
                .returnNew(true)).get().getNew();

        assertThat(updated.getProperties()).containsEntry("foo", null);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdateKeepNullFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("foo", "bar");
        collection.insertDocument(doc).get();

        doc.updateAttribute("foo", null);
        final BaseDocument updated = collection.insertDocument(doc, new DocumentCreateOptions()
                .overwriteMode(OverwriteMode.update)
                .keepNull(false)
                .returnNew(true)).get().getNew();

        assertThat(updated.getProperties()).doesNotContainKey("foo");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdateWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 2);
        DocumentCreateEntity<BaseDocument> updateResult = collection.insertDocument(
                doc,
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.update)
                        .versionAttribute("_version")
                        .returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeUpdateWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 0);
        DocumentCreateEntity<BaseDocument> updateResult = collection.insertDocument(
                doc,
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.update)
                        .versionAttribute("_version")
                        .returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(1);

    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsOverwriteModeUpdateWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 2);
        d2.addAttribute("_version", 2);
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> updateResult = collection.insertDocuments(
                Arrays.asList(d1, d2),
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.update)
                        .versionAttribute("_version")
                        .returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(2);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsOverwriteModeUpdateWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 0);
        d2.addAttribute("_version", 0);
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> updateResult = collection.insertDocuments(
                Arrays.asList(d1, d2),
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.update)
                        .versionAttribute("_version")
                        .returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(1);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeReplaceWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 2);
        DocumentCreateEntity<BaseDocument> updateResult = collection.insertDocument(
                doc,
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.replace)
                        .versionAttribute("_version")
                        .returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentOverwriteModeReplaceUpdateWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 0);
        DocumentCreateEntity<BaseDocument> updateResult = collection.insertDocument(
                doc,
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.replace)
                        .versionAttribute("_version")
                        .returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(1);

    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsOverwriteModeReplaceWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 2);
        d2.addAttribute("_version", 2);
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> updateResult = collection.insertDocuments(
                Arrays.asList(d1, d2),
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.replace)
                        .versionAttribute("_version")
                        .returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(2);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsOverwriteModeReplaceWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 0);
        d2.addAttribute("_version", 0);
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> updateResult = collection.insertDocuments(
                Arrays.asList(d1, d2),
                new DocumentCreateOptions()
                        .overwriteMode(OverwriteMode.replace)
                        .versionAttribute("_version")
                        .returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(1);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentWaitForSync(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateOptions options = new DocumentCreateOptions().waitForSync(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateOptions options = new DocumentCreateOptions().refillIndexCaches(true);
        final DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument(), options).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNotNull();
        assertThat(doc.getKey()).isNotNull();
        assertThat(doc.getRev()).isNotNull();
        assertThat(doc.getNew()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentAsJson(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = "doc-" + UUID.randomUUID();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\",\"a\":\"test\"}");
        final DocumentCreateEntity<?> doc = collection.insertDocument(rawJson).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo(collection.name() + "/" + key);
        assertThat(doc.getKey()).isEqualTo(key);
        assertThat(doc.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentAsBytes(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = "doc-" + UUID.randomUUID();
        Map<String, Object> doc = new HashMap<>();
        doc.put("_key", key);
        doc.put("a", "test");
        byte[] bytes = collection.getSerde().serializeUserData(doc);
        RawBytes rawJson = RawBytes.of(bytes);
        final DocumentCreateEntity<RawBytes> createEntity = collection.insertDocument(rawJson,
                new DocumentCreateOptions().returnNew(true)).get();
        assertThat(createEntity).isNotNull();
        assertThat(createEntity.getId()).isEqualTo(collection.name() + "/" + key);
        assertThat(createEntity.getKey()).isEqualTo(key);
        assertThat(createEntity.getRev()).isNotNull();
        assertThat(createEntity.getNew()).isNotNull().isInstanceOf(RawBytes.class);
        Map<String, Object> newDoc = collection.getSerde().deserializeUserData(createEntity.getNew().get(),
                Map.class, SerdeContext.EMPTY);
        assertThat(newDoc).containsAllEntriesOf(doc);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(new BaseDocument(),
                new DocumentCreateOptions().silent(true)).get();
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentSilentDontTouchInstance(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final String key = "testkey-" + UUID.randomUUID();
        doc.setKey(key);
        final DocumentCreateEntity<BaseDocument> meta = collection.insertDocument(doc,
                new DocumentCreateOptions().silent(true)).get();
        assertThat(meta).isNotNull();
        assertThat(meta.getKey()).isNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> info =
                collection.insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().silent(true), BaseDocument.class).get();
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> info =
                collection.insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().refillIndexCaches(true), BaseDocument.class).get();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocument(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentIfMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch(createResult.getRev());
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, options).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentIfMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifMatch("no");
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentIfNoneMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch("no");
        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, options).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(COLLECTION_NAME + "/" + createResult.getKey());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentIfNoneMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(new BaseDocument(), null).get();
        assertThat(createResult.getKey()).isNotNull();
        final DocumentReadOptions options = new DocumentReadOptions().ifNoneMatch(createResult.getRev());
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, options).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentAsJson(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\",\"a\":\"test\"}");
        collection.insertDocument(rawJson).get();
        final RawJson readResult = collection.getDocument(key, RawJson.class).get();
        assertThat(readResult.get()).contains("\"_key\":\"" + key + "\"").contains("\"_id\":\"" + COLLECTION_NAME + "/" + key + "\"");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentNotFound(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentNotFoundOptionsDefault(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class, new DocumentReadOptions()).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentNotFoundOptionsNull(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument document = collection.getDocument("no", BaseDocument.class, null).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentWrongKey(ArangoCollectionAsync collection) {
        Throwable thrown = catchThrowable(() -> collection.getDocument("no/no", BaseDocument.class).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @SlowTest
    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentDirtyRead(ArangoCollectionAsync collection) throws InterruptedException, ExecutionException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        collection.insertDocument(doc, new DocumentCreateOptions());
        Thread.sleep(2000);
        final RawJson document = collection.getDocument(doc.getKey(), RawJson.class,
                new DocumentReadOptions().allowDirtyRead(true)).get();
        assertThat(document).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values).get();
        final MultiDocumentEntity<BaseDocument> documents = collection.getDocuments(Arrays.asList("1", "2", "3"),
                BaseDocument.class).get();
        assertThat(documents).isNotNull();
        assertThat(documents.getDocuments()).hasSize(3);
        for (final BaseDocument document : documents.getDocuments()) {
            assertThat(document.getId()).isIn(COLLECTION_NAME + "/" + "1", COLLECTION_NAME + "/" + "2",
                    COLLECTION_NAME + "/" + "3");
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentsWithCustomShardingKey(ArangoCollectionAsync c) throws ExecutionException, InterruptedException {
        ArangoCollectionAsync collection = c.db().collection("customShardingKeyCollection");
        if (collection.exists().get()) collection.drop().get();

        collection.create(new CollectionCreateOptions().shardKeys("customField").numberOfShards(10)).get();

        List<BaseDocument> values =
                IntStream.range(0, 10).mapToObj(String::valueOf).map(key -> new BaseDocument()).peek(it -> it.addAttribute(
                        "customField", rnd())).collect(Collectors.toList());

        MultiDocumentEntity<DocumentCreateEntity<Void>> inserted = collection.insertDocuments(values).get();
        List<String> insertedKeys =
                inserted.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        final Collection<BaseDocument> documents =
                collection.getDocuments(insertedKeys, BaseDocument.class).get().getDocuments();

        assertThat(documents).hasSize(10);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentsDirtyRead(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument("1"));
        values.add(new BaseDocument("2"));
        values.add(new BaseDocument("3"));
        collection.insertDocuments(values).get();
        final MultiDocumentEntity<BaseDocument> documents = collection.getDocuments(Arrays.asList("1", "2", "3"),
                BaseDocument.class, new DocumentReadOptions().allowDirtyRead(true)).get();
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
    @MethodSource("asyncCols")
    void getDocumentsNotFound(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final MultiDocumentEntity<BaseDocument> readResult = collection.getDocuments(Collections.singleton("no"),
                BaseDocument.class).get();
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getDocumentsWrongKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final MultiDocumentEntity<BaseDocument> readResult = collection.getDocuments(Collections.singleton("no/no"),
                BaseDocument.class).get();
        assertThat(readResult).isNotNull();
        assertThat(readResult.getDocuments()).isEmpty();
        assertThat(readResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocument(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                null).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getNew()).isNull();
        assertThat(updateResult.getOld()).isNull();
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentWithDifferentReturnType(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final String key = "key-" + UUID.randomUUID();
        final BaseDocument doc = new BaseDocument(key);
        doc.addAttribute("a", "test");
        collection.insertDocument(doc).get();

        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(key,
                Collections.singletonMap("b", "test"), new DocumentUpdateOptions().returnNew(true), BaseDocument.class).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(key);
        BaseDocument updated = updateResult.getNew();
        assertThat(updated).isNotNull();
        assertThat(updated.getAttribute("a")).isEqualTo("test");
        assertThat(updated.getAttribute("b")).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentUpdateRev(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.addAttribute("foo", "bar");
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentIfMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getAttribute("a")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("a"))).isEqualTo("test1");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
        assertThat(readResult.getRevision()).isEqualTo(updateResult.getRev());
        assertThat(readResult.getProperties()).containsKey("c");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentIfMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        doc.updateAttribute("c", null);

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.updateDocument(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 2);
        DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(
                doc.getKey(),
                doc,
                new DocumentUpdateOptions().versionAttribute("_version").returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 0);
        DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(
                doc.getKey(),
                doc,
                new DocumentUpdateOptions().versionAttribute("_version").returnNew(true)
        ).get();
        assertThat(updateResult.getNew().getAttribute("_version")).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 2);
        d2.addAttribute("_version", 2);
        MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection.updateDocuments(
                Arrays.asList(d1, d2),
                new DocumentUpdateOptions().versionAttribute("_version").returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(2);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 0);
        d2.addAttribute("_version", 0);
        MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult = collection.updateDocuments(
                Arrays.asList(d1, d2),
                new DocumentUpdateOptions().versionAttribute("_version").returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(updateResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(1);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
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
    @MethodSource("asyncCols")
    void updateDocumentReturnOld(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.addAttribute("b", "test");
        final DocumentUpdateOptions options = new DocumentUpdateOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
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
    @MethodSource("asyncCols")
    void updateDocumentKeepNullTrue(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKey("a");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentKeepNullFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", null);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().keepNull(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getRevision()).isNotNull();
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentSerializeNullTrue(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final TestUpdateEntity doc = new TestUpdateEntity();
        doc.a = "foo";
        doc.b = "foo";
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        final TestUpdateEntity patchDoc = new TestUpdateEntity();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<?> updateResult = collection.updateDocument(createResult.getKey(), patchDoc).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(createResult.getKey());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKey("a");
        assertThat(readResult.getAttribute("a")).isEqualTo("bar");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentSerializeNullFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final TestUpdateEntitySerializeNullFalse doc = new TestUpdateEntitySerializeNullFalse();
        doc.a = "foo";
        doc.b = "foo";
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        final TestUpdateEntitySerializeNullFalse patchDoc = new TestUpdateEntitySerializeNullFalse();
        patchDoc.a = "bar";
        final DocumentUpdateEntity<?> updateResult = collection.updateDocument(createResult.getKey(), patchDoc).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getKey()).isEqualTo(createResult.getKey());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getProperties()).containsKeys("a", "b");
        assertThat(readResult.getAttribute("a")).isEqualTo("bar");
        assertThat(readResult.getAttribute("b")).isEqualTo("foo");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentMergeObjectsTrue(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(true);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap).containsKeys("a", "b");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentMergeObjectsFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final Map<String, String> a = new HashMap<>();
        a.put("a", "test");
        doc.addAttribute("a", a);
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        a.clear();
        a.put("b", "test");
        doc.updateAttribute("a", a);
        final DocumentUpdateOptions options = new DocumentUpdateOptions().mergeObjects(false);
        final DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(), doc,
                options).get();
        assertThat(updateResult).isNotNull();
        assertThat(updateResult.getId()).isEqualTo(createResult.getId());
        assertThat(updateResult.getRev()).isNotEqualTo(updateResult.getOldRev());
        assertThat(updateResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        final Object aResult = readResult.getAttribute("a");
        assertThat(aResult).isInstanceOf(Map.class);
        final Map<String, String> aMap = (Map<String, String>) aResult;
        assertThat(aMap.keySet()).doesNotContain("a");
        assertThat(aMap).containsKey("b");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentIgnoreRevsFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.updateAttribute("a", "test1");
        doc.setRevision("no");

        final DocumentUpdateOptions options = new DocumentUpdateOptions().ignoreRevs(false);
        Throwable thrown = catchThrowable(() -> collection.updateDocument(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final DocumentUpdateEntity<BaseDocument> meta = collection.updateDocument(createResult.getKey(),
                new BaseDocument(), new DocumentUpdateOptions().silent(true)).get();
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.updateDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentUpdateOptions().silent(true), BaseDocument.class).get();
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateNonExistingDocument(ArangoCollectionAsync collection) {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("a", "test");
        doc.addAttribute("c", "test");

        Throwable thrown = catchThrowable(() -> collection.updateDocument(doc.getKey(), doc, null).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
        assertThat(e.getErrorNum()).isEqualTo(1202);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentPreconditionFailed(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument("test-" + rnd());
        doc.addAttribute("foo", "a");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();

        doc.updateAttribute("foo", "b");
        collection.updateDocument(doc.getKey(), doc, null).get();

        doc.updateAttribute("foo", "c");
        Throwable thrown = catchThrowable(() -> collection.updateDocument(doc.getKey(), doc,
                new DocumentUpdateOptions().ifMatch(createResult.getRev())).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(412);
        assertThat(e.getErrorNum()).isEqualTo(1200);
        BaseDocument readDocument = collection.getDocument(doc.getKey(), BaseDocument.class).get();
        assertThat(readDocument.getAttribute("foo")).isEqualTo("b");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        BaseDocument doc = new BaseDocument();
        DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        doc.addAttribute("foo", "bar");
        DocumentUpdateEntity<BaseDocument> updateResult = collection.updateDocument(createResult.getKey(),
                doc, new DocumentUpdateOptions().refillIndexCaches(true)).get();
        assertThat(updateResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.updateDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentUpdateOptions().refillIndexCaches(true), BaseDocument.class).get();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocument(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, null).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getNew()).isNull();
        assertThat(replaceResult.getOld()).isNull();
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentUpdateRev(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, null).get();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentIfMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch(createResult.getRev());
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options).get();
        assertThat(replaceResult).isNotNull();
        assertThat(replaceResult.getId()).isEqualTo(createResult.getId());
        assertThat(replaceResult.getRev()).isNotEqualTo(replaceResult.getOldRev());
        assertThat(replaceResult.getOldRev()).isEqualTo(createResult.getRev());

        final BaseDocument readResult = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(createResult.getKey());
        assertThat(readResult.getRevision()).isEqualTo(replaceResult.getRev());
        assertThat(readResult.getProperties().keySet()).doesNotContain("a");
        assertThat(readResult.getAttribute("b")).isNotNull();
        assertThat(String.valueOf(readResult.getAttribute("b"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentIfMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.replaceDocument(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);

    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentIgnoreRevsFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        doc.setRevision("no");

        final DocumentReplaceOptions options = new DocumentReplaceOptions().ignoreRevs(false);
        Throwable thrown = catchThrowable(() -> collection.replaceDocument(createResult.getKey(), doc, options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 2);
        DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(
                doc.getKey(),
                doc,
                new DocumentReplaceOptions().versionAttribute("_version").returnNew(true)
        ).get();
        assertThat(replaceResult.getNew().getAttribute("_version")).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("_version", 1);
        collection.insertDocument(doc).get();
        doc.addAttribute("_version", 0);
        DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(
                doc.getKey(),
                doc,
                new DocumentReplaceOptions().versionAttribute("_version").returnNew(true)
        ).get();
        assertThat(replaceResult.getNew().getAttribute("_version")).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsWithExternalVersioning(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 2);
        d2.addAttribute("_version", 2);
        MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> replaceResult = collection.replaceDocuments(
                Arrays.asList(d1, d2),
                new DocumentReplaceOptions().versionAttribute("_version").returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(replaceResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(2);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsWithExternalVersioningFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));

        BaseDocument d1 = new BaseDocument(UUID.randomUUID().toString());
        d1.addAttribute("_version", 1);
        BaseDocument d2 = new BaseDocument(UUID.randomUUID().toString());
        d2.addAttribute("_version", 1);

        collection.insertDocuments(Arrays.asList(d1, d2)).get();

        d1.addAttribute("_version", 0);
        d2.addAttribute("_version", 0);
        MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> replaceResult = collection.replaceDocuments(
                Arrays.asList(d1, d2),
                new DocumentReplaceOptions().versionAttribute("_version").returnNew(true),
                BaseDocument.class
        ).get();

        assertThat(replaceResult.getDocuments()).allSatisfy(it -> {
            assertThat(it.getNew()).isNotNull();
            assertThat(it.getNew().getAttribute("_version")).isEqualTo(1);
        });
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnNew(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options).get();
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
    @MethodSource("asyncCols")
    void replaceDocumentReturnOld(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        doc.removeAttribute("a");
        doc.addAttribute("b", "test");
        final DocumentReplaceOptions options = new DocumentReplaceOptions().returnOld(true);
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(),
                doc, options).get();
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
    @MethodSource("asyncCols")
    void replaceDocumentSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final DocumentUpdateEntity<BaseDocument> meta = collection.replaceDocument(createResult.getKey(),
                new BaseDocument(), new DocumentReplaceOptions().silent(true)).get();
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentSilentDontTouchInstance(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        final DocumentUpdateEntity<BaseDocument> meta = collection.replaceDocument(createResult.getKey(), doc,
                new DocumentReplaceOptions().silent(true)).get();
        assertThat(meta.getRev()).isNull();
        assertThat(doc.getRevision()).isNull();
        assertThat(createResult.getRev()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.replaceDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentReplaceOptions().silent(true), BaseDocument.class).get();
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        final DocumentUpdateEntity<BaseDocument> replaceResult = collection.replaceDocument(createResult.getKey(), doc,
                new DocumentReplaceOptions().refillIndexCaches(true)).get();
        assertThat(replaceResult.getRev())
                .isNotNull()
                .isNotEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> info =
                collection.replaceDocuments(Collections.singletonList(new BaseDocument(createResult.getKey())),
                        new DocumentReplaceOptions().refillIndexCaches(true), BaseDocument.class).get();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocument(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        collection.deleteDocument(createResult.getKey()).get();
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentReturnOld(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("a", "test");
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().returnOld(true);
        final DocumentDeleteEntity<BaseDocument> deleteResult = collection.deleteDocument(createResult.getKey(),
                options, BaseDocument.class).get();
        assertThat(deleteResult.getOld()).isNotNull();
        assertThat(deleteResult.getOld()).isInstanceOf(BaseDocument.class);
        assertThat(deleteResult.getOld().getAttribute("a")).isNotNull();
        assertThat(String.valueOf(deleteResult.getOld().getAttribute("a"))).isEqualTo("test");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentIfMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<BaseDocument> createResult = collection.insertDocument(doc, null).get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch(createResult.getRev());
        collection.deleteDocument(createResult.getKey(), options).get();
        final BaseDocument document = collection.getDocument(createResult.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentIfMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(doc).get();
        final DocumentDeleteOptions options = new DocumentDeleteOptions().ifMatch("no");
        Throwable thrown = catchThrowable(() -> collection.deleteDocument(createResult.getKey(), options).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final DocumentDeleteEntity<BaseDocument> meta = collection.deleteDocument(createResult.getKey(),
                new DocumentDeleteOptions().silent(true), BaseDocument.class).get();
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isNull();
        assertThat(meta.getKey()).isNull();
        assertThat(meta.getRev()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsSilent(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentDeleteEntity<BaseDocument>> info = collection.deleteDocuments(
                Collections.singletonList(createResult.getKey()),
                new DocumentDeleteOptions().silent(true),
                BaseDocument.class).get();
        assertThat(info).isNotNull();
        assertThat(info.getDocuments()).isEmpty();
        assertThat(info.getDocumentsAndErrors()).isEmpty();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        DocumentDeleteEntity<?> deleteResult = collection.deleteDocument(createResult.getKey(),
                new DocumentDeleteOptions().refillIndexCaches(true)).get();
        assertThat(deleteResult.getRev())
                .isNotNull()
                .isEqualTo(createResult.getRev());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsRefillIndexCaches(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        final DocumentCreateEntity<?> createResult = collection.insertDocument(new BaseDocument()).get();
        final MultiDocumentEntity<DocumentDeleteEntity<BaseDocument>> info = collection.deleteDocuments(
                Collections.singletonList(createResult.getKey()),
                new DocumentDeleteOptions().refillIndexCaches(true),
                BaseDocument.class).get();
        assertThat(info.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null).get();
        final IndexEntity readResult = collection.getIndex(createResult.getId()).get();
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getIndexByKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null).get();
        final IndexEntity readResult = collection.getIndex(createResult.getId().split("/")[1]).get();
        assertThat(readResult.getId()).isEqualTo(createResult.getId());
        assertThat(readResult.getType()).isEqualTo(createResult.getType());
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null).get();
        final String id = collection.deleteIndex(createResult.getId()).get();
        assertThat(id).isEqualTo(createResult.getId());
        Throwable thrown = catchThrowable(() -> collection.db().getIndex(id).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteIndexByKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");
        final IndexEntity createResult = collection.ensurePersistentIndex(fields, null).get();
        final String id = collection.deleteIndex(createResult.getId().split("/")[1]).get();
        assertThat(id).isEqualTo(createResult.getId());
        Throwable thrown = catchThrowable(() -> collection.db().getIndex(id).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createGeoIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null).get();
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
    @MethodSource("asyncCols")
    void createGeoIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options).get();
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
    @MethodSource("asyncCols")
    void createGeoIndexLegacyPolygons(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);
        options.legacyPolygons(true);

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options).get();
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
    @MethodSource("asyncCols")
    void createGeo2Index(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, null).get();
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
    @MethodSource("asyncCols")
    void createGeo2IndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "geoIndex-" + rnd();
        final GeoIndexOptions options = new GeoIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureGeoIndex(fields, options).get();
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
    @MethodSource("asyncCols")
    void createPersistentIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, null).get();
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
    @MethodSource("asyncCols")
    void createPersistentIndexCacheEnabled(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, new PersistentIndexOptions().cacheEnabled(true)).get();
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
    @MethodSource("asyncCols")
    void createPersistentIndexStoredValues(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 10));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, new PersistentIndexOptions().storedValues("v1", "v2")).get();
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
    @MethodSource("asyncCols")
    void createPersistentIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options).get();
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
    @MethodSource("asyncCols")
    void createZKDIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 9));
        collection.truncate().get();
        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();
        final Collection<String> fields = Arrays.asList(f1, f2);

        final IndexEntity indexResult = collection.ensureZKDIndex(fields, null).get();
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
    @MethodSource("asyncCols")
    void createZKDIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 9));
        collection.truncate().get();

        String name = "ZKDIndex-" + rnd();
        final ZKDIndexOptions options =
                new ZKDIndexOptions().name(name).fieldValueTypes(ZKDIndexOptions.FieldValueTypes.DOUBLE);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensureZKDIndex(fields, options).get();
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
        collection.deleteIndex(indexResult.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createMDIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));
        collection.truncate().get();

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final IndexEntity indexResult = collection.ensureMDIndex(Arrays.asList(f1, f2), null).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getConstraint()).isNull();
        assertThat(indexResult.getFields()).contains(f1, f2);
        assertThat(indexResult.getFieldValueTypes()).isEqualTo(MDIFieldValueTypes.DOUBLE);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getMinLength()).isNull();
        assertThat(indexResult.getType()).isEqualTo(IndexType.mdi);
        assertThat(indexResult.getUnique()).isFalse();
        collection.deleteIndex(indexResult.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createMDIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));
        collection.truncate().get();

        String name = "MDIndex-" + rnd();
        final MDIndexOptions options = new MDIndexOptions()
                .name(name)
                .unique(false)
                .fieldValueTypes(MDIFieldValueTypes.DOUBLE)
                .estimates(false)
                .sparse(true)
                .storedValues(Arrays.asList("v1", "v2"));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final IndexEntity indexResult = collection.ensureMDIndex(Arrays.asList(f1, f2), options).get();
        assertThat(indexResult.getType()).isEqualTo(IndexType.mdi);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getName()).isEqualTo(name);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getEstimates()).isFalse();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getStoredValues())
                .hasSize(2)
                .contains("v1", "v2");
        assertThat(indexResult.getFields()).contains(f1, f2);
        assertThat(indexResult.getFieldValueTypes()).isEqualTo(MDIFieldValueTypes.DOUBLE);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        collection.deleteIndex(indexResult.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createMDPrefixedIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 12));
        collection.truncate().get();

        String name = "MDPrefixedIndex-" + rnd();
        final MDPrefixedIndexOptions options = new MDPrefixedIndexOptions()
                .name(name)
                .unique(false)
                .fieldValueTypes(MDIFieldValueTypes.DOUBLE)
                .estimates(false)
                .sparse(true)
                .storedValues(Arrays.asList("v1", "v2"))
                .prefixFields(Arrays.asList("p1", "p2"));

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final IndexEntity indexResult = collection.ensureMDPrefixedIndex(Arrays.asList(f1, f2), options).get();
        assertThat(indexResult.getType()).isEqualTo(IndexType.mdiPrefixed);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getName()).isEqualTo(name);
        assertThat(indexResult.getUnique()).isFalse();
        assertThat(indexResult.getEstimates()).isFalse();
        assertThat(indexResult.getSparse()).isTrue();
        assertThat(indexResult.getStoredValues())
                .hasSize(2)
                .contains("v1", "v2");
        assertThat(indexResult.getFields()).contains(f1, f2);
        assertThat(indexResult.getFieldValueTypes()).isEqualTo(MDIFieldValueTypes.DOUBLE);
        assertThat(indexResult.getPrefixFields()).contains("p1", "p2");
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        collection.deleteIndex(indexResult.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void indexEstimates(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        assumeTrue(isSingleServer());

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.estimates(true);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getEstimates()).isTrue();
        assertThat(indexResult.getSelectivityEstimate()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void indexEstimatesFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));
        assumeTrue(isSingleServer());

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.estimates(false);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getEstimates()).isFalse();
        assertThat(indexResult.getSelectivityEstimate()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void indexDeduplicate(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(true);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void indexDeduplicateFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 8));

        String name = "persistentIndex-" + rnd();
        final PersistentIndexOptions options = new PersistentIndexOptions();
        options.name(name);
        options.deduplicate(false);

        String f1 = "field-" + rnd();
        String f2 = "field-" + rnd();

        final Collection<String> fields = Arrays.asList(f1, f2);
        final IndexEntity indexResult = collection.ensurePersistentIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getDeduplicate()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createFulltextIndex(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, null).get();
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
    @MethodSource("asyncCols")
    void createFulltextIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String name = "fulltextIndex-" + rnd();
        final FulltextIndexOptions options = new FulltextIndexOptions();
        options.name(name);

        String f = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f);
        final IndexEntity indexResult = collection.ensureFulltextIndex(fields, options).get();
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
    @MethodSource("asyncCols")
    void createTtlIndexWithoutOptions(ArangoCollectionAsync collection) {
        assumeTrue(isAtLeastVersion(3, 5));
        final Collection<String> fields = new ArrayList<>();
        fields.add("a");

        Throwable thrown = catchThrowable(() -> collection.ensureTtlIndex(fields, null).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(10);
        assertThat(e.getMessage()).contains("expireAfter attribute must be a number");
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void createTtlIndexWithOptions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 5));

        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);

        String name = "ttlIndex-" + rnd();
        final TtlIndexOptions options = new TtlIndexOptions();
        options.name(name);
        options.expireAfter(3600);

        final IndexEntity indexResult = collection.ensureTtlIndex(fields, options).get();
        assertThat(indexResult).isNotNull();
        assertThat(indexResult.getFields()).contains(f1);
        assertThat(indexResult.getId()).startsWith(COLLECTION_NAME);
        assertThat(indexResult.getIsNewlyCreated()).isTrue();
        assertThat(indexResult.getType()).isEqualTo(IndexType.ttl);
        assertThat(indexResult.getExpireAfter()).isEqualTo(3600);
        assertThat(indexResult.getName()).isEqualTo(name);

        // revert changes
        collection.deleteIndex(indexResult.getId()).get();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getIndexes(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String f1 = "field-" + rnd();
        final Collection<String> fields = Collections.singletonList(f1);
        collection.ensurePersistentIndex(fields, null).get();
        long matchingIndexes =
                collection.getIndexes().get().stream().filter(i -> i.getType() == IndexType.persistent).filter(i -> i.getFields().contains(f1)).count();
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
    @MethodSource("asyncCols")
    void exists(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assertThat(collection.exists().get()).isTrue();
        assertThat(collection.db().collection(COLLECTION_NAME + "no").exists().get()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void truncate(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        collection.insertDocument(doc, null).get();
        final BaseDocument readResult = collection.getDocument(doc.getKey(), BaseDocument.class, null).get();
        assertThat(readResult.getKey()).isEqualTo(doc.getKey());
        final CollectionEntity truncateResult = collection.truncate().get();
        assertThat(truncateResult).isNotNull();
        assertThat(truncateResult.getId()).isNotNull();
        final BaseDocument document = collection.getDocument(doc.getKey(), BaseDocument.class, null).get();
        assertThat(document).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getCount(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        Long initialCount = collection.count().get().getCount();
        collection.insertDocument(RawJson.of("{}")).get();
        final CollectionPropertiesEntity count = collection.count().get();
        assertThat(count.getCount()).isEqualTo(initialCount + 1L);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void documentExists(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Boolean existsNot = collection.documentExists(rnd(), null).get();
        assertThat(existsNot).isFalse();

        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson).get();
        final Boolean exists = collection.documentExists(key, null).get();
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void documentExistsIfMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        final DocumentCreateEntity<?> createResult = collection.insertDocument(rawJson).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options).get();
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void documentExistsIfMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifMatch("no");
        final Boolean exists = collection.documentExists(key, options).get();
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void documentExistsIfNoneMatch(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        collection.insertDocument(rawJson).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch("no");
        final Boolean exists = collection.documentExists(key, options).get();
        assertThat(exists).isTrue();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void documentExistsIfNoneMatchFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String key = rnd();
        RawJson rawJson = RawJson.of("{\"_key\":\"" + key + "\"}");
        final DocumentCreateEntity<?> createResult = collection.insertDocument(rawJson).get();
        final DocumentExistsOptions options = new DocumentExistsOptions().ifNoneMatch(createResult.getRev());
        final Boolean exists = collection.documentExists(key, options).get();
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(), new BaseDocument(),
                new BaseDocument());

        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsOverwriteModeUpdate(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));

        final BaseDocument doc1 = new BaseDocument(UUID.randomUUID().toString());
        doc1.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta1 = collection.insertDocument(doc1).get();

        final BaseDocument doc2 = new BaseDocument(UUID.randomUUID().toString());
        doc2.addAttribute("foo", "a");
        final DocumentCreateEntity<?> meta2 = collection.insertDocument(doc2).get();

        doc1.addAttribute("bar", "b");
        doc2.addAttribute("bar", "b");

        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> repsert =
                collection.insertDocuments(Arrays.asList(doc1, doc2),
                        new DocumentCreateOptions().overwriteMode(OverwriteMode.update).returnNew(true), BaseDocument.class).get();
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
    @MethodSource("asyncCols")
    void insertDocumentsJson(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{}"));
        values.add(RawJson.of("{}"));
        values.add(RawJson.of("{}"));
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsRawData(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{},{},{}]");
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(3);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsRawDataReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"aaa\":33},{\"aaa\":33},{\"aaa\":33}]");
        final MultiDocumentEntity<DocumentCreateEntity<RawData>> docs =
                collection.insertDocuments(values, new DocumentCreateOptions().returnNew(true)).get();
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
    @MethodSource("asyncCols")
    void insertDocumentsOne(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(1);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsEmpty(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).isEmpty();
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void insertDocumentsReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        final DocumentCreateOptions options = new DocumentCreateOptions().returnNew(true);
        final MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> docs = collection.insertDocuments(values,
                options, BaseDocument.class).get();
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
    @MethodSource("asyncCols")
    void insertDocumentsFail(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final MultiDocumentEntity<?> docs = collection.insertDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getDocuments()).isNotNull();
        assertThat(docs.getDocuments()).hasSize(2);
        assertThat(docs.getErrors()).isNotNull();
        assertThat(docs.getErrors()).hasSize(1);
        assertThat(docs.getErrors().iterator().next().getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(), new BaseDocument(),
                new BaseDocument());

        final DocumentImportEntity docs = collection.importDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(values.size());
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonList(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<RawJson> values = Arrays.asList(
                RawJson.of("{}"),
                RawJson.of("{}"),
                RawJson.of("{}")
        );

        final DocumentImportEntity docs = collection.importDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(values.size());
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDuplicateDefaultError(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDuplicateError(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.error)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDuplicateIgnore(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.ignore)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isEqualTo(1);
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDuplicateReplace(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.replace)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDuplicateUpdate(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values,
                new DocumentImportOptions().onDuplicate(OnDuplicate.update)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsCompleteFail(ArangoCollectionAsync collection) {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        Throwable thrown = catchThrowable(() -> collection.importDocuments(values,
                new DocumentImportOptions().complete(true)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsDetails(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(k1), new BaseDocument(k2),
                new BaseDocument(k2));

        final DocumentImportEntity docs = collection.importDocuments(values, new DocumentImportOptions().details(true)).get();
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
    @MethodSource("asyncCols")
    void importDocumentsOverwriteFalse(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        collection.insertDocument(new BaseDocument()).get();
        Long initialCount = collection.count().get().getCount();

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(initialCount + 2L);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsOverwriteTrue(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        collection.insertDocument(new BaseDocument()).get();

        final Collection<BaseDocument> values = new ArrayList<>();
        values.add(new BaseDocument());
        values.add(new BaseDocument());
        collection.importDocuments(values, new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(2L);
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
    @MethodSource("asyncCols")
    void importDocumentsJson(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDuplicateDefaultError(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDuplicateError(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.error)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isEqualTo(1);
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDuplicateIgnore(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));
        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.ignore)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isEqualTo(1);
        assertThat(docs.getUpdated()).isZero();
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDuplicateReplace(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.replace)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDuplicateUpdate(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().onDuplicate(OnDuplicate.update)).get();
        assertThat(docs).isNotNull();
        assertThat(docs.getCreated()).isEqualTo(2);
        assertThat(docs.getEmpty()).isZero();
        assertThat(docs.getErrors()).isZero();
        assertThat(docs.getIgnored()).isZero();
        assertThat(docs.getUpdated()).isEqualTo(1);
        assertThat(docs.getDetails()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonCompleteFail(ArangoCollectionAsync collection) {
        final String values = "[{\"_key\":\"1\"},{\"_key\":\"2\"},{\"_key\":\"2\"}]";
        Throwable thrown = catchThrowable(() -> collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().complete(true)).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getErrorNum()).isEqualTo(1210);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonDetails(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        String k1 = rnd();
        String k2 = rnd();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", k1),
                Collections.singletonMap("_key", k2), Collections.singletonMap("_key", k2)));

        final DocumentImportEntity docs = collection.importDocuments(RawJson.of(values),
                new DocumentImportOptions().details(true)).get();
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
    @MethodSource("asyncCols")
    void importDocumentsJsonOverwriteFalse(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        collection.insertDocument(new BaseDocument()).get();
        Long initialCount = collection.count().get().getCount();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));
        collection.importDocuments(RawJson.of(values), new DocumentImportOptions().overwrite(false)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(initialCount + 2L);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void importDocumentsJsonOverwriteTrue(ArangoCollectionAsync collection) throws JsonProcessingException, ExecutionException, InterruptedException {
        collection.insertDocument(new BaseDocument()).get();

        final String values = mapper.writeValueAsString(Arrays.asList(Collections.singletonMap("_key", rnd()),
                Collections.singletonMap("_key", rnd())));
        collection.importDocuments(RawJson.of(values), new DocumentImportOptions().overwrite(true)).get();
        assertThat(collection.count().get().getCount()).isEqualTo(2L);
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
    @MethodSource("asyncCols")
    void deleteDocumentsByKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        collection.insertDocuments(values).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("2");
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(keys).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(2);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isIn("1", "2");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsRawDataByKeyReturnOld(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"_key\":\"1\"},{\"_key\":\"2\"}]");
        collection.insertDocuments(values).get();
        final RawData keys = RawJson.of("[\"1\",\"2\"]");
        MultiDocumentEntity<DocumentDeleteEntity<RawData>> deleteResult = collection.deleteDocuments(keys,
                new DocumentDeleteOptions().returnOld(true)).get();
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
    @MethodSource("asyncCols")
    void deleteDocumentsByDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        collection.insertDocuments(values).get();
        MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(values).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(2);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isIn("1", "2");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsByKeyOne(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values).get();
        final Collection<String> keys = new ArrayList<>();
        keys.add("1");
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(keys).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(1);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isEqualTo("1");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsByDocumentOne(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values).get();
        final MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteResult = collection.deleteDocuments(values).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).hasSize(1);
        for (final DocumentDeleteEntity<Void> i : deleteResult.getDocuments()) {
            assertThat(i.getKey()).isEqualTo("1");
        }
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsEmpty(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values);
        final Collection<String> keys = new ArrayList<>();
        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(keys).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsByKeyNotExisting(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        collection.insertDocuments(values);
        final Collection<String> keys = Arrays.asList(rnd(), rnd());

        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(keys).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void deleteDocumentsByDocumentsNotExisting(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        final MultiDocumentEntity<?> deleteResult = collection.deleteDocuments(values).get();
        assertThat(deleteResult).isNotNull();
        assertThat(deleteResult.getDocuments()).isEmpty();
        assertThat(deleteResult.getErrors()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = Arrays.asList(new BaseDocument(rnd()), new BaseDocument(rnd()));
        collection.insertDocuments(values).get();
        values.forEach(it -> it.addAttribute("a", "test"));

        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsWithDifferentReturnType(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        List<String> keys =
                IntStream.range(0, 3).mapToObj(it -> "key-" + UUID.randomUUID()).collect(Collectors.toList());
        List<BaseDocument> docs =
                keys.stream().map(BaseDocument::new).peek(it -> it.addAttribute("a", "test")).collect(Collectors.toList());

        collection.insertDocuments(docs).get();

        List<Map<String, Object>> modifiedDocs = docs.stream().peek(it -> it.addAttribute("b", "test")).map(it -> {
            Map<String, Object> map = new HashMap<>();
            map.put("_key", it.getKey());
            map.put("a", it.getAttribute("a"));
            map.put("b", it.getAttribute("b"));
            return map;
        }).collect(Collectors.toList());

        final MultiDocumentEntity<DocumentUpdateEntity<BaseDocument>> updateResult =
                collection.updateDocuments(modifiedDocs, new DocumentUpdateOptions().returnNew(true), BaseDocument.class).get();
        assertThat(updateResult.getDocuments()).hasSize(3);
        assertThat(updateResult.getErrors()).isEmpty();
        assertThat(updateResult.getDocuments().stream()).map(DocumentUpdateEntity::getNew).allMatch(it -> it.getAttribute("a").equals("test")).allMatch(it -> it.getAttribute("b").equals("test"));
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsOne(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            final BaseDocument e = new BaseDocument(UUID.randomUUID().toString());
            e.setKey("1");
            values.add(e);
        }
        collection.insertDocuments(values).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        final BaseDocument first = values.iterator().next();
        first.addAttribute("a", "test");
        updatedValues.add(first);
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsEmpty(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values).get();
        assertThat(updateResult.getDocuments()).isEmpty();
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsWithoutKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsJson(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{\"_key\":\"1\"}"));
        values.add(RawJson.of("{\"_key\":\"2\"}"));
        collection.insertDocuments(values);

        final Collection<RawJson> updatedValues = new ArrayList<>();
        updatedValues.add(RawJson.of("{\"_key\":\"1\", \"foo\":\"bar\"}"));
        updatedValues.add(RawJson.of("{\"_key\":\"2\", \"foo\":\"bar\"}"));
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsRawData(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void updateDocumentsRawDataReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values).get();

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateResult =
                collection.updateDocuments(updatedValues, new DocumentUpdateOptions().returnNew(true)).get();
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
    @MethodSource("asyncCols")
    void replaceDocuments(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
            values.add(new BaseDocument("2"));
        }
        collection.insertDocuments(values).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsOne(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
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
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsEmpty(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(values).get();
        assertThat(updateResult.getDocuments()).isEmpty();
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsWithoutKey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<BaseDocument> values = new ArrayList<>();
        {
            values.add(new BaseDocument("1"));
        }
        collection.insertDocuments(values).get();
        final Collection<BaseDocument> updatedValues = new ArrayList<>();
        for (final BaseDocument i : values) {
            i.addAttribute("a", "test");
            updatedValues.add(i);
        }
        updatedValues.add(new BaseDocument());
        final MultiDocumentEntity<?> updateResult = collection.updateDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(1);
        assertThat(updateResult.getErrors()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsJson(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final Collection<RawJson> values = new ArrayList<>();
        values.add(RawJson.of("{\"_key\":\"1\"}"));
        values.add(RawJson.of("{\"_key\":\"2\"}"));
        collection.insertDocuments(values).get();

        final Collection<RawJson> updatedValues = new ArrayList<>();
        updatedValues.add(RawJson.of("{\"_key\":\"1\", \"foo\":\"bar\"}"));
        updatedValues.add(RawJson.of("{\"_key\":\"2\", \"foo\":\"bar\"}"));
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsRawData(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values);

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        final MultiDocumentEntity<?> updateResult = collection.replaceDocuments(updatedValues).get();
        assertThat(updateResult.getDocuments()).hasSize(2);
        assertThat(updateResult.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void replaceDocumentsRawDataReturnNew(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final RawData values = RawJson.of("[{\"_key\":\"1\"}, {\"_key\":\"2\"}]");
        collection.insertDocuments(values).get();

        final RawData updatedValues = RawJson.of("[{\"_key\":\"1\", \"foo\":\"bar\"}, {\"_key\":\"2\", " +
                "\"foo\":\"bar\"}]");
        MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateResult =
                collection.replaceDocuments(updatedValues, new DocumentReplaceOptions().returnNew(true)).get();
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
    @MethodSource("asyncCols")
    void getInfo(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final CollectionEntity result = collection.getInfo().get();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getPropeties(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final CollectionPropertiesEntity result = collection.getProperties().get();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
        assertThat(result.getCount()).isNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void changeProperties(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final CollectionPropertiesEntity properties = collection.getProperties().get();
        assertThat(properties.getWaitForSync()).isNotNull();
        if (isAtLeastVersion(3, 7)) {
            assertThat(properties.getSchema()).isNull();
        }

        String schemaRule = ("{  " + "           \"properties\": {" + "               \"number\": {" + "             " +
                "      \"type\": \"number\"" + "               }" + "           }" + "       }").replaceAll("\\s", "");
        String schemaMessage = "The document has problems!";

        CollectionPropertiesOptions updatedOptions =
                new CollectionPropertiesOptions().waitForSync(!properties.getWaitForSync()).schema(new CollectionSchema().setLevel(CollectionSchema.Level.NEW).setMessage(schemaMessage).setRule(schemaRule));

        final CollectionPropertiesEntity changedProperties = collection.changeProperties(updatedOptions).get();
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
                .waitForSync(properties.getWaitForSync()).schema(new CollectionSchema())).get();
        if (isAtLeastVersion(3, 7)) {
            assertThat(revertedProperties.getSchema()).isNull();
        }

    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void rename(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        ArangoDatabaseAsync db = collection.db();

        if (!db.collection("c1").exists().get()) {
            db.collection("c1").create().get();
        }

        if (db.collection("c2").exists().get()) {
            db.collection("c2").drop().get();
        }

        final CollectionEntity result = db.collection("c1").rename("c2").get();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("c2");

        final CollectionEntity info = db.collection("c2").getInfo().get();
        assertThat(info.getName()).isEqualTo("c2");

        Throwable thrown = catchThrowable(() -> db.collection("c1").getInfo().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void responsibleShard(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 5));
        ShardEntity shard = collection.getResponsibleShard(new BaseDocument("testKey")).get();
        assertThat(shard).isNotNull();
        assertThat(shard.getShardId()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getRevision(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final CollectionRevisionEntity result = collection.getRevision().get();
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(COLLECTION_NAME);
        assertThat(result.getRevision()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void keyWithSpecialCharacter(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final String key = "myKey_-:.@()+,=;$!*'%-" + UUID.randomUUID();
        collection.insertDocument(new BaseDocument(key)).get();
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void alreadyUrlEncodedkey(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        final String key = "http%3A%2F%2Fexample.com%2F-" + UUID.randomUUID();
        collection.insertDocument(new BaseDocument(key)).get();
        final BaseDocument doc = collection.getDocument(key, BaseDocument.class).get();
        assertThat(doc).isNotNull();
        assertThat(doc.getKey()).isEqualTo(key);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void grantAccessRW(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null).get();
            collection.grantAccess("user1", Permissions.RW).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void grantAccessRO(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null).get();
            collection.grantAccess("user1", Permissions.RO).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void grantAccessNONE(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null).get();
            collection.grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void grantAccessUserNotFound(ArangoCollectionAsync collection) {
        Throwable thrown = catchThrowable(() -> collection.grantAccess("user1", Permissions.RW).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void revokeAccess(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null).get();
            collection.grantAccess("user1", Permissions.NONE).get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void revokeAccessUserNotFound(ArangoCollectionAsync collection) {
        Throwable thrown = catchThrowable(() -> collection.grantAccess("user1", Permissions.NONE).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void resetAccess(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = collection.db().arango();
        try {
            arangoDB.createUser("user1", "1234", null).get();
            collection.resetAccess("user1").get();
        } finally {
            arangoDB.deleteUser("user1").get();
        }
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void resetAccessUserNotFound(ArangoCollectionAsync collection) {
        Throwable thrown = catchThrowable(() -> collection.resetAccess("user1").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void getPermissions(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assertThat(collection.getPermissions("root").get()).isEqualTo(Permissions.RW);
    }

    @ParameterizedTest
    @MethodSource("asyncCols")
    void annotationsInParamsAndMethods(ArangoCollectionAsync collection) throws ExecutionException, InterruptedException {
        assumeTrue(collection.getSerde().getUserSerde() instanceof JacksonSerde, "JacksonSerde only");
        AnnotatedEntity entity = new AnnotatedEntity(UUID.randomUUID().toString());
        AnnotatedEntity doc = collection.insertDocument(entity, new DocumentCreateOptions().returnNew(true)).get().getNew();
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

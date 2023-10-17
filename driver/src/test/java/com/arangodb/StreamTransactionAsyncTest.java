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
import com.arangodb.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class StreamTransactionAsyncTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "StreamTransactionTest_collection";

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void beginStreamTransaction(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(null).get();
        assertThat(tx.getId()).isNotNull();
        assertThat(tx.getStatus()).isEqualTo(StreamTransactionStatus.running);
        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void beginStreamTransactionWithNonExistingCollectionsShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() ->
                db.beginStreamTransaction(new StreamTransactionOptions().writeCollections("notExistingCollection")).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void abortStreamTransaction(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null).get();
        StreamTransactionEntity abortedTx = db.abortStreamTransaction(begunTx.getId()).get();

        assertThat(abortedTx.getId()).isNotNull();
        assertThat(abortedTx.getId()).isEqualTo(begunTx.getId());
        assertThat(abortedTx.getStatus()).isEqualTo(StreamTransactionStatus.aborted);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void abortStreamTransactionTwice(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null).get();
        db.abortStreamTransaction(begunTx.getId());
        db.abortStreamTransaction(begunTx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void abortStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction("000000").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void abortStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction("invalidTransactionId").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void abortCommittedStreamTransactionShouldThrow(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.commitStreamTransaction(createdTx.getId()).get();
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction(createdTx.getId()).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getStreamTransaction(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        StreamTransactionEntity gotTx = db.getStreamTransaction(createdTx.getId()).get();

        assertThat(gotTx.getId()).isNotNull();
        assertThat(gotTx.getId()).isEqualTo(createdTx.getId());
        assertThat(gotTx.getStatus()).isEqualTo(StreamTransactionStatus.running);

        db.abortStreamTransaction(createdTx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.getStreamTransaction("000000").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.getStreamTransaction("invalidTransactionId").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void commitStreamTransaction(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        StreamTransactionEntity committedTx = db.commitStreamTransaction(createdTx.getId()).get();

        assertThat(committedTx.getId()).isNotNull();
        assertThat(committedTx.getId()).isEqualTo(createdTx.getId());
        assertThat(committedTx.getStatus()).isEqualTo(StreamTransactionStatus.committed);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void commitStreamTransactionTwice(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.commitStreamTransaction(createdTx.getId());
        db.commitStreamTransaction(createdTx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void commitStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction("000000").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void commitStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction("invalidTransactionId").get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void commitAbortedStreamTransactionShouldThrow(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.abortStreamTransaction(createdTx.getId()).get();
        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction(createdTx.getId()).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getDocument(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null).get();

        // assert that the document is not found from within the tx
        assertThat(collection.getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getDocumentWithNonExistingTransactionIdShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        Throwable thrown = catchThrowable(() -> collection
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("123456")).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getDocumentWithInvalidTransactionIdShouldThrow(ArangoDatabaseAsync db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        Throwable thrown = catchThrowable(() -> collection
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("abcde")).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getDocuments(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert documents from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc1 = collection
                .insertDocument(new BaseDocument(), null).get();

        DocumentCreateEntity<BaseDocument> externalDoc2 = collection
                .insertDocument(new BaseDocument(), null).get();

        // assert that the documents are not found from within the tx
        MultiDocumentEntity<BaseDocument> documents = collection
                .getDocuments(Arrays.asList(externalDoc1.getId(), externalDoc2.getId()), BaseDocument.class,
                        new DocumentReadOptions().streamTransactionId(tx.getId())).get();

        assertThat(documents.getDocuments()).isEmpty();

        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void insertDocument(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert a document from within the tx
        DocumentCreateEntity<BaseDocument> txDoc = collection
                .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx.getId())).get();

        // assert that the document is not found from outside the tx
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class, null).get()).isNull();

        // assert that the document is found from within the tx
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get()).isNotNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the document is found after commit
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class, null).get()).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void insertDocuments(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert documents from within the tx
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> txDocs = collection
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().streamTransactionId(tx.getId()), BaseDocument.class).get();

        List<String> keys = txDocs.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        // assert that the documents are not found from outside the tx
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments()).isEmpty();

        // assert that the documents are found from within the tx
        assertThat(collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())).get()
                .getDocuments()).hasSize(keys.size());

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document is found after commit
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments()).hasSize(keys.size());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void replaceDocument(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("test", "foo");

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        // replace document from within the tx
        doc.updateAttribute("test", "bar");
        collection.replaceDocument(createdDoc.getKey(), doc,
                new DocumentReplaceOptions().streamTransactionId(tx.getId())).get();

        // assert that the document has not been replaced from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the document has been replaced from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test",
                "bar");

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document has been replaced after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void replaceDocuments(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        List<BaseDocument> createdDocs = collection
                .insertDocuments(docs, new DocumentCreateOptions().returnNew(true), BaseDocument.class).get().getDocuments().stream()
                .map(DocumentCreateEntity::getNew).collect(Collectors.toList());

        List<String> keys = createdDocs.stream().map(BaseDocument::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        List<BaseDocument> modifiedDocs = createdDocs.stream().peek(doc -> doc.updateAttribute("test", "bar")).collect(Collectors.toList());

        // replace document from within the tx
        collection
                .replaceDocuments(modifiedDocs, new DocumentReplaceOptions().streamTransactionId(tx.getId())).get();

        // assert that the documents has not been replaced from outside the tx
        collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("foo"));

        // assert that the document has been replaced from within the tx
        collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())).get()
                .getDocuments().stream().map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document has been replaced after commit
        collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void updateDocument(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument(UUID.randomUUID().toString());
        doc.addAttribute("test", "foo");

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        // update document from within the tx
        doc.updateAttribute("test", "bar");
        collection
                .updateDocument(createdDoc.getKey(), doc, new DocumentUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been updated from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "foo");

        // assert that the document has been updated from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get().getProperties()).containsEntry("test", "bar")
        ;

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document has been updated after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()
                .getProperties()).containsEntry("test", "bar");

    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void updateDocuments(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        List<BaseDocument> createdDocs = collection
                .insertDocuments(docs, new DocumentCreateOptions().returnNew(true), BaseDocument.class).get().getDocuments().stream()
                .map(DocumentCreateEntity::getNew).collect(Collectors.toList());

        List<String> keys = createdDocs.stream().map(BaseDocument::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        List<BaseDocument> modifiedDocs = createdDocs.stream().peek(doc -> doc.updateAttribute("test", "bar")).collect(Collectors.toList());

        // update documents from within the tx
        collection
                .updateDocuments(modifiedDocs, new DocumentUpdateOptions().streamTransactionId(tx.getId())).get();

        // assert that the documents have not been updated from outside the tx
        collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("foo"));

        // assert that the documents have been updated from within the tx
        collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())).get()
                .getDocuments().stream().map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been updated after commit
        collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteDocument(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection
                .insertDocument(new BaseDocument(), null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        // delete document from within the tx
        collection
                .deleteDocument(createdDoc.getKey(), new DocumentDeleteOptions().streamTransactionId(tx.getId())).get();

        // assert that the document has not been deleted from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()).isNotNull();

        // assert that the document has been deleted from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document has been deleted after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null).get()).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void deleteDocuments(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        List<String> keys = collection
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument())).get()
                .getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        // delete document from within the tx
        collection
                .deleteDocuments(keys, new DocumentDeleteOptions().streamTransactionId(tx.getId())).get();

        // assert that the documents has not been deleted from outside the tx
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments()).hasSize(keys.size());

        // assert that the document has been deleted from within the tx
        assertThat(collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())).get()
                .getDocuments()).isEmpty();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the document has been deleted after commit
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).get().getDocuments()).isEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void documentExists(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null).get();

        // assert that the document is not found from within the tx
        assertThat(collection
                .documentExists(externalDoc.getKey(), new DocumentExistsOptions().streamTransactionId(tx.getId())).get()).isFalse();

        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void count(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        Long initialCount = collection.count().get().getCount();

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();

        // insert a document from outside the tx
        collection.insertDocument(new BaseDocument(), null).get();

        // assert that the document is not counted from within the tx
        assertThat(collection.count(new CollectionCountOptions().streamTransactionId(tx.getId())).get()
                .getCount()).isEqualTo(initialCount);

        db.abortStreamTransaction(tx.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void truncate(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument(), null).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME)).get();

        // truncate document from within the tx
        collection.truncate(new CollectionTruncateOptions().streamTransactionId(tx.getId())).get();

        // assert that the collection has not been truncated from outside the tx
        assertThat(collection.count().get().getCount()).isPositive();

        // assert that the collection has been truncated from within the tx
        assertThat(collection.count(new CollectionCountOptions().streamTransactionId(tx.getId())).get()
                .getCount()).isZero();

        db.commitStreamTransaction(tx.getId()).get();

        // assert that the collection has been truncated after commit
        assertThat(collection.count().get().getCount()).isZero();
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void createCursor(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
//        assumeTrue(isSingleServer());
//        assumeTrue(isAtLeastVersion(3, 5));
//        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
//
//        StreamTransactionEntity tx = db
//                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();
//        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
//
//        // insert a document from outside the tx
//        DocumentCreateEntity<BaseDocument> externalDoc = collection
//                .insertDocument(new BaseDocument(), null).get();
//
//        final Map<String, Object> bindVars = new HashMap<>();
//        bindVars.put("@collection", COLLECTION_NAME);
//        bindVars.put("key", externalDoc.getKey());
//
//        ArangoCursor<BaseDocument> cursor = db
//                .query("FOR doc IN @@collection FILTER doc._key == @key RETURN doc", BaseDocument.class, bindVars,
//                        new AqlQueryOptions().streamTransactionId(tx.getId()));
//
//        // assert that the document is not found from within the tx
//        assertThat(cursor.hasNext()).isFalse();
//
//        db.abortStreamTransaction(tx.getId());
//    }
//
//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void nextCursor(ArangoDatabaseAsync db) {
//        assumeTrue(isSingleServer());
//        assumeTrue(isAtLeastVersion(3, 5));
//        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
//
//        StreamTransactionEntity tx = db.beginStreamTransaction(
//                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));
//        ArangoCollection collection = db.collection(COLLECTION_NAME);
//
//        // insert documents from within the tx
//        List<String> keys = collection
//                .insertDocuments(IntStream.range(0, 10).mapToObj(it -> new BaseDocument()).collect(Collectors.toList()),
//                        new DocumentCreateOptions().streamTransactionId(tx.getId())).getDocuments().stream()
//                .map(DocumentEntity::getKey).collect(Collectors.toList());
//
//        final Map<String, Object> bindVars = new HashMap<>();
//        bindVars.put("@collection", COLLECTION_NAME);
//        bindVars.put("keys", keys);
//
//        ArangoCursor<BaseDocument> cursor = db
//                .query("FOR doc IN @@collection FILTER CONTAINS_ARRAY(@keys, doc._key) RETURN doc", BaseDocument.class, bindVars,
//                        new AqlQueryOptions().streamTransactionId(tx.getId()).batchSize(2));
//
//        List<BaseDocument> docs = cursor.asListRemaining();
//
//        // assert that all the keys are returned from the query
//        assertThat(docs.stream().map(BaseDocument::getKey).collect(Collectors.toList())).containsAll(keys);
//
//        db.abortStreamTransaction(tx.getId());
//    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void getStreamTransactions(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx1 = db.beginStreamTransaction(null).get();
        StreamTransactionEntity tx2 = db.beginStreamTransaction(null).get();

        List<String> createdIds = Arrays.asList(tx1.getId(), tx2.getId());
        Set<TransactionEntity> gotTxs = db.getStreamTransactions().get().stream().
                filter(it -> createdIds.contains(it.getId())).collect(Collectors.toSet());

        assertThat(gotTxs).hasSameSizeAs(createdIds);
        assertThat(gotTxs.stream()
                .allMatch(it -> it.getState() == StreamTransactionStatus.running)).isTrue();

        db.abortStreamTransaction(tx1.getId()).get();
        db.abortStreamTransaction(tx2.getId()).get();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncDbs")
    void transactionAllowImplicitFalse(ArangoDatabaseAsync db) throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().allowImplicit(false)).get();
        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null).get();

        // assert that we cannot read from collection
        Throwable thrown = catchThrowable(() -> collection.getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1652);
        assertThat(e.getMessage()).contains("unregistered collection used in transaction");

        db.abortStreamTransaction(tx.getId()).get();
    }

//    @ParameterizedTest(name = "{index}")
//    @MethodSource("asyncDbs")
//    void transactionDirtyRead(ArangoDatabaseAsync db) throws IOException, ExecutionException, InterruptedException {
//        assumeTrue(isCluster());
//        assumeTrue(isAtLeastVersion(3, 10));
//
//        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
//        DocumentCreateEntity<?> doc = collection.insertDocument(new BaseDocument()).get();
//
//        StreamTransactionEntity tx = db
//                .beginStreamTransaction(new StreamTransactionOptions()
//                        .readCollections(COLLECTION_NAME)
//                        .allowDirtyRead(true)).get();
//
//        MultiDocumentEntity<BaseDocument> readDocs = collection.getDocuments(Collections.singletonList(doc.getKey()),
//                BaseDocument.class,
//                new DocumentReadOptions().streamTransactionId(tx.getId())).get();
//
//        assertThat(readDocs.isPotentialDirtyRead()).isTrue();
//        assertThat(readDocs.getDocuments()).hasSize(1);
//
//        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col RETURN i", BaseDocument.class,
//                Collections.singletonMap("@col", COLLECTION_NAME),
//                new AqlQueryOptions().streamTransactionId(tx.getId()));
//            assertThat(cursor.isPotentialDirtyRead()).isTrue();
//            cursor.close();
//
//        db.abortStreamTransaction(tx.getId());
//    }

}

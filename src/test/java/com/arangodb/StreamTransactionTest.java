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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class StreamTransactionTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "StreamTransactionTest_collection";

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void beginStreamTransaction(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(null);
        assertThat(tx.getId()).isNotNull();
        assertThat(tx.getStatus()).isEqualTo(StreamTransactionStatus.running);
        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void beginStreamTransactionWithNonExistingCollectionsShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() ->
                db.beginStreamTransaction(new StreamTransactionOptions().writeCollections("notExistingCollection")));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void abortStreamTransaction(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null);
        StreamTransactionEntity abortedTx = db.abortStreamTransaction(begunTx.getId());

        assertThat(abortedTx.getId()).isNotNull();
        assertThat(abortedTx.getId()).isEqualTo(begunTx.getId());
        assertThat(abortedTx.getStatus()).isEqualTo(StreamTransactionStatus.aborted);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void abortStreamTransactionTwice(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null);
        db.abortStreamTransaction(begunTx.getId());
        db.abortStreamTransaction(begunTx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void abortStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction("000000"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void abortStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction("invalidTransactionId"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void abortCommittedStreamTransactionShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.commitStreamTransaction(createdTx.getId());
        Throwable thrown = catchThrowable(() -> db.abortStreamTransaction(createdTx.getId()));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getStreamTransaction(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        StreamTransactionEntity gotTx = db.getStreamTransaction(createdTx.getId());

        assertThat(gotTx.getId()).isNotNull();
        assertThat(gotTx.getId()).isEqualTo(createdTx.getId());
        assertThat(gotTx.getStatus()).isEqualTo(StreamTransactionStatus.running);

        db.abortStreamTransaction(createdTx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.getStreamTransaction("000000"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.getStreamTransaction("invalidTransactionId"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void commitStreamTransaction(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        StreamTransactionEntity committedTx = db.commitStreamTransaction(createdTx.getId());

        assertThat(committedTx.getId()).isNotNull();
        assertThat(committedTx.getId()).isEqualTo(createdTx.getId());
        assertThat(committedTx.getStatus()).isEqualTo(StreamTransactionStatus.committed);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void commitStreamTransactionTwice(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.commitStreamTransaction(createdTx.getId());
        db.commitStreamTransaction(createdTx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void commitStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction("000000"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void commitStreamTransactionWithInvalidTransactionIdShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction("invalidTransactionId"));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void commitAbortedStreamTransactionShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.abortStreamTransaction(createdTx.getId());
        Throwable thrown = catchThrowable(() -> db.commitStreamTransaction(createdTx.getId()));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocument(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null);

        // assert that the document is not found from within the tx
        assertThat(collection.getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocumentWithNonExistingTransactionIdShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);

        Throwable thrown = catchThrowable(() -> collection
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("123456")));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocumentWithInvalidTransactionIdShouldThrow(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        Throwable thrown = catchThrowable(() -> collection
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("abcde")));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getDocuments(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert documents from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc1 = collection
                .insertDocument(new BaseDocument(), null);

        DocumentCreateEntity<BaseDocument> externalDoc2 = collection
                .insertDocument(new BaseDocument(), null);

        // assert that the documents are not found from within the tx
        MultiDocumentEntity<BaseDocument> documents = collection
                .getDocuments(Arrays.asList(externalDoc1.getId(), externalDoc2.getId()), BaseDocument.class,
                        new DocumentReadOptions().streamTransactionId(tx.getId()));

        assertThat(documents.getDocuments()).isEmpty();

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void insertDocument(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert a document from within the tx
        DocumentCreateEntity<BaseDocument> txDoc = collection
                .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx.getId()));

        // assert that the document is not found from outside the tx
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class, null)).isNull();

        // assert that the document is found from within the tx
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId()))).isNotNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the document is found after commit
        assertThat(collection.getDocument(txDoc.getKey(), BaseDocument.class, null)).isNotNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void insertDocuments(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert documents from within the tx
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> txDocs = collection
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().streamTransactionId(tx.getId()));

        List<String> keys = txDocs.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        // assert that the documents are not found from outside the tx
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).getDocuments()).isEmpty();

        // assert that the documents are found from within the tx
        assertThat(collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments()).hasSize(keys.size());

        db.commitStreamTransaction(tx.getId());

        // assert that the document is found after commit
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).getDocuments()).hasSize(keys.size());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void replaceDocument(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // replace document from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        collection.replaceDocument(createdDoc.getKey(), doc,
                new DocumentReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been replaced from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the document has been replaced from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar");

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been replaced after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "bar");
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void replaceDocuments(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        List<BaseDocument> createdDocs = collection
                .insertDocuments(docs, new DocumentCreateOptions().returnNew(true)).getDocuments().stream()
                .map(DocumentCreateEntity::getNew).collect(Collectors.toList());

        List<String> keys = createdDocs.stream().map(BaseDocument::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        List<BaseDocument> modifiedDocs = createdDocs.stream().peek(doc -> {
            doc.getProperties().clear();
            doc.addAttribute("test", "bar");
        }).collect(Collectors.toList());

        // replace document from within the tx
        collection
                .replaceDocuments(modifiedDocs, new DocumentReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the documents has not been replaced from outside the tx
        collection.getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("foo"));

        // assert that the document has been replaced from within the tx
        collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments().stream().map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been replaced after commit
        collection.getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void updateDocument(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection.insertDocument(doc, null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // update document from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        collection
                .updateDocument(createdDoc.getKey(), doc, new DocumentUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been updated from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "foo");

        // assert that the document has been updated from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).getProperties()).containsEntry("test", "bar")
        ;

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been updated after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties()).containsEntry("test", "bar");

    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void updateDocuments(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        List<BaseDocument> createdDocs = collection
                .insertDocuments(docs, new DocumentCreateOptions().returnNew(true)).getDocuments().stream()
                .map(DocumentCreateEntity::getNew).collect(Collectors.toList());

        List<String> keys = createdDocs.stream().map(BaseDocument::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        List<BaseDocument> modifiedDocs = createdDocs.stream().peek(doc -> {
            doc.getProperties().clear();
            doc.addAttribute("test", "bar");
        }).collect(Collectors.toList());

        // update documents from within the tx
        collection
                .updateDocuments(modifiedDocs, new DocumentUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the documents have not been updated from outside the tx
        collection.getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("foo"));

        // assert that the documents have been updated from within the tx
        collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments().stream().map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been updated after commit
        collection.getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test")))
                .forEach(it -> assertThat(it).isEqualTo("bar"));
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteDocument(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> createdDoc = collection
                .insertDocument(new BaseDocument(), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // delete document from within the tx
        collection
                .deleteDocument(createdDoc.getKey(), null, new DocumentDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been deleted from outside the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)).isNotNull();

        // assert that the document has been deleted from within the tx
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId()))).isNull();

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been deleted after commit
        assertThat(collection.getDocument(createdDoc.getKey(), BaseDocument.class, null)).isNull();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void deleteDocuments(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        List<String> keys = collection
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()), null)
                .getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // delete document from within the tx
        collection
                .deleteDocuments(keys, null, new DocumentDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the documents has not been deleted from outside the tx
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).getDocuments()).hasSize(keys.size());

        // assert that the document has been deleted from within the tx
        assertThat(collection
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments()).isEmpty();

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been deleted after commit
        assertThat(collection.getDocuments(keys, BaseDocument.class, null).getDocuments()).isEmpty();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void documentExists(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null);

        // assert that the document is not found from within the tx
        assertThat(collection
                .documentExists(externalDoc.getKey(), new DocumentExistsOptions().streamTransactionId(tx.getId()))).isFalse();

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void count(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        Long initialCount = collection.count().getCount();

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert a document from outside the tx
        collection.insertDocument(new BaseDocument(), null);

        // assert that the document is not counted from within the tx
        assertThat(collection.count(new CollectionCountOptions().streamTransactionId(tx.getId()))
                .getCount()).isEqualTo(initialCount);

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void truncate(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        collection.insertDocument(new BaseDocument(), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // truncate document from within the tx
        collection.truncate(new CollectionTruncateOptions().streamTransactionId(tx.getId()));

        // assert that the collection has not been truncated from outside the tx
        assertThat(collection.count().getCount()).isPositive();

        // assert that the collection has been truncated from within the tx
        assertThat(collection.count(new CollectionCountOptions().streamTransactionId(tx.getId()))
                .getCount()).isZero();

        db.commitStreamTransaction(tx.getId());

        // assert that the collection has been truncated after commit
        assertThat(collection.count().getCount()).isZero();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void createCursor(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null);

        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", COLLECTION_NAME);
        bindVars.put("key", externalDoc.getKey());

        ArangoCursor<BaseDocument> cursor = db
                .query("FOR doc IN @@collection FILTER doc._key == @key RETURN doc", bindVars,
                        new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class);

        // assert that the document is not found from within the tx
        assertThat(cursor.hasNext()).isFalse();

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void nextCursor(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert documents from within the tx
        List<String> keys = collection
                .insertDocuments(IntStream.range(0, 10).mapToObj(it -> new BaseDocument()).collect(Collectors.toList()),
                        new DocumentCreateOptions().streamTransactionId(tx.getId())).getDocuments().stream()
                .map(DocumentEntity::getKey).collect(Collectors.toList());

        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", COLLECTION_NAME);
        bindVars.put("keys", keys);

        ArangoCursor<BaseDocument> cursor = db
                .query("FOR doc IN @@collection FILTER CONTAINS_ARRAY(@keys, doc._key) RETURN doc", bindVars,
                        new AqlQueryOptions().streamTransactionId(tx.getId()).batchSize(2), BaseDocument.class);

        List<BaseDocument> docs = cursor.asListRemaining();

        // assert that all the keys are returned from the query
        assertThat(docs.stream().map(BaseDocument::getKey).collect(Collectors.toList())).containsAll(keys);

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void getStreamTransactions(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx1 = db.beginStreamTransaction(null);
        StreamTransactionEntity tx2 = db.beginStreamTransaction(null);

        List<String> createdIds = Arrays.asList(tx1.getId(), tx2.getId());
        Set<TransactionEntity> gotTxs = db.getStreamTransactions().stream().
                filter(it -> createdIds.contains(it.getId())).collect(Collectors.toSet());

        assertThat(gotTxs).hasSameSizeAs(createdIds);
        assertThat(gotTxs.stream()
                .allMatch(it -> it.getStatus() == StreamTransactionStatus.running)).isTrue();

        db.abortStreamTransaction(tx1.getId());
        db.abortStreamTransaction(tx2.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionAllowImplicitFalse(ArangoDatabase db) {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().allowImplicit(false));
        ArangoCollection collection = db.collection(COLLECTION_NAME);

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = collection
                .insertDocument(new BaseDocument(), null);

        // assert that we cannot read from collection
        Throwable thrown = catchThrowable(() -> collection.getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        assertThat(e.getResponseCode()).isEqualTo(400);
        assertThat(e.getErrorNum()).isEqualTo(1652);
        assertThat(e.getMessage()).contains("unregistered collection used in transaction");

        db.abortStreamTransaction(tx.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void transactionDirtyRead(ArangoDatabase db) throws IOException {
        assumeTrue(isCluster());
        assumeTrue(isAtLeastVersion(3, 10));

        ArangoCollection collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument());

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions()
                        .readCollections(COLLECTION_NAME)
                        .allowDirtyRead(true));

        MultiDocumentEntity<BaseDocument> readDocs = collection.getDocuments(Collections.singletonList(doc.getKey()),
                BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId()));

        assertThat(readDocs.isPotentialDirtyRead()).isTrue();
        assertThat(readDocs.getDocuments()).hasSize(1);

        final ArangoCursor<BaseDocument> cursor = db.query("FOR i IN @@col RETURN i",
                Collections.singletonMap("@col", COLLECTION_NAME),
                new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class);
            assertThat(cursor.isPotentialDirtyRead()).isTrue();
            cursor.close();

        db.abortStreamTransaction(tx.getId());
    }

}

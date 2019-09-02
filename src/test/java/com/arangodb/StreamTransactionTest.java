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

import com.arangodb.ArangoDB.Builder;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class StreamTransactionTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_stream_transaction_test";

    public StreamTransactionTest(final Builder builder) {
        super(builder);
        if (db.collection(COLLECTION_NAME).exists())
            db.collection(COLLECTION_NAME).drop();

        db.createCollection(COLLECTION_NAME, null);
    }

    @After
    public void teardown() {
        if (db.collection(COLLECTION_NAME).exists())
            db.collection(COLLECTION_NAME).drop();
    }

    @Test
    public void beginStreamTransaction() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(null);
        assertThat(tx.getId(), is(notNullValue()));
        assertThat(tx.getStatus(), is(StreamTransactionStatus.running));
        db.abortStreamTransaction(tx.getId());
    }

    @Test(expected = ArangoDBException.class)
    public void beginStreamTransactionWithNonExistingCollectionsShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.beginStreamTransaction(new StreamTransactionOptions().writeCollections("notExistingCollection"));
    }

    @Test
    public void abortStreamTransaction() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null);
        StreamTransactionEntity abortedTx = db.abortStreamTransaction(begunTx.getId());

        assertThat(abortedTx.getId(), is(notNullValue()));
        assertThat(abortedTx.getId(), is(begunTx.getId()));
        assertThat(abortedTx.getStatus(), is(StreamTransactionStatus.aborted));
    }

    @Test
    public void abortStreamTransactionTwice() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null);
        db.abortStreamTransaction(begunTx.getId());
        db.abortStreamTransaction(begunTx.getId());
    }

    @Test(expected = ArangoDBException.class)
    public void abortStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.abortStreamTransaction("000000");
    }

    @Test(expected = ArangoDBException.class)
    public void abortStreamTransactionWithInvalidTransactionIdShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.abortStreamTransaction("invalidTransactionId");
    }

    @Test(expected = ArangoDBException.class)
    public void abortCommittedStreamTransactionShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.commitStreamTransaction(createdTx.getId());
        db.abortStreamTransaction(createdTx.getId());
    }

    @Test
    public void getStreamTransaction() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        StreamTransactionEntity gotTx = db.getStreamTransaction(createdTx.getId());

        assertThat(gotTx.getId(), is(notNullValue()));
        assertThat(gotTx.getId(), is(createdTx.getId()));
        assertThat(gotTx.getStatus(), is(StreamTransactionStatus.running));

        db.abortStreamTransaction(createdTx.getId());
    }

    @Test(expected = ArangoDBException.class)
    public void getStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.getStreamTransaction("000000");
    }

    @Test(expected = ArangoDBException.class)
    public void getStreamTransactionWithInvalidTransactionIdShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.getStreamTransaction("invalidTransactionId");
    }

    @Test
    public void commitStreamTransaction() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        StreamTransactionEntity committedTx = db.commitStreamTransaction(createdTx.getId());

        assertThat(committedTx.getId(), is(notNullValue()));
        assertThat(committedTx.getId(), is(createdTx.getId()));
        assertThat(committedTx.getStatus(), is(StreamTransactionStatus.committed));
    }

    @Test
    public void commitStreamTransactionTwice() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.commitStreamTransaction(createdTx.getId());
        db.commitStreamTransaction(createdTx.getId());
    }

    @Test(expected = ArangoDBException.class)
    public void commitStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.commitStreamTransaction("000000");
    }

    @Test(expected = ArangoDBException.class)
    public void commitStreamTransactionWithInvalidTransactionIdShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.commitStreamTransaction("invalidTransactionId");
    }

    @Test(expected = ArangoDBException.class)
    public void commitAbortedStreamTransactionShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null);
        db.abortStreamTransaction(createdTx.getId());
        db.commitStreamTransaction(createdTx.getId());
    }

    @Test
    public void getDocument() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        // assert that the document is not found from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.abortStreamTransaction(tx.getId());
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWithNonExistingTransactionIdShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.collection(COLLECTION_NAME)
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("123456"));
    }

    @Test(expected = ArangoDBException.class)
    public void getDocumentWithInvalidTransactionIdShouldThrow() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.collection(COLLECTION_NAME)
                .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("abcde"));
    }

    @Test
    public void getDocuments() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert documents from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc1 = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        DocumentCreateEntity<BaseDocument> externalDoc2 = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        // assert that the documents are not found from within the tx
        MultiDocumentEntity<BaseDocument> documents = db.collection(COLLECTION_NAME)
                .getDocuments(Arrays.asList(externalDoc1.getId(), externalDoc2.getId()), BaseDocument.class,
                        new DocumentReadOptions().streamTransactionId(tx.getId()));

        assertThat(documents.getDocuments(), is(empty()));

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    public void insertDocument() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // insert a document from within the tx
        DocumentCreateEntity<BaseDocument> txDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx.getId()));

        // assert that the document is not found from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class, null),
                is(nullValue()));

        // assert that the document is found from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())), is(notNullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the document is found after commit
        assertThat(db.collection(COLLECTION_NAME).getDocument(txDoc.getKey(), BaseDocument.class, null),
                is(notNullValue()));
    }

    @Test
    public void insertDocuments() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // insert documents from within the tx
        MultiDocumentEntity<DocumentCreateEntity<BaseDocument>> txDocs = db.collection(COLLECTION_NAME)
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()),
                        new DocumentCreateOptions().streamTransactionId(tx.getId()));

        List<String> keys = txDocs.getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        // assert that the documents are not found from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments(),
                is(empty()));

        // assert that the documents are found from within the tx
        assertThat(db.collection(COLLECTION_NAME)
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments(), hasSize(keys.size()));

        db.commitStreamTransaction(tx.getId());

        // assert that the document is found after commit
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments(),
                hasSize(keys.size()));
    }

    @Test
    public void replaceDocument() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        DocumentCreateEntity<BaseDocument> createdDoc = db.collection(COLLECTION_NAME).insertDocument(doc, null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // replace document from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        db.collection(COLLECTION_NAME).replaceDocument(createdDoc.getKey(), doc,
                new DocumentReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been replaced from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("foo"));

        // assert that the document has been replaced from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been replaced after commit
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("bar"));
    }

    @Test
    public void replaceDocuments() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        List<BaseDocument> createdDocs = db.collection(COLLECTION_NAME)
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
        db.collection(COLLECTION_NAME)
                .replaceDocuments(modifiedDocs, new DocumentReplaceOptions().streamTransactionId(tx.getId()));

        // assert that the documents has not been replaced from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("foo")));

        // assert that the document has been replaced from within the tx
        assertThat(db.collection(COLLECTION_NAME)
                        .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                        .getDocuments().stream().map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()),
                everyItem(is("bar")));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been replaced after commit
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("bar")));
    }

    @Test
    public void updateDocument() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        BaseDocument doc = new BaseDocument();
        doc.addAttribute("test", "foo");

        DocumentCreateEntity<BaseDocument> createdDoc = db.collection(COLLECTION_NAME).insertDocument(doc, null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // update document from within the tx
        doc.getProperties().clear();
        doc.addAttribute("test", "bar");
        db.collection(COLLECTION_NAME)
                .updateDocument(createdDoc.getKey(), doc, new DocumentUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been updated from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("foo"));

        // assert that the document has been updated from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).getProperties().get("test"), is("bar"));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been updated after commit
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null)
                .getProperties().get("test"), is("bar"));

    }

    @Test
    public void updateDocuments() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<BaseDocument> docs = IntStream.range(0, 3).mapToObj(it -> new BaseDocument())
                .peek(doc -> doc.addAttribute("test", "foo")).collect(Collectors.toList());

        List<BaseDocument> createdDocs = db.collection(COLLECTION_NAME)
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
        db.collection(COLLECTION_NAME)
                .updateDocuments(modifiedDocs, new DocumentUpdateOptions().streamTransactionId(tx.getId()));

        // assert that the documents have not been updated from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("foo")));

        // assert that the documents have been updated from within the tx
        List<String> values = db.collection(COLLECTION_NAME)
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments().stream().map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList());
        assertThat(values, everyItem(is("bar")));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been updated after commit
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments().stream()
                .map(it -> ((String) it.getAttribute("test"))).collect(Collectors.toList()), everyItem(is("bar")));
    }

    @Test
    public void deleteDocument() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        DocumentCreateEntity<BaseDocument> createdDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // delete document from within the tx
        db.collection(COLLECTION_NAME)
                .deleteDocument(createdDoc.getKey(), null, new DocumentDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the document has not been deleted from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null),
                is(notNullValue()));

        // assert that the document has been deleted from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())), is(nullValue()));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been deleted after commit
        assertThat(db.collection(COLLECTION_NAME).getDocument(createdDoc.getKey(), BaseDocument.class, null),
                is(nullValue()));
    }

    @Test
    public void deleteDocuments() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<String> keys = db.collection(COLLECTION_NAME)
                .insertDocuments(Arrays.asList(new BaseDocument(), new BaseDocument(), new BaseDocument()), null)
                .getDocuments().stream().map(DocumentEntity::getKey).collect(Collectors.toList());

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // delete document from within the tx
        db.collection(COLLECTION_NAME)
                .deleteDocuments(keys, null, new DocumentDeleteOptions().streamTransactionId(tx.getId()));

        // assert that the documents has not been deleted from outside the tx
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments(),
                hasSize(keys.size()));

        // assert that the document has been deleted from within the tx
        assertThat(db.collection(COLLECTION_NAME)
                .getDocuments(keys, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId()))
                .getDocuments(), is(empty()));

        db.commitStreamTransaction(tx.getId());

        // assert that the document has been deleted after commit
        assertThat(db.collection(COLLECTION_NAME).getDocuments(keys, BaseDocument.class, null).getDocuments(),
                is(empty()));
    }

    @Test
    public void documentExists() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        // assert that the document is not found from within the tx
        assertThat(db.collection(COLLECTION_NAME)
                        .documentExists(externalDoc.getKey(), new DocumentExistsOptions().streamTransactionId(tx.getId())),
                is(false));

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    public void count() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        Long initialCount = db.collection(COLLECTION_NAME).count().getCount();

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert a document from outside the tx
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);

        // assert that the document is not counted from within the tx
        assertThat(db.collection(COLLECTION_NAME).count(new CollectionCountOptions().streamTransactionId(tx.getId()))
                .getCount(), is(initialCount));

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    public void truncate() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(), null);

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // truncate document from within the tx
        db.collection(COLLECTION_NAME).truncate(new CollectionTruncateOptions().streamTransactionId(tx.getId()));

        // assert that the collection has not been truncated from outside the tx
        assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(greaterThan(0L)));

        // assert that the collection has been truncated from within the tx
        assertThat(db.collection(COLLECTION_NAME).count(new CollectionCountOptions().streamTransactionId(tx.getId()))
                .getCount(), is(0L));

        db.commitStreamTransaction(tx.getId());

        // assert that the collection has been truncated after commit
        assertThat(db.collection(COLLECTION_NAME).count().getCount(), is(0L));
    }

    @Test
    public void createCursor() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null);

        final Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("@collection", COLLECTION_NAME);
        bindVars.put("key", externalDoc.getKey());

        ArangoCursor<BaseDocument> cursor = db
                .query("FOR doc IN @@collection FILTER doc._key == @key RETURN doc", bindVars,
                        new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class);

        // assert that the document is not found from within the tx
        assertThat(cursor.hasNext(), is(false));

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    public void nextCursor() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        // insert documents from within the tx
        List<String> keys = db.collection(COLLECTION_NAME)
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
        assertThat(docs.stream().map(BaseDocument::getKey).collect(Collectors.toList()),
                containsInAnyOrder(keys.toArray()));

        db.abortStreamTransaction(tx.getId());
    }

    @Test
    public void getStreamTransactions() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx1 = db.beginStreamTransaction(null);
        StreamTransactionEntity tx2 = db.beginStreamTransaction(null);

        List<String> createdIds = Arrays.asList(tx1.getId(), tx2.getId());
        Set<TransactionEntity> gotTxs = db.getStreamTransactions().stream().
                filter(it -> createdIds.contains(it.getId())).collect(Collectors.toSet());

        assertThat(gotTxs.size(), is(createdIds.size()));
        assertThat(gotTxs.stream()
                .allMatch(it -> it.getStatus() == StreamTransactionStatus.running), is(true));

        db.abortStreamTransaction(tx1.getId());
        db.abortStreamTransaction(tx2.getId());
    }

}

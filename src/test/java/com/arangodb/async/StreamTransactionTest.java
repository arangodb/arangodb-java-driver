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
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class StreamTransactionTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_stream_transaction_test";

    public StreamTransactionTest() throws ExecutionException, InterruptedException {
        if (db.collection(COLLECTION_NAME).exists().get())
            db.collection(COLLECTION_NAME).drop().get();

        db.createCollection(COLLECTION_NAME, null).get();
    }

    @AfterEach
    void teardown() throws ExecutionException, InterruptedException {
        if (db.collection(COLLECTION_NAME).exists().get())
            db.collection(COLLECTION_NAME).drop().get();
    }

    @Test
    void beginStreamTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db.beginStreamTransaction(null).get();
        assertThat(tx.getId()).isNotNull();
        assertThat(tx.getStatus()).isEqualTo(StreamTransactionStatus.running);
        db.abortStreamTransaction(tx.getId()).get();
    }

    @Test
    void beginStreamTransactionWithNonExistingCollectionsShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.beginStreamTransaction(new StreamTransactionOptions().writeCollections("notExistingCollection")).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void abortStreamTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null).get();
        StreamTransactionEntity abortedTx = db.abortStreamTransaction(begunTx.getId()).get();

        assertThat(abortedTx.getId()).isNotNull();
        assertThat(abortedTx.getId()).isEqualTo(begunTx.getId());
        assertThat(abortedTx.getStatus()).isEqualTo(StreamTransactionStatus.aborted);
    }

    @Test
    void abortStreamTransactionTwice() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity begunTx = db.beginStreamTransaction(null).get();
        db.abortStreamTransaction(begunTx.getId()).get();
        db.abortStreamTransaction(begunTx.getId()).get();
    }

    @Test
    void abortStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.abortStreamTransaction("000000").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void abortStreamTransactionWithInvalidTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.abortStreamTransaction("invalidTransactionId").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void abortCommittedStreamTransactionShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.commitStreamTransaction(createdTx.getId()).get();

        try {
            db.abortStreamTransaction(createdTx.getId()).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getStreamTransaction() throws ExecutionException, InterruptedException {
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

    @Test
    void getStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.getStreamTransaction("000000").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getStreamTransactionWithInvalidTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.getStreamTransaction("invalidTransactionId").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void commitStreamTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        StreamTransactionEntity committedTx = db.commitStreamTransaction(createdTx.getId()).get();

        assertThat(committedTx.getId()).isNotNull();
        assertThat(committedTx.getId()).isEqualTo(createdTx.getId());
        assertThat(committedTx.getStatus()).isEqualTo(StreamTransactionStatus.committed);
    }

    @Test
    void commitStreamTransactionTwice() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.commitStreamTransaction(createdTx.getId()).get();
        db.commitStreamTransaction(createdTx.getId()).get();
    }

    @Test
    void commitStreamTransactionWhenTransactionIdDoesNotExistsShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.commitStreamTransaction("000000").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void commitStreamTransactionWithInvalidTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.commitStreamTransaction("invalidTransactionId").get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void commitAbortedStreamTransactionShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity createdTx = db.beginStreamTransaction(null).get();
        db.abortStreamTransaction(createdTx.getId()).get();

        try {
            db.commitStreamTransaction(createdTx.getId()).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getDocument() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx = db
                .beginStreamTransaction(new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();

        // insert a document from outside the tx
        DocumentCreateEntity<BaseDocument> externalDoc = db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(), null).get();

        // assert that the document is not found from within the tx
        assertThat(db.collection(COLLECTION_NAME).getDocument(externalDoc.getKey(), BaseDocument.class,
                new DocumentReadOptions().streamTransactionId(tx.getId())).get()).isNull();

        db.abortStreamTransaction(tx.getId()).get();
    }

    @Test
    void getDocumentWithNonExistingTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.collection(COLLECTION_NAME)
                    .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("123456"))
                    .get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void insertDocumentWithNonExistingTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.collection(COLLECTION_NAME)
                    .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId("123456")).get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getDocumentWithInvalidTransactionIdShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        try {
            db.collection(COLLECTION_NAME)
                    .getDocument("docId", BaseDocument.class, new DocumentReadOptions().streamTransactionId("abcde"))
                    .get();
            fail();
        } catch (ExecutionException e) {
            assertThat(e.getCause()).isInstanceOf(ArangoDBException.class);
        }
    }

    @Test
    void getStreamTransactions() throws ExecutionException, InterruptedException {
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
                .allMatch(it -> it.getStatus() == StreamTransactionStatus.running)).isTrue();

        db.abortStreamTransaction(tx1.getId()).get();
        db.abortStreamTransaction(tx2.getId()).get();
    }

}

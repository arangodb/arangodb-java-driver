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
import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.UUID;

import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class ConcurrentStreamTransactionsTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test";

    public ConcurrentStreamTransactionsTest(final Builder builder) {
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
    public void conflictOnInsertDocumentWithNotYetCommittedTx() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx1 = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        StreamTransactionEntity tx2 = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        String key = UUID.randomUUID().toString();

        // insert a document from within tx1
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(key), new DocumentCreateOptions().streamTransactionId(tx1.getId()));

        try {
            // insert conflicting document from within tx2
            db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key),
                    new DocumentCreateOptions().streamTransactionId(tx2.getId()));

            throw new RuntimeException("This should never be thrown");
        } catch (ArangoDBException e) {
            e.printStackTrace();
        }

        db.abortStreamTransaction(tx1.getId());
        db.abortStreamTransaction(tx2.getId());
    }

    @Test
    public void conflictOnInsertDocumentWithAlreadyCommittedTx() {
        assumeTrue(isSingleServer());
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        StreamTransactionEntity tx1 = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        StreamTransactionEntity tx2 = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME).writeCollections(COLLECTION_NAME));

        String key = UUID.randomUUID().toString();

        // insert a document from within tx1
        db.collection(COLLECTION_NAME)
                .insertDocument(new BaseDocument(key), new DocumentCreateOptions().streamTransactionId(tx1.getId()));

        // commit tx1
        db.commitStreamTransaction(tx1.getId());

        try {
            // insert conflicting document from within tx2
            db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key),
                    new DocumentCreateOptions().streamTransactionId(tx2.getId()));

            throw new RuntimeException("This should never be thrown");
        } catch (ArangoDBException e) {
            e.printStackTrace();
        }

        db.abortStreamTransaction(tx2.getId());
    }
}

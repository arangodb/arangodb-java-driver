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

import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
class StreamTransactionConflictsTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test-" + UUID.randomUUID();

    @BeforeAll
    static void init() {
        initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void conflictOnInsertDocumentWithNotYetCommittedTx(ArangoDatabase db) {
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

        // insert conflicting document from within tx2
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key),
                new DocumentCreateOptions().streamTransactionId(tx2.getId())));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;

        if (isAtLeastVersion(3, 8)) {
            assertThat(e.getResponseCode()).isEqualTo(409);
            assertThat(e.getErrorNum()).isEqualTo(1200);
        }

        db.abortStreamTransaction(tx1.getId());
        db.abortStreamTransaction(tx2.getId());
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    void conflictOnInsertDocumentWithAlreadyCommittedTx(ArangoDatabase db) {
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

        // insert conflicting document from within tx2
        Throwable thrown = catchThrowable(() -> db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key),
                new DocumentCreateOptions().streamTransactionId(tx2.getId())));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException e = (ArangoDBException) thrown;
        if (isAtLeastVersion(3, 8)) {
            assertThat(e.getResponseCode()).isEqualTo(409);
            assertThat(e.getErrorNum()).isEqualTo(1200);
        }

        db.abortStreamTransaction(tx2.getId());
    }
}

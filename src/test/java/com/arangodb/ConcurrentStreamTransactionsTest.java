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
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Michele Rastelli
 */
public class ConcurrentStreamTransactionsTest extends BaseJunit5 {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test";

    @BeforeAll
    public static void init() {
        BaseJunit5.initCollections(COLLECTION_NAME);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void concurrentWriteWithinSameTransaction(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        List<CompletableFuture<DocumentCreateEntity<BaseDocument>>> reqs = IntStream.range(0, 100)
                .mapToObj(it -> CompletableFuture.supplyAsync(() -> db.collection(COLLECTION_NAME)
                                .insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tx.getId())),
                        executor))
                .collect(Collectors.toList());

        List<DocumentCreateEntity<BaseDocument>> results = reqs.stream().map(CompletableFuture::join).collect(Collectors.toList());
        db.commitStreamTransaction(tx.getId());

        results.forEach(it -> {
            assertThat(it.getKey()).isNotNull();
            assertThat(db.collection(COLLECTION_NAME).documentExists(it.getKey())).isTrue();
        });

        executor.shutdownNow();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void concurrentAqlWriteWithinSameTransaction(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        List<CompletableFuture<ArangoCursor<BaseDocument>>> reqs = IntStream.range(0, 100)
                .mapToObj(it -> CompletableFuture.supplyAsync(() -> {
                            Map<String, Object> params = new HashMap<>();
                            params.put("doc", new BaseDocument("key-" + UUID.randomUUID()));
                            params.put("@col", COLLECTION_NAME);
                            return db.query("INSERT @doc INTO @@col RETURN NEW", params,
                                    new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class);
                        },
                        executor))
                .collect(Collectors.toList());

        List<ArangoCursor<BaseDocument>> results = reqs.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        db.commitStreamTransaction(tx.getId());

        results.forEach(it -> {
            String key = it.iterator().next().getKey();
            assertThat(key).isNotNull();
            assertThat(db.collection(COLLECTION_NAME).documentExists(key)).isTrue();
        });

        executor.shutdownNow();
    }


    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void failingAqlFromBTS57(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        String key = "key-" + UUID.randomUUID();
        String id = COLLECTION_NAME + "/" + key;
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("@col", COLLECTION_NAME);
        ArangoCursor<Boolean> req = db.query("" +
                        "LET d = DOCUMENT(@id)" +
                        "UPDATE d WITH { \"aaa\": \"aaa\" } IN @@col " +
                        "RETURN true",
                params,
                new AqlQueryOptions().streamTransactionId(tx.getId()), Boolean.class);

        db.commitStreamTransaction(tx.getId());
        assertThat(req.next()).isTrue();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void concurrentReadWithinSameTransaction(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        String key = "key-" + UUID.randomUUID();
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        List<CompletableFuture<BaseDocument>> reqs = IntStream.range(0, 100)
                .mapToObj(it -> CompletableFuture.supplyAsync(() -> db.collection(COLLECTION_NAME)
                                .getDocument(key, BaseDocument.class, new DocumentReadOptions().streamTransactionId(tx.getId())),
                        executor))
                .collect(Collectors.toList());

        List<BaseDocument> results = reqs.stream().map(CompletableFuture::join).collect(Collectors.toList());
        db.commitStreamTransaction(tx.getId());

        results.forEach(it -> assertThat(it.getKey()).isNotNull());

        executor.shutdownNow();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    public void concurrentAqlReadWithinSameTransaction(ArangoDatabase db) {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        String key = "key-" + UUID.randomUUID();
        String id = COLLECTION_NAME + "/" + key;
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME));

        List<CompletableFuture<ArangoCursor<BaseDocument>>> reqs = IntStream.range(0, 100)
                .mapToObj(it -> CompletableFuture.supplyAsync(() -> db.query(
                        "RETURN DOCUMENT(@id)",
                        Collections.singletonMap("id", id),
                        new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class),
                        executor))
                .collect(Collectors.toList());

        List<ArangoCursor<BaseDocument>> results = reqs.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        db.commitStreamTransaction(tx.getId());
        results.forEach(it -> assertThat(it.iterator().next().getKey()).isEqualTo(key));
        executor.shutdownNow();
    }

}

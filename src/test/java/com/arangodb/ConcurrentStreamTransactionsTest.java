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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class ConcurrentStreamTransactionsTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test";

    public ConcurrentStreamTransactionsTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    @BeforeClass
    public static void init() {
        BaseTest.initCollections(COLLECTION_NAME);
    }

    @Test
    public void concurrentWriteWithinSameTransaction() {
        assumeTrue(isAtLeastVersion(3, 5));
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
            assertThat(it.getKey(), is(notNullValue()));
            assertThat(db.collection(COLLECTION_NAME).documentExists(it.getKey()), is(true));
        });

        executor.shutdownNow();
    }

    @Test
    public void concurrentAqlWriteWithinSameTransaction() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        List<CompletableFuture<ArangoCursor<BaseDocument>>> reqs = IntStream.range(0, 100)
                .mapToObj(it -> CompletableFuture.supplyAsync(() -> {
                            Map<String, Object> params = new HashMap<>();
                            params.put("doc", new BaseDocument("key-" + UUID.randomUUID().toString()));
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
            assertThat(key, is(notNullValue()));
            assertThat(db.collection(COLLECTION_NAME).documentExists(key), is(true));
        });

        executor.shutdownNow();
    }


    @Test
    public void failingAqlFromBTS57() {
        assumeTrue(isAtLeastVersion(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        String key = "key-" + UUID.randomUUID().toString();
        String id = COLLECTION_NAME + "/" + key;
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key));

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME));

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("@col", COLLECTION_NAME);
        ArangoCursor<Boolean> req = db.query("" +
                        "LET d = DOCUMENT(@id)\n" +
                        "UPDATE d WITH { \"aaa\": \"aaa\" } IN @@col " +
                        "RETURN true",
                params,
                new AqlQueryOptions().streamTransactionId(tx.getId()), Boolean.class);

        db.commitStreamTransaction(tx.getId());
        assertThat(req.next(), is(true));
    }

    @Test
    public void concurrentReadWithinSameTransaction() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        String key = "key-" + UUID.randomUUID().toString();
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

        results.forEach(it -> assertThat(it.getKey(), is(notNullValue())));

        executor.shutdownNow();
    }

    @Test
    public void concurrentAqlReadWithinSameTransaction() {
        assumeTrue(isAtLeastVersion(3, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));
        ExecutorService executor = Executors.newCachedThreadPool();

        String key = "key-" + UUID.randomUUID().toString();
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
        results.forEach(it -> assertThat(it.iterator().next().getKey(), is(key)));
        executor.shutdownNow();
    }

}

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
import com.arangodb.ArangoIterator;
import com.arangodb.entity.ArangoDBEngine;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.model.*;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class ConcurrentStreamTransactionsTest extends BaseTest {

    private static final String COLLECTION_NAME = "db_concurrent_stream_transactions_test";

    public ConcurrentStreamTransactionsTest() throws ExecutionException, InterruptedException {
        if (db.collection(COLLECTION_NAME).exists().get())
            db.collection(COLLECTION_NAME).drop().get();

        db.createCollection(COLLECTION_NAME, null).get();
    }

    @After
    public void teardown() throws ExecutionException, InterruptedException {
        if (db.collection(COLLECTION_NAME).exists().get())
            db.collection(COLLECTION_NAME).drop().get();
    }


    @Test
    public void aqlWithDocumentExpression() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        String key = "key-" + UUID.randomUUID().toString();
        String id = COLLECTION_NAME + "/" + key;
        db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key)).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME)).get();

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("@col", COLLECTION_NAME);
        CompletableFuture<ArangoCursorAsync<Boolean>> req = db.query("" +
                        "LET d = DOCUMENT(@id)\n" +
                        "UPDATE d WITH { \"aaa\": \"aaa\" } IN @@col " +
                        "RETURN true",
                params,
                new AqlQueryOptions().streamTransactionId(tx.getId()), Boolean.class);

        req.get();
        db.commitStreamTransaction(tx.getId()).get();
    }


    /**
     * It performs the following steps 10 times:
     * - create a document having key <key> in the document collection <c>
     * - begin a transaction <t> having readCollections: [<c>]
     * - send requests getDocument(<key>) in collection <c> from within <t> 1000 times in parallel
     * - wait for the responses of all getDocument requests
     * - commit <t>
     */
    @Test
    public void concurrentReadWithinSameTransaction()
            throws ExecutionException, InterruptedException {

        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            final String key = "key-" + UUID.randomUUID().toString();
            db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key)).get();

            final StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().readCollections(COLLECTION_NAME)
            ).get();

            final List<CompletableFuture<BaseDocument>> requests = IntStream.range(0, 1_000)
                    .mapToObj(it -> db
                            .collection(COLLECTION_NAME)
                            .getDocument(
                                    key,
                                    BaseDocument.class,
                                    new DocumentReadOptions().streamTransactionId(tx.getId())
                            )
                    )
                    .collect(Collectors.toList());

            for (final CompletableFuture<BaseDocument> request : requests) {
                BaseDocument result = request.get();
                assertThat(result.getKey(), is(key));
            }
            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps 10 times:
     * - begin a transaction <t> in collection <c> having writeCollections: [<c>]
     * - send requests insertDocument(new empty document) in collection <c> from within <t> 1000 times in parallel
     * - wait for the responses of all insertDocument requests
     * - commit <t>
     */
    @Test
    public void concurrentWriteWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().writeCollections(COLLECTION_NAME)
            ).get();

            List<CompletableFuture<DocumentCreateEntity<BaseDocument>>> requests = IntStream.range(0, 1_000)
                    .mapToObj(it -> db
                            .collection(COLLECTION_NAME)
                            .insertDocument(
                                    new BaseDocument(),
                                    new DocumentCreateOptions().streamTransactionId(tx.getId())
                            )
                    )
                    .collect(Collectors.toList());

            for (CompletableFuture<DocumentCreateEntity<BaseDocument>> request : requests) {
                DocumentCreateEntity<BaseDocument> result = request.get();
                assertThat(result.getKey(), is(notNullValue()));
            }

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps 10 times:
     * - begin a transaction <t> in document collection <c> having readCollections: [<c>] and writeCollections: [<c>]
     * - send the following requests 500 times in parallel:
     * -   insertDocument(new empty document) in collection <c> from within <t>
     * -   getDocument(insertedDocumentKey) in collection <c> from within <t>
     * - wait for the responses of all requests
     * - commit <t>
     *
     * @apiNote not supported, see {@link #concurrentReadAndWriteWithinSameTransactionShouldThrow()} failure test
     */
    @Test
    @Ignore
    public void concurrentReadAndWriteWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);

        for (int i = 0; i < 10; i++) {
            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().writeCollections(COLLECTION_NAME).readCollections(COLLECTION_NAME)
            ).get();

            List<CompletableFuture<BaseDocument>> requests = IntStream.range(0, 500)
                    .mapToObj(it -> collection
                            .insertDocument(
                                    new BaseDocument(),
                                    new DocumentCreateOptions().streamTransactionId(tx.getId())
                            )
                            .thenCompose(created -> collection
                                    .getDocument(
                                            created.getKey(),
                                            BaseDocument.class,
                                            new DocumentReadOptions().streamTransactionId(tx.getId())
                                    )
                            )
                    )
                    .collect(Collectors.toList());

            for (CompletableFuture<BaseDocument> request : requests) {
                BaseDocument result = request.get();
                assertThat(result.getKey(), is(notNullValue()));
            }

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps:
     * - insertDocument in collection <c>, having key <key>
     * - begin a transaction <t> in document collection <c> having readCollections: [<c>] and writeCollections: [<c>]
     * - send the following requests 1000 times in parallel:
     * -   updateDocument(<key>) in collection <c> from within <t>
     * -   getDocument(<key>) in collection <c> from within <t>
     * - wait for the responses of all requests
     * - expect error from db
     */
    @Test
    @Ignore // this test fails to produce the expected error in cluster, ignoring for now
    public void concurrentReadAndWriteWithinSameTransactionShouldThrow() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        ArangoCollectionAsync collection = db.collection(COLLECTION_NAME);
        DocumentCreateEntity<BaseDocument> doc = collection.insertDocument(new BaseDocument()).get();

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().writeCollections(COLLECTION_NAME).readCollections(COLLECTION_NAME)
        ).get();

        List<CompletableFuture<?>> requests = IntStream.range(0, 1000)
                .boxed()
                .flatMap(it -> Stream.of(
                        collection
                                .updateDocument(
                                        doc.getKey(),
                                        new BaseDocument(),
                                        new DocumentUpdateOptions().streamTransactionId(tx.getId())
                                ),
                        collection
                                .getDocument(
                                        doc.getKey(),
                                        BaseDocument.class,
                                        new DocumentReadOptions().streamTransactionId(tx.getId())
                                )
                ))
                .collect(Collectors.toList());

        try {
            for (CompletableFuture<?> request : requests) {
                request.get();
            }
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof ArangoDBException)) {
                throw e;
            }
        }
    }


    /**
     * It performs the following steps 10 times:
     * - create a document having key <key> in the document collection <c>
     * - begin a transaction <t> having readCollections: [<c>]
     * - send 1000 requests in parallel creating an AQL cursor which reads the document <key> in collection <c> from within <t>
     * - wait for the responses of all cursor requests
     * - commit <t>
     */
    @Test
    public void concurrentAqlReadWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            String key = "key-" + UUID.randomUUID().toString();
            String id = COLLECTION_NAME + "/" + key;
            db.collection(COLLECTION_NAME).insertDocument(new BaseDocument(key)).get();

            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();

            List<CompletableFuture<ArangoCursorAsync<BaseDocument>>> requests = IntStream.range(0, 1_000)
                    .mapToObj(it -> db.query(
                            "RETURN DOCUMENT(@id)",
                            Collections.singletonMap("id", id),
                            new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class))
                    .collect(Collectors.toList());

            for (CompletableFuture<ArangoCursorAsync<BaseDocument>> request : requests) {
                assertThat(request.get().iterator().next().getKey(), is(key));
            }

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps 10 times:
     * - begin a transaction <t> in collection <c> having writeCollections: [<c>]
     * - send 1000 requests in parallel creating an AQL cursor which inserts a document in collection <c> from within <t>
     * - wait for the responses of all cursor requests
     * - commit <t>
     */
    @Test
    public void concurrentAqlWriteWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().writeCollections(COLLECTION_NAME)
            ).get();

            List<CompletableFuture<ArangoCursorAsync<BaseDocument>>> requests = IntStream.range(0, 1_000)
                    .mapToObj(it -> {
                        Map<String, Object> params = new HashMap<>();
                        params.put("doc", new BaseDocument("key-" + UUID.randomUUID().toString()));
                        params.put("@col", COLLECTION_NAME);
                        return db.query("INSERT @doc INTO @@col RETURN NEW", params,
                                new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class);
                    })
                    .collect(Collectors.toList());

            for (CompletableFuture<ArangoCursorAsync<BaseDocument>> request : requests) {
                String key = request.get().iterator().next().getKey();
                assertThat(key, is(notNullValue()));
            }

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps 10 times:
     * - begin a transaction <t> in document collection <c> having readCollections: [<c>] and writeCollections: [<c>]
     * - send the following requests 500 times in parallel:
     * -   create an AQL cursor which inserts a document in collection <c> from within <t>
     * -   create an AQL cursor which reads the inserted document from within <t>
     * - wait for the responses of all requests
     * - commit <t>
     */
    @Test
    public void concurrentAqlReadAndWriteWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 5, 6) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().writeCollections(COLLECTION_NAME)
            ).get();

            List<CompletableFuture<ArangoCursorAsync<BaseDocument>>> requests = IntStream.range(0, 500)
                    .mapToObj(it -> {
                        Map<String, Object> params = new HashMap<>();
                        params.put("doc", new BaseDocument("key-" + UUID.randomUUID().toString()));
                        params.put("@col", COLLECTION_NAME);
                        return db
                                .query(
                                        "INSERT @doc INTO @@col RETURN NEW",
                                        params,
                                        new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class
                                ).thenCompose(cursor -> db.query(
                                        "RETURN DOCUMENT(@id)",
                                        Collections.singletonMap("id", cursor.next().getId()),
                                        new AqlQueryOptions().streamTransactionId(tx.getId()), BaseDocument.class
                                ));
                    })
                    .collect(Collectors.toList());

            for (CompletableFuture<ArangoCursorAsync<BaseDocument>> request : requests) {
                String key = request.get().iterator().next().getKey();
                assertThat(key, is(notNullValue()));
            }

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps 10 times:
     * - create 100 documents in the document collection <c>
     * - begin a transaction <t> having readCollections: [<c>]
     * - send 10 requests in parallel each one creating a stream AQL cursor which reads all the documents in collection <c> from within <t>, batch size 10
     * - wait for the responses of all cursor requests
     * - consume all the batches of all cursors in parallel
     * - commit <t>
     *
     * @apiNote not supported, see {@link #concurrentAqlStreamWithinSameTransactionShouldThrow()} failure test
     */
    @Test
    @Ignore
    public void concurrentAqlStreamWithinSameTransaction() throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 6, 5));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        for (int i = 0; i < 10; i++) {
            List<CompletableFuture<DocumentCreateEntity<BaseDocument>>> inserts = IntStream.range(0, 100)
                    .mapToObj(it -> db.collection(COLLECTION_NAME).insertDocument(new BaseDocument()))
                    .collect(Collectors.toList());

            for (CompletableFuture<DocumentCreateEntity<BaseDocument>> insert : inserts) {
                insert.get();
            }

            StreamTransactionEntity tx = db.beginStreamTransaction(
                    new StreamTransactionOptions().readCollections(COLLECTION_NAME)).get();

            List<CompletableFuture<ArangoCursorAsync<BaseDocument>>> requests = IntStream.range(0, 10)
                    .mapToObj(it -> db.query(
                            "FOR doc IN @@col RETURN doc",
                            Collections.singletonMap("@col", COLLECTION_NAME),
                            new AqlQueryOptions()
                                    .streamTransactionId(tx.getId())
                                    .stream(true)
                                    .batchSize(10),
                            BaseDocument.class))
                    .collect(Collectors.toList());

            List<ArangoIterator<BaseDocument>> cursors = new ArrayList<>();
            for (CompletableFuture<ArangoCursorAsync<BaseDocument>> request : requests) {
                cursors.add(request.get().iterator());
            }

            boolean additionalElements;
            do {
                long remainingCursorsCount = cursors.stream()
                        .filter(Iterator::hasNext)
                        .map(Iterator::next)
                        .count();
                additionalElements = remainingCursorsCount > 0;
            } while (additionalElements);

            db.commitStreamTransaction(tx.getId()).get();
        }
    }


    /**
     * It performs the following steps:
     * - create 100 documents in the document collection <c>
     * - begin a transaction <t> having readCollections: [<c>]
     * - send 10 requests in parallel each one creating a stream AQL cursor which reads all the documents in collection <c> from within <t>, batch size 10
     * - wait for the responses of all cursor requests
     * - consume all the batches of all cursors in parallel
     * - expect error from db
     */
    @Test(timeout = 300_000L)
    public void concurrentAqlStreamWithinSameTransactionShouldThrow() throws ExecutionException, InterruptedException, IOException {
        assumeTrue(isAtLeastVersion(3, 7) ||
                isMinorVersionAndAtLeastPatch(3, 6, 6));
        assumeTrue(isStorageEngine(ArangoDBEngine.StorageEngineName.rocksdb));

        List<CompletableFuture<DocumentCreateEntity<BaseDocument>>> inserts = IntStream.range(0, 100)
                .mapToObj(it -> db.collection(COLLECTION_NAME).insertDocument(new BaseDocument()))
                .collect(Collectors.toList());

        for (CompletableFuture<DocumentCreateEntity<BaseDocument>> insert : inserts) {
            insert.get();
        }

        StreamTransactionEntity tx = db.beginStreamTransaction(
                new StreamTransactionOptions().readCollections(COLLECTION_NAME)
        ).get();

        List<CompletableFuture<ArangoCursorAsync<BaseDocument>>> requests = IntStream.range(0, 10)
                .mapToObj(it -> db.query(
                        "FOR doc IN @@col RETURN doc",
                        Collections.singletonMap("@col", COLLECTION_NAME),
                        new AqlQueryOptions()
                                .streamTransactionId(tx.getId())
                                .stream(true)
                                .batchSize(10),
                        BaseDocument.class))
                .collect(Collectors.toList());

        boolean threw = false;
        List<ArangoCursorAsync<BaseDocument>> cursors = new ArrayList<>();
        for (CompletableFuture<ArangoCursorAsync<BaseDocument>> request : requests) {
            try {
                cursors.add(request.get());
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ArangoDBException) {
                    threw = true;
                } else {
                    throw e;
                }
            }
        }

        for (ArangoCursorAsync<BaseDocument> it : cursors) {
            it.close();
        }

        db.abortStreamTransaction(tx.getId()).get();

        if (!threw)
            fail();
    }

}

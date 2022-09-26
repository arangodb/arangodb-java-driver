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

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.StreamTransactionStatus;
import com.arangodb.model.CollectionCountOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
public class StreamTransactionExclusiveParallelTest extends BaseJunit5 {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamTransactionExclusiveParallelTest.class);

    // transactions created during the test
    private final ConcurrentSkipListSet<String> txs = new ConcurrentSkipListSet<>();

    private final ExecutorService es = Executors.newFixedThreadPool(200);

    private final ConcurrentMap<String, LongAdder> runningCountByCollection = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> committedCountByCollection = new ConcurrentHashMap<>();

    @BeforeAll
    public static void init() {
        BaseJunit5.initDB();
    }

    @AfterEach
    public void abortTransactions() {
        for (String tx : txs) {
            ArangoDatabase db = BaseJunit5.dbsStream().iterator().next();
            if (StreamTransactionStatus.running.equals(db.getStreamTransaction(tx).getStatus())) {
                LOGGER.info("aborting " + tx);
                db.abortStreamTransaction(tx);
            }
        }
    }

    /**
     * Executes 10 times in parallel the following:
     * <p>
     * Executes 2 parallel stream transactions with exclusive lock on the same collection "c", which has 2 shards. Each
     * transaction inserts one document to different shard.
     * <p>
     * Observed failures:
     * <p>
     * - 3.6.12: Expected exactly 1 transaction running, but got: 2 (after having both inserted a document)
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    @Timeout(value = 15_000, unit = TimeUnit.MILLISECONDS)
    public void parallelExclusiveStreamTransactions(ArangoDatabase db) throws ExecutionException, InterruptedException {
        System.out.println("===================================");
        parallelizeTestsExecution(db, false);
    }

    /**
     * Executes 10 times in parallel the following:
     * <p>
     * Executes 2 parallel stream transactions with exclusive lock on the same collection "c", which has 2 shards. Each
     * transaction inserts one document to different shard. Then each transaction counts the collection documents.
     * <p>
     * Observed results:
     * <p>
     * - 3.6.12: test timed out after 15000 milliseconds due to deadlock on collection.count()
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("dbs")
    @Timeout(value = 15_000, unit = TimeUnit.MILLISECONDS)
    public void parallelExclusiveStreamTransactionsCounting(ArangoDatabase db) throws ExecutionException, InterruptedException {
        System.out.println("===================================");
        parallelizeTestsExecution(db, true);
    }

    private void parallelizeTestsExecution(ArangoDatabase db, boolean counting) throws ExecutionException, InterruptedException {
        assumeTrue(isAtLeastVersion(3, 7));
        assumeTrue(isCluster());

        CompletableFuture
                .allOf(
                        IntStream.range(0, 10)
                                .mapToObj(i -> doParallelExclusiveStreamTransactions(db, counting))
                                .collect(Collectors.toList())
                                .toArray(new CompletableFuture[10])
                )
                .get();

        // expect that all txs are committed
        for (String tx : txs) {
            StreamTransactionStatus status = db.getStreamTransaction(tx).getStatus();
            assertThat(status).isEqualTo(StreamTransactionStatus.committed);
        }

        es.shutdown();
    }

    private CompletableFuture<Void> doParallelExclusiveStreamTransactions(ArangoDatabase db, boolean counting) {

        final String colName = "col-" + UUID.randomUUID();

        // create collection with 2 shards
        final ArangoCollection col = db.collection(colName);
        if (!col.exists()) {
            db.createCollection(colName, new CollectionCreateOptions().numberOfShards(2));
        }

        // create 2 BaseDocuments having keys belonging to different shards
        final String k1 = "key-" + UUID.randomUUID();
        final BaseDocument d1 = new BaseDocument(k1);
        final String k1Shard = col.getResponsibleShard(d1).getShardId();
        String k;
        BaseDocument d;
        do {
            k = "key-" + UUID.randomUUID();
            d = new BaseDocument(k);
        } while (k1Shard.equals(col.getResponsibleShard(d).getShardId()));
        final BaseDocument d2 = d;

        // run actual test tasks with 2 threads in parallel
        return CompletableFuture
                .allOf(
                        CompletableFuture.runAsync(() -> executeTask(db, "1-" + colName, d1, colName, counting), es),
                        CompletableFuture.runAsync(() -> executeTask(db, "2-" + colName, d2, colName, counting), es)
                );
    }

    private void executeTask(ArangoDatabase db, String idx, BaseDocument d, String colName, boolean counting) {
        runningCountByCollection.computeIfAbsent(colName, k -> new LongAdder());
        committedCountByCollection.computeIfAbsent(colName, k -> new LongAdder());

        LOGGER.info("{}: beginning tx", idx);
        final String tx = db.beginStreamTransaction(new StreamTransactionOptions().exclusiveCollections(colName)).getId();
        runningCountByCollection.get(colName).increment();
        txs.add(tx);
        LOGGER.info("{}: begun tx #{}", idx, tx);

        LOGGER.info("{}: inserting document", idx);
        db.collection(colName).insertDocument(d, new DocumentCreateOptions().streamTransactionId(tx));
        LOGGER.info("{}: inserted document", idx);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (counting) {
            LOGGER.info("{}: counting collection documents", idx);
            Long count = db.collection(colName).count(new CollectionCountOptions().streamTransactionId(tx)).getCount();
            LOGGER.info("{}: counted {} collection documents", idx, count);

            if (count != 1 + committedCountByCollection.get(colName).intValue()) {
                throw new RuntimeException("tx #" + tx + ": transaction sees wrong count: " + count);
            }
        }

        // expect exactly 1 transaction running
        LOGGER.info("{}: tx status: {}", idx, db.getStreamTransaction(tx).getStatus());
        int runningTransactions = runningCountByCollection.get(colName).intValue();
        if (runningTransactions != 1) {
            throw new RuntimeException("Expected exactly 1 transaction running, but got: " + runningTransactions);
        }

        LOGGER.info("{}: committing", idx);
        db.commitStreamTransaction(tx);
        runningCountByCollection.get(colName).decrement();
        committedCountByCollection.get(colName).increment();
        LOGGER.info("{}: committed", idx);

    }

}

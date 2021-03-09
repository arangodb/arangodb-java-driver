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
import com.arangodb.entity.StreamTransactionEntity;
import com.arangodb.entity.StreamTransactionStatus;
import com.arangodb.model.CollectionCountOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class StreamTransactionExclusiveParallelTest extends BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamTransactionExclusiveParallelTest.class);

    private final String colName = "col-" + UUID.randomUUID().toString();

    // transactions created during the test
    private final ConcurrentSkipListSet<String> txs = new ConcurrentSkipListSet<>();

    @BeforeClass
    public static void init() {
        BaseTest.initDB();
    }

    public StreamTransactionExclusiveParallelTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    @After
    public void abortTransactions() {
        for (String tx : txs) {
            if (StreamTransactionStatus.running.equals(db.getStreamTransaction(tx).getStatus())) {
                LOGGER.info("aborting " + tx);
                db.abortStreamTransaction(tx);
            }
        }
    }

    /**
     * Executes 2 parallel stream transactions with exclusive lock on the same collection "c", which has 2 shards. Each
     * transaction inserts one document to different shard.
     * <p>
     * Observed failures:
     * <p>
     * - 3.6.12: Expected exactly 1 transaction running, but got: 2 (after having both inserted a document)
     */
    @Test(timeout = 15_000)
    public void parallelExclusiveStreamTransactions() throws ExecutionException, InterruptedException {
        doParallelExclusiveStreamTransactions(false);
    }

    /**
     * Executes 2 parallel stream transactions with exclusive lock on the same collection "c", which has 2 shards. Each
     * transaction inserts one document to different shard. Then each transaction counts the collection documents.
     * <p>
     * Observed results:
     * <p>
     * - 3.6.12: test timed out after 15000 milliseconds due to deadlock on collection.count()
     */
    @Test(timeout = 15_000)
    public void parallelExclusiveStreamTransactionsCounting() throws ExecutionException, InterruptedException {
        doParallelExclusiveStreamTransactions(true);
    }

    private void doParallelExclusiveStreamTransactions(boolean counting) throws InterruptedException, ExecutionException {
        assumeTrue(isCluster());

        // create collection with 2 shards
        final ArangoCollection col = db.collection(colName);
        if (!col.exists()) {
            db.createCollection(colName, new CollectionCreateOptions().numberOfShards(2));
        }

        // create 2 BaseDocuments having keys belonging to different shards
        final String k1 = "key-" + UUID.randomUUID().toString();
        final BaseDocument d1 = new BaseDocument(k1);
        final String k1Shard = col.getResponsibleShard(d1).getShardId();
        String k;
        BaseDocument d;
        do {
            k = "key-" + UUID.randomUUID().toString();
            d = new BaseDocument(k);
        } while (k1Shard.equals(col.getResponsibleShard(d).getShardId()));
        final BaseDocument d2 = d;

        // run actual test tasks with 2 threads in parallel
        ExecutorService es = Executors.newFixedThreadPool(10);
        CompletableFuture
                .allOf(
                        CompletableFuture.runAsync(() -> executeTask(1, d1, counting), es),
                        CompletableFuture.runAsync(() -> executeTask(2, d2, counting), es)
                )
                .get();
        es.shutdown();

        // expect that all txs are committed
        for (String tx : txs) {
            StreamTransactionStatus status = db.getStreamTransaction(tx).getStatus();
            assertThat(status, is(StreamTransactionStatus.committed));
        }
    }

    private void executeTask(int idx, BaseDocument d, boolean counting) {
        LOGGER.info("{}: beginning tx", idx);
        final String tx = db.beginStreamTransaction(new StreamTransactionOptions().exclusiveCollections(colName)).getId();
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

            long committedTxs = txs.stream()
                    .map(db::getStreamTransaction)
                    .map(StreamTransactionEntity::getStatus)
                    .filter(StreamTransactionStatus.committed::equals)
                    .count();

            if (count != 1 + committedTxs) {
                StreamTransactionStatus status = db.getStreamTransaction(tx).getStatus();
                throw new RuntimeException("tx #" + tx + " [status: " + status + "]: transaction sees wrong count: " + count);
            }
        }

        // expect exactly 1 transaction running
        LOGGER.info("{}: tx status: {}", idx, db.getStreamTransaction(tx).getStatus());
        long runningTransactions = txs.stream()
                .map(db::getStreamTransaction)
                .map(StreamTransactionEntity::getStatus)
                .filter(StreamTransactionStatus.running::equals)
                .count();
        if (runningTransactions != 1) {
            throw new RuntimeException("Expected exactly 1 transaction running, but got: " + runningTransactions);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("{}: committing", idx);
        db.commitStreamTransaction(tx);
        LOGGER.info("{}: committed", idx);

    }

}

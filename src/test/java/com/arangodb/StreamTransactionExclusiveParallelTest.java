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
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.StreamTransactionOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Michele Rastelli
 */
@RunWith(Parameterized.class)
public class StreamTransactionExclusiveParallelTest extends BaseTest {
    static {
        // http logging
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "ERROR");
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamTransactionExclusiveParallelTest.class);

    @BeforeClass
    public static void init() {
        BaseTest.initDB();
    }

    public StreamTransactionExclusiveParallelTest(final ArangoDB arangoDB) {
        super(arangoDB);
    }

    /**
     * Executes parallel stream transactions with exclusive lock on the same table
     */
    @Test(timeout = 15_000)
    public void parallelExclusiveStreamTransactions() throws InterruptedException {
        String colName = "col-" + UUID.randomUUID().toString();
        ArangoCollection col = db.collection(colName);
        if (!col.exists())
            col.create();

        List<Thread> threads = IntStream.range(0, 10)
                .mapToObj(i -> new Thread(() -> executeTx(colName)))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

    }

    private void executeTx(String colName) {
        for (int i = 0; i < 10; i++) {
            String tid = db.beginStreamTransaction(new StreamTransactionOptions()
                    .lockTimeout(0)
                    .exclusiveCollections(colName)
            ).getId();
            LOGGER.info(tid + " begun");
            db.collection(colName).insertDocument(new BaseDocument(), new DocumentCreateOptions().streamTransactionId(tid));
            LOGGER.info(tid + " inserted document");

            db.commitStreamTransaction(tid);
            LOGGER.info(tid + " committed");
        }
    }

}

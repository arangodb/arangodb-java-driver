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

package perf;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.Protocol;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michele Rastelli
 */
@Disabled
class SimpleAsyncPerfTest {
    private static final int TARGET = 500_000;
    private static final int MAX_PENDING_REQUESTS = 500;

    private void doGetVersion(ArangoDBAsync arangoDB) throws InterruptedException {
        AtomicInteger pendingReqsCount = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();

        while (completed.get() < TARGET) {
            pendingReqsCount.incrementAndGet();
            arangoDB.getVersion()
                    .thenAccept(it -> {
                        pendingReqsCount.decrementAndGet();
                        completed.incrementAndGet();
                    });
            while (pendingReqsCount.get() > MAX_PENDING_REQUESTS) {
                Thread.sleep(5);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersion(Protocol protocol) throws InterruptedException {
        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(protocol)
                .build()
                .async();
        // warmup
        doGetVersion(arangoDB);

        long start = new Date().getTime();
        doGetVersion(arangoDB);
        long end = new Date().getTime();
        long elapsedMs = end - start;
        System.out.println("elapsed ms: " + elapsedMs);
        long reqPerSec = TARGET * 1_000 / elapsedMs;
        System.out.println("req/s: " + reqPerSec);
        System.out.println("---");
    }
}

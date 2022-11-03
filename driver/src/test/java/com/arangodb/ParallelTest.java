package com.arangodb;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ParallelTest {

    @ParameterizedTest(name = "{index}")
    @EnumSource(Protocol.class)
    void connectionParallelism(Protocol protocol) throws InterruptedException {
        // test that connections are internally async and can have multiple pending requests
        // BTS-1102: the server does not run pipelined HTTP/1.1 requests in parallel
        assumeTrue(protocol != Protocol.HTTP_JSON && protocol != Protocol.HTTP_VPACK);
        ArangoDB adb = new ArangoDB.Builder()
                .useProtocol(protocol)
                .maxConnections(1)
                .build();

        ExecutorService es = Executors.newFixedThreadPool(3);
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tasks.add(es.submit(() -> adb.db().query("return sleep(1)", Void.class)));
        }

        Thread.sleep(2_000);
        assertThat(tasks).allMatch(Future::isDone);
        adb.shutdown();
        es.shutdown();
    }

}

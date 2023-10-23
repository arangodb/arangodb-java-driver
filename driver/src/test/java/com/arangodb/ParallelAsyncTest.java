package com.arangodb;

import com.arangodb.config.ConfigUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ParallelAsyncTest {

    @ParameterizedTest(name = "{index}")
    @EnumSource(Protocol.class)
    void connectionParallelism(Protocol protocol) throws InterruptedException {
        // test that connections are internally async and can have multiple pending requests
        // BTS-1102: the server does not run pipelined HTTP/1.1 requests in parallel
        assumeTrue(protocol != Protocol.HTTP_JSON && protocol != Protocol.HTTP_VPACK);
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .protocol(protocol)
                .maxConnections(1)
                .build()
                .async();

        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tasks.add(adb.db().query("return sleep(1)", Void.class));
        }

        Thread.sleep(2_000);
        assertThat(tasks).allMatch(Future::isDone);
        adb.shutdown();
    }

}

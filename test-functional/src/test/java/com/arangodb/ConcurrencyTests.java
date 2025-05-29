package com.arangodb;

import com.arangodb.config.ConfigUtils;
import com.arangodb.util.ProtocolSource;
import com.arangodb.util.SlowTest;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ConcurrencyTests {

    @SlowTest
    @ParameterizedTest
    @ProtocolSource
    void concurrentPendingRequests(Protocol protocol) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(10);
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .protocol(protocol).build();
        List<CompletableFuture<Void>> futures = IntStream.range(0, 10)
                .mapToObj(__ -> CompletableFuture.runAsync(() -> adb.db().query("RETURN SLEEP(1)", Void.class), es))
                .collect(Collectors.toList());
        for (CompletableFuture<Void> f : futures) {
            f.get();
        }
        adb.shutdown();
        es.shutdown();
    }

}

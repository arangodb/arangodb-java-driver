package com.arangodb;

import com.arangodb.config.ConfigUtils;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.util.ProtocolSource;
import com.arangodb.util.SlowTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.fail;

class ConcurrencyAsyncTests {

    @SlowTest
    @ParameterizedTest
    @ProtocolSource
    @Timeout(2)
    void executorLimit(Protocol protocol) {
        ExecutorService asyncExecutor = Executors.newCachedThreadPool();
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .maxConnections(1)
                .protocol(protocol)
                .build().async();

        List<CompletableFuture<ArangoDBVersion>> futures = IntStream.range(0, 20)
                .mapToObj(i -> adb.getVersion()
                        .whenCompleteAsync((dbVersion, ex) -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }, asyncExecutor))
                .collect(Collectors.toList());

        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                fail();
            }
        });
        adb.shutdown();
        asyncExecutor.shutdown();
    }


    @Disabled
    @ParameterizedTest
    @ProtocolSource
    @Timeout(2)
    void outgoingRequestsParallelismTest(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .maxConnections(20)
                .protocol(protocol).build().async();

        List<CompletableFuture<?>> reqs = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            reqs.add(adb.getVersion());
        }
        for (CompletableFuture<?> req : reqs) {
            req.get();
        }
        adb.shutdown();
    }

    @SlowTest
    @ParameterizedTest
    @ProtocolSource
    void concurrentPendingRequests(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ConfigUtils.loadConfig())
                .protocol(protocol).build().async();
        ExecutorService es = Executors.newFixedThreadPool(10);
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

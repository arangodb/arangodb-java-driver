package com.arangodb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class ConcurrencyTests {

    final Protocol protocol;

    public ConcurrencyTests(Protocol protocol) {
        this.protocol = protocol;
    }

    @Parameterized.Parameters
    public static Protocol[] protocols() {
        return Protocol.values();
    }

    @Test
    public void concurrentPendingRequests() throws ExecutionException, InterruptedException {
        ArangoDB adb = new ArangoDB.Builder().useProtocol(protocol).build();
        List<CompletableFuture<Void>> futures = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.runAsync(
                        () -> adb.db().query("RETURN SLEEP(1)", Void.class),
                        Executors.newFixedThreadPool(10))
                )
                .collect(Collectors.toList());
        for (CompletableFuture<Void> f : futures) {
            f.get();
        }
        adb.shutdown();
    }

}

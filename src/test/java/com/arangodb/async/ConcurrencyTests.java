package com.arangodb.async;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConcurrencyTests {

    @Test
    public void concurrentPendingRequests() throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDBAsync.Builder().build();
        List<CompletableFuture<ArangoCursorAsync<Void>>> reqs = IntStream.range(0, 10)
                .mapToObj(__ -> adb.db().query("RETURN SLEEP(1)", Void.class))
                .collect(Collectors.toList());
        for (CompletableFuture<ArangoCursorAsync<Void>> req : reqs) {
            req.get();
        }
    }

}

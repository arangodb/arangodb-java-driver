package com.arangodb.async;



import com.arangodb.mapping.ArangoJack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ConcurrencyTests {

    @Test
    void concurrentPendingRequests() throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDBAsync.Builder().serializer(new ArangoJack()).build();
        List<CompletableFuture<ArangoCursorAsync<Void>>> reqs = IntStream.range(0, 10)
                .mapToObj(__ -> adb.db().query("RETURN SLEEP(1)", Void.class))
                .collect(Collectors.toList());
        for (CompletableFuture<ArangoCursorAsync<Void>> req : reqs) {
            req.get();
        }
        adb.shutdown();
    }

}

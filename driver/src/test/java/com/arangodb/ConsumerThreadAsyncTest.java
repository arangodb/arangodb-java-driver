package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.ExecutionException;

public class ConsumerThreadAsyncTest extends BaseJunit5 {

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void nestedRequests(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .maxConnections(1)
                .build()
                .async();

        adb.getVersion()
                .thenCompose(it -> adb.getVersion())
                .thenCompose(it -> adb.getVersion())
                .thenCompose(it -> adb.getVersion())
                .get();

        adb.shutdown();
    }

}

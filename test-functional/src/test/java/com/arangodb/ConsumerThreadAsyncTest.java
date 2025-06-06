package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.util.ProtocolSource;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.concurrent.ExecutionException;

public class ConsumerThreadAsyncTest extends BaseJunit5 {

    @ParameterizedTest
    @ProtocolSource
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

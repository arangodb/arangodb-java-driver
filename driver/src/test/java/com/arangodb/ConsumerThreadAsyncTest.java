package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerThreadAsyncTest extends BaseJunit5 {

    private volatile Thread thread;

    private void setThread() {
        thread = Thread.currentThread();
    }

    private void sleep() {
        try {
            Thread.sleep(3_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Disabled
    void defaultConsumerThread(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .build()
                .async();

        adb.getVersion()
                .thenAccept(it -> setThread())
                .get();

        adb.shutdown();

        if (Protocol.VST.equals(protocol)) {
            assertThat(thread.getName()).startsWith("adb-vst-");
        } else {
            assertThat(thread.getName()).startsWith("adb-http-");
        }
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void customConsumerExecutor(Protocol protocol) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setName("custom-" + UUID.randomUUID());
            return t;
        });
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .asyncExecutor(es)
                .build()
                .async();

        adb.getVersion()
                .thenAccept(it -> setThread())
                .get();

        adb.shutdown();
        es.shutdown();
        assertThat(thread.getName()).startsWith("custom-");
    }

    /**
     * Generates warns from Vert.x BlockedThreadChecker
     */
    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Disabled
    void sleepOnDefaultConsumerThread(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .maxConnections(1)
                .build()
                .async();

        adb.getVersion()
                .thenAccept(it -> sleep())
                .get();

        adb.shutdown();
    }

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

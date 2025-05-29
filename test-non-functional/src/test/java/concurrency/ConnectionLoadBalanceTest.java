package concurrency;

import com.arangodb.*;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.internal.net.ConnectionPoolImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.TestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

public class ConnectionLoadBalanceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLoadBalanceTest.class);

    public static Stream<Arguments> configs() {
        return Stream.of(
                new Config(Protocol.HTTP_JSON, 10),
                new Config(Protocol.HTTP_JSON, 20),
                new Config(Protocol.HTTP2_JSON, 1),
                new Config(Protocol.HTTP2_JSON, 2)
        ).map(Arguments::of);
    }

    // Test the requests load balancing across different connections, when all the slots except 1 are busy
    @MethodSource("configs")
    @ParameterizedTest
    void loadBalanceToAvailableSlots(Config cfg) throws InterruptedException {
        doTestLoadBalance(cfg, 1);
    }

    // Test the requests load balancing across different connections, when all the slots are busy
    @MethodSource("configs")
    @ParameterizedTest
    void loadBalanceAllBusy(Config cfg) throws InterruptedException {
        doTestLoadBalance(cfg, 2);
    }

    void doTestLoadBalance(Config cfg, int sleepCycles) throws InterruptedException {
        int longTasksCount = cfg.maxStreams() * cfg.maxConnections * sleepCycles - 1;
        int shortTasksCount = 10;
        long sleepDuration = 2;

        ArangoDatabaseAsync db = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(cfg.protocol)
                .serde(TestUtils.createSerde(cfg.protocol))
                .maxConnections(cfg.maxConnections)
                .build().async().db();

        LOGGER.debug("starting...");

        CompletableFuture<Void> longRunningTasks = CompletableFuture.allOf(
                IntStream.range(0, longTasksCount)
                        .mapToObj(__ ->
                                db.query("RETURN SLEEP(@duration)", Void.class, Map.of("duration", sleepDuration)))
                        .toArray(CompletableFuture[]::new)
        );

        Thread.sleep(100);

        CompletableFuture<Void> shortRunningTasks = CompletableFuture.allOf(
                IntStream.range(0, shortTasksCount)
                        .mapToObj(__ -> db.getVersion())
                        .toArray(CompletableFuture[]::new)
        );

        LOGGER.debug("awaiting...");

        await()
                .timeout(Duration.ofSeconds(sleepDuration * sleepCycles - 1L))
                .until(shortRunningTasks::isDone);

        LOGGER.debug("completed shortRunningTasks");

        // join exceptional completions
        shortRunningTasks.join();

        await()
                .timeout(Duration.ofSeconds(sleepDuration * sleepCycles + 2L))
                .until(longRunningTasks::isDone);

        LOGGER.debug("completed longRunningTasks");

        // join exceptional completions
        longRunningTasks.join();

        db.arango().shutdown();
    }

    private record Config(
            Protocol protocol,
            int maxConnections
    ) {
        int maxStreams() {
            return switch (protocol) {
                case HTTP_JSON, HTTP_VPACK -> ConnectionPoolImpl.HTTP1_SLOTS;
                default -> ConnectionPoolImpl.HTTP2_SLOTS;
            };
        }
    }
}

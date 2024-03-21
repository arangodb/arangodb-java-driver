package resilience.ttl;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.Protocol;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.SingleServerTest;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

/**
 * @author Michele Rastelli
 */
class TtlTest extends SingleServerTest {

    static Stream<Arguments> args() {
        return Stream.of(
                Arguments.of(Protocol.HTTP_JSON, "UNREGISTERED"),
                Arguments.of(Protocol.HTTP2_JSON, "OUTBOUND GO_AWAY")
        );
    }

    @ParameterizedTest
    @MethodSource("args")
    void connectionTtl(Protocol p, String expectedLog) {
        ArangoDB arangoDB = dbBuilder()
                .connectionTtl(1_000L)
                .maxConnections(1)
                .protocol(p)
                .build();

        arangoDB.getVersion();

        await()
                .timeout(Duration.ofSeconds(3))
                .until(() -> logs.getLogs().anyMatch(it -> it.getFormattedMessage().contains(expectedLog)));

        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @MethodSource("args")
    void connectionTtlAsync(Protocol p, String expectedLog) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = dbBuilder()
                .connectionTtl(1_000L)
                .maxConnections(1)
                .protocol(p)
                .build()
                .async();

        arangoDB.getVersion().get();

        await()
                .timeout(Duration.ofSeconds(3))
                .until(() -> logs.getLogs().anyMatch(it -> it.getFormattedMessage().contains(expectedLog)));

        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

}

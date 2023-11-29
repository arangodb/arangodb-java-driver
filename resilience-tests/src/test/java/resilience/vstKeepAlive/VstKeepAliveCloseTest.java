package resilience.vstKeepAlive;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import resilience.SingleServerTest;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;

/**
 * @author Michele Rastelli
 */
class VstKeepAliveCloseTest extends SingleServerTest {

    private ArangoDB arangoDB;

    @BeforeEach
    void init() {
        arangoDB = dbBuilder()
                .protocol(Protocol.VST)
                .timeout(1000)
                .keepAliveInterval(1)
                .build();
    }

    @AfterEach
    void shutDown() {
        arangoDB.shutdown();
    }

    /**
     * after 3 consecutive VST keepAlive failures:
     * - log ERROR Connection unresponsive
     * - reconnect on next request
     */
    @Test
    @Timeout(10)
    void keepAliveCloseAndReconnect() throws IOException {
        arangoDB.getVersion();
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        await().until(() -> logs.getLogs()
                .filter(e -> e.getLevel().equals(Level.ERROR))
                .filter(e -> e.getFormattedMessage() != null)
                .anyMatch(e -> e.getFormattedMessage().contains("Connection unresponsive!")));
        toxic.setLatency(0);
        toxic.remove();
        arangoDB.getVersion();
    }

    /**
     * after 3 consecutive VST keepAlive failures:
     * - log ERROR Connection unresponsive
     * - reconnect on next request
     */
    @Test
    @Timeout(10)
    void keepAliveCloseAndReconnectAsync() throws IOException, ExecutionException, InterruptedException {
        arangoDB.async().getVersion().get();
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        await().until(() -> logs.getLogs()
                .filter(e -> e.getLevel().equals(Level.ERROR))
                .filter(e -> e.getFormattedMessage() != null)
                .anyMatch(e -> e.getFormattedMessage().contains("Connection unresponsive!")));
        toxic.setLatency(0);
        toxic.remove();
        arangoDB.async().getVersion().get();
    }

}

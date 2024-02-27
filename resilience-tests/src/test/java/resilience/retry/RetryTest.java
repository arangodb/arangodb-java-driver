package resilience.retry;

import ch.qos.logback.classic.Level;
import com.arangodb.*;
import io.vertx.core.http.HttpClosedException;
import resilience.SingleServerTest;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class RetryTest extends SingleServerTest {

    /**
     * on reconnection failure: - 3x logs WARN Could not connect to host[addr=127.0.0.1,port=8529] -
     * ArangoDBException("Cannot contact any host")
     * <p>
     * once the proxy is re-enabled: - the subsequent requests should be successful
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("adbProvider")
    void unreachableHost(ArangoDB arangoDB) {
        arangoDB.getVersion();
        getEndpoint().disableNow();

        for (int i = 0; i < 10; i++) {
            Throwable thrown = catchThrowable(arangoDB::getVersion);
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getMessage()).contains("Cannot contact any host");
            assertThat(thrown.getCause()).isNotNull();
            assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
            ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                    assertThat(e).isInstanceOf(ConnectException.class));
        }

        long warnsCount = logs.getLogs()
                .filter(e -> e.getLevel().equals(Level.WARN))
                .filter(e -> e.getFormattedMessage().contains("Could not connect to host[addr=127.0.0.1,port=18529]"))
                .count();
        assertThat(warnsCount).isGreaterThanOrEqualTo(3);

        getEndpoint().enable();
        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    /**
     * on reconnection failure: - 3x logs WARN Could not connect to host[addr=127.0.0.1,port=8529] -
     * ArangoDBException("Cannot contact any host")
     * <p>
     * once the proxy is re-enabled: - the subsequent requests should be successful
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncAdbProvider")
    void unreachableHostAsync(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        arangoDB.getVersion().get();
        getEndpoint().disableNow();

        for (int i = 0; i < 10; i++) {
            Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getMessage()).contains("Cannot contact any host");
            assertThat(thrown.getCause()).isNotNull();
            assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
            ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                    assertThat(e).isInstanceOf(ConnectException.class));
        }

        long warnsCount = logs.getLogs()
                .filter(e -> e.getLevel().equals(Level.WARN))
                .filter(e -> e.getFormattedMessage().contains("Could not connect to host[addr=127.0.0.1,port=18529]"))
                .count();
        assertThat(warnsCount).isGreaterThanOrEqualTo(3);

        getEndpoint().enable();
        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }

    /**
     * on delayed response:
     * - ArangoDBException with cause TimeoutException
     * <p>
     * once the delay is removed:
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void connectionTimeout(Protocol protocol) throws IOException, InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .timeout(500)
                .protocol(protocol)
                .build();

        arangoDB.getVersion();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(Throwable::getCause)
                .isInstanceOf(TimeoutException.class);

        toxic.remove();
        Thread.sleep(100);

        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    /**
     * on delayed response:
     * - ArangoDBException with cause TimeoutException
     * <p>
     * once the delay is removed:
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void connectionTimeoutAsync(Protocol protocol) throws IOException, InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .timeout(500)
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(Throwable::getCause)
                .isInstanceOf(TimeoutException.class);

        toxic.remove();
        Thread.sleep(100);

        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }


    /**
     * on closed pending requests of safe HTTP methods:
     * <p>
     * - retry 3 times
     * - ArangoDBMultipleException with 3 exceptions
     * <p>
     * once restored:
     * <p>
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void retryGetOnClosedConnection(Protocol protocol) throws IOException, InterruptedException {
        assumeTrue(protocol != Protocol.VST);
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.getVersion();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoint().disable(300);
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) thrown.getCause()).getExceptions();
        assertThat(exceptions).hasSize(3);
        assertThat(exceptions.get(0)).isInstanceOf(IOException.class);
        assertThat(exceptions.get(0).getCause()).isInstanceOf(HttpClosedException.class);
        assertThat(exceptions.get(1)).isInstanceOf(ConnectException.class);
        assertThat(exceptions.get(2)).isInstanceOf(ConnectException.class);

        toxic.remove();
        getEndpoint().enable();

        arangoDB.getVersion();
        arangoDB.shutdown();
    }

    /**
     * on closed pending requests of safe HTTP methods:
     * <p>
     * - retry 3 times
     * - ArangoDBMultipleException with 3 exceptions
     * <p>
     * once restored:
     * <p>
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void retryGetOnClosedConnectionAsync(Protocol protocol) throws IOException, InterruptedException, ExecutionException {
        assumeTrue(protocol != Protocol.VST);
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoint().disable(300);
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) thrown.getCause()).getExceptions();
        assertThat(exceptions).hasSize(3);
        assertThat(exceptions.get(0)).isInstanceOf(IOException.class);
        assertThat(exceptions.get(0).getCause()).isInstanceOf(HttpClosedException.class);
        assertThat(exceptions.get(1)).isInstanceOf(ConnectException.class);
        assertThat(exceptions.get(2)).isInstanceOf(ConnectException.class);

        toxic.remove();
        getEndpoint().enable();

        arangoDB.getVersion().get();
        arangoDB.shutdown();
    }


    /**
     * on closed pending requests of unsafe HTTP methods: - no retry should happen
     * <p>
     * once restored: - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void notRetryPostOnClosedConnection(Protocol protocol) throws IOException, InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.db().query("return null", Void.class);

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoint().disable(300);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return null", Void.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        if (protocol != Protocol.VST) {
            assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        }

        toxic.remove();
        getEndpoint().enable();

        arangoDB.db().query("return null", Void.class);
        arangoDB.shutdown();
    }

    /**
     * on closed pending requests of unsafe HTTP methods: - no retry should happen
     * <p>
     * once restored: - the subsequent requests should be successful
     */
    @ParameterizedTest
    @MethodSource("protocolProvider")
    void notRetryPostOnClosedConnectionAsync(Protocol protocol) throws IOException, InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        arangoDB.db().query("return null", Void.class).get();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoint().disable(300);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return null", Void.class).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        if (protocol != Protocol.VST) {
            assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        }

        toxic.remove();
        getEndpoint().enable();

        arangoDB.db().query("return null", Void.class).get();
        arangoDB.shutdown();
    }

}

package resilience.retry;

import ch.qos.logback.classic.Level;
import com.arangodb.*;
import io.vertx.core.http.HttpClosedException;
import org.junit.jupiter.params.provider.EnumSource;
import resilience.SingleServerTest;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class RetryTest extends SingleServerTest {

    static Stream<ArangoDB> arangoProvider() {
        return Stream.of(
                dbBuilder().protocol(Protocol.VST).build(),
                dbBuilder().protocol(Protocol.HTTP_VPACK).build(),
                dbBuilder().protocol(Protocol.HTTP2_VPACK).build()
        );
    }

    static Stream<ArangoDBAsync> asyncArangoProvider() {
        return arangoProvider().map(ArangoDB::async);
    }

    /**
     * on reconnection failure: - 3x logs WARN Could not connect to host[addr=127.0.0.1,port=8529] -
     * ArangoDBException("Cannot contact any host")
     * <p>
     * once the proxy is re-enabled: - the subsequent requests should be successful
     */
    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void unreachableHost(ArangoDB arangoDB) {
        arangoDB.getVersion();
        disableEndpoint();

        for (int i = 0; i < 10; i++) {
            Throwable thrown = catchThrowable(arangoDB::getVersion);
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getMessage()).contains("Cannot contact any host");
            assertThat(thrown.getCause()).isNotNull();
            assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
            ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                    assertThat(e).isInstanceOf(ConnectException.class));
        }

        long warnsCount = logs.getLoggedEvents().stream()
                .filter(e -> e.getLevel().equals(Level.WARN))
                .filter(e -> e.getMessage().contains("Could not connect to host[addr=127.0.0.1,port=18529]"))
                .count();
        assertThat(warnsCount).isGreaterThanOrEqualTo(3);

        enableEndpoint();
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
    @MethodSource("asyncArangoProvider")
    void unreachableHostAsync(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        arangoDB.getVersion().get();
        disableEndpoint();

        for (int i = 0; i < 10; i++) {
            Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getMessage()).contains("Cannot contact any host");
            assertThat(thrown.getCause()).isNotNull();
            assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
            ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                    assertThat(e).isInstanceOf(ConnectException.class));
        }

        long warnsCount = logs.getLoggedEvents().stream()
                .filter(e -> e.getLevel().equals(Level.WARN))
                .filter(e -> e.getMessage().contains("Could not connect to host[addr=127.0.0.1,port=18529]"))
                .count();
        assertThat(warnsCount).isGreaterThanOrEqualTo(3);

        enableEndpoint();
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
    @EnumSource(Protocol.class)
    void connectionTimeout(Protocol protocol) throws IOException, InterruptedException {
        // https://github.com/vert-x3/vertx-web/issues/2296
        // WebClient: HTTP/2 request timeout does not throw TimeoutException
        assumeTrue(protocol != Protocol.HTTP2_VPACK);
        assumeTrue(protocol != Protocol.HTTP2_JSON);

        ArangoDB arangoDB = dbBuilder()
                .timeout(1_000)
                .protocol(protocol)
                .build();

        arangoDB.getVersion();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        thrown.printStackTrace();
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
    @EnumSource(Protocol.class)
    void connectionTimeoutAsync(Protocol protocol) throws IOException, InterruptedException, ExecutionException {
        // https://github.com/vert-x3/vertx-web/issues/2296
        // WebClient: HTTP/2 request timeout does not throw TimeoutException
        assumeTrue(protocol != Protocol.HTTP2_VPACK);
        assumeTrue(protocol != Protocol.HTTP2_JSON);

        ArangoDBAsync arangoDB = dbBuilder()
                .timeout(1_000)
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        thrown.printStackTrace();
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
    @EnumSource(Protocol.class)
    void retryGetOnClosedConnection(Protocol protocol) throws IOException, InterruptedException {
        assumeTrue(protocol != Protocol.VST);
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.getVersion();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(this::disableEndpoint, 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        thrown.printStackTrace();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) thrown.getCause()).getExceptions();
        assertThat(exceptions).hasSize(3);
        assertThat(exceptions.get(0)).isInstanceOf(IOException.class);
        assertThat(exceptions.get(0).getCause()).isInstanceOf(HttpClosedException.class);
        assertThat(exceptions.get(1)).isInstanceOf(ConnectException.class);
        assertThat(exceptions.get(2)).isInstanceOf(ConnectException.class);

        toxic.remove();
        Thread.sleep(100);
        enableEndpoint();

        arangoDB.getVersion();
        arangoDB.shutdown();
        es.shutdown();
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
    @EnumSource(Protocol.class)
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

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(this::disableEndpoint, 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        thrown.printStackTrace();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) thrown.getCause()).getExceptions();
        assertThat(exceptions).hasSize(3);
        assertThat(exceptions.get(0)).isInstanceOf(IOException.class);
        assertThat(exceptions.get(0).getCause()).isInstanceOf(HttpClosedException.class);
        assertThat(exceptions.get(1)).isInstanceOf(ConnectException.class);
        assertThat(exceptions.get(2)).isInstanceOf(ConnectException.class);

        toxic.remove();
        Thread.sleep(100);
        enableEndpoint();

        arangoDB.getVersion().get();
        arangoDB.shutdown();
        es.shutdown();
    }


    /**
     * on closed pending requests of unsafe HTTP methods: - no retry should happen
     * <p>
     * once restored: - the subsequent requests should be successful
     */
    @ParameterizedTest
    @EnumSource(Protocol.class)
    void notRetryPostOnClosedConnection(Protocol protocol) throws IOException, InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.db().query("return null", Void.class);

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(this::disableEndpoint, 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return null", Void.class));
        thrown.printStackTrace();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        if (protocol != Protocol.VST) {
            assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        }

        toxic.remove();
        Thread.sleep(100);
        enableEndpoint();

        arangoDB.db().query("return null", Void.class);
        arangoDB.shutdown();
        es.shutdown();
    }

    /**
     * on closed pending requests of unsafe HTTP methods: - no retry should happen
     * <p>
     * once restored: - the subsequent requests should be successful
     */
    @ParameterizedTest
    @EnumSource(Protocol.class)
    void notRetryPostOnClosedConnectionAsync(Protocol protocol) throws IOException, InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        arangoDB.db().query("return null", Void.class).get();

        // slow down the driver connection
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(this::disableEndpoint, 300, TimeUnit.MILLISECONDS);

        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return null", Void.class).get()).getCause();
        thrown.printStackTrace();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        if (protocol != Protocol.VST) {
            assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        }

        toxic.remove();
        Thread.sleep(100);
        enableEndpoint();

        arangoDB.db().query("return null", Void.class).get();
        arangoDB.shutdown();
        es.shutdown();
    }

}

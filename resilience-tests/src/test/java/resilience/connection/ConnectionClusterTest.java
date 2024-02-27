package resilience.connection;

import ch.qos.logback.classic.Level;
import com.arangodb.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.ClusterTest;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class ConnectionClusterTest extends ClusterTest {

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFail(Protocol protocol) {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host("wrongHost", 8529)
                .protocol(protocol)
                .build();

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host!");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e -> {
            assertThat(e).isInstanceOf(UnknownHostException.class);
            assertThat(e.getMessage()).contains("wrongHost");
        });
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFailAsync(Protocol protocol) {
        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .host("wrongHost", 8529)
                .protocol(protocol)
                .build()
                .async();

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host!");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e -> {
            assertThat(e).isInstanceOf(UnknownHostException.class);
            assertThat(e.getMessage()).contains("wrongHost");
        });
        arangoDB.shutdown();
    }

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFailover(Protocol protocol) {
        ArangoDB arangoDB = new ArangoDB.Builder()
                .password("test")
                .host("wrongHost", 8529)
                .host("127.0.0.1", 8529)
                .protocol(protocol)
                .build();

        arangoDB.getVersion();

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
    }

    @ParameterizedTest
    @MethodSource("protocolProvider")
    void nameResolutionFailoverAsync(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .password("test")
                .host("wrongHost", 8529)
                .host("127.0.0.1", 8529)
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("adbProvider")
    void connectionFail(ArangoDB arangoDB) {
        disableAllEndpoints();

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                assertThat(e).isInstanceOf(ConnectException.class));

        arangoDB.shutdown();
        enableAllEndpoints();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncAdbProvider")
    void connectionFailAsync(ArangoDBAsync arangoDB) {
        disableAllEndpoints();

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("Cannot contact any host");
        assertThat(thrown.getCause()).isNotNull();
        assertThat(thrown.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        ((ArangoDBMultipleException) thrown.getCause()).getExceptions().forEach(e ->
                assertThat(e).isInstanceOf(ConnectException.class));
        arangoDB.shutdown();
        enableAllEndpoints();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("adbProvider")
    void connectionFailover(ArangoDB arangoDB) {
        getEndpoints().get(0).disableNow();
        getEndpoints().get(1).disableNow();

        arangoDB.getVersion();

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
        enableAllEndpoints();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncAdbProvider")
    void connectionFailoverAsync(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        getEndpoints().get(0).disableNow();
        getEndpoints().get(1).disableNow();

        arangoDB.getVersion().get();

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
        enableAllEndpoints();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("adbProvider")
    void connectionFailoverPost(ArangoDB arangoDB) {
        getEndpoints().get(0).disableNow();
        getEndpoints().get(1).disableNow();

        arangoDB.db().query("RETURN 1", Integer.class);

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
        enableAllEndpoints();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncAdbProvider")
    void connectionFailoverPostAsync(ArangoDBAsync arangoDB) throws ExecutionException, InterruptedException {
        getEndpoints().get(0).disableNow();
        getEndpoints().get(1).disableNow();

        arangoDB.db().query("RETURN 1", Integer.class).get();

        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));

        arangoDB.shutdown();
        enableAllEndpoints();
    }

}

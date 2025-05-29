package resilience.shutdown;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import io.vertx.core.http.HttpClosedException;
import org.junit.jupiter.params.ParameterizedTest;
import resilience.SingleServerTest;
import resilience.utils.ProtocolSource;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class ShutdownTest extends SingleServerTest {

    @ParameterizedTest
    @ProtocolSource
    void shutdown(Protocol protocol) throws InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.getVersion();
        arangoDB.shutdown();
        Thread.sleep(500);
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("closed");
    }

    @ParameterizedTest
    @ProtocolSource
    void shutdownAsync(Protocol protocol) throws InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();
        arangoDB.shutdown();
        Thread.sleep(500);
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("closed");
    }

    @ParameterizedTest
    @ProtocolSource
    void shutdownWithPendingRequests(Protocol protocol) {
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(arangoDB::shutdown, 500, TimeUnit.MILLISECONDS);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", Void.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        es.shutdown();
    }

    @ParameterizedTest
    @ProtocolSource
    void shutdownWithPendingRequestsAsync(Protocol protocol) {
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(arangoDB::shutdown, 500, TimeUnit.MILLISECONDS);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", Void.class).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        es.shutdown();
    }

}

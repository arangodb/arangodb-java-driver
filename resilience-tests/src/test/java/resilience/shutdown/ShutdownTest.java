package resilience.shutdown;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import io.vertx.core.http.HttpClosedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import resilience.SingleServerTest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Michele Rastelli
 */
class ShutdownTest extends SingleServerTest {

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void shutdown(Protocol protocol) throws InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        arangoDB.getVersion();
        arangoDB.shutdown();
        Thread.sleep(1_000);
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("closed");
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void shutdownAsync(Protocol protocol) throws InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();
        arangoDB.shutdown();
        Thread.sleep(1_000);
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getMessage()).contains("closed");
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void shutdownWithPendingRequests(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);
        ArangoDB arangoDB = dbBuilder()
                .protocol(protocol)
                .build();

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(arangoDB::shutdown, 200, TimeUnit.MILLISECONDS);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", Void.class));
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        es.shutdown();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void shutdownWithPendingRequestsAsync(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);
        ArangoDBAsync arangoDB = dbBuilder()
                .protocol(protocol)
                .build()
                .async();

        ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
        es.schedule(arangoDB::shutdown, 200, TimeUnit.MILLISECONDS);
        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", Void.class).get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);
        assertThat(thrown.getCause().getCause()).isInstanceOf(HttpClosedException.class);
        es.shutdown();
    }

}

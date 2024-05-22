package resilience.vertx;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arangodb.ArangoDB;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import resilience.SingleServerTest;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class VertxTest extends SingleServerTest {

    @Test
    void managedVertx() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .build();

        adb.getVersion();
        adb.shutdown();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnectionFactory"))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Creating new Vert.x instance"))
                .anySatisfy(it -> assertThat(it).contains("Closing Vert.x instance"));
    }

    @Test
    void reuseVertx() throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(() -> {
            ArangoDB adb = new ArangoDB.Builder()
                    .host("172.28.0.1", 8529)
                    .password("test")
                    .reuseVertx(true)
                    .build();
            adb.getVersion();
            adb.shutdown();
            return null;
        }).toCompletionStage().toCompletableFuture().get();
        vertx.close();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnectionFactory"))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Reusing existing Vert.x instance"));
    }

    @Test
    void notReuseVertx() throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(() -> {
            ArangoDB adb = new ArangoDB.Builder()
                    .host("172.28.0.1", 8529)
                    .password("test")
                    .reuseVertx(false)
                    .build();
            adb.getVersion();
            adb.shutdown();
            return null;
        }).toCompletionStage().toCompletableFuture().get();
        vertx.close();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnectionFactory"))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Found an existing Vert.x instance, set reuseVertx=true to reuse it"))
                .anySatisfy(it -> assertThat(it).contains("Creating new Vert.x instance"))
                .anySatisfy(it -> assertThat(it).contains("Closing Vert.x instance"));
    }

}

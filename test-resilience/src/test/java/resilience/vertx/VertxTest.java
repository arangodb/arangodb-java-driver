package resilience.vertx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.arangodb.ArangoDB;
import com.arangodb.PackageVersion;
import com.arangodb.http.HttpConnection;
import com.arangodb.http.HttpProtocolConfig;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import resilience.SingleServerTest;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class VertxTest extends SingleServerTest {

    public VertxTest() {
        super(Collections.singletonMap(HttpConnection.class, Level.DEBUG));
    }

    @Test
    void managedVertx() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .build();

        adb.getVersion();
        adb.shutdown();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnection"))
                .filteredOn(it -> it.getLevel().equals(Level.DEBUG))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Creating new Vert.x instance"))
                .anySatisfy(it -> assertThat(it).contains("Closing Vert.x instance"));
    }

    @Test
    void reuseVertx() {
        Vertx vertx = Vertx.vertx();
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocolConfig(HttpProtocolConfig.builder().vertx(vertx).build())
                .build();
        adb.getVersion();
        adb.shutdown();
        vertx.close();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnection"))
                .filteredOn(it -> it.getLevel().equals(Level.DEBUG))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Reusing existing Vert.x instance"));
    }

    @Test
    void reuseVertxFromVertxThread() throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(() -> {
            ArangoDB adb = new ArangoDB.Builder()
                    .host("172.28.0.1", 8529)
                    .password("test")
                    .protocolConfig(HttpProtocolConfig.builder().vertx(Vertx.currentContext().owner()).build())
                    .build();
            adb.getVersion();
            adb.shutdown();
            return null;
        }).toCompletionStage().toCompletableFuture().get();
        vertx.close();

        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnection"))
                .filteredOn(it -> it.getLevel().equals(Level.DEBUG))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Reusing existing Vert.x instance"));
    }

    @Test
    void existingVertxNotUsed() throws ExecutionException, InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.executeBlocking(() -> {
            ArangoDB adb = new ArangoDB.Builder()
                    .host("172.28.0.1", 8529)
                    .password("test")
                    .build();
            adb.getVersion();
            adb.shutdown();
            return null;
        }).toCompletionStage().toCompletableFuture().get();
        vertx.close();

        if (!PackageVersion.SHADED) {
            assertThat(logs.getLogs())
                    .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnectionFactory"))
                    .filteredOn(it -> it.getLevel().equals(Level.WARN))
                    .map(ILoggingEvent::getFormattedMessage)
                    .anySatisfy(it -> assertThat(it)
                            .contains("Found an existing Vert.x instance, you can reuse it by setting:")
                            .contains(".protocolConfig(HttpProtocolConfig.builder().vertx(Vertx.currentContext().owner()).build())")
                    );
        }
        assertThat(logs.getLogs())
                .filteredOn(it -> it.getLoggerName().equals("com.arangodb.http.HttpConnection"))
                .filteredOn(it -> it.getLevel().equals(Level.DEBUG))
                .map(ILoggingEvent::getFormattedMessage)
                .anySatisfy(it -> assertThat(it).contains("Creating new Vert.x instance"))
                .anySatisfy(it -> assertThat(it).contains("Closing Vert.x instance"));
    }

}

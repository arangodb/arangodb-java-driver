import com.arangodb.*;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.util.SlowTest;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CommunicationTest {

    @SlowTest
    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Timeout(5)
    void disconnectAsync(Protocol protocol) throws InterruptedException, ExecutionException {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .build()
                .async();
        arangoDB.getVersion().get();

        CompletableFuture<ArangoCursorAsync<Object>> result = arangoDB.db().query("return sleep(1)", null);
        Thread.sleep(500);
        arangoDB.shutdown();
        Throwable thrown = catchThrowable(result::get).getCause();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

    @SlowTest
    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Timeout(5)
    void disconnect(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

        ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .build();
        arangoDB.getVersion();

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            arangoDB.shutdown();
        }).start();

        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", null));
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

}

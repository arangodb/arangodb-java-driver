import com.arangodb.*;
import com.arangodb.config.ArangoConfigProperties;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CommunicationTest {

    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Timeout(2)
    void disconnectAsync(Protocol protocol) throws InterruptedException {
        // FIXME: fix for VST protocol (DE-708)
        assumeTrue(!Protocol.VST.equals(protocol));

        ArangoDBAsync arangoDB = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .build()
                .async();
        CompletableFuture<ArangoCursorAsync<Object>> result = arangoDB.db().query("return sleep(1)", null, null, null);
        Thread.sleep(200);
        arangoDB.shutdown();
        Throwable thrown = catchThrowable(result::get).getCause();
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    @Timeout(2)
    void disconnect(Protocol protocol) {
        // FIXME: fix for VST protocol (DE-708)
        assumeTrue(!Protocol.VST.equals(protocol));

        ArangoDB arangoDB = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .build();

        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            arangoDB.shutdown();
        }).start();

        Throwable thrown = catchThrowable(() -> arangoDB.db().query("return sleep(1)", null, null, null));
        assertThat(thrown)
                .isNotNull()
                .isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("closed");
    }

}

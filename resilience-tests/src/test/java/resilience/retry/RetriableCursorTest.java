package resilience.retry;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.model.AqlQueryOptions;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.SingleServerTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class RetriableCursorTest extends SingleServerTest {

    static Stream<ArangoDB> arangoProvider() {
        return Stream.of(
                dbBuilder().timeout(1_000).protocol(Protocol.VST).build(),
                dbBuilder().timeout(1_000).protocol(Protocol.HTTP_JSON).build(),
                dbBuilder().timeout(1_000).protocol(Protocol.HTTP2_VPACK).build()
        );
    }

    @ParameterizedTest
    @MethodSource("arangoProvider")
    void retryCursor(ArangoDB arangoDB) throws IOException {
        try (ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i",
                        String.class,
                        new AqlQueryOptions().batchSize(1).allowRetry(true))) {

            assertThat(cursor.hasNext()).isTrue();
            assertThat(cursor.next()).isEqualTo("1");
            assertThat(cursor.hasNext()).isTrue();
            Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
            Throwable thrown = catchThrowable(cursor::next);
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getCause()).isInstanceOfAny(TimeoutException.class, IOException.class);
            toxic.remove();
            assertThat(cursor.next()).isEqualTo("2");
            assertThat(cursor.hasNext()).isFalse();
        }
        arangoDB.shutdown();
    }

}

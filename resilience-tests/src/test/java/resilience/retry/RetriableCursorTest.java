package resilience.retry;

import com.arangodb.*;
import com.arangodb.model.AqlQueryOptions;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.SingleServerTest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
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
                dbBuilder().timeout(1_000).protocol(Protocol.HTTP_VPACK).build()
        );
    }

    static Stream<ArangoDBAsync> asyncArangoProvider() {
        return arangoProvider().map(ArangoDB::async);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void retryCursor(ArangoDB arangoDB) throws IOException, InterruptedException {
        try (ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i",
                        String.class,
                        new AqlQueryOptions().batchSize(1).allowRetry(true))) {

            assertThat(cursor.hasNext()).isTrue();
            assertThat(cursor.next()).isEqualTo("1");
            assertThat(cursor.hasNext()).isTrue();
            Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
            Thread.sleep(100);
            Throwable thrown = catchThrowable(cursor::next);
            assertThat(thrown).isInstanceOf(ArangoDBException.class);
            assertThat(thrown.getCause()).isInstanceOfAny(TimeoutException.class);
            toxic.remove();
            assertThat(cursor.next()).isEqualTo("2");
            assertThat(cursor.hasNext()).isFalse();
        }
        arangoDB.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void retryCursorAsync(ArangoDBAsync arangoDB) throws IOException, ExecutionException, InterruptedException {
        ArangoCursorAsync<String> c1 = arangoDB.db()
                .query("for i in 1..2 return i",
                        String.class,
                        new AqlQueryOptions().batchSize(1).allowRetry(true)).get();

        assertThat(c1.getResult()).containsExactly("1");
        assertThat(c1.hasMore()).isTrue();
        Latency toxic = getEndpoint().getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);
        Throwable thrown = catchThrowable(() -> c1.nextBatch().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOfAny(TimeoutException.class);
        toxic.remove();
        Thread.sleep(100);
        ArangoCursorAsync<String> c2 = c1.nextBatch().get();
        assertThat(c2.getResult()).containsExactly("2");
        assertThat(c2.hasMore()).isFalse();
        c2.close();
        arangoDB.shutdown();
    }

}

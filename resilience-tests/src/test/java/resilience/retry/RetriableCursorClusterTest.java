package resilience.retry;

import com.arangodb.*;
import com.arangodb.model.AqlQueryOptions;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import eu.rekawek.toxiproxy.model.toxic.Latency;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import resilience.ClusterTest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class RetriableCursorClusterTest extends ClusterTest {

    static Stream<ArangoDB> arangoProvider() {
        return builderProvider().map(it -> it.timeout(1_000).build());
    }

    static Stream<ArangoDBAsync> asyncArangoProvider() {
        return arangoProvider().map(ArangoDB::async);
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("arangoProvider")
    void retryCursor(ArangoDB arangoDB) throws IOException, InterruptedException {

        ArangoCursor<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i",
                        String.class,
                        new AqlQueryOptions().batchSize(1).allowRetry(true));

        assertThat(cursor.hasNext()).isTrue();
        assertThat(cursor.next()).isEqualTo("1");
        assertThat(cursor.hasNext()).isTrue();

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoints().get(0).disable(300);

        Throwable thrown = catchThrowable(cursor::next);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);

        assertThat(cursor.next()).isEqualTo("2");
        assertThat(cursor.hasNext()).isFalse();

        toxic.remove();
        enableAllEndpoints();
        arangoDB.shutdown();
    }

    @ParameterizedTest(name = "{index}")
    @MethodSource("asyncArangoProvider")
    void retryCursorAsync(ArangoDBAsync arangoDB) throws IOException, InterruptedException, ExecutionException {

        ArangoCursorAsync<String> cursor = arangoDB.db()
                .query("for i in 1..2 return i",
                        String.class,
                        new AqlQueryOptions().batchSize(1).allowRetry(true)).get();

        assertThat(cursor.getResult()).containsExactly("1");
        assertThat(cursor.hasMore()).isTrue();

        // slow down the driver connection
        Latency toxic = getEndpoints().get(0).getProxy().toxics().latency("latency", ToxicDirection.DOWNSTREAM, 10_000);
        Thread.sleep(100);

        getEndpoints().get(0).disable(300);

        Throwable thrown = catchThrowable(() -> cursor.nextBatch().get()).getCause();
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        assertThat(thrown.getCause()).isInstanceOf(IOException.class);

        ArangoCursorAsync<String> c2 = cursor.nextBatch().get();
        assertThat(c2.getResult()).containsExactly("2");
        assertThat(c2.hasMore()).isFalse();

        toxic.remove();
        enableAllEndpoints();
        arangoDB.shutdown();
    }
}

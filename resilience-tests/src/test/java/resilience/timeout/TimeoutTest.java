package resilience.timeout;

import com.arangodb.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import resilience.SingleServerTest;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Michele Rastelli
 */
class TimeoutTest extends SingleServerTest {

    /**
     * on timeout failure:
     * - throw exception
     * - expect operation performed (at most) once
     * <p>
     * after the exception:
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @EnumSource(Protocol.class)
    void requestTimeout(Protocol protocol) throws InterruptedException {
        ArangoDB arangoDB = dbBuilder()
                .timeout(1_000)
                .protocol(protocol)
                .build();

        arangoDB.getVersion();
        String colName = "timeoutTest";
        ArangoCollection col = arangoDB.db().collection(colName);
        if (!col.exists()) col.create();
        col.truncate();

        Throwable thrown = catchThrowable(() -> arangoDB.db()
                .query("INSERT {value:sleep(2)} INTO @@col RETURN NEW",
                        Map.class,
                        Collections.singletonMap("@col", colName))
        );

        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(Throwable::getCause)
                .isInstanceOf(TimeoutException.class);

        arangoDB.getVersion();

        Thread.sleep(2_000);
        assertThat(col.count().getCount()).isEqualTo(1);

        arangoDB.shutdown();
    }

    /**
     * on timeout failure:
     * - throw exception
     * - expect operation performed (at most) once
     * <p>
     * after the exception:
     * - the subsequent requests should be successful
     */
    @ParameterizedTest
    @EnumSource(Protocol.class)
    void requestTimeoutAsync(Protocol protocol) throws InterruptedException, ExecutionException {
        ArangoDBAsync arangoDB = dbBuilder()
                .timeout(1_000)
                .protocol(protocol)
                .build()
                .async();

        arangoDB.getVersion().get();
        String colName = "timeoutTest";
        ArangoCollectionAsync col = arangoDB.db().collection(colName);
        if (!col.exists().get()) col.create().get();
        col.truncate().get();

        Throwable thrown = catchThrowable(() -> arangoDB.db()
                .query("INSERT {value:sleep(2)} INTO @@col RETURN NEW",
                        Map.class,
                        Collections.singletonMap("@col", colName)).get()
        ).getCause();

        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .extracting(Throwable::getCause)
                .isInstanceOf(TimeoutException.class);

        arangoDB.getVersion().get();

        Thread.sleep(2_000);
        assertThat(col.count().get().getCount()).isEqualTo(1);

        arangoDB.shutdown();
    }

}

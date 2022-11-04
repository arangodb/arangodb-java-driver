package resilience.timeout;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import resilience.SingleServerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michele Rastelli
 */
class TimeoutTest extends SingleServerTest {

    static Stream<ArangoDB> arangoProvider() {
        return Stream.of(
                dbBuilder().timeout(1_000).useProtocol(Protocol.VST).build(),
                dbBuilder().timeout(1_000).useProtocol(Protocol.HTTP_VPACK).build(),
                dbBuilder().timeout(1_000).useProtocol(Protocol.HTTP2_VPACK).build()
        );
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
    @MethodSource("arangoProvider")
    void requestTimeout(ArangoDB arangoDB) throws InterruptedException {
        arangoDB.getVersion();
        String colName = "timeoutTest";
        ArangoCollection col = arangoDB.db().collection(colName);
        if (!col.exists()) col.create();
        col.truncate();

        try {
            arangoDB.db().query("" +
                            "INSERT {value:sleep(2)}\n" +
                            "INTO @@col\n" +
                            "RETURN NEW\n",
                    Collections.singletonMap("@col", colName),
                    Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            assertThat(e)
                    .isInstanceOf(ArangoDBException.class)
                    .extracting(Throwable::getCause)
                    .isInstanceOf(TimeoutException.class);
        }

        arangoDB.getVersion();

        Thread.sleep(2_000);
        assertThat(col.count().getCount()).isEqualTo(1);

        arangoDB.shutdown();
    }

}

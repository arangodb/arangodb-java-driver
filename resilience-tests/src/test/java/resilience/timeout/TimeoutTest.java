package resilience.timeout;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import resilience.SingleServerTest;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
        // https://github.com/vert-x3/vertx-web/issues/2296
        // WebClient: HTTP/2 request timeout does not throw TimeoutException
        assumeTrue(protocol != Protocol.HTTP2_VPACK);
        assumeTrue(protocol != Protocol.HTTP2_JSON);

        ArangoDB arangoDB = dbBuilder()
                .timeout(1_000)
                .useProtocol(protocol)
                .build();

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
                    Map.class,
                    Collections.singletonMap("@col", colName));
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

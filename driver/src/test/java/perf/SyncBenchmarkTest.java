package perf;

import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.Request;
import com.arangodb.internal.ArangoRequestParam;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Disabled
public class SyncBenchmarkTest {
    private final int warmupDurationSeconds = 15;
    private final int numberOfRequests = 1_000_000;

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersion(Protocol protocol) {
        System.out.println("-----------------------------------------");
        System.out.println("--- getVersion(): " + protocol);
        System.out.println("-----------------------------------------");

        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(protocol)
                .build();
        Benchmark benchmark = new Benchmark(warmupDurationSeconds, numberOfRequests) {
            @Override
            protected void sendRequest() {
                adb.getVersion();
            }

            @Override
            protected void shutdown() {
                adb.shutdown();
            }
        };
        benchmark.run();
        System.out.println("elapsed time [ms]: \t" + benchmark.waitComplete());
        System.out.println("throughput [req/s]: \t" + benchmark.getThroughput());
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersionWithDetails(Protocol protocol) {
        System.out.println("-----------------------------------------");
        System.out.println("--- getVersion w/ details: " + protocol);
        System.out.println("-----------------------------------------");

        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .protocol(protocol)
                .build();
        Benchmark benchmark = new Benchmark(warmupDurationSeconds, numberOfRequests) {
            private final Request<?> request = Request.builder()
                    .db(ArangoRequestParam.SYSTEM)
                    .method(Request.Method.GET)
                    .path("/_api/version")
                    .queryParam("details", "true")
                    .build();

            @Override
            protected void sendRequest() {
                adb.execute(request, Void.class);
            }

            @Override
            protected void shutdown() {
                adb.shutdown();
            }
        };
        benchmark.run();
        System.out.println("elapsed time [ms]: \t" + benchmark.waitComplete());
        System.out.println("throughput [req/s]: \t" + benchmark.getThroughput());
    }

}

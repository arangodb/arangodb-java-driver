package perf;

import com.arangodb.ArangoDB;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.internal.InternalRequest;
import com.arangodb.RequestType;
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

        ArangoDB adb = new ArangoDB.Builder().useProtocol(protocol).build();
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

        ArangoDB adb = new ArangoDB.Builder().useProtocol(protocol).build();
        Benchmark benchmark = new Benchmark(warmupDurationSeconds, numberOfRequests) {
            private final InternalRequest request = new InternalRequest(DbName.SYSTEM, RequestType.GET,
                    "/_api/version").putQueryParam("details", true);

            @Override
            protected void sendRequest() {
                adb.execute(request);
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

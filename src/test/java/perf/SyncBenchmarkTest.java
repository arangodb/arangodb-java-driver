package perf;

import com.arangodb.ArangoDB;
import com.arangodb.BaseJunit5;
import com.arangodb.DbName;
import com.arangodb.Protocol;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Disabled
public class SyncBenchmarkTest {
    private final int warmupDurationSeconds = 15;
    private final int numberOfRequests = 1_000_000;

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void getVersion(Protocol protocol) {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));
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
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));
        ArangoDB adb = new ArangoDB.Builder().useProtocol(protocol).build();
        Benchmark benchmark = new Benchmark(warmupDurationSeconds, numberOfRequests) {
            private final Request request = new Request(DbName.SYSTEM, RequestType.GET,
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

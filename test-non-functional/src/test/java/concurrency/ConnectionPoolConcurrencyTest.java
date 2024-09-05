package concurrency;

import com.arangodb.config.HostDescription;
import com.arangodb.internal.InternalRequest;
import com.arangodb.internal.InternalResponse;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.ConnectionPool;
import com.arangodb.internal.net.ConnectionPoolImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionPoolConcurrencyTest {

    private final ArangoConfig cfg = new ArangoConfig();

    {
        cfg.setMaxConnections(10_000);
    }

    private final ConnectionFactory cf = (config, host) -> new Connection() {
        @Override
        public void setJwt(String jwt) {
        }

        @Override
        public CompletableFuture<InternalResponse> executeAsync(InternalRequest request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
        }
    };

    @Test
    void foo() throws InterruptedException, ExecutionException, IOException {
        ConnectionPool cp = new ConnectionPoolImpl(HostDescription.parse("127.0.0.1:8529"), cfg, cf);
        ExecutorService es = Executors.newCachedThreadPool();

        List<? extends Future<?>> futures = es.invokeAll(Collections.nCopies(8, (Callable<?>) () -> {
            for (int i = 0; i < 10_000; i++) {
                cp.createConnection(HostDescription.parse("127.0.0.1:8529"));
                cp.connection();
                cp.setJwt("foo");
            }
            return null;
        }));

        for (Future<?> future : futures) {
            future.get();
        }
        cp.close();
        es.shutdown();
    }

}

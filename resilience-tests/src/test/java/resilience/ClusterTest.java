package resilience;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDB;
import com.arangodb.async.ArangoDBAsync;
import resilience.utils.MemoryAppender;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Tag("cluster")
public abstract class ClusterTest {

    protected static final String HOST = "127.0.0.1";
    protected static final String PASSWORD = "test";
    protected static final MemoryAppender logs = new MemoryAppender(Level.WARN);
    private static final List<Endpoint> endpoints = Arrays.asList(
            new Endpoint("cluster1", HOST, 18529, "172.28.0.1:8529"),
            new Endpoint("cluster2", HOST, 18539, "172.28.0.1:8539"),
            new Endpoint("cluster3", HOST, 18549, "172.28.0.1:8549")
    );

    @BeforeAll
    static void beforeAll() throws IOException {
        ToxiproxyClient client = new ToxiproxyClient(HOST, 8474);
        for (Endpoint ph : endpoints) {
            Proxy p = client.getProxyOrNull(ph.getName());
            if (p != null) {
                p.delete();
            }
            ph.setProxy(client.createProxy(ph.getName(), ph.getHost() + ":" + ph.getPort(), ph.getUpstream()));
        }
    }

    @AfterAll
    static void afterAll() throws IOException {
        for (Endpoint ph : endpoints) {
            ph.getProxy().delete();
        }
    }

    @BeforeEach
    void beforeEach() throws IOException {
        for (Endpoint ph : endpoints) {
            ph.getProxy().enable();
        }
    }

    protected static List<Endpoint> getEndpoints() {
        return endpoints;
    }

    protected static ArangoDB.Builder dbBuilder() {
        ArangoDB.Builder builder = new ArangoDB.Builder().password(PASSWORD);
        for (Endpoint ph : endpoints) {
            builder.host(ph.getHost(), ph.getPort());
        }
        return builder;
    }

    protected static ArangoDBAsync.Builder dbBuilderAsync() {
        ArangoDBAsync.Builder builder = new ArangoDBAsync.Builder().password(PASSWORD);
        for (Endpoint ph : endpoints) {
            builder.host(ph.getHost(), ph.getPort());
        }
        return builder;
    }

}

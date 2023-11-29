package resilience;

import com.arangodb.ArangoDB;
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
    protected static final MemoryAppender logs = new MemoryAppender();
    private static final List<Endpoint> endpoints = Arrays.asList(
            new Endpoint("cluster1", HOST, 18529, "172.28.0.1:8529"),
            new Endpoint("cluster2", HOST, 18539, "172.28.0.1:8539"),
            new Endpoint("cluster3", HOST, 18549, "172.28.0.1:8549")
    );

    @BeforeAll
    static void beforeAll() throws IOException {
        ToxiproxyClient client = new ToxiproxyClient(HOST, 8474);
        for (Endpoint endpoint : endpoints) {
            Proxy p = client.getProxyOrNull(endpoint.getName());
            if (p != null) {
                p.delete();
            }
            endpoint.setProxy(client.createProxy(endpoint.getName(), endpoint.getHost() + ":" + endpoint.getPort(), endpoint.getUpstream()));
        }
    }

    @AfterAll
    static void afterAll() throws IOException {
        for (Endpoint endpoint : endpoints) {
            endpoint.getProxy().delete();
        }
    }

    @BeforeEach
    void beforeEach() throws IOException {
        for (Endpoint endpoint : endpoints) {
            endpoint.getProxy().enable();
        }
    }

    protected static List<Endpoint> getEndpoints() {
        return endpoints;
    }

    protected static ArangoDB.Builder dbBuilder() {
        ArangoDB.Builder builder = new ArangoDB.Builder();
        for (Endpoint endpoint : endpoints) {
            builder.host(endpoint.getHost(), endpoint.getPort());
        }
        return builder.password(PASSWORD);
    }

    protected void enableAllEndpoints(){
        for (Endpoint endpoint : endpoints) {
            endpoint.enable();
        }
    }

    protected void disableAllEndpoints(){
        for (Endpoint endpoint : endpoints) {
            endpoint.disable();
        }
    }

}

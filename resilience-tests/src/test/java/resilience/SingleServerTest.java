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

@Tag("singleServer")
public abstract class SingleServerTest {

    protected static final String HOST = "127.0.0.1";
    protected static final String PASSWORD = "test";
    protected static final MemoryAppender logs = new MemoryAppender();
    private static final Endpoint endpoint = new Endpoint("singleServer", HOST, 18529, "172.28.0.1:8529");

    @BeforeAll
    static void beforeAll() throws IOException {
        ToxiproxyClient client = new ToxiproxyClient(HOST, 8474);
        Proxy p = client.getProxyOrNull(endpoint.getName());
        if (p != null) {
            p.delete();
        }
        endpoint.setProxy(client.createProxy(endpoint.getName(), HOST + ":" + endpoint.getPort(), endpoint.getUpstream()));
    }

    @AfterAll
    static void afterAll() throws IOException {
        endpoint.getProxy().delete();
    }

    @BeforeEach
    void beforeEach() {
        getEndpoint().enable();
        logs.reset();
    }

    protected static Endpoint getEndpoint() {
        return endpoint;
    }

    protected static ArangoDB.Builder dbBuilder() {
        return new ArangoDB.Builder()
                .host(endpoint.getHost(), endpoint.getPort())
                .password(PASSWORD);
    }

}

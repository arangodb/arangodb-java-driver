package resilience;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.Protocol;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.util.stream.Stream;

@Tag("singleServer")
public abstract class SingleServerTest extends TestUtils {

    private static final Endpoint endpoint = new Endpoint("singleServer", HOST, 18529, UPSTREAM_GW + ":8529");

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

    protected static Stream<Protocol> protocolProvider() {
        return Stream.of(Protocol.values())
                .filter(p -> !p.equals(Protocol.VST) || isLessThanVersion(3, 12));
    }

    protected static Stream<ArangoDB.Builder> builderProvider() {
        return protocolProvider().map(p -> dbBuilder().protocol(p));
    }

    protected static Stream<ArangoDB> adbProvider() {
        return builderProvider().map(ArangoDB.Builder::build);
    }

    protected static Stream<ArangoDBAsync> asyncAdbProvider() {
        return adbProvider().map(ArangoDB::async);
    }

}

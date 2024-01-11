package resilience;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBAsync;
import com.arangodb.Request;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import resilience.utils.MemoryAppender;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
            initServerId(endpoint);
        }
    }

    @AfterAll
    static void afterAll() throws IOException {
        for (Endpoint endpoint : endpoints) {
            endpoint.getProxy().delete();
        }
    }

    @BeforeEach
    void beforeEach() {
        enableAllEndpoints();
        logs.reset();
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

    protected static String serverIdGET(ArangoDB adb) {
        return adb.execute(Request.builder()
                        .method(Request.Method.GET)
                        .path("/_admin/status")
                        .build(), ObjectNode.class)
                .getBody()
                .get("serverInfo")
                .get("serverId")
                .textValue();
    }

    protected static String serverIdGET(ArangoDBAsync adb) {
        try {
            return adb.execute(Request.builder()
                            .method(Request.Method.GET)
                            .path("/_admin/status")
                            .build(), ObjectNode.class)
                    .get()
                    .getBody()
                    .get("serverInfo")
                    .get("serverId")
                    .textValue();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String serverIdPOST(ArangoDB adb) {
        return adb.execute(Request.builder()
                        .method(Request.Method.POST)
                        .path("/_admin/status")
                        .build(), ObjectNode.class)
                .getBody()
                .get("serverInfo")
                .get("serverId")
                .textValue();
    }

    protected static String serverIdPOST(ArangoDBAsync adb) {
        try {
            return adb.execute(Request.builder()
                            .method(Request.Method.POST)
                            .path("/_admin/status")
                            .build(), ObjectNode.class)
                    .get()
                    .getBody()
                    .get("serverInfo")
                    .get("serverId")
                    .textValue();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initServerId(Endpoint endpoint) {
        ArangoDB adb = new ArangoDB.Builder()
                .host(endpoint.getHost(), endpoint.getPort())
                .password(PASSWORD)
                .build();
        String serverId = serverIdGET(adb);
        endpoint.setServerId(serverId);
        adb.shutdown();
    }

    protected void enableAllEndpoints() {
        try {
            for (Endpoint endpoint : endpoints) {
                endpoint.getProxy().enable();
            }
            Thread.sleep(100);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void disableAllEndpoints() {
        try {
            for (Endpoint endpoint : endpoints) {
                endpoint.getProxy().disable();
            }
            Thread.sleep(100);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}

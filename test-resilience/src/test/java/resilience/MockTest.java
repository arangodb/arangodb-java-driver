package resilience;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDB;
import com.arangodb.Protocol;
import com.arangodb.internal.net.Communication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.integration.ClientAndServer;

import java.util.Collections;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

public class MockTest extends SingleServerTest {

    protected ClientAndServer mockServer;
    protected ArangoDB arangoDB;

    public MockTest() {
        super(Collections.singletonMap(Communication.class, Level.DEBUG));
    }

    @BeforeEach
    void before() {
        mockServer = startClientAndServer(getEndpoint().getHost(), getEndpoint().getPort());
        arangoDB = new ArangoDB.Builder()
                .protocol(Protocol.HTTP_JSON)
                .password(PASSWORD)
                .host("127.0.0.1", mockServer.getPort())
                .build();
    }

    @AfterEach
    void after() {
        arangoDB.shutdown();
        mockServer.stop();
    }

}

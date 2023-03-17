package resilience.http;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import resilience.SingleServerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class MockTest extends SingleServerTest {

    private ClientAndServer mockServer;
    private ArangoDB arangoDB;

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

    @Test
    void doTest() {
        arangoDB.getVersion();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version")
                )
                .respond(
                        response()
                                .withStatusCode(503)
                                .withBody("{\"error\":true,\"errorNum\":503,\"errorMessage\":\"boom\",\"code\":503}")
                );

        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("boom");
    }
}

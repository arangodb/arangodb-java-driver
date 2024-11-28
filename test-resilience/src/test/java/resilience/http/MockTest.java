package resilience.http;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.internal.net.Communication;
import com.fasterxml.jackson.core.JsonParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import resilience.SingleServerTest;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class MockTest extends SingleServerTest {

    private ClientAndServer mockServer;
    private ArangoDB arangoDB;

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

    @Test
    void retryOn503() {
        arangoDB.getVersion();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version"),
                        Times.exactly(2)
                )
                .respond(
                        response()
                                .withStatusCode(503)
                                .withBody("{\"error\":true,\"errorNum\":503,\"errorMessage\":\"boom\",\"code\":503}")
                );

        logs.reset();
        arangoDB.getVersion();
        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));
    }

    @Test
    void retryOn503Async() throws ExecutionException, InterruptedException {
        arangoDB.async().getVersion().get();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version"),
                        Times.exactly(2)
                )
                .respond(
                        response()
                                .withStatusCode(503)
                                .withBody("{\"error\":true,\"errorNum\":503,\"errorMessage\":\"boom\",\"code\":503}")
                );

        logs.reset();
        arangoDB.async().getVersion().get();
        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.WARN))
                .anyMatch(e -> e.getFormattedMessage().contains("Could not connect to host"));
    }

    @Test
    void unparsableData() {
        arangoDB.getVersion();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version")
                )
                .respond(
                        response()
                                .withStatusCode(504)
                                .withBody("upstream timed out")
                );

        logs.reset();
        Throwable thrown = catchThrowable(() -> arangoDB.getVersion());
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("[Unparsable data]")
                .hasMessageContaining("Response: {statusCode=504,");
        Throwable[] suppressed = thrown.getCause().getSuppressed();
        assertThat(suppressed).hasSize(1);
        assertThat(suppressed[0])
                .isInstanceOf(ArangoDBException.class)
                .cause()
                .isInstanceOf(JsonParseException.class);
        assertThat(logs.getLogs())
                .filteredOn(e -> e.getLevel().equals(Level.DEBUG))
                .anySatisfy(e -> assertThat(e.getFormattedMessage())
                        .contains("Received Response")
                        .contains("statusCode=504")
                        .contains("[Unparsable data]")
                );
    }

    @Test
    void textPlainData() {
        arangoDB.getVersion();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version")
                )
                .respond(
                        response()
                                .withStatusCode(504)
                                .withHeader("Content-Type", "text/plain")
                                .withBody("upstream timed out")
                );

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion());
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("upstream timed out");
    }

    @Test
    void textPlainDataWithCharset() {
        arangoDB.getVersion();

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/.*/_api/version")
                )
                .respond(
                        response()
                                .withStatusCode(504)
                                .withHeader("Content-Type", "text/plain; charset=utf-8")
                                .withBody("upstream timed out")
                );

        Throwable thrown = catchThrowable(() -> arangoDB.getVersion());
        assertThat(thrown)
                .isInstanceOf(ArangoDBException.class)
                .hasMessageContaining("upstream timed out");
    }

}

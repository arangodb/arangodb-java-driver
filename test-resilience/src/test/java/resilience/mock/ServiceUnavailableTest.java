package resilience.mock;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.mockserver.matchers.Times;
import resilience.MockTest;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class ServiceUnavailableTest extends MockTest {

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


}

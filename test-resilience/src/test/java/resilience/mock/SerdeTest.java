package resilience.mock;

import ch.qos.logback.classic.Level;
import com.arangodb.ArangoDBException;
import com.arangodb.Request;
import com.arangodb.Response;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.util.RawJson;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import resilience.MockTest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class SerdeTest extends MockTest {

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

    @Test
    void getDocumentsWithErrorField() {
        List<String> keys = Arrays.asList("1", "2", "3");

        String resp = "[" +
                "{\"error\":true,\"_key\":\"1\",\"_id\":\"col/1\",\"_rev\":\"_i4otI-q---\"}," +
                "{\"_key\":\"2\",\"_id\":\"col/2\",\"_rev\":\"_i4otI-q--_\"}," +
                "{\"_key\":\"3\",\"_id\":\"col/3\",\"_rev\":\"_i4otI-q--A\"}" +
                "]";

        mockServer
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/.*/_api/document/col")
                                .withQueryStringParameter("onlyget", "true")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(resp.getBytes(StandardCharsets.UTF_8))
                );

        MultiDocumentEntity<JsonNode> res = arangoDB.db().collection("col").getDocuments(keys, JsonNode.class);
        assertThat(res.getErrors()).isEmpty();
        assertThat(res.getDocuments()).hasSize(3)
                .anySatisfy(d -> assertThat(d.get("_key").textValue()).isEqualTo("1"))
                .anySatisfy(d -> assertThat(d.get("_key").textValue()).isEqualTo("2"))
                .anySatisfy(d -> assertThat(d.get("_key").textValue()).isEqualTo("3"));
    }

    @Test
    void getXArangoDumpJsonLines() {
        String resp = "{\"a\":1}\n" +
                "{\"b\":2}\n" +
                "{\"c\":3}";

        mockServer
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/_db/foo/_api/foo")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/x-arango-dump; charset=utf-8")
                                .withBody(resp.getBytes(StandardCharsets.UTF_8))
                );

        Response<RawJson> res = arangoDB.execute(Request.builder()
                .method(Request.Method.GET)
                .db("foo")
                .path("/_api/foo")
                .build(), RawJson.class);
        assertThat(res.getBody().get()).endsWith("{\"c\":3}");
    }
}

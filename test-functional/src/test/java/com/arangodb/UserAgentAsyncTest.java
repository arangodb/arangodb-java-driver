package com.arangodb;

import com.arangodb.util.ProtocolSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentAsyncTest extends BaseJunit5 {
    @ParameterizedTest
    @ProtocolSource
    void userAgentHeader(Protocol protocol) throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .build()
                .async();

        Response<JsonNode> resp = adb.execute(Request.builder()
                .method(Request.Method.GET)
                .path("/_admin/echo")
                .build(), JsonNode.class)
                .get();
        String headerValue = resp.getBody().get("headers").get("x-arango-driver").textValue();

        String jvmVersion = System.getProperty("java.specification.version");
        String expected = "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + jvmVersion + ")";

        assertThat(headerValue).isEqualTo(expected);
        adb.shutdown();
    }
}

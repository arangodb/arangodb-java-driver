package com.arangodb;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class UserAgentAsyncTest extends BaseJunit5 {

    private static final String EXPECTED_VERSION = "7.5.1";

    @Test
    void packageVersion() {
        assertThat(PackageVersion.VERSION).isEqualTo(EXPECTED_VERSION);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void userAgentHeader(Protocol protocol) throws ExecutionException, InterruptedException {
        assumeTrue(!protocol.equals(Protocol.VST) || BaseJunit5.isLessThanVersion(3, 12));

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

package com.arangodb;

import com.arangodb.util.ProtocolSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentTest extends BaseJunit5 {

    private static final String EXPECTED_VERSION = "7.25.0";

    private static final boolean SHADED = Boolean.parseBoolean(System.getProperty("shaded"));

    @Test
    void packageVersion() {
        assertThat(PackageVersion.VERSION).isEqualTo(EXPECTED_VERSION + (SHADED ? "-shaded" : ""));
    }

    @Test
    void packageVersionIsShaded() {
        assertThat(PackageVersion.SHADED).isEqualTo(SHADED);
    }

    @ParameterizedTest
    @ProtocolSource
    void userAgentHeader(Protocol protocol) {
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(config)
                .protocol(protocol)
                .build();

        Response<JsonNode> resp = adb.execute(Request.builder()
                .method(Request.Method.GET)
                .path("/_admin/echo")
                .build(), JsonNode.class);
        String headerValue = resp.getBody().get("headers").get("x-arango-driver").textValue();

        String jvmVersion = System.getProperty("java.specification.version");
        String expected = "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + jvmVersion + ")";

        assertThat(headerValue).isEqualTo(expected);
        adb.shutdown();
    }
}

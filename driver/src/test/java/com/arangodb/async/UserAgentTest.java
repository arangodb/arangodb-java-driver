package com.arangodb.async;


import com.arangodb.PackageVersion;
import com.arangodb.Request;
import com.arangodb.Response;
import com.arangodb.config.ArangoConfigProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentTest {
    @Test
    void userAgentHeader() throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDBAsync.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .build();

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
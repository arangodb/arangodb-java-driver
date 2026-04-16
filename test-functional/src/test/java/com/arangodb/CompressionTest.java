package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.util.ProtocolSource;
import org.junit.jupiter.params.ParameterizedTest;
import tools.jackson.databind.JsonNode;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Non-exhaustive tests of content encoding, executed during integration and native tests.
 * A complete test is at test-resilience/src/test/java/resilience/compression/CompressionTest.java
 *
 * @author Michele Rastelli
 */
class CompressionTest extends BaseJunit5 {

    @ParameterizedTest
    @ProtocolSource
    void gzip(Protocol protocol) {
        doTest(protocol, Compression.GZIP);
    }

    @ParameterizedTest
    @ProtocolSource
    void deflate(Protocol protocol) {
        doTest(protocol, Compression.DEFLATE);
    }

    void doTest(Protocol protocol, Compression compression) {
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(ArangoConfigProperties.fromFile())
                .protocol(protocol)
                .compression(compression)
                .compressionThreshold(0)
                .compressionLevel(3)
                .build();

        Response<JsonNode> resp = adb.execute(Request.builder()
                .method(Request.Method.POST)
                .path("/_admin/echo")
                .body(UUID.randomUUID().toString())
                .build(), JsonNode.class);

        String encoding = compression.toString().toLowerCase(Locale.ROOT);
        String reqAcceptEncoding = resp.getBody().get("headers").get("accept-encoding").stringValue();
        assertThat(reqAcceptEncoding).contains(encoding);

        adb.shutdown();
    }

}

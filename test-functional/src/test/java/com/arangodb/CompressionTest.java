package com.arangodb;

import com.arangodb.config.ArangoConfigProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Non-exhaustive tests of content encoding, executed during integration and native tests.
 * A complete test is at test-resilience/src/test/java/resilience/compression/CompressionTest.java
 *
 * @author Michele Rastelli
 */
class CompressionTest extends BaseJunit5 {

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void gzip(Protocol protocol) {
        doTest(protocol, Compression.GZIP);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void deflate(Protocol protocol) {
        doTest(protocol, Compression.DEFLATE);
    }

    void doTest(Protocol protocol, Compression compression) {
        assumeTrue(isAtLeastVersion(3, 12));
        assumeTrue(protocol != Protocol.VST);

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
        String reqAcceptEncoding = resp.getBody().get("headers").get("accept-encoding").textValue();
        assertThat(reqAcceptEncoding).contains(encoding);

        adb.shutdown();
    }

}

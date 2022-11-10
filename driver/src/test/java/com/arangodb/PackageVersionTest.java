package com.arangodb;

import com.arangodb.internal.config.FileConfigPropertiesProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.lanwen.verbalregex.VerbalExpression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PackageVersionTest extends BaseJunit5 {

    @Test
    void packageVersion() {
        VerbalExpression testRegex = VerbalExpression.regex()
                .startOfLine()
                // major
                .digit().atLeast(1)
                .then(".")
                // minor
                .digit().atLeast(1)
                .then(".")
                // patch
                .digit().atLeast(1)
                .maybe("-SNAPSHOT")
                .endOfLine()
                .build();
        assertThat(PackageVersion.VERSION).matches(testRegex.toString());
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void userAgentHeader(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);
        ArangoDB adb = new ArangoDB.Builder()
                .loadProperties(new FileConfigPropertiesProvider())
                .useProtocol(protocol)
                .build();

        Response<JsonNode> resp = adb.execute(Request.builder()
                .method(Request.Method.GET)
                .path("/_admin/echo")
                .build(), JsonNode.class);
        assertThat(resp.getBody().get("headers").get("user-agent").textValue()).endsWith(PackageVersion.VERSION);
        adb.shutdown();
    }
}

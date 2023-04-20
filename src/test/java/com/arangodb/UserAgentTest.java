package com.arangodb;

import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentTest extends BaseJunit5 {

    private static final String EXPECTED_VERSION = "6.23.0";

    @Test
    void packageVersion() {
        assertThat(PackageVersion.VERSION).isEqualTo(EXPECTED_VERSION);
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void userAgentHeader(Protocol protocol) {
        ArangoDB adb = new ArangoDB.Builder()
                .useProtocol(protocol)
                .build();

        Request request = new Request(DbName.SYSTEM, RequestType.GET, "/_admin/echo");
        Response resp = adb.execute(request);
        String headerValue = resp.getBody().get("headers").get("x-arango-driver").getAsString();

        String jvmVersion = System.getProperty("java.specification.version");
        String expected = "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + jvmVersion + ")";

        assertThat(headerValue).isEqualTo(expected);
        adb.shutdown();
    }
}

package com.arangodb.async;

import com.arangodb.DbName;
import com.arangodb.PackageVersion;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentTest {
    @Test
    void userAgentHeader() throws ExecutionException, InterruptedException {
        ArangoDBAsync adb = new ArangoDBAsync.Builder().build();
        Request request = new Request(DbName.SYSTEM, RequestType.GET, "/_admin/echo");
        Response resp = adb.execute(request).get();
        String headerValue = resp.getBody().get("headers").get("x-arango-driver").getAsString();

        String jvmVersion = System.getProperty("java.specification.version");
        String expected = "JavaDriver/" + PackageVersion.VERSION + " (JVM/" + jvmVersion + ")";

        assertThat(headerValue).isEqualTo(expected);
        adb.shutdown();
    }
}

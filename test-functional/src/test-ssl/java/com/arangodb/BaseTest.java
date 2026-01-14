package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.api.BeforeAll;

import java.net.URISyntaxException;
import java.nio.file.Paths;

abstract class BaseTest {
    /*-
     * a SSL trust store
     *
     * create the trust store for the self signed certificate:
     * keytool -import -alias "my arangodb server cert" -file server.pem -keystore example.truststore
     *
     * Documentation:
     * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/SSLSocketFactory.html
     */
    static final String SSL_TRUSTSTORE_RESOURCE = "/example.truststore";
    static final String SSL_TRUSTSTORE_PASSWORD = "12345678";
    static final String SSL_TRUSTSTORE_PATH;

    static {
        try {
            SSL_TRUSTSTORE_PATH = Paths.get(BaseTest.class.getResource(SSL_TRUSTSTORE_RESOURCE).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static ArangoDBVersion version;

    @BeforeAll
    static void fetchVersion() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslTrustStorePath(SSL_TRUSTSTORE_PATH)
                .sslTrustStorePassword(SSL_TRUSTSTORE_PASSWORD)
                .verifyHost(false)
                .build();
        version = adb.getVersion();
        adb.shutdown();
    }

}

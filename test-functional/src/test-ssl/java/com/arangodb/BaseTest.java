package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.api.BeforeAll;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
            Path tempFile = Files.createTempFile("example", ".truststore");
            tempFile.toFile().deleteOnExit();
            try (InputStream in = BaseTest.class.getResourceAsStream(SSL_TRUSTSTORE_RESOURCE)) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            SSL_TRUSTSTORE_PATH = tempFile.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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

package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.api.BeforeAll;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

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
    private static final String SSL_TRUSTSTORE = "/example.truststore";
    private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";
    static ArangoDBVersion version;

    @BeforeAll
    static void fetchVersion() {
        ArangoDB adb = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslContext(createSslContext())
                .verifyHost(false)
                .build();
        version = adb.getVersion();
        adb.shutdown();
    }

    static SSLContext createSslContext() {
        SSLContext sc;
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(SslExampleTest.class.getResourceAsStream(SSL_TRUSTSTORE), SSL_TRUSTSTORE_PASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, SSL_TRUSTSTORE_PASSWORD.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sc;
    }
}

/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import utils.ProtocolSource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class SslExampleTest extends BaseTest {

    @Disabled("Only local execution, in CircleCI port 8529 exposed to localhost")
    @ParameterizedTest
    @ProtocolSource
    void connect(Protocol protocol) {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .host("localhost", 8529)
                .password("test")
                .useSsl(true)
                .sslTrustStorePath(SSL_TRUSTSTORE_PATH)
                .sslTrustStorePassword(SSL_TRUSTSTORE_PASSWORD)
                .protocol(protocol)
                .build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @ParameterizedTest
    @ProtocolSource
    void noopHostnameVerifier(Protocol protocol) {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslTrustStorePath(SSL_TRUSTSTORE_PATH)
                .sslTrustStorePassword(SSL_TRUSTSTORE_PASSWORD)
                .verifyHost(false)
                .protocol(protocol)
                .build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @ParameterizedTest
    @ProtocolSource
    void hostnameVerifierFailure(Protocol protocol) {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslTrustStorePath(SSL_TRUSTSTORE_PATH)
                .sslTrustStorePassword(SSL_TRUSTSTORE_PASSWORD)
                .verifyHost(true)
                .protocol(protocol)
                .build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException ex = (ArangoDBException) thrown;
        assertThat(ex.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) ex.getCause()).getExceptions();
        exceptions.forEach(e -> assertThat(e).isInstanceOf(SSLHandshakeException.class));
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void connectWithSslContext(Protocol protocol) {
        final ArangoDB arangoDB = new ArangoDB.Builder()
                .protocol(protocol)
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslContext(createSslContext())
                .verifyHost(false)
                .build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    static SSLContext createSslContext() {
        SSLContext sc;
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(SslExampleTest.class.getResourceAsStream(SSL_TRUSTSTORE_RESOURCE), SSL_TRUSTSTORE_PASSWORD.toCharArray());

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

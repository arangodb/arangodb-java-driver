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
import utils.ProtocolSource;

import javax.net.ssl.SSLHandshakeException;
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
                .sslContext(createSslContext())
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
                .sslContext(createSslContext())
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
                .sslContext(createSslContext())
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


}

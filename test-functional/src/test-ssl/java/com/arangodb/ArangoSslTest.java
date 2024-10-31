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
import com.arangodb.http.HttpProtocolConfig;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.net.ssl.SSLHandshakeException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
class ArangoSslTest extends BaseTest {

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void httpProxy(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);

        final ArangoDB arangoDB = new ArangoDB.Builder()
                .protocol(protocol)
                .host("172.28.0.1", 8529)
                .password("test")
                .useSsl(true)
                .sslContext(createSslContext())
                .verifyHost(false)
                .protocolConfig(HttpProtocolConfig.builder()
                        .proxyOptions(new ProxyOptions()
                                .setType(ProxyType.HTTP)
                                .setHost("127.0.0.1")
                                .setPort(8888)
                                .setUsername("user")
                                .setPassword("password"))
                        .build())
                .build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void connect(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);

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

    @ParameterizedTest
    @EnumSource(Protocol.class)
    void connectWithoutValidSslContext(Protocol protocol) {
        assumeTrue(protocol != Protocol.VST);

        final ArangoDB arangoDB = new ArangoDB.Builder()
                .protocol(protocol)
                .host("172.28.0.1", 8529)
                .useSsl(true)
                .build();
        Throwable thrown = catchThrowable(arangoDB::getVersion);
        assertThat(thrown).isInstanceOf(ArangoDBException.class);
        ArangoDBException ex = (ArangoDBException) thrown;
        assertThat(ex.getCause()).isInstanceOf(ArangoDBMultipleException.class);
        List<Throwable> exceptions = ((ArangoDBMultipleException) ex.getCause()).getExceptions();
        exceptions.forEach(e -> assertThat(e).isInstanceOf(SSLHandshakeException.class));
    }

}

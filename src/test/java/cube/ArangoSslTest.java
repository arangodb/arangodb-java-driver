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

package cube;

import static cube.CubeUtils.arangoAwaitStrategy;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import org.arquillian.cube.containerobject.ConnectionMode;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.BindMode;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.Container;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.DockerContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;

import com.arangodb.entity.ArangoDBVersion;
import org.junit.runner.RunWith;

/**
 * @author Mark Vollmary
 */
@RunWith(Arquillian.class)
public class ArangoSslTest {

    private Path sslCertPath = Paths.get("docker/server.pem").toAbsolutePath();

    @DockerContainer
    Container server = Container.withContainerName("arangodbssl")
            .fromImage("docker.io/arangodb/arangodb:3.5.1")
            .withPortBinding(8529)
            .withAwaitStrategy(arangoAwaitStrategy())
            .withEnvironment("ARANGO_ROOT_PASSWORD", "test")
            .withConnectionMode(ConnectionMode.START_AND_STOP_AROUND_CLASS)
            .withVolume(sslCertPath.toString(), "/server.pem", BindMode.READ_ONLY)
            .withCommand("arangod --ssl.keyfile /server.pem --server.endpoint ssl://0.0.0.0:8529")
            .build();

    /*-
     * a SSL trust store
     *
     * create the trust store for the self signed certificate:
     * keytool -import -alias "my arangodb server cert" -file UnitTests/server.pem -keystore example.truststore
     *
     * Documentation:
     * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/conn/ssl/SSLSocketFactory.html
     */
    private static final String SSL_TRUSTSTORE = "/example.truststore";
    private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";

    @Test
    public void connect() throws Exception {
        final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(this.getClass().getResourceAsStream(SSL_TRUSTSTORE), SSL_TRUSTSTORE_PASSWORD.toCharArray());

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, SSL_TRUSTSTORE_PASSWORD.toCharArray());

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        final SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        final ArangoDB arangoDB = new ArangoDB.Builder()
                .host(server.getIpAddress(), server.getBindPort(8529))
                .useSsl(true).sslContext(sc).build();
        final ArangoDBVersion version = arangoDB.getVersion();
        assertThat(version, is(notNullValue()));
    }

    @Test
    public void connectWithoutValidSslContext() {
        try {
            final ArangoDB arangoDB = new ArangoDB.Builder()
                    .host(server.getIpAddress(), server.getBindPort(8529))
                    .useSsl(true).build();
            arangoDB.getVersion();
            fail("this should fail");
        } catch (final ArangoDBException ex) {
            assertThat(ex.getCause() instanceof SSLHandshakeException, is(true));
        }
    }

}

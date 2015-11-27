/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.ssl.SSLContexts;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.arangodb.entity.ArangoVersion;

/**
 * UnitTest for ArangoConfigure.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoConfigureTest {

	private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";

	/**
	 * a SSL trust store
	 * 
	 * create the trust store for the self signed certificate: keytool -import
	 * -alias "my arangodb server cert" -file UnitTests/server.pem -keystore
	 * example.truststore
	 * 
	 * https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/
	 * apache/http/conn/ssl/SSLSocketFactory.html
	 */
	private static final String SSL_TRUSTSTORE = "/example.truststore";

	@Test
	public void load_from_property_file() {
		// validate file in classpath.
		assertThat(getClass().getResource("/arangodb.properties"), is(notNullValue()));

		ArangoConfigure configure = new ArangoConfigure();
		assertThat(configure.getArangoHost().getPort(), is(8529));
		assertThat(configure.getArangoHost().getHost(), is(notNullValue()));
		assertThat(configure.getDefaultDatabase(), is(nullValue()));

	}

	@Test
	public void load_from_proerty_file2() {

		ArangoConfigure configure = new ArangoConfigure();
		configure.loadProperties("/arangodb-test.properties");

		assertThat(configure.getRetryCount(), is(10));
		assertThat(configure.getDefaultDatabase(), is("mydb2"));

		ArangoHost arangoHost = configure.getArangoHost();
		assertThat(arangoHost.getPort(), is(9999));
		assertThat(arangoHost.getHost(), is(notNullValue()));

		assertThat(configure.hasFallbackHost(), is(true));
	}

	@Test
	public void connect_timeout() throws ArangoException {

		ArangoConfigure configure = new ArangoConfigure();
		configure.getArangoHost().setHost("1.0.0.200");
		configure.setConnectionTimeout(1); // 1ms
		configure.init();

		ArangoDriver driver = new ArangoDriver(configure);

		try {
			driver.getCollections();
			fail("did no timeout");
		} catch (ArangoException e) {
			assertThat(e.getCause(), instanceOf(ConnectTimeoutException.class));
		}

		configure.shutdown();

	}

	@Test
	@Ignore(value = "this fails some times")
	public void so_connect_timeout() throws ArangoException {

		ArangoConfigure configure = new ArangoConfigure();
		configure.setConnectionTimeout(5000);
		configure.setTimeout(1); // 1ms
		configure.init();

		ArangoDriver driver = new ArangoDriver(configure);

		try {
			driver.getCollections();
			fail("did no timeout");
		} catch (ArangoException e) {
			assertThat(e.getCause(), instanceOf(SocketTimeoutException.class));
		}

		configure.shutdown();

	}

	@Test
	public void reconnectFallbackArangoHost() throws ArangoException {

		ArangoConfigure configure = new ArangoConfigure();

		// copy default arango host to fallback
		ArangoHost arangoHost = configure.getArangoHost();
		ArangoHost ah = new ArangoHost(arangoHost.getHost(), arangoHost.getPort());
		configure.addFallbackArangoHost(ah);

		// change default port to wrong port
		arangoHost.setPort(1025);
		configure.init();

		ArangoDriver driver = new ArangoDriver(configure);

		driver.getCollections();

		configure.shutdown();

	}

	@Test
	public void sslWithSelfSignedCertificateTest() throws ArangoException, KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, URISyntaxException {

		// create a sslContext for the self signed certificate
		URL resource = this.getClass().getResource(SSL_TRUSTSTORE);
		SSLContext sslContext = SSLContexts.custom()
				.loadTrustMaterial(Paths.get(resource.toURI()).toFile(), SSL_TRUSTSTORE_PASSWORD.toCharArray()).build();

		ArangoConfigure configuration = new ArangoConfigure("/ssl-arangodb.properties");
		configuration.setSslContext(sslContext);
		configuration.init();

		ArangoDriver arangoDriver = new ArangoDriver(configuration);

		ArangoVersion version = arangoDriver.getVersion();

		Assert.assertNotNull(version);
	}

}

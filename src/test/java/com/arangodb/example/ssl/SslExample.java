/*
 * Copyright (C) 2015 ArangoDB GmbH
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

package com.arangodb.example.ssl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;
import org.junit.Assert;
import org.junit.Test;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ArangoHost;
import com.arangodb.entity.ArangoVersion;
import com.arangodb.http.HttpResponseEntity;

/*-
 * Example for using a HTTPS connection
 * 
 * Create a self signed certificate for arangod (or use the test certificate of the unit tests)
 * https://docs.arangodb.com/ConfigureArango/Arangod.html
 * 
 * Start arangod with HTTP (port 8529) and HTTPS (port 8530): 
 * 
 * bin/arangod 
 *            --server.disable-authentication=false 
 *            --configuration ./etc/relative/arangod.conf 
 *            --server.endpoint ssl://localhost:8530 
 *            --server.keyfile UnitTests/server.pem 
 *            --server.endpoint tcp://localhost:8529
 *            ../database/ 
 * 
 * @author a-brandt
 *
 */
public class SslExample {

	private static final String SSL_TRUSTSTORE_PASSWORD = "12345678";

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

	@Test
	public void httpTest() throws ArangoException {

		ArangoConfigure configuration = null;
		try {
			configuration = new ArangoConfigure();
			// get host and port from arangodb.properties
			// configuration.setArangoHost(new ArangoHost("localhost", 8529));
			configuration.init();

			final ArangoDriver arangoDriver = new ArangoDriver(configuration);

			final ArangoVersion version = arangoDriver.getVersion();
			Assert.assertNotNull(version);
		} finally {
			if (configuration != null) {
				configuration.shutdown();
			}
		}
	}

	@Test
	public void sslConnectionTest() throws ArangoException {
		// use HTTPS with java default trust store
		ArangoConfigure configuration = null;
		try {
			configuration = new ArangoConfigure();
			configuration.setArangoHost(new ArangoHost("www.arangodb.com", 443));
			configuration.setUseSsl(true);
			configuration.init();

			final ArangoDriver arangoDriver = new ArangoDriver(configuration);

			final HttpResponseEntity response = arangoDriver.getHttpManager().doGet("/");
			Assert.assertEquals(200, response.getStatusCode());
		} finally {
			if (configuration != null) {
				configuration.shutdown();
			}
		}
	}

	@Test
	public void sslWithSelfSignedCertificateTest() throws ArangoException, KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, URISyntaxException {

		ArangoConfigure configuration = null;
		try {
			// create a sslContext for the self signed certificate
			final URL resource = this.getClass().getResource(SSL_TRUSTSTORE);
			final SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(new File(resource.toURI()), SSL_TRUSTSTORE_PASSWORD.toCharArray()).build();

			configuration = new ArangoConfigure("/ssl-arangodb.properties");
			configuration.setSslContext(sslContext);
			configuration.init();

			final ArangoDriver arangoDriver = new ArangoDriver(configuration);

			final ArangoVersion version = arangoDriver.getVersion();
			Assert.assertNotNull(version);
		} finally {
			if (configuration != null) {
				configuration.shutdown();
			}
		}
	}

	@Test
	public void sslHandshakeExceptionTest() {
		ArangoConfigure configuration = null;
		try {
			configuration = new ArangoConfigure("/ssl-arangodb.properties");
			configuration.init();

			final ArangoDriver arangoDriver = new ArangoDriver(configuration);

			try {
				// java do not trust self signed certificates

				arangoDriver.getVersion();
				Assert.fail("this should fail");

			} catch (final ArangoException e) {
				final Throwable cause = e.getCause();
				Assert.assertTrue(cause instanceof javax.net.ssl.SSLHandshakeException);
			}
		} finally {
			if (configuration != null) {
				configuration.shutdown();
			}
		}
	}

	@Test
	public void sslPeerUnverifiedExceptionTest() throws ArangoException, KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, URISyntaxException {

		ArangoConfigure configuration = null;
		try {
			// create a sslContext for the self signed certificate
			final URL resource = this.getClass().getResource(SSL_TRUSTSTORE);
			final SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(new File(resource.toURI()), SSL_TRUSTSTORE_PASSWORD.toCharArray()).build();

			configuration = new ArangoConfigure("/ssl-arangodb.properties");
			// 127.0.0.1 is the wrong name
			configuration.getArangoHost().setHost("127.0.0.1");
			configuration.setSslContext(sslContext);
			configuration.init();

			final ArangoDriver arangoDriver = new ArangoDriver(configuration);

			try {
				arangoDriver.getVersion();
				Assert.fail("this should fail");
			} catch (final ArangoException e) {
				final Throwable cause = e.getCause();
				Assert.assertTrue(cause instanceof javax.net.ssl.SSLPeerUnverifiedException);
			}
		} finally {
			if (configuration != null) {
				configuration.shutdown();
			}
		}
	}

}

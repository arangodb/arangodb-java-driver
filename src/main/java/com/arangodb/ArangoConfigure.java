/*
 * Copyright (C) 2012 tamtam180
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.http.BatchHttpManager;
import com.arangodb.util.IOUtils;

/**
 * Configure of ArangoDB.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author a-brandt
 * 
 */
public class ArangoConfigure {

	private static Logger logger = LoggerFactory.getLogger(ArangoConfigure.class);

	/** default host of ArangoDB */
	private static final String DEFAULT_HOST = "127.0.0.1";
	/** default port of ArangoDB */
	private static final int DEFAULT_PORT = 8529;

	/** default */
	private static final int DEFAULT_MAX_PER_CONNECTION = 20;
	/** default maximum conections */
	private static final int DEFAULT_MAX_CONNECTION = 20;

	/** default property file */
	private static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";

	private List<ArangoHost> arangoHosts;
	private int currentArangoHost;

	/** connection timeout(ms) */
	private int connectionTimeout = -1;
	/** socket read timeout(ms) */
	private int timeout = -1;

	/** max connection per configure */
	private int maxTotalConnection;
	/** max connection per host */
	private int maxPerConnection;

	/** Basic auth user */
	private String user;
	/** Basic auth password */
	private String password;

	/** proxy-host */
	private String proxyHost;
	/** proxy-port */
	private int proxyPort;

	/** http retry count */
	private int retryCount = 3;

	/**
	 * number of connect retries (0 means infinite)
	 */
	private int connectRetryCount = 3;

	/**
	 * milliseconds
	 */
	private int connectRetryWait = 1000;

	/** Default Database */
	String defaultDatabase;

	private boolean enableCURLLogger = false;

	private int validateAfterInactivity = -1;

	private boolean useSsl = false;

	private SSLContext sslContext = null;

	private String sslTrustStore = null;

	/**
	 * the default ArangoDB cursor batch size
	 */
	private int batchSize = 20;

	BatchHttpManager httpManager;

	public ArangoConfigure() {
		init(DEFAULT_PROPERTY_FILE);
	}

	public ArangoConfigure(final String propertyPath) {
		init(propertyPath);
	}

	private void init(final String propertyPath) {
		arangoHosts = new ArrayList<ArangoHost>();
		final ArangoHost defaultHost = new ArangoHost(DEFAULT_HOST, DEFAULT_PORT);
		arangoHosts.add(defaultHost);
		currentArangoHost = 0;

		this.maxPerConnection = DEFAULT_MAX_PER_CONNECTION;
		this.maxTotalConnection = DEFAULT_MAX_CONNECTION;
		loadProperties(propertyPath);
	}

	/**
	 * Load configure from arangodb.properties in classpath, if exists.
	 */
	public void loadProperties() {
		loadProperties(DEFAULT_PROPERTY_FILE);
	}

	/**
	 * Load configure from "propertyPath" in classpath, if exists.
	 * 
	 * @param propertyPath
	 */
	public void loadProperties(final String propertyPath) {
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream(propertyPath);
			if (in != null) {
				logger.debug("load property: file={}", propertyPath);

				loadProperties(in);
			}
		} catch (final IOException e) {
			logger.warn("load property error", e);
		} finally {
			if (in != null) {
				IOUtils.close(in);
			}
		}
	}

	private void loadProperties(final InputStream in) throws IOException {

		final Properties prop = new Properties();
		prop.load(in);

		//
		final String port = prop.getProperty("port");
		if (port != null) {
			arangoHosts.get(0).setPort(Integer.parseInt(port));
		}

		final String host = prop.getProperty("host");
		if (host != null) {
			arangoHosts.get(0).setHost(host);
		}

		final String arangoHost = prop.getProperty("arangoHost");
		if (arangoHost != null) {
			final ArangoHost ah = parseArangoHost(arangoHost);
			if (ah != null) {
				arangoHosts.get(0).setHost(ah.getHost());
				arangoHosts.get(0).setPort(ah.getPort());
			}
		}

		final String fallbackArangoHost = prop.getProperty("fallbackArangoHost");
		if (fallbackArangoHost != null) {
			final ArangoHost ah = parseArangoHost(fallbackArangoHost);
			if (ah != null) {
				addFallbackArangoHost(ah);
			}
		}

		final String timeoutProperty = prop.getProperty("timeout");
		if (timeoutProperty != null) {
			setTimeout(Integer.parseInt(timeoutProperty));
		}

		final String connectionTimeoutProperty = prop.getProperty("connectionTimeout");
		if (connectionTimeoutProperty != null) {
			setConnectionTimeout(Integer.parseInt(connectionTimeoutProperty));
		}

		final String proxyHostProperty = prop.getProperty("proxy.host");
		if (proxyHostProperty != null) {
			setProxyHost(proxyHostProperty);
		}

		final String proxyPortProperty = prop.getProperty("proxy.port");
		if (proxyPortProperty != null) {
			setProxyPort(Integer.parseInt(proxyPortProperty));
		}

		final String maxPerConnectionProperty = prop.getProperty("maxPerConnection");
		if (maxPerConnectionProperty != null) {
			setMaxPerConnection(Integer.parseInt(maxPerConnectionProperty));
		}

		final String maxTotalConnectionProperty = prop.getProperty("maxTotalConnection");
		if (maxTotalConnectionProperty != null) {
			setMaxTotalConnection(Integer.parseInt(maxTotalConnectionProperty));
		}

		final String retryCountProperty = prop.getProperty("retryCount");
		if (retryCountProperty != null) {
			setRetryCount(Integer.parseInt(retryCountProperty));
		}

		final String connnectRetryCount = prop.getProperty("connnectRetryCount");
		if (connnectRetryCount != null) {
			setConnectRetryCount(Integer.parseInt(connnectRetryCount));
		}

		final String connectRetryWaitProperty = prop.getProperty("connectRetryWait");
		if (connectRetryWaitProperty != null) {
			setConnectRetryWait(Integer.parseInt(connectRetryWaitProperty));
		}

		final String userProperty = prop.getProperty("user");
		if (userProperty != null) {
			setUser(userProperty);
		}

		final String passwordProperty = prop.getProperty("password");
		if (passwordProperty != null) {
			setPassword(passwordProperty);
		}

		final String defaultDatabaseProperty = prop.getProperty("defaultDatabase");
		if (defaultDatabaseProperty != null) {
			setDefaultDatabase(defaultDatabaseProperty);
		}

		final String enableCURLLoggerProperty = prop.getProperty("enableCURLLogger");
		if (enableCURLLoggerProperty != null) {
			setEnableCURLLogger(Boolean.parseBoolean(enableCURLLoggerProperty));
		}

		final String validateAfterInactivityProperty = prop.getProperty("validateAfterInactivity");
		if (validateAfterInactivityProperty != null) {
			setValidateAfterInactivity(Integer.parseInt(validateAfterInactivityProperty));
		}

		final String batchSizeProperty = prop.getProperty("batchSize");
		if (batchSizeProperty != null) {
			setBatchSize(Integer.parseInt(batchSizeProperty));
		}

		final String useSslProperty = prop.getProperty("useSsl");
		if (useSslProperty != null) {
			setUseSsl(Boolean.parseBoolean(useSslProperty));
		}

		final String sslTrustStoreProperty = prop.getProperty("sslTrustStore");
		if (sslTrustStoreProperty != null) {
			setSslTrustStore(sslTrustStoreProperty);
		}
	}

	private ArangoHost parseArangoHost(final String str) {
		if (str == null) {
			return null;
		}

		final String[] split = str.split(":", 2);
		if (split.length != 2) {
			return null;
		}

		return new ArangoHost(split[0], Integer.parseInt(split[1]));
	}

	public void init() {
		this.httpManager = new BatchHttpManager(this);
		this.httpManager.init();
	}

	public void shutdown() {
		if (httpManager != null) {
			httpManager.destroy();
			httpManager = null;
		}
	}

	public String getBaseUrl() {
		final ArangoHost currentHost = getCurrentHost();

		return (useSsl ? "https://" : "http://") + currentHost.getHost() + ":" + currentHost.getPort();
	}

	public String getEndpoint() {
		final ArangoHost currentHost = getCurrentHost();

		return (useSsl ? "ssl://" : "tcp://") + currentHost.getHost() + ":" + currentHost.getPort();
	}

	private ArangoHost getCurrentHost() {
		return arangoHosts.get(currentArangoHost);
	}

	public boolean hasFallbackHost() {
		return arangoHosts.size() > 1;
	}

	public ArangoHost changeCurrentHost() {
		currentArangoHost++;

		if (currentArangoHost >= arangoHosts.size()) {
			currentArangoHost = 0;
		}

		return getCurrentHost();
	}

	public static String getDefaultHost() {
		return DEFAULT_HOST;
	}

	public static int getDefaultMaxPerConnection() {
		return DEFAULT_MAX_PER_CONNECTION;
	}

	public static int getDefaultMaxConnection() {
		return DEFAULT_MAX_CONNECTION;
	}

	/**
	 * Get the default database host name and port
	 * 
	 * @return the host name and port
	 */
	public ArangoHost getArangoHost() {
		return arangoHosts.get(0);
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getMaxTotalConnection() {
		return maxTotalConnection;
	}

	public int getMaxPerConnection() {
		return maxPerConnection;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	/**
	 * Set the host name and port of the database
	 * 
	 * @param arangoHost
	 *            the host name and port
	 */
	public void setArangoHost(final ArangoHost arangoHost) {
		final ArangoHost host = arangoHosts.get(0);
		host.setHost(arangoHost.getHost());
		host.setPort(arangoHost.getPort());
	}

	/**
	 * Set the host name and port of the fallback database
	 * 
	 * @param arangoHost
	 *            the host name and port
	 */
	public void addFallbackArangoHost(final ArangoHost arangoHost) {
		arangoHosts.add(arangoHost);
	}

	public void setConnectionTimeout(final int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public void setMaxTotalConnection(final int maxTotalConnection) {
		this.maxTotalConnection = maxTotalConnection;
	}

	public void setMaxPerConnection(final int maxPerConnection) {
		this.maxPerConnection = maxPerConnection;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyPort(final int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(final int retryCount) {
		this.retryCount = retryCount;
	}

	public BatchHttpManager getHttpManager() {
		return httpManager;
	}

	public void setHttpManager(final BatchHttpManager httpManager) {
		this.httpManager = httpManager;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getDefaultDatabase() {
		return defaultDatabase;
	}

	/**
	 * Set the default database for the driver TODO: _system has to be a valid
	 * parameter
	 * 
	 * @param defaultDatabase
	 */
	public void setDefaultDatabase(final String defaultDatabase) {
		this.defaultDatabase = defaultDatabase;
	}

	public boolean isEnableCURLLogger() {
		return enableCURLLogger;
	}

	public void setEnableCURLLogger(final boolean enableCURLLogger) {
		this.enableCURLLogger = enableCURLLogger;
	}

	public int getValidateAfterInactivity() {
		return validateAfterInactivity;
	}

	public void setValidateAfterInactivity(final int validateAfterInactivity) {
		this.validateAfterInactivity = validateAfterInactivity;
	}

	public int getConnectRetryCount() {
		return connectRetryCount;
	}

	/**
	 * Set number of connect retries (0 means infinite)
	 * 
	 * @param connectRetryCount
	 *            number of connect retries
	 */
	public void setConnectRetryCount(final int connectRetryCount) {
		this.connectRetryCount = connectRetryCount;
	}

	public int getConnectRetryWait() {
		return connectRetryWait;
	}

	/**
	 * Set wait time for a connect retry
	 * 
	 * @param connectRetryWait
	 *            milliseconds to wait
	 */
	public void setConnectRetryWait(final int connectRetryWait) {
		this.connectRetryWait = connectRetryWait;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean getUseSsl() {
		return useSsl;
	}

	/**
	 * Configure the client to use HTTPS or HTTP
	 * 
	 * @param useSsl
	 *            set true to use HTTPS (default false)
	 */
	public void setUseSsl(final boolean useSsl) {
		this.useSsl = useSsl;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	/**
	 * Set SSL contest for HTTPS connections.
	 * 
	 * (do not use setSslTrustStore() together with setSslContext())
	 * 
	 * @param sslContext
	 */
	public void setSslContext(final SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public String getSslTrustStore() {
		return sslTrustStore;
	}

	/**
	 * Set file name of trust store
	 * 
	 * (do not use setSslTrustStore() together with setSslContext())
	 * 
	 * @param sslTrustStore
	 */
	public void setSslTrustStore(final String sslTrustStore) {
		this.sslTrustStore = sslTrustStore;
	}

}

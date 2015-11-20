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
	private static final int DEFAULT_MAX_PER_CONNECTION = 20; // 2;
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

	private boolean staleConnectionCheck = false;

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

	public ArangoConfigure(String propertyPath) {
		init(propertyPath);
	}

	private void init(String propertyPath) {
		arangoHosts = new ArrayList<ArangoHost>();
		ArangoHost defaultHost = new ArangoHost(DEFAULT_HOST, DEFAULT_PORT);
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
	public void loadProperties(String propertyPath) {
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream(propertyPath);
			if (in != null) {

				logger.debug("load property: file={}", propertyPath);

				Properties prop = new Properties();
				prop.load(in);

				//
				String port = prop.getProperty("port");
				if (port != null) {
					arangoHosts.get(0).setPort(Integer.parseInt(port));
				}

				String host = prop.getProperty("host");
				if (host != null) {
					arangoHosts.get(0).setHost(host);
				}

				String arangoHost = prop.getProperty("arangoHost");
				if (arangoHost != null) {
					ArangoHost ah = parseArangoHost(arangoHost);
					if (ah != null) {
						arangoHosts.get(0).setHost(ah.getHost());
						arangoHosts.get(0).setPort(ah.getPort());
					}
				}

				String fallbackArangoHost = prop.getProperty("fallbackArangoHost");
				if (fallbackArangoHost != null) {
					ArangoHost ah = parseArangoHost(fallbackArangoHost);
					if (ah != null) {
						addFallbackArangoHost(ah);
					}
				}

				String timeout = prop.getProperty("timeout");
				if (timeout != null) {
					setTimeout(Integer.parseInt(timeout));
				}

				String connectionTimeout = prop.getProperty("connectionTimeout");
				if (connectionTimeout != null) {
					setConnectionTimeout(Integer.parseInt(connectionTimeout));
				}

				String proxyHost = prop.getProperty("proxy.host");
				if (proxyHost != null) {
					setProxyHost(proxyHost);
				}

				String proxyPort = prop.getProperty("proxy.port");
				if (proxyPort != null) {
					setProxyPort(Integer.parseInt(proxyPort));
				}

				String maxPerConnection = prop.getProperty("maxPerConnection");
				if (maxPerConnection != null) {
					setMaxPerConnection(Integer.parseInt(maxPerConnection));
				}

				String maxTotalConnection = prop.getProperty("maxTotalConnection");
				if (maxTotalConnection != null) {
					setMaxTotalConnection(Integer.parseInt(maxTotalConnection));
				}

				String retryCount = prop.getProperty("retryCount");
				if (retryCount != null) {
					setRetryCount(Integer.parseInt(retryCount));
				}

				String connnectRetryCount = prop.getProperty("connnectRetryCount");
				if (connnectRetryCount != null) {
					setConnectRetryCount(Integer.parseInt(connnectRetryCount));
				}

				String connectRetryWait = prop.getProperty("connectRetryWait");
				if (connectRetryWait != null) {
					setConnectRetryWait(Integer.parseInt(connectRetryWait));
				}

				String user = prop.getProperty("user");
				if (user != null) {
					setUser(user);
				}

				String password = prop.getProperty("password");
				if (password != null) {
					setPassword(password);
				}

				String defaultDatabase = prop.getProperty("defaultDatabase");
				if (defaultDatabase != null) {
					setDefaultDatabase(defaultDatabase);
				}

				String enableCURLLogger = prop.getProperty("enableCURLLogger");
				if (enableCURLLogger != null) {
					setEnableCURLLogger(Boolean.parseBoolean(enableCURLLogger));
				}

				String staleConnectionCheck = prop.getProperty("staleConnectionCheck");
				if (staleConnectionCheck != null) {
					setStaleConnectionCheck(Boolean.parseBoolean(staleConnectionCheck));
				}

				String batchSize = prop.getProperty("batchSize");
				if (batchSize != null) {
					setBatchSize(Integer.parseInt(batchSize));
				}

				String useSsl = prop.getProperty("useSsl");
				if (useSsl != null) {
					setUseSsl(Boolean.parseBoolean(useSsl));
				}

				String sslTrustStore = prop.getProperty("sslTrustStore");
				if (sslTrustStore != null) {
					setSslTrustStore(sslTrustStore);
				}

			}
		} catch (IOException e) {
			logger.warn("load property error", e);
		} finally {
			if (in != null) {
				IOUtils.close(in);
			}
		}
	}

	private ArangoHost parseArangoHost(String str) {
		if (str == null) {
			return null;
		}

		String[] split = str.split(":", 2);
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
		ArangoHost currentHost = getCurrentHost();

		return (useSsl ? "https://" : "http://") + currentHost.getHost() + ":" + currentHost.getPort();
	}

	public String getEndpoint() {
		ArangoHost currentHost = getCurrentHost();

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
	 * Get the server port number
	 * 
	 * Don't use method. Please use {@link #getArangoHost() getArangoHost}
	 * 
	 * @deprecated
	 * @return the port number
	 */
	@Deprecated
	public int getClientPort() {
		return arangoHosts.get(0).getPort();
	}

	/**
	 * Get the server port number
	 * 
	 * Don't use method. Please use {@link #getArangoHost() getArangoHost}
	 * 
	 * @deprecated
	 * @return the port number
	 */
	@Deprecated
	public int getPort() {
		return arangoHosts.get(0).getPort();
	}

	/**
	 * Get the database host name
	 * 
	 * Don't use method. Please use {@link #getArangoHost() getArangoHost}
	 * 
	 * @deprecated
	 * @return the host name
	 */
	@Deprecated
	public String getHost() {
		return arangoHosts.get(0).getHost();
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
	 * Set the port number of the database
	 * 
	 * Don't use this method. Please use {@link #setArangoHost(ArangoHost)
	 * setArangoHost}
	 * 
	 * @param clientPort
	 *            the port number
	 * @deprecated
	 */
	@Deprecated
	public void setClinetPort(int clientPort) {
		arangoHosts.get(0).setPort(clientPort);
	}

	/**
	 * Set the port number of the database
	 * 
	 * Don't use this method. Please use {@link #setArangoHost(ArangoHost)
	 * setArangoHost}
	 * 
	 * @deprecated
	 * @param port
	 *            the port number
	 */
	@Deprecated
	public void setPort(int port) {
		arangoHosts.get(0).setPort(port);
	}

	/**
	 * Set the host name of the database
	 * 
	 * Don't use this method. Please use {@link #setArangoHost(ArangoHost)
	 * setArangoHost}
	 * 
	 * @deprecated
	 * @param host
	 *            the host name
	 */
	@Deprecated
	public void setHost(String host) {
		arangoHosts.get(0).setHost(host);
	}

	/**
	 * Set the host name and port of the database
	 * 
	 * @param arangoHost
	 *            the host name and port
	 */
	public void setArangoHost(ArangoHost arangoHost) {
		ArangoHost host = arangoHosts.get(0);
		host.setHost(arangoHost.getHost());
		host.setPort(arangoHost.getPort());
	}

	/**
	 * Set the host name and port of the fallback database
	 * 
	 * @param arangoHost
	 *            the host name and port
	 */
	public void addFallbackArangoHost(ArangoHost arangoHost) {
		arangoHosts.add(arangoHost);
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setMaxTotalConnection(int maxTotalConnection) {
		this.maxTotalConnection = maxTotalConnection;
	}

	public void setMaxPerConnection(int maxPerConnection) {
		this.maxPerConnection = maxPerConnection;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public BatchHttpManager getHttpManager() {
		return httpManager;
	}

	public void setHttpManager(BatchHttpManager httpManager) {
		this.httpManager = httpManager;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
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
	public void setDefaultDatabase(String defaultDatabase) {
		this.defaultDatabase = defaultDatabase;
	}

	public boolean isEnableCURLLogger() {
		return enableCURLLogger;
	}

	public void setEnableCURLLogger(boolean enableCURLLogger) {
		this.enableCURLLogger = enableCURLLogger;
	}

	public boolean isStaleConnectionCheck() {
		return staleConnectionCheck;
	}

	public void setStaleConnectionCheck(boolean staleConnectionCheck) {
		this.staleConnectionCheck = staleConnectionCheck;
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
	public void setConnectRetryCount(int connectRetryCount) {
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
	public void setConnectRetryWait(int connectRetryWait) {
		this.connectRetryWait = connectRetryWait;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
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
	public void setUseSsl(boolean useSsl) {
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
	public void setSslContext(SSLContext sslContext) {
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
	public void setSslTrustStore(String sslTrustStore) {
		this.sslTrustStore = sslTrustStore;
	}

}

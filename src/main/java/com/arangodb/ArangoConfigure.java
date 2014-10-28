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
import java.util.Properties;

import com.arangodb.http.BatchHttpManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.http.HttpManager;
import com.arangodb.util.IOUtils;

/**
 * Configure of ArangoDB.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class ArangoConfigure {

	
	private static Logger logger = LoggerFactory.getLogger(ArangoConfigure.class);

	/** default host of ArangoDB */
	private static final String DEFAULT_HOST = "127.0.0.1";
	/** default port of ArangoDB */
	private static final int DEFAULT_PORT = 8529;

	/** default  */
	private static final int DEFAULT_MAX_PER_CONNECTION = 20; // 2;
	/** default maximum conections */
	private static final int DEFAULT_MAX_CONNECTION = 20;

	/** default property file */
	private static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";

	/** server port */
	int port;
	/** server host */
	String host;
	/** connection timeout(ms) */
	int connectionTimeout = -1;
	/** socket read timeout(ms) */
	int timeout = -1;

	/** max connection per configure */
	int maxTotalConnection;
	/** max connection per host */
	int maxPerConnection;

	/** Basic auth user */
	String user;
	/** Basic auth password */
	String password;

	/** proxy-host */
	String proxyHost;
	/** proxy-port */
	int proxyPort;

	/** http retry count */
	int retryCount = 3;

	/** Default Database */
	String defaultDatabase;

	boolean enableCURLLogger = false;

	boolean staleConnectionCheck = false;

  BatchHttpManager httpManager;

	public ArangoConfigure() {
		init(DEFAULT_PROPERTY_FILE);
	}

	public ArangoConfigure(String propertyPath) {
		init(propertyPath);
	}

	private void init(String propertyPath) {
		this.host = DEFAULT_HOST;
		this.port = DEFAULT_PORT;
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

				logger.info("load property: file={}", propertyPath);

				Properties prop = new Properties();
				prop.load(in);

				//
				String port = prop.getProperty("port");
				if (port != null) {
					setPort(Integer.parseInt(port));
				}

				String host = prop.getProperty("host");
				if (host != null) {
					setHost(host);
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

			}
		} catch (IOException e) {
			logger.warn("load property error", e);
		} finally {
			if (in != null) {
				IOUtils.close(in);
			}
		}
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

	// TODO changes this for multiple host support
	public String getBaseUrl() {
		return "http://" + this.host + ":" + this.port;
	}

	// TODO changes this for multiple host support
	public String getEndpoint() {
		return "tcp://" + this.host + ":" + this.port;
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
	 * Don't use method. Please use {@link getPort}
	 * 
	 * @deprecated
	 * @see getPort
	 */
	@Deprecated
	public int getClientPort() {
		return port;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
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
	 * Don't use this method. Please use {@link setPort}
	 * 
	 * @deprecated
	 * @see setPort
	 */
	@Deprecated
	public void setClinetPort(int clinetPort) {
		this.port = clinetPort;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
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
	 * Set the default database for the driver
	 * TODO: _system has to be a valid parameter
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

}

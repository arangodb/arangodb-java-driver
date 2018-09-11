/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.net.Connection;
import com.arangodb.internal.net.ConnectionFactory;
import com.arangodb.internal.net.DirtyReadHostHandler;
import com.arangodb.internal.net.ExtendedHostResolver;
import com.arangodb.internal.net.FallbackHostHandler;
import com.arangodb.internal.net.Host;
import com.arangodb.internal.net.HostDescription;
import com.arangodb.internal.net.HostHandler;
import com.arangodb.internal.net.HostResolver;
import com.arangodb.internal.net.RandomHostHandler;
import com.arangodb.internal.net.RoundRobinHostHandler;
import com.arangodb.internal.net.SimpleHostResolver;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.internal.velocypack.VPackDriverModule;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;

/**
 * @author Mark Vollmary
 *
 */
public abstract class InternalArangoDBBuilder {

	private static final String PROPERTY_KEY_HOSTS = "arangodb.hosts";
	private static final String PROPERTY_KEY_HOST = "arangodb.host";
	private static final String PROPERTY_KEY_PORT = "arangodb.port";
	private static final String PROPERTY_KEY_TIMEOUT = "arangodb.timeout";
	private static final String PROPERTY_KEY_USER = "arangodb.user";
	private static final String PROPERTY_KEY_PASSWORD = "arangodb.password";
	private static final String PROPERTY_KEY_USE_SSL = "arangodb.usessl";
	private static final String PROPERTY_KEY_V_STREAM_CHUNK_CONTENT_SIZE = "arangodb.chunksize";
	private static final String PROPERTY_KEY_MAX_CONNECTIONS = "arangodb.connections.max";
	private static final String PROPERTY_KEY_CONNECTION_TTL = "arangodb.connections.ttl";
	private static final String PROPERTY_KEY_ACQUIRE_HOST_LIST = "arangodb.acquireHostList";
	private static final String PROPERTY_KEY_LOAD_BALANCING_STRATEGY = "arangodb.loadBalancingStrategy";
	private static final String DEFAULT_PROPERTY_FILE = "/arangodb.properties";

	protected final List<HostDescription> hosts;
	protected HostDescription host;
	protected Integer timeout;
	protected String user;
	protected String password;
	protected Boolean useSsl;
	protected SSLContext sslContext;
	protected Integer chunksize;
	protected Integer maxConnections;
	protected Long connectionTtl;
	protected final VPack.Builder vpackBuilder;
	protected final VPackParser.Builder vpackParserBuilder;
	protected ArangoSerializer serializer;
	protected ArangoDeserializer deserializer;
	protected Boolean acquireHostList;
	protected LoadBalancingStrategy loadBalancingStrategy;
	protected ArangoSerialization customSerializer;

	public InternalArangoDBBuilder() {
		super();
		vpackBuilder = new VPack.Builder();
		vpackParserBuilder = new VPackParser.Builder();
		vpackBuilder.registerModule(new VPackDriverModule());
		vpackParserBuilder.registerModule(new VPackDriverModule());
		host = new HostDescription(ArangoDefaults.DEFAULT_HOST, ArangoDefaults.DEFAULT_PORT);
		hosts = new ArrayList<HostDescription>();
		user = ArangoDefaults.DEFAULT_USER;
		loadProperties(ArangoDB.class.getResourceAsStream(DEFAULT_PROPERTY_FILE));
	}

	public InternalArangoDBBuilder loadProperties(final InputStream in) throws ArangoDBException {
		if (in != null) {
			final Properties properties = new Properties();
			try {
				properties.load(in);
				loadProperties(properties);
			} catch (final IOException e) {
				throw new ArangoDBException(e);
			}
		}
		return this;
	}

	protected void loadProperties(final Properties properties) {
		loadHosts(properties, this.hosts);
		final String host = loadHost(properties, this.host.getHost());
		final int port = loadPort(properties, this.host.getPort());
		this.host = new HostDescription(host, port);
		timeout = loadTimeout(properties, timeout);
		user = loadUser(properties, user);
		password = loadPassword(properties, password);
		useSsl = loadUseSsl(properties, useSsl);
		chunksize = loadChunkSize(properties, chunksize);
		maxConnections = loadMaxConnections(properties, maxConnections);
		connectionTtl = loadConnectionTtl(properties, connectionTtl);
		acquireHostList = loadAcquireHostList(properties, acquireHostList);
		loadBalancingStrategy = loadLoadBalancingStrategy(properties, loadBalancingStrategy);
	}

	protected void setHost(final String host, final int port) {
		hosts.add(new HostDescription(host, port));
	}

	protected void setTimeout(final Integer timeout) {
		this.timeout = timeout;
	}

	protected void setUser(final String user) {
		this.user = user;
	}

	protected void setPassword(final String password) {
		this.password = password;
	}

	protected void setUseSsl(final Boolean useSsl) {
		this.useSsl = useSsl;
	}

	protected void setSslContext(final SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	protected void setChunksize(final Integer chunksize) {
		this.chunksize = chunksize;
	}

	protected void setMaxConnections(final Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	protected void setConnectionTtl(final Long connectionTtl) {
		this.connectionTtl = connectionTtl;
	}

	protected void setAcquireHostList(final Boolean acquireHostList) {
		this.acquireHostList = acquireHostList;
	}

	protected void setLoadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
		this.loadBalancingStrategy = loadBalancingStrategy;
	}

	protected void serializer(final ArangoSerializer serializer) {
		this.serializer = serializer;
	}

	protected void deserializer(final ArangoDeserializer deserializer) {
		this.deserializer = deserializer;
	}

	protected void setSerializer(final ArangoSerialization serializer) {
		this.customSerializer = serializer;
	}

	protected HostResolver createHostResolver(
		final Collection<Host> hosts,
		final int maxConnections,
		final ConnectionFactory connectionFactory) {
		return Boolean.TRUE == acquireHostList
				? new ExtendedHostResolver(new ArrayList<Host>(hosts), maxConnections, connectionFactory)
				: new SimpleHostResolver(new ArrayList<Host>(hosts));
	}

	protected HostHandler createHostHandler(final HostResolver hostResolver) {
		final HostHandler hostHandler;
		if (loadBalancingStrategy != null) {
			switch (loadBalancingStrategy) {
			case ONE_RANDOM:
				hostHandler = new RandomHostHandler(hostResolver, new FallbackHostHandler(hostResolver));
				break;
			case ROUND_ROBIN:
				hostHandler = new RoundRobinHostHandler(hostResolver);
				break;
			case NONE:
			default:
				hostHandler = new FallbackHostHandler(hostResolver);
				break;
			}
		} else {
			hostHandler = new FallbackHostHandler(hostResolver);
		}
		return new DirtyReadHostHandler(hostHandler, new RoundRobinHostHandler(hostResolver));
	}

	private static void loadHosts(final Properties properties, final Collection<HostDescription> hosts) {
		final String hostsProp = properties.getProperty(PROPERTY_KEY_HOSTS);
		if (hostsProp != null) {
			final String[] hostsSplit = hostsProp.split(",");
			for (final String host : hostsSplit) {
				final String[] split = host.split(":");
				if (split.length != 2 || !split[1].matches("[0-9]+")) {
					throw new ArangoDBException(String.format(
						"Could not load property-value arangodb.hosts=%s. Expected format ip:port,ip:port,...",
						hostsProp));
				} else {
					hosts.add(new HostDescription(split[0], Integer.valueOf(split[1])));
				}
			}
		}
	}

	private static String loadHost(final Properties properties, final String currentValue) {
		final String host = getProperty(properties, PROPERTY_KEY_HOST, currentValue, ArangoDefaults.DEFAULT_HOST);
		if (host.contains(":")) {
			throw new ArangoDBException(String.format(
				"Could not load property-value arangodb.host=%s. Expect only ip. Do you mean arangodb.hosts=ip:port ?",
				host));
		}
		return host;
	}

	private static Integer loadPort(final Properties properties, final int currentValue) {
		return Integer.parseInt(getProperty(properties, PROPERTY_KEY_PORT, currentValue, ArangoDefaults.DEFAULT_PORT));
	}

	private static Integer loadTimeout(final Properties properties, final Integer currentValue) {
		return Integer
				.parseInt(getProperty(properties, PROPERTY_KEY_TIMEOUT, currentValue, ArangoDefaults.DEFAULT_TIMEOUT));
	}

	private static String loadUser(final Properties properties, final String currentValue) {
		return getProperty(properties, PROPERTY_KEY_USER, currentValue, ArangoDefaults.DEFAULT_USER);
	}

	private static String loadPassword(final Properties properties, final String currentValue) {
		return getProperty(properties, PROPERTY_KEY_PASSWORD, currentValue, null);
	}

	private static Boolean loadUseSsl(final Properties properties, final Boolean currentValue) {
		return Boolean.parseBoolean(
			getProperty(properties, PROPERTY_KEY_USE_SSL, currentValue, ArangoDefaults.DEFAULT_USE_SSL));
	}

	private static Integer loadChunkSize(final Properties properties, final Integer currentValue) {
		return Integer.parseInt(getProperty(properties, PROPERTY_KEY_V_STREAM_CHUNK_CONTENT_SIZE, currentValue,
			ArangoDefaults.CHUNK_DEFAULT_CONTENT_SIZE));
	}

	private static Integer loadMaxConnections(final Properties properties, final Integer currentValue) {
		return Integer.parseInt(getProperty(properties, PROPERTY_KEY_MAX_CONNECTIONS, currentValue,
			ArangoDefaults.MAX_CONNECTIONS_VST_DEFAULT));
	}

	private static Long loadConnectionTtl(final Properties properties, final Long currentValue) {
		final String ttl = getProperty(properties, PROPERTY_KEY_CONNECTION_TTL, currentValue,
			ArangoDefaults.CONNECTION_TTL_VST_DEFAULT);
		return ttl != null ? Long.parseLong(ttl) : null;
	}

	private static Boolean loadAcquireHostList(final Properties properties, final Boolean currentValue) {
		return Boolean.parseBoolean(getProperty(properties, PROPERTY_KEY_ACQUIRE_HOST_LIST, currentValue,
			ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST));
	}

	private static LoadBalancingStrategy loadLoadBalancingStrategy(
		final Properties properties,
		final LoadBalancingStrategy currentValue) {
		return LoadBalancingStrategy.valueOf(getProperty(properties, PROPERTY_KEY_LOAD_BALANCING_STRATEGY, currentValue,
			ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY).toUpperCase());
	}

	protected static <T> String getProperty(
		final Properties properties,
		final String key,
		final T currentValue,
		final T defaultValue) {
		return properties.getProperty(key,
			currentValue != null ? currentValue.toString() : defaultValue != null ? defaultValue.toString() : null);
	}

	protected <C extends Connection> Collection<Host> createHostList(
		final int maxConnections,
		final ConnectionFactory connectionFactory) {
		final Collection<Host> hostList = new ArrayList<Host>();
		for (final HostDescription host : hosts) {
			hostList.add(HostUtils.createHost(host, maxConnections, connectionFactory));
		}
		return hostList;
	}
}

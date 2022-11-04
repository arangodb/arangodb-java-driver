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

import com.arangodb.ArangoDBException;
import com.arangodb.config.ConfigPropertiesProvider;
import com.arangodb.config.ConfigPropertyKey;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.net.*;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.serde.ArangoSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoDBBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(InternalArangoDBBuilder.class);

    protected final List<HostDescription> hosts = new ArrayList<>();
    protected Integer timeout = ArangoDefaults.DEFAULT_TIMEOUT;
    protected String user = ArangoDefaults.DEFAULT_USER;
    protected String password = null;
    protected String jwt = null;
    protected Boolean useSsl = ArangoDefaults.DEFAULT_USE_SSL;
    protected SSLContext sslContext = null;
    protected Boolean verifyHost = ArangoDefaults.DEFAULT_VERIFY_HOST;
    protected Integer chunkSize = ArangoDefaults.CHUNK_DEFAULT_CONTENT_SIZE;
    protected Integer maxConnections = null;
    protected Long connectionTtl = ArangoDefaults.CONNECTION_TTL_VST_DEFAULT;
    protected Integer keepAliveInterval = null;
    protected Boolean acquireHostList = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST;
    protected Integer acquireHostListInterval = ArangoDefaults.DEFAULT_ACQUIRE_HOST_LIST_INTERVAL;
    protected LoadBalancingStrategy loadBalancingStrategy = ArangoDefaults.DEFAULT_LOAD_BALANCING_STRATEGY;
    protected ArangoSerde customSerializer;
    protected Integer responseQueueTimeSamples = ArangoDefaults.DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES;

    private static void loadHosts(final ConfigPropertiesProvider properties, final Collection<HostDescription> hosts) {
        final String hostsProp = properties.getProperty(ConfigPropertyKey.HOSTS);
        if (hostsProp != null) {
            final String[] hostsSplit = hostsProp.split(",");
            for (final String host : hostsSplit) {
                final String[] split = host.split(":");
                if (split.length != 2 || !split[1].matches("[0-9]+")) {
                    throw new ArangoDBException(String.format(
                            "Could not load property-value arangodb.hosts=%s. Expected format ip:port,ip:port,...",
                            hostsProp));
                } else {
                    hosts.add(new HostDescription(split[0], Integer.parseInt(split[1])));
                }
            }
        }
    }

    private static Integer loadTimeout(final ConfigPropertiesProvider properties, final Integer currentValue) {
        return Integer.parseInt(getProperty(properties, ConfigPropertyKey.TIMEOUT, currentValue));
    }

    private static String loadUser(final ConfigPropertiesProvider properties, final String currentValue) {
        return getProperty(properties, ConfigPropertyKey.USER, currentValue);
    }

    private static String loadPassword(final ConfigPropertiesProvider properties, final String currentValue) {
        return getProperty(properties, ConfigPropertyKey.PASSWORD, currentValue);
    }

    private static String loadJwt(final ConfigPropertiesProvider properties, final String currentValue) {
        return getProperty(properties, ConfigPropertyKey.JWT, currentValue);
    }

    private static Boolean loadUseSsl(final ConfigPropertiesProvider properties, final Boolean currentValue) {
        return Boolean.parseBoolean(getProperty(properties, ConfigPropertyKey.USE_SSL, currentValue));
    }

    private static Boolean loadVerifyHost(final ConfigPropertiesProvider properties, final Boolean currentValue) {
        return Boolean.parseBoolean(getProperty(properties, ConfigPropertyKey.VERIFY_HOST, currentValue));
    }

    private static Integer loadChunkSize(final ConfigPropertiesProvider properties, final Integer currentValue) {
        return Integer.parseInt(getProperty(properties, ConfigPropertyKey.VST_CHUNK_SIZE, currentValue));
    }

    private static Integer loadMaxConnections(final ConfigPropertiesProvider properties, final Integer currentValue) {
        String value = getProperty(properties, ConfigPropertyKey.MAX_CONNECTIONS, currentValue);
        return value != null ? Integer.parseInt(value) : null;
    }

    private static Long loadConnectionTtl(final ConfigPropertiesProvider properties, final Long currentValue) {
        final String ttl = getProperty(properties, ConfigPropertyKey.CONNECTION_TTL, currentValue);
        return ttl != null ? Long.parseLong(ttl) : null;
    }

    private static Integer loadKeepAliveInterval(final ConfigPropertiesProvider properties, final Integer currentValue) {
        final String keepAliveInterval = getProperty(properties, ConfigPropertyKey.KEEP_ALIVE_INTERVAL, currentValue);
        return keepAliveInterval != null ? Integer.parseInt(keepAliveInterval) : null;
    }

    private static Boolean loadAcquireHostList(final ConfigPropertiesProvider properties, final Boolean currentValue) {
        return Boolean.parseBoolean(getProperty(properties, ConfigPropertyKey.ACQUIRE_HOST_LIST, currentValue));
    }

    private static int loadAcquireHostListInterval(final ConfigPropertiesProvider properties, final Integer currentValue) {
        return Integer.parseInt(getProperty(properties, ConfigPropertyKey.ACQUIRE_HOST_LIST_INTERVAL, currentValue));
    }

    private static int loadResponseQueueTimeSamples(final ConfigPropertiesProvider properties, final Integer currentValue) {
        return Integer.parseInt(getProperty(properties, ConfigPropertyKey.RESPONSE_QUEUE_TIME_SAMPLES, currentValue));
    }

    private static LoadBalancingStrategy loadLoadBalancingStrategy(
            final ConfigPropertiesProvider properties,
            final LoadBalancingStrategy currentValue) {
        return LoadBalancingStrategy.valueOf(getProperty(properties, ConfigPropertyKey.LOAD_BALANCING_STRATEGY, currentValue).toUpperCase(Locale.ROOT));
    }

    protected static String getProperty(ConfigPropertiesProvider props, ConfigPropertyKey key, Object currentValue) {
        String defaultValue = currentValue == null ? null : currentValue.toString();
        return props.getProperty(key, defaultValue);
    }

    protected void doLoadProperties(final ConfigPropertiesProvider properties) {
        loadHosts(properties, hosts);
        timeout = loadTimeout(properties, timeout);
        user = loadUser(properties, user);
        password = loadPassword(properties, password);
        jwt = loadJwt(properties, jwt);
        useSsl = loadUseSsl(properties, useSsl);
        verifyHost = loadVerifyHost(properties, verifyHost);
        chunkSize = loadChunkSize(properties, chunkSize);
        maxConnections = loadMaxConnections(properties, maxConnections);
        connectionTtl = loadConnectionTtl(properties, connectionTtl);
        keepAliveInterval = loadKeepAliveInterval(properties, keepAliveInterval);
        acquireHostList = loadAcquireHostList(properties, acquireHostList);
        acquireHostListInterval = loadAcquireHostListInterval(properties, acquireHostListInterval);
        loadBalancingStrategy = loadLoadBalancingStrategy(properties, loadBalancingStrategy);
        responseQueueTimeSamples = loadResponseQueueTimeSamples(properties, responseQueueTimeSamples);
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

    protected void setJwt(final String jwt) {
        this.jwt = jwt;
    }

    protected void setUseSsl(final Boolean useSsl) {
        this.useSsl = useSsl;
    }

    protected void setSslContext(final SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    protected void setVerifyHost(final Boolean verifyHost) {
        this.verifyHost = verifyHost;
    }

    protected void setChunkSize(final Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    protected void setMaxConnections(final Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    protected void setConnectionTtl(final Long connectionTtl) {
        this.connectionTtl = connectionTtl;
    }

    protected void setKeepAliveInterval(final Integer keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    protected void setAcquireHostList(final Boolean acquireHostList) {
        this.acquireHostList = acquireHostList;
    }

    protected void setAcquireHostListInterval(final Integer acquireHostListInterval) {
        this.acquireHostListInterval = acquireHostListInterval;
    }

    protected void setLoadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = loadBalancingStrategy;
    }

    protected void setResponseQueueTimeSamples(final Integer responseQueueTimeSamples) {
        this.responseQueueTimeSamples = responseQueueTimeSamples;
    }

    protected void setSerializer(final ArangoSerde serializer) {
        this.customSerializer = serializer;
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

        LOG.debug("HostHandler is {}", hostHandler.getClass().getSimpleName());

        return new DirtyReadHostHandler(hostHandler, new RoundRobinHostHandler(hostResolver));
    }

    protected HostResolver createHostResolver(final Collection<Host> hosts, final int maxConnections,
                                              final ConnectionFactory connectionFactory) {

        if (acquireHostList != null && acquireHostList) {
            LOG.debug("acquireHostList -> Use ExtendedHostResolver");
            return new ExtendedHostResolver(new ArrayList<>(hosts), maxConnections, connectionFactory,
                    acquireHostListInterval);
        } else {
            LOG.debug("Use SimpleHostResolver");
            return new SimpleHostResolver(new ArrayList<>(hosts));
        }

    }

    protected <C extends Connection> Collection<Host> createHostList(
            final int maxConnections,
            final ConnectionFactory connectionFactory) {
        final Collection<Host> hostList = new ArrayList<>();
        for (final HostDescription host : hosts) {
            hostList.add(HostUtils.createHost(host, maxConnections, connectionFactory));
        }
        return hostList;
    }
}

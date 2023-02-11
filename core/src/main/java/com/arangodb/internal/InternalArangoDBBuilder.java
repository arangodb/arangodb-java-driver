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

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.Request;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.*;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.serde.ArangoSerde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;


/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoDBBuilder<T extends InternalArangoDBBuilder<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(InternalArangoDBBuilder.class);
    protected final ArangoConfig config = new ArangoConfig();

    public T loadProperties(final ArangoConfigProperties properties) {
        config.loadProperties(properties);
        return (T) this;
    }

    public T useProtocol(final Protocol protocol) {
        config.setProtocol(protocol);
        return (T) this;
    }

    /**
     * Adds a host to connect to. Multiple hosts can be added to provide fallbacks.
     *
     * @param host address of the host
     * @param port port of the host
     * @return {@link ArangoDB.Builder}
     */
    public T host(final String host, final int port) {
        config.addHost(new HostDescription(host, port));
        return (T) this;
    }

    /**
     * Sets the connection and request timeout in milliseconds.
     *
     * @param timeout timeout in milliseconds
     * @return {@link ArangoDB.Builder}
     */
    public T timeout(final Integer timeout) {
        config.setTimeout(timeout);
        return (T) this;
    }

    /**
     * Sets the username to use for authentication.
     *
     * @param user the user in the database (default: {@code root})
     * @return {@link ArangoDB.Builder}
     */
    public T user(final String user) {
        config.setUser(user);
        return (T) this;
    }

    /**
     * Sets the password for the user for authentication.
     *
     * @param password the password of the user in the database (default: {@code null})
     * @return {@link ArangoDB.Builder}
     */
    public T password(final String password) {
        config.setPassword(password);
        return (T) this;
    }

    /**
     * Sets the JWT for the user authentication.
     *
     * @param jwt token to use (default: {@code null})
     * @return {@link ArangoDB.Builder}
     */
    public T jwt(final String jwt) {
        config.setJwt(jwt);
        return (T) this;
    }

    /**
     * If set to {@code true} SSL will be used when connecting to an ArangoDB server.
     *
     * @param useSsl whether or not use SSL (default: {@code false})
     * @return {@link ArangoDB.Builder}
     */
    public T useSsl(final Boolean useSsl) {
        config.setUseSsl(useSsl);
        return (T) this;
    }

    /**
     * Sets the SSL context to be used when {@code true} is passed through {@link #useSsl(Boolean)}.
     *
     * @param sslContext SSL context to be used
     * @return {@link ArangoDB.Builder}
     */
    public T sslContext(final SSLContext sslContext) {
        config.setSslContext(sslContext);
        return (T) this;
    }

    /**
     * Set whether hostname verification is enabled
     *
     * @param verifyHost {@code true} if enabled
     * @return {@link ArangoDB.Builder}
     */
    public T verifyHost(final Boolean verifyHost) {
        config.setVerifyHost(verifyHost);
        return (T) this;
    }

    /**
     * Sets the chunk size when {@link Protocol#VST} is used.
     *
     * @param chunksize size of a chunk in bytes
     * @return {@link ArangoDB.Builder}
     */
    public T chunksize(final Integer chunksize) {
        config.setChunkSize(chunksize);
        return (T) this;
    }

    /**
     * Sets the maximum number of connections the built in connection pool will open per host.
     *
     * <p>
     * Defaults:
     * </p>
     *
     * <pre>
     * {@link Protocol#VST} == 1
     * {@link Protocol#HTTP_JSON} == 20
     * {@link Protocol#HTTP_VPACK} == 20
     * </pre>
     *
     * @param maxConnections max number of connections
     * @return {@link ArangoDB.Builder}
     */
    public T maxConnections(final Integer maxConnections) {
        config.setMaxConnections(maxConnections);
        return (T) this;
    }

    /**
     * Set the maximum time to life of a connection. After this time the connection will be closed automatically.
     *
     * @param connectionTtl the maximum time to life of a connection in milliseconds
     * @return {@link ArangoDB.Builder}
     */
    public T connectionTtl(final Long connectionTtl) {
        config.setConnectionTtl(connectionTtl);
        return (T) this;
    }

    /**
     * Set the keep-alive interval for VST connections. If set, every VST connection will perform a no-op request
     * every {@code keepAliveInterval} seconds, to avoid to be closed due to inactivity by the server (or by the
     * external environment, eg. firewall, intermediate routers, operating system).
     *
     * @param keepAliveInterval interval in seconds
     * @return {@link ArangoDB.Builder}
     */
    public T keepAliveInterval(final Integer keepAliveInterval) {
        config.setKeepAliveInterval(keepAliveInterval);
        return (T) this;
    }

    /**
     * Whether or not the driver should acquire a list of available coordinators in an ArangoDB cluster or a single
     * server with active failover. In case of Active-Failover deployment set to {@code true} to enable automatic
     * master discovery.
     *
     * <p>
     * The host list will be used for failover and load balancing.
     * </p>
     *
     * @param acquireHostList whether or not automatically acquire a list of available hosts (default: false)
     * @return {@link ArangoDB.Builder}
     */
    public T acquireHostList(final Boolean acquireHostList) {
        config.setAcquireHostList(acquireHostList);
        return (T) this;
    }

    /**
     * Setting the Interval for acquireHostList
     *
     * @param acquireHostListInterval Interval in milliseconds
     * @return {@link ArangoDB.Builder}
     */
    public T acquireHostListInterval(final Integer acquireHostListInterval) {
        config.setAcquireHostListInterval(acquireHostListInterval);
        return (T) this;
    }

    /**
     * Sets the load balancing strategy to be used in an ArangoDB cluster setup. In case of Active-Failover
     * deployment set to {@link LoadBalancingStrategy#NONE} or not set at all, since that would be the default.
     *
     * @param loadBalancingStrategy the load balancing strategy to be used (default:
     *                              {@link LoadBalancingStrategy#NONE}
     * @return {@link ArangoDB.Builder}
     */
    public T loadBalancingStrategy(final LoadBalancingStrategy loadBalancingStrategy) {
        config.setLoadBalancingStrategy(loadBalancingStrategy);
        return (T) this;
    }

    /**
     * Setting the amount of samples kept for queue time metrics
     *
     * @param responseQueueTimeSamples amount of samples to keep
     * @return {@link ArangoDB.Builder}
     */
    public T responseQueueTimeSamples(final Integer responseQueueTimeSamples) {
        config.setResponseQueueTimeSamples(responseQueueTimeSamples);
        return (T) this;
    }

    /**
     * Sets the serde for the user data.
     * This is used to serialize and deserialize all the data payload such as:
     * - documents, vertexes, edges
     * - AQL bind vars
     * - body payload of requests and responses in {@link ArangoDB#execute(Request, Class)}
     * <p>
     * However, note that the following types will always be serialized and deserialized using the internal serde:
     * - {@link com.fasterxml.jackson.databind.JsonNode}
     * - {@link com.arangodb.util.RawJson}
     * - {@link com.arangodb.util.RawBytes}
     * - {@link com.arangodb.entity.BaseDocument}
     * - {@link com.arangodb.entity.BaseEdgeDocument}
     *
     * @param serde custom serde for the user data
     * @return {@link ArangoDB.Builder}
     */
    public T serde(final ArangoSerde serde) {
        config.setUserDataSerde(serde);
        return (T) this;
    }

    protected ProtocolProvider protocolProvider(Protocol protocol) {
        ServiceLoader<ProtocolProvider> loader = ServiceLoader.load(ProtocolProvider.class);
        for (ProtocolProvider p : loader) {
            if (p.supportsProtocol(protocol)) {
                return p;
            }
        }
        throw new ArangoDBException("No ProtocolProvider found for protocol: " + protocol);
    }

    protected HostHandler createHostHandler(final HostResolver hostResolver) {

        final HostHandler hostHandler;

        LoadBalancingStrategy loadBalancingStrategy = config.getLoadBalancingStrategy();
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
        Boolean acquireHostList = config.getAcquireHostList();
        if (acquireHostList != null && acquireHostList) {
            LOG.debug("acquireHostList -> Use ExtendedHostResolver");
            return new ExtendedHostResolver(new ArrayList<>(hosts), maxConnections, connectionFactory,
                    config.getAcquireHostListInterval());
        } else {
            LOG.debug("Use SimpleHostResolver");
            return new SimpleHostResolver(new ArrayList<>(hosts));
        }

    }

    protected <C extends Connection> Collection<Host> createHostList(
            final int maxConnections,
            final ConnectionFactory connectionFactory) {
        final Collection<Host> hostList = new ArrayList<>();
        for (final HostDescription host : config.getHosts()) {
            hostList.add(HostUtils.createHost(host, maxConnections, connectionFactory));
        }
        return hostList;
    }
}

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
import com.arangodb.Protocol;
import com.arangodb.config.ArangoConfigProperties;
import com.arangodb.config.ArangoConfigPropertiesImpl;
import com.arangodb.entity.LoadBalancingStrategy;
import com.arangodb.internal.net.*;
import com.arangodb.internal.serde.InternalSerdeProvider;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.serde.ArangoSerde;
import com.arangodb.serde.ArangoSerdeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoDBBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(InternalArangoDBBuilder.class);

    protected final List<HostDescription> hosts = new ArrayList<>();
    protected Protocol protocol;
    protected Integer timeout;
    protected String user;
    protected String password;
    protected String jwt;
    protected Boolean useSsl;
    protected SSLContext sslContext;
    protected Boolean verifyHost;
    protected Integer chunkSize;
    protected Integer maxConnections;
    protected Long connectionTtl;
    protected Integer keepAliveInterval;
    protected Boolean acquireHostList;
    protected Integer acquireHostListInterval;
    protected LoadBalancingStrategy loadBalancingStrategy;
    protected ArangoSerde userDataSerde;
    protected Integer responseQueueTimeSamples;

    protected InternalArangoDBBuilder() {
        // load default properties
        doLoadProperties(new ArangoConfigPropertiesImpl());
    }

    protected static ArangoSerdeProvider serdeProvider() {
        ServiceLoader<ArangoSerdeProvider> loader = ServiceLoader.load(ArangoSerdeProvider.class);
        Iterator<ArangoSerdeProvider> it = loader.iterator();
        ArangoSerdeProvider serdeProvider;
        if (!it.hasNext()) {
            LOG.info("No ArangoSerdeProvider found, using InternalSerdeProvider.");
            serdeProvider = new InternalSerdeProvider();
        } else {
            serdeProvider = it.next();
            if (it.hasNext()) {
                throw new ArangoDBException("Found multiple serde providers! Please set explicitly the one to use.");
            }
        }
        return serdeProvider;
    }

    protected void doLoadProperties(final ArangoConfigProperties properties) {
        // FIXME: rm config.Host and use HostDescription
        hosts.addAll(properties.getHosts().stream()
                .map(it -> new HostDescription(it.getName(), it.getPort()))
                .collect(Collectors.toList()));
        protocol = properties.getProtocol();
        timeout = properties.getTimeout();
        user = properties.getUser();
        // FIXME: make password field Optional
        password = properties.getPassword().orElse(null);
        // FIXME: make jwt field Optional
        jwt = properties.getJwt().orElse(null);
        useSsl = properties.getUseSsl();
        verifyHost = properties.getVerifyHost();
        chunkSize = properties.getVstChunkSize();
        // FIXME: make maxConnections field Optional
        maxConnections = properties.getMaxConnections().orElse(null);
        // FIXME: make connectionTtl field Optional
        connectionTtl = properties.getConnectionTtl().orElse(null);
        // FIXME: make keepAliveInterval field Optional
        keepAliveInterval = properties.getKeepAliveInterval().orElse(null);
        acquireHostList = properties.getAcquireHostList();
        acquireHostListInterval = properties.getAcquireHostListInterval();
        loadBalancingStrategy = properties.getLoadBalancingStrategy();
        responseQueueTimeSamples = properties.getResponseQueueTimeSamples();
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

    protected void setUserDataSerde(final ArangoSerde serde) {
        this.userDataSerde = serde;
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

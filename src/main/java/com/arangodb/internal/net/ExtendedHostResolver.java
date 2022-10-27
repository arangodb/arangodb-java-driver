/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;
import com.arangodb.DbName;
import com.arangodb.internal.ArangoExecutorSync;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.Request;
import com.arangodb.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 */
public class ExtendedHostResolver implements HostResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedHostResolver.class);

    private final HostSet hosts;

    private final Integer maxConnections;
    private final ConnectionFactory connectionFactory;
    private final Integer acquireHostListInterval;
    private long lastUpdate;
    private ArangoExecutorSync executor;
    private InternalSerde arangoSerialization;


    public ExtendedHostResolver(final List<Host> hosts, final Integer maxConnections,
                                final ConnectionFactory connectionFactory, Integer acquireHostListInterval) {

        this.acquireHostListInterval = acquireHostListInterval;
        this.hosts = new HostSet(hosts);
        this.maxConnections = maxConnections;
        this.connectionFactory = connectionFactory;

        lastUpdate = 0;
    }

    @Override
    public void init(ArangoExecutorSync executor, InternalSerde arangoSerialization) {
        this.executor = executor;
        this.arangoSerialization = arangoSerialization;
    }

    @Override
    public HostSet resolve(boolean initial, boolean closeConnections) {

        if (!initial && isExpired()) {

            lastUpdate = System.currentTimeMillis();

            final Collection<String> endpoints = resolveFromServer();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resolve {} Endpoints", endpoints.size());
                LOGGER.debug("Endpoints {}", Arrays.deepToString(endpoints.toArray()));
            }

            if (!endpoints.isEmpty()) {
                hosts.markAllForDeletion();
            }

            for (final String endpoint : endpoints) {
                LOGGER.debug("Create HOST from {}", endpoint);

                if (endpoint.matches(".*://.+:[0-9]+")) {

                    final String[] s = endpoint.replaceAll(".*://", "").split(":");
                    if (s.length == 2) {
                        final HostDescription description = new HostDescription(s[0], Integer.parseInt(s[1]));
                        hosts.addHost(HostUtils.createHost(description, maxConnections, connectionFactory));
                    } else if (s.length == 4) {
                        // IPV6 Address - TODO: we need a proper function to resolve AND support IPV4 & IPV6 functions
                        // globally
                        final HostDescription description = new HostDescription("127.0.0.1", Integer.parseInt(s[3]));
                        hosts.addHost(HostUtils.createHost(description, maxConnections, connectionFactory));
                    } else {
                        LOGGER.warn("Skip Endpoint (Missing Port) {}", endpoint);
                    }

                } else {
                    LOGGER.warn("Skip Endpoint (Format) {}", endpoint);
                }
            }
            hosts.clearAllMarkedForDeletion();
        }

        return hosts;
    }

    private Collection<String> resolveFromServer() {

        Collection<String> response;

        try {

            response = executor.execute(
                    new Request(DbName.SYSTEM, RequestType.GET, "/_api/cluster/endpoints"),
                    response1 -> {
                        final List<Map<String, String>> tmp = arangoSerialization.deserialize(response1.getBody(),
                                "/endpoints",
                                constructParametricType(List.class,
                                        constructParametricType(Map.class, String.class, String.class)));
                        Collection<String> endpoints = new ArrayList<>();
                        for (final Map<String, String> map : tmp) {
                            endpoints.add(map.get("endpoint"));
                        }
                        return endpoints;
                    }, null);
        } catch (final ArangoDBException e) {
            final Integer responseCode = e.getResponseCode();

            // responseCode == 403: single server < 3.7
            // responseCode == 501: single server >= 3.7
            if (responseCode != null && (responseCode == 403 || responseCode == 501)) {
                response = Collections.emptyList();
            } else {
                throw e;
            }
        }

        return response;
    }

    private boolean isExpired() {
        return System.currentTimeMillis() > (lastUpdate + acquireHostListInterval);
    }

}

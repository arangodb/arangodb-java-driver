/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
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
import com.arangodb.config.HostDescription;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.util.AsyncQueue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionPoolImpl implements ConnectionPool {

    public static final int HTTP1_SLOTS = 1;                // HTTP/1: max 1 pending request
    public static final int HTTP1_SLOTS_PIPELINING = 10;    // HTTP/1: max pipelining
    public static final int HTTP2_SLOTS = 32;               // HTTP/2: max streams, hard-coded see BTS-2049

    private final AsyncQueue<Connection> slots = new AsyncQueue<>();
    private final HostDescription host;
    private final ArangoConfig config;
    private final int maxConnections;
    private final List<Connection> connections;
    private final ConnectionFactory factory;
    private final int maxSlots;
    private volatile String jwt = null;
    private volatile boolean closed = false;

    public ConnectionPoolImpl(final HostDescription host, final ArangoConfig config, final ConnectionFactory factory) {
        super();
        this.host = host;
        this.config = config;
        this.maxConnections = config.getMaxConnections();
        this.factory = factory;
        connections = new CopyOnWriteArrayList<>();
        switch (config.getProtocol()) {
            case HTTP_JSON:
            case HTTP_VPACK:
                maxSlots = config.getPipelining() ? HTTP1_SLOTS_PIPELINING : HTTP1_SLOTS;
                break;
            default:
                maxSlots = HTTP2_SLOTS;
        }
    }

    @Override
    public Connection createConnection() {
        Connection c = factory.create(config, host, this);
        c.setJwt(jwt);
        return c;
    }

    @Override
    public CompletableFuture<Connection> connection() {
        if (closed) {
            throw new ArangoDBException("Connection pool already closed!");
        }

        if (connections.size() < maxConnections) {
            Connection connection = createConnection();
            connections.add(connection);
            for (int i = 0; i < maxSlots; i++) {
                slots.offer((connection));
            }
        }

        return slots.poll();
    }

    @Override
    public void release(Connection connection) {
        slots.offer(connection);
    }

    @Override
    public void setJwt(String jwt) {
        if (jwt != null) {
            this.jwt = jwt;
            for (Connection connection : connections) {
                connection.setJwt(jwt);
            }
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        for (final Connection connection : connections) {
            connection.close();
        }
    }

    @Override
    public String toString() {
        return "ConnectionPoolImpl [host=" + host + ", maxConnections=" + maxConnections + ", connections="
                + connections.size() + ", factory=" + factory.getClass().getSimpleName() + "]";
    }

}

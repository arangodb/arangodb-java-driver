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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vollmary
 */
public class ConnectionPoolImpl implements ConnectionPool {

    private final HostDescription host;
    private final ArangoConfig config;
    private final int maxConnections;
    private final List<Connection> connections;
    private final ConnectionFactory factory;
    private int current;
    private volatile String jwt = null;
    private boolean closed = false;

    public ConnectionPoolImpl(final HostDescription host, final ArangoConfig config, final ConnectionFactory factory) {
        super();
        this.host = host;
        this.config = config;
        this.maxConnections = config.getMaxConnections();
        this.factory = factory;
        connections = new ArrayList<>();
        current = 0;
    }

    @Override
    public Connection createConnection(final HostDescription host) {
        Connection c = factory.create(config, host);
        c.setJwt(jwt);
        return c;
    }

    @Override
    public synchronized Connection connection() {
        if (closed) {
            throw new ArangoDBException("Connection pool already closed!");
        }

        final Connection connection;

        if (connections.size() < maxConnections) {
            connection = createConnection(host);
            connections.add(connection);
            current++;
        } else {
            final int index = Math.floorMod(current++, connections.size());
            connection = connections.get(index);
        }

        return connection;
    }

    @Override
    public void setJwt(String jwt) {
        this.jwt = jwt;
        for (Connection connection : connections) {
            connection.setJwt(jwt);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        for (final Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }

    @Override
    public String toString() {
        return "ConnectionPoolImpl [host=" + host + ", maxConnections=" + maxConnections + ", connections="
                + connections.size() + ", current=" + current + ", factory=" + factory.getClass().getSimpleName() + "]";
    }

}

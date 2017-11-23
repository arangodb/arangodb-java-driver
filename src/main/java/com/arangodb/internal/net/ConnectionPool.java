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

import java.io.IOException;
import java.util.LinkedList;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.Host;

/**
 * @author Mark Vollmary
 *
 */
public abstract class ConnectionPool<C extends Connection> {

	private final LinkedList<C> connections;
	private final int maxConnections;

	public ConnectionPool(final Integer maxConnections) {
		super();
		this.maxConnections = maxConnections;
		connections = new LinkedList<C>();
	}

	public abstract C createConnection(final Host host);

	public synchronized C connection(final HostHandle hostHandle) {
		final C c;
		if (hostHandle == null || hostHandle.getHost() == null) {
			if (connections.size() < maxConnections) {
				c = createConnection(null);
			} else {
				c = connections.removeFirst();
			}
			if (hostHandle != null) {
				hostHandle.setHost(c.getHost());
			}
		} else {
			final Host host = hostHandle.getHost();
			C tmp = null;
			for (final C connection : connections) {
				if (connection.getHost().equals(host)) {
					tmp = connection;
					connections.remove(tmp);
					break;
				}
			}
			c = tmp != null ? tmp : createConnection(host);
		}
		connections.add(c);
		return c;
	}

	public void disconnect() throws IOException {
		while (!connections.isEmpty()) {
			connections.removeLast().close();
		}
	}

	public void closeConnection(final C connection) {
		try {
			connection.close();
			connections.remove(connection);
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}

	public void closeConnectionOnError(final C connection) {
		try {
			connection.closeOnError();
			connections.remove(connection);
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}
}

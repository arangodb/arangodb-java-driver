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

package com.arangodb.internal.velocystream;

import java.util.LinkedList;

import com.arangodb.internal.ArangoDBConstants;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ConnectionPool<C extends Connection> {

	private final LinkedList<C> connections;
	private final int maxConnections;

	public ConnectionPool(final Integer maxConnections) {
		super();
		this.maxConnections = maxConnections != null ? Math.max(1, maxConnections)
				: ArangoDBConstants.MAX_CONNECTIONS_VST_DEFAULT;
		connections = new LinkedList<C>();
	}

	public abstract C createConnection();

	public synchronized C connection() {
		final C c;
		if (connections.size() < maxConnections) {
			c = createConnection();
		} else {
			c = connections.removeFirst();
		}
		connections.add(c);
		return c;
	}

	public void disconnect() {
		while (!connections.isEmpty()) {
			connections.removeLast().close();
		}
	}

}

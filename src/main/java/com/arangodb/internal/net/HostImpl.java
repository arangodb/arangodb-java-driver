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

package com.arangodb.internal.net;

import com.arangodb.ArangoDBException;

import java.io.IOException;

/**
 * @author Mark Vollmary
 *
 */
public class HostImpl implements Host {

	private final ConnectionPool connectionPool;
	private final HostDescription description;
	private boolean markforDeletion = false;

	public HostImpl(final ConnectionPool connectionPool, final HostDescription description) {
		super();
		this.connectionPool = connectionPool;
		this.description = description;
	}

	@Override
	public void close() throws IOException {
		connectionPool.close();
	}

	@Override
	public HostDescription getDescription() {
		return description;
	}

	@Override
	public Connection connection() {
		return connectionPool.connection();
	}

	@Override
	public void closeOnError() {
		try {
			connectionPool.close();
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public String toString() {
		return "HostImpl [connectionPool=" + connectionPool + ", description=" + description + ", markforDeletion="
				+ markforDeletion + "]";
	}

	public boolean isMarkforDeletion() {
		return markforDeletion;
	}

	public void setMarkforDeletion(boolean markforDeletion) {
		this.markforDeletion = markforDeletion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HostImpl other = (HostImpl) obj;
		if (description == null) {
			return other.description == null;
		} else return description.equals(other.description);
	}
	
	
	
}

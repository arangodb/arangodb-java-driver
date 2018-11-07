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

import java.io.IOException;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.util.IOUtils;

/**
 * @author Mark Vollmary
 *
 */
public class HostImpl implements Host {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HostImpl.class);

	private final ConnectionPool connectionPool;
	private final HostDescription description;

	public HostImpl(final ConnectionPool connectionPool, final HostDescription description) {
		super();
		this.connectionPool = connectionPool;
		this.description = description;
	}

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(connectionPool);
		LOGGER.warn("HostImpl {} being closed", this);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
		LOGGER.warn("HostImpl {} finalize() called", this);
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

}

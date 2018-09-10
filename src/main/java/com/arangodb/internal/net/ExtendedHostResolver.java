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

import java.util.Collection;
import java.util.List;

import com.arangodb.internal.util.HostUtils;

/**
 * @author Mark Vollmary
 *
 */
public class ExtendedHostResolver implements HostResolver {

	private static final long MAX_CACHE_TIME = 60 * 60 * 1000;
	private EndpointResolver resolver;
	private final List<Host> hosts;
	private final Integer maxConnections;
	private final ConnectionFactory connectionFactory;
	private long lastUpdate;

	public ExtendedHostResolver(final List<Host> hosts, final Integer maxConnections,
		final ConnectionFactory connectionFactory) {
		super();
		this.hosts = hosts;
		this.maxConnections = maxConnections;
		this.connectionFactory = connectionFactory;
		lastUpdate = 0;
	}

	@Override
	public void init(final EndpointResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public List<Host> resolve(final boolean initial, final boolean closeConnections) {
		if (!initial && isExpired()) {
			lastUpdate = System.currentTimeMillis();
			final Collection<String> endpoints = resolver.resolve(closeConnections);
			if (!endpoints.isEmpty()) {
				hosts.clear();
			}
			for (final String endpoint : endpoints) {
				if (endpoint.matches(".*://.+:[0-9]+")) {
					final String[] s = endpoint.replaceAll(".*://", "").split(":");
					if (s.length == 2) {
						final HostDescription description = new HostDescription(s[0], Integer.valueOf(s[1]));
						hosts.add(HostUtils.createHost(description, maxConnections, connectionFactory));
					}
				}
			}
		}
		return hosts;
	}

	private boolean isExpired() {
		return System.currentTimeMillis() > lastUpdate + MAX_CACHE_TIME;
	}
}

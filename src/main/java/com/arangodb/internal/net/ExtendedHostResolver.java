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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.ArangoExecutorSync;
import com.arangodb.internal.ArangoRequestParam;
import com.arangodb.internal.util.HostUtils;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class ExtendedHostResolver implements HostResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedHostResolver.class);

	private static final long MAX_CACHE_TIME = 60 * 60 * 1000;

	private HostSet hosts;

	private final Integer maxConnections;
	private final ConnectionFactory connectionFactory;

	private long lastUpdate;

	private ArangoExecutorSync executor;
	private ArangoSerialization arangoSerialization;

	public ExtendedHostResolver(final List<Host> hosts, final Integer maxConnections,
		final ConnectionFactory connectionFactory) {
		super();
		this.hosts = new HostSet(hosts);
		this.maxConnections = maxConnections;
		this.connectionFactory = connectionFactory;
		lastUpdate = 0;
	}

	@Override
	public void init(ArangoExecutorSync executor, ArangoSerialization arangoSerialization) {
		this.executor = executor;
		this.arangoSerialization = arangoSerialization;
	}

	@Override

	public HostSet resolve(final boolean initial, final boolean closeConnections) {

		if (!initial && isExpired()) {

			lastUpdate = System.currentTimeMillis();

			final Collection<String> endpoints = resolveFromServer();
			LOGGER.debug("Resolve " + endpoints.size() + " Endpoints");
			LOGGER.debug("Endpoints " + Arrays.deepToString(endpoints.toArray()));

			if (!endpoints.isEmpty()) {
				hosts.clear();
			}

			for (final String endpoint : endpoints) {
				LOGGER.debug("Create HOST from " + endpoint);

				if (endpoint.matches(".*://.+:[0-9]+")) {

					final String[] s = endpoint.replaceAll(".*://", "").split(":");
					if (s.length == 2) {
						final HostDescription description = new HostDescription(s[0], Integer.valueOf(s[1]));
						hosts.addHost(HostUtils.createHost(description, maxConnections, connectionFactory));
					} else if (s.length == 4) {
						// IPV6 Address - TODO: we need a proper function to resolve AND support IPV4 & IPV6 functions
						// globally
						final HostDescription description = new HostDescription("127.0.0.1", Integer.valueOf(s[3]));
						hosts.addHost(HostUtils.createHost(description, maxConnections, connectionFactory));
					} else {
						LOGGER.warn("Skip Endpoint (Missing Port)" + endpoint);
					}

				} else {
					LOGGER.warn("Skip Endpoint (Format)" + endpoint);
				}
			}
		}

		return hosts;
	}

	private Collection<String> resolveFromServer() throws ArangoDBException {

		Collection<String> response;

		try {

			response = executor.execute(
				new Request(ArangoRequestParam.SYSTEM, RequestType.GET, "/_api/cluster/endpoints"),
				new ResponseDeserializer<Collection<String>>() {
					@Override
					public Collection<String> deserialize(final Response response) throws VPackException {
						final VPackSlice field = response.getBody().get("endpoints");
						Collection<String> endpoints;
						if (field.isNone()) {
							endpoints = Collections.<String> emptyList();
						} else {
							final Collection<Map<String, String>> tmp = arangoSerialization.deserialize(field, Collection.class);
							endpoints = new ArrayList<String>();
							for (final Map<String, String> map : tmp) {
								for (final String value : map.values()) {
									endpoints.add(value);
								}
							}
						}
						return endpoints;
					}
				}, null);
		} catch (final ArangoDBException e) {
			final Integer responseCode = e.getResponseCode();
			if (responseCode != null && responseCode == 403) {
				response = Collections.<String> emptyList();
			} else {
				throw e;
			}
		}

		return response;
	}

	private boolean isExpired() {
		return System.currentTimeMillis() > lastUpdate + MAX_CACHE_TIME;
	}
}

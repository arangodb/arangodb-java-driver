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

import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

/**
 * @author Mark Vollmary
 *
 */
public final class ArangoDefaults {

	private ArangoDefaults() {
		super();
	}

	public static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
	public static final int LONG_BYTES = Long.SIZE / Byte.SIZE;

	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final Integer DEFAULT_PORT = 8529;
	public static final Integer DEFAULT_TIMEOUT = 0;
	public static final String DEFAULT_USER = "root";
	public static final Boolean DEFAULT_USE_SSL = false;
	public static final int CHUNK_MIN_HEADER_SIZE = INTEGER_BYTES + INTEGER_BYTES + LONG_BYTES;
	public static final int CHUNK_MAX_HEADER_SIZE = CHUNK_MIN_HEADER_SIZE + LONG_BYTES;
	public static final int CHUNK_DEFAULT_CONTENT_SIZE = 30000;
	public static final int MAX_CONNECTIONS_VST_DEFAULT = 1;
	public static final Integer CONNECTION_TTL_VST_DEFAULT = null;
	public static final int MAX_CONNECTIONS_HTTP_DEFAULT = 20;
	public static final Protocol DEFAULT_NETWORK_PROTOCOL = Protocol.VST;
	public static final boolean DEFAULT_ACQUIRE_HOST_LIST = false;
	public static final int DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
	public static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;

}

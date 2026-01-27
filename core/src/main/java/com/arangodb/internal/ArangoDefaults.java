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

import com.arangodb.Compression;
import com.arangodb.Protocol;
import com.arangodb.config.HostDescription;
import com.arangodb.entity.LoadBalancingStrategy;

import java.util.Collections;
import java.util.List;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public final class ArangoDefaults {

    private static final int MB = 1024 * 1024;
    public static final int MAX_CONNECTIONS_HTTP_DEFAULT = 20;
    public static final int MAX_CONNECTIONS_HTTP2_DEFAULT = 1;

    // default config properties
    public static final List<HostDescription> DEFAULT_HOSTS = Collections.emptyList();
    public static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP2_JSON;
    public static final String DEFAULT_USER = "root";
    public static final Integer DEFAULT_TIMEOUT = 0;
    public static final Long DEFAULT_CONNECTION_TTL_HTTP = 30_000L;
    public static final Boolean DEFAULT_USE_SSL = false;
    public static final String DEFAULT_SSL_PROTOCOL = "TLS";
    public static final String DEFAULT_SSL_TRUST_STORE_TYPE = "PKCS12";
    public static final Boolean DEFAULT_VERIFY_HOST = true;
    public static final Boolean DEFAULT_PIPELINING = false;
    public static final Integer DEFAULT_CONNECTION_WINDOW_SIZE = 32 * MB;
    public static final Integer DEFAULT_INITIAL_WINDOW_SIZE = 2 * MB;
    public static final Boolean DEFAULT_ACQUIRE_HOST_LIST = false;
    public static final Integer DEFAULT_ACQUIRE_HOST_LIST_INTERVAL = 60 * 60 * 1000; // hour
    public static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
    public static final Integer DEFAULT_RESPONSE_QUEUE_TIME_SAMPLES = 10;

    // region compression
    public static final Compression DEFAULT_COMPRESSION = Compression.NONE;
    public static final Integer DEFAULT_COMPRESSION_THRESHOLD = 1024;
    public static final Integer DEFAULT_COMPRESSION_LEVEL = 6;
    // endregion

    private ArangoDefaults() {
        super();
    }

}

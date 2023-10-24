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

package com.arangodb.internal;

import com.arangodb.ArangoSerdeAccessor;
import com.arangodb.internal.config.ArangoConfig;
import com.arangodb.internal.net.CommunicationProtocol;
import com.arangodb.internal.serde.InternalSerde;
import com.arangodb.internal.util.EncodeUtils;

/**
 * @author Mark Vollmary
 */
public abstract class ArangoExecuteable implements ArangoSerdeAccessor {

    private static final String SLASH = "/";

    private final ArangoExecutorSync executorSync;
    private final ArangoExecutorAsync executorAsync;
    private final InternalSerde serde;

    protected ArangoExecuteable(final CommunicationProtocol protocol, final ArangoConfig config) {
        this(new ArangoExecutorSync(protocol, config), new ArangoExecutorAsync(protocol, config), config.getInternalSerde());
    }

    protected ArangoExecuteable(final ArangoExecuteable other) {
        this(other.executorSync, other.executorAsync, other.serde);
    }

    private ArangoExecuteable(final ArangoExecutorSync executorSync,
                              final ArangoExecutorAsync executorAsync,
                              final InternalSerde serde) {
        this.executorSync = executorSync;
        this.executorAsync = executorAsync;
        this.serde = serde;
    }


    protected static String createPath(final String... params) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) continue;
            if (i > 0) {
                sb.append(SLASH);
            }
            final String param;
            if (params[i].contains(SLASH)) {
                param = createPath(params[i].split(SLASH));
            } else {
                param = EncodeUtils.encodeURIComponent(params[i]);
            }
            sb.append(param);
        }
        return sb.toString();
    }

    protected ArangoExecutorSync executorSync() {
        return executorSync;
    }

    protected ArangoExecutorAsync executorAsync() {
        return executorAsync;
    }

    @Override
    public InternalSerde getSerde() {
        return serde;
    }

    protected InternalRequest request(final String dbName, final RequestType requestType, final String... path) {
        return new InternalRequest(dbName, requestType, createPath(path));
    }

}

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
import com.arangodb.DbName;
import com.arangodb.internal.util.EncodeUtils;
import com.arangodb.serde.InternalSerde;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

import java.util.Map.Entry;

/**
 * @author Mark Vollmary
 */
public abstract class ArangoExecuteable<E extends ArangoExecutor> implements ArangoSerdeAccessor {

    private static final String SLASH = "/";

    protected final E executor;
    protected final InternalSerde serde;
    protected final ArangoContext context;

    protected ArangoExecuteable(final E executor, final InternalSerde serde, final ArangoContext context) {
        super();
        this.executor = executor;
        this.serde = serde;
        this.context = context;
    }

    protected E executor() {
        return executor;
    }

    @Override
    public InternalSerde getSerde() {
        return serde;
    }

    protected Request request(final DbName dbName, final RequestType requestType, final String... path) {
        final Request request = new Request(dbName, requestType, createPath(path));
        for (final Entry<String, String> header : context.getHeaderParam().entrySet()) {
            request.putHeaderParam(header.getKey(), header.getValue());
        }
        return request;
    }

    protected static String createPath(final String... params) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
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

}

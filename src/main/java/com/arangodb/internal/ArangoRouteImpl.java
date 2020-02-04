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

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoRoute;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class ArangoRouteImpl extends InternalArangoRoute<ArangoDBImpl, ArangoDatabaseImpl, ArangoExecutorSync>
        implements ArangoRoute {

    protected ArangoRouteImpl(final ArangoDatabaseImpl db, final String path, final Map<String, String> headerParam) {
        super(db, path, headerParam);
    }

    @Override
    public ArangoRoute route(final String... path) {
        return new ArangoRouteImpl(db, createPath(this.path, createPath(path)), headerParam);
    }

    @Override
    public ArangoRoute withHeader(final String key, final Object value) {
        _withHeader(key, value);
        return this;
    }

    @Override
    public ArangoRoute withQueryParam(final String key, final Object value) {
        _withQueryParam(key, value);
        return this;
    }

    @Override
    public ArangoRoute withBody(final Object body) {
        _withBody(body);
        return this;
    }

    private Response request(final RequestType requestType) {
        return executor.execute(createRequest(requestType), response -> response);
    }

    @Override
    public Response delete() throws ArangoDBException {
        return request(RequestType.DELETE);
    }

    @Override
    public Response get() throws ArangoDBException {
        return request(RequestType.GET);
    }

    @Override
    public Response head() throws ArangoDBException {
        return request(RequestType.HEAD);
    }

    @Override
    public Response patch() throws ArangoDBException {
        return request(RequestType.PATCH);
    }

    @Override
    public Response post() throws ArangoDBException {
        return request(RequestType.POST);
    }

    @Override
    public Response put() throws ArangoDBException {
        return request(RequestType.PUT);
    }

}

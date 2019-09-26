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

package com.arangodb.async.internal;

import com.arangodb.async.ArangoRouteAsync;
import com.arangodb.internal.InternalArangoRoute;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoRouteAsyncImpl
        extends InternalArangoRoute<ArangoDBAsyncImpl, ArangoDatabaseAsyncImpl, ArangoExecutorAsync>
        implements ArangoRouteAsync {

    ArangoRouteAsyncImpl(final ArangoDatabaseAsyncImpl db, final String path,
                         final Map<String, String> headerParam) {
        super(db, path, headerParam);
    }

    @Override
    public ArangoRouteAsync route(final String... path) {
        return new ArangoRouteAsyncImpl(db, createPath(this.path, createPath(path)), headerParam);
    }

    @Override
    public ArangoRouteAsync withHeader(final String key, final Object value) {
        _withHeader(key, value);
        return this;
    }

    @Override
    public ArangoRouteAsync withQueryParam(final String key, final Object value) {
        _withQueryParam(key, value);
        return this;
    }

    @Override
    public ArangoRouteAsync withBody(final Object body) {
        _withBody(body);
        return this;
    }

    private CompletableFuture<Response> request(final RequestType requestType) {
        return executor.execute(createRequest(requestType), response -> response);
    }

    @Override
    public CompletableFuture<Response> delete() {
        return request(RequestType.DELETE);
    }

    @Override
    public CompletableFuture<Response> get() {
        return request(RequestType.GET);
    }

    @Override
    public CompletableFuture<Response> head() {
        return request(RequestType.HEAD);
    }

    @Override
    public CompletableFuture<Response> patch() {
        return request(RequestType.PATCH);
    }

    @Override
    public CompletableFuture<Response> post() {
        return request(RequestType.POST);
    }

    @Override
    public CompletableFuture<Response> put() {
        return request(RequestType.PUT);
    }

}

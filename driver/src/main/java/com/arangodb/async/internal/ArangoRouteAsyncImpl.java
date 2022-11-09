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
import com.arangodb.internal.RequestType;
import com.arangodb.internal.InternalResponse;

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
        final String[] fullPath = new String[path.length + 1];
        fullPath[0] = this.path;
        System.arraycopy(path, 0, fullPath, 1, path.length);
        return new ArangoRouteAsyncImpl(db, String.join("/", fullPath), headerParam);
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

    private CompletableFuture<InternalResponse> request(final RequestType requestType) {
        return executor.execute(createRequest(requestType), response -> response);
    }

    @Override
    public CompletableFuture<InternalResponse> delete() {
        return request(RequestType.DELETE);
    }

    @Override
    public CompletableFuture<InternalResponse> get() {
        return request(RequestType.GET);
    }

    @Override
    public CompletableFuture<InternalResponse> head() {
        return request(RequestType.HEAD);
    }

    @Override
    public CompletableFuture<InternalResponse> patch() {
        return request(RequestType.PATCH);
    }

    @Override
    public CompletableFuture<InternalResponse> post() {
        return request(RequestType.POST);
    }

    @Override
    public CompletableFuture<InternalResponse> put() {
        return request(RequestType.PUT);
    }

}

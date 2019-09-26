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

package com.arangodb.async.internal;

import com.arangodb.async.ArangoVertexCollectionAsync;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.internal.InternalArangoVertexCollection;
import com.arangodb.model.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoVertexCollectionAsyncImpl extends
        InternalArangoVertexCollection<ArangoDBAsyncImpl, ArangoDatabaseAsyncImpl, ArangoGraphAsyncImpl, ArangoExecutorAsync>
        implements ArangoVertexCollectionAsync {

    ArangoVertexCollectionAsyncImpl(final ArangoGraphAsyncImpl graph, final String name) {
        super(graph, name);
    }

    @Override
    public CompletableFuture<Void> drop() {
        return executor.execute(dropRequest(), Void.class);
    }

    @Override
    public <T> CompletableFuture<VertexEntity> insertVertex(final T value) {
        return executor.execute(insertVertexRequest(value, new VertexCreateOptions()),
                insertVertexResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<VertexEntity> insertVertex(final T value, final VertexCreateOptions options) {
        return executor.execute(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<T> getVertex(final String key, final Class<T> type) {
        return getVertex(key, type, null);
    }

    @Override
    public <T> CompletableFuture<T> getVertex(
            final String key,
            final Class<T> type,
            final GraphDocumentReadOptions options) {
        boolean isCatchException = options != null ? options.isCatchException() : new GraphDocumentReadOptions().isCatchException();
        return executor.execute(getVertexRequest(key, options), getVertexResponseDeserializer(type))
                .exceptionally(ExceptionUtil.catchGetDocumentExceptions(isCatchException));
    }

    @Override
    public <T> CompletableFuture<VertexUpdateEntity> replaceVertex(final String key, final T value) {
        return executor.execute(replaceVertexRequest(key, value, new VertexReplaceOptions()),
                replaceVertexResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<VertexUpdateEntity> replaceVertex(
            final String key,
            final T value,
            final VertexReplaceOptions options) {
        return executor.execute(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<VertexUpdateEntity> updateVertex(final String key, final T value) {
        return executor.execute(updateVertexRequest(key, value, new VertexUpdateOptions()),
                updateVertexResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<VertexUpdateEntity> updateVertex(
            final String key,
            final T value,
            final VertexUpdateOptions options) {
        return executor.execute(updateVertexRequest(key, value, options), updateVertexResponseDeserializer(value));
    }

    @Override
    public CompletableFuture<Void> deleteVertex(final String key) {
        return executor.execute(deleteVertexRequest(key, new VertexDeleteOptions()), Void.class);
    }

    @Override
    public CompletableFuture<Void> deleteVertex(final String key, final VertexDeleteOptions options) {
        return executor.execute(deleteVertexRequest(key, options), Void.class);
    }

}

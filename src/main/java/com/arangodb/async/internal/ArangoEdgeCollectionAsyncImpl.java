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

import com.arangodb.async.ArangoEdgeCollectionAsync;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.internal.InternalArangoEdgeCollection;
import com.arangodb.model.*;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoEdgeCollectionAsyncImpl extends
        InternalArangoEdgeCollection<ArangoDBAsyncImpl, ArangoDatabaseAsyncImpl, ArangoGraphAsyncImpl, ArangoExecutorAsync>
        implements ArangoEdgeCollectionAsync {

    ArangoEdgeCollectionAsyncImpl(final ArangoGraphAsyncImpl graph, final String name) {
        super(graph, name);
    }

    @Override
    public <T> CompletableFuture<EdgeEntity> insertEdge(final T value) {
        return executor.execute(insertEdgeRequest(value, new EdgeCreateOptions()),
                insertEdgeResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<EdgeEntity> insertEdge(final T value, final EdgeCreateOptions options) {
        return executor.execute(insertEdgeRequest(value, options), insertEdgeResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<T> getEdge(final String key, final Class<T> type) {
        return executor.execute(getEdgeRequest(key, new GraphDocumentReadOptions()), getEdgeResponseDeserializer(type));
    }

    @Override
    public <T> CompletableFuture<T> getEdge(final String key, final Class<T> type, final GraphDocumentReadOptions options) {
        return executor.execute(getEdgeRequest(key, options), getEdgeResponseDeserializer(type));
    }

    @Override
    public <T> CompletableFuture<EdgeUpdateEntity> replaceEdge(final String key, final T value) {
        return executor.execute(replaceEdgeRequest(key, value, new EdgeReplaceOptions()),
                replaceEdgeResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<EdgeUpdateEntity> replaceEdge(
            final String key,
            final T value,
            final EdgeReplaceOptions options) {
        return executor.execute(replaceEdgeRequest(key, value, options), replaceEdgeResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<EdgeUpdateEntity> updateEdge(final String key, final T value) {
        return executor.execute(updateEdgeRequest(key, value, new EdgeUpdateOptions()),
                updateEdgeResponseDeserializer(value));
    }

    @Override
    public <T> CompletableFuture<EdgeUpdateEntity> updateEdge(
            final String key,
            final T value,
            final EdgeUpdateOptions options) {
        return executor.execute(updateEdgeRequest(key, value, options), updateEdgeResponseDeserializer(value));
    }

    @Override
    public CompletableFuture<Void> deleteEdge(final String key) {
        return executor.execute(deleteEdgeRequest(key, new EdgeDeleteOptions()), Void.class);
    }

    @Override
    public CompletableFuture<Void> deleteEdge(final String key, final EdgeDeleteOptions options) {
        return executor.execute(deleteEdgeRequest(key, options), Void.class);
    }

}

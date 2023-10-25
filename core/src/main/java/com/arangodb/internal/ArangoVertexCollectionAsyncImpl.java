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

import com.arangodb.ArangoGraphAsync;
import com.arangodb.ArangoVertexCollectionAsync;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoVertexCollectionAsyncImpl extends InternalArangoVertexCollection implements ArangoVertexCollectionAsync {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoVertexCollectionAsyncImpl.class);

    private final ArangoGraphAsync graph;

    protected ArangoVertexCollectionAsyncImpl(final ArangoGraphAsyncImpl graph, final String name) {
        super(graph, graph.db().name(), graph.name(), name);
        this.graph = graph;
    }

    @Override
    public ArangoGraphAsync graph() {
        return graph;
    }

    @Override
    public CompletableFuture<Void> drop() {
        return drop(new VertexCollectionDropOptions());
    }

    @Override
    public CompletableFuture<Void> drop(final VertexCollectionDropOptions options) {
        return executorAsync().execute(() -> dropRequest(options), Void.class);
    }

    @Override
    public CompletableFuture<VertexEntity> insertVertex(final Object value) {
        return executorAsync().execute(() -> insertVertexRequest(value, new VertexCreateOptions()),
                insertVertexResponseDeserializer());
    }

    @Override
    public CompletableFuture<VertexEntity> insertVertex(final Object value, final VertexCreateOptions options) {
        return executorAsync().execute(() -> insertVertexRequest(value, options), insertVertexResponseDeserializer());
    }

    @Override
    public <T> CompletableFuture<T> getVertex(final String key, final Class<T> type) {
        return executorAsync().execute(() -> getVertexRequest(key, new GraphDocumentReadOptions()),
                        getVertexResponseDeserializer(type))
                .exceptionally(e -> {
                    // FIXME
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                    return null;
                });

    }

    @Override
    public <T> CompletableFuture<T> getVertex(final String key, final Class<T> type, final GraphDocumentReadOptions options) {
        return executorAsync().execute(() -> getVertexRequest(key, options), getVertexResponseDeserializer(type))
                .exceptionally(e -> {
                    // FIXME
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                    return null;
                });
    }

    @Override
    public CompletableFuture<VertexUpdateEntity> replaceVertex(final String key, final Object value) {
        return executorAsync().execute(() -> replaceVertexRequest(key, value, new VertexReplaceOptions()),
                replaceVertexResponseDeserializer());
    }

    @Override
    public CompletableFuture<VertexUpdateEntity> replaceVertex(final String key, final Object value, final VertexReplaceOptions options) {
        return executorAsync().execute(() -> replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer());
    }

    @Override
    public CompletableFuture<VertexUpdateEntity> updateVertex(final String key, final Object value) {
        return executorAsync().execute(() -> updateVertexRequest(key, value, new VertexUpdateOptions()),
                updateVertexResponseDeserializer());
    }

    @Override
    public CompletableFuture<VertexUpdateEntity> updateVertex(final String key, final Object value, final VertexUpdateOptions options) {
        return executorAsync().execute(() -> updateVertexRequest(key, value, options), updateVertexResponseDeserializer());
    }

    @Override
    public CompletableFuture<Void> deleteVertex(final String key) {
        return executorAsync().execute(() -> deleteVertexRequest(key, new VertexDeleteOptions()), Void.class);
    }

    @Override
    public CompletableFuture<Void> deleteVertex(final String key, final VertexDeleteOptions options) {
        return executorAsync().execute(() -> deleteVertexRequest(key, options), Void.class);
    }

}

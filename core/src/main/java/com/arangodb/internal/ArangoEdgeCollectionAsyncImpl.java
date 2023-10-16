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

import com.arangodb.*;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark Vollmary
 */
public class ArangoEdgeCollectionAsyncImpl extends InternalArangoEdgeCollection implements ArangoEdgeCollectionAsync {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoEdgeCollectionAsyncImpl.class);
    private final ArangoGraphAsync graph;

    protected ArangoEdgeCollectionAsyncImpl(final ArangoGraphAsyncImpl graph, final String name) {
        super(graph, graph.db().name(), graph.name(), name);
        this.graph = graph;
    }

    @Override
    public ArangoGraphAsync graph() {
        return graph;
    }

    @Override
    public CompletableFuture<Void> drop() {
        return drop(new EdgeCollectionDropOptions());
    }

    @Override
    public CompletableFuture<Void> drop(final EdgeCollectionDropOptions options) {
        return executorAsync().execute(removeEdgeDefinitionRequest(options), Void.class);
    }

    @Override
    public CompletableFuture<EdgeEntity> insertEdge(final Object value) {
        return executorAsync().execute(insertEdgeRequest(value, new EdgeCreateOptions()),
                insertEdgeResponseDeserializer());
    }

    @Override
    public CompletableFuture<EdgeEntity> insertEdge(final Object value, final EdgeCreateOptions options) {
        return executorAsync().execute(insertEdgeRequest(value, options), insertEdgeResponseDeserializer());
    }

    @Override
    public <T> CompletableFuture<T> getEdge(final String key, final Class<T> type) {
        return executorAsync().execute(getEdgeRequest(key, new GraphDocumentReadOptions()),
                getEdgeResponseDeserializer(type));
    }

    @Override
    public <T> CompletableFuture<T> getEdge(final String key, final Class<T> type, final GraphDocumentReadOptions options) {
        return executorAsync().execute(getEdgeRequest(key, options), getEdgeResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<EdgeUpdateEntity> replaceEdge(final String key, final Object value) {
        return executorAsync().execute(replaceEdgeRequest(key, value, new EdgeReplaceOptions()),
                replaceEdgeResponseDeserializer());
    }

    @Override
    public CompletableFuture<EdgeUpdateEntity> replaceEdge(final String key, final Object value, final EdgeReplaceOptions options) {
        return executorAsync().execute(replaceEdgeRequest(key, value, options), replaceEdgeResponseDeserializer());
    }

    @Override
    public CompletableFuture<EdgeUpdateEntity> updateEdge(final String key, final Object value) {
        return executorAsync().execute(updateEdgeRequest(key, value, new EdgeUpdateOptions()),
                updateEdgeResponseDeserializer());
    }

    @Override
    public CompletableFuture<EdgeUpdateEntity> updateEdge(final String key, final Object value, final EdgeUpdateOptions options) {
        return executorAsync().execute(updateEdgeRequest(key, value, options), updateEdgeResponseDeserializer());
    }

    @Override
    public CompletableFuture<Void> deleteEdge(final String key) {
        return executorAsync().execute(deleteEdgeRequest(key, new EdgeDeleteOptions()), Void.class);
    }

    @Override
    public CompletableFuture<Void> deleteEdge(final String key, final EdgeDeleteOptions options) {
        return executorAsync().execute(deleteEdgeRequest(key, options), Void.class);
    }

}

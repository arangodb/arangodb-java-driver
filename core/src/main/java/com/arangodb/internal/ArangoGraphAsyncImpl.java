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
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.ReplaceEdgeDefinitionOptions;
import com.arangodb.model.VertexCollectionCreateOptions;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.arangodb.internal.ArangoErrors.ERROR_GRAPH_NOT_FOUND;
import static com.arangodb.internal.ArangoErrors.matches;

public class ArangoGraphAsyncImpl extends InternalArangoGraph implements ArangoGraphAsync {

    private final ArangoDatabaseAsync db;

    protected ArangoGraphAsyncImpl(final ArangoDatabaseAsyncImpl db, final String name) {
        super(db, db.name(), name);
        this.db = db;
    }

    @Override
    public ArangoDatabaseAsync db() {
        return db;
    }

    @Override
    public CompletableFuture<Boolean> exists() {
        return getInfo()
                .thenApply(Objects::nonNull)
                .exceptionally(err -> {
                    Throwable e = err instanceof CompletionException ? err.getCause() : err;
                    if (e instanceof ArangoDBException) {
                        ArangoDBException aEx = (ArangoDBException) e;
                        if (matches(aEx, 404, ERROR_GRAPH_NOT_FOUND)) {
                            return false;
                        }
                    }
                    throw ArangoDBException.of(e);
                });
    }

    @Override
    public CompletableFuture<GraphEntity> create(final Iterable<EdgeDefinition> edgeDefinitions) {
        return db().createGraph(name(), edgeDefinitions);
    }

    @Override
    public CompletableFuture<GraphEntity> create(final Iterable<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options) {
        return db().createGraph(name(), edgeDefinitions, options);
    }

    @Override
    public CompletableFuture<Void> drop() {
        return executorAsync().execute(this::dropRequest, Void.class);
    }

    @Override
    public CompletableFuture<Void> drop(final boolean dropCollections) {
        return executorAsync().execute(() -> dropRequest(dropCollections), Void.class);
    }

    @Override
    public CompletableFuture<GraphEntity> getInfo() {
        return executorAsync().execute(this::getInfoRequest, getInfoResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<String>> getVertexCollections() {
        return executorAsync().execute(this::getVertexCollectionsRequest, getVertexCollectionsResponseDeserializer());
    }

    @Override
    public CompletableFuture<GraphEntity> addVertexCollection(final String name) {
        return addVertexCollection(name, new VertexCollectionCreateOptions());
    }

    @Override
    public CompletableFuture<GraphEntity> addVertexCollection(final String name, final VertexCollectionCreateOptions options) {
        return executorAsync().execute(() -> addVertexCollectionRequest(name, options), addVertexCollectionResponseDeserializer());
    }

    @Override
    public ArangoVertexCollectionAsync vertexCollection(final String name) {
        return new ArangoVertexCollectionAsyncImpl(this, name);
    }

    @Override
    public ArangoEdgeCollectionAsync edgeCollection(final String name) {
        return new ArangoEdgeCollectionAsyncImpl(this, name);
    }

    @Override
    public CompletableFuture<Collection<String>> getEdgeDefinitions() {
        return executorAsync().execute(this::getEdgeDefinitionsRequest, getEdgeDefinitionsDeserializer());
    }

    @Override
    public CompletableFuture<GraphEntity> addEdgeDefinition(final EdgeDefinition definition) {
        return executorAsync().execute(() -> addEdgeDefinitionRequest(definition), addEdgeDefinitionResponseDeserializer());
    }

    @Override
    public CompletableFuture<GraphEntity> replaceEdgeDefinition(final EdgeDefinition definition) {
        return replaceEdgeDefinition(definition, new ReplaceEdgeDefinitionOptions());
    }

    @Override
    public CompletableFuture<GraphEntity> replaceEdgeDefinition(final EdgeDefinition definition, final ReplaceEdgeDefinitionOptions options) {
        return executorAsync().execute(() -> replaceEdgeDefinitionRequest(definition, options), replaceEdgeDefinitionResponseDeserializer());
    }

}

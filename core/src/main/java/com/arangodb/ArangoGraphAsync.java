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

package com.arangodb;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.ReplaceEdgeDefinitionOptions;
import com.arangodb.model.VertexCollectionCreateOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoGraph}
 */
@ThreadSafe
public interface ArangoGraphAsync extends ArangoSerdeAccessor {

    /**
     * @return database async API
     */
    ArangoDatabaseAsync db();

    /**
     * @return graph name
     */
    String name();

    /**
     * Asynchronous version of {@link ArangoGraph#exists()}
     */
    CompletableFuture<Boolean> exists();

    /**
     * Asynchronous version of {@link ArangoGraph#create(Iterable)}
     */
    CompletableFuture<GraphEntity> create(Iterable<EdgeDefinition> edgeDefinitions);

    /**
     * Asynchronous version of {@link ArangoGraph#create(Iterable, GraphCreateOptions)}
     */
    CompletableFuture<GraphEntity> create(Iterable<EdgeDefinition> edgeDefinitions, GraphCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoGraph#drop()}
     */
    CompletableFuture<Void> drop();

    /**
     * Asynchronous version of {@link ArangoGraph#drop(boolean)}
     */
    CompletableFuture<Void> drop(boolean dropCollections);

    /**
     * Asynchronous version of {@link ArangoGraph#getInfo()}
     */
    CompletableFuture<GraphEntity> getInfo();

    /**
     * Asynchronous version of {@link ArangoGraph#getVertexCollections()}
     */
    CompletableFuture<Collection<String>> getVertexCollections();

    /**
     * Asynchronous version of {@link ArangoGraph#addVertexCollection(String)}
     */
    CompletableFuture<GraphEntity> addVertexCollection(String name);

    /**
     * Asynchronous version of {@link ArangoGraph#addVertexCollection(String, VertexCollectionCreateOptions)}
     */
    CompletableFuture<GraphEntity> addVertexCollection(String name, VertexCollectionCreateOptions options);

    /**
     * Returns a {@code ArangoVertexCollectionAsync} instance for the given vertex collection name.
     *
     * @param name Name of the vertex collection
     * @return collection handler
     */
    ArangoVertexCollectionAsync vertexCollection(String name);

//    /**
//     * Returns a {@code ArangoEdgeCollectionAsync} instance for the given edge collection name.
//     *
//     * @param name Name of the edge collection
//     * @return collection handler
//     */
//    ArangoEdgeCollectionAsync edgeCollection(String name);

    /**
     * Asynchronous version of {@link ArangoGraph#getEdgeDefinitions()}
     */
    CompletableFuture<Collection<String>> getEdgeDefinitions();

    /**
     * Asynchronous version of {@link ArangoGraph#addEdgeDefinition(EdgeDefinition)}
     */
    CompletableFuture<GraphEntity> addEdgeDefinition(EdgeDefinition definition);

    /**
     * Asynchronous version of {@link ArangoGraph#replaceEdgeDefinition(EdgeDefinition)}
     */
    CompletableFuture<GraphEntity> replaceEdgeDefinition(EdgeDefinition definition);

    /**
     * Asynchronous version of {@link ArangoGraph#replaceEdgeDefinition(EdgeDefinition, ReplaceEdgeDefinitionOptions)}
     */
    CompletableFuture<GraphEntity> replaceEdgeDefinition(EdgeDefinition definition, ReplaceEdgeDefinitionOptions options);

}

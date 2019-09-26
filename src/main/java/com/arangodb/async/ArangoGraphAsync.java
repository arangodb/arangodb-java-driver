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

package com.arangodb.async;

import com.arangodb.ArangoSerializationAccessor;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for operations on ArangoDB graph level.
 *
 * @author Mark Vollmary
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/">API Documentation</a>
 */
@SuppressWarnings("unused")
public interface ArangoGraphAsync extends ArangoSerializationAccessor {

    /**
     * The the handler of the database the named graph is within
     *
     * @return database handler
     */
    ArangoDatabaseAsync db();

    /**
     * The name of the collection
     *
     * @return collection name
     */
    String name();

    /**
     * Checks whether the graph exists
     *
     * @return true if the graph exists, otherwise false
     */
    CompletableFuture<Boolean> exists();

    /**
     * Creates the graph in the graph module. The creation of a graph requires the name of the graph and a definition of
     * its edges.
     *
     * @param edgeDefinitions An array of definitions for the edge
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> create(final Collection<EdgeDefinition> edgeDefinitions);

    /**
     * Creates the graph in the graph module. The creation of a graph requires the name of the graph and a definition of
     * its edges.
     *
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> createGraph(
            final Collection<EdgeDefinition> edgeDefinitions,
            final GraphCreateOptions options);

    /**
     * Delete an existing graph
     *
     * @return void
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#drop-a-graph">API Documentation</a>
     */
    CompletableFuture<Void> drop();

    /**
     * Delete an existing graph including
     *
     * @param dropCollections Drop collections of this graph as well. Collections will only be dropped if they are not used in other
     *                        graphs.
     * @return void
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#drop-a-graph">API Documentation</a>
     */
    CompletableFuture<Void> drop(boolean dropCollections);

    /**
     * Get a graph from the graph module
     *
     * @return the definition content of this graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#get-a-graph">API Documentation</a>
     */
    CompletableFuture<GraphEntity> getInfo();

    /**
     * Lists all vertex collections used in this graph
     *
     * @return all vertex collections within this graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-vertex-collections">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getVertexCollections();

    /**
     * Adds a vertex collection to the set of collections of the graph. If the collection does not exist, it will be
     * created.
     *
     * @param name The name of the collection
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-vertex-collection">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> addVertexCollection(final String name);

    /**
     * Returns a handler of the vertex collection by the given name
     *
     * @param name Name of the vertex collection
     * @return collection handler
     */
    ArangoVertexCollectionAsync vertexCollection(final String name);

    /**
     * Returns a handler of the edge collection by the given name
     *
     * @param name Name of the edge collection
     * @return collection handler
     */
    ArangoEdgeCollectionAsync edgeCollection(final String name);

    /**
     * Lists all edge collections used in this graph
     *
     * @return all edge collections within this graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-edge-definitions">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getEdgeDefinitions();

    /**
     * Add a new edge definition to the graph
     *
     * @param definition
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-edge-definition">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> addEdgeDefinition(final EdgeDefinition definition);

    /**
     * Change one specific edge definition. This will modify all occurrences of this definition in all graphs known to
     * your database
     *
     * @param definition The edge definition
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#replace-an-edge-definition">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> replaceEdgeDefinition(final EdgeDefinition definition);

    /**
     * Remove one edge definition from the graph. This will only remove the edge collection, the vertex collections
     * remain untouched and can still be used in your queries
     *
     * @param definitionName The name of the edge collection used in the definition
     * @return information about the graph
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/Gharial/Management.html#remove-an-edge-definition-from-the-graph">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> removeEdgeDefinition(final String definitionName);

}

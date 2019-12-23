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

import java.util.Collection;

/**
 * Interface for operations on ArangoDB graph level.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/gharial.html">API Documentation</a>
 */
public interface ArangoGraph extends ArangoSerializationAccessor {

    /**
     * The the handler of the database the named graph is within
     *
     * @return database handler
     */
    ArangoDatabase db();

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
    boolean exists() throws ArangoDBException;

    /**
     * Creates the graph in the graph module. The creation of a graph requires the name of the graph and a definition of
     * its edges.
     *
     * @param edgeDefinitions An array of definitions for the edge
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity create(Collection<EdgeDefinition> edgeDefinitions) throws ArangoDBException;

    /**
     * Creates the graph in the graph module. The creation of a graph requires the name of the graph and a definition of
     * its edges.
     *
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity create(Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options) throws ArangoDBException;

    /**
     * Deletes the graph from the database.
     *
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#drop-a-graph">API Documentation</a>
     */
    void drop() throws ArangoDBException;

    /**
     * Deletes the graph from the database.
     *
     * @param dropCollections Drop collections of this graph as well. Collections will only be
     *                        dropped if they are not used in other graphs.
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/gharial-management.html#drop-a-graph">API
     * Documentation</a>
     */
    void drop(boolean dropCollections) throws ArangoDBException;

    /**
     * Retrieves general information about the graph.
     *
     * @return the definition content of this graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#get-a-graph">API Documentation</a>
     */
    GraphEntity getInfo() throws ArangoDBException;

    /**
     * Fetches all vertex collections from the graph and returns a list of collection names.
     *
     * @return all vertex collections within this graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#list-vertex-collections">API
     * Documentation</a>
     */
    Collection<String> getVertexCollections() throws ArangoDBException;

    /**
     * Adds a vertex collection to the set of collections of the graph. If the collection does not exist, it will be
     * created.
     *
     * @param name Name of the vertex collection
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#add-vertex-collection">API
     * Documentation</a>
     */
    GraphEntity addVertexCollection(String name) throws ArangoDBException;

    /**
     * Returns a {@code ArangoVertexCollection} instance for the given vertex collection name.
     *
     * @param name Name of the vertex collection
     * @return collection handler
     */
    ArangoVertexCollection vertexCollection(String name);

    /**
     * Returns a {@code ArangoEdgeCollection} instance for the given edge collection name.
     *
     * @param name Name of the edge collection
     * @return collection handler
     */
    ArangoEdgeCollection edgeCollection(String name);

    /**
     * Fetches all edge collections from the graph and returns a list of collection names.
     *
     * @return all edge collections within this graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#list-edge-definitions">API
     * Documentation</a>
     */
    Collection<String> getEdgeDefinitions() throws ArangoDBException;

    /**
     * Adds the given edge definition to the graph.
     *
     * @param definition The edge definition
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#add-edge-definition">API
     * Documentation</a>
     */
    GraphEntity addEdgeDefinition(EdgeDefinition definition) throws ArangoDBException;

    /**
     * Change one specific edge definition. This will modify all occurrences of this definition in all graphs known to
     * your database
     *
     * @param definition The edge definition
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#replace-an-edge-definition">API
     * Documentation</a>
     */
    GraphEntity replaceEdgeDefinition(EdgeDefinition definition) throws ArangoDBException;

    /**
     * Remove one edge definition from the graph. This will only remove the edge collection, the vertex collections
     * remain untouched and can still be used in your queries
     *
     * @param definitionName The name of the edge collection used in the definition
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/gharial-management.html#remove-an-edge-definition-from-the-graph">API
     * Documentation</a>
     */
    GraphEntity removeEdgeDefinition(String definitionName) throws ArangoDBException;

}

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

import java.util.Collection;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.GraphCreateOptions;

/**
 * @author Mark Vollmary
 *
 */
public interface ArangoGraph {

	/**
	 * The the handler of the database the named graph is within
	 * 
	 * @return database handler
	 */
	public ArangoDatabase db();

	/**
	 * The name of the collection
	 * 
	 * @return collection name
	 */
	public String name();

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
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity create(final Collection<EdgeDefinition> edgeDefinitions) throws ArangoDBException;

	/**
	 * Creates the graph in the graph module. The creation of a graph requires the name of the graph and a definition of
	 * its edges.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @param options
	 *            Additional options, can be null
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity create(final Collection<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options)
			throws ArangoDBException;

	/**
	 * Delete an existing graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#drop-a-graph">API Documentation</a>
	 * @throws ArangoDBException
	 */
	void drop() throws ArangoDBException;

	/**
	 * Get a graph from the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#get-a-graph">API Documentation</a>
	 * @return the definition content of this graph
	 * @throws ArangoDBException
	 */
	GraphEntity getInfo() throws ArangoDBException;

	/**
	 * Lists all vertex collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-vertex-collections">API
	 *      Documentation</a>
	 * @return all vertex collections within this graph
	 * @throws ArangoDBException
	 */
	Collection<String> getVertexCollections() throws ArangoDBException;

	/**
	 * Adds a vertex collection to the set of collections of the graph. If the collection does not exist, it will be
	 * created.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-vertex-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity addVertexCollection(final String name) throws ArangoDBException;

	/**
	 * Returns a handler of the vertex collection by the given name
	 * 
	 * @param name
	 *            Name of the vertex collection
	 * @return collection handler
	 */
	ArangoVertexCollection vertexCollection(final String name);

	/**
	 * Returns a handler of the edge collection by the given name
	 * 
	 * @param name
	 *            Name of the edge collection
	 * @return collection handler
	 */
	ArangoEdgeCollection edgeCollection(final String name);

	/**
	 * Lists all edge collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-edge-definitions">API
	 *      Documentation</a>
	 * @return all edge collections within this graph
	 * @throws ArangoDBException
	 */
	Collection<String> getEdgeDefinitions() throws ArangoDBException;

	/**
	 * Add a new edge definition to the graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-edge-definition">API
	 *      Documentation</a>
	 * @param definition
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity addEdgeDefinition(final EdgeDefinition definition) throws ArangoDBException;

	/**
	 * Change one specific edge definition. This will modify all occurrences of this definition in all graphs known to
	 * your database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#replace-an-edge-definition">API
	 *      Documentation</a>
	 * @param definition
	 *            The edge definition
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity replaceEdgeDefinition(final EdgeDefinition definition) throws ArangoDBException;

	/**
	 * Remove one edge definition from the graph. This will only remove the edge collection, the vertex collections
	 * remain untouched and can still be used in your queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Gharial/Management.html#remove-an-edge-definition-from-the-graph">API
	 *      Documentation</a>
	 * @param definitionName
	 *            The name of the edge collection used in the definition
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity removeEdgeDefinition(final String definitionName) throws ArangoDBException;

}

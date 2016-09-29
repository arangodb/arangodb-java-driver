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
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.VertexCollectionCreateOptions;
import com.arangodb.velocypack.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoGraph extends ArangoExecuteable {

	private final ArangoDatabase db;
	private final String name;

	protected ArangoGraph(final ArangoDatabase db, final String name) {
		super(db.communication(), db.vpack(), db.vpackNull(), db.vpackParser(), db.documentCache(),
				db.collectionCache());
		this.db = db;
		this.name = name;
	}

	protected ArangoDatabase db() {
		return db;
	}

	protected String name() {
		return name;
	}

	/**
	 * Delete an existing graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#drop-a-graph">API Documentation</a>
	 * @throws ArangoDBException
	 */
	public void drop() throws ArangoDBException {
		executeSync(dropRequest(), Void.class);
	}

	/**
	 * Delete an existing graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#drop-a-graph">API Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> dropAsync() {
		return executeAsync(dropRequest(), Void.class);
	}

	private Request dropRequest() {
		return new Request(db.name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_GHARIAL, name));
	}

	/**
	 * Get a graph from the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#get-a-graph">API Documentation</a>
	 * @return the definition content of this graph
	 * @throws ArangoDBException
	 */
	public GraphEntity getInfo() throws ArangoDBException {
		return executeSync(getInfoRequest(), getInfoResponseDeserializer());
	}

	/**
	 * Get a graph from the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#get-a-graph">API Documentation</a>
	 * @return the definition content of this graph
	 */
	public CompletableFuture<GraphEntity> getInfoAsync() {
		return executeAsync(getInfoRequest(), getInfoResponseDeserializer());
	}

	private Request getInfoRequest() {
		return new Request(db.name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_GHARIAL, name));
	}

	private ResponseDeserializer<GraphEntity> getInfoResponseDeserializer() {
		return addVertexCollectionResponseDeserializer();
	}

	/**
	 * Lists all vertex collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-vertex-collections">API
	 *      Documentation</a>
	 * @return all vertex collections within this graph
	 * @throws ArangoDBException
	 */
	public Collection<String> getVertexCollections() throws ArangoDBException {
		return executeSync(getVertexCollectionsRequest(), getVertexCollectionsResponseDeserializer());
	}

	/**
	 * Lists all vertex collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-vertex-collections">API
	 *      Documentation</a>
	 * @return all vertex collections within this graph
	 */
	public CompletableFuture<Collection<String>> getVertexCollectionsAsync() {
		return executeAsync(getVertexCollectionsRequest(), getVertexCollectionsResponseDeserializer());
	}

	private Request getVertexCollectionsRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.VERTEX));
	}

	private ResponseDeserializer<Collection<String>> getVertexCollectionsResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.COLLECTIONS),
			new Type<Collection<String>>() {
			}.getType());
	}

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
	public GraphEntity addVertexCollection(final String name) throws ArangoDBException {
		return executeSync(addVertexCollectionRequest(name), addVertexCollectionResponseDeserializer());
	}

	/**
	 * Adds a vertex collection to the set of collections of the graph. If the collection does not exist, it will be
	 * created.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-vertex-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @return information about the graph
	 */
	public CompletableFuture<GraphEntity> addVertexCollectionAsync(final String name) {
		return executeAsync(addVertexCollectionRequest(name), addVertexCollectionResponseDeserializer());
	}

	private Request addVertexCollectionRequest(final String name) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name(), ArangoDBConstants.VERTEX));
		request.setBody(serialize(OptionsBuilder.build(new VertexCollectionCreateOptions(), name)));
		return request;
	}

	private ResponseDeserializer<GraphEntity> addVertexCollectionResponseDeserializer() {
		return addEdgeDefinitionResponseDeserializer();
	}

	public ArangoVertexCollection vertexCollection(final String name) {
		return new ArangoVertexCollection(this, name);
	}

	public ArangoEdgeCollection edgeCollection(final String name) {
		return new ArangoEdgeCollection(this, name);
	}

	/**
	 * Lists all edge collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-edge-definitions">API
	 *      Documentation</a>
	 * @return all edge collections within this graph
	 * @throws ArangoDBException
	 */
	public Collection<String> getEdgeDefinitions() throws ArangoDBException {
		return executeSync(getEdgeDefinitionsRequest(), getEdgeDefinitionsDeserializer());
	}

	/**
	 * Lists all edge collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-edge-definitions">API
	 *      Documentation</a>
	 * @return all edge collections within this graph
	 */
	public CompletableFuture<Collection<String>> getEdgeDefinitionsAsync() {
		return executeAsync(getEdgeDefinitionsRequest(), getEdgeDefinitionsDeserializer());
	}

	private Request getEdgeDefinitionsRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE));
	}

	private ResponseDeserializer<Collection<String>> getEdgeDefinitionsDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.COLLECTIONS),
			new Type<Collection<String>>() {
			}.getType());
	}

	/**
	 * Add a new edge definition to the graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-edge-definition">API
	 *      Documentation</a>
	 * @param definition
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	public GraphEntity addEdgeDefinition(final EdgeDefinition definition) throws ArangoDBException {
		return executeSync(addEdgeDefinitionRequest(definition), addEdgeDefinitionResponseDeserializer());
	}

	/**
	 * Add a new edge definition to the graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#add-edge-definition">API
	 *      Documentation</a>
	 * @param definition
	 * @return information about the graph
	 */
	public CompletableFuture<GraphEntity> addEdgeDefinitionAsync(final EdgeDefinition definition) {
		return executeAsync(addEdgeDefinitionRequest(definition), addEdgeDefinitionResponseDeserializer());
	}

	private Request addEdgeDefinitionRequest(final EdgeDefinition definition) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE));
		request.setBody(serialize(definition));
		return request;
	}

	private ResponseDeserializer<GraphEntity> addEdgeDefinitionResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPH), GraphEntity.class);
	}

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
	public GraphEntity replaceEdgeDefinition(final EdgeDefinition definition) throws ArangoDBException {
		return executeSync(replaceEdgeDefinitionRequest(definition), replaceEdgeDefinitionResponseDeserializer());
	}

	/**
	 * Change one specific edge definition. This will modify all occurrences of this definition in all graphs known to
	 * your database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#replace-an-edge-definition">API
	 *      Documentation</a>
	 * @param definition
	 *            The edge definition
	 * @return information about the graph
	 */
	public CompletableFuture<GraphEntity> replaceEdgeDefinitionAsync(final EdgeDefinition definition) {
		return executeAsync(replaceEdgeDefinitionRequest(definition), replaceEdgeDefinitionResponseDeserializer());
	}

	private Request replaceEdgeDefinitionRequest(final EdgeDefinition definition) {
		final Request request = new Request(db.name(), RequestType.PUT, createPath(ArangoDBConstants.PATH_API_GHARIAL,
			name, ArangoDBConstants.EDGE, definition.getCollection()));
		request.setBody(serialize(definition));
		return request;
	}

	private ResponseDeserializer<GraphEntity> replaceEdgeDefinitionResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPH), GraphEntity.class);
	}

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
	public GraphEntity removeEdgeDefinition(final String definitionName) throws ArangoDBException {
		return executeSync(removeEdgeDefinitionRequest(definitionName), removeEdgeDefinitionResponseDeserializer());
	}

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
	 */
	public CompletableFuture<GraphEntity> removeEdgeDefinitionAsync(final String definitionName) {
		return executeAsync(removeEdgeDefinitionRequest(definitionName), removeEdgeDefinitionResponseDeserializer());
	}

	private Request removeEdgeDefinitionRequest(final String definitionName) {
		return new Request(db.name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE, definitionName));
	}

	private ResponseDeserializer<GraphEntity> removeEdgeDefinitionResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPH), GraphEntity.class);
	}
}

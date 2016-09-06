package com.arangodb;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.GraphResult;
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
	public GraphResult getInfo() throws ArangoDBException {
		return executeSync(getInfoRequest(), getInfoResponseDeserializer());
	}

	/**
	 * Get a graph from the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#get-a-graph">API Documentation</a>
	 * @return the definition content of this graph
	 */
	public CompletableFuture<GraphResult> getInfoAsync() {
		return executeAsync(getInfoRequest(), getInfoResponseDeserializer());
	}

	private Request getInfoRequest() {
		return new Request(db.name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_GHARIAL, name));
	}

	private ResponseDeserializer<GraphResult> getInfoResponseDeserializer() {
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
	public GraphResult addVertexCollection(final String name) throws ArangoDBException {
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
	public CompletableFuture<GraphResult> addVertexCollectionAsync(final String name) {
		return executeAsync(addVertexCollectionRequest(name), addVertexCollectionResponseDeserializer());
	}

	private Request addVertexCollectionRequest(final String name) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name(), ArangoDBConstants.VERTEX));
		request.setBody(serialize(OptionsBuilder.build(new VertexCollectionCreateOptions(), name)));
		return request;
	}

	private ResponseDeserializer<GraphResult> addVertexCollectionResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPH), GraphResult.class);
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
	public Collection<String> getEdgeCollections() throws ArangoDBException {
		return executeSync(getEdgeCollectionsRequest(), getEdgeCollectionsResponseDeserializer());
	}

	/**
	 * Lists all edge collections used in this graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-edge-definitions">API
	 *      Documentation</a>
	 * @return all edge collections within this graph
	 */
	public CompletableFuture<Collection<String>> getEdgeCollectionsAsync() {
		return executeAsync(getEdgeCollectionsRequest(), getEdgeCollectionsResponseDeserializer());
	}

	private Request getEdgeCollectionsRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, name, ArangoDBConstants.EDGE));
	}

	private ResponseDeserializer<Collection<String>> getEdgeCollectionsResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.COLLECTIONS),
			new Type<Collection<String>>() {
			}.getType());
	}
}

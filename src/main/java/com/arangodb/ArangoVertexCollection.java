package com.arangodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.VertexResult;
import com.arangodb.entity.VertexUpdateResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.VertexCreateOptions;
import com.arangodb.model.VertexDeleteOptions;
import com.arangodb.model.VertexReplaceOptions;
import com.arangodb.model.VertexUpdateOptions;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoVertexCollection extends ArangoExecuteable {

	private final ArangoGraph graph;
	private final String name;

	protected ArangoVertexCollection(final ArangoGraph graph, final String name) {
		super(graph.communication(), graph.vpack(), graph.vpackNull(), graph.vpackParser(), graph.documentCache(),
				graph.collectionCache());
		this.graph = graph;
		this.name = name;
	}

	/**
	 * Removes a vertex collection from the graph and optionally deletes the collection, if it is not used in any other
	 * graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#remove-vertex-collection">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	public void drop() throws ArangoDBException {
		executeSync(dropRequest(), Void.class);
	}

	/**
	 * Removes a vertex collection from the graph and optionally deletes the collection, if it is not used in any other
	 * graph
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#remove-vertex-collection">API
	 *      Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> dropAsync() {
		return executeAsync(dropRequest(), Void.class);
	}

	private Request dropRequest() {
		return new Request(graph.db().name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name));
	}

	/**
	 * Creates a new vertex in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
	 * @param value
	 *            A representation of a single vertex (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexResult insertVertex(final T value, final VertexCreateOptions options) throws ArangoDBException {
		return executeSync(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
	}

	/**
	 * Creates a new vertex in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
	 * @param value
	 *            A representation of a single vertex (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 */
	public <T> CompletableFuture<VertexResult> insertVertexAsync(final T value, final VertexCreateOptions options) {
		return executeAsync(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
	}

	private <T> Request insertVertexRequest(final T value, final VertexCreateOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name));
		final VertexCreateOptions params = (options != null ? options : new VertexCreateOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<VertexResult> insertVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.VERTEX);
			final VertexResult doc = deserialize(body, VertexResult.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.ID, doc.getId());
			values.put(DocumentField.Type.KEY, doc.getKey());
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		};
	}

	/**
	 * Fetches an existing vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#get-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the vertex identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getVertex(final String key, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		return executeSync(getVertexRequest(key, options), getVertexResponseDeserializer(type));
	}

	/**
	 * Fetches an existing vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#get-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the vertex identified by the key
	 */
	public <T> CompletableFuture<T> getVertexAsync(
		final String key,
		final Class<T> type,
		final DocumentReadOptions options) {
		return executeAsync(getVertexRequest(key, options), type);
	}

	private Request getVertexRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name, key));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<T> getVertexResponseDeserializer(final Class<T> type) {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.VERTEX), type);
	}

	/**
	 * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#replace-a-vertex">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexUpdateResult replaceVertex(final String key, final T value, final VertexReplaceOptions options)
			throws ArangoDBException {
		return executeSync(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer(value));
	}

	/**
	 * Replaces the vertex with key with the one in the body, provided there is such a vertex and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#replace-a-vertex">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 */
	public <T> CompletableFuture<VertexUpdateResult> replaceVertexAsync(
		final String key,
		final T value,
		final VertexReplaceOptions options) {
		return executeAsync(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer(value));
	}

	private <T> Request replaceVertexRequest(final String key, final T value, final VertexReplaceOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name, key));
		final VertexReplaceOptions params = (options != null ? options : new VertexReplaceOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<VertexUpdateResult> replaceVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.VERTEX);
			final VertexUpdateResult doc = deserialize(body, VertexUpdateResult.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		};
	}

	/**
	 * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#modify-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexUpdateResult updateVertex(final String key, final T value, final VertexUpdateOptions options)
			throws ArangoDBException {
		return executeSync(updateVertexRequest(key, value, options), updateVertexResponseDeserializer(value));
	}

	/**
	 * Partially updates the vertex identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#modify-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> CompletableFuture<VertexUpdateResult> updateVertexAsync(
		final String key,
		final T value,
		final VertexUpdateOptions options) throws ArangoDBException {
		return executeAsync(updateVertexRequest(key, value, options), updateVertexResponseDeserializer(value));
	}

	private <T> Request updateVertexRequest(final String key, final T value, final VertexUpdateOptions options) {
		final Request request;
		request = new Request(graph.db().name(), RequestType.PATCH,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name, key));
		final VertexUpdateOptions params = (options != null ? options : new VertexUpdateOptions());
		request.putParameter(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return request;
	}

	private <T> ResponseDeserializer<VertexUpdateResult> updateVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.VERTEX);
			return deserialize(body, VertexUpdateResult.class);
		};
	}

	/**
	 * Removes a vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param options
	 *            Additional options, can be null
	 * @throws ArangoDBException
	 */
	public void deleteVertex(final String key, final VertexDeleteOptions options) throws ArangoDBException {
		executeSync(deleteVertexRequest(key, options), Void.class);
	}

	/**
	 * Removes a vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param options
	 *            Additional options, can be null
	 */
	public CompletableFuture<Void> deleteVertexAsync(final String key, final VertexDeleteOptions options) {
		return executeAsync(deleteVertexRequest(key, options), Void.class);
	}

	private Request deleteVertexRequest(final String key, final VertexDeleteOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name, key));
		final VertexDeleteOptions params = (options != null ? options : new VertexDeleteOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

}

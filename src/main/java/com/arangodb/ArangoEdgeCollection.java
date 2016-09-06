package com.arangodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.EdgeResult;
import com.arangodb.entity.EdgeUpdateResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.EdgeCreateOptions;
import com.arangodb.model.EdgeReplaceOptions;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoEdgeCollection extends ArangoExecuteable {

	private final ArangoGraph graph;
	private final String name;

	protected ArangoEdgeCollection(final ArangoGraph graph, final String name) {
		super(graph.communication(), graph.vpack(), graph.vpackNull(), graph.vpackParser(), graph.documentCache(),
				graph.collectionCache());
		this.graph = graph;
		this.name = name;
	}

	/**
	 * Creates a new edge in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#create-an-edge">API Documentation</a>
	 * @param value
	 *            A representation of a single edge (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeResult insertEdge(final T value, final EdgeCreateOptions options) throws ArangoDBException {
		return executeSync(insertEdgeRequest(value, options), insertEdgeResponseDeserializer(value));
	}

	/**
	 * Creates a new edge in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#create-an-edge">API Documentation</a>
	 * @param value
	 *            A representation of a single edge (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeResult> insertEdgeAsync(final T value, final EdgeCreateOptions options) {
		return executeAsync(insertEdgeRequest(value, options), insertEdgeResponseDeserializer(value));
	}

	private <T> Request insertEdgeRequest(final T value, final EdgeCreateOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, name));
		final EdgeCreateOptions params = (options != null ? options : new EdgeCreateOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<EdgeResult> insertEdgeResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.EDGE);
			final EdgeResult doc = deserialize(body, EdgeResult.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.ID, doc.getId());
			values.put(DocumentField.Type.KEY, doc.getKey());
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		};
	}

	/**
	 * Fetches an existing edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#get-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the edge identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getEdge(final String key, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		return executeSync(getEdgeRequest(key, options), getEdgeResponseDeserializer(type));
	}

	/**
	 * Fetches an existing edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#get-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the edge identified by the key
	 */
	public <T> CompletableFuture<T> getEdgeAsync(
		final String key,
		final Class<T> type,
		final DocumentReadOptions options) {
		return executeAsync(getEdgeRequest(key, options), getEdgeResponseDeserializer(type));
	}

	private Request getEdgeRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, name, key));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<T> getEdgeResponseDeserializer(final Class<T> type) {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.EDGE), type);
	}

	/**
	 * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#replace-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeUpdateResult replaceEdge(final String key, final T value, final EdgeReplaceOptions options)
			throws ArangoDBException {
		return executeSync(replaceEdgeRequest(key, value, options), replaceEdgeResponseDeserializer(value));
	}

	/**
	 * Replaces the edge with key with the one in the body, provided there is such a edge and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#replace-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeUpdateResult> replaceEdgeAsync(
		final String key,
		final T value,
		final EdgeReplaceOptions options) {
		return executeAsync(replaceEdgeRequest(key, value, options), replaceEdgeResponseDeserializer(value));
	}

	private <T> Request replaceEdgeRequest(final String key, final T value, final EdgeReplaceOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, name, key));
		final EdgeReplaceOptions params = (options != null ? options : new EdgeReplaceOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<EdgeUpdateResult> replaceEdgeResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.EDGE);
			final EdgeUpdateResult doc = deserialize(body, EdgeUpdateResult.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		};
	}
}

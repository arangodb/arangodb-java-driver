package com.arangodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.DocumentField;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.DocumentCreateOptions;
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
	 * Adds a vertex to the given collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
	 * @param value
	 *            A representation of a single vertex (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> DocumentCreateResult<T> insertVertex(final T value, final DocumentCreateOptions options)
			throws ArangoDBException {
		return executeSync(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
	}

	/**
	 * Adds a vertex to the given collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
	 * @param value
	 *            A representation of a single vertex (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the vertex
	 * @return
	 */
	public <T> CompletableFuture<DocumentCreateResult<T>> insertVertexAsync(
		final T value,
		final DocumentCreateOptions options) {
		return executeAsync(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
	}

	private <T> Request insertVertexRequest(final T value, final DocumentCreateOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name));
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<DocumentCreateResult<T>> insertVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get().get(ArangoDBConstants.VERTEX);
			final DocumentCreateResult<T> doc = deserialize(body, DocumentCreateResult.class);
			final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
			if (newDoc.isObject()) {
				doc.setNew(deserialize(newDoc, value.getClass()));
			}
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.ID, doc.getId());
			values.put(DocumentField.Type.KEY, doc.getKey());
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		};
	}

}

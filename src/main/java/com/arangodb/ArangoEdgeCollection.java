package com.arangodb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.EdgeResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.EdgeCreateOptions;
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
}

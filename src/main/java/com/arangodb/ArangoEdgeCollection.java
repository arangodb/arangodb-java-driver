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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.EdgeCreateOptions;
import com.arangodb.model.EdgeDeleteOptions;
import com.arangodb.model.EdgeReplaceOptions;
import com.arangodb.model.EdgeUpdateOptions;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

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

	private String createDocumentHandle(final String key) {
		validateDocumentKey(key);
		return createPath(name, key);
	}

	/**
	 * Creates a new edge in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#create-an-edge">API Documentation</a>
	 * @param value
	 *            A representation of a single edge (POJO, VPackSlice or String for Json)
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeEntity insertEdge(final T value) throws ArangoDBException {
		return executeSync(insertEdgeRequest(value, new EdgeCreateOptions()), insertEdgeResponseDeserializer(value));
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
	public <T> EdgeEntity insertEdge(final T value, final EdgeCreateOptions options) throws ArangoDBException {
		return executeSync(insertEdgeRequest(value, options), insertEdgeResponseDeserializer(value));
	}

	/**
	 * Creates a new edge in the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#create-an-edge">API Documentation</a>
	 * @param value
	 *            A representation of a single edge (POJO, VPackSlice or String for Json)
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeEntity> insertEdgeAsync(final T value) {
		return executeAsync(insertEdgeRequest(value, new EdgeCreateOptions()), insertEdgeResponseDeserializer(value));
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
	public <T> CompletableFuture<EdgeEntity> insertEdgeAsync(final T value, final EdgeCreateOptions options) {
		return executeAsync(insertEdgeRequest(value, options), insertEdgeResponseDeserializer(value));
	}

	private <T> Request insertEdgeRequest(final T value, final EdgeCreateOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, name));
		final EdgeCreateOptions params = (options != null ? options : new EdgeCreateOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<EdgeEntity> insertEdgeResponseDeserializer(final T value) {
		return new ResponseDeserializer<EdgeEntity>() {
			@Override
			public EdgeEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.EDGE);
				final EdgeEntity doc = ArangoEdgeCollection.this.deserialize(body, EdgeEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.ID, doc.getId());
				values.put(DocumentField.Type.KEY, doc.getKey());
				values.put(DocumentField.Type.REV, doc.getRev());
				documentCache.setValues(value, values);
				return doc;
			}
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
	 * @return the edge identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getEdge(final String key, final Class<T> type) throws ArangoDBException {
		return executeSync(getEdgeRequest(key, new DocumentReadOptions()), getEdgeResponseDeserializer(type));
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
	 * @return the edge identified by the key
	 */
	public <T> CompletableFuture<T> getEdgeAsync(final String key, final Class<T> type) {
		return executeAsync(getEdgeRequest(key, new DocumentReadOptions()), getEdgeResponseDeserializer(type));
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
		final Request request = new Request(graph.db().name(), RequestType.GET, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, createDocumentHandle(key)));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<T> getEdgeResponseDeserializer(final Class<T> type) {
		return new ResponseDeserializer<T>() {
			@Override
			public T deserialize(final Response response) throws VPackException {
				return ArangoEdgeCollection.this.deserialize(response.getBody().get(ArangoDBConstants.EDGE), type);
			}
		};
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
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeUpdateEntity replaceEdge(final String key, final T value) throws ArangoDBException {
		return executeSync(replaceEdgeRequest(key, value, new EdgeReplaceOptions()),
			replaceEdgeResponseDeserializer(value));
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
	public <T> EdgeUpdateEntity replaceEdge(final String key, final T value, final EdgeReplaceOptions options)
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
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeUpdateEntity> replaceEdgeAsync(final String key, final T value) {
		return executeAsync(replaceEdgeRequest(key, value, new EdgeReplaceOptions()),
			replaceEdgeResponseDeserializer(value));
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
	public <T> CompletableFuture<EdgeUpdateEntity> replaceEdgeAsync(
		final String key,
		final T value,
		final EdgeReplaceOptions options) {
		return executeAsync(replaceEdgeRequest(key, value, options), replaceEdgeResponseDeserializer(value));
	}

	private <T> Request replaceEdgeRequest(final String key, final T value, final EdgeReplaceOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.PUT, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, createDocumentHandle(key)));
		final EdgeReplaceOptions params = (options != null ? options : new EdgeReplaceOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<EdgeUpdateEntity> replaceEdgeResponseDeserializer(final T value) {
		return new ResponseDeserializer<EdgeUpdateEntity>() {
			@Override
			public EdgeUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.EDGE);
				final EdgeUpdateEntity doc = ArangoEdgeCollection.this.deserialize(body, EdgeUpdateEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				documentCache.setValues(value, values);
				return doc;
			}
		};
	}

	/**
	 * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeUpdateEntity updateEdge(final String key, final T value) throws ArangoDBException {
		return executeSync(updateEdgeRequest(key, value, new EdgeUpdateOptions()),
			updateEdgeResponseDeserializer(value));
	}

	/**
	 * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 * @throws ArangoDBException
	 */
	public <T> EdgeUpdateEntity updateEdge(final String key, final T value, final EdgeUpdateOptions options)
			throws ArangoDBException {
		return executeSync(updateEdgeRequest(key, value, options), updateEdgeResponseDeserializer(value));
	}

	/**
	 * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeUpdateEntity> updateEdgeAsync(final String key, final T value) {
		return executeAsync(updateEdgeRequest(key, value, new EdgeUpdateOptions()),
			updateEdgeResponseDeserializer(value));
	}

	/**
	 * Partially updates the edge identified by document-key. The value must contain a document with the attributes to
	 * patch (the patch document). All attributes from the patch document will be added to the existing document if they
	 * do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param type
	 *            The type of the edge-document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the edge
	 */
	public <T> CompletableFuture<EdgeUpdateEntity> updateEdgeAsync(
		final String key,
		final T value,
		final EdgeUpdateOptions options) {
		return executeAsync(updateEdgeRequest(key, value, options), updateEdgeResponseDeserializer(value));
	}

	private <T> Request updateEdgeRequest(final String key, final T value, final EdgeUpdateOptions options) {
		final Request request;
		request = new Request(graph.db().name(), RequestType.PATCH, createPath(ArangoDBConstants.PATH_API_GHARIAL,
			graph.name(), ArangoDBConstants.EDGE, createDocumentHandle(key)));
		final EdgeUpdateOptions params = (options != null ? options : new EdgeUpdateOptions());
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return request;
	}

	private <T> ResponseDeserializer<EdgeUpdateEntity> updateEdgeResponseDeserializer(final T value) {
		return new ResponseDeserializer<EdgeUpdateEntity>() {
			@Override
			public EdgeUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.EDGE);
				return ArangoEdgeCollection.this.deserialize(body, EdgeUpdateEntity.class);
			}
		};
	}

	/**
	 * Removes a edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#remove-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @throws ArangoDBException
	 */
	public void deleteEdge(final String key) throws ArangoDBException {
		executeSync(deleteEdgeRequest(key, new EdgeDeleteOptions()), Void.class);
	}

	/**
	 * Removes a edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#remove-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param options
	 *            Additional options, can be null
	 * @throws ArangoDBException
	 */
	public void deleteEdge(final String key, final EdgeDeleteOptions options) throws ArangoDBException {
		executeSync(deleteEdgeRequest(key, options), Void.class);
	}

	/**
	 * Removes a edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#remove-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 */
	public CompletableFuture<Void> deleteEdgeAsync(final String key) {
		return executeAsync(deleteEdgeRequest(key, new EdgeDeleteOptions()), Void.class);
	}

	/**
	 * Removes a edge
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#remove-an-edge">API Documentation</a>
	 * @param key
	 *            The key of the edge
	 * @param options
	 *            Additional options, can be null
	 */
	public CompletableFuture<Void> deleteEdgeAsync(final String key, final EdgeDeleteOptions options) {
		return executeAsync(deleteEdgeRequest(key, options), Void.class);
	}

	private Request deleteEdgeRequest(final String key, final EdgeDeleteOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.DELETE, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.EDGE, createDocumentHandle(key)));
		final EdgeDeleteOptions params = (options != null ? options : new EdgeDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

}

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

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.VertexCreateOptions;
import com.arangodb.model.VertexDeleteOptions;
import com.arangodb.model.VertexReplaceOptions;
import com.arangodb.model.VertexUpdateOptions;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

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

	private String createDocumentHandle(final String key) {
		validateDocumentKey(key);
		return createPath(name, key);
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
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexEntity insertVertex(final T value) throws ArangoDBException {
		return executeSync(insertVertexRequest(value, new VertexCreateOptions()),
			insertVertexResponseDeserializer(value));
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
	public <T> VertexEntity insertVertex(final T value, final VertexCreateOptions options) throws ArangoDBException {
		return executeSync(insertVertexRequest(value, options), insertVertexResponseDeserializer(value));
	}

	private <T> Request insertVertexRequest(final T value, final VertexCreateOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, name));
		final VertexCreateOptions params = (options != null ? options : new VertexCreateOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<VertexEntity> insertVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexEntity>() {
			@Override
			public VertexEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.VERTEX);
				final VertexEntity doc = ArangoVertexCollection.this.deserialize(body, VertexEntity.class);
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
	 * Fetches an existing vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#get-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @param type
	 *            The type of the vertex-document (POJO class, VPackSlice or String for Json)
	 * @return the vertex identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getVertex(final String key, final Class<T> type) throws ArangoDBException {
		return executeSync(getVertexRequest(key, new DocumentReadOptions()), getVertexResponseDeserializer(type));
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

	private Request getVertexRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.GET, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, createDocumentHandle(key)));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<T> getVertexResponseDeserializer(final Class<T> type) {
		return new ResponseDeserializer<T>() {
			@Override
			public T deserialize(final Response response) throws VPackException {
				return ArangoVertexCollection.this.deserialize(response.getBody().get(ArangoDBConstants.VERTEX), type);
			}
		};
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
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexUpdateEntity replaceVertex(final String key, final T value) throws ArangoDBException {
		return executeSync(replaceVertexRequest(key, value, new VertexReplaceOptions()),
			replaceVertexResponseDeserializer(value));
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
	public <T> VertexUpdateEntity replaceVertex(final String key, final T value, final VertexReplaceOptions options)
			throws ArangoDBException {
		return executeSync(replaceVertexRequest(key, value, options), replaceVertexResponseDeserializer(value));
	}

	private <T> Request replaceVertexRequest(final String key, final T value, final VertexReplaceOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.PUT, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, createDocumentHandle(key)));
		final VertexReplaceOptions params = (options != null ? options : new VertexReplaceOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<VertexUpdateEntity> replaceVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexUpdateEntity>() {
			@Override
			public VertexUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.VERTEX);
				final VertexUpdateEntity doc = ArangoVertexCollection.this.deserialize(body, VertexUpdateEntity.class);
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				documentCache.setValues(value, values);
				return doc;
			}
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
	 * @return information about the vertex
	 * @throws ArangoDBException
	 */
	public <T> VertexUpdateEntity updateVertex(final String key, final T value) throws ArangoDBException {
		return executeSync(updateVertexRequest(key, value, new VertexUpdateOptions()),
			updateVertexResponseDeserializer(value));
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
	public <T> VertexUpdateEntity updateVertex(final String key, final T value, final VertexUpdateOptions options)
			throws ArangoDBException {
		return executeSync(updateVertexRequest(key, value, options), updateVertexResponseDeserializer(value));
	}

	private <T> Request updateVertexRequest(final String key, final T value, final VertexUpdateOptions options) {
		final Request request;
		request = new Request(graph.db().name(), RequestType.PATCH, createPath(ArangoDBConstants.PATH_API_GHARIAL,
			graph.name(), ArangoDBConstants.VERTEX, createDocumentHandle(key)));
		final VertexUpdateOptions params = (options != null ? options : new VertexUpdateOptions());
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return request;
	}

	private <T> ResponseDeserializer<VertexUpdateEntity> updateVertexResponseDeserializer(final T value) {
		return new ResponseDeserializer<VertexUpdateEntity>() {
			@Override
			public VertexUpdateEntity deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody().get(ArangoDBConstants.VERTEX);
				return ArangoVertexCollection.this.deserialize(body, VertexUpdateEntity.class);
			}
		};
	}

	/**
	 * Removes a vertex
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
	 * @param key
	 *            The key of the vertex
	 * @throws ArangoDBException
	 */
	public void deleteVertex(final String key) throws ArangoDBException {
		executeSync(deleteVertexRequest(key, new VertexDeleteOptions()), Void.class);
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

	private Request deleteVertexRequest(final String key, final VertexDeleteOptions options) {
		final Request request = new Request(graph.db().name(), RequestType.DELETE, createPath(
			ArangoDBConstants.PATH_API_GHARIAL, graph.name(), ArangoDBConstants.VERTEX, createDocumentHandle(key)));
		final VertexDeleteOptions params = (options != null ? options : new VertexDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

}

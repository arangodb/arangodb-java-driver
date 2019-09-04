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

package com.arangodb.internal;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.VertexEntity;
import com.arangodb.entity.VertexUpdateEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
 *
 */
public abstract class InternalArangoVertexCollection<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, G extends InternalArangoGraph<A, D, E>, E extends ArangoExecutor>
		extends ArangoExecuteable<E> {

	private static final String PATH_API_GHARIAL = "/_api/gharial";
	private static final String VERTEX = "vertex";

	private final G graph;
	private final String name;

	protected InternalArangoVertexCollection(final G graph, final String name) {
		super(graph.executor, graph.util, graph.context);
		this.graph = graph;
		this.name = name;
	}

	public G graph() {
		return graph;
	}

	public String name() {
		return name;
	}

	protected Request dropRequest() {
		return request(graph.db().name(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), VERTEX, name);
	}

	protected <T> Request insertVertexRequest(final T value, final VertexCreateOptions options) {
		final Request request = request(graph.db().name(), RequestType.POST, PATH_API_GHARIAL, graph.name(), VERTEX,
			name);
		final VertexCreateOptions params = (options != null ? options : new VertexCreateOptions());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<VertexEntity> insertVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get(VERTEX);
			final VertexEntity doc = util().deserialize(body, VertexEntity.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.ID, doc.getId());
			values.put(DocumentField.Type.KEY, doc.getKey());
			values.put(DocumentField.Type.REV, doc.getRev());
			executor.documentCache().setValues(value, values);
			return doc;
		};
	}

	protected Request getVertexRequest(final String key, final GraphDocumentReadOptions options) {
		final Request request = request(graph.db().name(), RequestType.GET, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final GraphDocumentReadOptions params = (options != null ? options : new GraphDocumentReadOptions());
		request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		if (params.getAllowDirtyRead() == Boolean.TRUE) {
			RequestUtils.allowDirtyRead(request);
		}
		return request;
	}

	protected <T> ResponseDeserializer<T> getVertexResponseDeserializer(final Class<T> type) {
		return response -> util(Serializer.CUSTOM).deserialize(response.getBody().get(VERTEX), type);
	}

	protected <T> Request replaceVertexRequest(final String key, final T value, final VertexReplaceOptions options) {
		final Request request = request(graph.db().name(), RequestType.PUT, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexReplaceOptions params = (options != null ? options : new VertexReplaceOptions());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<VertexUpdateEntity> replaceVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get(VERTEX);
			final VertexUpdateEntity doc = util().deserialize(body, VertexUpdateEntity.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.REV, doc.getRev());
			executor.documentCache().setValues(value, values);
			return doc;
		};
	}

	protected <T> Request updateVertexRequest(final String key, final T value, final VertexUpdateOptions options) {
		final Request request;
		request = request(graph.db().name(), RequestType.PATCH, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexUpdateOptions params = (options != null ? options : new VertexUpdateOptions());
		request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.setBody(
			util(Serializer.CUSTOM).serialize(value, new ArangoSerializer.Options().serializeNullValues(true)));
		return request;
	}

	protected <T> ResponseDeserializer<VertexUpdateEntity> updateVertexResponseDeserializer(final T value) {
		return response -> {
			final VPackSlice body = response.getBody().get(VERTEX);
			final VertexUpdateEntity doc = util().deserialize(body, VertexUpdateEntity.class);
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.REV, doc.getRev());
			executor.documentCache().setValues(value, values);
			return doc;
		};
	}

	protected Request deleteVertexRequest(final String key, final VertexDeleteOptions options) {
		final Request request = request(graph.db().name(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), VERTEX,
			DocumentUtil.createDocumentHandle(name, key));
		final VertexDeleteOptions params = (options != null ? options : new VertexDeleteOptions());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		return request;
	}

}

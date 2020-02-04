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
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
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
 */
public abstract class InternalArangoEdgeCollection<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, G extends InternalArangoGraph<A, D, E>, E extends ArangoExecutor>
        extends ArangoExecuteable<E> {

    private static final String PATH_API_GHARIAL = "/_api/gharial";
    private static final String EDGE = "edge";

    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final G graph;
    private final String name;

    protected InternalArangoEdgeCollection(final G graph, final String name) {
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

    protected <T> Request insertEdgeRequest(final T value, final EdgeCreateOptions options) {
        final Request request = request(graph.db().name(), RequestType.POST, PATH_API_GHARIAL, graph.name(), EDGE,
                name);
        final EdgeCreateOptions params = (options != null ? options : new EdgeCreateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.setBody(util(Serializer.CUSTOM).serialize(value));
        return request;
    }

    protected <T> ResponseDeserializer<EdgeEntity> insertEdgeResponseDeserializer(final T value) {
        return response -> {
            final VPackSlice body = response.getBody().get(EDGE);
            final EdgeEntity doc = util().deserialize(body, EdgeEntity.class);
            final Map<DocumentField.Type, String> values = new HashMap<>();
            values.put(DocumentField.Type.ID, doc.getId());
            values.put(DocumentField.Type.KEY, doc.getKey());
            values.put(DocumentField.Type.REV, doc.getRev());
            executor.documentCache().setValues(value, values);
            return doc;
        };
    }

    protected Request getEdgeRequest(final String key, final GraphDocumentReadOptions options) {
        final Request request = request(graph.db().name(), RequestType.GET, PATH_API_GHARIAL, graph.name(), EDGE,
                DocumentUtil.createDocumentHandle(name, key));
        final GraphDocumentReadOptions params = (options != null ? options : new GraphDocumentReadOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        if (params.getAllowDirtyRead() == Boolean.TRUE) {
            RequestUtils.allowDirtyRead(request);
        }
        return request;
    }

    protected <T> ResponseDeserializer<T> getEdgeResponseDeserializer(final Class<T> type) {
        return response -> util(Serializer.CUSTOM).deserialize(response.getBody().get(EDGE), type);
    }

    protected <T> Request replaceEdgeRequest(final String key, final T value, final EdgeReplaceOptions options) {
        final Request request = request(graph.db().name(), RequestType.PUT, PATH_API_GHARIAL, graph.name(), EDGE,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeReplaceOptions params = (options != null ? options : new EdgeReplaceOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(util(Serializer.CUSTOM).serialize(value));
        return request;
    }

    protected <T> ResponseDeserializer<EdgeUpdateEntity> replaceEdgeResponseDeserializer(final T value) {
        return response -> {
            final VPackSlice body = response.getBody().get(EDGE);
            final EdgeUpdateEntity doc = util().deserialize(body, EdgeUpdateEntity.class);
            final Map<DocumentField.Type, String> values = new HashMap<>();
            values.put(DocumentField.Type.REV, doc.getRev());
            executor.documentCache().setValues(value, values);
            return doc;
        };
    }

    protected <T> Request updateEdgeRequest(final String key, final T value, final EdgeUpdateOptions options) {
        final Request request;
        request = request(graph.db().name(), RequestType.PATCH, PATH_API_GHARIAL, graph.name(), EDGE,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeUpdateOptions params = (options != null ? options : new EdgeUpdateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(
                util(Serializer.CUSTOM).serialize(value, new ArangoSerializer.Options().serializeNullValues(true)));
        return request;
    }

    protected <T> ResponseDeserializer<EdgeUpdateEntity> updateEdgeResponseDeserializer(final T value) {
        return response -> {
            final VPackSlice body = response.getBody().get(EDGE);
            final EdgeUpdateEntity doc = util().deserialize(body, EdgeUpdateEntity.class);
            final Map<DocumentField.Type, String> values = new HashMap<>();
            values.put(DocumentField.Type.REV, doc.getRev());
            executor.documentCache().setValues(value, values);
            return doc;
        };
    }

    protected Request deleteEdgeRequest(final String key, final EdgeDeleteOptions options) {
        final Request request = request(graph.db().name(), RequestType.DELETE, PATH_API_GHARIAL, graph.name(), EDGE,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeDeleteOptions params = (options != null ? options : new EdgeDeleteOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        return request;
    }

}

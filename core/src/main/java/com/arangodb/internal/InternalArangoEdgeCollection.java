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

import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EdgeUpdateEntity;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;

/**
 * @author Mark Vollmary
 */
public abstract class InternalArangoEdgeCollection extends ArangoExecuteable {

    private static final String PATH_API_GHARIAL = "/_api/gharial";
    private static final String TRANSACTION_ID = "x-arango-trx-id";
    private static final String EDGE_PATH = "edge";
    private static final String EDGE_JSON_POINTER = "/edge";

    private final String dbName;
    private final String graphName;
    private final String name;

    protected InternalArangoEdgeCollection(final ArangoExecuteable executeable,
                                           final String dbName,
                                           final String graphName,
                                           final String name) {
        super(executeable);
        this.dbName = dbName;
        this.graphName = graphName;
        this.name = name;
    }

    public String name() {
        return name;
    }

    protected InternalRequest removeEdgeDefinitionRequest(final EdgeCollectionDropOptions options) {
        return request(dbName, RequestType.DELETE, PATH_API_GHARIAL, graphName, "edge", name)
                .putQueryParam("waitForSync", options.getWaitForSync())
                .putQueryParam("dropCollections", options.getDropCollections());
    }

    protected <T> InternalRequest insertEdgeRequest(final T value, final EdgeCreateOptions options) {
        final InternalRequest request = request(dbName, RequestType.POST, PATH_API_GHARIAL, graphName, EDGE_PATH,
                name);
        final EdgeCreateOptions params = (options != null ? options : new EdgeCreateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<EdgeEntity> insertEdgeResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), EDGE_JSON_POINTER, EdgeEntity.class);
    }

    protected InternalRequest getEdgeRequest(final String key, final GraphDocumentReadOptions options) {
        final InternalRequest request = request(dbName, RequestType.GET, PATH_API_GHARIAL, graphName, EDGE_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final GraphDocumentReadOptions params = (options != null ? options : new GraphDocumentReadOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        if (Boolean.TRUE.equals(params.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        return request;
    }

    protected <T> ResponseDeserializer<T> getEdgeResponseDeserializer(final Class<T> type) {
        return response -> getSerde().deserializeUserData(getSerde().extract(response.getBody(), EDGE_JSON_POINTER), type);
    }

    protected <T> InternalRequest replaceEdgeRequest(final String key, final T value, final EdgeReplaceOptions options) {
        final InternalRequest request = request(dbName, RequestType.PUT, PATH_API_GHARIAL, graphName, EDGE_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeReplaceOptions params = (options != null ? options : new EdgeReplaceOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<EdgeUpdateEntity> replaceEdgeResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), EDGE_JSON_POINTER, EdgeUpdateEntity.class);
    }

    protected <T> InternalRequest updateEdgeRequest(final String key, final T value, final EdgeUpdateOptions options) {
        final InternalRequest request;
        request = request(dbName, RequestType.PATCH, PATH_API_GHARIAL, graphName, EDGE_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeUpdateOptions params = (options != null ? options : new EdgeUpdateOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        request.setBody(getSerde().serializeUserData(value));
        return request;
    }

    protected ResponseDeserializer<EdgeUpdateEntity> updateEdgeResponseDeserializer() {
        return response -> getSerde().deserialize(response.getBody(), EDGE_JSON_POINTER, EdgeUpdateEntity.class);
    }

    protected InternalRequest deleteEdgeRequest(final String key, final EdgeDeleteOptions options) {
        final InternalRequest request = request(dbName, RequestType.DELETE, PATH_API_GHARIAL, graphName, EDGE_PATH,
                DocumentUtil.createDocumentHandle(name, key));
        final EdgeDeleteOptions params = (options != null ? options : new EdgeDeleteOptions());
        request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
        request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
        request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
        return request;
    }

}

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

import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.*;

import java.util.Collection;
import java.util.Map;

import static com.arangodb.internal.serde.SerdeUtils.constructListType;
import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoDatabase extends ArangoExecuteable {

    protected static final String PATH_API_DATABASE = "/_api/database";
    private static final String PATH_API_VERSION = "/_api/version";
    private static final String PATH_API_ENGINE = "/_api/engine";
    private static final String PATH_API_CURSOR = "/_api/cursor";
    private static final String PATH_API_TRANSACTION = "/_api/transaction";
    private static final String PATH_API_BEGIN_STREAM_TRANSACTION = "/_api/transaction/begin";
    private static final String PATH_API_AQLFUNCTION = "/_api/aqlfunction";
    private static final String PATH_API_EXPLAIN = "/_api/explain";
    private static final String PATH_API_QUERY = "/_api/query";
    private static final String PATH_API_QUERY_CACHE = "/_api/query-cache";
    private static final String PATH_API_QUERY_CACHE_PROPERTIES = "/_api/query-cache/properties";
    private static final String PATH_API_QUERY_PROPERTIES = "/_api/query/properties";
    private static final String PATH_API_QUERY_CURRENT = "/_api/query/current";
    private static final String PATH_API_QUERY_SLOW = "/_api/query/slow";
    private static final String PATH_API_ADMIN_ROUTING_RELOAD = "/_admin/routing/reload";
    private static final String PATH_API_USER = "/_api/user";

    private static final String TRANSACTION_ID = "x-arango-trx-id";

    private final String name;

    protected InternalArangoDatabase(final ArangoExecuteable executeable, final String name) {
        super(executeable);
        this.name = name;
    }

    public String name() {
        return name;
    }

    protected ResponseDeserializer<Collection<String>> getDatabaseResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(String.class));
    }

    protected InternalRequest getAccessibleDatabasesRequest() {
        return request(name, RequestType.GET, PATH_API_DATABASE, "user");
    }

    protected InternalRequest getVersionRequest() {
        return request(name, RequestType.GET, PATH_API_VERSION);
    }

    protected InternalRequest getEngineRequest() {
        return request(name, RequestType.GET, PATH_API_ENGINE);
    }

    protected InternalRequest createCollectionRequest(final String name, final CollectionCreateOptions options) {

        byte[] body = getSerde().serialize(OptionsBuilder.build(options != null ? options :
                new CollectionCreateOptions(), name));

        return request(this.name, RequestType.POST, InternalArangoCollection.PATH_API_COLLECTION).setBody(body);
    }

    protected InternalRequest getCollectionsRequest(final CollectionsReadOptions options) {
        final InternalRequest request;
        request = request(name, RequestType.GET, InternalArangoCollection.PATH_API_COLLECTION);
        final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
        request.putQueryParam("excludeSystem", params.getExcludeSystem());
        return request;
    }

    protected ResponseDeserializer<Collection<CollectionEntity>> getCollectionsResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(CollectionEntity.class));
    }

    protected InternalRequest dropRequest() {
        return request(ArangoRequestParam.SYSTEM, RequestType.DELETE, PATH_API_DATABASE, name);
    }

    protected ResponseDeserializer<Boolean> createDropResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Boolean.class);
    }

    protected InternalRequest grantAccessRequest(final String user, final Permissions permissions) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                name).setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected InternalRequest resetAccessRequest(final String user) {
        return request(ArangoRequestParam.SYSTEM, RequestType.DELETE, PATH_API_USER, user, ArangoRequestParam.DATABASE,
                name);
    }

    protected InternalRequest updateUserDefaultCollectionAccessRequest(final String user, final Permissions permissions) {
        return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE, name
                , "*").setBody(getSerde().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
    }

    protected InternalRequest getPermissionsRequest(final String user) {
        return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE, name);
    }

    protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                Permissions.class);
    }

    protected InternalRequest queryRequest(final String query, final Map<String, Object> bindVars,
                                           final AqlQueryOptions options) {
        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();
        final InternalRequest request = request(name, RequestType.POST, PATH_API_CURSOR)
                .setBody(getSerde().serialize(OptionsBuilder.build(opt, query, bindVars)));
        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected InternalRequest queryNextRequest(String id, AqlQueryOptions options, String nextBatchId) {
        final InternalRequest request = request(name, RequestType.POST, PATH_API_CURSOR, id, nextBatchId);
        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();
        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected InternalRequest queryCloseRequest(final String id, final AqlQueryOptions options) {
        final InternalRequest request = request(name, RequestType.DELETE, PATH_API_CURSOR, id);
        final AqlQueryOptions opt = options != null ? options : new AqlQueryOptions();
        if (Boolean.TRUE.equals(opt.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(request);
        }
        request.putHeaderParam(TRANSACTION_ID, opt.getStreamTransactionId());
        return request;
    }

    protected InternalRequest explainQueryRequest(final String query, final Map<String, Object> bindVars,
                                                  final AqlQueryExplainOptions options) {
        final AqlQueryExplainOptions opt = options != null ? options : new AqlQueryExplainOptions();
        return request(name, RequestType.POST, PATH_API_EXPLAIN)
                .setBody(getSerde().serialize(OptionsBuilder.build(opt, query, bindVars)));
    }

    protected InternalRequest parseQueryRequest(final String query) {
        return request(name, RequestType.POST, PATH_API_QUERY).setBody(getSerde().serialize(OptionsBuilder.build(new AqlQueryParseOptions(), query)));
    }

    protected InternalRequest clearQueryCacheRequest() {
        return request(name, RequestType.DELETE, PATH_API_QUERY_CACHE);
    }

    protected InternalRequest getQueryCachePropertiesRequest() {
        return request(name, RequestType.GET, PATH_API_QUERY_CACHE_PROPERTIES);
    }

    protected InternalRequest setQueryCachePropertiesRequest(final QueryCachePropertiesEntity properties) {
        return request(name, RequestType.PUT, PATH_API_QUERY_CACHE_PROPERTIES).setBody(getSerde().serialize(properties));
    }

    protected InternalRequest getQueryTrackingPropertiesRequest() {
        return request(name, RequestType.GET, PATH_API_QUERY_PROPERTIES);
    }

    protected InternalRequest setQueryTrackingPropertiesRequest(final QueryTrackingPropertiesEntity properties) {
        return request(name, RequestType.PUT, PATH_API_QUERY_PROPERTIES).setBody(getSerde().serialize(properties));
    }

    protected InternalRequest getCurrentlyRunningQueriesRequest() {
        return request(name, RequestType.GET, PATH_API_QUERY_CURRENT);
    }

    protected InternalRequest getSlowQueriesRequest() {
        return request(name, RequestType.GET, PATH_API_QUERY_SLOW);
    }

    protected InternalRequest clearSlowQueriesRequest() {
        return request(name, RequestType.DELETE, PATH_API_QUERY_SLOW);
    }

    protected InternalRequest killQueryRequest(final String id) {
        return request(name, RequestType.DELETE, PATH_API_QUERY, id);
    }

    protected InternalRequest createAqlFunctionRequest(final String name, final String code,
                                                       final AqlFunctionCreateOptions options) {
        return request(this.name, RequestType.POST, PATH_API_AQLFUNCTION).setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options : new AqlFunctionCreateOptions(), name, code)));
    }

    protected InternalRequest deleteAqlFunctionRequest(final String name, final AqlFunctionDeleteOptions options) {
        final InternalRequest request = request(this.name, RequestType.DELETE, PATH_API_AQLFUNCTION, name);
        final AqlFunctionDeleteOptions params = options != null ? options : new AqlFunctionDeleteOptions();
        request.putQueryParam("group", params.getGroup());
        return request;
    }

    public <T> ResponseDeserializer<CursorEntity<T>> cursorEntityDeserializer(final Class<T> type) {
        return (response) -> {
            CursorEntity<T> e = getSerde().deserialize(response.getBody(), constructParametricType(CursorEntity.class, type));
            boolean potentialDirtyRead = Boolean.parseBoolean(response.getMeta("X-Arango-Potential-Dirty-Read"));
            e.setPotentialDirtyRead(potentialDirtyRead);
            return e;
        };
    }

    protected ResponseDeserializer<Integer> deleteAqlFunctionResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), "/deletedCount", Integer.class);
    }

    protected InternalRequest getAqlFunctionsRequest(final AqlFunctionGetOptions options) {
        final InternalRequest request = request(name, RequestType.GET, PATH_API_AQLFUNCTION);
        final AqlFunctionGetOptions params = options != null ? options : new AqlFunctionGetOptions();
        request.putQueryParam("namespace", params.getNamespace());
        return request;
    }

    protected ResponseDeserializer<Collection<AqlFunctionEntity>> getAqlFunctionsResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(AqlFunctionEntity.class));
    }

    protected InternalRequest createGraphRequest(final String name, final Iterable<EdgeDefinition> edgeDefinitions,
                                                 final GraphCreateOptions options) {
        GraphCreateOptions opts = options != null ? options : new GraphCreateOptions();
        return request(this.name, RequestType.POST, InternalArangoGraph.PATH_API_GHARIAL)
                .putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, opts.getWaitForSync())
                .setBody(getSerde().serialize(OptionsBuilder.build(opts, name, edgeDefinitions)));
    }

    protected ResponseDeserializer<GraphEntity> createGraphResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), "/graph", GraphEntity.class);
    }

    protected InternalRequest getGraphsRequest() {
        return request(name, RequestType.GET, InternalArangoGraph.PATH_API_GHARIAL);
    }

    protected ResponseDeserializer<Collection<GraphEntity>> getGraphsResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), "/graphs",
                constructListType(GraphEntity.class));
    }

    protected InternalRequest transactionRequest(final String action, final TransactionOptions options) {
        return request(name, RequestType.POST, PATH_API_TRANSACTION).setBody(getSerde().serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
    }

    protected <T> ResponseDeserializer<T> transactionResponseDeserializer(final Class<T> type) {
        return (response) -> {
            byte[] userContent = getSerde().extract(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER);
            return getSerde().deserializeUserData(userContent, type);
        };
    }

    protected InternalRequest beginStreamTransactionRequest(final StreamTransactionOptions options) {
        StreamTransactionOptions opts = options != null ? options : new StreamTransactionOptions();
        InternalRequest r = request(name, RequestType.POST, PATH_API_BEGIN_STREAM_TRANSACTION).setBody(getSerde().serialize(opts));
        if(Boolean.TRUE.equals(opts.getAllowDirtyRead())) {
            RequestUtils.allowDirtyRead(r);
        }
        return r;
    }

    protected InternalRequest abortStreamTransactionRequest(String id) {
        return request(name, RequestType.DELETE, PATH_API_TRANSACTION, id);
    }

    protected InternalRequest getStreamTransactionsRequest() {
        return request(name, RequestType.GET, PATH_API_TRANSACTION);
    }

    protected InternalRequest getStreamTransactionRequest(String id) {
        return request(name, RequestType.GET, PATH_API_TRANSACTION, id);
    }

    protected ResponseDeserializer<Collection<TransactionEntity>> transactionsResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), "/transactions",
                constructListType(TransactionEntity.class));
    }

    protected InternalRequest commitStreamTransactionRequest(String id) {
        return request(name, RequestType.PUT, PATH_API_TRANSACTION, id);
    }

    protected ResponseDeserializer<StreamTransactionEntity> streamTransactionResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                StreamTransactionEntity.class);
    }

    protected InternalRequest getInfoRequest() {
        return request(name, RequestType.GET, PATH_API_DATABASE, "current");
    }

    protected ResponseDeserializer<DatabaseEntity> getInfoResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                DatabaseEntity.class);
    }

    protected InternalRequest reloadRoutingRequest() {
        return request(name, RequestType.POST, PATH_API_ADMIN_ROUTING_RELOAD);
    }

    protected InternalRequest getViewsRequest() {
        return request(name, RequestType.GET, InternalArangoView.PATH_API_VIEW);
    }

    protected ResponseDeserializer<Collection<ViewEntity>> getViewsResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(ViewEntity.class));
    }

    protected InternalRequest createViewRequest(final String name, final ViewType type) {
        return request(this.name, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(OptionsBuilder.build(new ViewCreateOptions(), name, type)));
    }

    protected InternalRequest createArangoSearchRequest(final String name, final ArangoSearchCreateOptions options) {
        return request(this.name, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(ArangoSearchOptionsBuilder.build(options != null ? options : new ArangoSearchCreateOptions(), name)));
    }

    protected InternalRequest createSearchAliasRequest(final String name, final SearchAliasCreateOptions options) {
        return request(this.name, RequestType.POST, InternalArangoView.PATH_API_VIEW).setBody(getSerde().serialize(
                SearchAliasOptionsBuilder.build(options != null ? options : new SearchAliasCreateOptions(), name)));
    }

    protected InternalRequest getAnalyzerRequest(final String name) {
        return request(this.name, RequestType.GET, InternalArangoView.PATH_API_ANALYZER, name);
    }

    protected InternalRequest getAnalyzersRequest() {
        return request(name, RequestType.GET, InternalArangoView.PATH_API_ANALYZER);
    }

    protected ResponseDeserializer<Collection<SearchAnalyzer>> getSearchAnalyzersResponseDeserializer() {
        return (response) -> getSerde().deserialize(response.getBody(), ArangoResponseField.RESULT_JSON_POINTER,
                constructListType(SearchAnalyzer.class));
    }

    protected InternalRequest createAnalyzerRequest(final SearchAnalyzer options) {
        return request(name, RequestType.POST, InternalArangoView.PATH_API_ANALYZER).setBody(getSerde().serialize(options));
    }

    protected InternalRequest deleteAnalyzerRequest(final String name, final AnalyzerDeleteOptions options) {
        InternalRequest request = request(this.name, RequestType.DELETE, InternalArangoView.PATH_API_ANALYZER, name);
        request.putQueryParam("force", options != null ? options.getForce() : null);
        return request;
    }

}

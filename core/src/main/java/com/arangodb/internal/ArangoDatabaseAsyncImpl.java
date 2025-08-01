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

import com.arangodb.*;
import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.internal.cursor.ArangoCursorAsyncImpl;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.SearchAliasCreateOptions;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.arangodb.internal.ArangoErrors.ERROR_ARANGO_DATABASE_NOT_FOUND;
import static com.arangodb.internal.ArangoErrors.matches;
import static com.arangodb.internal.serde.SerdeUtils.constructListType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoDatabaseAsyncImpl extends InternalArangoDatabase implements ArangoDatabaseAsync {

    private final ArangoDBAsync arangoDB;

    protected ArangoDatabaseAsyncImpl(final ArangoDBAsyncImpl arangoDB, final String name) {
        super(arangoDB, name);
        this.arangoDB = arangoDB;
    }

    @Override
    public ArangoDBAsync arango() {
        return arangoDB;
    }

    @Override
    public CompletableFuture<ArangoDBVersion> getVersion() {
        return executorAsync().execute(this::getVersionRequest, ArangoDBVersion.class);
    }

    @Override
    public CompletableFuture<ArangoDBEngine> getEngine() {
        return executorAsync().execute(this::getEngineRequest, ArangoDBEngine.class);
    }

    @Override
    public CompletableFuture<Boolean> exists() {
        return getInfo()
                .thenApply(Objects::nonNull)
                .exceptionally(err -> {
                    Throwable e = err instanceof CompletionException ? err.getCause() : err;
                    if (e instanceof ArangoDBException) {
                        ArangoDBException aEx = (ArangoDBException) e;
                        if (matches(aEx, 404, ERROR_ARANGO_DATABASE_NOT_FOUND)) {
                            return false;
                        }
                    }
                    throw ArangoDBException.of(e);
                });
    }

    @Override
    public CompletableFuture<Collection<String>> getAccessibleDatabases() {
        return executorAsync().execute(this::getAccessibleDatabasesRequest, getDatabaseResponseDeserializer());
    }

    @Override
    public ArangoCollectionAsync collection(String name) {
        return new ArangoCollectionAsyncImpl(this, name);
    }

    @Override
    public CompletableFuture<CollectionEntity> createCollection(final String name) {
        return executorAsync().execute(() -> createCollectionRequest(name, new CollectionCreateOptions()), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> createCollection(final String name, final CollectionCreateOptions options) {
        return executorAsync().execute(() -> createCollectionRequest(name, options), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<Collection<CollectionEntity>> getCollections() {
        return executorAsync()
                .execute(() -> getCollectionsRequest(new CollectionsReadOptions()), getCollectionsResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<CollectionEntity>> getCollections(final CollectionsReadOptions options) {
        return executorAsync().execute(() -> getCollectionsRequest(options), getCollectionsResponseDeserializer());
    }

    @Override
    public CompletableFuture<IndexEntity> getIndex(final String id) {
        final String[] split = id.split("/");
        return collection(split[0]).getIndex(split[1]);
    }

    @Override
    public CompletableFuture<String> deleteIndex(final String id) {
        final String[] split = id.split("/");
        return collection(split[0]).deleteIndex(split[1]);
    }

    @Override
    public CompletableFuture<Boolean> create() {
        return arango().createDatabase(name());
    }

    @Override
    public CompletableFuture<Boolean> drop() {
        return executorAsync().execute(this::dropRequest, createDropResponseDeserializer());
    }

    @Override
    public CompletableFuture<Void> grantAccess(final String user, final Permissions permissions) {
        return executorAsync().execute(() -> grantAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Void> grantAccess(final String user) {
        return executorAsync().execute(() -> grantAccessRequest(user, Permissions.RW), Void.class);
    }

    @Override
    public CompletableFuture<Void> revokeAccess(final String user) {
        return executorAsync().execute(() -> grantAccessRequest(user, Permissions.NONE), Void.class);
    }

    @Override
    public CompletableFuture<Void> resetAccess(final String user) {
        return executorAsync().execute(() -> resetAccessRequest(user), Void.class);
    }

    @Override
    public CompletableFuture<Void> grantDefaultCollectionAccess(final String user, final Permissions permissions) {
        return executorAsync().execute(() -> updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Permissions> getPermissions(final String user) {
        return executorAsync().execute(() -> getPermissionsRequest(user), getPermissionsResponseDeserialzer());
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> query(
            final String query, final Class<T> type, final Map<String, ?> bindVars, final AqlQueryOptions options) {
        final InternalRequest request = queryRequest(query, bindVars, options);
        final HostHandle hostHandle = new HostHandle();
        return executorAsync().execute(() -> request, cursorEntityDeserializer(type), hostHandle)
                .thenApply(res -> new ArangoCursorAsyncImpl<>(this, res, type, hostHandle, options.getAllowRetry()));
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type, AqlQueryOptions options) {
        return query(query, type, null, options);
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type, Map<String, ?> bindVars) {
        return query(query, type, bindVars, new AqlQueryOptions());
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type) {
        return query(query, type, null, new AqlQueryOptions());
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> cursor(final String cursorId, final Class<T> type) {
        return cursor(cursorId, type, null, new AqlQueryOptions());
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type, AqlQueryOptions options) {
        return cursor(cursorId, type, null, options);
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> cursor(final String cursorId, final Class<T> type, final String nextBatchId) {
        return cursor(cursorId, type, nextBatchId, new AqlQueryOptions());
    }

    @Override
    public <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type, String nextBatchId, AqlQueryOptions options) {
        options.allowRetry(nextBatchId != null);
        HostHandle hostHandle = new HostHandle();
        return executorAsync()
                .execute(() -> queryNextRequest(cursorId, options, nextBatchId),
                        cursorEntityDeserializer(type),
                        hostHandle)
                .thenApply(res -> new ArangoCursorAsyncImpl<>(this, res, type, hostHandle, nextBatchId != null));
    }

    @Override
    public CompletableFuture<AqlExecutionExplainEntity> explainQuery(
            final String query, final Map<String, ?> bindVars, final AqlQueryExplainOptions options) {
        return executorAsync().execute(() -> explainQueryRequest(query, bindVars, options), AqlExecutionExplainEntity.class);
    }

    @Override
    public CompletableFuture<AqlQueryExplainEntity> explainAqlQuery(
            String query, Map<String, ?> bindVars, AqlQueryExplainOptions options) {
        return executorAsync().execute(() -> explainQueryRequest(query, bindVars, options), AqlQueryExplainEntity.class);
    }

    @Override
    public CompletableFuture<AqlQueryExplainEntity> explainAqlQuery(String query, Map<String, ?> bindVars, ExplainAqlQueryOptions options) {
        return executorAsync().execute(() -> explainQueryRequest(query, bindVars, options), AqlQueryExplainEntity.class);
    }

    @Override
    public CompletableFuture<AqlParseEntity> parseQuery(final String query) {
        return executorAsync().execute(() -> parseQueryRequest(query), AqlParseEntity.class);
    }

    @Override
    public CompletableFuture<Void> clearQueryCache() {
        return executorAsync().execute(this::clearQueryCacheRequest, Void.class);
    }

    @Override
    public CompletableFuture<QueryCachePropertiesEntity> getQueryCacheProperties() {
        return executorAsync().execute(this::getQueryCachePropertiesRequest, QueryCachePropertiesEntity.class);
    }

    @Override
    public CompletableFuture<QueryCachePropertiesEntity> setQueryCacheProperties(final QueryCachePropertiesEntity properties) {
        return executorAsync().execute(() -> setQueryCachePropertiesRequest(properties), QueryCachePropertiesEntity.class);
    }

    @Override
    public CompletableFuture<QueryTrackingPropertiesEntity> getQueryTrackingProperties() {
        return executorAsync().execute(this::getQueryTrackingPropertiesRequest, QueryTrackingPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<QueryTrackingPropertiesEntity> setQueryTrackingProperties(final QueryTrackingPropertiesEntity properties) {
        return executorAsync().execute(() -> setQueryTrackingPropertiesRequest(properties), QueryTrackingPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<Collection<QueryEntity>> getCurrentlyRunningQueries() {
        return executorAsync().execute(this::getCurrentlyRunningQueriesRequest,
                constructListType(QueryEntity.class));
    }

    @Override
    public CompletableFuture<Collection<QueryEntity>> getSlowQueries() {
        return executorAsync().execute(this::getSlowQueriesRequest,
                constructListType(QueryEntity.class));
    }

    @Override
    public CompletableFuture<Void> clearSlowQueries() {
        return executorAsync().execute(this::clearSlowQueriesRequest, Void.class);
    }

    @Override
    public CompletableFuture<Void> killQuery(final String id) {
        return executorAsync().execute(() -> killQueryRequest(id), Void.class);
    }

    @Override
    public CompletableFuture<Void> createAqlFunction(
            final String name, final String code, final AqlFunctionCreateOptions options) {
        return executorAsync().execute(() -> createAqlFunctionRequest(name, code, options), Void.class);
    }

    @Override
    public CompletableFuture<Integer> deleteAqlFunction(final String name, final AqlFunctionDeleteOptions options) {
        return executorAsync().execute(() -> deleteAqlFunctionRequest(name, options), deleteAqlFunctionResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<AqlFunctionEntity>> getAqlFunctions(final AqlFunctionGetOptions options) {
        return executorAsync().execute(() -> getAqlFunctionsRequest(options), getAqlFunctionsResponseDeserializer());
    }

    @Override
    public ArangoGraphAsync graph(final String name) {
        return new ArangoGraphAsyncImpl(this, name);
    }

    @Override
    public CompletableFuture<GraphEntity> createGraph(final String name, final Iterable<EdgeDefinition> edgeDefinitions) {
        return createGraph(name, edgeDefinitions, new GraphCreateOptions());
    }

    @Override
    public CompletableFuture<GraphEntity> createGraph(
            final String name, final Iterable<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options) {
        return executorAsync().execute(() -> createGraphRequest(name, edgeDefinitions, options), createGraphResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<GraphEntity>> getGraphs() {
        return executorAsync().execute(this::getGraphsRequest, getGraphsResponseDeserializer());
    }

    @Override
    public <T> CompletableFuture<T> transaction(final String action, final Class<T> type, final TransactionOptions options) {
        return executorAsync().execute(() -> transactionRequest(action, options), transactionResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<StreamTransactionEntity> beginStreamTransaction(StreamTransactionOptions options) {
        return executorAsync().execute(() -> beginStreamTransactionRequest(options), streamTransactionResponseDeserializer());
    }

    @Override
    public CompletableFuture<StreamTransactionEntity> abortStreamTransaction(String id) {
        return executorAsync().execute(() -> abortStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public CompletableFuture<StreamTransactionEntity> getStreamTransaction(String id) {
        return executorAsync().execute(() -> getStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<TransactionEntity>> getStreamTransactions() {
        return executorAsync().execute(this::getStreamTransactionsRequest, transactionsResponseDeserializer());
    }

    @Override
    public CompletableFuture<StreamTransactionEntity> commitStreamTransaction(String id) {
        return executorAsync().execute(() -> commitStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public CompletableFuture<DatabaseEntity> getInfo() {
        return executorAsync().execute(this::getInfoRequest, getInfoResponseDeserializer());
    }

    @Override
    public CompletableFuture<Void> reloadRouting() {
        return executorAsync().execute(this::reloadRoutingRequest, Void.class);
    }

    @Override
    public CompletableFuture<Collection<ViewEntity>> getViews() {
        return executorAsync().execute(this::getViewsRequest, getViewsResponseDeserializer());
    }

    @Override
    public ArangoViewAsync view(final String name) {
        return new ArangoViewAsyncImpl(this, name);
    }

    @Override
    public ArangoSearchAsync arangoSearch(final String name) {
        return new ArangoSearchAsyncImpl(this, name);
    }

    @Override
    public SearchAliasAsync searchAlias(String name) {
        return new SearchAliasAsyncImpl(this, name);
    }

    @Override
    public CompletableFuture<ViewEntity> createView(final String name, final ViewType type) {
        return executorAsync().execute(() -> createViewRequest(name, type), ViewEntity.class);
    }

    @Override
    public CompletableFuture<ViewEntity> createArangoSearch(final String name, final ArangoSearchCreateOptions options) {
        return executorAsync().execute(() -> createArangoSearchRequest(name, options), ViewEntity.class);
    }

    @Override
    public CompletableFuture<ViewEntity> createSearchAlias(String name, SearchAliasCreateOptions options) {
        return executorAsync().execute(() -> createSearchAliasRequest(name, options), ViewEntity.class);
    }

    @Override
    public CompletableFuture<SearchAnalyzer> createSearchAnalyzer(SearchAnalyzer analyzer) {
        return executorAsync().execute(() -> createAnalyzerRequest(analyzer), SearchAnalyzer.class);
    }

    @Override
    public CompletableFuture<SearchAnalyzer> getSearchAnalyzer(String name) {
        return executorAsync().execute(() -> getAnalyzerRequest(name), SearchAnalyzer.class);
    }

    @Override
    public CompletableFuture<Collection<SearchAnalyzer>> getSearchAnalyzers() {
        return executorAsync().execute(this::getAnalyzersRequest, getSearchAnalyzersResponseDeserializer());
    }

    @Override
    public CompletableFuture<Void> deleteSearchAnalyzer(String name) {
        return deleteSearchAnalyzer(name, null);
    }

    @Override
    public CompletableFuture<Void> deleteSearchAnalyzer(String name, AnalyzerDeleteOptions options) {
        return executorAsync().execute(() -> deleteAnalyzerRequest(name, options), Void.class);
    }

}

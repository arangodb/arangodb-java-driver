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
import com.arangodb.internal.cursor.ArangoCursorImpl;
import com.arangodb.internal.net.HostHandle;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.SearchAliasCreateOptions;

import java.util.Collection;
import java.util.Map;

import static com.arangodb.internal.ArangoErrors.*;
import static com.arangodb.internal.serde.SerdeUtils.constructListType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoDatabaseImpl extends InternalArangoDatabase implements ArangoDatabase {

    private final ArangoDB arangoDB;

    protected ArangoDatabaseImpl(final ArangoDBImpl arangoDB, final String name) {
        super(arangoDB, name);
        this.arangoDB = arangoDB;
    }

    @Override
    public ArangoDB arango() {
        return arangoDB;
    }

    @Override
    public ArangoDBVersion getVersion() {
        return executorSync().execute(getVersionRequest(), ArangoDBVersion.class);
    }

    @Override
    public ArangoDBEngine getEngine() {
        return executorSync().execute(getEngineRequest(), ArangoDBEngine.class);
    }

    @Override
    public boolean exists() {
        try {
            getInfo();
            return true;
        } catch (final ArangoDBException e) {
            if (matches(e, 404, ERROR_ARANGO_DATABASE_NOT_FOUND)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public Collection<String> getAccessibleDatabases() {
        return executorSync().execute(getAccessibleDatabasesRequest(), getDatabaseResponseDeserializer());
    }

    @Override
    public ArangoCollection collection(final String name) {
        return new ArangoCollectionImpl(this, name);
    }

    @Override
    public CollectionEntity createCollection(final String name) {
        return executorSync().execute(createCollectionRequest(name, new CollectionCreateOptions()), CollectionEntity.class);
    }

    @Override
    public CollectionEntity createCollection(final String name, final CollectionCreateOptions options) {
        return executorSync().execute(createCollectionRequest(name, options), CollectionEntity.class);
    }

    @Override
    public Collection<CollectionEntity> getCollections() {
        return executorSync()
                .execute(getCollectionsRequest(new CollectionsReadOptions()), getCollectionsResponseDeserializer());
    }

    @Override
    public Collection<CollectionEntity> getCollections(final CollectionsReadOptions options) {
        return executorSync().execute(getCollectionsRequest(options), getCollectionsResponseDeserializer());
    }

    @Override
    public IndexEntity getIndex(final String id) {
        final String[] split = id.split("/");
        return collection(split[0]).getIndex(split[1]);
    }

    @Override
    public String deleteIndex(final String id) {
        final String[] split = id.split("/");
        return collection(split[0]).deleteIndex(split[1]);
    }

    @Override
    public Boolean create() {
        return arango().createDatabase(name());
    }

    @Override
    public Boolean drop() {
        return executorSync().execute(dropRequest(), createDropResponseDeserializer());
    }

    @Override
    public void grantAccess(final String user, final Permissions permissions) {
        executorSync().execute(grantAccessRequest(user, permissions), Void.class);
    }

    @Override
    public void grantAccess(final String user) {
        executorSync().execute(grantAccessRequest(user, Permissions.RW), Void.class);
    }

    @Override
    public void revokeAccess(final String user) {
        executorSync().execute(grantAccessRequest(user, Permissions.NONE), Void.class);
    }

    @Override
    public void resetAccess(final String user) {
        executorSync().execute(resetAccessRequest(user), Void.class);
    }

    @Override
    public void grantDefaultCollectionAccess(final String user, final Permissions permissions) {
        executorSync().execute(updateUserDefaultCollectionAccessRequest(user, permissions), Void.class);
    }

    @Override
    public Permissions getPermissions(final String user) {
        return executorSync().execute(getPermissionsRequest(user), getPermissionsResponseDeserialzer());
    }

    @Override
    public <T> ArangoCursor<T> query(
            final String query, final Class<T> type, final Map<String, Object> bindVars, final AqlQueryOptions options) {
        final InternalRequest request = queryRequest(query, bindVars, options);
        final HostHandle hostHandle = new HostHandle();
        final CursorEntity<T> result = executorSync().execute(request, cursorEntityDeserializer(type), hostHandle);
        return createCursor(result, type, options, hostHandle);
    }

    @Override
    public <T> ArangoCursor<T> query(final String query, final Class<T> type, final Map<String, Object> bindVars) {
        return query(query, type, bindVars, new AqlQueryOptions());
    }

    @Override
    public <T> ArangoCursor<T> query(final String query, final Class<T> type, final AqlQueryOptions options) {
        return query(query, type, null, options);
    }

    @Override
    public <T> ArangoCursor<T> query(final String query, final Class<T> type) {
        return query(query, type, null, new AqlQueryOptions());
    }

    @Override
    public <T> ArangoCursor<T> cursor(final String cursorId, final Class<T> type) {
        return cursor(cursorId, type, null);
    }

    @Override
    public <T> ArangoCursor<T> cursor(final String cursorId, final Class<T> type, final String nextBatchId) {
        final HostHandle hostHandle = new HostHandle();
        final CursorEntity<T> result = executorSync().execute(
                queryNextRequest(cursorId, new AqlQueryOptions(), nextBatchId),
                cursorEntityDeserializer(type),
                hostHandle);
        return createCursor(result, type, null, hostHandle);
    }

    private <T> ArangoCursor<T> createCursor(
            final CursorEntity<T> result,
            final Class<T> type,
            final AqlQueryOptions opts,
            final HostHandle hostHandle) {
        AqlQueryOptions options = opts != null ? opts : new AqlQueryOptions();

        final ArangoCursorExecute<T> execute = new ArangoCursorExecute<T>() {
            @Override
            public CursorEntity<T> next(final String id, final String nextBatchId) {
                return executorSync().execute(queryNextRequest(id, options, nextBatchId), cursorEntityDeserializer(type), hostHandle);
            }

            @Override
            public void close(final String id) {
                try {
                    executorSync().execute(queryCloseRequest(id, options), Void.class, hostHandle);
                } catch (final ArangoDBException e) {
                    // ignore errors Response: 404, Error: 1600 - cursor not found
                    if (!matches(e, 404, 1600)) {
                        throw e;
                    }
                }
            }
        };
        return new ArangoCursorImpl<>(execute, type, result, options.getAllowRetry());
    }

    @Override
    public AqlExecutionExplainEntity explainQuery(
            final String query, final Map<String, Object> bindVars, final AqlQueryExplainOptions options) {
        return executorSync().execute(explainQueryRequest(query, bindVars, options), AqlExecutionExplainEntity.class);
    }

    @Override
    public AqlParseEntity parseQuery(final String query) {
        return executorSync().execute(parseQueryRequest(query), AqlParseEntity.class);
    }

    @Override
    public void clearQueryCache() {
        executorSync().execute(clearQueryCacheRequest(), Void.class);
    }

    @Override
    public QueryCachePropertiesEntity getQueryCacheProperties() {
        return executorSync().execute(getQueryCachePropertiesRequest(), QueryCachePropertiesEntity.class);
    }

    @Override
    public QueryCachePropertiesEntity setQueryCacheProperties(final QueryCachePropertiesEntity properties) {
        return executorSync().execute(setQueryCachePropertiesRequest(properties), QueryCachePropertiesEntity.class);
    }

    @Override
    public QueryTrackingPropertiesEntity getQueryTrackingProperties() {
        return executorSync().execute(getQueryTrackingPropertiesRequest(), QueryTrackingPropertiesEntity.class);
    }

    @Override
    public QueryTrackingPropertiesEntity setQueryTrackingProperties(final QueryTrackingPropertiesEntity properties) {
        return executorSync().execute(setQueryTrackingPropertiesRequest(properties), QueryTrackingPropertiesEntity.class);
    }

    @Override
    public Collection<QueryEntity> getCurrentlyRunningQueries() {
        return executorSync().execute(getCurrentlyRunningQueriesRequest(),
                constructListType(QueryEntity.class));
    }

    @Override
    public Collection<QueryEntity> getSlowQueries() {
        return executorSync().execute(getSlowQueriesRequest(),
                constructListType(QueryEntity.class));
    }

    @Override
    public void clearSlowQueries() {
        executorSync().execute(clearSlowQueriesRequest(), Void.class);
    }

    @Override
    public void killQuery(final String id) {
        executorSync().execute(killQueryRequest(id), Void.class);
    }

    @Override
    public void createAqlFunction(
            final String name, final String code, final AqlFunctionCreateOptions options) {
        executorSync().execute(createAqlFunctionRequest(name, code, options), Void.class);
    }

    @Override
    public Integer deleteAqlFunction(final String name, final AqlFunctionDeleteOptions options) {
        return executorSync().execute(deleteAqlFunctionRequest(name, options), deleteAqlFunctionResponseDeserializer());
    }

    @Override
    public Collection<AqlFunctionEntity> getAqlFunctions(final AqlFunctionGetOptions options) {
        return executorSync().execute(getAqlFunctionsRequest(options), getAqlFunctionsResponseDeserializer());
    }

    @Override
    public ArangoGraph graph(final String name) {
        return new ArangoGraphImpl(this, name);
    }

    @Override
    public GraphEntity createGraph(final String name, final Iterable<EdgeDefinition> edgeDefinitions) {
        return createGraph(name, edgeDefinitions, new GraphCreateOptions());
    }

    @Override
    public GraphEntity createGraph(
            final String name, final Iterable<EdgeDefinition> edgeDefinitions, final GraphCreateOptions options) {
        return executorSync().execute(createGraphRequest(name, edgeDefinitions, options), createGraphResponseDeserializer());
    }

    @Override
    public Collection<GraphEntity> getGraphs() {
        return executorSync().execute(getGraphsRequest(), getGraphsResponseDeserializer());
    }

    @Override
    public <T> T transaction(final String action, final Class<T> type, final TransactionOptions options) {
        return executorSync().execute(transactionRequest(action, options), transactionResponseDeserializer(type));
    }

    @Override
    public StreamTransactionEntity beginStreamTransaction(StreamTransactionOptions options) {
        return executorSync().execute(beginStreamTransactionRequest(options), streamTransactionResponseDeserializer());
    }

    @Override
    public StreamTransactionEntity abortStreamTransaction(String id) {
        return executorSync().execute(abortStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public StreamTransactionEntity getStreamTransaction(String id) {
        return executorSync().execute(getStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public Collection<TransactionEntity> getStreamTransactions() {
        return executorSync().execute(getStreamTransactionsRequest(), transactionsResponseDeserializer());
    }

    @Override
    public StreamTransactionEntity commitStreamTransaction(String id) {
        return executorSync().execute(commitStreamTransactionRequest(id), streamTransactionResponseDeserializer());
    }

    @Override
    public DatabaseEntity getInfo() {
        return executorSync().execute(getInfoRequest(), getInfoResponseDeserializer());
    }

    @Override
    public void reloadRouting() {
        executorSync().execute(reloadRoutingRequest(), Void.class);
    }

    @Override
    public Collection<ViewEntity> getViews() {
        return executorSync().execute(getViewsRequest(), getViewsResponseDeserializer());
    }

    @Override
    public ArangoView view(final String name) {
        return new ArangoViewImpl(this, name);
    }

    @Override
    public ArangoSearch arangoSearch(final String name) {
        return new ArangoSearchImpl(this, name);
    }

    @Override
    public SearchAlias searchAlias(String name) {
        return new SearchAliasImpl(this, name);
    }

    @Override
    public ViewEntity createView(final String name, final ViewType type) {
        return executorSync().execute(createViewRequest(name, type), ViewEntity.class);
    }

    @Override
    public ViewEntity createArangoSearch(final String name, final ArangoSearchCreateOptions options) {
        return executorSync().execute(createArangoSearchRequest(name, options), ViewEntity.class);
    }

    @Override
    public ViewEntity createSearchAlias(String name, SearchAliasCreateOptions options) {
        return executorSync().execute(createSearchAliasRequest(name, options), ViewEntity.class);
    }

    @Override
    public SearchAnalyzer createSearchAnalyzer(SearchAnalyzer analyzer) {
        return executorSync().execute(createAnalyzerRequest(analyzer), SearchAnalyzer.class);
    }

    @Override
    public SearchAnalyzer getSearchAnalyzer(String name) {
        return executorSync().execute(getAnalyzerRequest(name), SearchAnalyzer.class);
    }

    @Override
    public Collection<SearchAnalyzer> getSearchAnalyzers() {
        return executorSync().execute(getAnalyzersRequest(), getSearchAnalyzersResponseDeserializer());
    }

    @Override
    public void deleteSearchAnalyzer(String name) {
        deleteSearchAnalyzer(name, null);
    }

    @Override
    public void deleteSearchAnalyzer(String name, AnalyzerDeleteOptions options) {
        executorSync().execute(deleteAnalyzerRequest(name, options), Void.class);
    }

}

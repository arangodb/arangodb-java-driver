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

import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.analyzer.SearchAnalyzer;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.model.arangosearch.SearchAliasCreateOptions;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoDatabase}
 */
@ThreadSafe
public interface ArangoDatabaseAsync extends ArangoSerdeAccessor {

    /**
     * @return main entry point for async API
     */
    ArangoDBAsync arango();

    /**
     * @return database name
     */
    String name();

    /**
     * Asynchronous version of {@link ArangoDatabase#getVersion()}
     */
    CompletableFuture<ArangoDBVersion> getVersion();

    /**
     * Asynchronous version of {@link ArangoDatabase#getEngine()}
     */
    CompletableFuture<ArangoDBEngine> getEngine();

    /**
     * Asynchronous version of {@link ArangoDatabase#exists()}
     */
    CompletableFuture<Boolean> exists();

    /**
     * Asynchronous version of {@link ArangoDatabase#getAccessibleDatabases()}
     */
    CompletableFuture<Collection<String>> getAccessibleDatabases();

    /**
     * Returns a {@code ArangoCollectionAsync} instance for the given collection name.
     *
     * @param name Name of the collection
     * @return collection handler
     */
    ArangoCollectionAsync collection(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#createCollection(String)}
     */
    CompletableFuture<CollectionEntity> createCollection(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#createCollection(String, CollectionCreateOptions)}
     */
    CompletableFuture<CollectionEntity> createCollection(String name, CollectionCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#getCollections()}
     */
    CompletableFuture<Collection<CollectionEntity>> getCollections();

    /**
     * Asynchronous version of {@link ArangoDatabase#getCollections(CollectionsReadOptions)}
     */
    CompletableFuture<Collection<CollectionEntity>> getCollections(CollectionsReadOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#getIndex(String)}
     */
    CompletableFuture<IndexEntity> getIndex(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#deleteIndex(String)}
     */
    CompletableFuture<String> deleteIndex(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#create()}
     */
    CompletableFuture<Boolean> create();

    /**
     * Asynchronous version of {@link ArangoDatabase#drop()}
     */
    CompletableFuture<Boolean> drop();

    /**
     * Asynchronous version of {@link ArangoDatabase#grantAccess(String, Permissions)}
     */
    CompletableFuture<Void> grantAccess(String user, Permissions permissions);

    /**
     * Asynchronous version of {@link ArangoDatabase#grantAccess(String)}
     */
    CompletableFuture<Void> grantAccess(String user);

    /**
     * Asynchronous version of {@link ArangoDatabase#revokeAccess(String)}
     */
    CompletableFuture<Void> revokeAccess(String user);

    /**
     * Asynchronous version of {@link ArangoDatabase#resetAccess(String)}
     */
    CompletableFuture<Void> resetAccess(String user);

    /**
     * Asynchronous version of {@link ArangoDatabase#grantDefaultCollectionAccess(String, Permissions)}
     */
    CompletableFuture<Void> grantDefaultCollectionAccess(String user, Permissions permissions);

    /**
     * Asynchronous version of {@link ArangoDatabase#getPermissions(String)}
     */
    CompletableFuture<Permissions> getPermissions(String user);

    <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type, Map<String, Object> bindVars, AqlQueryOptions options);

    <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type, AqlQueryOptions options);

    <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type, Map<String, Object> bindVars);

    <T> CompletableFuture<ArangoCursorAsync<T>> query(String query, Class<T> type);

    <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type);

    <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type, AqlQueryOptions options);

    <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type, String nextBatchId);

    <T> CompletableFuture<ArangoCursorAsync<T>> cursor(String cursorId, Class<T> type, String nextBatchId, AqlQueryOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#explainQuery(String, Map, AqlQueryExplainOptions)}
     */
    CompletableFuture<AqlExecutionExplainEntity> explainQuery(String query, Map<String, Object> bindVars, AqlQueryExplainOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#parseQuery(String)}
     */
    CompletableFuture<AqlParseEntity> parseQuery(String query);

    /**
     * Asynchronous version of {@link ArangoDatabase#clearQueryCache()}
     */
    CompletableFuture<Void> clearQueryCache();

    /**
     * Asynchronous version of {@link ArangoDatabase#getQueryCacheProperties()}
     */
    CompletableFuture<QueryCachePropertiesEntity> getQueryCacheProperties();

    /**
     * Asynchronous version of {@link ArangoDatabase#setQueryCacheProperties(QueryCachePropertiesEntity)}
     */
    CompletableFuture<QueryCachePropertiesEntity> setQueryCacheProperties(QueryCachePropertiesEntity properties);

    /**
     * Asynchronous version of {@link ArangoDatabase#getQueryTrackingProperties()}
     */
    CompletableFuture<QueryTrackingPropertiesEntity> getQueryTrackingProperties();

    /**
     * Asynchronous version of {@link ArangoDatabase#setQueryTrackingProperties(QueryTrackingPropertiesEntity)}
     */
    CompletableFuture<QueryTrackingPropertiesEntity> setQueryTrackingProperties(QueryTrackingPropertiesEntity properties);

    /**
     * Asynchronous version of {@link ArangoDatabase#getCurrentlyRunningQueries()}
     */
    CompletableFuture<Collection<QueryEntity>> getCurrentlyRunningQueries();

    /**
     * Asynchronous version of {@link ArangoDatabase#getSlowQueries()}
     */
    CompletableFuture<Collection<QueryEntity>> getSlowQueries();

    /**
     * Asynchronous version of {@link ArangoDatabase#clearSlowQueries()}
     */
    CompletableFuture<Void> clearSlowQueries();

    /**
     * Asynchronous version of {@link ArangoDatabase#killQuery(String)}
     */
    CompletableFuture<Void> killQuery(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#createAqlFunction(String, String, AqlFunctionCreateOptions)}
     */
    CompletableFuture<Void> createAqlFunction(String name, String code, AqlFunctionCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#deleteAqlFunction(String, AqlFunctionDeleteOptions)}
     */
    CompletableFuture<Integer> deleteAqlFunction(String name, AqlFunctionDeleteOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#getAqlFunctions(AqlFunctionGetOptions)}
     */
    CompletableFuture<Collection<AqlFunctionEntity>> getAqlFunctions(AqlFunctionGetOptions options);

    /**
     * Returns a {@code ArangoGraphAsync} instance for the given graph name.
     *
     * @param name Name of the graph
     * @return graph handler
     */
    ArangoGraphAsync graph(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#createGraph(String, Iterable)}
     */
    CompletableFuture<GraphEntity> createGraph(String name, Iterable<EdgeDefinition> edgeDefinitions);

    /**
     * Asynchronous version of {@link ArangoDatabase#createGraph(String, Iterable, GraphCreateOptions)}
     */
    CompletableFuture<GraphEntity> createGraph(String name, Iterable<EdgeDefinition> edgeDefinitions, GraphCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#getGraphs()}
     */
    CompletableFuture<Collection<GraphEntity>> getGraphs();

    /**
     * Asynchronous version of {@link ArangoDatabase#transaction(String, Class, TransactionOptions)}
     */
    <T> CompletableFuture<T> transaction(String action, Class<T> type, TransactionOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#beginStreamTransaction(StreamTransactionOptions)}
     */
    CompletableFuture<StreamTransactionEntity> beginStreamTransaction(StreamTransactionOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#abortStreamTransaction(String)}
     */
    CompletableFuture<StreamTransactionEntity> abortStreamTransaction(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#getStreamTransaction(String)}
     */
    CompletableFuture<StreamTransactionEntity> getStreamTransaction(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#getStreamTransactions()}
     */
    CompletableFuture<Collection<TransactionEntity>> getStreamTransactions();

    /**
     * Asynchronous version of {@link ArangoDatabase#commitStreamTransaction(String)}
     */
    CompletableFuture<StreamTransactionEntity> commitStreamTransaction(String id);

    /**
     * Asynchronous version of {@link ArangoDatabase#getInfo()}
     */
    CompletableFuture<DatabaseEntity> getInfo();

    /**
     * Asynchronous version of {@link ArangoDatabase#reloadRouting()}
     */
    CompletableFuture<Void> reloadRouting();

    /**
     * Asynchronous version of {@link ArangoDatabase#getViews()}
     */
    CompletableFuture<Collection<ViewEntity>> getViews();

    /**
     * Returns a {@code ArangoViewAsync} instance for the given view name.
     *
     * @param name Name of the view
     * @return view handler
     * @since ArangoDB 3.4.0
     */
    ArangoViewAsync view(String name);

    /**
     * Returns a {@link ArangoSearchAsync} instance for the given view name.
     *
     * @param name Name of the view
     * @return ArangoSearch view handler
     * @since ArangoDB 3.4.0
     */
    ArangoSearchAsync arangoSearch(String name);

    /**
     * Returns a {@link SearchAliasAsync} instance for the given view name.
     *
     * @param name Name of the view
     * @return SearchAlias view handler
     * @since ArangoDB 3.10
     */
    SearchAliasAsync searchAlias(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#createView(String, ViewType)}
     */
    CompletableFuture<ViewEntity> createView(String name, ViewType type);

    /**
     * Asynchronous version of {@link ArangoDatabase#createArangoSearch(String, ArangoSearchCreateOptions)}
     */
    CompletableFuture<ViewEntity> createArangoSearch(String name, ArangoSearchCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#createSearchAlias(String, SearchAliasCreateOptions)}
     */
    CompletableFuture<ViewEntity> createSearchAlias(String name, SearchAliasCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoDatabase#createSearchAnalyzer(SearchAnalyzer)}
     */
    CompletableFuture<SearchAnalyzer> createSearchAnalyzer(SearchAnalyzer analyzer);

    /**
     * Asynchronous version of {@link ArangoDatabase#getSearchAnalyzer(String)}
     */
    CompletableFuture<SearchAnalyzer> getSearchAnalyzer(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#getSearchAnalyzers()}
     */
    CompletableFuture<Collection<SearchAnalyzer>> getSearchAnalyzers();

    /**
     * Asynchronous version of {@link ArangoDatabase#deleteSearchAnalyzer(String)}
     */
    CompletableFuture<Void> deleteSearchAnalyzer(String name);

    /**
     * Asynchronous version of {@link ArangoDatabase#deleteSearchAnalyzer(String, AnalyzerDeleteOptions)}
     */
    CompletableFuture<Void> deleteSearchAnalyzer(String name, AnalyzerDeleteOptions options);

}

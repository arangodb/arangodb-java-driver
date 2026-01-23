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

/**
 * Interface for operations on ArangoDB database level.
 *
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/databases/">Databases API Documentation</a>
 * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/">Query API Documentation</a>
 */
@ThreadSafe
public interface ArangoDatabase extends ArangoSerdeAccessor {

    /**
     * Return the main entry point for the ArangoDB driver
     *
     * @return main entry point
     */
    ArangoDB arango();

    /**
     * Returns the name of the database
     *
     * @return database name
     */
    String name();

    /**
     * Returns the server name and version number.
     *
     * @return the server version, number
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/administration/#get-the-server-version">API
     * Documentation</a>
     */
    ArangoDBVersion getVersion();

    /**
     * Returns the name of the used storage engine.
     *
     * @return the storage engine name
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/administration/#get-the-storage-engine-type">API
     * Documentation</a>
     */
    ArangoDBEngine getEngine();

    /**
     * Checks whether the database exists
     *
     * @return true if the database exists, otherwise false
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/databases/#get-information-about-the-current-database">API
     * Documentation</a>
     */
    boolean exists();

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/databases/#list-the-accessible-databases">API
     * Documentation</a>
     */
    Collection<String> getAccessibleDatabases();

    /**
     * Returns a {@code ArangoCollection} instance for the given collection name.
     *
     * @param name Name of the collection
     * @return collection handler
     */
    ArangoCollection collection(String name);

    /**
     * Creates a collection for the given collection's name, then returns collection information from the server.
     *
     * @param name The name of the collection
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#create-a-collection">API
     * Documentation</a>
     */
    CollectionEntity createCollection(String name);

    /**
     * Creates a collection with the given {@code options} for this collection's name, then returns collection
     * information from the server.
     *
     * @param name    The name of the collection
     * @param options Additional options, can be null
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#create-a-collection">API
     * Documentation</a>
     */
    CollectionEntity createCollection(String name, CollectionCreateOptions options);

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @return list of information about all collections
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#list-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections();

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @param options Additional options, can be null
     * @return list of information about all collections
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#list-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections(CollectionsReadOptions options);

    /**
     * Returns an index
     *
     * @param id The index-handle
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#get-an-index">API Documentation</a>
     */
    IndexEntity getIndex(String id);

    /**
     * Deletes an index
     *
     * @param id The index-handle
     * @return the id of the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#delete-an-index">API Documentation</a>
     */
    String deleteIndex(String id);

    /**
     * Creates the database
     *
     * @return true if the database was created successfully.
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/databases/#create-a-database">API
     * Documentation</a>
     */
    Boolean create();

    /**
     * Deletes the database from the server.
     *
     * @return true if the database was dropped successfully
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/databases/#drop-a-database">API
     * Documentation</a>
     */
    Boolean drop();

    /**
     * Grants or revoke access to the database for user {@code user}. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user, Permissions permissions);

    /**
     * Grants access to the database for user {@code user}. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user The name of the user
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user);

    /**
     * Revokes access to the database dbname for user {@code user}. You need permission to the _system database in order
     * to execute this call.
     *
     * @param user The name of the user
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-database-access-level">
     * API Documentation</a>
     */
    void revokeAccess(String user);

    /**
     * Clear the database access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#clear-a-users-database-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void resetAccess(String user);

    /**
     * Sets the default access level for collections within this database for the user {@code user}. You need permission
     * to the _system database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-database-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void grantDefaultCollectionAccess(String user, Permissions permissions);

    /**
     * Get specific database access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#get-a-users-database-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @param options  Additional options that will be passed to the query API, can be null
     * @return cursor of the results
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#create-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Class<T> type, Map<String, ?> bindVars, AqlQueryOptions options);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query   An AQL query string
     * @param type    The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options that will be passed to the query API, can be null
     * @return cursor of the results
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#create-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Class<T> type, AqlQueryOptions options);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @return cursor of the results
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#create-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Class<T> type, Map<String, ?> bindVars);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query An AQL query string
     * @param type  The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#create-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Class<T> type);

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId The ID of the cursor
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#read-the-next-batch-from-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type);

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId The ID of the cursor
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param options  options
     * @return cursor of the results
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#read-the-next-batch-from-a-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type, AqlQueryOptions options);

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId    The ID of the cursor
     * @param type        The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param nextBatchId The ID of the next cursor batch (set only if cursor allows retries, see
     *                    {@link AqlQueryOptions#allowRetry(Boolean)}
     * @return cursor of the results
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#read-the-next-batch-from-a-cursor">API Documentation</a>
     * @since ArangoDB 3.11
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type, String nextBatchId);

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId    The ID of the cursor
     * @param type        The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param nextBatchId The ID of the next cursor batch (set only if cursor allows retries, see
     *                    {@link AqlQueryOptions#allowRetry(Boolean)}
     * @param options     options
     * @return cursor of the results
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#read-the-next-batch-from-a-cursor">API Documentation</a>
     * @since ArangoDB 3.11
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type, String nextBatchId, AqlQueryOptions options);

    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#explain-an-aql-query">API
     * Documentation</a>
     * @deprecated for removal, use {@link ArangoDatabase#explainAqlQuery(String, Map, AqlQueryExplainOptions)} instead
     */
    @Deprecated
    AqlExecutionExplainEntity explainQuery(String query, Map<String, ?> bindVars, AqlQueryExplainOptions options);

    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#explain-an-aql-query">API
     * Documentation</a>
     * @deprecated for removal, use {@link ArangoDatabase#explainAqlQuery(String, Map, ExplainAqlQueryOptions)} instead
     */
    @Deprecated
    AqlQueryExplainEntity explainAqlQuery(String query, Map<String, ?> bindVars, AqlQueryExplainOptions options);


    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#explain-an-aql-query">API
     * Documentation</a>
     */
    AqlQueryExplainEntity explainAqlQuery(String query, Map<String, ?> bindVars, ExplainAqlQueryOptions options);

    /**
     * Parse an AQL query and return information about it This method is for query validation only. To actually query
     * the database, see {@link ArangoDatabase#query(String, Class, Map, AqlQueryOptions)}
     *
     * @param query the query which you want parse
     * @return imformation about the query
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#parse-an-aql-query">API
     * Documentation</a>
     */
    AqlParseEntity parseQuery(String query);

    /**
     * Clears the AQL query cache
     *
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-query-results-cache/#clear-the-aql-query-results-cache">API
     * Documentation</a>
     */
    void clearQueryCache();

    /**
     * Returns the global configuration for the AQL query cache
     *
     * @return configuration for the AQL query cache
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-query-results-cache/#get-the-aql-query-results-cache-configuration">API
     * Documentation</a>
     */
    QueryCachePropertiesEntity getQueryCacheProperties();

    /**
     * Changes the configuration for the AQL query cache. Note: changing the properties may invalidate all results in
     * the cache.
     *
     * @param properties properties to be set
     * @return current set of properties
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-query-results-cache/#set-the-aql-query-results-cache-configuration">API
     * Documentation</a>
     */
    QueryCachePropertiesEntity setQueryCacheProperties(QueryCachePropertiesEntity properties);

    /**
     * Returns the configuration for the AQL query tracking
     *
     * @return configuration for the AQL query tracking
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#get-the-aql-query-tracking-configuration">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity getQueryTrackingProperties();

    /**
     * Changes the configuration for the AQL query tracking
     *
     * @param properties properties to be set
     * @return current set of properties
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#update-the-aql-query-tracking-configuration">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity setQueryTrackingProperties(QueryTrackingPropertiesEntity properties);

    /**
     * Returns a list of currently running AQL queries
     *
     * @return a list of currently running AQL queries
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-running-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getCurrentlyRunningQueries();

    /**
     * Returns a list of slow running AQL queries
     *
     * @return a list of slow running AQL queries
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#list-the-slow-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getSlowQueries();

    /**
     * Clears the list of slow AQL queries
     *
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#clear-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    void clearSlowQueries();

    /**
     * Kills a running query. The query will be terminated at the next cancelation point.
     *
     * @param id The id of the query
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/aql-queries/#kill-a-running-aql-query">API
     * Documentation</a>
     */
    void killQuery(String id);

    /**
     * Create a new AQL user function
     *
     * @param name    A valid AQL function name, e.g.: `"myfuncs::accounting::calculate_vat"`
     * @param code    A String evaluating to a JavaScript function
     * @param options Additional options, can be null
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/queries/user-defined-aql-functions/#create-a-user-defined-aql-function">API
     * Documentation</a>
     */
    void createAqlFunction(String name, String code, AqlFunctionCreateOptions options);

    /**
     * Deletes the AQL user function with the given name from the database.
     *
     * @param name    The name of the user function to delete
     * @param options Additional options, can be null
     * @return number of deleted functions (since ArangoDB 3.4.0)
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/user-defined-aql-functions/#remove-a-user-defined-aql-function">API
     * Documentation</a>
     */
    Integer deleteAqlFunction(String name, AqlFunctionDeleteOptions options);

    /**
     * Gets all reqistered AQL user functions
     *
     * @param options Additional options, can be null
     * @return all reqistered AQL user functions
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/queries/user-defined-aql-functions/#list-the-registered-user-defined-aql-functions">API
     * Documentation</a>
     */
    Collection<AqlFunctionEntity> getAqlFunctions(AqlFunctionGetOptions options);

    /**
     * Returns a {@code ArangoGraph} instance for the given graph name.
     *
     * @param name Name of the graph
     * @return graph handler
     */
    ArangoGraph graph(String name);

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @return information about the graph
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Iterable<EdgeDefinition> edgeDefinitions);

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Iterable<EdgeDefinition> edgeDefinitions, GraphCreateOptions options);

    /**
     * Lists all graphs known to the graph module
     *
     * @return graphs stored in this database
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/graphs/named-graphs/#list-all-graphs">API
     * Documentation</a>
     */
    Collection<GraphEntity> getGraphs();

    /**
     * Performs a server-side transaction and returns its return value.
     *
     * @param action  A String evaluating to a JavaScript function to be executed on the server.
     * @param type    The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return the result of the transaction if it succeeded
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/javascript-transactions/#execute-a-javascript-transaction">API
     * Documentation</a>
     */
    <T> T transaction(String action, Class<T> type, TransactionOptions options);

    /**
     * Begins a Stream Transaction.
     *
     * @param options Additional options, can be null
     * @return information about the transaction
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/stream-transactions/#begin-a-stream-transaction">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity beginStreamTransaction(StreamTransactionOptions options);

    /**
     * Aborts a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/stream-transactions/#abort-a-stream-transaction">API
     * Documentation</a>
     */
    StreamTransactionEntity abortStreamTransaction(String id);

    /**
     * Gets information about a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/stream-transactions/#get-the-status-of-a-stream-transaction">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity getStreamTransaction(String id);

    /**
     * Gets all the currently running Stream Transactions.
     *
     * @return all the currently running Stream Transactions
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/stream-transactions/#list-the-running-stream-transactions">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<TransactionEntity> getStreamTransactions();

    /**
     * Commits a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/transactions/stream-transactions/#commit-a-stream-transaction">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity commitStreamTransaction(String id);

    /**
     * Retrieves information about the current database
     *
     * @return information about the current database
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/databases/#get-information-about-the-current-database">API
     * Documentation</a>
     */
    DatabaseEntity getInfo();

    /**
     * Reload the routing table.
     *
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/administration/#reload-the-routing-table">API
     * Documentation</a>
     */
    void reloadRouting();

    /**
     * Fetches all views from the database and returns a list of view descriptions.
     *
     * @return list of information about all views
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#list-all-views">API Documentation</a>
     * @since ArangoDB 3.4.0
     */
    Collection<ViewEntity> getViews();

    /**
     * Returns a {@code ArangoView} instance for the given view name.
     *
     * @param name Name of the view
     * @return view handler
     * @since ArangoDB 3.4.0
     */
    ArangoView view(String name);

    /**
     * Returns a {@link ArangoSearch} instance for the given view name.
     *
     * @param name Name of the view
     * @return ArangoSearch view handler
     * @since ArangoDB 3.4.0
     */
    ArangoSearch arangoSearch(String name);

    /**
     * Returns a {@link SearchAlias} instance for the given view name.
     *
     * @param name Name of the view
     * @return SearchAlias view handler
     * @since ArangoDB 3.10
     */
    SearchAlias searchAlias(String name);

    /**
     * Creates a view of the given {@code type}, then returns view information from the server.
     *
     * @param name The name of the view
     * @param type The type of the view
     * @return information about the view
     * @since ArangoDB 3.4.0
     */
    ViewEntity createView(String name, ViewType type);

    /**
     * Creates a ArangoSearch view with the given {@code options}, then returns view information from the server.
     *
     * @param name    The name of the view
     * @param options Additional options, can be null
     * @return information about the view
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/views/arangosearch-views/#create-an-arangosearch-view">API
     * Documentation</a>
     * @since ArangoDB 3.4.0
     */
    ViewEntity createArangoSearch(String name, ArangoSearchCreateOptions options);

    /**
     * Creates a SearchAlias view with the given {@code options}, then returns view information from the server.
     *
     * @param name    The name of the view
     * @param options Additional options, can be null
     * @return information about the view
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/views/search-alias-views/#create-a-search-alias-view">API
     * Documentation</a>
     * @since ArangoDB 3.10
     */
    ViewEntity createSearchAlias(String name, SearchAliasCreateOptions options);

    /**
     * Creates an Analyzer
     *
     * @param analyzer SearchAnalyzer
     * @return the created Analyzer
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/analyzers/#create-an-analyzer">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    SearchAnalyzer createSearchAnalyzer(SearchAnalyzer analyzer);

    /**
     * Gets information about an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @return information about an Analyzer
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/analyzers/#get-an-analyzer-definition">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    SearchAnalyzer getSearchAnalyzer(String name);

    /**
     * Retrieves all analyzers definitions.
     *
     * @return collection of all analyzers definitions
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/analyzers/#list-all-analyzers">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<SearchAnalyzer> getSearchAnalyzers();

    /**
     * Deletes an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/analyzers/#remove-an-analyzer">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteSearchAnalyzer(String name);

    /**
     * Deletes an Analyzer
     *
     * @param name    of the Analyzer without database prefix
     * @param options AnalyzerDeleteOptions
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/analyzers/#remove-an-analyzer">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteSearchAnalyzer(String name, AnalyzerDeleteOptions options);

}

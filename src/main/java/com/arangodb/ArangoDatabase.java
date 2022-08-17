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

import java.util.Collection;
import java.util.Map;

/**
 * Interface for operations on ArangoDB database level.
 *
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/database.html">Databases API Documentation</a>
 * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html">Query API Documentation</a>
 */
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
    DbName dbName();

    /**
     * Returns the server name and version number.
     *
     * @return the server version, number
     * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-version">API
     * Documentation</a>
     */
    ArangoDBVersion getVersion();

    /**
     * Returns the name of the used storage engine.
     *
     * @return the storage engine name
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-database-engine-type">API
     * Documentation</a>
     */
    ArangoDBEngine getEngine();

    /**
     * Checks whether the database exists
     *
     * @return true if the database exists, otherwise false
     */
    boolean exists();

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-accessible-databases">API
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
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
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
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity createCollection(String name, CollectionCreateOptions options);

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @return list of information about all collections
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#reads-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections();

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @param options Additional options, can be null
     * @return list of information about all collections
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#reads-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections(CollectionsReadOptions options);

    /**
     * Returns an index
     *
     * @param id The index-handle
     * @return information about the index
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-index">API Documentation</a>
     */
    IndexEntity getIndex(String id);

    /**
     * Deletes an index
     *
     * @param id The index-handle
     * @return the id of the index
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#delete-index">API Documentation</a>
     */
    String deleteIndex(String id);

    /**
     * Creates the database
     *
     * @return true if the database was created successfully.
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     */
    Boolean create();

    /**
     * Deletes the database from the server.
     *
     * @return true if the database was dropped successfully
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#drop-database">API
     * Documentation</a>
     */
    Boolean drop();

    /**
     * Grants or revoke access to the database for user {@code user}. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user, Permissions permissions);

    /**
     * Grants access to the database for user {@code user}. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user The name of the user
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user);

    /**
     * Revokes access to the database dbname for user {@code user}. You need permission to the _system database in order
     * to execute this call.
     *
     * @param user The name of the user
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void revokeAccess(String user);

    /**
     * Clear the database access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
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
     * @since ArangoDB 3.2.0
     */
    void grantDefaultCollectionAccess(String user, Permissions permissions);

    /**
     * Get specific database access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#get-the-database-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @param options  Additional options that will be passed to the query API, can be null
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, AqlQueryOptions options, Class<T> type);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query   An AQL query string
     * @param options Additional options that will be passed to the query API, can be null
     * @param type    The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, AqlQueryOptions options, Class<T> type);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @param type     The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, Class<T> type);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query An AQL query string
     * @param type  The type of the result (POJO or {@link com.arangodb.util.RawData})
     * @return cursor of the results
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
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
     * "https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors
     * .html#read-next-batch-from-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type);

    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#explain-an-aql-query">API
     * Documentation</a>
     */
    AqlExecutionExplainEntity explainQuery(String query, Map<String, Object> bindVars, AqlQueryExplainOptions options);

    /**
     * Parse an AQL query and return information about it This method is for query validation only. To actually query
     * the database, see {@link ArangoDatabase#query(String, Map, AqlQueryOptions, Class)}
     *
     * @param query the query which you want parse
     * @return imformation about the query
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#parse-an-aql-query">API
     * Documentation</a>
     */
    AqlParseEntity parseQuery(String query);

    /**
     * Clears the AQL query cache
     *
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache
     * .html#clears-any-results-in-the-aql-query-results-cache">API
     * Documentation</a>
     */
    void clearQueryCache();

    /**
     * Returns the global configuration for the AQL query cache
     *
     * @return configuration for the AQL query cache
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache
     * .html#returns-the-global-properties-for-the-aql-query-results-cache">API
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
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache
     * .html#globally-adjusts-the-aql-query-results-cache-properties">API
     * Documentation</a>
     */
    QueryCachePropertiesEntity setQueryCacheProperties(QueryCachePropertiesEntity properties);

    /**
     * Returns the configuration for the AQL query tracking
     *
     * @return configuration for the AQL query tracking
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity getQueryTrackingProperties();

    /**
     * Changes the configuration for the AQL query tracking
     *
     * @param properties properties to be set
     * @return current set of properties
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#changes-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity setQueryTrackingProperties(QueryTrackingPropertiesEntity properties);

    /**
     * Returns a list of currently running AQL queries
     *
     * @return a list of currently running AQL queries
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-currently-running-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getCurrentlyRunningQueries();

    /**
     * Returns a list of slow running AQL queries
     *
     * @return a list of slow running AQL queries
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getSlowQueries();

    /**
     * Clears the list of slow AQL queries
     *
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#clears-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    void clearSlowQueries();

    /**
     * Kills a running query. The query will be terminated at the next cancelation point.
     *
     * @param id The id of the query
     * @see <a href= "https://www.arangodb.com/docs/stable/http/aql-query.html#kills-a-running-aql-query">API
     * Documentation</a>
     */
    void killQuery(String id);

    /**
     * Create a new AQL user function
     *
     * @param name    A valid AQL function name, e.g.: `"myfuncs::accounting::calculate_vat"`
     * @param code    A String evaluating to a JavaScript function
     * @param options Additional options, can be null
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-user-functions.html#create-aql-user-function">API
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
     * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#remove-existing-aql-user-function">API
     * Documentation</a>
     */
    Integer deleteAqlFunction(String name, AqlFunctionDeleteOptions options);

    /**
     * Gets all reqistered AQL user functions
     *
     * @param options Additional options, can be null
     * @return all reqistered AQL user functions
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#return-registered-aql-user-functions">API
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
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions);

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options);

    /**
     * Lists all graphs known to the graph module
     *
     * @return graphs stored in this database
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#list-all-graphs">API
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
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-js-transaction.html#execute-transaction">API
     * Documentation</a>
     */
    <T> T transaction(String action, Class<T> type, TransactionOptions options);

    /**
     * Begins a Stream Transaction.
     *
     * @param options Additional options, can be null
     * @return information about the transaction
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#begin-a-transaction">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity beginStreamTransaction(StreamTransactionOptions options);

    /**
     * Aborts a Stream Transaction.
     *
     * @return information about the transaction
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#abort-transaction">API
     * Documentation</a>
     */
    StreamTransactionEntity abortStreamTransaction(String id);

    /**
     * Gets information about a Stream Transaction.
     *
     * @return information about the transaction
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#get-transaction-status">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity getStreamTransaction(String id);

    /**
     * Gets all the currently running Stream Transactions.
     *
     * @return all the currently running Stream Transactions
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#list-currently-ongoing-transactions">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<TransactionEntity> getStreamTransactions();

    /**
     * Commits a Stream Transaction.
     *
     * @return information about the transaction
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#commit-or-abort-a-transaction">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity commitStreamTransaction(String id);

    /**
     * Retrieves information about the current database
     *
     * @return information about the current database
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#information-of-the-database">API
     * Documentation</a>
     */
    DatabaseEntity getInfo();

    /**
     * Reads a single document
     *
     * @param id   The id of the document
     * @param type The type of the document (POJO or {@link com.arangodb.util.RawData})
     * @return the document identified by the id
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String id, Class<T> type);

    /**
     * Reads a single document
     *
     * @param id      The id of the document
     * @param type    The type of the document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return the document identified by the id
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String id, Class<T> type, DocumentReadOptions options);

    /**
     * Reload the routing table.
     *
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring
     * .html#reloads-the-routing-information">API
     * Documentation</a>
     */
    void reloadRouting();

    /**
     * Returns a new {@link ArangoRoute} instance for the given path (relative to the database) that can be used to
     * perform arbitrary requests.
     *
     * @param path The database-relative URL of the route
     * @return {@link ArangoRoute}
     */
    ArangoRoute route(String... path);

    /**
     * Fetches all views from the database and returns an list of view descriptions.
     *
     * @return list of information about all views
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#list-all-views">API Documentation</a>
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
     * Returns a {@code ArangoSearch} instance for the given ArangoSearch view name.
     *
     * @param name Name of the view
     * @return ArangoSearch view handler
     * @since ArangoDB 3.4.0
     */
    ArangoSearch arangoSearch(String name);

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
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#create-an-arangosearch-view">API
     * Documentation</a>
     * @since ArangoDB 3.4.0
     */
    ViewEntity createArangoSearch(String name, ArangoSearchCreateOptions options);

    /**
     * Creates an Analyzer
     *
     * @param analyzer SearchAnalyzer
     * @return the created Analyzer
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    SearchAnalyzer createSearchAnalyzer(SearchAnalyzer analyzer);

    /**
     * Gets information about an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @return information about an Analyzer
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    SearchAnalyzer getSearchAnalyzer(String name);

    /**
     * Retrieves all analyzers definitions.
     *
     * @return collection of all analyzers definitions
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<SearchAnalyzer> getSearchAnalyzers();

    /**
     * Deletes an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteSearchAnalyzer(String name);

    /**
     * Deletes an Analyzer
     *
     * @param name    of the Analyzer without database prefix
     * @param options AnalyzerDeleteOptions
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteSearchAnalyzer(String name, AnalyzerDeleteOptions options);

}

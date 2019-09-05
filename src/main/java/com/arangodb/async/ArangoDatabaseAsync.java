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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoSerializationAccessor;
import com.arangodb.entity.*;
import com.arangodb.entity.arangosearch.AnalyzerEntity;
import com.arangodb.model.*;
import com.arangodb.model.arangosearch.AnalyzerDeleteOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for operations on ArangoDB database level.
 *
 * @author Mark Vollmary
 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/">Databases API Documentation</a>
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/">Query API Documentation</a>
 */
@SuppressWarnings("unused")
public interface ArangoDatabaseAsync extends ArangoSerializationAccessor {

    /**
     * Return the main entry point for the ArangoDB driver
     *
     * @return main entry point
     */
    ArangoDBAsync arango();

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
     * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
     * Documentation</a>
     */
    CompletableFuture<ArangoDBVersion> getVersion();

    /**
     * Returns the name of the used storage engine.
     *
     * @return the storage engine name
     * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-database-engine-type">API
     * Documentation</a>
     */
    CompletableFuture<ArangoDBEngine> getEngine();

    /**
     * Checks whether the database exists
     *
     * @return true if the database exists, otherwise false
     */
    CompletableFuture<Boolean> exists();

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
     * Documentation</a>
     */
    CompletableFuture<Collection<String>> getAccessibleDatabases();

    /**
     * Returns a handler of the collection by the given name
     *
     * @param name Name of the collection
     * @return collection handler
     */
    ArangoCollectionAsync collection(final String name);

    /**
     * Creates a collection
     *
     * @param name The name of the collection
     * @return information about the collection
     * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> createCollection(final String name);

    /**
     * Creates a collection
     *
     * @param name    The name of the collection
     * @param options Additional options, can be null
     * @return information about the collection
     * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> createCollection(final String name, final CollectionCreateOptions options);

    /**
     * Returns all collections
     *
     * @return list of information about all collections
     * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
     * Documentation</a>
     */
    CompletableFuture<Collection<CollectionEntity>> getCollections();

    /**
     * Returns all collections
     *
     * @param options Additional options, can be null
     * @return list of information about all collections
     * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
     * Documentation</a>
     */
    CompletableFuture<Collection<CollectionEntity>> getCollections(final CollectionsReadOptions options);

    /**
     * Returns an index
     *
     * @param id The index-handle
     * @return information about the index
     * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
     */
    CompletableFuture<IndexEntity> getIndex(final String id);

    /**
     * Deletes an index
     *
     * @param id The index handle
     * @return the id of the index
     * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
     */
    CompletableFuture<String> deleteIndex(final String id);

    /**
     * Creates the database
     *
     * @return true if the database was created successfully.
     * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#create-database">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> create();

    /**
     * Drop an existing database
     *
     * @return true if the database was dropped successfully
     * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#drop-database">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> drop();

    /**
     * Grants access to the database dbname for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @return void
     * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
     * API Documentation</a>
     */
    CompletableFuture<Void> grantAccess(final String user, final Permissions permissions);

    /**
     * Grants access to the database dbname for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user The name of the user
     * @return void
     * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
     * API Documentation</a>
     */
    CompletableFuture<Void> grantAccess(final String user);

    /**
     * Revokes access to the database dbname for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user The name of the user
     * @return void
     * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
     * API Documentation</a>
     */
    CompletableFuture<Void> revokeAccess(final String user);

    /**
     * Clear the database access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @return void
     * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Void> resetAccess(final String user);

    /**
     * Sets the default access level for collections within this database for the user <code>user</code>. You need
     * permission to the _system database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Void> grantDefaultCollectionAccess(final String user, final Permissions permissions);

    /**
     * Get specific database access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/#get-the-database-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Permissions> getPermissions(final String user);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    contains the query string to be executed
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @param type     The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return cursor of the results
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> CompletableFuture<ArangoCursorAsync<T>> query(
            final String query,
            final Map<String, Object> bindVars,
            final AqlQueryOptions options,
            final Class<T> type);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query   contains the query string to be executed
     * @param options Additional options, can be null
     * @param type    The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return cursor of the results
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> CompletableFuture<ArangoCursorAsync<T>> query(
            final String query,
            final AqlQueryOptions options,
            final Class<T> type);

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    contains the query string to be executed
     * @param bindVars key/value pairs representing the bind parameters
     * @param type     The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return cursor of the results
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> CompletableFuture<ArangoCursorAsync<T>> query(
            final String query,
            final Map<String, Object> bindVars,
            final Class<T> type);

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query contains the query string to be executed
     * @param type  The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return cursor of the results
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> CompletableFuture<ArangoCursorAsync<T>> query(final String query, final Class<T> type);

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId The ID of the cursor
     * @param type     The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
     * @return cursor of the results
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#read-next-batch-from-cursor">API
     * Documentation</a>
     */
    <T> CompletableFuture<ArangoCursorAsync<T>> cursor(final String cursorId, final Class<T> type);

    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#explain-an-aql-query">API
     * Documentation</a>
     */
    CompletableFuture<AqlExecutionExplainEntity> explainQuery(
            final String query,
            final Map<String, Object> bindVars,
            final AqlQueryExplainOptions options);

    /**
     * Parse an AQL query and return information about it This method is for query validation only. To actually query
     * the database, see {@link ArangoDatabaseAsync#query(String, Map, AqlQueryOptions, Class)}
     *
     * @param query the query which you want parse
     * @return imformation about the query
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#parse-an-aql-query">API
     * Documentation</a>
     */
    CompletableFuture<AqlParseEntity> parseQuery(final String query);

    /**
     * Clears the AQL query cache
     *
     * @return void
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#clears-any-results-in-the-aql-query-cache">API
     * Documentation</a>
     */
    CompletableFuture<Void> clearQueryCache();

    /**
     * Returns the global configuration for the AQL query cache
     *
     * @return configuration for the AQL query cache
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#returns-the-global-properties-for-the-aql-query-cache">API
     * Documentation</a>
     */
    CompletableFuture<QueryCachePropertiesEntity> getQueryCacheProperties();

    /**
     * Changes the configuration for the AQL query cache. Note: changing the properties may invalidate all results in
     * the cache.
     *
     * @param properties properties to be set
     * @return current set of properties
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#globally-adjusts-the-aql-query-result-cache-properties">API
     * Documentation</a>
     */
    CompletableFuture<QueryCachePropertiesEntity> setQueryCacheProperties(final QueryCachePropertiesEntity properties);

    /**
     * Returns the configuration for the AQL query tracking
     *
     * @return configuration for the AQL query tracking
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    CompletableFuture<QueryTrackingPropertiesEntity> getQueryTrackingProperties();

    /**
     * Changes the configuration for the AQL query tracking
     *
     * @param properties properties to be set
     * @return current set of properties
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#changes-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    CompletableFuture<QueryTrackingPropertiesEntity> setQueryTrackingProperties(
            final QueryTrackingPropertiesEntity properties);

    /**
     * Returns a list of currently running AQL queries
     *
     * @return a list of currently running AQL queries
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-currently-running-aql-queries">API
     * Documentation</a>
     */
    CompletableFuture<Collection<QueryEntity>> getCurrentlyRunningQueries();

    /**
     * Returns a list of slow running AQL queries
     *
     * @return a list of slow running AQL queries
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    CompletableFuture<Collection<QueryEntity>> getSlowQueries();

    /**
     * Clears the list of slow AQL queries
     *
     * @return void
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#clears-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    CompletableFuture<Void> clearSlowQueries();

    /**
     * Kills a running query. The query will be terminated at the next cancelation point.
     *
     * @param id The id of the query
     * @return void
     * @see <a href= "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#kills-a-running-aql-query">API
     * Documentation</a>
     */
    CompletableFuture<Void> killQuery(final String id);

    /**
     * Create a new AQL user function
     *
     * @param name    the fully qualified name of the user functions
     * @param code    a string representation of the function body
     * @param options Additional options, can be null
     * @return void
     * @see <a href="https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#create-aql-user-function">API
     * Documentation</a>
     */
    CompletableFuture<Void> createAqlFunction(
            final String name,
            final String code,
            final AqlFunctionCreateOptions options);

    /**
     * Remove an existing AQL user function
     *
     * @param name    the name of the AQL user function
     * @param options Additional options, can be null
     * @return number of deleted functions (since ArangoDB 3.4.0)
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#remove-existing-aql-user-function">API
     * Documentation</a>
     */
    CompletableFuture<Integer> deleteAqlFunction(final String name, final AqlFunctionDeleteOptions options);

    /**
     * Gets all reqistered AQL user functions
     *
     * @param options Additional options, can be null
     * @return all reqistered AQL user functions
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#return-registered-aql-user-functions">API
     * Documentation</a>
     */
    CompletableFuture<Collection<AqlFunctionEntity>> getAqlFunctions(final AqlFunctionGetOptions options);

    /**
     * Returns a handler of the graph by the given name
     *
     * @param name Name of the graph
     * @return graph handler
     */
    ArangoGraphAsync graph(final String name);

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> createGraph(final String name, final Collection<EdgeDefinition> edgeDefinitions);

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
     * Documentation</a>
     */
    CompletableFuture<GraphEntity> createGraph(
            final String name,
            final Collection<EdgeDefinition> edgeDefinitions,
            final GraphCreateOptions options);

    /**
     * Lists all graphs known to the graph module
     *
     * @return graphs stored in this database
     * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-all-graphs">API
     * Documentation</a>
     */
    CompletableFuture<Collection<GraphEntity>> getGraphs();

    /**
     * Execute a server-side transaction
     *
     * @param action  the actual transaction operations to be executed, in the form of stringified JavaScript code
     * @param type    The type of the result (POJO class, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return the result of the transaction if it succeeded
     * @see <a href="https://docs.arangodb.com/current/HTTP/Transaction/index.html#execute-transaction">API
     * Documentation</a>
     */
    <T> CompletableFuture<T> transaction(final String action, final Class<T> type, final TransactionOptions options);

    /**
     * Begins a Stream Transaction.
     *
     * @param options Additional options, can be null
     * @return information about the transaction
     * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#begin-a-transaction">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<StreamTransactionEntity> beginStreamTransaction(StreamTransactionOptions options);

    /**
     * Aborts a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#abort-transaction">API
     * Documentation</a>
     */
    CompletableFuture<StreamTransactionEntity> abortStreamTransaction(String id);

    /**
     * Gets information about a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#get-transaction-status">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<StreamTransactionEntity> getStreamTransaction(String id);

    /**
     * Gets all the currently running Stream Transactions.
     *
     * @return all the currently running Stream Transactions
     * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#list-currently-ongoing-transactions">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<Collection<TransactionEntity>> getStreamTransactions();

    /**
     * Commits a Stream Transaction.
     *
     * @return information about the transaction
     * @see <a href="https://docs.arangodb.com/current/HTTP/transaction-stream-transaction.html#commit-or-abort-a-transaction">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<StreamTransactionEntity> commitStreamTransaction(String id);

    /**
     * Retrieves information about the current database
     *
     * @return information about the current database
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
     * Documentation</a>
     */
    CompletableFuture<DatabaseEntity> getInfo();

    /**
     * Execute a server-side traversal
     *
     * @param vertexClass The type of the vertex documents (POJO class, VPackSlice or String for Json)
     * @param edgeClass   The type of the edge documents (POJO class, VPackSlice or String for Json)
     * @param options     Additional options
     * @return Result of the executed traversal
     * @see <a href= "https://docs.arangodb.com/current/HTTP/Traversal/index.html#executes-a-traversal">API
     * Documentation</a>
     */
    <V, E> CompletableFuture<TraversalEntity<V, E>> executeTraversal(
            final Class<V> vertexClass,
            final Class<E> edgeClass,
            final TraversalOptions options);

    /**
     * Reads a single document
     *
     * @param id   The id of the document
     * @param type The type of the document (POJO class, VPackSlice or String for Json)
     * @return the document identified by the id
     * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<T> getDocument(final String id, final Class<T> type) throws ArangoDBException;

    /**
     * Reads a single document
     *
     * @param id      The id of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return the document identified by the id
     * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<T> getDocument(final String id, final Class<T> type, final DocumentReadOptions options)
            throws ArangoDBException;

    /**
     * Reload the routing table.
     *
     * @return void
     * @see <a href=
     * "https://docs.arangodb.com/current/HTTP/AdministrationAndMonitoring/index.html#reloads-the-routing-information">API
     * Documentation</a>
     */
    CompletableFuture<Void> reloadRouting();

    /**
     * Returns a new {@link ArangoRouteAsync} instance for the given path (relative to the database) that can be used to
     * perform arbitrary requests.
     *
     * @param path The database-relative URL of the route
     * @return {@link ArangoRouteAsync}
     */
    ArangoRouteAsync route(String... path);

    /**
     * Fetches all views from the database and returns an list of view descriptions.
     *
     * @return list of information about all views
     * @see <a href="https://docs.arangodb.com/current/HTTP/Views/Getting.html#reads-all-views">API Documentation</a>
     * @since ArangoDB 3.4.0
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
     * Returns a {@code ArangoSearchAsync} instance for the given ArangoSearch view name.
     *
     * @param name Name of the view
     * @return ArangoSearch view handler
     * @since ArangoDB 3.4.0
     */
    ArangoSearchAsync arangoSearch(String name);

    /**
     * Creates a view of the given {@code type}, then returns view information from the server.
     *
     * @param name The name of the view
     * @param type The type of the view
     * @return information about the view
     * @since ArangoDB 3.4.0
     */
    CompletableFuture<ViewEntity> createView(String name, ViewType type);

    /**
     * Creates a ArangoSearch view with the given {@code options}, then returns view information from the server.
     *
     * @param name    The name of the view
     * @param options Additional options, can be null
     * @return information about the view
     * @see <a href="https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#create-arangosearch-view">API
     * Documentation</a>
     * @since ArangoDB 3.4.0
     */
    CompletableFuture<ViewEntity> createArangoSearch(String name, ArangoSearchCreateOptions options);

    /**
     * Creates an Analyzer
     *
     * @param options AnalyzerEntity
     * @return the created Analyzer
     * @see <a href="https://docs.arangodb.com/current/HTTP/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<AnalyzerEntity> createAnalyzer(AnalyzerEntity options);

    /**
     * Gets information about an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @return information about an Analyzer
     * @see <a href="https://docs.arangodb.com/current/HTTP/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<AnalyzerEntity> getAnalyzer(String name);

    /**
     * Retrieves all analyzers definitions.
     *
     * @return collection of all analyzers definitions
     * @see <a href="https://docs.arangodb.com/current/HTTP/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<Collection<AnalyzerEntity>> getAnalyzers();

    /**
     * Deletes an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @see <a href="https://docs.arangodb.com/current/HTTP/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<Void> deleteAnalyzer(String name);

    /**
     * Deletes an Analyzer
     *
     * @param name    of the Analyzer without database prefix
     * @param options AnalyzerDeleteOptions
     * @see <a href="https://docs.arangodb.com/current/HTTP/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<Void> deleteAnalyzer(String name, AnalyzerDeleteOptions options);

}

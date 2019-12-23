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
import com.arangodb.entity.arangosearch.AnalyzerEntity;
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
@SuppressWarnings("UnusedReturnValue")
public interface ArangoDatabase extends ArangoSerializationAccessor {

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
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-version">API
     * Documentation</a>
     */
    ArangoDBVersion getVersion() throws ArangoDBException;

    /**
     * Returns the name of the used storage engine.
     *
     * @return the storage engine name
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/miscellaneous-functions.html#return-server-database-engine-type">API
     * Documentation</a>
     */
    ArangoDBEngine getEngine() throws ArangoDBException;

    /**
     * Checks whether the database exists
     *
     * @return true if the database exists, otherwise false
     */
    boolean exists() throws ArangoDBException;

    /**
     * Retrieves a list of all databases the current user can access
     *
     * @return a list of all databases the current user can access
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#list-of-accessible-databases">API
     * Documentation</a>
     */
    Collection<String> getAccessibleDatabases() throws ArangoDBException;

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
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity createCollection(String name) throws ArangoDBException;

    /**
     * Creates a collection with the given {@code options} for this collection's name, then returns collection
     * information from the server.
     *
     * @param name    The name of the collection
     * @param options Additional options, can be null
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity createCollection(String name, CollectionCreateOptions options) throws ArangoDBException;

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @return list of information about all collections
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#reads-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections() throws ArangoDBException;

    /**
     * Fetches all collections from the database and returns an list of collection descriptions.
     *
     * @param options Additional options, can be null
     * @return list of information about all collections
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#reads-all-collections">API
     * Documentation</a>
     */
    Collection<CollectionEntity> getCollections(CollectionsReadOptions options) throws ArangoDBException;

    /**
     * Returns an index
     *
     * @param id The index-handle
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-index">API Documentation</a>
     */
    IndexEntity getIndex(String id) throws ArangoDBException;

    /**
     * Deletes an index
     *
     * @param id The index-handle
     * @return the id of the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#delete-index">API Documentation</a>
     */
    String deleteIndex(String id) throws ArangoDBException;

    /**
     * Creates the database
     *
     * @return true if the database was created successfully.
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#create-database">API
     * Documentation</a>
     */
    Boolean create() throws ArangoDBException;

    /**
     * Deletes the database from the server.
     *
     * @return true if the database was dropped successfully
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/database-database-management.html#drop-database">API
     * Documentation</a>
     */
    Boolean drop() throws ArangoDBException;

    /**
     * Grants or revoke access to the database for user {@code user}. You need permission to the _system database in
     * order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user, Permissions permissions) throws ArangoDBException;

    /**
     * Grants access to the database for user {@code user}. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user The name of the user
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void grantAccess(String user) throws ArangoDBException;

    /**
     * Revokes access to the database dbname for user {@code user}. You need permission to the _system database in order
     * to execute this call.
     *
     * @param user The name of the user
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     */
    void revokeAccess(String user) throws ArangoDBException;

    /**
     * Clear the database access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-database-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void resetAccess(String user) throws ArangoDBException;

    /**
     * Sets the default access level for collections within this database for the user {@code user}. You need permission
     * to the _system database in order to execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @throws ArangoDBException
     * @since ArangoDB 3.2.0
     */
    void grantDefaultCollectionAccess(String user, Permissions permissions) throws ArangoDBException;

    /**
     * Get specific database access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#get-the-database-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user) throws ArangoDBException;

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @param options  Additional options that will be passed to the query API, can be null
     * @param type     The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
     * @return cursor of the results
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, AqlQueryOptions options, Class<T> type)
            throws ArangoDBException;

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query   An AQL query string
     * @param options Additional options that will be passed to the query API, can be null
     * @param type    The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
     * @return cursor of the results
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, AqlQueryOptions options, Class<T> type) throws ArangoDBException;

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
     * {@code ArangoCursor} instance for the result list.
     *
     * @param query    An AQL query string
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @param type     The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
     * @return cursor of the results
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, Class<T> type) throws ArangoDBException;

    /**
     * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
     * result list.
     *
     * @param query An AQL query string
     * @param type  The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
     * @return cursor of the results
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#create-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> query(String query, Class<T> type) throws ArangoDBException;

    /**
     * Return an cursor from the given cursor-ID if still existing
     *
     * @param cursorId The ID of the cursor
     * @param type     The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
     * @return cursor of the results
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cursor-accessing-cursors.html#read-next-batch-from-cursor">API
     * Documentation</a>
     */
    <T> ArangoCursor<T> cursor(String cursorId, Class<T> type) throws ArangoDBException;

    /**
     * Explain an AQL query and return information about it
     *
     * @param query    the query which you want explained
     * @param bindVars key/value pairs representing the bind parameters
     * @param options  Additional options, can be null
     * @return information about the query
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#explain-an-aql-query">API
     * Documentation</a>
     */
    AqlExecutionExplainEntity explainQuery(String query, Map<String, Object> bindVars, AqlQueryExplainOptions options)
            throws ArangoDBException;

    /**
     * Parse an AQL query and return information about it This method is for query validation only. To actually query
     * the database, see {@link ArangoDatabase#query(String, Map, AqlQueryOptions, Class)}
     *
     * @param query the query which you want parse
     * @return imformation about the query
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-query.html#parse-an-aql-query">API
     * Documentation</a>
     */
    AqlParseEntity parseQuery(String query) throws ArangoDBException;

    /**
     * Clears the AQL query cache
     *
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache.html#clears-any-results-in-the-aql-query-results-cache">API
     * Documentation</a>
     */
    void clearQueryCache() throws ArangoDBException;

    /**
     * Returns the global configuration for the AQL query cache
     *
     * @return configuration for the AQL query cache
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache.html#returns-the-global-properties-for-the-aql-query-results-cache">API
     * Documentation</a>
     */
    QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoDBException;

    /**
     * Changes the configuration for the AQL query cache. Note: changing the properties may invalidate all results in
     * the cache.
     *
     * @param properties properties to be set
     * @return current set of properties
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query-cache.html#globally-adjusts-the-aql-query-results-cache-properties">API
     * Documentation</a>
     */
    QueryCachePropertiesEntity setQueryCacheProperties(QueryCachePropertiesEntity properties) throws ArangoDBException;

    /**
     * Returns the configuration for the AQL query tracking
     *
     * @return configuration for the AQL query tracking
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity getQueryTrackingProperties() throws ArangoDBException;

    /**
     * Changes the configuration for the AQL query tracking
     *
     * @param properties properties to be set
     * @return current set of properties
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#changes-the-properties-for-the-aql-query-tracking">API
     * Documentation</a>
     */
    QueryTrackingPropertiesEntity setQueryTrackingProperties(QueryTrackingPropertiesEntity properties)
            throws ArangoDBException;

    /**
     * Returns a list of currently running AQL queries
     *
     * @return a list of currently running AQL queries
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-currently-running-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getCurrentlyRunningQueries() throws ArangoDBException;

    /**
     * Returns a list of slow running AQL queries
     *
     * @return a list of slow running AQL queries
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#returns-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    Collection<QueryEntity> getSlowQueries() throws ArangoDBException;

    /**
     * Clears the list of slow AQL queries
     *
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-query.html#clears-the-list-of-slow-aql-queries">API
     * Documentation</a>
     */
    void clearSlowQueries() throws ArangoDBException;

    /**
     * Kills a running query. The query will be terminated at the next cancelation point.
     *
     * @param id The id of the query
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/aql-query.html#kills-a-running-aql-query">API
     * Documentation</a>
     */
    void killQuery(String id) throws ArangoDBException;

    /**
     * Create a new AQL user function
     *
     * @param name    A valid AQL function name, e.g.: `"myfuncs::accounting::calculate_vat"`
     * @param code    A String evaluating to a JavaScript function
     * @param options Additional options, can be null
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/aql-user-functions.html#create-aql-user-function">API
     * Documentation</a>
     */
    void createAqlFunction(String name, String code, AqlFunctionCreateOptions options) throws ArangoDBException;

    /**
     * Deletes the AQL user function with the given name from the database.
     *
     * @param name    The name of the user function to delete
     * @param options Additional options, can be null
     * @return number of deleted functions (since ArangoDB 3.4.0)
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#remove-existing-aql-user-function">API
     * Documentation</a>
     */
    Integer deleteAqlFunction(String name, AqlFunctionDeleteOptions options) throws ArangoDBException;

    /**
     * Gets all reqistered AQL user functions
     *
     * @param options Additional options, can be null
     * @return all reqistered AQL user functions
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/aql-user-functions.html#return-registered-aql-user-functions">API
     * Documentation</a>
     */
    Collection<AqlFunctionEntity> getAqlFunctions(AqlFunctionGetOptions options) throws ArangoDBException;

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
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions) throws ArangoDBException;

    /**
     * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
     * of its edges.
     *
     * @param name            Name of the graph
     * @param edgeDefinitions An array of definitions for the edge
     * @param options         Additional options, can be null
     * @return information about the graph
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#create-a-graph">API
     * Documentation</a>
     */
    GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options)
            throws ArangoDBException;

    /**
     * Lists all graphs known to the graph module
     *
     * @return graphs stored in this database
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/gharial-management.html#list-all-graphs">API
     * Documentation</a>
     */
    Collection<GraphEntity> getGraphs() throws ArangoDBException;

    /**
     * Performs a server-side transaction and returns its return value.
     *
     * @param action  A String evaluating to a JavaScript function to be executed on the server.
     * @param type    The type of the result (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return the result of the transaction if it succeeded
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-js-transaction.html#execute-transaction">API
     * Documentation</a>
     */
    <T> T transaction(String action, Class<T> type, TransactionOptions options) throws ArangoDBException;

    /**
     * Begins a Stream Transaction.
     *
     * @param options Additional options, can be null
     * @return information about the transaction
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#begin-a-transaction">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity beginStreamTransaction(StreamTransactionOptions options) throws ArangoDBException;

    /**
     * Aborts a Stream Transaction.
     *
     * @return information about the transaction
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#abort-transaction">API
     * Documentation</a>
     */
    StreamTransactionEntity abortStreamTransaction(String id) throws ArangoDBException;

    /**
     * Gets information about a Stream Transaction.
     *
     * @return information about the transaction
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#get-transaction-status">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity getStreamTransaction(String id) throws ArangoDBException;

    /**
     * Gets all the currently running Stream Transactions.
     *
     * @return all the currently running Stream Transactions
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#list-currently-ongoing-transactions">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<TransactionEntity> getStreamTransactions() throws ArangoDBException;

    /**
     * Commits a Stream Transaction.
     *
     * @return information about the transaction
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/transaction-stream-transaction.html#commit-or-abort-a-transaction">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    StreamTransactionEntity commitStreamTransaction(String id) throws ArangoDBException;

    /**
     * Retrieves information about the current database
     *
     * @return information about the current database
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/database-database-management.html#information-of-the-database">API
     * Documentation</a>
     */
    DatabaseEntity getInfo() throws ArangoDBException;

    /**
     * Execute a server-side traversal
     *
     * @param vertexClass The type of the vertex documents (POJO class, VPackSlice or String for JSON)
     * @param edgeClass   The type of the edge documents (POJO class, VPackSlice or String for JSON)
     * @param options     Additional options
     * @return Result of the executed traversal
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/traversal.html#executes-a-traversal">API
     * Documentation</a>
     */
    <V, E> TraversalEntity<V, E> executeTraversal(Class<V> vertexClass, Class<E> edgeClass, TraversalOptions options)
            throws ArangoDBException;

    /**
     * Reads a single document
     *
     * @param id   The id of the document
     * @param type The type of the document (POJO class, VPackSlice or String for JSON)
     * @return the document identified by the id
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String id, Class<T> type) throws ArangoDBException;

    /**
     * Reads a single document
     *
     * @param id      The id of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return the document identified by the id
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String id, Class<T> type, DocumentReadOptions options) throws ArangoDBException;

    /**
     * Reload the routing table.
     *
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/administration-and-monitoring.html#reloads-the-routing-information">API
     * Documentation</a>
     */
    void reloadRouting() throws ArangoDBException;

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
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#list-all-views">API Documentation</a>
     * @since ArangoDB 3.4.0
     */
    Collection<ViewEntity> getViews() throws ArangoDBException;

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
     * @throws ArangoDBException
     * @since ArangoDB 3.4.0
     */
    ViewEntity createView(String name, ViewType type) throws ArangoDBException;

    /**
     * Creates a ArangoSearch view with the given {@code options}, then returns view information from the server.
     *
     * @param name    The name of the view
     * @param options Additional options, can be null
     * @return information about the view
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html#create-an-arangosearch-view">API
     * Documentation</a>
     * @since ArangoDB 3.4.0
     */
    ViewEntity createArangoSearch(String name, ArangoSearchCreateOptions options) throws ArangoDBException;

    /**
     * Creates an Analyzer
     *
     * @param options AnalyzerEntity
     * @return the created Analyzer
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    AnalyzerEntity createAnalyzer(AnalyzerEntity options) throws ArangoDBException;

    /**
     * Gets information about an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @return information about an Analyzer
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    AnalyzerEntity getAnalyzer(String name) throws ArangoDBException;

    /**
     * Retrieves all analyzers definitions.
     *
     * @return collection of all analyzers definitions
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    Collection<AnalyzerEntity> getAnalyzers() throws ArangoDBException;

    /**
     * Deletes an Analyzer
     *
     * @param name of the Analyzer without database prefix
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteAnalyzer(String name) throws ArangoDBException;

    /**
     * Deletes an Analyzer
     *
     * @param name    of the Analyzer without database prefix
     * @param options AnalyzerDeleteOptions
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/analyzers.html">API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    void deleteAnalyzer(String name, AnalyzerDeleteOptions options) throws ArangoDBException;

}

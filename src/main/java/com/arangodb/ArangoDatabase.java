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

import java.util.Collection;
import java.util.Map;

import com.arangodb.entity.AqlExecutionExplainEntity;
import com.arangodb.entity.AqlFunctionEntity;
import com.arangodb.entity.AqlParseEntity;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import com.arangodb.model.AqlFunctionCreateOptions;
import com.arangodb.model.AqlFunctionDeleteOptions;
import com.arangodb.model.AqlFunctionGetOptions;
import com.arangodb.model.AqlQueryExplainOptions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;

/**
 * Interface for operations on ArangoDB database level.
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/">Databases API Documentation</a>
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/">Query API Documentation</a>
 * @author Mark Vollmary
 */
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
	 * @see <a href="https://docs.arangodb.com/current/HTTP/MiscellaneousFunctions/index.html#return-server-version">API
	 *      Documentation</a>
	 * @return the server version, number
	 * @throws ArangoDBException
	 */
	ArangoDBVersion getVersion() throws ArangoDBException;

	/**
	 * Checks whether the database exists
	 * 
	 * @return true if the database exists, otherwise false
	 */
	boolean exists() throws ArangoDBException;

	/**
	 * Retrieves a list of all databases the current user can access
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#list-of-accessible-databases">API
	 *      Documentation</a>
	 * @return a list of all databases the current user can access
	 * @throws ArangoDBException
	 */
	Collection<String> getAccessibleDatabases() throws ArangoDBException;

	/**
	 * Returns a {@code ArangoCollection} instance for the given collection name.
	 * 
	 * @param name
	 *            Name of the collection
	 * @return collection handler
	 */
	ArangoCollection collection(String name);

	/**
	 * Creates a collection for the given collection's name, then returns collection information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity createCollection(String name) throws ArangoDBException;

	/**
	 * Creates a collection with the given {@code options} for this collection's name, then returns collection
	 * information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @param options
	 *            Additional options, can be null
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity createCollection(String name, CollectionCreateOptions options) throws ArangoDBException;

	/**
	 * Fetches all collections from the database and returns an list of collection descriptions.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @return list of information about all collections
	 * @throws ArangoDBException
	 */
	Collection<CollectionEntity> getCollections() throws ArangoDBException;

	/**
	 * Fetches all collections from the database and returns an list of collection descriptions.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return list of information about all collections
	 * @throws ArangoDBException
	 */
	Collection<CollectionEntity> getCollections(CollectionsReadOptions options) throws ArangoDBException;

	/**
	 * Returns an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity getIndex(String id) throws ArangoDBException;

	/**
	 * Deletes an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return the id of the index
	 * @throws ArangoDBException
	 */
	String deleteIndex(String id) throws ArangoDBException;

	/**
	 * Creates the database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#create-database">API
	 *      Documentation</a>
	 * @return true if the database was created successfully.
	 * @throws ArangoDBException
	 */
	Boolean create() throws ArangoDBException;

	/**
	 * Deletes the database from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#drop-database">API
	 *      Documentation</a>
	 * @return true if the database was dropped successfully
	 * @throws ArangoDBException
	 */
	Boolean drop() throws ArangoDBException;

	/**
	 * Grants or revoke access to the database for user {@code user}. You need permission to the _system database in
	 * order to execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @throws ArangoDBException
	 */
	void grantAccess(String user, Permissions permissions) throws ArangoDBException;

	/**
	 * Grants access to the database for user {@code user}. You need permission to the _system database in order to
	 * execute this call.
	 *
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	void grantAccess(String user) throws ArangoDBException;

	/**
	 * Revokes access to the database dbname for user {@code user}. You need permission to the _system database in order
	 * to execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	void revokeAccess(String user) throws ArangoDBException;

	/**
	 * Clear the database access level, revert back to the default access level.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	void resetAccess(String user) throws ArangoDBException;

	/**
	 * Sets the default access level for collections within this database for the user {@code user}. You need permission
	 * to the _system database in order to execute this call.
	 * 
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	void grantDefaultCollectionAccess(String user, Permissions permissions) throws ArangoDBException;

	/**
	 * Get specific database access level
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/#get-the-database-access-level"> API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return permissions of the user
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	Permissions getPermissions(String user) throws ArangoDBException;

	/**
	 * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
	 * {@code ArangoCursor} instance for the result list.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            An AQL query string
	 * @param bindVars
	 *            key/value pairs defining the variables to bind the query to
	 * @param options
	 *            Additional options that will be passed to the query API, can be null
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	<T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, AqlQueryOptions options, Class<T> type)
			throws ArangoDBException;

	/**
	 * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
	 * result list.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            An AQL query string
	 * @param options
	 *            Additional options that will be passed to the query API, can be null
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	<T> ArangoCursor<T> query(String query, AqlQueryOptions options, Class<T> type) throws ArangoDBException;

	/**
	 * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new
	 * {@code ArangoCursor} instance for the result list.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            An AQL query string
	 * @param bindVars
	 *            key/value pairs defining the variables to bind the query to
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	<T> ArangoCursor<T> query(String query, Map<String, Object> bindVars, Class<T> type) throws ArangoDBException;

	/**
	 * Performs a database query using the given {@code query}, then returns a new {@code ArangoCursor} instance for the
	 * result list.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            An AQL query string
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	<T> ArangoCursor<T> query(String query, Class<T> type) throws ArangoDBException;

	/**
	 * Return an cursor from the given cursor-ID if still existing
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#read-next-batch-from-cursor">API
	 *      Documentation</a>
	 * @param cursorId
	 *            The ID of the cursor
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for JSON, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	<T> ArangoCursor<T> cursor(String cursorId, Class<T> type) throws ArangoDBException;

	/**
	 * Explain an AQL query and return information about it
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#explain-an-aql-query">API
	 *      Documentation</a>
	 * @param query
	 *            the query which you want explained
	 * @param bindVars
	 *            key/value pairs representing the bind parameters
	 * @param options
	 *            Additional options, can be null
	 * @return information about the query
	 * @throws ArangoDBException
	 */
	AqlExecutionExplainEntity explainQuery(String query, Map<String, Object> bindVars, AqlQueryExplainOptions options)
			throws ArangoDBException;

	/**
	 * Parse an AQL query and return information about it This method is for query validation only. To actually query
	 * the database, see {@link ArangoDatabase#query(String, Map, AqlQueryOptions, Class)}
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#parse-an-aql-query">API
	 *      Documentation</a>
	 * @param query
	 *            the query which you want parse
	 * @return imformation about the query
	 * @throws ArangoDBException
	 */
	AqlParseEntity parseQuery(String query) throws ArangoDBException;

	/**
	 * Clears the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#clears-any-results-in-the-aql-query-cache">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	void clearQueryCache() throws ArangoDBException;

	/**
	 * Returns the global configuration for the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#returns-the-global-properties-for-the-aql-query-cache">API
	 *      Documentation</a>
	 * @return configuration for the AQL query cache
	 * @throws ArangoDBException
	 */
	QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoDBException;

	/**
	 * Changes the configuration for the AQL query cache. Note: changing the properties may invalidate all results in
	 * the cache.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#globally-adjusts-the-aql-query-result-cache-properties">API
	 *      Documentation</a>
	 * @param properties
	 *            properties to be set
	 * @return current set of properties
	 * @throws ArangoDBException
	 */
	QueryCachePropertiesEntity setQueryCacheProperties(QueryCachePropertiesEntity properties) throws ArangoDBException;

	/**
	 * Returns the configuration for the AQL query tracking
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-properties-for-the-aql-query-tracking">API
	 *      Documentation</a>
	 * @return configuration for the AQL query tracking
	 * @throws ArangoDBException
	 */
	QueryTrackingPropertiesEntity getQueryTrackingProperties() throws ArangoDBException;

	/**
	 * Changes the configuration for the AQL query tracking
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#changes-the-properties-for-the-aql-query-tracking">API
	 *      Documentation</a>
	 * @param properties
	 *            properties to be set
	 * @return current set of properties
	 * @throws ArangoDBException
	 */
	QueryTrackingPropertiesEntity setQueryTrackingProperties(QueryTrackingPropertiesEntity properties)
			throws ArangoDBException;

	/**
	 * Returns a list of currently running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-currently-running-aql-queries">API
	 *      Documentation</a>
	 * @return a list of currently running AQL queries
	 * @throws ArangoDBException
	 */
	Collection<QueryEntity> getCurrentlyRunningQueries() throws ArangoDBException;

	/**
	 * Returns a list of slow running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @return a list of slow running AQL queries
	 * @throws ArangoDBException
	 */
	Collection<QueryEntity> getSlowQueries() throws ArangoDBException;

	/**
	 * Clears the list of slow AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#clears-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	void clearSlowQueries() throws ArangoDBException;

	/**
	 * Kills a running query. The query will be terminated at the next cancelation point.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#kills-a-running-aql-query">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the query
	 * @throws ArangoDBException
	 */
	void killQuery(String id) throws ArangoDBException;

	/**
	 * Create a new AQL user function
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#create-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            A valid AQL function name, e.g.: `"myfuncs::accounting::calculate_vat"`
	 * @param code
	 *            A String evaluating to a JavaScript function
	 * @param options
	 *            Additional options, can be null
	 * @throws ArangoDBException
	 */
	void createAqlFunction(String name, String code, AqlFunctionCreateOptions options) throws ArangoDBException;

	/**
	 * Deletes the AQL user function with the given name from the database.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#remove-existing-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the user function to delete
	 * @param options
	 *            Additional options, can be null
	 * @return number of deleted functions (since ArangoDB 3.4.0)
	 * @throws ArangoDBException
	 */
	Integer deleteAqlFunction(String name, AqlFunctionDeleteOptions options) throws ArangoDBException;

	/**
	 * Gets all reqistered AQL user functions
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#return-registered-aql-user-functions">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return all reqistered AQL user functions
	 * @throws ArangoDBException
	 */
	Collection<AqlFunctionEntity> getAqlFunctions(AqlFunctionGetOptions options) throws ArangoDBException;

	/**
	 * Returns a {@code ArangoGraph} instance for the given graph name.
	 * 
	 * @param name
	 *            Name of the graph
	 * @return graph handler
	 */
	ArangoGraph graph(String name);

	/**
	 * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
	 * of its edges.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param name
	 *            Name of the graph
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions) throws ArangoDBException;

	/**
	 * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
	 * of its edges.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param name
	 *            Name of the graph
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @param options
	 *            Additional options, can be null
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	GraphEntity createGraph(String name, Collection<EdgeDefinition> edgeDefinitions, GraphCreateOptions options)
			throws ArangoDBException;

	/**
	 * Lists all graphs known to the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-all-graphs">API
	 *      Documentation</a>
	 * @return graphs stored in this database
	 * @throws ArangoDBException
	 */
	Collection<GraphEntity> getGraphs() throws ArangoDBException;

	/**
	 * Performs a server-side transaction and returns its return value.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Transaction/index.html#execute-transaction">API
	 *      Documentation</a>
	 * @param action
	 *            A String evaluating to a JavaScript function to be executed on the server.
	 * @param type
	 *            The type of the result (POJO class, VPackSlice or String for JSON)
	 * @param options
	 *            Additional options, can be null
	 * @return the result of the transaction if it succeeded
	 * @throws ArangoDBException
	 */
	<T> T transaction(String action, Class<T> type, TransactionOptions options) throws ArangoDBException;

	/**
	 * Retrieves information about the current database
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
	 *      Documentation</a>
	 * @return information about the current database
	 * @throws ArangoDBException
	 */
	DatabaseEntity getInfo() throws ArangoDBException;

	/**
	 * Execute a server-side traversal
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Traversal/index.html#executes-a-traversal">API
	 *      Documentation</a>
	 * @param vertexClass
	 *            The type of the vertex documents (POJO class, VPackSlice or String for JSON)
	 * @param edgeClass
	 *            The type of the edge documents (POJO class, VPackSlice or String for JSON)
	 * @param options
	 *            Additional options
	 * @return Result of the executed traversal
	 * @throws ArangoDBException
	 */
	<V, E> TraversalEntity<V, E> executeTraversal(Class<V> vertexClass, Class<E> edgeClass, TraversalOptions options)
			throws ArangoDBException;

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for JSON)
	 * @return the document identified by the id
	 * @throws ArangoDBException
	 */
	<T> T getDocument(String id, Class<T> type) throws ArangoDBException;

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for JSON)
	 * @param options
	 *            Additional options, can be null
	 * @return the document identified by the id
	 * @throws ArangoDBException
	 */
	<T> T getDocument(String id, Class<T> type, DocumentReadOptions options) throws ArangoDBException;

	/**
	 * Reload the routing table.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AdministrationAndMonitoring/index.html#reloads-the-routing-information">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	void reloadRouting() throws ArangoDBException;

	/**
	 * Returns a new {@link ArangoRoute} instance for the given path (relative to the database) that can be used to
	 * perform arbitrary requests.
	 * 
	 * @param path
	 *            The database-relative URL of the route
	 * @return {@link ArangoRoute}
	 */
	ArangoRoute route(String... path);

	/**
	 * Fetches all views from the database and returns an list of view descriptions.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/Getting.html#reads-all-views">API Documentation</a>
	 * @return list of information about all views
	 * @throws ArangoDBException
	 * @since ArangoDB 3.4.0
	 */
	Collection<ViewEntity> getViews() throws ArangoDBException;

	/**
	 * Returns a {@code ArangoView} instance for the given view name.
	 * 
	 * @param name
	 *            Name of the view
	 * @return view handler
	 * @since ArangoDB 3.4.0
	 */
	ArangoView view(String name);

	/**
	 * Returns a {@code ArangoSearch} instance for the given ArangoSearch view name.
	 * 
	 * @param name
	 *            Name of the view
	 * @return ArangoSearch view handler
	 * @since ArangoDB 3.4.0
	 */
	ArangoSearch arangoSearch(String name);

	/**
	 * Creates a view of the given {@code type}, then returns view information from the server.
	 * 
	 * @param name
	 *            The name of the view
	 * @param type
	 *            The type of the view
	 * @return information about the view
	 * @since ArangoDB 3.4.0
	 * @throws ArangoDBException
	 */
	ViewEntity createView(String name, ViewType type) throws ArangoDBException;

	/**
	 * Creates a ArangoSearch view with the given {@code options}, then returns view information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Views/ArangoSearch.html#create-arangosearch-view">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the view
	 * @param options
	 *            Additional options, can be null
	 * @return information about the view
	 * @since ArangoDB 3.4.0
	 * @throws ArangoDBException
	 */
	ViewEntity createArangoSearch(String name, ArangoSearchCreateOptions options) throws ArangoDBException;

}

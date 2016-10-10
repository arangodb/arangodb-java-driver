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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.AqlExecutionExplainEntity;
import com.arangodb.entity.AqlFunctionEntity;
import com.arangodb.entity.AqlParseEntity;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.PathEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.velocystream.Communication;
import com.arangodb.model.AqlFunctionCreateOptions;
import com.arangodb.model.AqlFunctionDeleteOptions;
import com.arangodb.model.AqlFunctionGetOptions;
import com.arangodb.model.AqlQueryExplainOptions;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.AqlQueryParseOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.TraversalOptions;
import com.arangodb.model.UserAccessOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDatabase extends ArangoExecuteable {

	private final String name;

	protected ArangoDatabase(final ArangoDB arangoDB, final String name) {
		super(arangoDB.communication(), arangoDB.vpack(), arangoDB.vpackNull(), arangoDB.vpackParser(),
				arangoDB.documentCache(), arangoDB.collectionCache());
		this.name = name;
	}

	protected ArangoDatabase(final Communication communication, final VPack vpacker, final VPack vpackerNull,
		final VPackParser vpackParser, final DocumentCache documentCache, final CollectionCache collectionCache,
		final String name) {
		super(communication, vpacker, vpackerNull, vpackParser, documentCache, collectionCache);
		this.name = name;
	}

	protected String name() {
		return name;
	}

	public ArangoCollection collection(final String name) {
		return new ArangoCollection(this, name);
	}

	/**
	 * Creates a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity createCollection(final String name) throws ArangoDBException {
		return executeSync(createCollectionRequest(name, new CollectionCreateOptions()), CollectionEntity.class);
	}

	/**
	 * Creates a collection
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
	public CollectionEntity createCollection(final String name, final CollectionCreateOptions options)
			throws ArangoDBException {
		return executeSync(createCollectionRequest(name, options), CollectionEntity.class);
	}

	/**
	 * Creates a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @param options
	 *            Additional options, can be null
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionEntity> createCollectionAsync(final String name) {
		return executeAsync(createCollectionRequest(name, new CollectionCreateOptions()), CollectionEntity.class);
	}

	/**
	 * Creates a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param name
	 *            The name of the collection
	 * @param options
	 *            Additional options, can be null
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionEntity> createCollectionAsync(
		final String name,
		final CollectionCreateOptions options) {
		return executeAsync(createCollectionRequest(name, options), CollectionEntity.class);
	}

	private Request createCollectionRequest(final String name, final CollectionCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION).setBody(
			serialize(OptionsBuilder.build(options != null ? options : new CollectionCreateOptions(), name)));
	}

	/**
	 * Returns all collections
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @return list of information about all collections
	 * @throws ArangoDBException
	 */
	public Collection<CollectionEntity> getCollections() throws ArangoDBException {
		return executeSync(getCollectionsRequest(new CollectionsReadOptions()), getCollectionsResponseDeserializer());
	}

	/**
	 * Returns all collections
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return list of information about all collections
	 * @throws ArangoDBException
	 */
	public Collection<CollectionEntity> getCollections(final CollectionsReadOptions options) throws ArangoDBException {
		return executeSync(getCollectionsRequest(options), getCollectionsResponseDeserializer());
	}

	/**
	 * Returns all collections
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @return list of information about all collections
	 */
	public CompletableFuture<Collection<CollectionEntity>> getCollectionsAsync() {
		return executeAsync(getCollectionsRequest(new CollectionsReadOptions()), getCollectionsResponseDeserializer());
	}

	/**
	 * Returns all collections
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return list of information about all collections
	 */
	public CompletableFuture<Collection<CollectionEntity>> getCollectionsAsync(final CollectionsReadOptions options) {
		return executeAsync(getCollectionsRequest(options), getCollectionsResponseDeserializer());
	}

	private Request getCollectionsRequest(final CollectionsReadOptions options) {
		final Request request;
		request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_COLLECTION);
		final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
		request.putQueryParam(ArangoDBConstants.EXCLUDE_SYSTEM, params.getExcludeSystem());
		return request;
	}

	private ResponseDeserializer<Collection<CollectionEntity>> getCollectionsResponseDeserializer() {
		return (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<CollectionEntity>>() {
			}.getType());
		};
	}

	/**
	 * Returns an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity getIndex(final String id) throws ArangoDBException {
		return executeSync(getIndexRequest(id), IndexEntity.class);
	}

	/**
	 * Returns an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 */
	public CompletableFuture<IndexEntity> getIndexAsync(final String id) {
		return executeAsync(getIndexRequest(id), IndexEntity.class);
	}

	private Request getIndexRequest(final String id) {
		return new Request(name, RequestType.GET, createPath(ArangoDBConstants.PATH_API_INDEX, id));
	}

	/**
	 * Deletes an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return the id of the index
	 * @throws ArangoDBException
	 */
	public String deleteIndex(final String id) throws ArangoDBException {
		return executeSync(deleteIndexRequest(id), deleteIndexResponseDeserializer());
	}

	/**
	 * Deletes an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
	 * @param id
	 *            The index handle
	 * @return the id of the index
	 */
	public CompletableFuture<String> deleteIndexAsync(final String id) {
		return executeAsync(deleteIndexRequest(id), deleteIndexResponseDeserializer());
	}

	private Request deleteIndexRequest(final String id) {
		return new Request(name, RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_INDEX, id));
	}

	private ResponseDeserializer<String> deleteIndexResponseDeserializer() {
		return response -> response.getBody().get().get(ArangoDBConstants.ID).getAsString();
	}

	/**
	 * Drop an existing database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#drop-database">API
	 *      Documentation</a>
	 * @return true if the database was dropped successfully
	 * @throws ArangoDBException
	 */
	public Boolean drop() throws ArangoDBException {
		return executeSync(dropRequest(), createDropResponseDeserializer());
	}

	/**
	 * Drop an existing database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#drop-database">API
	 *      Documentation</a>
	 * @return true if the database was dropped successfully
	 */
	public CompletableFuture<Boolean> dropAsync() {
		return executeAsync(dropRequest(), createDropResponseDeserializer());
	}

	private Request dropRequest() {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_DATABASE, name));
	}

	private ResponseDeserializer<Boolean> createDropResponseDeserializer() {
		return response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean();
	}

	/**
	 * Grants access to the database dbname for user user. You need permission to the _system database in order to
	 * execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	public void grantAccess(final String user) throws ArangoDBException {
		executeSync(grantAccessRequest(user), Void.class);
	}

	/**
	 * Grants access to the database dbname for user user. You need permission to the _system database in order to
	 * execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return void
	 */
	public CompletableFuture<Void> grantAccessAync(final String user) {
		return executeAsync(grantAccessRequest(user), Void.class);
	}

	private Request grantAccessRequest(final String user) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name))
						.setBody(serialize(OptionsBuilder.build(new UserAccessOptions(), ArangoDBConstants.RW)));
	}

	/**
	 * Revokes access to the database dbname for user user. You need permission to the _system database in order to
	 * execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	public void revokeAccess(final String user) throws ArangoDBException {
		executeSync(revokeAccessRequest(user), Void.class);
	}

	/**
	 * Revokes access to the database dbname for user user. You need permission to the _system database in order to
	 * execute this call.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-database-access">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return void
	 */
	public CompletableFuture<Void> revokeAccessAsync(final String user) {
		return executeAsync(revokeAccessRequest(user), Void.class);
	}

	private Request revokeAccessRequest(final String user) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name))
						.setBody(serialize(OptionsBuilder.build(new UserAccessOptions(), ArangoDBConstants.NONE)));
	}

	/**
	 * Create a cursor and return the first results
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            contains the query string to be executed
	 * @param bindVars
	 *            key/value pairs representing the bind parameters
	 * @param options
	 *            Additional options, can be null
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
	 * @return cursor of the results
	 * @throws ArangoDBException
	 */
	public <T> ArangoCursor<T> query(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions options,
		final Class<T> type) throws ArangoDBException {
		return unwrap(queryAsync(query, bindVars, options, type));
	}

	/**
	 * Create a cursor and return the first results
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
	 *      Documentation</a>
	 * @param query
	 *            contains the query string to be executed
	 * @param bindVars
	 *            key/value pairs representing the bind parameters
	 * @param options
	 *            Additional options, can be null
	 * @param type
	 *            The type of the result (POJO class, VPackSlice, String for Json, or Collection/List/Map)
	 * @return cursor of the results
	 */
	public <T> CompletableFuture<ArangoCursor<T>> queryAsync(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions options,
		final Class<T> type) throws ArangoDBException {
		final Request request = new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_CURSOR).setBody(
			serialize(OptionsBuilder.build(options != null ? options : new AqlQueryOptions(), query, bindVars)));
		final CompletableFuture<CursorEntity> execution = executeAsync(request, CursorEntity.class);
		return execution.thenApply(result -> {
			return new ArangoCursor<>(this, type, result);
		});
	}

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
	public AqlExecutionExplainEntity explainQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryExplainOptions options) throws ArangoDBException {
		return executeSync(explainQueryRequest(query, bindVars, options), AqlExecutionExplainEntity.class);
	}

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
	 */
	public CompletableFuture<AqlExecutionExplainEntity> explainQueryAsync(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryExplainOptions options) {
		return executeAsync(explainQueryRequest(query, bindVars, options), AqlExecutionExplainEntity.class);
	}

	private Request explainQueryRequest(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryExplainOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_EXPLAIN).setBody(
			serialize(OptionsBuilder.build(options != null ? options : new AqlQueryExplainOptions(), query, bindVars)));
	}

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
	public AqlParseEntity parseQuery(final String query) throws ArangoDBException {
		return executeSync(parseQueryRequest(query), AqlParseEntity.class);
	}

	/**
	 * Parse an AQL query and return information about it This method is for query validation only. To actually query
	 * the database, see {@link ArangoDatabase#queryAsync(String, Map, AqlQueryOptions, Class)}
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#parse-an-aql-query">API
	 *      Documentation</a>
	 * @param query
	 *            the query which you want parse
	 * @return imformation about the query
	 */
	public CompletableFuture<AqlParseEntity> parseQueryAsync(final String query) {
		return executeAsync(parseQueryRequest(query), AqlParseEntity.class);
	}

	private Request parseQueryRequest(final String query) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_QUERY)
				.setBody(serialize(OptionsBuilder.build(new AqlQueryParseOptions(), query)));
	}

	/**
	 * Clears the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#clears-any-results-in-the-aql-query-cache">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	public void clearQueryCache() throws ArangoDBException {
		executeSync(clearQueryCacheRequest(), Void.class);
	}

	/**
	 * Clears the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#clears-any-results-in-the-aql-query-cache">API
	 *      Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> clearQueryCacheAsync() {
		return executeAsync(clearQueryCacheRequest(), Void.class);
	}

	private Request clearQueryCacheRequest() {
		return new Request(name, RequestType.DELETE, ArangoDBConstants.PATH_API_QUERY_CACHE);
	}

	/**
	 * Returns the global configuration for the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#returns-the-global-properties-for-the-aql-query-cache">API
	 *      Documentation</a>
	 * @return configuration for the AQL query cache
	 * @throws ArangoDBException
	 */
	public QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoDBException {
		return executeSync(getQueryCachePropertiesRequest(), QueryCachePropertiesEntity.class);
	}

	/**
	 * Returns the global configuration for the AQL query cache
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQueryCache/index.html#returns-the-global-properties-for-the-aql-query-cache">API
	 *      Documentation</a>
	 * @return configuration for the AQL query cache
	 */
	public CompletableFuture<QueryCachePropertiesEntity> getQueryCachePropertiesAsync() {
		return executeAsync(getQueryCachePropertiesRequest(), QueryCachePropertiesEntity.class);
	}

	private Request getQueryCachePropertiesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_CACHE_PROPERTIES);
	}

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
	public QueryCachePropertiesEntity setQueryCacheProperties(final QueryCachePropertiesEntity properties)
			throws ArangoDBException {
		return executeSync(setQueryCachePropertiesRequest(properties), QueryCachePropertiesEntity.class);
	}

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
	 */
	public CompletableFuture<QueryCachePropertiesEntity> setQueryCachePropertiesAsync(
		final QueryCachePropertiesEntity properties) {
		return executeAsync(setQueryCachePropertiesRequest(properties), QueryCachePropertiesEntity.class);
	}

	private Request setQueryCachePropertiesRequest(final QueryCachePropertiesEntity properties) {
		return new Request(name, RequestType.PUT, ArangoDBConstants.PATH_API_QUERY_CACHE_PROPERTIES)
				.setBody(serialize(properties));
	}

	/**
	 * Returns the configuration for the AQL query tracking
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-properties-for-the-aql-query-tracking">API
	 *      Documentation</a>
	 * @return configuration for the AQL query tracking
	 * @throws ArangoDBException
	 */
	public QueryTrackingPropertiesEntity getQueryTrackingProperties() throws ArangoDBException {
		return executeSync(getQueryTrackingPropertiesRequest(), QueryTrackingPropertiesEntity.class);
	}

	/**
	 * Returns the configuration for the AQL query tracking
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-properties-for-the-aql-query-tracking">API
	 *      Documentation</a>
	 * @return configuration for the AQL query tracking
	 */
	public CompletableFuture<QueryTrackingPropertiesEntity> getQueryTrackingPropertiesAsync() {
		return executeAsync(getQueryTrackingPropertiesRequest(), QueryTrackingPropertiesEntity.class);
	}

	private Request getQueryTrackingPropertiesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_PROPERTIES);
	}

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
	public QueryTrackingPropertiesEntity setQueryTrackingProperties(final QueryTrackingPropertiesEntity properties)
			throws ArangoDBException {
		return executeSync(setQueryTrackingPropertiesRequest(properties), QueryTrackingPropertiesEntity.class);
	}

	/**
	 * Changes the configuration for the AQL query tracking
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#changes-the-properties-for-the-aql-query-tracking">API
	 *      Documentation</a>
	 * @param properties
	 *            properties to be set
	 * @return current set of properties
	 */
	public CompletableFuture<QueryTrackingPropertiesEntity> setQueryTrackingPropertiesAsync(
		final QueryTrackingPropertiesEntity properties) {
		return executeAsync(setQueryTrackingPropertiesRequest(properties), QueryTrackingPropertiesEntity.class);
	}

	private Request setQueryTrackingPropertiesRequest(final QueryTrackingPropertiesEntity properties) {
		return new Request(name, RequestType.PUT, ArangoDBConstants.PATH_API_QUERY_PROPERTIES)
				.setBody(serialize(properties));
	}

	/**
	 * Returns a list of currently running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-currently-running-aql-queries">API
	 *      Documentation</a>
	 * @return a list of currently running AQL queries
	 * @throws ArangoDBException
	 */
	public Collection<QueryEntity> getCurrentlyRunningQueries() throws ArangoDBException {
		return executeSync(getCurrentlyRunningQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	/**
	 * Returns a list of currently running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-currently-running-aql-queries">API
	 *      Documentation</a>
	 * @return a list of currently running AQL queries
	 */
	public CompletableFuture<Collection<QueryEntity>> getCurrentlyRunningQueriesAsync() {
		return executeAsync(getCurrentlyRunningQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	private Request getCurrentlyRunningQueriesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_CURRENT);
	}

	/**
	 * Returns a list of slow running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @return a list of slow running AQL queries
	 * @throws ArangoDBException
	 */
	public Collection<QueryEntity> getSlowQueries() throws ArangoDBException {
		return executeSync(getSlowQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	/**
	 * Returns a list of slow running AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#returns-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @return a list of slow running AQL queries
	 */
	public CompletableFuture<Collection<QueryEntity>> getSlowQueriesAsync() {
		return executeAsync(getSlowQueriesRequest(), new Type<Collection<QueryEntity>>() {
		}.getType());
	}

	private Request getSlowQueriesRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_QUERY_SLOW);
	}

	/**
	 * Clears the list of slow AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#clears-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	public void clearSlowQueries() throws ArangoDBException {
		executeSync(clearSlowQueriesRequest(), Void.class);
	}

	/**
	 * Clears the list of slow AQL queries
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#clears-the-list-of-slow-aql-queries">API
	 *      Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> clearSlowQueriesAsync() {
		return executeAsync(clearSlowQueriesRequest(), Void.class);
	}

	private Request clearSlowQueriesRequest() {
		return new Request(name, RequestType.DELETE, ArangoDBConstants.PATH_API_QUERY_SLOW);
	}

	/**
	 * Kills a running query. The query will be terminated at the next cancelation point.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#kills-a-running-aql-query">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the query
	 * @throws ArangoDBException
	 */
	public void killQuery(final String id) throws ArangoDBException {
		executeSync(killQueryRequest(id), Void.class);
	}

	/**
	 * Kills a running query. The query will be terminated at the next cancelation point.
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/AqlQuery/index.html#kills-a-running-aql-query">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the query
	 * @return void
	 */
	public CompletableFuture<Void> killQueryAsync(final String id) {
		return executeAsync(killQueryRequest(id), Void.class);
	}

	private Request killQueryRequest(final String id) {
		return new Request(name, RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_QUERY, id));
	}

	/**
	 * Create a new AQL user function
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#create-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            the fully qualified name of the user functions
	 * @param code
	 *            a string representation of the function body
	 * @param options
	 *            Additional options, can be null
	 * @throws ArangoDBException
	 */
	public void createAqlFunction(final String name, final String code, final AqlFunctionCreateOptions options)
			throws ArangoDBException {
		executeSync(createAqlFunctionRequest(name, code, options), Void.class);
	}

	/**
	 * Create a new AQL user function
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#create-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            the fully qualified name of the user functions
	 * @param code
	 *            a string representation of the function body
	 * @param options
	 *            Additional options, can be null
	 * @return void
	 */
	public CompletableFuture<Void> createAqlFunctionAsync(
		final String name,
		final String code,
		final AqlFunctionCreateOptions options) {
		return executeAsync(createAqlFunctionRequest(name, code, options), Void.class);

	}

	private Request createAqlFunctionRequest(
		final String name,
		final String code,
		final AqlFunctionCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_AQLFUNCTION).setBody(
			serialize(OptionsBuilder.build(options != null ? options : new AqlFunctionCreateOptions(), name, code)));
	}

	/**
	 * Remove an existing AQL user function
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#remove-existing-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            the name of the AQL user function
	 * @param options
	 *            Additional options, can be null
	 * @throws ArangoDBException
	 */
	public void deleteAqlFunction(final String name, final AqlFunctionDeleteOptions options) throws ArangoDBException {
		executeSync(deleteAqlFunctionRequest(name, options), Void.class);
	}

	/**
	 * Remove an existing AQL user function
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#remove-existing-aql-user-function">API
	 *      Documentation</a>
	 * @param name
	 *            the name of the AQL user function
	 * @param options
	 *            Additional options, can be null
	 * @return void
	 */
	public CompletableFuture<Void> deleteAqlFunctionAsync(final String name, final AqlFunctionDeleteOptions options) {
		return executeAsync(deleteAqlFunctionRequest(name, options), Void.class);
	}

	private Request deleteAqlFunctionRequest(final String name, final AqlFunctionDeleteOptions options) {
		final Request request = new Request(name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_AQLFUNCTION, name));
		final AqlFunctionDeleteOptions params = options != null ? options : new AqlFunctionDeleteOptions();
		request.putQueryParam(ArangoDBConstants.GROUP, params.getGroup());
		return request;
	}

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
	public Collection<AqlFunctionEntity> getAqlFunctions(final AqlFunctionGetOptions options) throws ArangoDBException {
		return executeSync(getAqlFunctionsRequest(options), new Type<Collection<AqlFunctionEntity>>() {
		}.getType());
	}

	/**
	 * Gets all reqistered AQL user functions
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AqlUserFunctions/index.html#return-registered-aql-user-functions">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return all reqistered AQL user functions
	 */
	public CompletableFuture<Collection<AqlFunctionEntity>> getAqlFunctionsAsync(final AqlFunctionGetOptions options) {
		return executeAsync(getAqlFunctionsRequest(options), new Type<Collection<AqlFunctionEntity>>() {
		}.getType());
	}

	private Request getAqlFunctionsRequest(final AqlFunctionGetOptions options) {
		final Request request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_AQLFUNCTION);
		final AqlFunctionGetOptions params = options != null ? options : new AqlFunctionGetOptions();
		request.putQueryParam(ArangoDBConstants.NAMESPACE, params.getNamespace());
		return request;
	}

	public ArangoGraph graph(final String name) {
		return new ArangoGraph(this, name);
	}

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
	public GraphEntity createGraph(final String name, final Collection<EdgeDefinition> edgeDefinitions)
			throws ArangoDBException {
		return executeSync(createGraphRequest(name, edgeDefinitions, new GraphCreateOptions()),
			createGraphResponseDeserializer());
	}

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
	public GraphEntity createGraph(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions,
		final GraphCreateOptions options) throws ArangoDBException {
		return executeSync(createGraphRequest(name, edgeDefinitions, options), createGraphResponseDeserializer());
	}

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
	 */
	public CompletableFuture<GraphEntity> createGraphAsync(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions) {
		return executeAsync(createGraphRequest(name, edgeDefinitions, new GraphCreateOptions()),
			createGraphResponseDeserializer());
	}

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
	 */
	public CompletableFuture<GraphEntity> createGraphAsync(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions,
		final GraphCreateOptions options) {
		return executeAsync(createGraphRequest(name, edgeDefinitions, options), createGraphResponseDeserializer());
	}

	private Request createGraphRequest(
		final String name,
		final Collection<EdgeDefinition> edgeDefinitions,
		final GraphCreateOptions options) {
		return new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_GHARIAL).setBody(serialize(
			OptionsBuilder.build(options != null ? options : new GraphCreateOptions(), name, edgeDefinitions)));
	}

	private ResponseDeserializer<GraphEntity> createGraphResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPH), GraphEntity.class);
	}

	/**
	 * Lists all graphs known to the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-all-graphs">API
	 *      Documentation</a>
	 * @return graphs stored in this database
	 * @throws ArangoDBException
	 */
	public Collection<GraphEntity> getGraphs() throws ArangoDBException {
		return executeSync(getGraphsRequest(), getGraphsResponseDeserializer());
	}

	/**
	 * Lists all graphs known to the graph module
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#list-all-graphs">API
	 *      Documentation</a>
	 * @return graphs stored in this database
	 */
	public CompletableFuture<Collection<GraphEntity>> getGraphsAsync() {
		return executeAsync(getGraphsRequest(), getGraphsResponseDeserializer());
	}

	private Request getGraphsRequest() {
		return new Request(name, RequestType.GET, ArangoDBConstants.PATH_API_GHARIAL);
	}

	private ResponseDeserializer<Collection<GraphEntity>> getGraphsResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.GRAPHS),
			new Type<Collection<GraphEntity>>() {
			}.getType());
	}

	/**
	 * Execute a server-side transaction
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Transaction/index.html#execute-transaction">API
	 *      Documentation</a>
	 * @param action
	 *            the actual transaction operations to be executed, in the form of stringified JavaScript code
	 * @param type
	 *            The type of the result (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the result of the transaction if it succeeded
	 * @throws ArangoDBException
	 */
	public <T> T transaction(final String action, final Class<T> type, final TransactionOptions options)
			throws ArangoDBException {
		return executeSync(transactionRequest(action, options), transactionResponseDeserializer(type));
	}

	/**
	 * Execute a server-side transaction
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Transaction/index.html#execute-transaction">API
	 *      Documentation</a>
	 * @param action
	 *            the actual transaction operations to be executed, in the form of stringified JavaScript code
	 * @param type
	 *            The type of the result (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the result of the transaction if it succeeded
	 */
	public <T> CompletableFuture<T> transactionAsync(
		final String action,
		final Class<T> type,
		final TransactionOptions options) {
		return executeAsync(transactionRequest(action, options), transactionResponseDeserializer(type));
	}

	private Request transactionRequest(final String action, final TransactionOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRANSACTION)
				.setBody(serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
	}

	private <T> ResponseDeserializer<T> transactionResponseDeserializer(final Class<T> type) {
		return response -> {
			final Optional<VPackSlice> body = response.getBody();
			if (body.isPresent()) {
				final VPackSlice result = body.get().get(ArangoDBConstants.RESULT);
				if (!result.isNone()) {
					return deserialize(result, type);
				}
			}
			return null;
		};
	}

	/**
	 * Retrieves information about the current database
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
	 *      Documentation</a>
	 * @return information about the current database
	 * @throws ArangoDBException
	 */
	public DatabaseEntity getInfo() throws ArangoDBException {
		return executeSync(getInfoRequest(), getInfoResponseDeserializer());
	}

	/**
	 * Retrieves information about the current database
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
	 *      Documentation</a>
	 * @return information about the current database
	 */
	public CompletableFuture<DatabaseEntity> getInfoAsync() {
		return executeAsync(getInfoRequest(), getInfoResponseDeserializer());
	}

	private Request getInfoRequest() {
		return new Request(name, RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_DATABASE, ArangoDBConstants.CURRENT));
	}

	private ResponseDeserializer<DatabaseEntity> getInfoResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.RESULT), DatabaseEntity.class);
	}

	/**
	 * Execute a server-side traversal
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Traversal/index.html#executes-a-traversal">API
	 *      Documentation</a>
	 * @param vertexClass
	 *            The type of the vertex documents (POJO class, VPackSlice or String for Json)
	 * @param edgeClass
	 *            The type of the edge documents (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options
	 * @return Result of the executed traversal
	 * @throws ArangoDBException
	 */
	public <V, E> TraversalEntity<V, E> executeTraversal(
		final Class<V> vertexClass,
		final Class<E> edgeClass,
		final TraversalOptions options) throws ArangoDBException {
		final Request request = executeTraversalRequest(options);
		return executeSync(request, executeTraversalResponseDeserializer(vertexClass, edgeClass));
	}

	/**
	 * Execute a server-side traversal
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/Traversal/index.html#executes-a-traversal">API
	 *      Documentation</a>
	 * @param vertexClass
	 *            The type of the vertex documents (POJO class, VPackSlice or String for Json)
	 * @param edgeClass
	 *            The type of the edge documents (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options
	 * @return Result of the executed traversal
	 */
	public <V, E> CompletableFuture<TraversalEntity<V, E>> executeTraversalAsync(
		final Class<V> vertexClass,
		final Class<E> edgeClass,
		final TraversalOptions options) {
		final Request request = executeTraversalRequest(options);
		return executeAsync(request, executeTraversalResponseDeserializer(vertexClass, edgeClass));
	}

	private Request executeTraversalRequest(final TraversalOptions options) {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRAVERSAL)
				.setBody(serialize(options != null ? options : new TransactionOptions()));
	}

	private <E, V> ResponseDeserializer<TraversalEntity<V, E>> executeTraversalResponseDeserializer(
		final Class<V> vertexClass,
		final Class<E> edgeClass) {
		return response -> {
			final TraversalEntity<V, E> result = new TraversalEntity<>();
			final VPackSlice visited = response.getBody().get().get(ArangoDBConstants.RESULT)
					.get(ArangoDBConstants.VISITED);
			result.setVertices(deserializeVertices(vertexClass, visited));

			final Collection<PathEntity<V, E>> paths = new ArrayList<>();
			for (final Iterator<VPackSlice> iterator = visited.get("paths").arrayIterator(); iterator.hasNext();) {
				final PathEntity<V, E> path = new PathEntity<>();
				final VPackSlice next = iterator.next();
				path.setEdges(deserializeEdges(edgeClass, next));
				path.setVertices(deserializeVertices(vertexClass, next));
				paths.add(path);
			}
			result.setPaths(paths);
			return result;
		};
	}

	private <V> Collection<V> deserializeVertices(final Class<V> vertexClass, final VPackSlice vpack)
			throws VPackException {
		final Collection<V> vertices = new ArrayList<>();
		for (final Iterator<VPackSlice> iterator = vpack.get(ArangoDBConstants.VERTICES).arrayIterator(); iterator
				.hasNext();) {
			vertices.add(deserialize(iterator.next(), vertexClass));
		}
		return vertices;
	}

	private <E> Collection<E> deserializeEdges(final Class<E> edgeClass, final VPackSlice next) throws VPackException {
		final Collection<E> edges = new ArrayList<>();
		for (final Iterator<VPackSlice> iteratorEdge = next.get(ArangoDBConstants.EDGES).arrayIterator(); iteratorEdge
				.hasNext();) {
			edges.add(deserialize(iteratorEdge.next(), edgeClass));
		}
		return edges;
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @return the document identified by the id
	 * @throws ArangoDBException
	 */
	public <T> Optional<T> getDocument(final String id, final Class<T> type) throws ArangoDBException {
		validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocument(split[1], type);
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the document identified by the id
	 * @throws ArangoDBException
	 */
	public <T> Optional<T> getDocument(final String id, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocument(split[1], type, options);
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @return the document identified by the id
	 */
	public <T> CompletableFuture<Optional<T>> getDocumentAsync(final String id, final Class<T> type)
			throws ArangoDBException {
		validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocumentAsync(split[1], type);
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param id
	 *            The id of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the document identified by the id
	 */
	public <T> CompletableFuture<Optional<T>> getDocumentAsync(
		final String id,
		final Class<T> type,
		final DocumentReadOptions options) throws ArangoDBException {
		validateDocumentId(id);
		final String[] split = id.split("/");
		return collection(split[0]).getDocumentAsync(split[1], type, options);
	}

	/**
	 * Reload the routing table.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AdministrationAndMonitoring/index.html#reloads-the-routing-information">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	public void reloadRouting() throws ArangoDBException {
		executeSync(reloadRoutingRequest(), Void.class);
	}

	/**
	 * Reload the routing table.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/AdministrationAndMonitoring/index.html#reloads-the-routing-information">API
	 *      Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> reloadRoutingAsync() {
		return executeAsync(reloadRoutingRequest(), Void.class);
	}

	private Request reloadRoutingRequest() {
		return new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_ADMIN_ROUTING_RELOAD);
	}
}

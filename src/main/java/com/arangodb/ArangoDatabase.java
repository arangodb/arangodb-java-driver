package com.arangodb;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.CursorResult;
import com.arangodb.entity.DatabaseResult;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionsReadOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.TransactionOptions;
import com.arangodb.model.UserAccessOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;

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
		validateCollectionName(name);
		return new ArangoCollection(this, name);
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
	public CollectionResult createCollection(final String name, final CollectionCreateOptions options)
			throws ArangoDBException {
		return unwrap(createCollectionAsync(name, options));
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
	public CompletableFuture<CollectionResult> createCollectionAsync(
		final String name,
		final CollectionCreateOptions options) {
		validateCollectionName(name);
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new CollectionCreateOptions(), name)));
		return executeSync(CollectionResult.class, request);
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
	public Collection<CollectionResult> getCollections(final CollectionsReadOptions options) throws ArangoDBException {
		return unwrap(getCollectionsAsync(options));
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
	public CompletableFuture<Collection<CollectionResult>> getCollectionsAsync(final CollectionsReadOptions options) {
		final Request request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_COLLECTION);
		final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
		request.putParameter(ArangoDBConstants.EXCLUDE_SYSTEM, params.getExcludeSystem());
		return executeSync(request, (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<CollectionResult>>() {
			}.getType());
		});
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
	public IndexResult getIndex(final String id) throws ArangoDBException {
		return unwrap(getIndexAsync(id));
	}

	/**
	 * Returns an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 */
	public CompletableFuture<IndexResult> getIndexAsync(final String id) {
		return executeSync(IndexResult.class,
			new Request(name, RequestType.GET, createPath(ArangoDBConstants.PATH_API_INDEX, id)));
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
		return unwrap(deleteIndexAsync(id));
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
		return executeAsync(new Request(name, RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_INDEX, id)),
			response -> response.getBody().get().get(ArangoDBConstants.ID).getAsString());
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
		return unwrap(dropAsync());
	}

	/**
	 * Drop an existing database
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#drop-database">API
	 *      Documentation</a>
	 * @return true if the database was dropped successfully
	 */
	public CompletableFuture<Boolean> dropAsync() {
		validateDBName(name);
		return executeAsync(
			new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
					createPath(ArangoDBConstants.PATH_API_DATABASE, name)),
			response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
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
	public void grandAccess(final String user) throws ArangoDBException {
		unwrap(grandAccessAync(user));
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
	public CompletableFuture<Void> grandAccessAync(final String user) {
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name));
		request.setBody(serialize(OptionsBuilder.build(new UserAccessOptions(), ArangoDBConstants.RW)));
		return executeSync(Void.class, request);
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
		unwrap(revokeAccessAsync(user));
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
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, name));
		request.setBody(serialize(OptionsBuilder.build(new UserAccessOptions(), ArangoDBConstants.NONE)));
		return executeSync(Void.class, request);
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
		final Request request = new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_CURSOR);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new AqlQueryOptions(), query, bindVars)));
		final CompletableFuture<CursorResult> execution = executeAsync(CursorResult.class, request);
		return execution.thenApply(result -> {
			return new ArangoCursor<>(this, type, result);
		});
	}

	/**
	 * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
	 * of its edges.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param name
	 *            Name of the graph
	 * @param options
	 *            Additional options, can be null
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @return information about the graph
	 * @throws ArangoDBException
	 */
	public GraphResult createGraph(
		final String name,
		final GraphCreateOptions options,
		final EdgeDefinition... edgeDefinitions) throws ArangoDBException {
		return unwrap(createGraphAsync(name, options, edgeDefinitions));
	}

	/**
	 * Create a new graph in the graph module. The creation of a graph requires the name of the graph and a definition
	 * of its edges.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API
	 *      Documentation</a>
	 * @param name
	 *            Name of the graph
	 * @param options
	 *            Additional options, can be null
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @return information about the graph
	 */
	public CompletableFuture<GraphResult> createGraphAsync(
		final String name,
		final GraphCreateOptions options,
		final EdgeDefinition... edgeDefinitions) {
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_GHARIAL);
		request.setBody(serialize(
			OptionsBuilder.build(options != null ? options : new GraphCreateOptions(), name, edgeDefinitions)));
		return executeSync(request,
			response -> deserialize(response.getBody().get().get(ArangoDBConstants.RESULT), GraphResult.class));
	}

	/**
	 * execute a server-side transaction
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
		return unwrap(transactionAsync(action, type, options));
	}

	/**
	 * execute a server-side transaction
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
		final Request request = new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRANSACTION);
		request.setBody(serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
		return executeSync(request, response -> {
			final Optional<VPackSlice> body = response.getBody();
			if (body.isPresent()) {
				final VPackSlice result = body.get().get(ArangoDBConstants.RESULT);
				if (!result.isNone()) {
					return deserialize(result, type);
				}
			}
			return null;
		});
	}

	/**
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
	 *      Documentation</a>
	 * @return information about the current database
	 * @throws ArangoDBException
	 */
	public DatabaseResult getInfo() throws ArangoDBException {
		return unwrap(getInfoAsync());
	}

	/**
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Database/DatabaseManagement.html#information-of-the-database">API
	 *      Documentation</a>
	 * @return information about the current database
	 */
	public CompletableFuture<DatabaseResult> getInfoAsync() {
		return executeSync(
			new Request(name, RequestType.GET,
					createPath(ArangoDBConstants.PATH_API_DATABASE, ArangoDBConstants.CURRENT)),
			response -> deserialize(response.getBody().get().get(ArangoDBConstants.RESULT), DatabaseResult.class));
	}
}

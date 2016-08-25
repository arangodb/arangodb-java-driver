package com.arangodb;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.arangodb.entity.CollectionResult;
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
public class ArangoDatabase extends Executeable {

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

	public CollectionResult createCollection(final String name, final CollectionCreateOptions options)
			throws ArangoDBException {
		return unwrap(createCollectionAsync(name, options));
	}

	public CompletableFuture<CollectionResult> createCollectionAsync(
		final String name,
		final CollectionCreateOptions options) {
		validateCollectionName(name);
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new CollectionCreateOptions(), name)));
		return execute(CollectionResult.class, request);
	}

	public CollectionResult readCollection(final String name) throws ArangoDBException {
		return unwrap(readCollectionAsync(name));
	}

	public CompletableFuture<CollectionResult> readCollectionAsync(final String name) {
		final Request request = new Request(name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name));
		return execute(CollectionResult.class, request);
	}

	public Collection<CollectionResult> readCollections(final CollectionsReadOptions options) throws ArangoDBException {
		return unwrap(readCollectionsAsync(options));
	}

	public CompletableFuture<Collection<CollectionResult>> readCollectionsAsync(final CollectionsReadOptions options) {
		final Request request = new Request(name(), RequestType.GET, ArangoDBConstants.PATH_API_COLLECTION);
		final CollectionsReadOptions params = (options != null ? options : new CollectionsReadOptions());
		request.putParameter(ArangoDBConstants.EXCLUDE_SYSTEM, params.getExcludeSystem());
		return execute(request, (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<CollectionResult>>() {
			}.getType());
		});
	}

	public IndexResult readIndex(final String id) throws ArangoDBException {
		return unwrap(readIndexAsync(id));
	}

	public CompletableFuture<IndexResult> readIndexAsync(final String id) {
		return execute(IndexResult.class,
			new Request(name, RequestType.GET, createPath(ArangoDBConstants.PATH_API_INDEX, id)));
	}

	public String deleteIndex(final String id) throws ArangoDBException {
		return unwrap(deleteIndexAsync(id));
	}

	public CompletableFuture<String> deleteIndexAsync(final String id) {
		return execute(new Request(name, RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_INDEX, id)),
			response -> response.getBody().get().get(ArangoDBConstants.ID).getAsString());
	}

	public Boolean drop() throws ArangoDBException {
		return unwrap(dropAsync());
	}

	public CompletableFuture<Boolean> dropAsync() {
		validateDBName(name);
		return execute(
			new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
					createPath(ArangoDBConstants.PATH_API_DATABASE, name)),
			response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
	}

	public void grandAccess(final String user) throws ArangoDBException {
		unwrap(grandAccessAync(user));
	}

	public CompletableFuture<Void> grandAccessAync(final String user) {
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, "database", name));
		request.setBody(serialize(new UserAccessOptions().grant("rw")));
		return execute(Void.class, request);
	}

	public void revokeAccess(final String user) throws ArangoDBException {
		unwrap(revokeAccessAsync(user));
	}

	public CompletableFuture<Void> revokeAccessAsync(final String user) {
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_USER, user, "database", name));
		request.setBody(serialize(new UserAccessOptions().grant("none")));
		return execute(Void.class, request);
	}

	public <T> Cursor<T> query(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions options,
		final Class<T> type) throws ArangoDBException {
		return new Cursor<>(this,
				OptionsBuilder.build(options != null ? options : new AqlQueryOptions(), query, bindVars), type);
	}

	public GraphResult createGraph(final String name, final GraphCreateOptions options) throws ArangoDBException {
		return unwrap(createGraphAsync(name, options));
	}

	public CompletableFuture<GraphResult> createGraphAsync(final String name, final GraphCreateOptions options) {
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_GHARIAL);
		request.setBody(serialize(OptionsBuilder.build(options != null ? options : new GraphCreateOptions(), name)));
		return execute(request,
			response -> deserialize(response.getBody().get().get(ArangoDBConstants.RESULT), GraphResult.class));
	}

	public <T> T transaction(final String action, final Class<T> type, final TransactionOptions options)
			throws ArangoDBException {
		return unwrap(transactionAsync(action, type, options));
	}

	public <T> CompletableFuture<T> transactionAsync(
		final String action,
		final Class<T> type,
		final TransactionOptions options) {
		final Request request = new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_TRANSACTION);
		request.setBody(serialize(OptionsBuilder.build(options != null ? options : new TransactionOptions(), action)));
		return execute(request, response -> {
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
}

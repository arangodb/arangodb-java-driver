package com.arangodb.model;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBImpl extends ArangoDB {

	public ArangoDBImpl(final Communication.Builder commBuilder, final VPack vpack, final VPack vpackNull,
		final VPackParser vpackParser, final CollectionCache collectionCache) {
		super(commBuilder, vpack, vpackNull, vpackParser, collectionCache);
		final Communication cacheCom = commBuilder.build(vpack, collectionCache);
		collectionCache.init(name -> {
			return new DB(cacheCom, vpackNull, vpack, vpackParser, documentCache, null, name);
		});
	}

	@Override
	public void shutdown() {
		communication.disconnect();
	}

	@Override
	public Boolean createDB(final String name) throws ArangoDBException {
		return unwrap(createDBAsync(name));
	}

	@Override
	public CompletableFuture<Boolean> createDBAsync(final String name) {
		validateDBName(name);
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.POST,
				ArangoDBConstants.PATH_API_DATABASE);
		request.setBody(serialize(new DBCreateOptions().name(name)));
		return execute(request, response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
	}

	@Override
	public DB db() {
		return db(ArangoDBConstants.SYSTEM);
	}

	@Override
	public DB db(final String name) {
		validateDBName(name);
		return new DB(this, name);
	}

	@Override
	public Collection<String> getDBs() throws ArangoDBException {
		return unwrap(getDBsAsync());
	}

	@Override
	public CompletableFuture<Collection<String>> getDBsAsync() {
		return execute(new Request(db().name(), RequestType.GET, ArangoDBConstants.PATH_API_DATABASE), (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<String>>() {
			}.getType());
		});
	}

	@Override
	public ArangoDBVersion getVersion() throws ArangoDBException {
		return unwrap(getVersionAsync());
	}

	@Override
	public CompletableFuture<ArangoDBVersion> getVersionAsync() {
		// TODO details
		return execute(ArangoDBVersion.class,
			new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_VERSION));
	}

	@Override
	public UserResult createUser(final String user, final String passwd, final UserCreateOptions options)
			throws ArangoDBException {
		return unwrap(createUserAsync(user, passwd, options));
	}

	@Override
	public CompletableFuture<UserResult> createUserAsync(
		final String user,
		final String passwd,
		final UserCreateOptions options) {
		final Request request = new Request(db().name(), RequestType.POST, ArangoDBConstants.PATH_API_USER);
		request.setBody(serialize((options != null ? options : new UserCreateOptions()).user(user).passwd(passwd)));
		return execute(UserResult.class, request);
	}

	@Override
	public void deleteUser(final String user) throws ArangoDBException {
		unwrap(deleteUserAsync(user));
	}

	@Override
	public CompletableFuture<Void> deleteUserAsync(final String user) {
		return execute(Void.class,
			new Request(db().name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_USER, user)));
	}

	@Override
	public UserResult getUser(final String user) {
		return unwrap(getUserAsync(user));
	}

	@Override
	public CompletableFuture<UserResult> getUserAsync(final String user) {
		return execute(UserResult.class,
			new Request(db().name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_USER, user)));
	}

	@Override
	public Collection<UserResult> getUsers() {
		return unwrap(getUsersAsync());
	}

	@Override
	public CompletableFuture<Collection<UserResult>> getUsersAsync() {
		return execute(new Request(db().name(), RequestType.GET, ArangoDBConstants.PATH_API_USER), (response) -> {
			final VPackSlice result = response.getBody().get().get(ArangoDBConstants.RESULT);
			return deserialize(result, new Type<Collection<UserResult>>() {
			}.getType());
		});
	}

}

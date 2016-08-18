package com.arangodb.model;

import com.arangodb.ArangoDB;
import com.arangodb.entity.ArangoDBVersion;
import com.arangodb.entity.UserResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;

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
	public Executeable<Boolean> createDB(final String name) {
		validateDBName(name);
		final Request request = new Request(ArangoDBConstants.SYSTEM, RequestType.POST,
				ArangoDBConstants.PATH_API_DATABASE);
		request.setBody(serialize(new DBCreate.Options().build(name)));
		return execute(request, response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
	}

	@Override
	public Executeable<Boolean> deleteDB(final String name) {
		validateDBName(name);
		return execute(
			new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE,
					createPath(ArangoDBConstants.PATH_API_DATABASE, name)),
			response -> response.getBody().get().get(ArangoDBConstants.RESULT).getAsBoolean());
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
	public Executeable<ArangoDBVersion> getVersion() {
		// TODO details
		return execute(ArangoDBVersion.class,
			new Request(ArangoDBConstants.SYSTEM, RequestType.GET, ArangoDBConstants.PATH_API_VERSION));
	}

	@Override
	public Executeable<UserResult> createUser(
		final String user,
		final String passwd,
		final UserCreate.Options options) {
		final Request request = new Request(db().name(), RequestType.POST, ArangoDBConstants.PATH_API_USER);
		request.setBody(serialize((options != null ? options : new UserCreate.Options()).build(user, passwd)));
		return execute(UserResult.class, request);
	}

	@Override
	public Executeable<Void> deleteUser(final String user) {
		return execute(Void.class,
			new Request(db().name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_USER, user)));
	}
}

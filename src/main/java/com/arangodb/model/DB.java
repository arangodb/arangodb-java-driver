package com.arangodb.model;

import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DB extends ExecuteBase {

	private final String name;

	public DB(final Communication communication, final VPack vpacker, final VPack vpackerNull,
		final VPackParser vpackParser, final DocumentCache documentCache, final CollectionCache collectionCache,
		final String name) {
		super(communication, vpacker, vpackerNull, vpackParser, documentCache, collectionCache);
		this.name = name;
	}

	protected String name() {
		return name;
	}

	public DBCollection collection(final String name) {
		validateCollectionName(name);
		return new DBCollection(this, name);
	}

	public Executeable<CollectionResult> createCollection(final String name, final CollectionCreate.Options options) {
		validateCollectionName(name);
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION);
		request.setBody(serialize((options != null ? options : new CollectionCreate.Options()).build(name)));
		return execute(CollectionResult.class, request);
	}

	public Executeable<Void> deleteCollection(final String name) {
		validateCollectionName(name);
		return execute(Void.class,
			new Request(name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_COLLECTION, name)));
	}

	public Executeable<CollectionResult> readCollection(final String name) {
		final Request request = new Request(name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name));
		return execute(CollectionResult.class, request);
	}

	public Executeable<IndexResult> readIndex(final String id) {
		// TODO validate id
		return execute(IndexResult.class,
			new Request(name, RequestType.GET, createPath(ArangoDBConstants.PATH_API_INDEX, id)));
	}

	public Executeable<String> deleteIndex(final String id) {
		// TODO validate id
		return execute(new Request(name, RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_INDEX, id)),
			response -> response.getBody().get().get(ArangoDBConstants.ID).getAsString());
	}

}

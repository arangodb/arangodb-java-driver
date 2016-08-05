package com.arangodb.model;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DB extends ExecuteBase {

	private final String name;

	public DB(final Communication communication, final VPack vpack, final String name) {
		super(communication, vpack);
		this.name = name;
	}

	protected Communication communication() {
		return communication;
	}

	protected VPack vpack() {
		return vpack;
	}

	protected String name() {
		return name;
	}

	public DBCollection collection(final String name) {
		return new DBCollection(this, name);
	}

	public Executeable<CollectionEntity> createCollection(final String name, final CollectionCreateOptions options) {
		return execute(CollectionEntity.class,
			new Request(name, RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION));
	}

	public Executeable<Boolean> deleteCollection(final String name) {
		return execute(Boolean.class, new Request(name, RequestType.DELETE, ArangoDBConstants.PATH_API_COLLECTION));
	}

	public static class CollectionCreateOptions {

	}
}

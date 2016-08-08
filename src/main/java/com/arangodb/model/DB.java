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

	public DB(final Communication communication, final VPack vpacker, final String name) {
		super(communication, vpacker);
		this.name = name;
	}

	protected Communication communication() {
		return communication;
	}

	protected VPack vpack() {
		return vpacker;
	}

	protected String name() {
		return name;
	}

	public DBCollection collection(final String name) {
		return new DBCollection(this, name);
	}

	public Executeable<CollectionEntity> createCollection(final String name, final CollectionCreate.Options options) {
		final Request request = new Request(name(), RequestType.POST, ArangoDBConstants.PATH_API_COLLECTION);
		request.setBody(serialize((options != null ? options : new CollectionCreate.Options()).build(name)));
		return execute(CollectionEntity.class, request);
	}

	public Executeable<Void> deleteCollection(final String name) {
		return execute(Void.class,
			new Request(name(), RequestType.DELETE, ArangoDBConstants.PATH_API_COLLECTION + name));
	}

}

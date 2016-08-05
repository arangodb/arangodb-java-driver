package com.arangodb.model;

import com.arangodb.internal.net.Communication;
import com.arangodb.velocypack.VPack;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DB {

	private final Communication communication;
	private final VPack vpack;
	private final String name;

	public DB(final Communication communication, final VPack vpack, final String name) {
		this.communication = communication;
		this.vpack = vpack;
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

	public CollectionCreate collectionCreate(final String name, final CollectionCreate.Options options) {
		return new CollectionCreate(this, name, options);
	}

	public CollectionDelete collectionDelete(final String name) {
		return new CollectionDelete(this, name);
	}

}

package com.arangodb.model;

import com.arangodb.ArangoDB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DB {

	private final ArangoDB arangoDB;
	private final String name;

	public DB(final ArangoDB arangoDB, final String name) {
		this.arangoDB = arangoDB;
		this.name = name;
	}

	protected ArangoDB arangoDB() {
		return arangoDB;
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

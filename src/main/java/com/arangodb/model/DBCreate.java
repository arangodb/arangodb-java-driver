package com.arangodb.model;

import java.util.concurrent.Future;

import com.arangodb.ArangoDB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCreate implements Executeable<Boolean> {

	private final ArangoDB arangoDB;
	private final String name;

	public DBCreate(final ArangoDB arangoDB, final String name) {
		this.arangoDB = arangoDB;
		this.name = name;
	}

	@Override
	public Future<Boolean> execute(final ExecuteCallback<Boolean> callback) {
		return null;
	}

}

package com.arangodb.model;

import java.util.concurrent.CompletableFuture;

import com.arangodb.ArangoDB;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCreate extends Executeable<Boolean> {

	private final ArangoDB arangoDB;
	private final String name;

	public DBCreate(final ArangoDB arangoDB, final String name) {
		super(arangoDB.db(), Boolean.class);
		this.arangoDB = arangoDB;
		this.name = name;
	}

	@Override
	public CompletableFuture<Boolean> executeAsync() {
		return null;
	}

}

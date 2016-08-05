package com.arangodb.model;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionDelete extends Executeable<Boolean> {

	private final DB db;
	private final String name;

	protected CollectionDelete(final DB db, final String name) {
		super(db, Boolean.class);
		this.db = db;
		this.name = name;
	}

	@Override
	public CompletableFuture<Boolean> executeAsync() {
		return null;
	}

}

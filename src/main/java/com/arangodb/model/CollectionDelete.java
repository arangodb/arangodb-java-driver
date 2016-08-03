package com.arangodb.model;

import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionDelete implements Executeable<Boolean> {

	private final DB db;
	private final String name;

	protected CollectionDelete(final DB db, final String name) {
		this.db = db;
		this.name = name;
	}

	@Override
	public Future<Boolean> execute(final ExecuteCallback<Boolean> callback) {
		return null;
	}

}

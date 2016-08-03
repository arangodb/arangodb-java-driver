package com.arangodb.model;

import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDelete implements Executeable<Boolean> {

	private final DBCollection dbCollection;
	private final String key;
	private final Options options;

	public static class Options {

	}

	protected DocumentDelete(final DBCollection dbCollection, final String key, final Options options) {
		this.dbCollection = dbCollection;
		this.key = key;
		this.options = options;
	}

	@Override
	public Future<Boolean> execute(final ExecuteCallback<Boolean> callback) {
		return null;
	}
}

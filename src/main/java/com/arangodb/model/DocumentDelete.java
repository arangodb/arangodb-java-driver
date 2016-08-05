package com.arangodb.model;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDelete extends Executeable<Boolean> {

	private final DBCollection dbCollection;
	private final String key;
	private final Options options;

	public static class Options {

	}

	protected DocumentDelete(final DBCollection dbCollection, final String key, final Options options) {
		super(dbCollection, Boolean.class);
		this.dbCollection = dbCollection;
		this.key = key;
		this.options = options;
	}

	@Override
	public CompletableFuture<Boolean> executeAsync() {
		return null;
	}

}

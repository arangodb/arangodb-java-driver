package com.arangodb.model;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentCreate<T> extends Executeable<T> {

	private final DBCollection dbCollection;
	private final T value;
	private final Options options;

	public static class Options {

	}

	protected DocumentCreate(final DBCollection dbCollection, final T value, final Options options) {
		super(dbCollection, null);// TODO
		this.dbCollection = dbCollection;
		this.value = value;
		this.options = options;
	}

	@Override
	public CompletableFuture<T> executeAsync() {
		return null;
	}

}

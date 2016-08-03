package com.arangodb.model;

import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentCreate<T> implements Executeable<T> {

	private final DBCollection dbCollection;
	private final T value;
	private final Options options;

	public static class Options {

	}

	protected DocumentCreate(final DBCollection dbCollection, final T value, final Options options) {
		this.dbCollection = dbCollection;
		this.value = value;
		this.options = options;
	}

	@Override
	public Future<T> execute(final ExecuteCallback<T> callback) {
		return null;
	}

}

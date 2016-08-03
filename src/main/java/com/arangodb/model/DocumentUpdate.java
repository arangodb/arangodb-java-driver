package com.arangodb.model;

import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentUpdate<T> implements Executeable<T> {

	private final DBCollection dbCollection;
	private final String key;
	private final T value;
	private final Options options;

	public static class Options {

	}

	protected DocumentUpdate(final DBCollection dbCollection, final String key, final T value, final Options options) {
		this.dbCollection = dbCollection;
		this.key = key;
		this.value = value;
		this.options = options;
	}

	@Override
	public Future<T> execute(final ExecuteCallback<T> callback) {
		return null;
	}
}

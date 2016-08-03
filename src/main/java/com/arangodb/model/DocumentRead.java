package com.arangodb.model;

import java.util.concurrent.Future;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentRead<T> implements Executeable<T> {

	private final DBCollection dbCollection;
	private final String key;
	private final Class<T> type;
	private final Options options;

	public static class Options {

	}

	public DocumentRead(final DBCollection dbCollection, final String key, final Class<T> type, final Options options) {
		this.dbCollection = dbCollection;
		this.key = key;
		this.type = type;
		this.options = options;
	}

	@Override
	public Future<T> execute(final ExecuteCallback<T> callback) {
		return null;
	}
}

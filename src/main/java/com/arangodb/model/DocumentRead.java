package com.arangodb.model;

import java.util.concurrent.CompletableFuture;

import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.arangodb.model.Executeable#executeAsync()
	 */
	@Override
	public CompletableFuture<T> executeAsync() {

		final Request request = new Request();

		final Communication communication = null;
		// communication.send(message, future);
		return null;
	}

}

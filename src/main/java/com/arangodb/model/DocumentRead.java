package com.arangodb.model;

import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class DocumentRead<T> extends Executeable<T> {

	private final DBCollection dbCollection;
	private final String key;
	private final Options options;

	public static class Options {

	}

	public DocumentRead(final DBCollection dbCollection, final String key, final Class<T> type, final Options options) {
		super(dbCollection, type);
		this.dbCollection = dbCollection;
		this.key = key;
		this.options = options;
	}

	@Override
	protected Request createRequest() {
		return new Request(dbCollection.db().name(), RequestType.POST, "/api/document...");
	}

}

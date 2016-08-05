package com.arangodb.model;

import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollection extends ExecuteBase {

	private final DB db;
	private final String name;

	protected DBCollection(final DB db, final String name) {
		super(db);
		this.db = db;
		this.name = name;
	}

	protected DB db() {
		return db;
	}

	protected String name() {
		return name;
	}

	private String createDocumentHandle(final String key) {
		return String.format("%s/%s", name, key);
	}

	public <T> Executeable<T> createDocument(final T value, final DocumentCreateOptions options) {
		return execute((Class<T>) value.getClass(),
			new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_DOCUMENT + name));
	}

	public <T> Executeable<T> readDocument(final String key, final Class<T> type, final DocumentReadOptions options) {
		return execute(type,
			new Request(db.name(), RequestType.GET, ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key)));
	}

	public <T> Executeable<T> updateDocument(final String key, final T value, final DocumentUpdateOptions options) {
		return execute((Class<T>) value.getClass(),
			new Request(db.name(), RequestType.PATCH, ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key)));
	}

	public Executeable<Boolean> deleteDocument(final String key, final DocumentDeleteOptions options) {
		return execute(Boolean.class, new Request(db.name(), RequestType.DELETE,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key)));
	}

	public static class DocumentCreateOptions {
	}

	public static class DocumentReadOptions {
	}

	public static class DocumentUpdateOptions {
	}

	public static class DocumentDeleteOptions {
	}

}

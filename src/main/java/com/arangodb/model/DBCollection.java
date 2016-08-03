package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollection {

	private final DB db;
	private final String name;

	protected DBCollection(final DB db, final String name) {
		this.db = db;
		this.name = name;
	}

	protected DB db() {
		return db;
	}

	protected String name() {
		return name;
	}

	public <T> DocumentCreate<T> documentCreate(final T value, final DocumentCreate.Options options) {
		return new DocumentCreate<>(this, value, options);
	}

	public <T> DocumentRead<T> documentRead(final String key, final Class<T> type, final DocumentRead.Options options) {
		return new DocumentRead<>(this, key, type, options);
	}

	public <T> DocumentUpdate<T> documentUpdate(final String key, final T value, final DocumentUpdate.Options options) {
		return new DocumentUpdate<>(this, key, value, options);
	}

	public DocumentDelete documentDelete(final String key, final DocumentDelete.Options options) {
		return new DocumentDelete(this, key, options);
	}

	public DocumentReadAll documentReadAll() {
		return new DocumentReadAll(this);
	}

}

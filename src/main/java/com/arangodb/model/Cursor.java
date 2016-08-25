package com.arangodb.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import com.arangodb.entity.CursorResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Cursor<T> implements Iterable<T> {

	private final DB db;
	private final Class<T> type;
	private final CursorIterator<T> iterator;
	private final String id;
	private final Optional<Integer> count;
	private final Optional<Map<String, Object>> extra;
	private final boolean cached;

	public Cursor(final DB db, final AqlQueryOptions options, final Class<T> type) {
		super();
		this.db = db;
		this.type = type;
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_CURSOR);
		request.setBody(db.serialize(options));
		final CursorResult result = db.unwrap(db.execute(CursorResult.class, request));
		count = Optional.ofNullable(result.getCount());
		extra = Optional.ofNullable(result.getExtra());
		cached = result.getCached().booleanValue();
		iterator = new CursorIterator<>(this, result);
		id = result.getId();
	}

	public DB getDb() {
		return db;
	}

	public Class<T> getType() {
		return type;
	}

	public Optional<Integer> getCount() {
		return count;
	}

	public Optional<Map<String, Object>> getExtra() {
		return extra;
	}

	public boolean isCached() {
		return cached;
	}

	public CursorResult execute() {
		return db.unwrap(db.execute(CursorResult.class,
			new Request(db.name(), RequestType.PUT, db.createPath(ArangoDBConstants.PATH_API_CURSOR, id))));
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}

}

package com.arangodb;

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
public class ArangoCursor<T> implements Iterable<T> {

	private final ArangoDatabase db;
	private final Class<T> type;
	private final ArangoCursorIterator<T> iterator;
	private final String id;
	private final Optional<Integer> count;
	private final Optional<Map<String, Object>> extra;
	private final boolean cached;

	public ArangoCursor(final ArangoDatabase db, final Class<T> type, final CursorResult result) {
		super();
		this.db = db;
		this.type = type;
		count = Optional.ofNullable(result.getCount());
		extra = Optional.ofNullable(result.getExtra());
		cached = result.getCached().booleanValue();
		iterator = new ArangoCursorIterator<>(this, result);
		id = result.getId();
	}

	protected ArangoDatabase getDb() {
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

	protected CursorResult executeNext() {
		return db.unwrap(db.execute(CursorResult.class,
			new Request(db.name(), RequestType.PUT, db.createPath(ArangoDBConstants.PATH_API_CURSOR, id))));
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}

}

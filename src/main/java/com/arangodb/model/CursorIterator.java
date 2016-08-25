package com.arangodb.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.arangodb.entity.CursorResult;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class CursorIterator<T> implements Iterator<T> {

	private CursorResult result;
	private int pos;

	private final Cursor<T> cursor;

	public CursorIterator(final Cursor<T> cursor, final CursorResult result) {
		super();
		this.cursor = cursor;
		this.result = result;
		pos = 0;
	}

	@Override
	public boolean hasNext() {
		return pos < result.getResult().size() || result.getHasMore();
	}

	@Override
	public T next() {
		if (pos >= result.getResult().size() && result.getHasMore()) {
			result = cursor.execute();
			pos = 0;
		}
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		return cursor.getDb().deserialize(result.getResult().get(pos++), cursor.getType());
	}

}

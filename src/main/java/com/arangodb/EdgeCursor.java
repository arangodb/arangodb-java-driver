package com.arangodb;

import com.arangodb.entity.EdgeEntity;

/**
 * @author a-brandt
 */
public class EdgeCursor<T> extends BaseCursorProxy<T, EdgeEntity<T>> {

	public EdgeCursor(BaseCursor<T, EdgeEntity<T>> baseCursor) {
		super(baseCursor);
	}
}

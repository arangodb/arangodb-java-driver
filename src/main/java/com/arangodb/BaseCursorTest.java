package com.arangodb;

import com.arangodb.entity.DocumentEntity;

/**
 * @author a-brandt
 */
public class BaseCursorTest<T> extends BaseCursorProxy<T, DocumentEntity<T>> {

	public BaseCursorTest(DocumentCursorResult<T, DocumentEntity<T>> baseCursor) {
		super(baseCursor);
	}
}

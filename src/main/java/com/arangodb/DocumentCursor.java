package com.arangodb;

import com.arangodb.entity.DocumentEntity;

/**
 * @author a-brandt
 */
public class DocumentCursor<T> extends BaseCursorProxy<T, DocumentEntity<T>> {

	public DocumentCursor(DocumentCursorResult<T, DocumentEntity<T>> baseCursor) {
		super(baseCursor);
	}
}

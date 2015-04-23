package com.arangodb;

import java.util.Map;

import com.arangodb.entity.BaseCursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * @author a-brandt
 */

public interface InternalCursorDocumentDriver extends BaseDriverInterface {

	BaseCursorEntity<?, ?> validateQuery(String database, String query) throws ArangoException;

	<T, S extends DocumentEntity<T>> BaseCursorEntity<T, S> executeBaseCursorEntityQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<S> classDocumentEntity,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

	<T, S extends DocumentEntity<T>> BaseCursorEntity<T, S> continueBaseCursorEntityQuery(
		String database,
		long cursorId,
		Class<S> classDocumentEntity,
		Class<T> clazz) throws ArangoException;

	DefaultEntity finishQuery(String database, long cursorId) throws ArangoException;

	<T, S extends DocumentEntity<T>> BaseCursor<T, S> executeBaseCursorQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<S> classDocumentEntity,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

}

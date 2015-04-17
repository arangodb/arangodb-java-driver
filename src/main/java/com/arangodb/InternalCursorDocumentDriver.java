package com.arangodb;

import java.util.Map;

import com.arangodb.entity.DocumentCursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * @author a-brandt
 */

public interface InternalCursorDocumentDriver extends BaseDriverInterface {

	DocumentCursorEntity<?> validateQuery(String database, String query) throws ArangoException;

	<T> DocumentCursorEntity<T> executeQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

	<T> DocumentCursorEntity<T> executeQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize) throws ArangoException;

	<T> DocumentCursorEntity<T> continueQuery(String database, long cursorId, Class<?>... clazz) throws ArangoException;

	DefaultEntity finishQuery(String database, long cursorId) throws ArangoException;

	<T> DocumentCursor<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException;

	<T> DocumentCursor<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize) throws ArangoException;
}

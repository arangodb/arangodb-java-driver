package com.arangodb;

import java.util.Map;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalSimpleDriver extends BaseDriverInterface {

	@Deprecated
	<T> CursorEntity<T> executeSimpleAll(String database, String collectionName, int skip, int limit, Class<T> clazz)
			throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeSimpleAllWithResultSet(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	<T> DocumentCursor<T> executeSimpleAllDocuments(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorEntity<T> executeSimpleByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeSimpleByExampleWithResultSet(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResultSet(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	<T> DocumentCursor<T> executeSimpleByExampleDocuments(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	<T> ScalarExampleEntity<T> executeSimpleFirstExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Class<T> clazz) throws ArangoException;

	<T> ScalarExampleEntity<T> executeSimpleAny(String database, String collectionName, Class<T> clazz)
			throws ArangoException;

	@Deprecated
	<T> CursorEntity<T> executeSimpleRange(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeSimpleRangeWithResultSet(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	<T> DocumentCursor<T> executeSimpleRangeWithDocuments(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException;

	SimpleByResultEntity executeSimpleRemoveByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Boolean waitForSync,
		Integer limit) throws ArangoException;

	SimpleByResultEntity executeSimpleReplaceByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Map<String, Object> newValue,
		Boolean waitForSync,
		Integer limit) throws ArangoException;

	SimpleByResultEntity executeSimpleUpdateByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Map<String, Object> newValue,
		Boolean keepNull,
		Boolean waitForSync,
		Integer limit) throws ArangoException;

	@Deprecated
	<T> CursorEntity<T> executeSimpleFulltext(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException;

	@Deprecated
	<T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException;

	<T> DocumentCursor<T> executeSimpleFulltextWithDocuments(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException;

}

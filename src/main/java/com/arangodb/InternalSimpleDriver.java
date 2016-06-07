package com.arangodb;

import java.util.Map;

import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalSimpleDriver extends BaseDriverInterface {

	<T> DocumentCursor<T> executeSimpleAllDocuments(
		String database,
		String collectionName,
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

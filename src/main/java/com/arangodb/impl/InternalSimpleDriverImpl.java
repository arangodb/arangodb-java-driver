/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.impl;

import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.CursorResultSet;
import com.arangodb.DocumentCursor;
import com.arangodb.DocumentCursorResult;
import com.arangodb.InternalCursorDriver;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.DocumentResultEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalSimpleDriverImpl extends BaseArangoDriverWithCursorImpl
		implements com.arangodb.InternalSimpleDriver {

	private static final String WAIT_FOR_SYNC = "waitForSync";
	private static final String LIMIT = "limit";
	private static final String EXAMPLE = "example";
	private static final String COLLECTION = "collection";

	InternalSimpleDriverImpl(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
		super(configure, cursorDriver, httpManager);
	}

	// ----- all --------------------

	@Override
	public <T> CursorEntity<T> executeSimpleAll(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleAll(database, collectionName, skip, limit, clazz);
	}

	@Override
	public <T> CursorResultSet<T> executeSimpleAllWithResultSet(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<T> entity = executeSimpleAll(database, collectionName, skip, limit, clazz);
		return new CursorResultSet<T>(database, cursorDriver, entity, clazz);
	}

	@Override
	public <T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleAll(database, collectionName, skip, limit, DocumentEntity.class, clazz);
	}

	@Override
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<DocumentEntity<T>> entity = executeSimpleAllWithDocument(database, collectionName, skip, limit,
			clazz);
		return new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
	}

	@Override
	public <T> DocumentCursor<T> executeSimpleAllDocuments(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleAll(database, collectionName, skip, limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- example --------------------

	@Override
	public <T> CursorEntity<T> executeSimpleByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleByExample(database, collectionName, example, skip, limit, clazz);
	}

	@Override
	public <T> CursorResultSet<T> executeSimpleByExampleWithResultSet(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<T> entity = executeSimpleByExample(database, collectionName, example, skip, limit, clazz);
		return new CursorResultSet<T>(database, cursorDriver, entity, clazz);
	}

	@Override
	public <T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleByExample(database, collectionName, example, skip, limit, DocumentEntity.class,
			clazz);
	}

	@Override
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResultSet(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<DocumentEntity<T>> entity = executeSimpleByExampleWithDocument(database, collectionName, example,
			skip, limit, clazz);
		return new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
	}

	@Override
	public <T> DocumentCursor<T> executeSimpleByExampleDocuments(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleByExample(database, collectionName, example, skip, limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- first --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> ScalarExampleEntity<T> executeSimpleFirstExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Class<T> clazz) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/first-example"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example).get()));

		return createEntity(res, ScalarExampleEntity.class, clazz);
	}

	// ----- any --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> ScalarExampleEntity<T> executeSimpleAny(String database, String collectionName, Class<T> clazz)
			throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/any"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).get()));

		return createEntity(res, ScalarExampleEntity.class, clazz);
	}

	// ----- range --------------------

	@Override
	public <T> CursorEntity<T> executeSimpleRange(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit, clazz);
	}

	@Override
	public <T> CursorResultSet<T> executeSimpleRangeWithResultSet(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<T> entity = executeSimpleRange(database, collectionName, attribute, left, right, closed, skip,
			limit, clazz);
		return new CursorResultSet<T>(database, cursorDriver, entity, clazz);
	}

	@Override
	public <T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit,
			DocumentEntity.class, clazz);
	}

	@Override
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		CursorEntity<DocumentEntity<T>> entity = executeSimpleRangeWithDocument(database, collectionName, attribute,
			left, right, closed, skip, limit, clazz);
		return new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
	}

	@Override
	public <T> DocumentCursor<T> executeSimpleRangeWithDocuments(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<T> clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- remove-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleRemoveByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Boolean waitForSync,
		Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager
				.doPut(createEndpointUrl(database, "/_api/simple/remove-by-example"), null,
					EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
							.put(WAIT_FOR_SYNC, waitForSync)
							.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get()));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- replace-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleReplaceByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Map<String, Object> newValue,
		Boolean waitForSync,
		Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/replace-by-example"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
					.put("newValue", newValue).put(WAIT_FOR_SYNC, waitForSync)
					.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get()));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- update-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleUpdateByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		Map<String, Object> newValue,
		Boolean keepNull,
		Boolean waitForSync,
		Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/update-by-example"), null,
			EntityFactory.toJsonString(
				new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example).put("newValue", newValue)
						.put("keepNull", keepNull).put(WAIT_FOR_SYNC, waitForSync)
						.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get(),
				keepNull != null && !keepNull));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- Fulltext --------------------

	@Override
	public <T> CursorEntity<T> executeSimpleFulltext(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleFulltext(database, collectionName, attribute, query, skip, limit, index, clazz);
	}

	@Override
	public <T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException {

		CursorEntity<T> entity = executeSimpleFulltext(database, collectionName, attribute, query, skip, limit, index,
			clazz);
		return new CursorResultSet<T>(database, cursorDriver, entity, clazz);
	}

	@Override
	public <T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException {

		return internalExecuteSimpleFulltext(database, collectionName, attribute, query, skip, limit, index,
			DocumentEntity.class, clazz);
	}

	@Override
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException {

		CursorEntity<DocumentEntity<T>> entity = executeSimpleFulltextWithDocument(database, collectionName, attribute,
			query, skip, limit, index, clazz);
		return new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
	}

	@Override
	public <T> DocumentCursor<T> executeSimpleFulltextWithDocuments(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<T> clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleFulltext(database, collectionName, attribute, query, skip, limit, index);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- first --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> DocumentResultEntity<T> executeSimpleFirst(
		String database,
		String collectionName,
		Integer count,
		Class<T> clazz) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/first"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("count", count).get()));

		return createEntity(res, DocumentResultEntity.class, clazz);
	}

	// ----- last --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> DocumentResultEntity<T> executeSimpleLast(
		String database,
		String collectionName,
		Integer count,
		Class<T> clazz) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/last"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("count", count).get()));

		return createEntity(res, DocumentResultEntity.class, clazz);
	}

	// ----- private functions

	private HttpResponseEntity getSimpleAll(String database, String collectionName, int skip, int limit)
			throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/all"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName)
					.put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/by-example"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
					.put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleRange(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/range"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("attribute", attribute)
					.put("left", left).put("right", right).put("closed", closed).put("skip", skip > 0 ? skip : null)
					.put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleFulltext(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/fulltext"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("attribute", attribute)
					.put("query", query).put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null)
					.put("index", index).get()));
	}

	@SuppressWarnings("unchecked")
	private <T> CursorEntity<T> internalExecuteSimpleAll(
		String database,
		String collectionName,
		int skip,
		int limit,
		Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleAll(database, collectionName, skip, limit);

		return createEntity(res, CursorEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> CursorEntity<T> internalExecuteSimpleFulltext(
		String database,
		String collectionName,
		String attribute,
		String query,
		int skip,
		int limit,
		String index,
		Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleFulltext(database, collectionName, attribute, query, skip, limit, index);
		return createEntity(res, CursorEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> CursorEntity<T> internalExecuteSimpleByExample(
		String database,
		String collectionName,
		Map<String, Object> example,
		int skip,
		int limit,
		Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleByExample(database, collectionName, example, skip, limit);

		return createEntity(res, CursorEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> CursorEntity<T> internalExecuteSimpleRange(
		String database,
		String collectionName,
		String attribute,
		Object left,
		Object right,
		Boolean closed,
		int skip,
		int limit,
		Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = getSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit);
		return createEntity(res, CursorEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> DocumentCursor<T> responseToDocumentCursor(String database, Class<T> clazz, HttpResponseEntity res)
			throws ArangoException {

		CursorEntity<DocumentEntity<T>> baseCursorEntity = createEntity(res, CursorEntity.class, DocumentEntity.class,
			clazz);

		DocumentCursorResult<T, DocumentEntity<T>> baseCursor = new DocumentCursorResult<T, DocumentEntity<T>>(database,
				cursorDriver, baseCursorEntity, DocumentEntity.class, clazz);

		return new DocumentCursor<T>(baseCursor);
	}

}

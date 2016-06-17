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
import com.arangodb.DocumentCursor;
import com.arangodb.DocumentCursorResult;
import com.arangodb.InternalCursorDriver;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DocumentEntity;
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

	InternalSimpleDriverImpl(final ArangoConfigure configure, final InternalCursorDriver cursorDriver,
		final HttpManager httpManager) {
		super(configure, cursorDriver, httpManager);
	}

	// ----- all --------------------

	@Override
	public <T> DocumentCursor<T> executeSimpleAllDocuments(
		final String database,
		final String collectionName,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {

		final HttpResponseEntity res = getSimpleAll(database, collectionName, skip, limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- example --------------------

	@Override
	public <T> DocumentCursor<T> executeSimpleByExampleDocuments(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {

		final HttpResponseEntity res = getSimpleByExample(database, collectionName, example, skip, limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- first --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> ScalarExampleEntity<T> executeSimpleFirstExample(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final Class<T> clazz) throws ArangoException {

		validateCollectionName(collectionName);
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/first-example"),
			null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example).get()));

		return createEntity(res, ScalarExampleEntity.class, clazz);
	}

	// ----- any --------------------

	@SuppressWarnings("unchecked")
	@Override
	public <T> ScalarExampleEntity<T> executeSimpleAny(
		final String database,
		final String collectionName,
		final Class<T> clazz) throws ArangoException {

		validateCollectionName(collectionName);
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/any"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).get()));

		return createEntity(res, ScalarExampleEntity.class, clazz);
	}

	// ----- range --------------------

	@Override
	public <T> DocumentCursor<T> executeSimpleRangeWithDocuments(
		final String database,
		final String collectionName,
		final String attribute,
		final Object left,
		final Object right,
		final Boolean closed,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {

		final HttpResponseEntity res = getSimpleRange(database, collectionName, attribute, left, right, closed, skip,
			limit);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- remove-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleRemoveByExample(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		final HttpResponseEntity res = httpManager
				.doPut(createEndpointUrl(database, "/_api/simple/remove-by-example"), null,
					EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
							.put(WAIT_FOR_SYNC, waitForSync)
							.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get()));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- replace-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleReplaceByExample(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final Map<String, Object> newValue,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/replace-by-example"),
			null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
					.put("newValue", newValue).put(WAIT_FOR_SYNC, waitForSync)
					.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get()));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- update-by-example --------------------

	@Override
	public SimpleByResultEntity executeSimpleUpdateByExample(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final Map<String, Object> newValue,
		final Boolean keepNull,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {

		validateCollectionName(collectionName);
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/update-by-example"),
			null,
			EntityFactory.toJsonString(
				new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example).put("newValue", newValue)
						.put("keepNull", keepNull).put(WAIT_FOR_SYNC, waitForSync)
						.put(LIMIT, limit != null && limit.intValue() > 0 ? limit : null).get(),
				keepNull != null && !keepNull));

		return createEntity(res, SimpleByResultEntity.class);
	}

	// ----- Fulltext --------------------

	@Override
	public <T> DocumentCursor<T> executeSimpleFulltextWithDocuments(
		final String database,
		final String collectionName,
		final String attribute,
		final String query,
		final int skip,
		final int limit,
		final String index,
		final Class<T> clazz) throws ArangoException {

		final HttpResponseEntity res = getSimpleFulltext(database, collectionName, attribute, query, skip, limit,
			index);
		return responseToDocumentCursor(database, clazz, res);
	}

	// ----- private functions

	private HttpResponseEntity getSimpleAll(
		final String database,
		final String collectionName,
		final int skip,
		final int limit) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/all"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName)
					.put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleByExample(
		final String database,
		final String collectionName,
		final Map<String, Object> example,
		final int skip,
		final int limit) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/by-example"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put(EXAMPLE, example)
					.put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleRange(
		final String database,
		final String collectionName,
		final String attribute,
		final Object left,
		final Object right,
		final Boolean closed,
		final int skip,
		final int limit) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/range"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("attribute", attribute)
					.put("left", left).put("right", right).put("closed", closed).put("skip", skip > 0 ? skip : null)
					.put(LIMIT, limit > 0 ? limit : null).get()));
	}

	private HttpResponseEntity getSimpleFulltext(
		final String database,
		final String collectionName,
		final String attribute,
		final String query,
		final int skip,
		final int limit,
		final String index) throws ArangoException {
		validateCollectionName(collectionName);
		return httpManager.doPut(createEndpointUrl(database, "/_api/simple/fulltext"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("attribute", attribute)
					.put("query", query).put("skip", skip > 0 ? skip : null).put(LIMIT, limit > 0 ? limit : null)
					.put("index", index).get()));
	}

	@SuppressWarnings("unchecked")
	private <T> DocumentCursor<T> responseToDocumentCursor(
		final String database,
		final Class<T> clazz,
		final HttpResponseEntity res) throws ArangoException {

		final CursorEntity<DocumentEntity<T>> baseCursorEntity = createEntity(res, CursorEntity.class,
			DocumentEntity.class, clazz);

		final DocumentCursorResult<T, DocumentEntity<T>> baseCursor = new DocumentCursorResult<T, DocumentEntity<T>>(
				database, cursorDriver, baseCursorEntity, DocumentEntity.class, clazz);

		return new DocumentCursor<T>(baseCursor);
	}

}

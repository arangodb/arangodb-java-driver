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

package at.orz.arangodb.impl;

import java.util.Map;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.CursorResultSet;
import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.DocumentResultEntity;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.entity.ScalarExampleEntity;
import at.orz.arangodb.entity.SimpleByResultEntity;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalSimpleDriverImpl extends BaseArangoDriverWithCursorImpl {

	InternalSimpleDriverImpl(ArangoConfigure configure,
			InternalCursorDriverImpl cursorDriver) {
		super(configure, cursorDriver);
	}

	// ----- all --------------------

	private <T> CursorEntity<T> _executeSimpleAll(
			String database,
			String collectionName, int skip, int limit,
			Class<?>... clazz) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/all"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("skip", skip > 0 ? skip : null)
						.put("limit", limit > 0 ? limit : null)
						.get())
				);
		
		return createEntity(res, CursorEntity.class, clazz);

	}
	
	public <T> CursorEntity<T> executeSimpleAll(
			String database,
			String collectionName, int skip, int limit,
			Class<?> clazz) throws ArangoException {
		
		CursorEntity<T> entity = _executeSimpleAll(database, collectionName, skip, limit, clazz);
		return entity;
		//return EntityFactory.createResult(entity, clazz);
	}

	public <T> CursorResultSet<T> executeSimpleAllWithResultSet(
			String database,
			String collectionName, int skip, int limit,
			Class<?> clazz) throws ArangoException {
		
		CursorEntity<T> entity = executeSimpleAll(database, collectionName, skip, limit, clazz);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, cursorDriver, entity, clazz);
		return rs;
		
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(
			String database,
			String collectionName, int skip, int limit,
			Class<?> clazz) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = _executeSimpleAll(database, collectionName, skip, limit, DocumentEntity.class, clazz);
		//return EntityFactory.createDocumentResult(entity, clazz);
		return entity;
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(
			String database,
			String collectionName, int skip, int limit,
			Class<?> clazz) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = executeSimpleAllWithDocument(database, collectionName, skip, limit, clazz);
		CursorResultSet<DocumentEntity<T>> rs = new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
		return rs;
		
	}


	// ----- example --------------------

	private <T> CursorEntity<T> _executeSimpleByExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			int skip, int limit,
			Class<?>... clazz
			) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/by-example"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("example", example)
						.put("skip", skip > 0 ? skip : null)
						.put("limit", limit > 0 ? limit : null)
						.get())
				);

		return createEntity(res, CursorEntity.class, clazz);

	}
	
	public <T> CursorEntity<T> executeSimpleByExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = _executeSimpleByExample(database, collectionName, example, skip, limit, clazz);
		return entity;
		
	}

	public <T> CursorResultSet<T> executeSimpleByExampleWithResultSet(
			String database,
			String collectionName, Map<String, Object> example,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = executeSimpleByExample(database, collectionName, example, skip, limit, clazz);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, cursorDriver, entity, clazz);
		return rs;
		
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(
			String database,
			String collectionName,
			Map<String, Object> example,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = _executeSimpleByExample(database, collectionName, example, skip, limit, DocumentEntity.class, clazz);
		return entity;
		//return EntityFactory.createDocumentResult(entity, clazz);
		
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResultSet(
			String database,
			String collectionName, Map<String, Object> example,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = executeSimpleByExampleWithDocument(database, collectionName, example, skip, limit, clazz);
		CursorResultSet<DocumentEntity<T>> rs = new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
		return rs;
		
	}

	// ----- first --------------------

	public <T> ScalarExampleEntity<T> executeSimpleFirstExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			Class<?> clazz
			) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/first-example"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("example", example)
						.get())
				);
		
		ScalarExampleEntity<T> entity = createEntity(res, ScalarExampleEntity.class, clazz);
		return entity;
		//return EntityFactory.createScalarExampleEntity(entity, clazz);
		
	}

	// ----- any --------------------

	public <T> ScalarExampleEntity<T> executeSimpleAny(
			String database,
			String collectionName,
			Class<?> clazz
			) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/any"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.get())
				);
		
		ScalarExampleEntity<T> entity = createEntity(res, ScalarExampleEntity.class, clazz);
		return entity;
		//return EntityFactory.createScalarExampleEntity(entity, clazz);
		
	}

	// ----- range --------------------

	private <T> CursorEntity<T> _executeSimpleRange(
			String database,
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?>... clazz
			) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/range"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("attribute", attribute)
						.put("left", left)
						.put("right", right)
						.put("closed", closed)
						.put("skip", skip > 0 ? skip : null)
						.put("limit", limit > 0 ? limit : null)
						.get())
				);
		
		return createEntity(res, CursorEntity.class, clazz);
		
	}
	
	public <T> CursorEntity<T> executeSimpleRange(
			String database,
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = _executeSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit, clazz);
		//return EntityFactory.createResult(entity, clazz);
		return entity;
		
	}

	public <T> CursorResultSet<T> executeSimpleRangeWithResultSet(
			String database,
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = executeSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit, clazz);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, cursorDriver, entity, clazz);
		return rs;
		
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
			String database,
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = _executeSimpleRange(database, collectionName, attribute, left, right, closed, skip, limit, DocumentEntity.class, clazz);
		return entity;
		
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
			String database,
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = executeSimpleRangeWithDocument(database, collectionName, attribute, left, right, closed, skip, limit, clazz);
		CursorResultSet<DocumentEntity<T>> rs = new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
		return rs;
		
	}
	
	// ----- remove-by-example --------------------

	public SimpleByResultEntity executeSimpleRemoveByExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			Boolean waitForSync,
			Integer limit) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/remove-by-example"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("example", example)
						.put("waitForSync", waitForSync)
						.put("limit", limit != null && limit.intValue() > 0 ? limit : null)
						.get())
				);
		
		SimpleByResultEntity entity = createEntity(res, SimpleByResultEntity.class);
		return entity;
		
	}

	// ----- replace-by-example --------------------

	public SimpleByResultEntity executeSimpleReplaceByExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			Map<String, Object> newValue,
			Boolean waitForSync,
			Integer limit) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/replace-by-example"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("example", example)
						.put("newValue", newValue)
						.put("waitForSync", waitForSync)
						.put("limit", limit != null && limit.intValue() > 0 ? limit : null)
						.get())
				);
		
		SimpleByResultEntity entity = createEntity(res, SimpleByResultEntity.class);
		return entity;
		
	}

	// ----- update-by-example --------------------

	public SimpleByResultEntity executeSimpleUpdateByExample(
			String database,
			String collectionName,
			Map<String, Object> example,
			Map<String, Object> newValue,
			Boolean keepNull,
			Boolean waitForSync,
			Integer limit) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/update-by-example"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("example", example)
						.put("newValue", newValue)
						.put("keepNull", keepNull)
						.put("waitForSync", waitForSync)
						.put("limit", limit != null && limit.intValue() > 0 ? limit : null)
						.get(), 
						keepNull != null && !keepNull
						)
				);

		SimpleByResultEntity entity = createEntity(res, SimpleByResultEntity.class);
		return entity;
		
	}
	
	// ----- Fulltext --------------------

	private <T> CursorEntity<T> _executeSimpleFulltext(
			String database,
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?>... clazz
			) throws ArangoException {

		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/fulltext"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("attribute", attribute)
						.put("query", query)
						.put("skip", skip > 0 ? skip : null)
						.put("limit", limit > 0 ? limit : null)
						.put("index", index)
						.get())
				);
		
		return createEntity(res, CursorEntity.class, clazz);
	}

	public <T> CursorEntity<T> executeSimpleFulltext(
			String database,
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = _executeSimpleFulltext(database, collectionName, attribute, query, skip, limit, index, clazz);
		//return EntityFactory.createResult(entity, clazz);
		return entity;
	}

	public <T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
			String database,
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<T> entity = executeSimpleFulltext(database, collectionName, attribute, query, skip, limit, index, clazz);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, cursorDriver, entity, clazz);
		return rs;
		
	}

	
	public <T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
			String database,
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = _executeSimpleFulltext(database, collectionName, attribute, query, skip, limit, index, DocumentEntity.class, clazz);
		//return EntityFactory.createDocumentResult(entity, clazz);
		return entity;
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
			String database,
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = executeSimpleFulltextWithDocument(database, collectionName, attribute, query, skip, limit, index, clazz);
		CursorResultSet<DocumentEntity<T>> rs = new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
		return rs;
		
	}

	// ----- // Fulltext --------------------


	// ----- first --------------------

	public <T> DocumentResultEntity<T> executeSimpleFirst(
			String database,
			String collectionName,
			Integer count,
			Class<?> clazz) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/first"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("count", count)
						.get())
				);
		
		return createEntity(res, DocumentResultEntity.class, clazz);
		
	}

	// ----- last --------------------

	public <T> DocumentResultEntity<T> executeSimpleLast(
			String database,
			String collectionName,
			Integer count,
			Class<?> clazz) throws ArangoException {
		
		validateCollectionName(collectionName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/simple/last"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("collection", collectionName)
						.put("count", count)
						.get())
				);
		
		return createEntity(res, DocumentResultEntity.class, clazz);
		
	}

}

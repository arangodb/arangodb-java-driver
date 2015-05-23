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

import java.util.Collections;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.CursorResultSet;
import com.arangodb.DocumentCursorResult;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ShortestPathOptions;

/**
 * @author tamtam180 - kirscheless at gmail.com
 */
public class InternalCursorDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalCursorDriver {

	InternalCursorDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public CursorEntity<?> validateQuery(String database, String query) throws ArangoException {
		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, "/_api/query"), null,
			EntityFactory.toJsonString(new MapBuilder("query", query).get()));

		return createEntity(res, CursorEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<T> executeCursorEntityQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = getCursor(database, query, bindVars, aqlQueryOptions);

		return createEntity(res, CursorEntity.class, clazz);
	}

	private HttpResponseEntity getCursor(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions) throws ArangoException {

		Map<String, Object> map = aqlQueryOptions.toMap();
		map.put("query", query);
		map.put("bindVars", bindVars == null ? Collections.emptyMap() : bindVars);

		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, "/_api/cursor"), null,
			EntityFactory.toJsonString(map));
		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<T> continueQuery(String database, long cursorId, Class<?>... clazz) throws ArangoException {

		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/cursor", cursorId), null, null);

		return createEntity(res, CursorEntity.class, clazz);
	}

	@Override
	public DefaultEntity finishQuery(String database, long cursorId) throws ArangoException {
		HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/cursor/", cursorId), null);

		try {
			return createEntity(res, DefaultEntity.class);
		} catch (ArangoException e) {
			// TODO Mode
			if (e.getErrorNumber() == 1600) {
				// 既に削除されている
				return (DefaultEntity) e.getEntity();
			}
			throw e;
		}
	}

	@Override
	public <T, S extends DocumentEntity<T>> DocumentCursorResult<T, S> executeBaseCursorQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<S> classDocumentEntity,
		Class<T> clazz) throws ArangoException {

		CursorEntity<S> entity = executeCursorEntityQuery(database, query, bindVars, aqlQueryOptions,
			classDocumentEntity, clazz);

		return new DocumentCursorResult<T, S>(database, this, entity, classDocumentEntity, clazz);
	}

	@Override
	public <T> CursorResult<T> executeAqlQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		AqlQueryOptions aqlQueryOptions,
		Class<T> clazz) throws ArangoException {

		CursorEntity<T> entity = executeCursorEntityQuery(database, query, bindVars, aqlQueryOptions, clazz);

		return new CursorResult<T>(database, this, entity, clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> ShortestPathEntity<V, E> getShortesPath(
		String database,
		String graphName,
		Object startVertexExample,
		Object endVertexExample,
		ShortestPathOptions shortestPathOptions,
		AqlQueryOptions aqlQueryOptions,
		Class<V> vertexClass,
		Class<E> edgeClass) throws ArangoException {

		validateCollectionName(graphName);

		String query = "for i in graph_shortest_path(@graphName, @startVertexExample, @endVertexExample, @options) return i";

		Map<String, Object> options = shortestPathOptions == null ? new MapBuilder().get() : shortestPathOptions
				.toMap();

		Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName)
				.put("startVertexExample", startVertexExample).put("endVertexExample", endVertexExample)
				.put("options", options).get();

		HttpResponseEntity res = getCursor(database, query, bindVars, aqlQueryOptions);

		return createEntity(res, ShortestPathEntity.class, vertexClass, edgeClass);
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<T> executeQuery(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException {

		AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(calcCount).setBatchSize(batchSize)
				.setFullCount(fullCount);

		HttpResponseEntity res = getCursor(database, query, bindVars, aqlQueryOptions);
		return createEntity(res, CursorEntity.class, clazz);
	}

	@Deprecated
	@Override
	public <T> CursorResultSet<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize,
		Boolean fullCount) throws ArangoException {

		CursorEntity<T> entity = executeQuery(database, query, bindVars, clazz, calcCount, batchSize, fullCount);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, this, entity, clazz);
		return rs;

	}

	@Deprecated
	@Override
	public <T> CursorResultSet<T> executeQueryWithResultSet(
		String database,
		String query,
		Map<String, Object> bindVars,
		Class<T> clazz,
		Boolean calcCount,
		Integer batchSize) throws ArangoException {

		CursorEntity<T> entity = executeQuery(database, query, bindVars, clazz, calcCount, batchSize, false);
		CursorResultSet<T> rs = new CursorResultSet<T>(database, this, entity, clazz);
		return rs;

	}

}

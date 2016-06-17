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
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorRawResult;
import com.arangodb.CursorResult;
import com.arangodb.DocumentCursorResult;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.QueriesResultEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.GraphQueryUtil;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ShortestPathOptions;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 */
public class InternalCursorDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalCursorDriver {

	InternalCursorDriverImpl(final ArangoConfigure configure, final HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public CursorEntity<?> validateQuery(final String database, final String query) throws ArangoException {
		final HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, "/_api/query"), null,
			EntityFactory.toJsonString(new MapBuilder("query", query).get()));

		return createEntity(res, CursorEntity.class);
	}

	@Override
	public String executeAqlQueryJSON(
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		return getJSONResponseText(getCursor(database, query, bindVars, aqlQueryOptions));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<T> executeCursorEntityQuery(
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<?>... clazz) throws ArangoException {

		final HttpResponseEntity res = getCursor(database, query, bindVars, aqlQueryOptions);

		return createEntity(res, CursorEntity.class, clazz);
	}

	private HttpResponseEntity getCursor(
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		final Map<String, Object> map = aqlQueryOptions.toMap();
		map.put("query", query);
		map.put("bindVars", bindVars == null ? Collections.emptyMap() : bindVars);

		return httpManager.doPost(createEndpointUrl(database, "/_api/cursor"), null, EntityFactory.toJsonString(map));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<T> continueQuery(final String database, final long cursorId, final Class<?>... clazz)
			throws ArangoException {

		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/cursor", cursorId), null,
			null);

		return createEntity(res, CursorEntity.class, clazz);
	}

	@Override
	public DefaultEntity finishQuery(final String database, final long cursorId) throws ArangoException {
		final HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/cursor/", cursorId),
			null);

		try {
			return createEntity(res, DefaultEntity.class);
		} catch (final ArangoException e) {
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
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<S> classDocumentEntity,
		final Class<T> clazz) throws ArangoException {

		final CursorEntity<S> entity = executeCursorEntityQuery(database, query, bindVars, aqlQueryOptions,
			classDocumentEntity, clazz);

		return new DocumentCursorResult<T, S>(database, this, entity, classDocumentEntity, clazz);
	}

	@Override
	public <T> CursorResult<T> executeAqlQuery(
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<T> clazz) throws ArangoException {

		final CursorEntity<T> entity = executeCursorEntityQuery(database, query, bindVars, aqlQueryOptions, clazz);

		return new CursorResult<T>(database, this, entity, clazz);
	}

	@Override
	public CursorRawResult executeAqlQueryRaw(
		final String database,
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		final CursorEntity<JsonObject> entity = executeCursorEntityQuery(database, query, bindVars, aqlQueryOptions,
			JsonObject.class);

		return new CursorRawResult(database, this, entity);
	}

	/**
	 * @deprecated use AQL instead
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	@Override
	public <V, E> ShortestPathEntity<V, E> getShortestPath(
		final String database,
		final String graphName,
		final Object startVertexExample,
		final Object endVertexExample,
		final ShortestPathOptions shortestPathOptions,
		final AqlQueryOptions aqlQueryOptions,
		final Class<V> vertexClass,
		final Class<E> edgeClass,
		final ArangoDriver driver) throws ArangoException {

		validateCollectionName(graphName);

		ShortestPathOptions tmpShortestPathOptions = shortestPathOptions;
		if (tmpShortestPathOptions == null) {
			tmpShortestPathOptions = new ShortestPathOptions();
		}

		MapBuilder mapBuilder = new MapBuilder();
		final String query = GraphQueryUtil.createShortestPathQuery(driver, database, graphName, startVertexExample,
			endVertexExample, tmpShortestPathOptions, vertexClass, edgeClass, mapBuilder);

		final Map<String, Object> bindVars = mapBuilder.get();
		final HttpResponseEntity res = getCursor(database, query, bindVars, aqlQueryOptions);

		return createEntity(res, ShortestPathEntity.class, vertexClass, edgeClass);
	}

	@Override
	public QueryTrackingPropertiesEntity getQueryTrackingProperties(final String database) throws ArangoException {

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/query/properties"), null,
			null);

		return createEntity(res, QueryTrackingPropertiesEntity.class);
	}

	@Override
	public QueryTrackingPropertiesEntity setQueryTrackingProperties(
		final String database,
		final QueryTrackingPropertiesEntity properties) throws ArangoException {
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/query/properties"), null,
			EntityFactory.toJsonString(properties));

		return createEntity(res, QueryTrackingPropertiesEntity.class);
	}

	@Override
	public QueriesResultEntity getCurrentlyRunningQueries(final String database) throws ArangoException {
		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/query/current"), null,
			null);

		return createEntity(res, QueriesResultEntity.class);
	}

	@Override
	public QueriesResultEntity getSlowQueries(final String database) throws ArangoException {
		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/query/slow"), null, null);

		return createEntity(res, QueriesResultEntity.class);
	}

	@Override
	public DefaultEntity deleteSlowQueries(final String database) throws ArangoException {
		final HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/query/slow"), null,
			null);

		return createEntity(res, DefaultEntity.class);
	}

	@Override
	public DefaultEntity killQuery(final String database, final String id) throws ArangoException {
		final HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/query", id), null, null);

		return createEntity(res, DefaultEntity.class);
	}

}

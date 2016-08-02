/*
 * Copyright (C) 2012,2013 tamtam180
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

import java.util.ArrayList;
import java.util.List;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.InternalCursorDriver;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphGetCollectionsResultEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.http.BatchHttpManager;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.CollectionUtils;
import com.arangodb.util.EdgeUtils;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.StringUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 * @author a-brandt
 */
public class InternalGraphDriverImpl extends BaseArangoDriverWithCursorImpl
		implements com.arangodb.InternalGraphDriver {

	private static final String WAIT_FOR_SYNC = "waitForSync";
	private static final String IF_NONE_MATCH = "If-None-Match";
	private static final String IF_MATCH = "If-Match";
	private static final String UNKNOWN_ERROR = "unknown error";
	private static final String VERTEX = "/vertex";
	private static final String EDGE = "/edge";
	private static final String EXCLUDE_ORPHAN = "excludeOrphan";

	InternalGraphDriverImpl(final ArangoConfigure configure, final InternalCursorDriver cursorDriver,
		final HttpManager httpManager) {
		super(configure, cursorDriver, httpManager);
	}

	@Override
	public GraphEntity createGraph(final String databaseName, final String graphName, final Boolean waitForSync)
			throws ArangoException {
		final HttpResponseEntity response = httpManager.doPost(createGharialEndpointUrl(databaseName),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(),
			EntityFactory.toJsonString(new MapBuilder().put("name", graphName).get()));
		return createEntity(response, GraphEntity.class);
	}

	@Override
	public GraphEntity createGraph(
		final String databaseName,
		final String graphName,
		final List<EdgeDefinitionEntity> edgeDefinitions,
		final List<String> orphanCollections,
		final Boolean waitForSync) throws ArangoException {

		final HttpResponseEntity response = httpManager.doPost(createGharialEndpointUrl(databaseName),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(),
			EntityFactory.toJsonString(new MapBuilder().put("name", graphName).put("edgeDefinitions", edgeDefinitions)
					.put("orphanCollections", orphanCollections).get()));
		return createEntity(response, GraphEntity.class);
	}

	@Override
	public GraphsEntity getGraphs(final String databaseName) throws ArangoException {

		final GraphsEntity graphsEntity = new GraphsEntity();
		final List<GraphEntity> graphEntities = new ArrayList<GraphEntity>();
		final List<String> graphList = this.getGraphList(databaseName);
		if (CollectionUtils.isNotEmpty(graphList)) {
			for (final String graphName : graphList) {
				graphEntities.add(this.getGraph(databaseName, graphName));
			}
		}
		graphsEntity.setGraphs(graphEntities);
		return graphsEntity;

	}

	@Override
	public List<String> getGraphList(final String databaseName) throws ArangoException {
		final HttpResponseEntity res = httpManager.doGet(createGharialEndpointUrl(databaseName));
		final GraphsEntity graphsEntity = createEntity(res, GraphsEntity.class);
		final List<String> graphList = new ArrayList<String>();
		final List<GraphEntity> graphs = graphsEntity.getGraphs();
		if (CollectionUtils.isNotEmpty(graphs)) {
			for (final GraphEntity graph : graphs) {
				graphList.add(graph.getDocumentKey());
			}
		}
		return graphList;
	}

	@Override
	public GraphEntity getGraph(final String databaseName, final String graphName) throws ArangoException {
		validateCollectionName(graphName); // ??
		final HttpResponseEntity res = httpManager.doGet(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName)), new MapBuilder().get(), null);
		return createEntity(res, GraphEntity.class);

	}

	@Override
	public DeletedEntity deleteGraph(final String databaseName, final String graphName, final Boolean dropCollections)
			throws ArangoException {
		validateCollectionName(graphName); // ??
		final HttpResponseEntity res = httpManager.doDelete(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName)), new MapBuilder().get(),
			new MapBuilder().put("dropCollections", dropCollections).get());

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		DeletedEntity result;
		if (isInBatchMode()) {
			result = new DeletedEntity();
		} else {
			result = createEntity(res, DeletedEntity.class, null, true);
		}

		return result;
	}

	@Override
	public List<String> getVertexCollections(
		final String databaseName,
		final String graphName,
		final boolean excludeOrphan) throws ArangoException {
		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doGet(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX),
			new MapBuilder().put(EXCLUDE_ORPHAN, excludeOrphan).get());

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphGetCollectionsResultEntity result;
		if (isInBatchMode()) {
			result = new GraphGetCollectionsResultEntity();
		} else {
			result = createEntity(res, GraphGetCollectionsResultEntity.class, null, true);
		}
		return result.getCollections();
	}

	/**
	 * Removes a vertex collection from the graph and optionally deletes the
	 * collection, if it is not used in any other graph.
	 *
	 * @param databaseName
	 * @param graphName
	 * @param collectionName
	 * @param dropCollection
	 * @throws ArangoException
	 */
	@Override
	public DeletedEntity deleteVertexCollection(
		final String databaseName,
		final String graphName,
		final String collectionName,
		final Boolean dropCollection) throws ArangoException {
		validateDatabaseName(databaseName, false);
		validateCollectionName(collectionName);
		validateCollectionName(graphName);

		final HttpResponseEntity res = httpManager.doDelete(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName)),
			new MapBuilder().get(), new MapBuilder().put("dropCollection", dropCollection).get());

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		DeletedEntity result;
		if (isInBatchMode()) {
			result = new DeletedEntity();
		} else {
			result = createEntity(res, DeletedEntity.class, null, true);
		}

		return result;
	}

	@Override
	public GraphEntity createVertexCollection(
		final String databaseName,
		final String graphName,
		final String collectionName) throws ArangoException {
		validateCollectionName(graphName);
		validateCollectionName(collectionName);

		final HttpResponseEntity res = httpManager.doPost(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).get()));

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphEntity result;
		if (isInBatchMode()) {
			result = new GraphEntity();
		} else {
			result = createEntity(res, GraphEntity.class, null, true);
		}

		return result;
	}

	@Override
	public List<String> getEdgeCollections(final String databaseName, final String graphName) throws ArangoException {
		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager
				.doGet(createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), EDGE));

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphGetCollectionsResultEntity result;
		if (isInBatchMode()) {
			result = new GraphGetCollectionsResultEntity();
		} else {
			result = createEntity(res, GraphGetCollectionsResultEntity.class, null, true);
		}

		return result.getCollections();
	}

	@Override
	public GraphEntity createEdgeDefinition(
		final String databaseName,
		final String graphName,
		final EdgeDefinitionEntity edgeDefinition) throws ArangoException {

		validateCollectionName(graphName);
		validateCollectionName(edgeDefinition.getCollection());

		final String edgeDefinitionJson = this.convertToString(edgeDefinition);

		final HttpResponseEntity res = httpManager.doPost(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), EDGE), null, edgeDefinitionJson);

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphEntity result;
		if (isInBatchMode()) {
			result = new GraphEntity();
		} else {
			result = createEntity(res, GraphEntity.class, null, true);
		}

		return result;
	}

	@Override
	public GraphEntity replaceEdgeDefinition(
		final String databaseName,
		final String graphName,
		final String edgeName,
		final EdgeDefinitionEntity edgeDefinition) throws ArangoException {

		validateCollectionName(graphName);
		validateCollectionName(edgeDefinition.getCollection());

		final String edgeDefinitionJson = this.convertToString(edgeDefinition);

		final HttpResponseEntity res = httpManager.doPut(createGharialEndpointUrl(databaseName,
			StringUtils.encodeUrl(graphName), EDGE, StringUtils.encodeUrl(edgeName)), null, edgeDefinitionJson);

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphEntity result;
		if (isInBatchMode()) {
			result = new GraphEntity();
		} else {
			result = createEntity(res, GraphEntity.class, null, true);
		}

		return result;

	}

	@Override
	public GraphEntity deleteEdgeDefinition(
		final String databaseName,
		final String graphName,
		final String edgeName,
		final Boolean dropCollection) throws ArangoException {
		validateCollectionName(graphName);
		validateCollectionName(edgeName);

		final HttpResponseEntity res = httpManager.doDelete(createGharialEndpointUrl(databaseName,
			StringUtils.encodeUrl(graphName), EDGE, StringUtils.encodeUrl(edgeName)),
			new MapBuilder().put("dropCollection", dropCollection).get());

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		GraphEntity result;
		if (isInBatchMode()) {
			result = new GraphEntity();
		} else {
			result = createEntity(res, GraphEntity.class, null, true);
		}

		return result;
	}

	@Override
	public <T> VertexEntity<T> createVertex(
		final String database,
		final String graphName,
		final String collectionName,
		final T vertex,
		final Boolean waitForSync) throws ArangoException {
		return createVertex(database, graphName, collectionName, null, vertex, waitForSync);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> createVertex(
		final String database,
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean waitForSync) throws ArangoException {

		JsonObject obj;
		if (vertex == null) {
			obj = new JsonObject();
		} else {
			final JsonElement elem = EntityFactory.toJsonElement(vertex, false);
			if (elem.isJsonObject()) {
				obj = elem.getAsJsonObject();
			} else {
				throw new IllegalArgumentException("vertex need object type(not support array, primitive, etc..).");
			}
		}
		if (key != null) {
			obj.addProperty("_key", key);
		}

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPost(
			createGharialEndpointUrl(database, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName)),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), EntityFactory.toJsonString(obj));

		if (wrongResult(res)) {
			throw new ArangoException(UNKNOWN_ERROR);
		}

		VertexEntity<T> result;
		if (isInBatchMode()) {
			result = new VertexEntity<T>();
			result.setEntity(vertex);
		} else {
			if (vertex != null) {
				result = createEntity(res, VertexEntity.class, vertex.getClass());
			} else {
				result = createEntity(res, VertexEntity.class);
			}
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		}
		return result;
	}

	private boolean wrongResult(final HttpResponseEntity res) {
		if (res.isJsonResponse()) {
			return false;
		}
		if (httpManager instanceof BatchHttpManager && ((BatchHttpManager) httpManager).isBatchModeActive()) {
			// we are in batch mode
			return false;
		}

		return true;
	}

	private boolean isInBatchMode() {
		return httpManager instanceof BatchHttpManager && ((BatchHttpManager) httpManager).isBatchModeActive();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> getVertex(
		final String databaseName,
		final String graphName,
		final String collectionName,
		final String key,
		final Class<T> clazz,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doGet(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_MATCH, ifMatchRevision, true).put(IF_NONE_MATCH, ifNoneMatchRevision, true).get(),
			new MapBuilder().get());

		return createEntity(res, VertexEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> replaceVertex(
		final String databaseName,
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean waitForSync,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPut(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_MATCH, ifMatchRevision, true).put(IF_NONE_MATCH, ifNoneMatchRevision, true).get(),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), EntityFactory.toJsonString(vertex));

		VertexEntity<T> result;
		if (vertex != null) {
			result = createEntity(res, VertexEntity.class, vertex.getClass());
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		} else {
			result = createEntity(res, VertexEntity.class);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> updateVertex(
		final String databaseName,
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean keepNull,
		final Boolean waitForSync,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPatch(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_MATCH, ifMatchRevision, true).put(IF_NONE_MATCH, ifNoneMatchRevision, true).get(),
			new MapBuilder().put("keepNull", keepNull).put(WAIT_FOR_SYNC, waitForSync).get(),
			EntityFactory.toJsonString(vertex, keepNull != null && !keepNull));

		VertexEntity<T> result;
		if (vertex != null) {
			result = createEntity(res, VertexEntity.class, vertex.getClass());
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		} else {
			result = createEntity(res, VertexEntity.class);
		}
		return result;
	}

	@Override
	public DeletedEntity deleteVertex(
		final String databaseName,
		final String graphName,
		final String collectionName,
		final String key,
		final Boolean waitForSync,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doDelete(
			createGharialEndpointUrl(databaseName, StringUtils.encodeUrl(graphName), VERTEX,
				StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_MATCH, ifMatchRevision, true).put(IF_NONE_MATCH, ifNoneMatchRevision, true).get(),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get());

		return createEntity(res, DeletedEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> createEdge(
		final String database,
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync) throws ArangoException {

		final JsonObject obj = EdgeUtils.valueToEdgeJsonObject(key, fromHandle, toHandle, value);

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPost(
			createGharialEndpointUrl(database, StringUtils.encodeUrl(graphName), EDGE,
				StringUtils.encodeUrl(edgeCollectionName)),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), EntityFactory.toJsonString(obj));

		final EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateEdgeAttributes(value, entity.getDocumentRevision(), entity.getDocumentHandle(),
				entity.getDocumentKey(), fromHandle, toHandle);
		}

		entity.setFromVertexHandle(fromHandle);
		entity.setToVertexHandle(toHandle);
		return entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> getEdge(
		final String database,
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Class<T> clazz,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doGet(
			createGharialEndpointUrl(database, StringUtils.encodeUrl(graphName), EDGE,
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_NONE_MATCH, ifNoneMatchRevision, true).put(IF_MATCH, ifMatchRevision, true).get(),
			new MapBuilder().get());

		return createEntity(res, EdgeEntity.class, clazz);
	}

	@Override
	public DeletedEntity deleteEdge(
		final String database,
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Boolean waitForSync,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doDelete(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), EDGE,
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_NONE_MATCH, ifNoneMatchRevision, true).put(IF_MATCH, ifMatchRevision, true).get(),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get());

		return createEntity(res, DeletedEntity.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> replaceEdge(
		final String database,
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		final JsonObject obj = EdgeUtils.valueToEdgeJsonObject(key, fromHandle, toHandle, value);

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPut(
			createGharialEndpointUrl(database, StringUtils.encodeUrl(graphName), EDGE,
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_NONE_MATCH, ifNoneMatchRevision, true).put(IF_MATCH, ifMatchRevision, true).get(),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), EntityFactory.toJsonString(obj));

		final EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateEdgeAttributes(entity.getEntity(), entity.getDocumentRevision(),
				entity.getDocumentHandle(), entity.getDocumentKey(), fromHandle, toHandle);
		}
		if (fromHandle != null) {
			entity.setFromVertexHandle(fromHandle);
		}
		if (toHandle != null) {
			entity.setToVertexHandle(toHandle);
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> updateEdge(
		final String database,
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync,
		final Boolean keepNull,
		final String ifMatchRevision,
		final String ifNoneMatchRevision) throws ArangoException {

		final JsonObject obj = EdgeUtils.valueToEdgeJsonObject(key, fromHandle, toHandle, value);

		validateCollectionName(graphName);
		final HttpResponseEntity res = httpManager.doPatch(
			createGharialEndpointUrl(database, StringUtils.encodeUrl(graphName), EDGE,
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put(IF_NONE_MATCH, ifNoneMatchRevision, true).put(IF_MATCH, ifMatchRevision, true).get(),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).put("keepNull", keepNull).get(),
			EntityFactory.toJsonString(obj));

		final EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateEdgeAttributes(entity.getEntity(), entity.getDocumentRevision(),
				entity.getDocumentHandle(), entity.getDocumentKey(), fromHandle, toHandle);
		}

		if (fromHandle != null) {
			entity.setFromVertexHandle(fromHandle);
		}
		if (toHandle != null) {
			entity.setToVertexHandle(toHandle);
		}

		return entity;
	}

	private String convertToString(final EdgeDefinitionEntity edgeDefinition) {
		final JsonObject rawEdgeDefinition = (JsonObject) EntityFactory
				.toJsonElement(new MapBuilder().put("edgeDefinition", edgeDefinition).get(), false);
		final JsonElement edgeDefinitionJson = rawEdgeDefinition.get("edgeDefinition");
		return edgeDefinitionJson.toString();
	}

}

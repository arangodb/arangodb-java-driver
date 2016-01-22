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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.InternalCursorDriver;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.Direction;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.FilterCondition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphGetCollectionsResultEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.CollectionUtils;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.StringUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 * @author a-brandt
 */
@SuppressWarnings("deprecation")
public class InternalGraphDriverImpl extends BaseArangoDriverWithCursorImpl
		implements com.arangodb.InternalGraphDriver {

	InternalGraphDriverImpl(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
		super(configure, cursorDriver, httpManager);
	}

	@Override
	public GraphEntity createGraph(String databaseName, String graphName, Boolean waitForSync) throws ArangoException {
		HttpResponseEntity response = httpManager.doPost(createEndpointUrl(databaseName, "/_api/gharial"),
			new MapBuilder().put("waitForSync", waitForSync).get(),
			EntityFactory.toJsonString(new MapBuilder().put("name", graphName).get()));
		return createEntity(response, GraphEntity.class);
	}

	@Override
	public GraphEntity createGraph(
		String databaseName,
		String graphName,
		List<EdgeDefinitionEntity> edgeDefinitions,
		List<String> orphanCollections,
		Boolean waitForSync) throws ArangoException {

		HttpResponseEntity response = httpManager.doPost(createEndpointUrl(databaseName, "/_api/gharial"),
			new MapBuilder().put("waitForSync", waitForSync).get(),
			EntityFactory.toJsonString(new MapBuilder().put("name", graphName).put("edgeDefinitions", edgeDefinitions)
					.put("orphanCollections", orphanCollections).get()));
		return createEntity(response, GraphEntity.class);
	}

	@Override
	public GraphsEntity getGraphs(String databaseName) throws ArangoException {

		GraphsEntity graphsEntity = new GraphsEntity();
		List<GraphEntity> graphEntities = new ArrayList<GraphEntity>();
		List<String> graphList = this.getGraphList(databaseName);
		if (CollectionUtils.isNotEmpty(graphList)) {
			for (String graphName : graphList) {
				graphEntities.add(this.getGraph(databaseName, graphName));
			}
		}
		graphsEntity.setGraphs(graphEntities);
		return graphsEntity;

	}

	@Override
	public List<String> getGraphList(String databaseName) throws ArangoException {
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(databaseName, "/_api/gharial"));
		GraphsEntity graphsEntity = createEntity(res, GraphsEntity.class);
		List<String> graphList = new ArrayList<String>();
		List<GraphEntity> graphs = graphsEntity.getGraphs();
		if (CollectionUtils.isNotEmpty(graphs)) {
			for (GraphEntity graph : graphs) {
				graphList.add(graph.getDocumentKey());
			}
		}
		return graphList;
	}

	@Override
	public GraphEntity getGraph(String databaseName, String graphName) throws ArangoException {
		validateCollectionName(graphName); // ??
		HttpResponseEntity res = httpManager.doGet(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName)), new MapBuilder().get(),
			null);
		return createEntity(res, GraphEntity.class);

	}

	@Override
	public DeletedEntity deleteGraph(String databaseName, String graphName, Boolean dropCollections)
			throws ArangoException {
		validateCollectionName(graphName); // ??
		HttpResponseEntity res = httpManager.doDelete(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName)), new MapBuilder().get(),
			new MapBuilder().put("dropCollections", dropCollections).get());

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		DeletedEntity result = createEntity(res, DeletedEntity.class, null, true);

		return result;

	}

	@Override
	public List<String> getVertexCollections(String databaseName, String graphName) throws ArangoException {
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager
				.doGet(createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "/vertex"));

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphGetCollectionsResultEntity result = createEntity(res, GraphGetCollectionsResultEntity.class, null, true);

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
		String databaseName,
		String graphName,
		String collectionName,
		Boolean dropCollection) throws ArangoException {
		validateDatabaseName(databaseName, false);
		validateCollectionName(collectionName);
		validateCollectionName(graphName);

		HttpResponseEntity res = httpManager.doDelete(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "/vertex",
				StringUtils.encodeUrl(collectionName)),
			new MapBuilder().get(), new MapBuilder().put("dropCollection", dropCollection).get());

		DeletedEntity result = createEntity(res, DeletedEntity.class, null, true);

		return result;

	}

	@Override
	public GraphEntity createVertexCollection(String databaseName, String graphName, String collectionName)
			throws ArangoException {
		validateCollectionName(graphName);
		validateCollectionName(collectionName);

		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "/vertex"), null,
			EntityFactory.toJsonString(new MapBuilder().put("collection", collectionName).get()));

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphEntity result = createEntity(res, GraphEntity.class, null, true);

		return result;
	}

	@Override
	public List<String> getEdgeCollections(String databaseName, String graphName) throws ArangoException {
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager
				.doGet(createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "/edge"));

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphGetCollectionsResultEntity result = createEntity(res, GraphGetCollectionsResultEntity.class, null, true);
		return result.getCollections();
	}

	@Override
	public GraphEntity createEdgeDefinition(String databaseName, String graphName, EdgeDefinitionEntity edgeDefinition)
			throws ArangoException {

		validateCollectionName(graphName);
		validateCollectionName(edgeDefinition.getCollection());

		String edgeDefinitionJson = this.convertToString(edgeDefinition);

		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "/edge"), null,
			edgeDefinitionJson);

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphEntity result = createEntity(res, GraphEntity.class, null, true);

		return result;
	}

	@Override
	public GraphEntity replaceEdgeDefinition(
		String databaseName,
		String graphName,
		String edgeName,
		EdgeDefinitionEntity edgeDefinition) throws ArangoException {

		validateCollectionName(graphName);
		validateCollectionName(edgeDefinition.getCollection());

		String edgeDefinitionJson = this.convertToString(edgeDefinition);

		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(databaseName, "/_api/gharial",
			StringUtils.encodeUrl(graphName), "/edge", StringUtils.encodeUrl(edgeName)), null, edgeDefinitionJson);
		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphEntity result = createEntity(res, GraphEntity.class, null, true);

		return result;

	}

	@Override
	public GraphEntity deleteEdgeDefinition(
		String databaseName,
		String graphName,
		String edgeName,
		Boolean dropCollection) throws ArangoException {
		validateCollectionName(graphName);
		validateCollectionName(edgeName);

		HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(databaseName, "/_api/gharial",
			StringUtils.encodeUrl(graphName), "/edge", StringUtils.encodeUrl(edgeName)),
			new MapBuilder().put("dropCollection", dropCollection).get());
		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		GraphEntity result = createEntity(res, GraphEntity.class, null, true);

		return result;
	}

	@Override
	public <T> VertexEntity<T> createVertex(
		String database,
		String graphName,
		String collectionName,
		T vertex,
		Boolean waitForSync) throws ArangoException {
		return createVertex(database, graphName, collectionName, null, vertex, waitForSync);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> createVertex(
		String database,
		String graphName,
		String collectionName,
		String key,
		T vertex,
		Boolean waitForSync) throws ArangoException {

		JsonObject obj;
		if (vertex == null) {
			obj = new JsonObject();
		} else {
			JsonElement elem = EntityFactory.toJsonElement(vertex, false);
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
		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "vertex",
				StringUtils.encodeUrl(collectionName)),
			new MapBuilder().put("waitForSync", waitForSync).get(), EntityFactory.toJsonString(obj));

		if (!res.isJsonResponse()) {
			throw new ArangoException("unknown error");
		}

		VertexEntity<T> result = createEntity(res, VertexEntity.class, vertex.getClass());
		if (vertex != null) {
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> getVertex(
		String databaseName,
		String graphName,
		String collectionName,
		String key,
		Class<T> clazz,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doGet(
			createEndpointUrl(StringUtils.encodeUrl(databaseName), "/_api/gharial", StringUtils.encodeUrl(graphName),
				"vertex", StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-Match", ifMatchRevision, true).put("If-None-Match", ifNoneMatchRevision, true)
					.get(),
			new MapBuilder().get());

		return createEntity(res, VertexEntity.class, clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> replaceVertex(
		String databaseName,
		String graphName,
		String collectionName,
		String key,
		T vertex,
		Boolean waitForSync,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPut(
			createEndpointUrl(StringUtils.encodeUrl(databaseName), "/_api/gharial", StringUtils.encodeUrl(graphName),
				"vertex", StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-Match", ifMatchRevision, true).put("If-None-Match", ifNoneMatchRevision, true)
					.get(),
			new MapBuilder().put("waitForSync", waitForSync).get(), EntityFactory.toJsonString(vertex));

		VertexEntity<T> result = createEntity(res, VertexEntity.class, vertex.getClass());
		if (vertex != null) {
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> VertexEntity<T> updateVertex(
		String databaseName,
		String graphName,
		String collectionName,
		String key,
		T vertex,
		Boolean keepNull,
		Boolean waitForSync,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPatch(
			createEndpointUrl(databaseName, "/_api/gharial", StringUtils.encodeUrl(graphName), "vertex",
				StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-Match", ifMatchRevision, true).put("If-None-Match", ifNoneMatchRevision, true)
					.get(),
			new MapBuilder().put("keepNull", keepNull).put("waitForSync", waitForSync).get(),
			EntityFactory.toJsonString(vertex, keepNull != null && !keepNull));

		VertexEntity<T> result = createEntity(res, VertexEntity.class, vertex.getClass());
		if (vertex != null) {
			result.setEntity(vertex);
			annotationHandler.updateDocumentAttributes(result.getEntity(), result.getDocumentRevision(),
				result.getDocumentHandle(), result.getDocumentKey());
		}
		return result;
	}

	@Override
	public DeletedEntity deleteVertex(
		String databaseName,
		String graphName,
		String collectionName,
		String key,
		Boolean waitForSync,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doDelete(
			createEndpointUrl(StringUtils.encodeUrl(databaseName), "/_api/gharial", StringUtils.encodeUrl(graphName),
				"vertex", StringUtils.encodeUrl(collectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-Match", ifMatchRevision, true).put("If-None-Match", ifNoneMatchRevision, true)
					.get(),
			new MapBuilder().put("waitForSync", waitForSync).get());

		return createEntity(res, DeletedEntity.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> createEdge(
		String database,
		String graphName,
		String edgeCollectionName,
		String key,
		String fromHandle,
		String toHandle,
		T value,
		Boolean waitForSync) throws ArangoException {

		JsonObject obj;
		if (value == null) {
			obj = new JsonObject();
		} else {
			JsonElement elem = EntityFactory.toJsonElement(value, false);
			if (elem.isJsonObject()) {
				obj = elem.getAsJsonObject();
			} else {
				throw new IllegalArgumentException("value need object type(not support array, primitive, etc..).");
			}
		}
		if (key != null) {
			obj.addProperty("_key", key);
		}
		obj.addProperty("_from", fromHandle);
		obj.addProperty("_to", toHandle);

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "/edge",
				StringUtils.encodeUrl(edgeCollectionName)),
			new MapBuilder().put("waitForSync", waitForSync).get(), EntityFactory.toJsonString(obj));

		EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
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
		String database,
		String graphName,
		String edgeCollectionName,
		String key,
		Class<T> clazz,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doGet(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "edge",
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true)
					.get(),
			new MapBuilder().get());

		return createEntity(res, EdgeEntity.class, clazz);
	}

	@Override
	public DeletedEntity deleteEdge(
		String database,
		String graphName,
		String edgeCollectionName,
		String key,
		Boolean waitForSync,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doDelete(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "edge",
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true)
					.get(),
			new MapBuilder().put("waitForSync", waitForSync).get());

		return createEntity(res, DeletedEntity.class);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> replaceEdge(
		String database,
		String graphName,
		String edgeCollectionName,
		String key,
		T value,
		Boolean waitForSync,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPut(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "/edge",
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true)
					.get(),
			new MapBuilder().put("waitForSync", waitForSync).get(),
			value == null ? null : EntityFactory.toJsonString(value));

		EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateDocumentAttributes(entity.getEntity(), entity.getDocumentRevision(),
				entity.getDocumentHandle(), entity.getDocumentKey());
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EdgeEntity<T> updateEdge(
		String database,
		String graphName,
		String edgeCollectionName,
		String key,
		T value,
		Boolean waitForSync,
		Boolean keepNull,
		Long ifMatchRevision,
		Long ifNoneMatchRevision) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPatch(
			createEndpointUrl(database, "/_api/gharial", StringUtils.encodeUrl(graphName), "/edge",
				StringUtils.encodeUrl(edgeCollectionName), StringUtils.encodeUrl(key)),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true)
					.get(),
			new MapBuilder().put("waitForSync", waitForSync).put("keepNull", keepNull).get(),
			value == null ? null : EntityFactory.toJsonString(value));

		EdgeEntity<T> entity = createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateDocumentAttributes(entity.getEntity(), entity.getDocumentRevision(),
				entity.getDocumentHandle(), entity.getDocumentKey());
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> CursorEntity<EdgeEntity<T>> getEdges(
		String database,
		String graphName,
		String vertexKey,
		Class<T> clazz,
		Integer batchSize,
		Integer limit,
		Boolean count,
		Direction direction,
		Collection<String> labels,
		ArangoDriver driver,
		FilterCondition... properties) throws ArangoException {

		validateCollectionName(graphName);

		String getEdges = "var db = require('internal').db;\n" + "var graph = require('org/arangodb/general-graph');\n"
				+ "var g = graph._graph('" + graphName + "');\n" + "g._edges();";
		driver.executeScript(getEdges);

		Map<String, Object> filter = new MapBuilder().put("direction", toLower(direction)).put("labels", labels)
				.put("properties", properties).get();

		HttpResponseEntity res = httpManager.doPost(
			createEndpointUrl(database, "/_api/graph", StringUtils.encodeUrl(graphName), "edges",
				StringUtils.encodeUrl(vertexKey)),
			null, EntityFactory.toJsonString(new MapBuilder().put("batchSize", batchSize).put("limit", limit)
					.put("count", count).put("filter", filter).get()));

		return createEntity(res, CursorEntity.class, EdgeEntity.class, clazz);

	}

	private String convertToString(EdgeDefinitionEntity edgeDefinition) {
		JsonObject rawEdgeDefinition = (JsonObject) EntityFactory
				.toJsonElement(new MapBuilder().put("edgeDefinition", edgeDefinition).get(), false);
		JsonElement edgeDefinitionJson = rawEdgeDefinition.get("edgeDefinition");
		return edgeDefinitionJson.toString();
	}

	private String toLower(Enum<?> e) {
		if (e == null) {
			return null;
		}
		return e.name().toLowerCase(Locale.US);
	}

}

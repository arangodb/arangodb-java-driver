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

package at.orz.arangodb.impl;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.CursorResultSet;
import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DeletedEntity;
import at.orz.arangodb.entity.Direction;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;
import at.orz.arangodb.entity.GraphsEntity;
import at.orz.arangodb.entity.marker.VertexEntity;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;
import at.orz.arangodb.util.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 */
public class InternalGraphDriverImpl extends BaseArangoDriverWithCursorImpl {

	InternalGraphDriverImpl(ArangoConfigure configure, InternalCursorDriverImpl cursorDriver) {
		super(configure, cursorDriver);
	}
	
	private String toLower(Enum<?> e) {
		if (e == null) {
			return null;
		}
		return e.name().toLowerCase(Locale.US);
	}

	public GraphEntity createGraph(
			String database, 
			String documentKey, String vertices, String edges,
			Boolean waitForSync) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/graph"), 
				new MapBuilder().put("waitForSync", waitForSync).get(), 
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("_key", documentKey)
						.put("vertices", vertices)
						.put("edges", edges).get()));
		
		return createEntity(res, GraphEntity.class);
		
	}
	
	public GraphsEntity getGraphs(String database) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/graph"));
		
		return createEntity(res, GraphsEntity.class);
		
	}

	public GraphEntity getGraph(String database, String name, Long IfNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
		
		// TODO: If-Non-Match, If-Match Header
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(name)),
				new MapBuilder().put("If-None-Match", IfNoneMatchRevision, true).put("If-Match", ifMatchRevision).get(),
				null);
		
		return createEntity(res, GraphEntity.class);
		
	}

	public DeletedEntity deleteGraph(String database, String name, Long ifMatchRevision) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doDelete(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(name)), 
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(),
				null);
		
		return createEntity(res, DeletedEntity.class);
		
	}
	
	public <T> DocumentEntity<T> createVertex(String database, String graphName, Object vertex, Boolean waitForSync) throws ArangoException {
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertex"), 
				new MapBuilder().put("waitForSync", waitForSync).get(),
				EntityFactory.toJsonString(vertex));
		return createEntity(res, VertexEntity.class, vertex.getClass());
	}

	public <T> DocumentEntity<T> getVertex(
			String database, 
			String graphName, String key, Class<?> clazz, 
			Long rev, Long ifNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertex", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("rev", rev).get());
		
		return createEntity(res, VertexEntity.class, clazz);
		
	}
	
	public DeletedEntity deleteVertex(
			String database,
			String graphName, String key,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doDelete(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertex", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("waitForSync", waitForSync).put("rev", rev).get());
		
		return createEntity(res, DeletedEntity.class);
		
	}

	public <T> DocumentEntity<T> replaceVertex(
			String database,
			String graphName, String key, Object vertex, 
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertex", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("waitForSync", waitForSync).put("rev", rev).get(),
				EntityFactory.toJsonString(vertex));
		
		return createEntity(res, VertexEntity.class, vertex.getClass());
		
	}

	public <T> DocumentEntity<T> updateVertex(
			String database,
			String graphName, String key, Object vertex, Boolean keepNull, 
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPatch(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertex", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("keepNull", keepNull).put("waitForSync", waitForSync).put("rev", rev).get(),
				EntityFactory.toJsonString(vertex, keepNull != null && !keepNull)
				);
				
		return createEntity(res, VertexEntity.class, vertex.getClass());
		
	}

	public <T> CursorEntity<DocumentEntity<T>> getVertices(
			String database,
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		
		validateCollectionName(graphName);
		Map<String, Object> filter = new MapBuilder().put("direction", toLower(direction)).put("labels", labels).put("properties", properties).get();
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "vertices", StringUtils.encodeUrl(vertexKey)),
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("batchSize", batchSize)
						.put("limit", limit)
						.put("count", count)
						.put("filter", filter)
						.get())
				);

		return createEntity(res, CursorEntity.class, DocumentEntity.class, clazz);
		
	}

	public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
			String database,
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		
		CursorEntity<DocumentEntity<T>> entity = getVertices(database, graphName, vertexKey, clazz, batchSize, limit, count, direction, labels, properties);
		CursorResultSet<DocumentEntity<T>> rs = new CursorResultSet<DocumentEntity<T>>(database, cursorDriver, entity, DocumentEntity.class, clazz);
		return rs;
	}

	public <T> EdgeEntity<T> createEdge(
			String database,
			String graphName, String key, String fromHandle, String toHandle, 
			Object value, String label, Boolean waitForSync
			) throws ArangoException {

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
		obj.addProperty("_key", key);
		obj.addProperty("_from", fromHandle);
		obj.addProperty("_to", toHandle);
		obj.addProperty("$label", label);
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName) , "/edge"),
				new MapBuilder().put("waitForSync", waitForSync).get(),
				EntityFactory.toJsonString(obj));
		
		return createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());
		
	}
	
	public <T> EdgeEntity<T> getEdge(
			String database,
			String graphName, String key, Class<?> clazz,
			Long rev, Long ifNoneMatchRevision, Long ifMatchRevision
			) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "edge", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("rev", rev).get());
		
		return createEntity(res, EdgeEntity.class, clazz);
		
	}
	
	public DeletedEntity deleteEdge(
			String database,
			String graphName, String key,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
		
		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doDelete(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "edge", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("waitForSync", waitForSync).put("rev", rev).get());
		
		return createEntity(res, DeletedEntity.class);
		
	}
	
	public <T> EdgeEntity<T> replaceEdge(
			String database,
			String graphName, String key,
			Object value,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {

		validateCollectionName(graphName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName) , "/edge", StringUtils.encodeUrl(key)),
				new MapBuilder().put("If-Match", ifMatchRevision, true).get(), 
				new MapBuilder().put("waitForSync", waitForSync).put("rev", rev).get(),
				value == null ? null : EntityFactory.toJsonString(value));
		
		return createEntity(res, EdgeEntity.class, value == null ? null : value.getClass());

	}

	public <T> CursorEntity<EdgeEntity<T>> getEdges(
			String database,
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		
		validateCollectionName(graphName);
		
		Map<String, Object> filter = new MapBuilder().put("direction", toLower(direction)).put("labels", labels).put("properties", properties).get();
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/graph", StringUtils.encodeUrl(graphName), "edges", StringUtils.encodeUrl(vertexKey)),
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("batchSize", batchSize)
						.put("limit", limit)
						.put("count", count)
						.put("filter", filter)
						.get())
				);

		return createEntity(res, CursorEntity.class, EdgeEntity.class, clazz);
		
	}

	public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
			String database,
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		
		CursorEntity<EdgeEntity<T>> entity = getEdges(database, graphName, vertexKey, clazz, batchSize, limit, count, direction, labels, properties);
		CursorResultSet<EdgeEntity<T>> rs = new CursorResultSet<EdgeEntity<T>>(database, cursorDriver, entity, EdgeEntity.class, clazz);
		return rs;
	}

	
}

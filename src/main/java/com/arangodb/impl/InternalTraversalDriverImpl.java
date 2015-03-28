/**
 * Copyright 2004-2015 triAGENS GmbH, Cologne, Germany
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is triAGENS GmbH, Cologne, Germany
 *
 * @author a-brandt
 * @author Copyright 2015, triAGENS GmbH, Cologne, Germany
 */

package com.arangodb.impl;

import java.util.HashMap;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;

/**
 * @author a-brandt
 */
public class InternalTraversalDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalTraversalDriver {

	InternalTraversalDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public <V, E> TraversalEntity<V, E> getTraversal(
		String databaseName,
		String graphName,
		String edgeCollection,
		String startVertex,
		Class<V> vertexClazz,
		Class<E> edgeClass,
		String filter,
		Long minDepth,
		Long maxDepth,
		String visitor,
		Direction direction,
		String init,
		String expander,
		String sort,
		Strategy strategy,
		Order order,
		ItemOrder itemOrder,
		Uniqueness verticesUniqueness,
		Uniqueness edgesUniqueness,
		Long maxIterations) throws ArangoException {

		Map<String, Object> object = new HashMap<String, Object>();

		if (graphName != null) {
			object.put("graphName", graphName);
		}
		if (edgeCollection != null) {
			object.put("edgeCollection", edgeCollection);
		}
		if (startVertex != null) {
			object.put("startVertex", startVertex);
		}
		if (filter != null) {
			object.put("filter", filter);
		}
		if (minDepth != null) {
			object.put("minDepth", minDepth);
		}
		if (maxDepth != null) {
			object.put("maxDepth", maxDepth);
		}
		if (visitor != null) {
			object.put("visitor", visitor);
		}
		if (direction != null) {
			object.put("direction", direction.toString().toLowerCase());
		}
		if (init != null) {
			object.put("init", init);
		}
		if (expander != null) {
			object.put("expander", expander);
		}
		if (sort != null) {
			object.put("sort", sort);
		}
		if (strategy != null) {
			object.put("strategy", strategy.toString().toLowerCase());
		}
		if (order != null) {
			object.put("order", order.toString().toLowerCase());
		}
		if (itemOrder != null) {
			object.put("itemOrder", itemOrder.toString().toLowerCase());
		}
		if (verticesUniqueness != null || edgesUniqueness != null) {
			Map<String, Object> uniqueness = new HashMap<String, Object>();

			if (verticesUniqueness != null) {
				uniqueness.put("vertices", verticesUniqueness.toString().toLowerCase());
			}
			if (edgesUniqueness != null) {
				uniqueness.put("edges", edgesUniqueness.toString().toLowerCase());
			}

			object.put("uniqueness", uniqueness);
		}
		if (maxIterations != null) {
			object.put("maxIterations", maxIterations);
		}

		String body = EntityFactory.toJsonString(object);

		HttpResponseEntity response = httpManager.doPost(createEndpointUrl(baseUrl, databaseName, "/_api/traversal"),
			null, body);

		// TraversalEntity<V, E> traversal = createEntity(response,
		// TraversalEntity.class);
		//
		// return traversal;

		return null;
	}

}

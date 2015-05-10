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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.TraversalQueryOptions;

/**
 * @author a-brandt
 */
public class InternalTraversalDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalTraversalDriver {

	InternalTraversalDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> TraversalEntity<V, E> getTraversal(
		String databaseName,
		TraversalQueryOptions traversalQueryOptions,
		Class<V> vertexClazz,
		Class<E> edgeClazz) throws ArangoException {

		if (traversalQueryOptions == null) {
			traversalQueryOptions = new TraversalQueryOptions();
		}

		String body = EntityFactory.toJsonString(traversalQueryOptions.toMap());

		HttpResponseEntity response = httpManager
				.doPost(createEndpointUrl(databaseName, "/_api/traversal"), null, body);

		return createEntity(response, TraversalEntity.class, vertexClazz, edgeClazz);
	}

}

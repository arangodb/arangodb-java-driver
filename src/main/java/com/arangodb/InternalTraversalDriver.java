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

package com.arangodb;

import com.arangodb.entity.TraversalEntity;
import com.arangodb.impl.BaseDriverInterface;
import com.arangodb.util.TraversalQueryOptions;

public interface InternalTraversalDriver extends BaseDriverInterface {

	public enum Strategy {
		DEPTHFIRST,
		BREADTHFIRST
	}

	public enum Order {
		PREORDER,
		POSTORDER
	}

	public enum ItemOrder {
		FORWARD,
		BACKWARD
	}

	public enum Uniqueness {
		NONE,
		GLOBAL,
		PATH
	}

	<V, E> TraversalEntity<V, E> getTraversal(
		String databaseName,
		TraversalQueryOptions traversalQueryOptions,
		Class<V> vertexClazz,
		Class<E> edgeClazz) throws ArangoException;
}

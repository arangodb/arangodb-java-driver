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

package com.arangodb.entity;

/**
 * @author a-brandt
 */
public class TraversalEntity<V, E> extends BaseEntity {

	/**
	 * Visited vertices and edges.
	 */
	private VisitedEntity<V, E> visited;

	public VisitedEntity<V, E> getVisited() {
		return visited;
	}

	public void setVisited(VisitedEntity<V, E> visited) {
		this.visited = visited;
	}

}

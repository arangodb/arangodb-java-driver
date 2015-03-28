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

import java.util.List;

import com.arangodb.entity.marker.VertexEntity;

/**
 * @author a-brandt
 */
public class VisitedEntity<V, E> extends BaseEntity {

	/**
	 * List of vertices.
	 */
	private List<VertexEntity<V>> vertices;

	/**
	 * List of paths.
	 */
	private List<PathEntity<V, E>> paths;

	public List<VertexEntity<V>> getVertices() {
		return vertices;
	}

	public void setVertices(List<VertexEntity<V>> vertices) {
		this.vertices = vertices;
	}

	public List<PathEntity<V, E>> getPaths() {
		return paths;
	}

	public void setPaths(List<PathEntity<V, E>> paths) {
		this.paths = paths;
	}

}

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

/**
 * @author a-brandt
 */
public class ShortestPathEntity<V, E> extends BaseEntity {

	/**
	 * End vertex handle.
	 */
	private String vertex;

	/**
	 * path distance
	 */
	private Long distance;

	/**
	 * start vertex handle
	 */
	private String startVertex;

	/**
	 * List of paths.
	 */
	private List<PathEntity<V, E>> paths;

	public String getVertex() {
		return vertex;
	}

	public void setVertex(String vertex) {
		this.vertex = vertex;
	}

	/**
	 * Retuns the distance
	 * 
	 * @return the distance
	 */
	public Long getDistance() {
		return distance;
	}

	public void setDistance(Long distance) {
		this.distance = distance;
	}

	/**
	 * Returns the document handle of the start vertex
	 * 
	 * @return the document handle of the start vertex
	 */
	public String getStartVertex() {
		return startVertex;
	}

	public void setStartVertex(String startVertex) {
		this.startVertex = startVertex;
	}

	/**
	 * Returns the path list
	 * 
	 * @return list of PathEntity objects
	 */
	public List<PathEntity<V, E>> getPaths() {
		return paths;
	}

	public void setPaths(List<PathEntity<V, E>> paths) {
		this.paths = paths;
	}

}

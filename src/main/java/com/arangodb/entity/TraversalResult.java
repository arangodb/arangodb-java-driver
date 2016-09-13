package com.arangodb.entity;

import java.util.Collection;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class TraversalResult<V, E> {

	private Collection<V> vertices;
	private Collection<PathEntity<V, E>> paths;

	public TraversalResult() {
		super();
	}

	public Collection<V> getVertices() {
		return vertices;
	}

	public void setVertices(final Collection<V> vertices) {
		this.vertices = vertices;
	}

	public Collection<PathEntity<V, E>> getPaths() {
		return paths;
	}

	public void setPaths(final Collection<PathEntity<V, E>> paths) {
		this.paths = paths;
	}

}

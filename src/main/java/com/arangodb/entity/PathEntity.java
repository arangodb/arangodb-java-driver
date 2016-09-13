package com.arangodb.entity;

import java.util.Collection;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class PathEntity<V, E> {

	private Collection<E> edges;
	private Collection<V> vertices;

	public PathEntity() {
		super();
	}

	public Collection<E> getEdges() {
		return edges;
	}

	public void setEdges(final Collection<E> edges) {
		this.edges = edges;
	}

	public Collection<V> getVertices() {
		return vertices;
	}

	public void setVertices(final Collection<V> vertices) {
		this.vertices = vertices;
	}

}

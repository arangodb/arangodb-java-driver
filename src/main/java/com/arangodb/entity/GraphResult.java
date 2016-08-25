package com.arangodb.entity;

import java.util.Collection;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class GraphResult {

	private String name;
	private Collection<EdgeDefinition> edgeDefinitions;
	private Collection<String> orphanCollections;

	public String getName() {
		return name;
	}

	public Collection<EdgeDefinition> getEdgeDefinitions() {
		return edgeDefinitions;
	}

	public Collection<String> getOrphanCollections() {
		return orphanCollections;
	}

}

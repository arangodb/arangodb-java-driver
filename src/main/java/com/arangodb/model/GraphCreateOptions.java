package com.arangodb.model;

import java.util.Arrays;
import java.util.Collection;

import com.arangodb.entity.EdgeDefinition;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class GraphCreateOptions {

	private String name;
	private Collection<EdgeDefinition> edgeDefinitions;
	private Collection<String> orphanCollections;

	protected String getName() {
		return name;
	}

	protected GraphCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

	public Collection<EdgeDefinition> getEdgeDefinitions() {
		return edgeDefinitions;
	}

	public GraphCreateOptions edgeDefinitions(final EdgeDefinition... edgeDefinitions) {
		this.edgeDefinitions = Arrays.asList(edgeDefinitions);
		return this;
	}

	public Collection<String> getOrphanCollections() {
		return orphanCollections;
	}

	public GraphCreateOptions orphanCollections(final String... orphanCollections) {
		this.orphanCollections = Arrays.asList(orphanCollections);
		return this;
	}

}

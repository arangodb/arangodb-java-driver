package com.arangodb.model;

import java.util.Arrays;
import java.util.Collection;

import com.arangodb.entity.EdgeDefinition;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API Documentation</a>
 */
public class GraphCreateOptions {

	private String name;
	private Collection<EdgeDefinition> edgeDefinitions;
	private Collection<String> orphanCollections;

	public GraphCreateOptions() {
		super();
	}

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

	protected GraphCreateOptions edgeDefinitions(final Collection<EdgeDefinition> edgeDefinitions) {
		this.edgeDefinitions = edgeDefinitions;
		return this;
	}

	public Collection<String> getOrphanCollections() {
		return orphanCollections;
	}

	/**
	 * @param orphanCollections
	 *            Additional vertex collections
	 * @return options
	 */
	public GraphCreateOptions orphanCollections(final String... orphanCollections) {
		this.orphanCollections = Arrays.asList(orphanCollections);
		return this;
	}

}

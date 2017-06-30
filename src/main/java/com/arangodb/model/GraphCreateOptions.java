/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.model;

import java.util.Arrays;
import java.util.Collection;

import com.arangodb.entity.EdgeDefinition;

/**
 * @author Mark Vollmary
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Management.html#create-a-graph">API Documentation</a>
 */
public class GraphCreateOptions {

	private String name;
	private Collection<EdgeDefinition> edgeDefinitions;
	private Collection<String> orphanCollections;
	private Boolean isSmart;
	private SmartOptions options;

	public GraphCreateOptions() {
		super();
	}

	protected String getName() {
		return name;
	}

	/**
	 * @param name
	 *            Name of the graph
	 * @return options
	 */
	protected GraphCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

	public Collection<EdgeDefinition> getEdgeDefinitions() {
		return edgeDefinitions;
	}

	/**
	 * @param edgeDefinitions
	 *            An array of definitions for the edge
	 * @return options
	 */
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

	public Boolean getIsSmart() {
		return isSmart;
	}

	/**
	 * 
	 * @param isSmart
	 *            Define if the created graph should be smart. This only has effect in Enterprise version.
	 * @return options
	 */
	public GraphCreateOptions isSmart(final Boolean isSmart) {
		this.isSmart = isSmart;
		return this;
	}

	public Integer getNumberOfShards() {
		return getOptions().getNumberOfShards();
	}

	/**
	 * @param numberOfShards
	 *            The number of shards that is used for every collection within this graph. Cannot be modified later.
	 * @return options
	 */
	public GraphCreateOptions numberOfShards(final Integer numberOfShards) {
		getOptions().setNumberOfShards(numberOfShards);
		return this;
	}

	public String getSmartGraphAttribute() {
		return getOptions().getSmartGraphAttribute();
	}

	/**
	 * @param smartGraphAttribute
	 *            The attribute name that is used to smartly shard the vertices of a graph. Every vertex in this Graph
	 *            has to have this attribute. Cannot be modified later.
	 * @return options
	 */
	public GraphCreateOptions smartGraphAttribute(final String smartGraphAttribute) {
		getOptions().setSmartGraphAttribute(smartGraphAttribute);
		return this;
	}

	private SmartOptions getOptions() {
		if (options == null) {
			options = new SmartOptions();
		}
		return options;
	}

	public static class SmartOptions {
		private Integer numberOfShards;
		private String smartGraphAttribute;

		public SmartOptions() {
			super();
		}

		public Integer getNumberOfShards() {
			return numberOfShards;
		}

		public void setNumberOfShards(final Integer numberOfShards) {
			this.numberOfShards = numberOfShards;
		}

		public String getSmartGraphAttribute() {
			return smartGraphAttribute;
		}

		public void setSmartGraphAttribute(final String smartGraphAttribute) {
			this.smartGraphAttribute = smartGraphAttribute;
		}

	}

}

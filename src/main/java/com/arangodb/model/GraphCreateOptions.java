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
 * @author Mark - mark at arangodb.com
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

	public GraphCreateOptions isSmart(final Boolean isSmart) {
		this.isSmart = isSmart;
		return this;
	}

	public Integer getNumberOfShards() {
		return getOptions().getNumberOfShards();
	}

	public void numberOfShards(final Integer numberOfShards) {
		getOptions().setNumberOfShards(numberOfShards);
	}

	public String getSmartGraphAttribute() {
		return getOptions().getSmartGraphAttribute();
	}

	public void smartGraphAttribute(final String smartGraphAttribute) {
		getOptions().setSmartGraphAttribute(smartGraphAttribute);
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

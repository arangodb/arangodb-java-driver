/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.entity;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 */
public class GraphEntity extends BaseEntity implements DocumentHolder {

	/**
	 * Revision of graph.
	 */
	@SerializedName("_rev")
	String documentRevision;

	/**
	 * Id of graph.
	 */
	@SerializedName("_id")
	String documentHandle;

	/**
	 * Key of graph.
	 */
	@SerializedName("_key")
	String documentKey;

	/**
	 * List of collections of the graph which are not used in an edge
	 * definition.
	 */
	List<String> orphanCollections;

	/**
	 * The edge definitions of the graph.
	 */
	EdgeDefinitionsEntity edgeDefinitionsEntity;

	/**
	 * Name of the graph.
	 */
	String name;

	@Override
	public String getDocumentRevision() {
		return documentRevision;
	}

	@Override
	public String getDocumentHandle() {
		return documentHandle;
	}

	@Override
	public String getDocumentKey() {
		return documentKey;
	}

	@Override
	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
	}

	@Override
	public void setDocumentHandle(final String documentHandle) {
		this.documentHandle = documentHandle;
	}

	@Override
	public void setDocumentKey(final String documentKey) {
		this.documentKey = documentKey;
	}

	public EdgeDefinitionsEntity getEdgeDefinitionsEntity() {
		return edgeDefinitionsEntity;
	}

	public List<EdgeDefinitionEntity> getEdgeDefinitions() {
		return edgeDefinitionsEntity.getEdgeDefinitions();
	}

	public void setEdgeDefinitionsEntity(final EdgeDefinitionsEntity edgeDefinitionsEntity) {
		this.edgeDefinitionsEntity = edgeDefinitionsEntity;
	}

	public void setEdgeDefinitions(final List<EdgeDefinitionEntity> edgeDefinitions) {
		this.edgeDefinitionsEntity.setEdgeDefinitions(edgeDefinitions);
	}

	public List<String> getOrphanCollections() {
		return orphanCollections;
	}

	public void setOrphanCollections(final List<String> orphanCollections) {
		this.orphanCollections = orphanCollections;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}

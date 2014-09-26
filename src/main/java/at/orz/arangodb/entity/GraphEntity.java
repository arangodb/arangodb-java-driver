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

package at.orz.arangodb.entity;

import com.google.gson.annotations.SerializedName;


/**
 * @author tamtam180 - kirscheless at gmail.com
 * @since 1.4.0
 */
public class GraphEntity extends BaseEntity implements DocumentHolder {

	@SerializedName("_rev")
	long documentRevision;
	@SerializedName("_id")
	String documentHandle;
	@SerializedName("_key")
	String documentKey;

	String edges;
	String vertices;

	public long getDocumentRevision() {
		return documentRevision;
	}
	public String getDocumentHandle() {
		return documentHandle;
	}
	public String getDocumentKey() {
		return documentKey;
	}
	public String getEdges() {
		return edges;
	}
	public String getVertices() {
		return vertices;
	}
	public void setDocumentRevision(long documentRevision) {
		this.documentRevision = documentRevision;
	}
	public void setDocumentHandle(String documentHandle) {
		this.documentHandle = documentHandle;
	}
	public void setDocumentKey(String documentKey) {
		this.documentKey = documentKey;
	}
	public void setEdges(String edges) {
		this.edges = edges;
	}
	public void setVertices(String vertices) {
		this.vertices = vertices;
	}
	
}

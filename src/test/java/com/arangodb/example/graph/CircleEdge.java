/*
 * Copyright (C) 2015 ArangoDB GmbH
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

package com.arangodb.example.graph;

import com.arangodb.entity.BaseDocument;
import com.google.gson.annotations.SerializedName;

public class CircleEdge {

	@SerializedName(BaseDocument.ID)
	private String documentHandle;

	@SerializedName(BaseDocument.KEY)
	private String documentKey;

	@SerializedName(BaseDocument.REV)
	private String documentRevision;

	@SerializedName(BaseDocument.FROM)
	String fromVertexHandle;

	@SerializedName(BaseDocument.TO)
	String toVertexHandle;

	private Boolean theFalse;
	private Boolean theTruth;
	private String label;

	public CircleEdge() {
	}

	public CircleEdge(final Boolean theFalse, final Boolean theTruth, final String label) {
		this.theFalse = theFalse;
		this.theTruth = theTruth;
		this.label = label;
	}

	public Boolean getTheFalse() {
		return theFalse;
	}

	public void setTheFalse(final Boolean theFalse) {
		this.theFalse = theFalse;
	}

	public Boolean getTheTruth() {
		return theTruth;
	}

	public void setTheTruth(final Boolean theTruth) {
		this.theTruth = theTruth;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public String getDocumentHandle() {
		return documentHandle;
	}

	public void setDocumentHandle(final String documentHandle) {
		this.documentHandle = documentHandle;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public void setDocumentKey(final String documentKey) {
		this.documentKey = documentKey;
	}

	public String getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
	}

	public String getFromVertexHandle() {
		return fromVertexHandle;
	}

	public String getToVertexHandle() {
		return toVertexHandle;
	}

	public void setFromVertexHandle(final String fromVertexHandle) {
		this.fromVertexHandle = fromVertexHandle;
	}

	public void setToVertexHandle(final String toVertexHandle) {
		this.toVertexHandle = toVertexHandle;
	}

}

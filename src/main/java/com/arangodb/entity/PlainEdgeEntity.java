package com.arangodb.entity;

import com.google.gson.annotations.SerializedName;

public class PlainEdgeEntity extends BaseEntity {

	@SerializedName("_rev")
	String documentRevision;
	@SerializedName("_id")
	String documentHandle;
	@SerializedName("_key")
	String documentKey;
	@SerializedName("_from")
	String fromCollection;
	@SerializedName("_to")
	String toCollection;

	public String getDocumentRevision() {
		return documentRevision;
	}

	public void setDocumentRevision(final String documentRevision) {
		this.documentRevision = documentRevision;
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

	public String getFromCollection() {
		return fromCollection;
	}

	public void setFromCollection(final String fromCollection) {
		this.fromCollection = fromCollection;
	}

	public String getToCollection() {
		return toCollection;
	}

	public void setToCollection(final String toCollection) {
		this.toCollection = toCollection;
	}

}

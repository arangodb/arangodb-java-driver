package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VertexCollectionCreateOptions {

	private String collection;

	public VertexCollectionCreateOptions() {
		super();
	}

	protected String getCollection() {
		return collection;
	}

	protected VertexCollectionCreateOptions collection(final String collection) {
		this.collection = collection;
		return this;
	}

}

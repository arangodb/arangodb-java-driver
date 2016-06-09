package com.arangodb.util;

import java.util.List;

public class GraphVerticesOptions {

	private List<String> vertexCollectionRestriction;

	/**
	 * One or multiple vertex collections that should be considered.
	 * 
	 * @return One or multiple vertex collections that should be considered.
	 */
	public List<String> getVertexCollectionRestriction() {
		return vertexCollectionRestriction;
	}

	/**
	 * One or multiple vertex collections that should be considered.
	 * 
	 * @param vertexCollectionRestriction
	 */
	public void setVertexCollectionRestriction(List<String> vertexCollectionRestriction) {
		this.vertexCollectionRestriction = vertexCollectionRestriction;
	}

}

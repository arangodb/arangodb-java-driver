package com.arangodb.entity;

import com.arangodb.velocypack.annotations.SerializedName;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VertexUpdateResult extends DocumentResult {

	@SerializedName("_oldRev")
	private String oldRev;

	public VertexUpdateResult() {
		super();
	}

	public String getOldRev() {
		return oldRev;
	}

}

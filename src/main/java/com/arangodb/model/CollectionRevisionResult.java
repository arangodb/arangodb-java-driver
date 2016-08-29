package com.arangodb.model;

import com.arangodb.entity.CollectionResult;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionRevisionResult extends CollectionResult {

	private String revision;

	public String getRevision() {
		return revision;
	}

}

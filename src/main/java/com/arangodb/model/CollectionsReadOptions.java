package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionsReadOptions {

	private Boolean excludeSystem;

	public CollectionsReadOptions excludeSystem(final Boolean excludeSystem) {
		this.excludeSystem = excludeSystem;
		return this;
	}

	public Boolean getExcludeSystem() {
		return excludeSystem;
	}

}

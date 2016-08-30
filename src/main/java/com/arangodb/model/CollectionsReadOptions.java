package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#reads-all-collections">API
 *      Documentation</a>
 */
public class CollectionsReadOptions {

	private Boolean excludeSystem;

	public CollectionsReadOptions() {
		super();
	}

	public Boolean getExcludeSystem() {
		return excludeSystem;
	}

	/**
	 * @param excludeSystem
	 *            Whether or not system collections should be excluded from the result.
	 * @return options
	 */
	public CollectionsReadOptions excludeSystem(final Boolean excludeSystem) {
		this.excludeSystem = excludeSystem;
		return this;
	}

}

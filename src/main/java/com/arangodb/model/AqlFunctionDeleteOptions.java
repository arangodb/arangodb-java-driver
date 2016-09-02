package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlFunctionDeleteOptions {

	private Boolean group;

	public AqlFunctionDeleteOptions() {
		super();
	}

	public Boolean getGroup() {
		return group;
	}

	public AqlFunctionDeleteOptions group(final Boolean group) {
		this.group = group;
		return this;
	}

}

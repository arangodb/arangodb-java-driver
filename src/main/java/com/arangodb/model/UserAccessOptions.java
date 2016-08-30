package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class UserAccessOptions {

	private String grant;

	public UserAccessOptions() {
		super();
	}

	protected String getGrant() {
		return grant;
	}

	protected UserAccessOptions grant(final String grant) {
		this.grant = grant;
		return this;
	}

}

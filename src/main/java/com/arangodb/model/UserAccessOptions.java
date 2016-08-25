package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class UserAccessOptions {

	private String grant;

	public String getGrant() {
		return grant;
	}

	public UserAccessOptions grant(final String grant) {
		this.grant = grant;
		return this;
	}

}

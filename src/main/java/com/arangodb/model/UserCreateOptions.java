package com.arangodb.model;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class UserCreateOptions {

	private String user;
	private String passwd;
	private Boolean active;
	private Map<String, Object> extra;

	protected String getUser() {
		return user;
	}

	protected UserCreateOptions user(final String user) {
		this.user = user;
		return this;
	}

	protected String getPasswd() {
		return passwd;
	}

	protected UserCreateOptions passwd(final String passwd) {
		this.passwd = passwd;
		return this;
	}

	public Boolean getActive() {
		return active;
	}

	public UserCreateOptions active(final Boolean active) {
		this.active = active;
		return this;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public UserCreateOptions extra(final Map<String, Object> extra) {
		this.extra = extra;
		return this;
	}

}

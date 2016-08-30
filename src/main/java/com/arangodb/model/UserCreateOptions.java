package com.arangodb.model;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#create-user">API Documentation</a>
 */
public class UserCreateOptions {

	private String user;
	private String passwd;
	private Boolean active;
	private Map<String, Object> extra;

	public UserCreateOptions() {
		super();
	}

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

	/**
	 * @param active
	 *            An optional flag that specifies whether the user is active. If not specified, this will default to
	 *            true
	 * @return options
	 */
	public UserCreateOptions active(final Boolean active) {
		this.active = active;
		return this;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	/**
	 * @param extra
	 *            Optional data about the user
	 * @return options
	 */
	public UserCreateOptions extra(final Map<String, Object> extra) {
		this.extra = extra;
		return this;
	}

}

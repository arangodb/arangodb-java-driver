package com.arangodb.model;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/UserManagement/index.html#replace-user">API Documentation</a>
 */
public class UserUpdateOptions {

	private String passwd;
	private Boolean active;
	private Map<String, Object> extra;

	public UserUpdateOptions() {
		super();
	}

	public String getPasswd() {
		return passwd;
	}

	/**
	 * @param passwd
	 *            The user password
	 * @return options
	 */
	public UserUpdateOptions passwd(final String passwd) {
		this.passwd = passwd;
		return this;
	}

	/**
	 * @param active
	 *            An optional flag that specifies whether the user is active. If not specified, this will default to
	 *            true
	 * @return options
	 */
	public Boolean getActive() {
		return active;
	}

	public UserUpdateOptions active(final Boolean active) {
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
	public UserUpdateOptions extra(final Map<String, Object> extra) {
		this.extra = extra;
		return this;
	}

}

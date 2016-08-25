package com.arangodb.model;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class UserUpdateOptions {

	private String passwd;
	private Boolean active;
	private Map<String, Object> extra;

	public String getPasswd() {
		return passwd;
	}

	public UserUpdateOptions passwd(final String passwd) {
		this.passwd = passwd;
		return this;
	}

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

	public UserUpdateOptions extra(final Map<String, Object> extra) {
		this.extra = extra;
		return this;
	}

}

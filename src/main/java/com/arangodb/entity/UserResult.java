package com.arangodb.entity;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class UserResult {

	private String user;
	private Boolean active;
	private Map<String, Object> extra;
	private Boolean changePassword;

	public String getUser() {
		return user;
	}

	public Boolean getActive() {
		return active;
	}

	public Map<String, Object> getExtra() {
		return extra;
	}

	public Boolean getChangePassword() {
		return changePassword;
	}

}

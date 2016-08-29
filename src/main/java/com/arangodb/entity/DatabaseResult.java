package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DatabaseResult {

	private String id;
	private String name;
	private String path;
	private Boolean isSystem;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

}

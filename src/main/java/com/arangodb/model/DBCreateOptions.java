package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCreateOptions {

	private String name;

	public String getName() {
		return name;
	}

	public DBCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

}

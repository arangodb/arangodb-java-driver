package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCreateOptions {

	private String name;

	public DBCreateOptions() {
		super();
	}

	public String getName() {
		return name;
	}

	protected DBCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

}

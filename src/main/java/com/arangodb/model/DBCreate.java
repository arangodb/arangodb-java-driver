package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
@SuppressWarnings("unused")
public class DBCreate {

	private final String name;

	private DBCreate(final String name) {
		super();
		this.name = name;
	}

	public static class Options {

		public DBCreate build(final String name) {
			return new DBCreate(name);
		}
	}
}

package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlQueryParseOptions {

	private String query;

	public AqlQueryParseOptions() {
		super();
	}

	protected String getQuery() {
		return query;
	}

	protected AqlQueryParseOptions query(final String query) {
		this.query = query;
		return this;
	}

}

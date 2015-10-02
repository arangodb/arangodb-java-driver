package com.arangodb.entity;

import java.util.List;

public class QueriesResultEntity extends BaseEntity {

	/**
	 * The list of queries
	 */
	private List<QueryEntity> queries;

	public QueriesResultEntity() {

	}

	public QueriesResultEntity(List<QueryEntity> queries) {
		this.queries = queries;
	}

	/**
	 * Returns the list of queries
	 * 
	 * @return the list of queries
	 */
	public List<QueryEntity> getQueries() {
		return queries;
	}

	/**
	 * Sets the list of queries
	 * 
	 * @param queries
	 *            the list of queries
	 */
	public void setQueries(List<QueryEntity> queries) {
		this.queries = queries;
	}

}

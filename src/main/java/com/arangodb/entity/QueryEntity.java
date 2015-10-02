package com.arangodb.entity;

import java.util.Date;

public class QueryEntity {

	/**
	 * the query's id
	 */
	private String id;

	/**
	 * the query string (potentially truncated)
	 */
	private String query;

	/**
	 * the date and time when the query was started
	 */
	private Date started;

	/**
	 * the query's run time up to the point the list of queries was queried
	 */
	private Double runTime;

	/**
	 * Returns the query's id
	 * 
	 * @return the query's id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the query's id
	 * 
	 * @param id
	 *            the query's id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the query string (potentially truncated)
	 * 
	 * @return
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Set the query string
	 * 
	 * @param query
	 *            the query string
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Returns the date and time when the query was started
	 * 
	 * @return the date and time
	 */
	public Date getStarted() {
		return started;
	}

	/**
	 * Sets the date and time when the query was started
	 * 
	 * @param started
	 *            the date and time
	 */
	public void setStarted(Date started) {
		this.started = started;
	}

	/**
	 * Returns the query's run time up to the point the list of queries was
	 * queried
	 * 
	 * @return the query's run time
	 */
	public Double getRunTime() {
		return runTime;
	}

	/**
	 * Sets the query's run time up to the point the list of queries was queried
	 * 
	 * @param runTime
	 *            the query's run time
	 */

	public void setRunTime(Double runTime) {
		this.runTime = runTime;
	}

}

/*
 * Copyright (C) 2012,2013 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.entity;

/**
 * @author a-brandt
 */
public class QueryCachePropertiesEntity extends BaseEntity {

	/**
	 * The mode the AQL query cache operates in. The mode is one of the
	 * following values: "off", "on" or "demand".
	 */
	private String mode;

	/**
	 * The maximum number of query results that will be stored per
	 * database-specific cache.
	 */
	private Long maxResults;

	public QueryCachePropertiesEntity() {
	}

	/**
	 * Returns the mode the AQL query cache operates in.
	 * 
	 * @return The mode is one of the following values: "off", "on" or "demand".
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Sets the mode the AQL query cache operates in.
	 * 
	 * @param mode
	 *            The mode is one of the following values: "off", "on" or
	 *            "demand".
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Returns the maximum number of query results that will be stored per
	 * database-specific cache.
	 * 
	 * @return the maximum number
	 */
	public Long getMaxResults() {
		return maxResults;
	}

	/**
	 * Sets the maximum number of query results that will be stored per
	 * database-specific cache.
	 * 
	 * @param maxResults
	 *            the maximum number
	 */
	public void setMaxResults(Long maxResults) {
		this.maxResults = maxResults;
	}

}

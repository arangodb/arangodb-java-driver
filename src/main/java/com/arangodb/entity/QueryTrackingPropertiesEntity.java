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
public class QueryTrackingPropertiesEntity extends BaseEntity {

	/**
	 * if set to true, then queries will be tracked. If set to false, neither
	 * queries nor slow queries will be tracked.
	 */
	private Boolean enabled;

	/**
	 * if set to true, then slow queries will be tracked in the list of slow
	 * queries if their runtime exceeds the value set in slowQueryThreshold. In
	 * order for slow queries to be tracked, the enabled property must also be
	 * set to true.
	 */
	private Boolean trackSlowQueries;

	/**
	 * the maximum number of slow queries to keep in the list of slow queries.
	 * If the list of slow queries is full, the oldest entry in it will be
	 * discarded when additional slow queries occur.
	 */
	private Long maxSlowQueries;

	/**
	 * the threshold value for treating a query as slow. A query with a runtime
	 * greater or equal to this threshold value will be put into the list of
	 * slow queries when slow query tracking is enabled. The value for
	 * slowQueryThreshold is specified in seconds.
	 */
	private Long slowQueryThreshold;

	/**
	 * the maximum query string length to keep in the list of queries. Query
	 * strings can have arbitrary lengths, and this property can be used to save
	 * memory in case very long query strings are used. The value is specified
	 * in bytes.
	 */
	private Long maxQueryStringLength;

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getTrackSlowQueries() {
		return trackSlowQueries;
	}

	public void setTrackSlowQueries(Boolean trackSlowQueries) {
		this.trackSlowQueries = trackSlowQueries;
	}

	public Long getMaxSlowQueries() {
		return maxSlowQueries;
	}

	public void setMaxSlowQueries(Long maxSlowQueries) {
		this.maxSlowQueries = maxSlowQueries;
	}

	public Long getSlowQueryThreshold() {
		return slowQueryThreshold;
	}

	public void setSlowQueryThreshold(Long slowQueryThreshold) {
		this.slowQueryThreshold = slowQueryThreshold;
	}

	public Long getMaxQueryStringLength() {
		return maxQueryStringLength;
	}

	public void setMaxQueryStringLength(Long maxQueryStringLength) {
		this.maxQueryStringLength = maxQueryStringLength;
	}

}

/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.model;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
 *      Documentation</a>
 */
public class AqlQueryOptions {

	private Boolean count;
	private Integer ttl;
	private Integer batchSize;
	private Boolean cache;
	private Map<String, Object> bindVars;
	private String query;
	private Options options;

	public AqlQueryOptions() {
		super();
	}

	public Boolean getCount() {
		return count;
	}

	/**
	 * @param count
	 *            indicates whether the number of documents in the result set should be returned in the "count"
	 *            attribute of the result. Calculating the "count" attribute might have a performance impact for some
	 *            queries in the future so this option is turned off by default, and "count" is only returned when
	 *            requested.
	 * @return options
	 */
	public AqlQueryOptions count(final Boolean count) {
		this.count = count;
		return this;
	}

	public Integer getTtl() {
		return ttl;
	}

	/**
	 * @param ttl
	 *            The time-to-live for the cursor (in seconds). The cursor will be removed on the server automatically
	 *            after the specified amount of time. This is useful to ensure garbage collection of cursors that are
	 *            not fully fetched by clients. If not set, a server-defined value will be used.
	 * @return options
	 */
	public AqlQueryOptions ttl(final Integer ttl) {
		this.ttl = ttl;
		return this;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	/**
	 * @param batchSize
	 *            maximum number of result documents to be transferred from the server to the client in one roundtrip.
	 *            If this attribute is not set, a server-controlled default value will be used. A batchSize value of 0
	 *            is disallowed.
	 * @return options
	 */
	public AqlQueryOptions batchSize(final Integer batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public Boolean getCache() {
		return cache;
	}

	/**
	 * @param cache
	 *            flag to determine whether the AQL query cache shall be used. If set to false, then any query cache
	 *            lookup will be skipped for the query. If set to true, it will lead to the query cache being checked
	 *            for the query if the query cache mode is either on or demand.
	 * @return options
	 */
	public AqlQueryOptions cache(final Boolean cache) {
		this.cache = cache;
		return this;
	}

	protected Map<String, Object> getBindVars() {
		return bindVars;
	}

	/**
	 * @param bindVars
	 *            key/value pairs representing the bind parameters
	 * @return options
	 */
	protected AqlQueryOptions bindVars(final Map<String, Object> bindVars) {
		this.bindVars = bindVars;
		return this;
	}

	protected String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query which you want parse
	 * @return options
	 */
	protected AqlQueryOptions query(final String query) {
		this.query = query;
		return this;
	}

	/**
	 * @return If set to true, then the additional query profiling information will be returned in the sub-attribute
	 *         profile of the extra return attribute if the query result is not served from the query cache.
	 */
	public Boolean getProfile() {
		return options != null ? options.profile : null;
	}

	/**
	 * @param profile
	 *            If set to true, then the additional query profiling information will be returned in the sub-attribute
	 *            profile of the extra return attribute if the query result is not served from the query cache.
	 * @return options
	 */
	public AqlQueryOptions profile(final Boolean profile) {
		getOptions().profile = profile;
		return this;
	}

	public Boolean getFullCount() {
		return options != null ? options.fullCount : null;
	}

	/**
	 * @param fullCount
	 *            if set to true and the query contains a LIMIT clause, then the result will have an extra attribute
	 *            with the sub-attributes stats and fullCount, { ... , "extra": { "stats": { "fullCount": 123 } } }. The
	 *            fullCount attribute will contain the number of documents in the result before the last LIMIT in the
	 *            query was applied. It can be used to count the number of documents that match certain filter criteria,
	 *            but only return a subset of them, in one go. It is thus similar to MySQL's SQL_CALC_FOUND_ROWS hint.
	 *            Note that setting the option will disable a few LIMIT optimizations and may lead to more documents
	 *            being processed, and thus make queries run longer. Note that the fullCount attribute will only be
	 *            present in the result if the query has a LIMIT clause and the LIMIT clause is actually used in the
	 *            query.
	 * @return options
	 */
	public AqlQueryOptions fullCount(final Boolean fullCount) {
		getOptions().fullCount = fullCount;
		return this;
	}

	public Integer getMaxPlans() {
		return options != null ? options.maxPlans : null;
	}

	/**
	 * 
	 * @param maxPlans
	 *            Limits the maximum number of plans that are created by the AQL query optimizer.
	 * @return options
	 */
	public AqlQueryOptions maxPlans(final Integer maxPlans) {
		getOptions().maxPlans = maxPlans;
		return this;
	}

	public Collection<String> getRules() {
		return options != null ? options.optimizer != null ? options.optimizer.rules : null : null;
	}

	/**
	 * 
	 * @param rules
	 *            A list of to-be-included or to-be-excluded optimizer rules can be put into this attribute, telling the
	 *            optimizer to include or exclude specific rules. To disable a rule, prefix its name with a -, to enable
	 *            a rule, prefix it with a +. There is also a pseudo-rule all, which will match all optimizer rules
	 * @return options
	 */
	public AqlQueryOptions rules(final Collection<String> rules) {
		getOptions().getOptimizer().rules = rules;
		return this;
	}

	private Options getOptions() {
		if (options == null) {
			options = new Options();
		}
		return options;
	}

	private static class Options {
		private Boolean profile;
		private Optimizer optimizer;
		private Boolean fullCount;
		private Integer maxPlans;

		protected Optimizer getOptimizer() {
			if (optimizer == null) {
				optimizer = new Optimizer();
			}
			return optimizer;
		}

	}

	private static class Optimizer {
		private Collection<String> rules;
	}

}

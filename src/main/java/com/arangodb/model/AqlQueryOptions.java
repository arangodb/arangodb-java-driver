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

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://docs.arangodb.com/current/HTTP/AqlQueryCursor/AccessingCursors.html#create-cursor">API
 * Documentation</a>
 */
public class AqlQueryOptions implements Serializable {

	private static final long serialVersionUID = 1L;

	private Boolean count;
	private Integer ttl;
	private Integer batchSize;
	private Boolean cache;
	private Long memoryLimit;
	private VPackSlice bindVars;
	private String query;
	private Options options;
	@Expose(serialize = false)
	private Boolean allowDirtyRead;
	private String streamTransactionId;

	public AqlQueryOptions() {
		super();
	}

	public Boolean getCount() {
		return count;
	}

	/**
	 * @param count indicates whether the number of documents in the result set should be returned in the "count"
	 *              attribute of the result. Calculating the "count" attribute might have a performance impact for some
	 *              queries in the future so this option is turned off by default, and "count" is only returned when
	 *              requested.
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
	 * @param ttl The time-to-live for the cursor (in seconds). The cursor will be removed on the server automatically
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
	 * @param batchSize maximum number of result documents to be transferred from the server to the client in one roundtrip.
	 *                  If this attribute is not set, a server-controlled default value will be used. A batchSize value of 0
	 *                  is disallowed.
	 * @return options
	 */
	public AqlQueryOptions batchSize(final Integer batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public Long getMemoryLimit() {
		return memoryLimit;
	}

	/**
	 * @param memoryLimit the maximum number of memory (measured in bytes) that the query is allowed to use. If set, then the
	 *                    query will fail with error "resource limit exceeded" in case it allocates too much memory. A value of
	 *                    0 indicates that there is no memory limit.
	 * @return options
	 * @since ArangoDB 3.1.0
	 */
	public AqlQueryOptions memoryLimit(final Long memoryLimit) {
		this.memoryLimit = memoryLimit;
		return this;
	}

	public Boolean getCache() {
		return cache;
	}

	/**
	 * @param cache flag to determine whether the AQL query cache shall be used. If set to false, then any query cache
	 *              lookup will be skipped for the query. If set to true, it will lead to the query cache being checked
	 *              for the query if the query cache mode is either on or demand.
	 * @return options
	 */
	public AqlQueryOptions cache(final Boolean cache) {
		this.cache = cache;
		return this;
	}

	protected VPackSlice getBindVars() {
		return bindVars;
	}

	/**
	 * @param bindVars key/value pairs representing the bind parameters
	 * @return options
	 */
	protected AqlQueryOptions bindVars(final VPackSlice bindVars) {
		this.bindVars = bindVars;
		return this;
	}

	protected String getQuery() {
		return query;
	}

	/**
	 * @param query the query which you want parse
	 * @return options
	 */
	protected AqlQueryOptions query(final String query) {
		this.query = query;
		return this;
	}

	public Boolean getFailOnWarning() {
		return options != null ? options.failOnWarning : null;
	}

	/**
	 * @param failOnWarning When set to true, the query will throw an exception and abort instead of producing a warning. This
	 *                      option should be used during development to catch potential issues early. When the attribute is set to
	 *                      false, warnings will not be propagated to exceptions and will be returned with the query result. There
	 *                      is also a server configuration option --query.fail-on-warning for setting the default value for
	 *                      failOnWarning so it does not need to be set on a per-query level.
	 * @return options
	 */
	public AqlQueryOptions failOnWarning(final Boolean failOnWarning) {
		getOptions().failOnWarning = failOnWarning;
		return this;
	}

	/**
	 * @return If set to true, then the additional query profiling information will be returned in the sub-attribute
	 * profile of the extra return attribute if the query result is not served from the query cache.
	 */
	public Boolean getProfile() {
		return options != null ? options.profile : null;
	}

	/**
	 * @param profile If set to true, then the additional query profiling information will be returned in the sub-attribute
	 *                profile of the extra return attribute if the query result is not served from the query cache.
	 * @return options
	 */
	public AqlQueryOptions profile(final Boolean profile) {
		getOptions().profile = profile;
		return this;
	}

	public Long getMaxTransactionSize() {
		return options != null ? options.maxTransactionSize : null;
	}

	/**
	 * @param maxTransactionSize Transaction size limit in bytes. Honored by the RocksDB storage engine only.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions maxTransactionSize(final Long maxTransactionSize) {
		getOptions().maxTransactionSize = maxTransactionSize;
		return this;
	}

	public Long getMaxWarningCount() {
		return options != null ? options.maxWarningCount : null;
	}

	/**
	 * @param maxWarningCount Limits the maximum number of warnings a query will return. The number of warnings a query will return
	 *                        is limited to 10 by default, but that number can be increased or decreased by setting this attribute.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions maxWarningCount(final Long maxWarningCount) {
		getOptions().maxWarningCount = maxWarningCount;
		return this;
	}

	public Long getIntermediateCommitCount() {
		return options != null ? options.intermediateCommitCount : null;
	}

	/**
	 * @param intermediateCommitCount Maximum number of operations after which an intermediate commit is performed automatically. Honored by
	 *                                the RocksDB storage engine only.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions intermediateCommitCount(final Long intermediateCommitCount) {
		getOptions().intermediateCommitCount = intermediateCommitCount;
		return this;
	}

	public Long getIntermediateCommitSize() {
		return options != null ? options.intermediateCommitSize : null;
	}

	/**
	 * @param intermediateCommitSize Maximum total size of operations after which an intermediate commit is performed automatically.
	 *                               Honored by the RocksDB storage engine only.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions intermediateCommitSize(final Long intermediateCommitSize) {
		getOptions().intermediateCommitSize = intermediateCommitSize;
		return this;
	}

	public Double getSatelliteSyncWait() {
		return options != null ? options.satelliteSyncWait : null;
	}

	/**
	 * @param satelliteSyncWait This enterprise parameter allows to configure how long a DBServer will have time to bring the
	 *                          satellite collections involved in the query into sync. The default value is 60.0 (seconds). When the
	 *                          max time has been reached the query will be stopped.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions satelliteSyncWait(final Double satelliteSyncWait) {
		getOptions().satelliteSyncWait = satelliteSyncWait;
		return this;
	}

	public Boolean getSkipInaccessibleCollections() {
		return options != null ? options.skipInaccessibleCollections : null;
	}

	/**
	 * @param skipInaccessibleCollections AQL queries (especially graph traversals) will treat collection to which a user has no access rights
	 *                                    as if these collections were empty. Instead of returning a forbidden access error, your queries will
	 *                                    execute normally. This is intended to help with certain use-cases: A graph contains several
	 *                                    collections and different users execute AQL queries on that graph. You can now naturally limit the
	 *                                    accessible results by changing the access rights of users on collections. This feature is only
	 *                                    available in the Enterprise Edition.
	 * @return options
	 * @since ArangoDB 3.2.0
	 */
	public AqlQueryOptions skipInaccessibleCollections(final Boolean skipInaccessibleCollections) {
		getOptions().skipInaccessibleCollections = skipInaccessibleCollections;
		return this;
	}

	public Boolean getFullCount() {
		return options != null ? options.fullCount : null;
	}

	/**
	 * @param fullCount if set to true and the query contains a LIMIT clause, then the result will have an extra attribute
	 *                  with the sub-attributes stats and fullCount, { ... , "extra": { "stats": { "fullCount": 123 } } }. The
	 *                  fullCount attribute will contain the number of documents in the result before the last LIMIT in the
	 *                  query was applied. It can be used to count the number of documents that match certain filter criteria,
	 *                  but only return a subset of them, in one go. It is thus similar to MySQL's SQL_CALC_FOUND_ROWS hint.
	 *                  Note that setting the option will disable a few LIMIT optimizations and may lead to more documents
	 *                  being processed, and thus make queries run longer. Note that the fullCount attribute will only be
	 *                  present in the result if the query has a LIMIT clause and the LIMIT clause is actually used in the
	 *                  query.
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
	 * @param maxPlans Limits the maximum number of plans that are created by the AQL query optimizer.
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
	 * @param rules A list of to-be-included or to-be-excluded optimizer rules can be put into this attribute, telling the
	 *              optimizer to include or exclude specific rules. To disable a rule, prefix its name with a -, to enable
	 *              a rule, prefix it with a +. There is also a pseudo-rule all, which will match all optimizer rules
	 * @return options
	 */
	public AqlQueryOptions rules(final Collection<String> rules) {
		getOptions().getOptimizer().rules = rules;
		return this;
	}

	public Boolean getStream() {
		return options != null ? options.stream : null;
	}

	/**
	 * @param stream Specify true and the query will be executed in a streaming fashion. The query result is not stored on
	 *               the server, but calculated on the fly. Beware: long-running queries will need to hold the collection
	 *               locks for as long as the query cursor exists. When set to false a query will be executed right away in
	 *               its entirety. In that case query results are either returned right away (if the resultset is small
	 *               enough), or stored on the arangod instance and accessible via the cursor API (with respect to the
	 *               ttl). It is advisable to only use this option on short-running queries or without exclusive locks
	 *               (write-locks on MMFiles). Please note that the query options cache, count and fullCount will not work
	 *               on streaming queries. Additionally query statistics, warnings and profiling data will only be
	 *               available after the query is finished. The default value is false
	 * @return options
	 * @since ArangoDB 3.4.0
	 */
	public AqlQueryOptions stream(final Boolean stream) {
		getOptions().stream = stream;
		return this;
	}

	public Collection<String> getShardIds() {
		return options != null ? options.shardIds : null;
	}

	/**
	 * Restrict query to shards by given ids. This is an internal option. Use at your own risk.
	 *
	 * @param shardIds
	 * @return options
	 */
	public AqlQueryOptions shardIds(final String... shardIds) {
		getOptions().getShardIds().addAll(Arrays.asList(shardIds));
		return this;
	}

	private Options getOptions() {
		if (options == null) {
			options = new Options();
		}
		return options;
	}

	private static class Options implements Serializable {

		private static final long serialVersionUID = 1L;

		private Boolean failOnWarning;
		private Boolean profile;
		private Long maxTransactionSize;
		private Long maxWarningCount;
		private Long intermediateCommitCount;
		private Long intermediateCommitSize;
		private Double satelliteSyncWait;
		private Boolean skipInaccessibleCollections;
		private Optimizer optimizer;
		private Boolean fullCount;
		private Integer maxPlans;
		private Boolean stream;
		private Collection<String> shardIds;

		protected Optimizer getOptimizer() {
			if (optimizer == null) {
				optimizer = new Optimizer();
			}
			return optimizer;
		}

		protected Collection<String> getShardIds() {
			if (shardIds == null) {
                shardIds = new ArrayList<>();
			}
			return shardIds;
		}

	}

	private static class Optimizer {
		private Collection<String> rules;
	}

	/**
	 * @param allowDirtyRead Set to {@code true} allows reading from followers in an active-failover setup.
	 * @return options
	 * @see <a href="https://docs.arangodb.com/current/Manual/Administration/ActiveFailover/#reading-from-follower">API
	 * Documentation</a>
	 * @since ArangoDB 3.4.0
	 */
	public AqlQueryOptions allowDirtyRead(final Boolean allowDirtyRead) {
		this.allowDirtyRead = allowDirtyRead;
		return this;
	}

	public Boolean getAllowDirtyRead() {
		return allowDirtyRead;
	}

	public String getStreamTransactionId() {
		return streamTransactionId;
	}

	/**
	 * @param streamTransactionId If set, the operation will be executed within the transaction.
	 * @return options
	 * @since ArangoDB 3.5.0
	 */
	public AqlQueryOptions streamTransactionId(final String streamTransactionId) {
		this.streamTransactionId = streamTransactionId;
		return this;
	}

}

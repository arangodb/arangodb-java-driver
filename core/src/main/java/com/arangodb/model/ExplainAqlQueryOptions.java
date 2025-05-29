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

import com.arangodb.internal.serde.UserDataInside;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Michele Rastelli
 */
public final class ExplainAqlQueryOptions {

    private Map<String, Object> bindVars;
    private String query;
    private AqlQueryOptions.Options options;

    public ExplainAqlQueryOptions() {
        super();
    }

    @UserDataInside
    public Map<String, Object> getBindVars() {
        return bindVars;
    }

    /**
     * @param bindVars key/value pairs representing the bind parameters
     * @return options
     */
    ExplainAqlQueryOptions bindVars(final Map<String, Object> bindVars) {
        this.bindVars = bindVars;
        return this;
    }

    public String getQuery() {
        return query;
    }

    /**
     * @param query the query which you want explained
     * @return options
     */
    ExplainAqlQueryOptions query(final String query) {
        this.query = query;
        return this;
    }

    public AqlQueryOptions.Options getOptions() {
        if (options == null) {
            options = new AqlQueryOptions.Options();
        }
        return options;
    }

    public ExplainAqlQueryOptions options(final AqlQueryOptions.Options options) {
        this.options = options;
        return this;
    }


    // ------------------------------------
    // --- accessors for nested options ---
    // ------------------------------------

    @JsonIgnore
    public Map<String, Object> getCustomOptions() {
        return getOptions().getCustomOptions();
    }

    /**
     * Set an additional custom option in the form of key-value pair.
     *
     * @param key   option name
     * @param value option value
     * @return this
     */
    public ExplainAqlQueryOptions customOption(String key, Object value) {
        getOptions().setCustomOption(key, value);
        return this;
    }

    @JsonIgnore
    public Boolean getAllPlans() {
        return getOptions().getAllPlans();
    }

    /**
     * @param value if set to true, all possible execution plans will be returned. The default is false, meaning only
     *              the optimal plan will be returned.
     * @return this
     */
    public ExplainAqlQueryOptions allPlans(final Boolean value) {
        getOptions().setAllPlans(value);
        return this;
    }

    @JsonIgnore
    public Boolean getAllowDirtyReads() {
        return getOptions().getAllowDirtyReads();
    }

    /**
     * @param allowDirtyReads If you set this option to true and execute the query against a cluster deployment, then
     *                        the Coordinator is allowed to read from any shard replica and not only from the leader.
     *                        You may observe data inconsistencies (dirty reads) when reading from followers, namely
     *                        obsolete revisions of documents because changes have not yet been replicated to the
     *                        follower, as well as changes to documents before they are officially committed on the
     *                        leader.
     * @return this
     */
    public ExplainAqlQueryOptions allowDirtyReads(final Boolean allowDirtyReads) {
        getOptions().setAllowDirtyReads(allowDirtyReads);
        return this;
    }

    @JsonIgnore
    public Boolean getAllowRetry() {
        return getOptions().getAllowRetry();
    }

    /**
     * @param allowRetry Set this option to true to make it possible to retry fetching the latest batch from a cursor.
     *                   <p/>
     *                   This makes possible to safely retry invoking {@link com.arangodb.ArangoCursor#next()} in
     *                   case of I/O exceptions (which are actually thrown as {@link com.arangodb.ArangoDBException}
     *                   with cause {@link java.io.IOException})
     *                   <p/>
     *                   If set to false (default), then it is not safe to retry invoking
     *                   {@link com.arangodb.ArangoCursor#next()} in case of I/O exceptions, since the request to
     *                   fetch the next batch is not idempotent (i.e. the cursor may advance multiple times on the
     *                   server).
     *                   <p/>
     *                   Note: once you successfully received the last batch, you should call
     *                   {@link com.arangodb.ArangoCursor#close()} so that the server does not unnecessary keep the
     *                   batch until the cursor times out ({@link AqlQueryOptions#ttl(Integer)}).
     * @return this
     * @since ArangoDB 3.11
     */
    public ExplainAqlQueryOptions allowRetry(final Boolean allowRetry) {
        getOptions().setAllowRetry(allowRetry);
        return this;
    }

    @JsonIgnore
    public Boolean getFailOnWarning() {
        return getOptions().getFailOnWarning();
    }

    /**
     * @param failOnWarning When set to true, the query will throw an exception and abort instead of producing a
     *                      warning. This option should be used during development to catch potential issues early.
     *                      When the attribute is set to false, warnings will not be propagated to exceptions and will
     *                      be returned with the query result. There is also a server configuration option
     *                      --query.fail-on-warning for setting the default value for failOnWarning so it does not
     *                      need to be set on a per-query level.
     * @return this
     */
    public ExplainAqlQueryOptions failOnWarning(final Boolean failOnWarning) {
        getOptions().setFailOnWarning(failOnWarning);
        return this;
    }

    @JsonIgnore
    public Boolean getFillBlockCache() {
        return getOptions().getFillBlockCache();
    }

    /**
     * @param fillBlockCache if set to <code>true</code> or not specified, this will make the query store
     *                       the data it reads via the RocksDB storage engine in the RocksDB block cache. This is
     *                       usually the desired behavior. The option can be set to <code>false</code> for queries that
     *                       are known to either read a lot of data that would thrash the block cache, or for queries
     *                       that read data known to be outside of the hot set. By setting the option
     *                       to <code>false</code>, data read by the query will not make it into the RocksDB block
     *                       cache if it is not already in there, thus leaving more room for the actual hot set.
     * @return this
     * @since ArangoDB 3.8.1
     */
    public ExplainAqlQueryOptions fillBlockCache(final Boolean fillBlockCache) {
        getOptions().setFillBlockCache(fillBlockCache);
        return this;
    }

    @JsonIgnore
    public String getForceOneShardAttributeValue() {
        return getOptions().getForceOneShardAttributeValue();
    }

    /**
     * @param forceOneShardAttributeValue This query option can be used in complex queries in case the query optimizer
     *                                    cannot automatically detect that the query can be limited to only a single
     *                                    server (e.g. in a disjoint smart graph case).
     *                                    <p/>
     *                                    If the option is set incorrectly, i.e. to a wrong shard key value, then the
     *                                    query may be shipped to a wrong DB server and may not return results (i.e.
     *                                    empty result set).
     *                                    <p/>
     *                                    Use at your own risk.
     * @return this
     */
    public ExplainAqlQueryOptions forceOneShardAttributeValue(final String forceOneShardAttributeValue) {
        getOptions().setForceOneShardAttributeValue(forceOneShardAttributeValue);
        return this;
    }

    @JsonIgnore
    public Boolean getFullCount() {
        return getOptions().getFullCount();
    }

    /**
     * @param fullCount if set to true and the query contains a LIMIT clause, then the result will have an extra
     *                  attribute
     *                  with the sub-attributes stats and fullCount, { ... , "extra": { "stats": { "fullCount": 123 }
     *                  } }. The
     *                  fullCount attribute will contain the number of documents in the result before the last LIMIT
     *                  in the
     *                  query was applied. It can be used to count the number of documents that match certain filter
     *                  criteria,
     *                  but only return a subset of them, in one go. It is thus similar to MySQL's
     *                  SQL_CALC_FOUND_ROWS hint.
     *                  Note that setting the option will disable a few LIMIT optimizations and may lead to more
     *                  documents
     *                  being processed, and thus make queries run longer. Note that the fullCount attribute will
     *                  only be
     *                  present in the result if the query has a LIMIT clause and the LIMIT clause is actually used
     *                  in the
     *                  query.
     * @return this
     */
    public ExplainAqlQueryOptions fullCount(final Boolean fullCount) {
        getOptions().setFullCount(fullCount);
        return this;
    }

    @JsonIgnore
    public Long getIntermediateCommitCount() {
        return getOptions().getIntermediateCommitCount();
    }

    /**
     * @param intermediateCommitCount Maximum number of operations after which an intermediate commit is performed
     *                                automatically. Honored by
     *                                the RocksDB storage engine only.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions intermediateCommitCount(final Long intermediateCommitCount) {
        getOptions().setIntermediateCommitCount(intermediateCommitCount);
        return this;
    }

    @JsonIgnore
    public Long getIntermediateCommitSize() {
        return getOptions().getIntermediateCommitSize();
    }

    /**
     * @param intermediateCommitSize Maximum total size of operations after which an intermediate commit is performed
     *                               automatically.
     *                               Honored by the RocksDB storage engine only.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions intermediateCommitSize(final Long intermediateCommitSize) {
        getOptions().setIntermediateCommitSize(intermediateCommitSize);
        return this;
    }

    @JsonIgnore
    public Integer getMaxDNFConditionMembers() {
        return getOptions().getMaxDNFConditionMembers();
    }

    /**
     * @param maxDNFConditionMembers A threshold for the maximum number of OR sub-nodes in the internal representation
     *                               of an AQL FILTER condition.
     *                               <p/>
     *                               Yon can use this option to limit the computation time and memory usage when
     *                               converting complex AQL FILTER conditions into the internal DNF (disjunctive normal
     *                               form) format. FILTER conditions with a lot of logical branches (AND, OR, NOT) can
     *                               take a large amount of processing time and memory. This query option limits the
     *                               computation time and memory usage for such conditions.
     *                               <p/>
     *                               Once the threshold value is reached during the DNF conversion of a FILTER
     *                               condition, the conversion is aborted, and the query continues with a simplified
     *                               internal representation of the condition, which cannot be used for index lookups.
     *                               <p/>
     *                               You can set the threshold globally instead of per query with the
     *                               --query.max-dnf-condition-members startup option.
     * @return this
     */
    public ExplainAqlQueryOptions maxDNFConditionMembers(final Integer maxDNFConditionMembers) {
        getOptions().setMaxDNFConditionMembers(maxDNFConditionMembers);
        return this;
    }

    @JsonIgnore
    public Integer getMaxNodesPerCallstack() {
        return getOptions().getMaxNodesPerCallstack();
    }

    /**
     * @param maxNodesPerCallstack The number of execution nodes in the query plan after that stack splitting is
     *                             performed to avoid a potential stack overflow. Defaults to the configured value of
     *                             the startup option --query.max-nodes-per-callstack.
     *                             <p/>
     *                             This option is only useful for testing and debugging and normally does not need any
     *                             adjustment.
     * @return this
     */
    public ExplainAqlQueryOptions maxNodesPerCallstack(final Integer maxNodesPerCallstack) {
        getOptions().setMaxNodesPerCallstack(maxNodesPerCallstack);
        return this;
    }

    @JsonIgnore
    public Integer getMaxNumberOfPlans() {
        return getOptions().getMaxNumberOfPlans();
    }

    /**
     * @param maxNumberOfPlans Limits the maximum number of plans that are created by the AQL query optimizer.
     * @return this
     */
    public ExplainAqlQueryOptions maxNumberOfPlans(final Integer maxNumberOfPlans) {
        getOptions().setMaxNumberOfPlans(maxNumberOfPlans);
        return this;
    }

    @JsonIgnore
    public Double getMaxRuntime() {
        return getOptions().getMaxRuntime();
    }

    /**
     * @param maxRuntime The query has to be executed within the given runtime or it will be killed. The value is specified
     *                   in seconds. The default value is 0.0 (no timeout).
     * @return this
     */
    public ExplainAqlQueryOptions maxRuntime(final Double maxRuntime) {
        getOptions().setMaxRuntime(maxRuntime);
        return this;
    }

    @JsonIgnore
    public Long getMaxTransactionSize() {
        return getOptions().getMaxTransactionSize();
    }

    /**
     * @param maxTransactionSize Transaction size limit in bytes. Honored by the RocksDB storage engine only.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions maxTransactionSize(final Long maxTransactionSize) {
        getOptions().setMaxTransactionSize(maxTransactionSize);
        return this;
    }

    @JsonIgnore
    public Long getMaxWarningCount() {
        return getOptions().getMaxWarningCount();
    }

    /**
     * @param maxWarningCount Limits the maximum number of warnings a query will return. The number of warnings a
     *                        query will return
     *                        is limited to 10 by default, but that number can be increased or decreased by setting
     *                        this attribute.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions maxWarningCount(final Long maxWarningCount) {
        getOptions().setMaxWarningCount(maxWarningCount);
        return this;
    }

    @JsonIgnore
    public AqlQueryOptions.Optimizer getOptimizer() {
        return getOptions().getOptimizer();
    }

    /**
     * @param optimizer Options related to the query optimizer.
     * @return this
     */
    public ExplainAqlQueryOptions optimizer(final AqlQueryOptions.Optimizer optimizer) {
        getOptions().setOptimizer(optimizer);
        return this;
    }

    @JsonIgnore
    public Boolean getProfile() {
        return getOptions().getProfile();
    }

    /**
     * @param profile If set to true, then the additional query profiling information will be returned in the
     *                sub-attribute
     *                profile of the extra return attribute if the query result is not served from the query cache.
     * @return this
     */
    public ExplainAqlQueryOptions profile(final Boolean profile) {
        getOptions().setProfile(profile);
        return this;
    }

    @JsonIgnore
    public Double getSatelliteSyncWait() {
        return getOptions().getSatelliteSyncWait();
    }

    /**
     * @param satelliteSyncWait This parameter allows to configure how long a DBServer will have time to
     *                          bring the
     *                          satellite collections involved in the query into sync. The default value is 60.0
     *                          (seconds). When the
     *                          max time has been reached the query will be stopped.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions satelliteSyncWait(final Double satelliteSyncWait) {
        getOptions().setSatelliteSyncWait(satelliteSyncWait);
        return this;
    }

    @JsonIgnore
    public Collection<String> getShardIds() {
        return getOptions().getShardIds();
    }

    /**
     * Restrict query to shards by given ids. This is an internal option. Use at your own risk.
     *
     * @param shardIds
     * @return this
     */
    public ExplainAqlQueryOptions shardIds(final String... shardIds) {
        getOptions().setShardIds(Arrays.asList(shardIds));
        return this;
    }

    @JsonIgnore
    public Boolean getSkipInaccessibleCollections() {
        return getOptions().getSkipInaccessibleCollections();
    }

    /**
     * @param skipInaccessibleCollections AQL queries (especially graph traversals) will treat collection to which a
     *                                    user has no access rights
     *                                    as if these collections were empty. Instead of returning a forbidden access
     *                                    error, your queries will
     *                                    execute normally. This is intended to help with certain use-cases: A graph
     *                                    contains several
     *                                    collections and different users execute AQL queries on that graph. You can
     *                                    now naturally limit the
     *                                    accessible results by changing the access rights of users on collections.
     * @return this
     * @since ArangoDB 3.2.0
     */
    public ExplainAqlQueryOptions skipInaccessibleCollections(final Boolean skipInaccessibleCollections) {
        getOptions().setSkipInaccessibleCollections(skipInaccessibleCollections);
        return this;
    }

    @JsonIgnore
    public Long getSpillOverThresholdMemoryUsage() {
        return getOptions().getSpillOverThresholdMemoryUsage();
    }

    /**
     * @param spillOverThresholdMemoryUsage This option allows queries to store intermediate and final results
     *                                      temporarily on disk if the amount of memory used (in bytes) exceeds the
     *                                      specified value. This is used for decreasing the memory usage during the
     *                                      query execution.
     *                                      <p/>
     *                                      This option only has an effect on queries that use the SORT operation but
     *                                      without a LIMIT, and if you enable the spillover feature by setting a path
     *                                      for the directory to store the temporary data in with the
     *                                      --temp.intermediate-results-path startup option.
     *                                      <p/>
     *                                      Default value: 128MB.
     *                                      <p/>
     *                                      Spilling data from RAM onto disk is an experimental feature and is turned
     *                                      off by default. The query results are still built up entirely in RAM on
     *                                      Coordinators and single servers for non-streaming queries. To avoid the
     *                                      buildup of the entire query result in RAM, use a streaming query (see the
     *                                      stream option).
     * @return this
     */
    public ExplainAqlQueryOptions spillOverThresholdMemoryUsage(final Long spillOverThresholdMemoryUsage) {
        getOptions().setSpillOverThresholdMemoryUsage(spillOverThresholdMemoryUsage);
        return this;
    }

    @JsonIgnore
    public Long getSpillOverThresholdNumRows() {
        return getOptions().getSpillOverThresholdNumRows();
    }

    /**
     * @param spillOverThresholdNumRows This option allows queries to store intermediate and final results temporarily
     *                                  on disk if the number of rows produced by the query exceeds the specified value.
     *                                  This is used for decreasing the memory usage during the query execution. In a
     *                                  query that iterates over a collection that contains documents, each row is a
     *                                  document, and in a query that iterates over temporary values
     *                                  (i.e. FOR i IN 1..100), each row is one of such temporary values.
     *                                  <p/>
     *                                  This option only has an effect on queries that use the SORT operation but
     *                                  without a LIMIT, and if you enable the spillover feature by setting a path for
     *                                  the directory to store the temporary data in with the
     *                                  --temp.intermediate-results-path startup option.
     *                                  <p/>
     *                                  Default value: 5000000 rows.
     *                                  <p/>
     *                                  Spilling data from RAM onto disk is an experimental feature and is turned off
     *                                  by default. The query results are still built up entirely in RAM on Coordinators
     *                                  and single servers for non-streaming queries. To avoid the buildup of the entire
     *                                  query result in RAM, use a streaming query (see the stream option).
     * @return this
     */
    public ExplainAqlQueryOptions spillOverThresholdNumRows(final Long spillOverThresholdNumRows) {
        getOptions().setSpillOverThresholdNumRows(spillOverThresholdNumRows);
        return this;
    }

    @JsonIgnore
    public Boolean getStream() {
        return getOptions().getStream();
    }

    /**
     * @param stream Specify true and the query will be executed in a streaming fashion. The query result is not
     *               stored on
     *               the server, but calculated on the fly. Beware: long-running queries will need to hold the
     *               collection
     *               locks for as long as the query cursor exists. When set to false a query will be executed right
     *               away in
     *               its entirety. In that case query results are either returned right away (if the resultset is small
     *               enough), or stored on the arangod instance and accessible via the cursor API (with respect to the
     *               ttl). It is advisable to only use this option on short-running queries or without exclusive locks
     *               (write-locks on MMFiles). Please note that the query options cache, count and fullCount will not
     *               work
     *               on streaming queries. Additionally query statistics, warnings and profiling data will only be
     *               available after the query is finished. The default value is false
     * @return this
     * @since ArangoDB 3.4.0
     */
    public ExplainAqlQueryOptions stream(final Boolean stream) {
        getOptions().setStream(stream);
        return this;
    }

    @JsonIgnore
    public Collection<String> getRules() {
        return getOptions().getOptimizer().getRules();
    }

    /**
     * @param rules A list of to-be-included or to-be-excluded optimizer rules can be put into this attribute,
     *              telling the
     *              optimizer to include or exclude specific rules. To disable a rule, prefix its name with a -, to
     *              enable
     *              a rule, prefix it with a +. There is also a pseudo-rule all, which will match all optimizer rules
     * @return this
     */
    public ExplainAqlQueryOptions rules(final Collection<String> rules) {
        getOptions().getOptimizer().setRules(rules);
        return this;
    }

}

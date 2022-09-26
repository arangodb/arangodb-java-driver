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

import com.arangodb.entity.IndexType;
import com.arangodb.entity.InvertedIndexField;
import com.arangodb.entity.InvertedIndexPrimarySort;
import com.arangodb.entity.arangosearch.*;

import java.util.*;

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-inverted.html">API Documentation</a>
 * @since ArangoDB 3.10
 */
public class InvertedIndexOptions extends IndexOptions<InvertedIndexOptions> {

    protected final IndexType type = IndexType.inverted;
    private Integer parallelism;
    private InvertedIndexPrimarySort primarySort;
    private final Collection<StoredValue> storedValues = new ArrayList<>();
    private String analyzer;
    private final Set<AnalyzerFeature> features = new HashSet<>();
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private Boolean searchField;
    private final Collection<InvertedIndexField> fields = new ArrayList<>();
    private Long consolidationIntervalMsec;
    private Long commitIntervalMsec;
    private Long cleanupIntervalStep;
    private ConsolidationPolicy consolidationPolicy;
    private Long writebufferIdle;
    private Long writebufferActive;
    private Long writebufferSizeMax;

    public InvertedIndexOptions() {
        super();
    }

    @Override
    protected InvertedIndexOptions getThis() {
        return this;
    }

    protected IndexType getType() {
        return type;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    /**
     * @param parallelism The number of threads to use for indexing the fields. Default: 2
     * @return this
     */
    public InvertedIndexOptions parallelism(Integer parallelism) {
        this.parallelism = parallelism;
        return this;
    }

    public InvertedIndexPrimarySort getPrimarySort() {
        return primarySort;
    }

    /**
     * @param primarySort You can define a primary sort order to enable an AQL optimization. If a query iterates over
     *                    all documents of a collection, wants to sort them by attribute values, and the (left-most)
     *                    fields to sort by, as well as their sorting direction, match with the primarySort definition,
     *                    then the SORT operation is optimized away.
     * @return this
     */
    public InvertedIndexOptions primarySort(InvertedIndexPrimarySort primarySort) {
        this.primarySort = primarySort;
        return this;
    }

    public Collection<StoredValue> getStoredValues() {
        return storedValues;
    }

    /**
     * @param storedValues The optional storedValues attribute can contain an array of paths to additional attributes to
     *                     store in the index. These additional attributes cannot be used for index lookups or for
     *                     sorting, but they can be used for projections. This allows an index to fully cover more
     *                     queries and avoid extra document lookups.
     * @return this
     */
    public InvertedIndexOptions storedValues(StoredValue... storedValues) {
        Collections.addAll(this.storedValues, storedValues);
        return this;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    /**
     * @param analyzer The name of an Analyzer to use by default. This Analyzer is applied to the values of the indexed
     *                 fields for which you don’t define Analyzers explicitly.
     * @return this
     */
    public InvertedIndexOptions analyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public Set<AnalyzerFeature> getFeatures() {
        return features;
    }

    /**
     * @param features A list of Analyzer features to use by default. They define what features are enabled for the
     *                 default analyzer.
     * @return this
     */
    public InvertedIndexOptions features(AnalyzerFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public Boolean getIncludeAllFields() {
        return includeAllFields;
    }

    /**
     * @param includeAllFields This option only applies if you use the inverted index in a search-alias Views. If set to
     *                         true, then all sub-attributes of this field are indexed, excluding any sub-attributes
     *                         that are configured separately by other elements in the fields array (and their
     *                         sub-attributes). The analyzer and features properties apply to the sub-attributes. If set
     *                         to false, then sub-attributes are ignored. The default value is defined by the top-level
     *                         includeAllFields option, or false if not set.
     * @return this
     */
    public InvertedIndexOptions includeAllFields(Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    public Boolean getTrackListPositions() {
        return trackListPositions;
    }

    /**
     * @param trackListPositions This option only applies if you use the inverted index in a search-alias Views. If set
     *                           to true, then track the value position in arrays for array values. For example, when
     *                           querying a document like { attr: [ "valueX", "valueY", "valueZ" ] }, you need to
     *                           specify the array element, e.g. doc.attr[1] == "valueY". If set to false, all values in
     *                           an array are treated as equal alternatives. You don’t specify an array element in
     *                           queries, e.g. doc.attr == "valueY", and all elements are searched for a match. Default:
     *                           the value defined by the top-level trackListPositions option, or false if not set.
     * @return this
     */
    public InvertedIndexOptions trackListPositions(Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    public Boolean getSearchField() {
        return searchField;
    }

    /**
     * @param searchField This option only applies if you use the inverted index in a search-alias Views. You can set
     *                    the option to true to get the same behavior as with arangosearch Views regarding the indexing
     *                    of array values as the default. If enabled, both, array and primitive values (strings,
     *                    numbers, etc.) are accepted. Every element of an array is indexed according to the
     *                    trackListPositions option. If set to false, it depends on the attribute path. If it explicitly
     *                    expand an array ([*]), then the elements are indexed separately. Otherwise, the array is
     *                    indexed as a whole, but only geopoint and aql Analyzers accept array inputs. You cannot use an
     *                    array expansion if searchField is enabled.
     * @return this
     */
    public InvertedIndexOptions searchField(Boolean searchField) {
        this.searchField = searchField;
        return this;
    }

    public Collection<InvertedIndexField> getFields() {
        return fields;
    }

    /**
     * @param fields An array of attribute paths as strings to index the fields with the default options, or objects to
     *               specify options for the fields.
     * @return this
     */
    public InvertedIndexOptions fields(InvertedIndexField... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    /**
     * @param consolidationIntervalMsec Wait at least this many milliseconds between applying ‘consolidationPolicy’ to
     *                                  consolidate View data store and possibly release space on the filesystem
     *                                  (default: 1000, to disable use: 0). For the case where there are a lot of data
     *                                  modification operations, a higher value could potentially have the data store
     *                                  consume more space and file handles. For the case where there are a few data
     *                                  modification operations, a lower value will impact performance due to no segment
     *                                  candidates available for consolidation. Background: For data modification
     *                                  ArangoSearch Views follow the concept of a “versioned data store”. Thus old
     *                                  versions of data may be removed once there are no longer any users of the old
     *                                  data. The frequency of the cleanup and compaction operations are governed by
     *                                  ‘consolidationIntervalMsec’ and the candidates for compaction are selected via
     *                                  ‘consolidationPolicy’.
     * @return this
     */
    public InvertedIndexOptions consolidationIntervalMsec(Long consolidationIntervalMsec) {
        this.consolidationIntervalMsec = consolidationIntervalMsec;
        return this;
    }

    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    /**
     * @param commitIntervalMsec Wait at least this many milliseconds between committing View data store changes and
     *                           making documents visible to queries (default: 1000, to disable use: 0). For the case
     *                           where there are a lot of inserts/updates, a lower value, until commit, will cause the
     *                           index not to account for them and memory usage would continue to grow. For the case
     *                           where there are a few inserts/updates, a higher value will impact performance and waste
     *                           disk space for each commit call without any added benefits. Background: For data
     *                           retrieval ArangoSearch Views follow the concept of “eventually-consistent”, i.e.
     *                           eventually all the data in ArangoDB will be matched by corresponding query expressions.
     *                           The concept of ArangoSearch View “commit” operation is introduced to control the
     *                           upper-bound on the time until document addition/removals are actually reflected by
     *                           corresponding query expressions. Once a “commit” operation is complete all documents
     *                           added/removed prior to the start of the “commit” operation will be reflected by queries
     *                           invoked in subsequent ArangoDB transactions, in-progress ArangoDB transactions will
     *                           still continue to return a repeatable-read state.
     * @return this
     */
    public InvertedIndexOptions commitIntervalMsec(Long commitIntervalMsec) {
        this.commitIntervalMsec = commitIntervalMsec;
        return this;
    }

    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    /**
     * @param cleanupIntervalStep Wait at least this many commits between removing unused files in the ArangoSearch data
     *                            directory (default: 2, to disable use: 0). For the case where the consolidation
     *                            policies merge segments often (i.e. a lot of commit+consolidate), a lower value will
     *                            cause a lot of disk space to be wasted. For the case where the consolidation policies
     *                            rarely merge segments (i.e. few inserts/deletes), a higher value will impact
     *                            performance without any added benefits. Background: With every “commit” or
     *                            “consolidate” operation a new state of the View internal data-structures is created on
     *                            disk. Old states/snapshots are released once there are no longer any users remaining.
     *                            However, the files for the released states/snapshots are left on disk, and only
     *                            removed by “cleanup” operation.
     * @return this
     */
    public InvertedIndexOptions cleanupIntervalStep(Long cleanupIntervalStep) {
        this.cleanupIntervalStep = cleanupIntervalStep;
        return this;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    /**
     * @param consolidationPolicy The consolidation policy to apply for selecting which segments should be merged
     *                            (default: {}). Background: With each ArangoDB transaction that inserts documents one
     *                            or more ArangoSearch internal segments gets created. Similarly for removed documents
     *                            the segments that contain such documents will have these documents marked as
     *                            ‘deleted’. Over time this approach causes a lot of small and sparse segments to be
     *                            created. A “consolidation” operation selects one or more segments and copies all of
     *                            their valid documents into a single new segment, thereby allowing the search algorithm
     *                            to perform more optimally and for extra file handles to be released once old segments
     *                            are no longer used.
     * @return this
     */
    public InvertedIndexOptions consolidationPolicy(ConsolidationPolicy consolidationPolicy) {
        this.consolidationPolicy = consolidationPolicy;
        return this;
    }

    public Long getWritebufferIdle() {
        return writebufferIdle;
    }

    /**
     * @param writebufferIdle Maximum number of writers (segments) cached in the pool (default: 64, use 0 to disable)
     * @return this
     */
    public InvertedIndexOptions writebufferIdle(Long writebufferIdle) {
        this.writebufferIdle = writebufferIdle;
        return this;
    }

    public Long getWritebufferActive() {
        return writebufferActive;
    }

    /**
     * @param writebufferActive Maximum number of concurrent active writers (segments) that perform a transaction. Other
     *                          writers (segments) wait till current active writers (segments) finish (default: 0, use 0
     *                          to disable)
     * @return this
     */
    public InvertedIndexOptions writebufferActive(Long writebufferActive) {
        this.writebufferActive = writebufferActive;
        return this;
    }

    public Long getWritebufferSizeMax() {
        return writebufferSizeMax;
    }

    /**
     * @param writebufferSizeMax Maximum memory byte size per writer (segment) before a writer (segment) flush is
     *                           triggered. 0 value turns off this limit for any writer (buffer) and data will be
     *                           flushed periodically based on the value defined for the flush thread (ArangoDB server
     *                           startup option). 0 value should be used carefully due to high potential memory
     *                           consumption (default: 33554432, use 0 to disable)
     * @return this
     */
    public InvertedIndexOptions writebufferSizeMax(Long writebufferSizeMax) {
        this.writebufferSizeMax = writebufferSizeMax;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvertedIndexOptions that = (InvertedIndexOptions) o;
        return type == that.type && Objects.equals(parallelism, that.parallelism) && Objects.equals(primarySort, that.primarySort) && Objects.equals(storedValues, that.storedValues) && Objects.equals(analyzer, that.analyzer) && Objects.equals(features, that.features) && Objects.equals(includeAllFields, that.includeAllFields) && Objects.equals(trackListPositions, that.trackListPositions) && Objects.equals(searchField, that.searchField) && Objects.equals(fields, that.fields) && Objects.equals(consolidationIntervalMsec, that.consolidationIntervalMsec) && Objects.equals(commitIntervalMsec, that.commitIntervalMsec) && Objects.equals(cleanupIntervalStep, that.cleanupIntervalStep) && Objects.equals(consolidationPolicy, that.consolidationPolicy) && Objects.equals(writebufferIdle, that.writebufferIdle) && Objects.equals(writebufferActive, that.writebufferActive) && Objects.equals(writebufferSizeMax, that.writebufferSizeMax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parallelism, primarySort, storedValues, analyzer, features, includeAllFields, trackListPositions, searchField, fields, consolidationIntervalMsec, commitIntervalMsec, cleanupIntervalStep, consolidationPolicy, writebufferIdle, writebufferActive, writebufferSizeMax);
    }
}

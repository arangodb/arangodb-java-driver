/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.entity.arangosearch;

import com.arangodb.entity.ViewEntity;
import com.arangodb.internal.serde.InternalDeserializers;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html">API Documentation</a>
 */
public final class ArangoSearchPropertiesEntity extends ViewEntity {

    private Long consolidationIntervalMsec;
    private Long commitIntervalMsec;
    private Long cleanupIntervalStep;
    private ConsolidationPolicy consolidationPolicy;
    private Collection<PrimarySort> primarySort;
    private Collection<CollectionLink> links;
    private ArangoSearchCompression primarySortCompression;
    private Collection<StoredValue> storedValues;
    private Collection<String> optimizeTopK;
    private Boolean primarySortCache;
    private Boolean primaryKeyCache;

    /**
     * @return Wait at least this many milliseconds between committing view data store changes and making documents
     * visible to queries (default: 1000, to disable use: 0). For the case where there are a lot of inserts/updates, a
     * lower value, until commit, will cause the index not to account for them and memory usage would continue to grow.
     * For the case where there are a few inserts/updates, a higher value will impact performance and waste disk space
     * for each commit call without any added benefits. Background: For data retrieval ArangoSearch views follow the
     * concept of “eventually-consistent”, i.e. eventually all the data in ArangoDB will be matched by corresponding
     * query expressions. The concept of ArangoSearch view “commit” operation is introduced to control the upper-bound
     * on the time until document addition/removals are actually reflected by corresponding query expressions. Once a
     * “commit” operation is complete all documents added/removed prior to the start of the “commit” operation will be
     * reflected by queries invoked in subsequent ArangoDB transactions, in-progress ArangoDB transactions will still
     * continue to return a repeatable-read state.
     */
    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    /**
     * @return Wait at least this many milliseconds between committing index data changes and making them visible to
     * queries (default: 60000, to disable use: 0). For the case where there are a lot of inserts/updates, a
     * lower value, until commit, will cause the index not to account for them and memory usage would continue
     * to grow. For the case where there are a few inserts/updates, a higher value will impact performance and
     * waste disk space for each commit call without any added benefits.
     */
    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    /**
     * @return Wait at least this many commits between removing unused files in data directory (default: 10, to disable
     * use: 0). For the case where the consolidation policies merge segments often (i.e. a lot of
     * commit+consolidate), a lower value will cause a lot of disk space to be wasted. For the case where the
     * consolidation policies rarely merge segments (i.e. few inserts/deletes), a higher value will impact
     * performance without any added benefits.
     */
    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    /**
     * @return A list of linked collections
     */
    @JsonDeserialize(using = InternalDeserializers.CollectionLinksDeserializer.class)
    public Collection<CollectionLink> getLinks() {
        return links;
    }

    /**
     * @return A list of primary sort objects
     */
    public Collection<PrimarySort> getPrimarySort() {
        return primarySort;
    }

    /**
     * @return Defines how to compress the primary sort data (introduced in v3.7.0). ArangoDB v3.5 and v3.6 always
     * compress the index using LZ4.
     * @since ArangoDB 3.7
     */
    public ArangoSearchCompression getPrimarySortCompression() {
        return primarySortCompression;
    }

    /**
     * @return An array of objects to describe which document attributes to store in the View index. It can then cover
     * search queries, which means the data can be taken from the index directly and accessing the storage engine can be
     * avoided.
     * @since ArangoDB 3.7
     */
    public Collection<StoredValue> getStoredValues() {
        return storedValues;
    }

    /**
     * @return An array of strings defining optimized sort expressions.
     * @since ArangoDB 3.11, Enterprise Edition only
     */
    public Collection<String> getOptimizeTopK() {
        return optimizeTopK;
    }

    public Boolean getPrimarySortCache() {
        return primarySortCache;
    }

    public Boolean getPrimaryKeyCache() {
        return primaryKeyCache;
    }
}

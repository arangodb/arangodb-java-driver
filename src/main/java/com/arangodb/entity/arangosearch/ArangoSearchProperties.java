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

import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html">API Documentation</a>
 */
public class ArangoSearchProperties {

    private Long consolidationIntervalMsec;
    private Long commitIntervalMsec;
    private Long cleanupIntervalStep;
    private ConsolidationPolicy consolidationPolicy;
    private final Collection<PrimarySort> primarySorts;
    private final Collection<CollectionLink> links;
    private ArangoSearchCompression primarySortCompression;
    private final Collection<StoredValue> storedValues;
    private Boolean primarySortCache;
    private Boolean primaryKeyCache;

    public ArangoSearchProperties() {
        super();
        links = new ArrayList<>();
        primarySorts = new ArrayList<>();
        storedValues = new ArrayList<>();
    }

    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    public void setCommitIntervalMsec(final Long commitIntervalMsec) {
        this.commitIntervalMsec = commitIntervalMsec;
    }

    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    public void setConsolidationIntervalMsec(final Long consolidationIntervalMsec) {
        this.consolidationIntervalMsec = consolidationIntervalMsec;
    }

    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    public void setCleanupIntervalStep(final Long cleanupIntervalStep) {
        this.cleanupIntervalStep = cleanupIntervalStep;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    public void setConsolidationPolicy(final ConsolidationPolicy consolidationPolicy) {
        this.consolidationPolicy = consolidationPolicy;
    }

    public Collection<CollectionLink> getLinks() {
        return links;
    }

    public void addLink(final CollectionLink... links) {
        this.links.addAll(Arrays.asList(links));
    }

    public Collection<PrimarySort> getPrimarySort() {
        return primarySorts;
    }

    public void addPrimarySort(final PrimarySort... primarySorts) {
        this.primarySorts.addAll(Arrays.asList(primarySorts));
    }

    /**
     * @return Defines how to compress the primary sort data (introduced in v3.7.0). ArangoDB v3.5 and v3.6 always
     * compress the index using LZ4.
     * @since ArangoDB 3.7
     */
    public ArangoSearchCompression getPrimarySortCompression() {
        return primarySortCompression;
    }

    public void setPrimarySortCompression(ArangoSearchCompression primarySortCompression) {
        this.primarySortCompression = primarySortCompression;
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

    public void addStoredValues(final StoredValue... storedValues) {
        this.storedValues.addAll(Arrays.asList(storedValues));
    }

    public Boolean getPrimarySortCache() {
        return primarySortCache;
    }


    /**
     * @param primarySortCache If you enable this option, then the primary sort columns are always cached in memory.
     *                         This can improve the performance of queries that utilize the primary sort order.
     *                         Otherwise, these values are memory-mapped and it is up to the operating system to load
     *                         them from disk into memory and to evict them from memory.
     * @since ArangoDB 3.9.6, Enterprise Edition only
     */
    public void setPrimarySortCache(final Boolean primarySortCache) {
        this.primarySortCache = primarySortCache;
    }

    public Boolean getPrimaryKeyCache() {
        return primaryKeyCache;
    }

    /**
     * @param primaryKeyCache If you enable this option, then the primary key columns are always cached in memory. This
     *                        can improve the performance of queries that return many documents. Otherwise, these values
     *                        are memory-mapped and it is up to the operating system to load them from disk into memory
     *                        and to evict them from memory.
     * @since ArangoDB 3.9.6, Enterprise Edition only
     */
    public void setPrimaryKeyCache(final Boolean primaryKeyCache) {
        this.primaryKeyCache = primaryKeyCache;
    }

}

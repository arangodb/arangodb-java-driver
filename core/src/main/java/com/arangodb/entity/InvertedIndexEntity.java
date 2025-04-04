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

package com.arangodb.entity;

import com.arangodb.entity.arangosearch.AnalyzerFeature;
import com.arangodb.entity.arangosearch.ConsolidationPolicy;
import com.arangodb.entity.arangosearch.StoredValue;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.10
 */
public final class InvertedIndexEntity {

    private String id;
    private Boolean isNewlyCreated;
    private Boolean unique;
    private Boolean sparse;
    private Long version;
    private Integer code;
    private IndexType type;
    private String name;
    private Collection<InvertedIndexField> fields;
    private Boolean searchField;
    private Collection<StoredValue> storedValues;
    private Collection<String> optimizeTopK;
    private InvertedIndexPrimarySort primarySort;
    private String analyzer;
    private Set<AnalyzerFeature> features;
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private Long cleanupIntervalStep;
    private Long commitIntervalMsec;
    private Long consolidationIntervalMsec;
    private ConsolidationPolicy consolidationPolicy;
    private Long writebufferIdle;
    private Long writebufferActive;
    private Long writebufferSizeMax;
    private Boolean cache;
    private Boolean primaryKeyCache;

    public String getId() {
        return id;
    }

    public Boolean getIsNewlyCreated() {
        return isNewlyCreated;
    }

    public Boolean getUnique() {
        return unique;
    }

    public Boolean getSparse() {
        return sparse;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getCode() {
        return code;
    }

    public IndexType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Collection<InvertedIndexField> getFields() {
        return fields;
    }

    public Boolean getSearchField() {
        return searchField;
    }

    public Collection<StoredValue> getStoredValues() {
        return storedValues;
    }

    public Collection<String> getOptimizeTopK() {
        return optimizeTopK;
    }

    public InvertedIndexPrimarySort getPrimarySort() {
        return primarySort;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public Set<AnalyzerFeature> getFeatures() {
        return features;
    }

    public Boolean getIncludeAllFields() {
        return includeAllFields;
    }

    public Boolean getTrackListPositions() {
        return trackListPositions;
    }

    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    public Long getWritebufferIdle() {
        return writebufferIdle;
    }

    public Long getWritebufferActive() {
        return writebufferActive;
    }

    public Long getWritebufferSizeMax() {
        return writebufferSizeMax;
    }

    public Boolean getCache() {
        return cache;
    }

    public Boolean getPrimaryKeyCache() {
        return primaryKeyCache;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InvertedIndexEntity)) return false;
        InvertedIndexEntity that = (InvertedIndexEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(isNewlyCreated, that.isNewlyCreated) && Objects.equals(unique, that.unique) && Objects.equals(sparse, that.sparse) && Objects.equals(version, that.version) && Objects.equals(code, that.code) && type == that.type && Objects.equals(name, that.name) && Objects.equals(fields, that.fields) && Objects.equals(searchField, that.searchField) && Objects.equals(storedValues, that.storedValues) && Objects.equals(optimizeTopK, that.optimizeTopK) && Objects.equals(primarySort, that.primarySort) && Objects.equals(analyzer, that.analyzer) && Objects.equals(features, that.features) && Objects.equals(includeAllFields, that.includeAllFields) && Objects.equals(trackListPositions, that.trackListPositions) && Objects.equals(cleanupIntervalStep, that.cleanupIntervalStep) && Objects.equals(commitIntervalMsec, that.commitIntervalMsec) && Objects.equals(consolidationIntervalMsec, that.consolidationIntervalMsec) && Objects.equals(consolidationPolicy, that.consolidationPolicy) && Objects.equals(writebufferIdle, that.writebufferIdle) && Objects.equals(writebufferActive, that.writebufferActive) && Objects.equals(writebufferSizeMax, that.writebufferSizeMax) && Objects.equals(cache, that.cache) && Objects.equals(primaryKeyCache, that.primaryKeyCache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, isNewlyCreated, unique, sparse, version, code, type, name, fields, searchField, storedValues, optimizeTopK, primarySort, analyzer, features, includeAllFields, trackListPositions, cleanupIntervalStep, commitIntervalMsec, consolidationIntervalMsec, consolidationPolicy, writebufferIdle, writebufferActive, writebufferSizeMax, cache, primaryKeyCache);
    }
}

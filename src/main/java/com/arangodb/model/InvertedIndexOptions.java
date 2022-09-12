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
import com.arangodb.entity.arangosearch.*;

import java.util.*;

/**
 * TODO: add documentation
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-inverted.html">API Documentation</a>
 * @since ArangoDB 3.10
 */
public class InvertedIndexOptions extends IndexOptions<InvertedIndexOptions> {

    protected final IndexType type = IndexType.inverted;
    private Integer parallelism;
    private PrimarySort primarySort;
    private final Collection<StoredValue> storedValues = new ArrayList<>();
    private String analyzer;
    private final Collection<AnalyzerFeature> features = new ArrayList<>();
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

    public InvertedIndexOptions parallelism(Integer parallelism) {
        this.parallelism = parallelism;
        return this;
    }

    public PrimarySort getPrimarySort() {
        return primarySort;
    }

    public InvertedIndexOptions primarySort(PrimarySort primarySort) {
        this.primarySort = primarySort;
        return this;
    }

    public Collection<StoredValue> getStoredValues() {
        return storedValues;
    }

    public InvertedIndexOptions storedValues(StoredValue... storedValues) {
        Collections.addAll(this.storedValues, storedValues);
        return this;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public InvertedIndexOptions analyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }

    public Collection<AnalyzerFeature> getFeatures() {
        return features;
    }

    public InvertedIndexOptions features(AnalyzerFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public Boolean getIncludeAllFields() {
        return includeAllFields;
    }

    public InvertedIndexOptions includeAllFields(Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    public Boolean getTrackListPositions() {
        return trackListPositions;
    }

    public InvertedIndexOptions trackListPositions(Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    public Boolean getSearchField() {
        return searchField;
    }

    public InvertedIndexOptions searchField(Boolean searchField) {
        this.searchField = searchField;
        return this;
    }

    public Collection<InvertedIndexField> getFields() {
        return fields;
    }

    public InvertedIndexOptions fields(InvertedIndexField... fields) {
        Collections.addAll(this.fields, fields);
        return this;
    }

    public Long getConsolidationIntervalMsec() {
        return consolidationIntervalMsec;
    }

    public InvertedIndexOptions consolidationIntervalMsec(Long consolidationIntervalMsec) {
        this.consolidationIntervalMsec = consolidationIntervalMsec;
        return this;
    }

    public Long getCommitIntervalMsec() {
        return commitIntervalMsec;
    }

    public InvertedIndexOptions commitIntervalMsec(Long commitIntervalMsec) {
        this.commitIntervalMsec = commitIntervalMsec;
        return this;
    }

    public Long getCleanupIntervalStep() {
        return cleanupIntervalStep;
    }

    public InvertedIndexOptions cleanupIntervalStep(Long cleanupIntervalStep) {
        this.cleanupIntervalStep = cleanupIntervalStep;
        return this;
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return consolidationPolicy;
    }

    public InvertedIndexOptions consolidationPolicy(ConsolidationPolicy consolidationPolicy) {
        this.consolidationPolicy = consolidationPolicy;
        return this;
    }

    public Long getWritebufferIdle() {
        return writebufferIdle;
    }

    public InvertedIndexOptions writebufferIdle(Long writebufferIdle) {
        this.writebufferIdle = writebufferIdle;
        return this;
    }

    public Long getWritebufferActive() {
        return writebufferActive;
    }

    public InvertedIndexOptions writebufferActive(Long writebufferActive) {
        this.writebufferActive = writebufferActive;
        return this;
    }

    public Long getWritebufferSizeMax() {
        return writebufferSizeMax;
    }

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

    public static class PrimarySort {
        private final List<Field> fields = new ArrayList<>();
        private ArangoSearchCompression compression;

        public List<Field> getFields() {
            return fields;
        }

        public PrimarySort fields(Field... fields) {
            Collections.addAll(this.fields, fields);
            return this;
        }

        public ArangoSearchCompression getCompression() {
            return compression;
        }

        public PrimarySort compression(ArangoSearchCompression compression) {
            this.compression = compression;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrimarySort that = (PrimarySort) o;
            return Objects.equals(fields, that.fields) && compression == that.compression;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fields, compression);
        }

        public static class Field {
            private final String field;
            private final Direction direction;

            public Field(String field, Direction direction) {
                this.field = field;
                this.direction = direction;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Field field1 = (Field) o;
                return Objects.equals(field, field1.field) && direction == field1.direction;
            }

            @Override
            public int hashCode() {
                return Objects.hash(field, direction);
            }

            public enum Direction {
                asc,
                desc
            }

        }

    }

    public static class InvertedIndexField {
        private String name;
        private String analyzer;
        private Boolean includeAllFields;
        private Boolean searchField;
        private Boolean trackListPositions;
        private final Collection<AnalyzerFeature> features = new ArrayList<>();
        private final Collection<InvertedIndexField> nested = new ArrayList<>();

        public String getName() {
            return name;
        }

        public InvertedIndexField name(String name) {
            this.name = name;
            return this;
        }

        public String getAnalyzer() {
            return analyzer;
        }

        public InvertedIndexField analyzer(String analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public Boolean getIncludeAllFields() {
            return includeAllFields;
        }

        public InvertedIndexField includeAllFields(Boolean includeAllFields) {
            this.includeAllFields = includeAllFields;
            return this;
        }

        public Boolean getSearchField() {
            return searchField;
        }

        public InvertedIndexField searchField(Boolean searchField) {
            this.searchField = searchField;
            return this;
        }

        public Boolean getTrackListPositions() {
            return trackListPositions;
        }

        public InvertedIndexField trackListPositions(Boolean trackListPositions) {
            this.trackListPositions = trackListPositions;
            return this;
        }

        public Collection<AnalyzerFeature> getFeatures() {
            return features;
        }

        public InvertedIndexField features(AnalyzerFeature... features) {
            Collections.addAll(this.features, features);
            return this;
        }

        public Collection<InvertedIndexField> getNested() {
            return nested;
        }

        public InvertedIndexField nested(InvertedIndexField... nested) {
            Collections.addAll(this.nested, nested);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InvertedIndexField that = (InvertedIndexField) o;
            return Objects.equals(name, that.name) && Objects.equals(analyzer, that.analyzer) && Objects.equals(includeAllFields, that.includeAllFields) && Objects.equals(searchField, that.searchField) && Objects.equals(trackListPositions, that.trackListPositions) && Objects.equals(features, that.features) && Objects.equals(nested, that.nested);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, analyzer, includeAllFields, searchField, trackListPositions, features, nested);
        }
    }

}

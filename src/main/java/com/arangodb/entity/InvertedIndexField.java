package com.arangodb.entity;

import com.arangodb.entity.arangosearch.AnalyzerFeature;

import java.util.*;

/**
 * TODO: add documentation
 *
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-inverted.html">API Documentation</a>
 * @since ArangoDB 3.10
 */
public class InvertedIndexField {
    private String name;
    private String analyzer;
    private Boolean includeAllFields;
    private Boolean searchField;
    private Boolean trackListPositions;
    private final Set<AnalyzerFeature> features = new HashSet<>();
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

    public Set<AnalyzerFeature> getFeatures() {
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

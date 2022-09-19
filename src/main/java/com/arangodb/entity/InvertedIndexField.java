package com.arangodb.entity;

import com.arangodb.entity.arangosearch.AnalyzerFeature;

import java.util.*;

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-inverted.html">API Documentation</a>
 * @since ArangoDB 3.10
 */
public class InvertedIndexField implements Entity {
    private String name;
    private String analyzer;
    private Boolean includeAllFields;
    private Boolean searchField;
    private Boolean trackListPositions;
    private final Set<AnalyzerFeature> features = new HashSet<>();
    private Collection<InvertedIndexField> nested;

    public String getName() {
        return name;
    }

    /**
     * @param name An attribute path. The . character denotes sub-attributes.
     * @return this
     */
    public InvertedIndexField name(String name) {
        this.name = name;
        return this;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    /**
     * @param analyzer The name of an Analyzer to use for this field. Default: the value defined by the top-level
     *                 analyzer option, or if not set, the default identity Analyzer.
     * @return this
     */
    public InvertedIndexField analyzer(String analyzer) {
        this.analyzer = analyzer;
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
    public InvertedIndexField includeAllFields(Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    public Boolean getSearchField() {
        return searchField;
    }

    /**
     * @param searchField This option only applies if you use the inverted index in a search-alias Views. You can set
     *                    the option to true to get the same behavior as with arangosearch Views regarding the indexing
     *                    of array values for this field. If enabled, both, array and primitive values (strings,
     *                    numbers, etc.) are accepted. Every element of an array is indexed according to the
     *                    trackListPositions option. If set to false, it depends on the attribute path. If it explicitly
     *                    expand an array ([*]), then the elements are indexed separately. Otherwise, the array is
     *                    indexed as a whole, but only geopoint and aql Analyzers accept array inputs. You cannot use an
     *                    array expansion if searchField is enabled. Default: the value defined by the top-level
     *                    searchField option, or false if not set.
     * @return this
     */
    public InvertedIndexField searchField(Boolean searchField) {
        this.searchField = searchField;
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
    public InvertedIndexField trackListPositions(Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    public Set<AnalyzerFeature> getFeatures() {
        return features;
    }

    /**
     * @param features A list of Analyzer features to use for this field. They define what features are enabled for the
     *                 analyzer.
     * @return this
     */
    public InvertedIndexField features(AnalyzerFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public Collection<InvertedIndexField> getNested() {
        return nested;
    }

    /**
     * @param nested Index the specified sub-objects that are stored in an array. Other than with the fields property,
     *               the values get indexed in a way that lets you query for co-occurring values. For example, you can
     *               search the sub-objects and all the conditions need to be met by a single sub-object instead of
     *               across all of them. This property is available in the Enterprise Edition only.
     * @return this
     */
    public InvertedIndexField nested(InvertedIndexField... nested) {
        if (this.nested == null) this.nested = new ArrayList<>();
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

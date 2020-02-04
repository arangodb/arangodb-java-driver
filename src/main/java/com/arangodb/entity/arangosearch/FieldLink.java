package com.arangodb.entity.arangosearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FieldLink {

    private final String name;
    private final Collection<String> analyzers;
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private StoreValuesType storeValues;
    private final Collection<FieldLink> fields;

    private FieldLink(final String name) {
        super();
        this.name = name;
        fields = new ArrayList<>();
        analyzers = new ArrayList<>();
    }

    /**
     * Creates an instance of {@code FieldLink} on the given field name
     *
     * @param name Name of a field
     * @return new instance of {@code FieldLink}
     */
    public static FieldLink on(final String name) {
        return new FieldLink(name);
    }

    /**
     * @param analyzers The list of analyzers to be used for indexing of string values (default: ["identity"]).
     * @return link
     */
    public FieldLink analyzers(final String... analyzers) {
        this.analyzers.addAll(Arrays.asList(analyzers));
        return this;
    }

    /**
     * @param includeAllFields The flag determines whether or not to index all fields on a particular level of depth (default:
     *                         false).
     * @return link
     */
    public FieldLink includeAllFields(final Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    /**
     * @param trackListPositions The flag determines whether or not values in a lists should be treated separate (default: false).
     * @return link
     */
    public FieldLink trackListPositions(final Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    /**
     * @param storeValues How should the view track the attribute values, this setting allows for additional value retrieval
     *                    optimizations (default "none").
     * @return link
     */
    public FieldLink storeValues(final StoreValuesType storeValues) {
        this.storeValues = storeValues;
        return this;
    }

    /**
     * @param fields A list of linked fields
     * @return link
     */
    public FieldLink fields(final FieldLink... fields) {
        this.fields.addAll(Arrays.asList(fields));
        return this;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getAnalyzers() {
        return analyzers;
    }

    public Boolean getIncludeAllFields() {
        return includeAllFields;
    }

    public Boolean getTrackListPositions() {
        return trackListPositions;
    }

    public StoreValuesType getStoreValues() {
        return storeValues;
    }

    public Collection<FieldLink> getFields() {
        return fields;
    }

}
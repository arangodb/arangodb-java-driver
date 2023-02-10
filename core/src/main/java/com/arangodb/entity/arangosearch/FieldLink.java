package com.arangodb.entity.arangosearch;

import com.arangodb.internal.serde.InternalDeserializers;
import com.arangodb.internal.serde.InternalSerializers;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;
import java.util.Collection;

public final class FieldLink {

    private final String name;
    private Collection<String> analyzers;
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private StoreValuesType storeValues;
    private Collection<FieldLink> fields;
    private Collection<FieldLink> nested;
    private Boolean inBackground;

    private FieldLink(final String name) {
        super();
        this.name = name;
    }

    /**
     * Creates an instance of {@code FieldLink} on the given field name
     *
     * @param name Name of a field
     * @return new instance of {@code FieldLink}
     */
    @JsonCreator
    public static FieldLink on(@JsonProperty("name") final String name) {
        return new FieldLink(name);
    }

    /**
     * @param analyzers The list of analyzers to be used for indexing of string values (default: ["identity"]).
     * @return link
     */
    public FieldLink analyzers(final String... analyzers) {
        this.analyzers = Arrays.asList(analyzers);
        return this;
    }

    /**
     * @param includeAllFields The flag determines whether or not to index all fields on a particular level of depth
     *                         (default:
     *                         false).
     * @return link
     */
    public FieldLink includeAllFields(final Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    /**
     * @param trackListPositions The flag determines whether or not values in a lists should be treated separate
     *                           (default: false).
     * @return link
     */
    public FieldLink trackListPositions(final Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    /**
     * @param storeValues How should the view track the attribute values, this setting allows for additional value
     *                    retrieval
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
    @JsonDeserialize(using = InternalDeserializers.FieldLinksDeserializer.class)
    public FieldLink fields(final FieldLink... fields) {
        this.fields = Arrays.asList(fields);
        return this;
    }

    /**
     * @param nested A list of nested fields
     * @return link
     * @since ArangoDB 3.10
     */
    @JsonDeserialize(using = InternalDeserializers.FieldLinksDeserializer.class)
    public FieldLink nested(final FieldLink... nested) {
        this.nested = Arrays.asList(nested);
        return this;
    }

    /**
     * @param inBackground If set to true, then no exclusive lock is used on the source collection during View index
     *                     creation, so that it remains basically available. inBackground is an option that can be set
     *                     when adding links. It does not get persisted as it is not a View property, but only a
     *                     one-off option. (default: false)
     * @return link
     */
    public FieldLink inBackground(final Boolean inBackground) {
        this.inBackground = inBackground;
        return this;
    }

    @JsonIgnore
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

    @JsonSerialize(using = InternalSerializers.FieldLinksSerializer.class)
    public Collection<FieldLink> getFields() {
        return fields;
    }

    @JsonSerialize(using = InternalSerializers.FieldLinksSerializer.class)
    public Collection<FieldLink> getNested() {
        return nested;
    }

    public Boolean getInBackground() {
        return inBackground;
    }
}
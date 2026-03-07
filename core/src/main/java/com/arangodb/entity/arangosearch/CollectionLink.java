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

import com.arangodb.internal.serde.InternalDeserializers;
import com.arangodb.internal.serde.InternalSerializers;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class CollectionLink {

    private final String name;
    private Collection<String> analyzers;
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private StoreValuesType storeValues;
    private Collection<FieldLink> fields;
    private Collection<FieldLink> nested;
    private Boolean inBackground;
    private Boolean cache;

    private CollectionLink(final String name) {
        super();
        this.name = name;
    }

    /**
     * Creates an instance of {@code CollectionLink} on the given collection name
     *
     * @param name Name of a collection
     * @return new instance of {@code CollectionLink}
     */
    @JsonCreator
    public static CollectionLink on(@JsonProperty("name") final String name) {
        return new CollectionLink(name);
    }

    /**
     * @param analyzers The list of analyzers to be used for indexing of string values (default: ["identity"]).
     * @return link
     */
    public CollectionLink analyzers(final String... analyzers) {
        this.analyzers = Arrays.asList(analyzers);
        return this;
    }

    /**
     * @param includeAllFields The flag determines whether or not to index all fields on a particular level of depth
     *                         (default:
     *                         false).
     * @return link
     */
    public CollectionLink includeAllFields(final Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    /**
     * @param trackListPositions The flag determines whether or not values in a lists should be treated separate
     *                           (default: false).
     * @return link
     */
    public CollectionLink trackListPositions(final Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    /**
     * @param storeValues How should the view track the attribute values, this setting allows for additional value
     *                    retrieval
     *                    optimizations (default "none").
     * @return link
     */
    public CollectionLink storeValues(final StoreValuesType storeValues) {
        this.storeValues = storeValues;
        return this;
    }

    /**
     * @param fields A list of linked fields
     * @return link
     */
    @JsonDeserialize(using = InternalDeserializers.FieldLinksDeserializer.class)
    public CollectionLink fields(final FieldLink... fields) {
        this.fields = Arrays.asList(fields);
        return this;
    }

    /**
     * @param nested A list of nested fields
     * @return link
     * @since ArangoDB 3.10
     */
    @JsonDeserialize(using = InternalDeserializers.FieldLinksDeserializer.class)
    public CollectionLink nested(final FieldLink... nested) {
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
    public CollectionLink inBackground(final Boolean inBackground) {
        this.inBackground = inBackground;
        return this;
    }

    /**
     * @param cache If you enable this option, then field normalization values are always cached in memory. This can
     *              improve the performance of scoring and ranking queries. Otherwise, these values are memory-mapped
     *              and it is up to the operating system to load them from disk into memory and to evict them from
     *              memory.
     * @return link
     * @since ArangoDB 3.9.5
     */
    public CollectionLink cache(final Boolean cache) {
        this.cache = cache;
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

    public Boolean getCache() {
        return cache;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionLink)) return false;
        CollectionLink that = (CollectionLink) o;
        return Objects.equals(name, that.name) && Objects.equals(analyzers, that.analyzers) && Objects.equals(includeAllFields, that.includeAllFields) && Objects.equals(trackListPositions, that.trackListPositions) && storeValues == that.storeValues && Objects.equals(fields, that.fields) && Objects.equals(nested, that.nested) && Objects.equals(inBackground, that.inBackground) && Objects.equals(cache, that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, analyzers, includeAllFields, trackListPositions, storeValues, fields, nested, inBackground, cache);
    }
}
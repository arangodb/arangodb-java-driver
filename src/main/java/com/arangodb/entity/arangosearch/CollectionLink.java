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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public class CollectionLink {

    private final String name;
    private final Collection<String> analyzers;
    private Boolean includeAllFields;
    private Boolean trackListPositions;
    private StoreValuesType storeValues;
    private final Collection<FieldLink> fields;

    private CollectionLink(final String name) {
        super();
        this.name = name;
        fields = new ArrayList<>();
        analyzers = new ArrayList<>();
    }

    /**
     * Creates an instance of {@code CollectionLink} on the given collection name
     *
     * @param name Name of a collection
     * @return new instance of {@code CollectionLink}
     */
    public static CollectionLink on(final String name) {
        return new CollectionLink(name);
    }

    /**
     * @param analyzers The list of analyzers to be used for indexing of string values (default: ["identity"]).
     * @return link
     */
    public CollectionLink analyzers(final String... analyzers) {
        this.analyzers.addAll(Arrays.asList(analyzers));
        return this;
    }

    /**
     * @param includeAllFields The flag determines whether or not to index all fields on a particular level of depth (default:
     *                         false).
     * @return link
     */
    public CollectionLink includeAllFields(final Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
        return this;
    }

    /**
     * @param trackListPositions The flag determines whether or not values in a lists should be treated separate (default: false).
     * @return link
     */
    public CollectionLink trackListPositions(final Boolean trackListPositions) {
        this.trackListPositions = trackListPositions;
        return this;
    }

    /**
     * @param storeValues How should the view track the attribute values, this setting allows for additional value retrieval
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
    public CollectionLink fields(final FieldLink... fields) {
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
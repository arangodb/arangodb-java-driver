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
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author Michele Rastelli
 * @see <a href="https://docs.arangodb.com/devel/develop/http-api/indexes/multi-dimensional">API Documentation</a>
 * @since ArangoDB 3.12
 */
public final class MDIndexOptions extends IndexOptions<MDIndexOptions> {

    final IndexType type = IndexType.mdi;
    private Iterable<String> fields;
    private Boolean unique;
    private FieldValueTypes fieldValueTypes;
    private Boolean estimates;
    private Boolean sparse;
    private Iterable<String> storedValues;


    public MDIndexOptions() {
        super();
    }

    @Override
    MDIndexOptions getThis() {
        return this;
    }

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute names used for each dimension. Array expansions are not allowed.
     * @return options
     */
    MDIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    public IndexType getType() {
        return type;
    }

    public Boolean getUnique() {
        return unique;
    }

    /**
     * @param unique if true, then create a unique index
     * @return options
     */
    public MDIndexOptions unique(final Boolean unique) {
        this.unique = unique;
        return this;
    }

    public FieldValueTypes getFieldValueTypes() {
        return fieldValueTypes;
    }

    /**
     * @param fieldValueTypes must be {@link FieldValueTypes#DOUBLE}, currently only doubles are supported as values.
     * @return options
     */
    public MDIndexOptions fieldValueTypes(final FieldValueTypes fieldValueTypes) {
        this.fieldValueTypes = fieldValueTypes;
        return this;
    }

    public Boolean getEstimates() {
        return estimates;
    }

    /**
     * @param estimates controls whether index selectivity estimates are maintained for the index. Not maintaining index
     *                  selectivity estimates can have a slightly positive impact on write performance.
     *                  The downside of turning off index selectivity estimates is that the query optimizer is not able
     *                  to determine the usefulness of different competing indexes in AQL queries when there are
     *                  multiple candidate indexes to choose from.
     *                  The estimates attribute is optional and defaults to true if not set.
     *                  It cannot be disabled for non-unique multi-dimensional indexes because they have a fixed
     *                  selectivity estimate of 1.
     * @return options
     */
    public MDIndexOptions estimates(final Boolean estimates) {
        this.estimates = estimates;
        return this;
    }

    public Boolean getSparse() {
        return sparse;
    }

    /**
     * @param sparse if true, then create a sparse index
     * @return options
     */
    public MDIndexOptions sparse(final Boolean sparse) {
        this.sparse = sparse;
        return this;
    }

    public Iterable<String> getStoredValues() {
        return storedValues;
    }

    /**
     * @param storedValues can contain an array of paths to additional attributes to store in the index.
     *                     These additional attributes cannot be used for index lookups or for sorting, but they can be
     *                     used for projections. This allows an index to fully cover more queries and avoid extra
     *                     document lookups.
     *                     You can have the same attributes in storedValues and fields as the attributes in fields
     *                     cannot be used for projections, but you can also store additional attributes that are not
     *                     listed in fields.
     *                     Attributes in storedValues cannot overlap with the attributes specified in prefixFields.
     *                     Non-existing attributes are stored as null values inside storedValues.
     *                     The maximum number of attributes in storedValues is 32.
     * @return options
     */
    public MDIndexOptions storedValues(final Iterable<String> storedValues) {
        this.storedValues = storedValues;
        return this;
    }

    public enum FieldValueTypes {
        @JsonProperty("double")
        DOUBLE
    }

}

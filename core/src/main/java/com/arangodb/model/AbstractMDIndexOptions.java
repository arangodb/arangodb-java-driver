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

import com.arangodb.arch.NoRawTypesInspection;
import com.arangodb.entity.IndexType;


/**
 * @author Michele Rastelli
 * @since ArangoDB 3.12
 */
@NoRawTypesInspection
public abstract class AbstractMDIndexOptions<T extends AbstractMDIndexOptions<T>> extends IndexOptions<T> {

    private Iterable<String> fields;
    private Boolean unique;
    private MDIFieldValueTypes fieldValueTypes;
    private Boolean estimates;
    private Boolean sparse;
    private Iterable<String> storedValues;


    protected AbstractMDIndexOptions() {
        super();
    }

    public abstract IndexType getType();

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute names used for each dimension. Array expansions are not allowed.
     * @return options
     */
    T fields(final Iterable<String> fields) {
        this.fields = fields;
        return getThis();
    }

    public Boolean getUnique() {
        return unique;
    }

    /**
     * @param unique if true, then create a unique index
     * @return options
     */
    public T unique(final Boolean unique) {
        this.unique = unique;
        return getThis();
    }

    public MDIFieldValueTypes getFieldValueTypes() {
        return fieldValueTypes;
    }

    /**
     * @param fieldValueTypes must be {@link MDIFieldValueTypes#DOUBLE}, currently only doubles are supported as values.
     * @return options
     */
    public T fieldValueTypes(final MDIFieldValueTypes fieldValueTypes) {
        this.fieldValueTypes = fieldValueTypes;
        return getThis();
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
    public T estimates(final Boolean estimates) {
        this.estimates = estimates;
        return getThis();
    }

    public Boolean getSparse() {
        return sparse;
    }

    /**
     * @param sparse if true, then create a sparse index
     * @return options
     */
    public T sparse(final Boolean sparse) {
        this.sparse = sparse;
        return getThis();
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
    public T storedValues(final Iterable<String> storedValues) {
        this.storedValues = storedValues;
        return getThis();
    }

}

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Mark Vollmary
 */
public final class PersistentIndexOptions extends IndexOptions<PersistentIndexOptions> {

    private final IndexType type = IndexType.persistent;
    private Iterable<String> fields;
    private Boolean unique;
    private Boolean sparse;
    private Boolean deduplicate;
    private Boolean estimates;
    private Boolean cacheEnabled;
    private Collection<String> storedValues;

    public PersistentIndexOptions() {
        super();
    }

    @Override
    PersistentIndexOptions getThis() {
        return this;
    }

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    PersistentIndexOptions fields(final Iterable<String> fields) {
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
    public PersistentIndexOptions unique(final Boolean unique) {
        this.unique = unique;
        return this;
    }

    public Boolean getSparse() {
        return sparse;
    }

    /**
     * @param sparse if true, then create a sparse index
     * @return options
     */
    public PersistentIndexOptions sparse(final Boolean sparse) {
        this.sparse = sparse;
        return this;
    }

    public Boolean getDeduplicate() {
        return deduplicate;
    }

    /**
     * @param deduplicate if false, the deduplication of array values is turned off. Default: {@code true}
     * @return options
     */
    public PersistentIndexOptions deduplicate(final Boolean deduplicate) {
        this.deduplicate = deduplicate;
        return this;
    }

    /**
     * @param estimates This attribute controls whether index selectivity estimates are maintained for the index.
     *                  Default: {@code
     *                  true}
     * @since ArangoDB 3.8
     */
    public PersistentIndexOptions estimates(final Boolean estimates) {
        this.estimates = estimates;
        return this;
    }

    public Boolean getEstimates() {
        return estimates;
    }

    /**
     * @param cacheEnabled enables in-memory caching of index entries
     * @return options
     * @since ArangoDB 3.10
     */
    public PersistentIndexOptions cacheEnabled(final Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public Collection<String> getStoredValues() {
        return storedValues;
    }

    /**
     * @param storedValues (optional) array of paths to additional attributes to store in the index. These additional
     *                     attributes cannot be used for index lookups or for sorting, but they can be used for
     *                     projections. This allows an index to fully cover more queries and avoid extra document
     *                     lookups. The maximum number of attributes in `storedValues` is 32.
     * @return options
     */
    public PersistentIndexOptions storedValues(final String... storedValues) {
        if (this.storedValues == null) {
            this.storedValues = new HashSet<>();
        }
        Collections.addAll(this.storedValues, storedValues);
        return this;
    }

}

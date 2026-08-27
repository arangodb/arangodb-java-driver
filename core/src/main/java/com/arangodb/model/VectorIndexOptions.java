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
import com.arangodb.entity.VectorIndexParams;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class VectorIndexOptions extends IndexOptions<VectorIndexOptions> {

    private final IndexType type = IndexType.vector;
    private Iterable<String> fields;
    private Integer parallelism;
    private VectorIndexParams params;
    private Boolean sparse;
    private Collection<String> storedValues;

    @Override
    VectorIndexOptions getThis() {
        return this;
    }

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list with exactly one attribute path to specify where the vector embedding is stored in each
     *               document. Since ArangoDB 3.12.9, the vector data can be inserted after the index is created;
     *               the server trains the index when enough data is available.
     *               If you want to index another vector embedding attribute, you need to create a separate vector
     *               index.
     * @return this
     */
    VectorIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    /**
     * @param parallelism The number of threads to use for indexing.
     * @return this
     */
    public VectorIndexOptions parallelism(final Integer parallelism) {
        this.parallelism = parallelism;
        return this;
    }

    public VectorIndexParams getParams() {
        return params;
    }

    /**
     * @param params The parameters as used by the Faiss library.
     * @return this
     */
    public VectorIndexOptions params(final VectorIndexParams params) {
        this.params = params;
        return this;
    }

    public Boolean getSparse() {
        return sparse;
    }

    /**
     * @param sparse Whether to create a sparse index that excludes documents with the attribute for indexing missing
     *               or set to null. This attribute is defined by fields.
     * @return this
     */
    public VectorIndexOptions sparse(final Boolean sparse) {
        this.sparse = sparse;
        return this;
    }

    public Collection<String> getStoredValues() {
        return storedValues;
    }

    /**
     * @param storedValues Store additional attributes in the index (introduced in v3.12.7). Unlike with other index
     *                     types up to ArangoDB 3.12.9, these are used for filtering but not covering projections.
     *                     From ArangoDB 3.12.10 onward, they can also cover projections. The maximum number of
     *                     attributes that you can use in storedValues is 32.
     * @return this
     */
    public VectorIndexOptions storedValues(final String... storedValues) {
        if (this.storedValues == null) {
            this.storedValues = new HashSet<>();
        }
        Collections.addAll(this.storedValues, storedValues);
        return this;
    }

    public IndexType getType() {
        return type;
    }

}

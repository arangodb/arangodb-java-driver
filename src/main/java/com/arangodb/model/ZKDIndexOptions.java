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

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-multi-dim.html">API Documentation</a>
 * @since ArangoDB 3.9
 */
public class ZKDIndexOptions extends IndexOptions<ZKDIndexOptions> {

    private Iterable<String> fields;
    protected final IndexType type = IndexType.zkd;
    private Boolean unique;
    private FieldValueTypes fieldValueTypes;

    public ZKDIndexOptions() {
        super();
    }

    @Override
    protected ZKDIndexOptions getThis() {
        return this;
    }

    protected Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    protected ZKDIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    protected IndexType getType() {
        return type;
    }

    public Boolean getUnique() {
        return unique;
    }

    /**
     * @param unique if true, then create a unique index
     * @return options
     */
    public ZKDIndexOptions unique(final Boolean unique) {
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
    public ZKDIndexOptions fieldValueTypes(final FieldValueTypes fieldValueTypes) {
        this.fieldValueTypes = fieldValueTypes;
        return this;
    }

    public enum FieldValueTypes {
        DOUBLE
    }

}

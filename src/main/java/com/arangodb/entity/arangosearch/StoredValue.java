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

package com.arangodb.entity.arangosearch;


import java.util.List;
import java.util.Objects;

/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html">API Documentation</a>
 * @since ArangoDB 3.7
 */
public class StoredValue {

    private final List<String> fields;
    private final ArangoSearchCompression compression;

    /**
     * @param fields      A list of attribute paths. The . character denotes sub-attributes.
     * @param compression Defines how to compress the attribute values.
     */
    public StoredValue(List<String> fields, ArangoSearchCompression compression) {
        this.fields = fields;
        this.compression = compression;
    }

    public StoredValue(List<String> fields) {
        this(fields, null);
    }

    public List<String> getFields() {
        return fields;
    }

    public ArangoSearchCompression getCompression() {
        return compression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredValue that = (StoredValue) o;
        return Objects.equals(fields, that.fields) && compression == that.compression;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, compression);
    }
}

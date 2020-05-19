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

/**
 * @author Michele Rastelli
 */
public class StoredValue {

    private final List<String> fields;
    private final ArangoSearchCompression compression;

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

}

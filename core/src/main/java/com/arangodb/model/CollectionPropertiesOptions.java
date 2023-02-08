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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mark Vollmary
 * @see
 * <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#change-properties-of-a-collection">API
 * Documentation</a>
 */
public final class CollectionPropertiesOptions {

    private Boolean waitForSync;
    private CollectionSchema schema;
    private List<ComputedValue> computedValues;

    public CollectionPropertiesOptions() {
        super();
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync If true then creating or changing a document will wait until the data has been synchronized
     *                    to disk.
     * @return options
     */
    public CollectionPropertiesOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public CollectionSchema getSchema() {
        return schema;
    }

    /**
     * @param schema object that specifies the collection level schema for documents
     * @return options
     * @since ArangoDB 3.7
     */
    public CollectionPropertiesOptions schema(final CollectionSchema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @param computedValues An optional list of computed values.
     * @return options
     * @since ArangoDB 3.10
     */
    public CollectionPropertiesOptions computedValues(final ComputedValue... computedValues) {
        if(this.computedValues == null) {
            this.computedValues = new ArrayList<>();
        }
        Collections.addAll(this.computedValues, computedValues);
        return this;
    }

    public List<ComputedValue> getComputedValues() {
        return computedValues;
    }
}

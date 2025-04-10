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

package com.arangodb.entity;

import com.arangodb.model.CollectionSchema;
import com.arangodb.model.ComputedValue;

import java.util.List;
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public class CollectionEntity {

    private String id;
    private String name;
    private Boolean waitForSync;
    private Boolean isSystem;
    private CollectionStatus status;
    private CollectionType type;
    private CollectionSchema schema;
    private List<ComputedValue> computedValues;

    public CollectionEntity() {
        super();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    @Deprecated
    public CollectionStatus getStatus() {
        return status;
    }

    public CollectionType getType() {
        return type;
    }

    /**
     * @return Optional object that specifies the collection level schema for documents.
     * @since ArangoDB 3.7
     */
    public CollectionSchema getSchema() {
        return schema;
    }

    /**
     * @return A list of computed values.
     * @since ArangoDB 3.10
     */
    public List<ComputedValue> getComputedValues() {
        return computedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionEntity)) return false;
        CollectionEntity that = (CollectionEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(waitForSync, that.waitForSync) && Objects.equals(isSystem, that.isSystem) && status == that.status && type == that.type && Objects.equals(schema, that.schema) && Objects.equals(computedValues, that.computedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, waitForSync, isSystem, status, type, schema, computedValues);
    }
}

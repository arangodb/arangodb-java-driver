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

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html">API Documentation</a>
 */
public class CollectionEntity implements Entity {

    private String id;
    private String name;
    private Boolean waitForSync;
    private Boolean isVolatile;
    private Boolean isSystem;
    private CollectionStatus status;
    private CollectionType type;
    private CollectionSchema schema;

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

    public Boolean getIsVolatile() {
        return isVolatile;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

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

}

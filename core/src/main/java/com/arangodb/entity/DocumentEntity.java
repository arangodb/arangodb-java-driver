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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public class DocumentEntity {

    @JsonProperty("_key")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String key;

    @JsonProperty("_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonProperty("_rev")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String rev;

    public DocumentEntity() {
        super();
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getRev() {
        return rev;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DocumentEntity)) return false;
        DocumentEntity that = (DocumentEntity) o;
        return Objects.equals(key, that.key) && Objects.equals(id, that.id) && Objects.equals(rev, that.rev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, id, rev);
    }
}

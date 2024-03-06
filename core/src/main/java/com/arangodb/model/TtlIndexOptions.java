/*
 * DISCLAIMER
 *
 * Copyright 2019 ArangoDB GmbH, Cologne, Germany
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
 * @author Heiko Kernbach
 */
public final class TtlIndexOptions extends IndexOptions<TtlIndexOptions> {

    private final IndexType type = IndexType.ttl;
    private Iterable<String> fields;
    private Integer expireAfter;

    public TtlIndexOptions() {
        super();
    }

    @Override
    TtlIndexOptions getThis() {
        return this;
    }

    public Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    TtlIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    public IndexType getType() {
        return type;
    }

    /**
     * @param expireAfter The time (in seconds) after a document’s creation after which the documents count as
     *                    “expired”.
     * @return options
     */
    public TtlIndexOptions expireAfter(final Integer expireAfter) {
        this.expireAfter = expireAfter;
        return this;
    }

    public Integer getExpireAfter() {
        return expireAfter;
    }

}

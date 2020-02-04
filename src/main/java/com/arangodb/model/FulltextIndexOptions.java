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
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-fulltext.html#create-fulltext-index">API
 * Documentation</a>
 */
public class FulltextIndexOptions extends IndexOptions<FulltextIndexOptions> {

    private Iterable<String> fields;
    private final IndexType type = IndexType.fulltext;
    private Integer minLength;

    public FulltextIndexOptions() {
        super();
    }

    @Override
    protected FulltextIndexOptions getThis() {
        return this;
    }

    protected Iterable<String> getFields() {
        return fields;
    }

    /**
     * @param fields A list of attribute paths
     * @return options
     */
    protected FulltextIndexOptions fields(final Iterable<String> fields) {
        this.fields = fields;
        return this;
    }

    protected IndexType getType() {
        return type;
    }

    public Integer getMinLength() {
        return minLength;
    }

    /**
     * @param minLength Minimum character length of words to index. Will default to a server-defined value if unspecified. It
     *                  is thus recommended to set this value explicitly when creating the index.
     * @return options
     */
    public FulltextIndexOptions minLength(final Integer minLength) {
        this.minLength = minLength;
        return this;
    }

}

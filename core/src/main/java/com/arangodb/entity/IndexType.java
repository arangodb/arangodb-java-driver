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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Mark Vollmary
 * @author Heiko Kernbach
 */
public enum IndexType {

    primary,

    hash,

    skiplist,

    persistent,

    geo,

    geo1,

    geo2,

    /**
     * @deprecated since ArangoDB 3.10, use ArangoSearch or Inverted indexes instead.
     */
    @Deprecated
    fulltext,

    edge,

    ttl,

    zkd,

    /**
     * Multi Dimensional Index
     * @see <a href="https://docs.arango.ai/arangodb/stable/indexes-and-search/indexing/working-with-indexes/multi-dimensional-indexes">Ref Doc</a>
     * @since ArangoDB 3.12
     */
    mdi,

    /**
     * Multi Dimensional Prefixed Index
     * @see <a href="https://docs.arango.ai/arangodb/stable/indexes-and-search/indexing/working-with-indexes/multi-dimensional-indexes">Ref Doc</a>
     * @since ArangoDB 3.12
     */
    @JsonProperty("mdi-prefixed")
    mdiPrefixed,

    /**
     * @since ArangoDB 3.10
     */
    inverted,

    /**
     * @since ArangoDB 3.12
     */
    vector
}

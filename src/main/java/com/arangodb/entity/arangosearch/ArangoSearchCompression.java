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


/**
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/views-arangosearch.html">API Documentation</a>
 * @since ArangoDB 3.7
 */
public enum ArangoSearchCompression {

    /**
     * (default): use LZ4 fast compression.
     */
    lz4("lz4"),

    /**
     * disable compression to trade space for speed.
     */
    none("none");

    private final String value;

    ArangoSearchCompression(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

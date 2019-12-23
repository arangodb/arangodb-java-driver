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

package com.arangodb.entity;

/**
 * @author Axel Becker
 * https://www.arangodb.com/docs/stable/http/collection-creating.html
 */
public enum ShardingStrategy {

    COMMUNITY_COMPAT("community-compat"),
    ENTERPRISE_COMPAT("enterprise-compat"),
    ENTERPRISE_SMART_EDGE_COMPAT("enterprise-smart-edge-compat"),
    HASH("hash"),
    ENTERPRISE_HASH_SMART_EDGE("enterprise-hash-smart-edge");

    private final String internalName;

    ShardingStrategy(String internalName) {
        this.internalName = internalName;
    }

    public String getInternalName() {
        return this.internalName;
    }

}

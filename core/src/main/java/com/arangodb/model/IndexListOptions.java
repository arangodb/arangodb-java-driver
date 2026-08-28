/*
 * DISCLAIMER
 *
 * Copyright 2026 ArangoDB GmbH, Cologne, Germany
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

/**
 * Options for listing indexes of a collection.
 *
 * @since ArangoDB 3.12.10
 */
public final class IndexListOptions {
    private Boolean withHidden;
    private Boolean withStats;

    public Boolean getWithHidden() {
        return withHidden;
    }

    /**
     * @param withHidden whether to include hidden indexes and per-shard vector-index training details
     * @return this
     */
    public IndexListOptions withHidden(final Boolean withHidden) {
        this.withHidden = withHidden;
        return this;
    }

    public Boolean getWithStats() {
        return withStats;
    }

    /**
     * @param withStats Whether to include figures and estimates in the result.
     * @return this
     */
    public IndexListOptions withStats(final Boolean withStats) {
        this.withStats = withStats;
        return this;
    }
}

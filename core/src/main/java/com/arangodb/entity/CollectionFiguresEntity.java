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

package com.arangodb.entity;

import java.util.Map;
import java.util.Objects;

/**
 * Statistics returned in the {@code figures} attribute of a collection response.
 * Optional metrics are null when not reported by the server.
 */
public final class CollectionFiguresEntity {

    private Indexes indexes;
    private Long documentsSize;
    private Boolean cacheInUse;
    private Long cacheSize;
    private Long cacheUsage;
    private Double cacheLifeTimeHitRate;
    private Double cacheWindowedHitRate;
    private Map<String, Object> engine;

    /**
     * @return aggregate index statistics, including the predefined indexes
     */
    public Indexes getIndexes() {
        return indexes;
    }

    /**
     * @return approximate on-disk document size in bytes
     */
    public Long getDocumentsSize() {
        return documentsSize;
    }

    /**
     * @return whether the document cache is enabled
     */
    public Boolean getCacheInUse() {
        return cacheInUse;
    }

    /**
     * @return total document-cache memory usage in bytes
     */
    public Long getCacheSize() {
        return cacheSize;
    }

    /**
     * @return memory used by data in the document cache, in bytes
     */
    public Long getCacheUsage() {
        return cacheUsage;
    }

    /**
     * @return lifetime cache hit percentage, or null when the cache is not in use;
     * in a cluster, the sum of the shard percentages
     */
    public Double getCacheLifeTimeHitRate() {
        return cacheLifeTimeHitRate;
    }

    /**
     * @return recent cache hit percentage, or null when the cache is not in use;
     * in a cluster, the sum of the shard percentages
     */
    public Double getCacheWindowedHitRate() {
        return cacheWindowedHitRate;
    }

    /**
     * Extended storage-engine statistics, requested with
     * {@link com.arangodb.model.CollectionFiguresOptions#details(Boolean)}.
     * These debugging details are represented as a map because their structure is subject to change.
     *
     * @return storage-engine details, or null when not reported by the server
     * @since ArangoDB 3.8.0
     */
    public Map<String, Object> getEngine() {
        return engine;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionFiguresEntity)) return false;
        CollectionFiguresEntity that = (CollectionFiguresEntity) o;
        return Objects.equals(indexes, that.indexes)
                && Objects.equals(documentsSize, that.documentsSize)
                && Objects.equals(cacheInUse, that.cacheInUse)
                && Objects.equals(cacheSize, that.cacheSize)
                && Objects.equals(cacheUsage, that.cacheUsage)
                && Objects.equals(cacheLifeTimeHitRate, that.cacheLifeTimeHitRate)
                && Objects.equals(cacheWindowedHitRate, that.cacheWindowedHitRate)
                && Objects.equals(engine, that.engine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexes, documentsSize, cacheInUse, cacheSize, cacheUsage,
                cacheLifeTimeHitRate, cacheWindowedHitRate, engine);
    }

    /**
     * Aggregate statistics for all indexes of the collection.
     */
    public static final class Indexes {

        private Long count;
        private Long size;

        /**
         * @return number of indexes, including the predefined indexes
         */
        public Long getCount() {
            return count;
        }

        /**
         * @return total index memory allocation in bytes
         */
        public Long getSize() {
            return size;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Indexes)) return false;
            Indexes that = (Indexes) o;
            return Objects.equals(count, that.count) && Objects.equals(size, that.size);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, size);
        }
    }
}

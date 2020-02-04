/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;

import java.util.Collection;

/**
 * @author Mark Vollmary
 */
public class ArangoSearchPropertiesEntity extends ViewEntity {

    private final ArangoSearchProperties properties;

    public ArangoSearchPropertiesEntity(final String id, final String name, final ViewType type,
                                        final ArangoSearchProperties properties) {
        super(id, name, type);
        this.properties = properties;
    }

    /**
     * @return Wait at least this many milliseconds between committing index data changes and making them visible to
     * queries (default: 60000, to disable use: 0). For the case where there are a lot of inserts/updates, a
     * lower value, until commit, will cause the index not to account for them and memory usage would continue
     * to grow. For the case where there are a few inserts/updates, a higher value will impact performance and
     * waste disk space for each commit call without any added benefits.
     */
    public Long getConsolidationIntervalMsec() {
        return properties.getConsolidationIntervalMsec();
    }

    /**
     * @return Wait at least this many milliseconds between committing view data store changes and making documents
     * visible to queries (default: 1000, to disable use: 0). For the case where there are a lot of inserts/updates, a
     * lower value, until commit, will cause the index not to account for them and memory usage would continue to grow.
     * For the case where there are a few inserts/updates, a higher value will impact performance and waste disk space
     * for each commit call without any added benefits. Background: For data retrieval ArangoSearch views follow the
     * concept of “eventually-consistent”, i.e. eventually all the data in ArangoDB will be matched by corresponding
     * query expressions. The concept of ArangoSearch view “commit” operation is introduced to control the upper-bound
     * on the time until document addition/removals are actually reflected by corresponding query expressions. Once a
     * “commit” operation is complete all documents added/removed prior to the start of the “commit” operation will be
     * reflected by queries invoked in subsequent ArangoDB transactions, in-progress ArangoDB transactions will still
     * continue to return a repeatable-read state.
     */
    public Long getCommitIntervalMsec() {
        return properties.getCommitIntervalMsec();
    }

    /**
     * @return Wait at least this many commits between removing unused files in data directory (default: 10, to disable
     * use: 0). For the case where the consolidation policies merge segments often (i.e. a lot of
     * commit+consolidate), a lower value will cause a lot of disk space to be wasted. For the case where the
     * consolidation policies rarely merge segments (i.e. few inserts/deletes), a higher value will impact
     * performance without any added benefits.
     */
    public Long getCleanupIntervalStep() {
        return properties.getCleanupIntervalStep();
    }

    public ConsolidationPolicy getConsolidationPolicy() {
        return properties.getConsolidationPolicy();
    }

    /**
     * @return A list of linked collections
     */
    public Collection<CollectionLink> getLinks() {
        return properties.getLinks();
    }

    /**
     * @return A list of primary sort objects
     */
    public Collection<PrimarySort> getPrimarySort() {
        return properties.getPrimarySort();
    }
}

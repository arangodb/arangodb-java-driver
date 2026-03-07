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

import com.arangodb.entity.ReplicationFactor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mark Vollmary
 */
public final class CollectionPropertiesOptions {

    private Boolean cacheEnabled;
    private List<ComputedValue> computedValues;
    private ReplicationFactor replicationFactor;
    private CollectionSchema schema;
    private Boolean waitForSync;
    private Integer writeConcern;

    public CollectionPropertiesOptions() {
        super();
    }

    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * @param cacheEnabled Whether the in-memory hash cache for documents should be enabled for this collection. Can be
     *                     controlled globally with the --cache.size startup option. The cache can speed up repeated
     *                     reads of the same documents via their document keys. If the same documents are not fetched
     *                     often or are modified frequently, then you may disable the cache to avoid the maintenance
     *                     costs.
     * @return this
     */
    public CollectionPropertiesOptions cacheEnabled(final Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    public List<ComputedValue> getComputedValues() {
        return computedValues;
    }

    /**
     * @param computedValues An optional list of computed values.
     * @return this
     * @since ArangoDB 3.10
     */
    public CollectionPropertiesOptions computedValues(final ComputedValue... computedValues) {
        if (this.computedValues == null) {
            this.computedValues = new ArrayList<>();
        }
        Collections.addAll(this.computedValues, computedValues);
        return this;
    }

    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    /**
     * @param replicationFactor In a cluster, this attribute determines how many copies of each shard are kept on
     *                          different DB-Servers. The value 1 means that only one copy (no synchronous replication)
     *                          is kept. A value of k means that k-1 replicas are kept. For SatelliteCollections, it
     *                          needs to be the string "satellite", which matches the replication factor to the number
     *                          of DB-Servers.
     *                          <p>
     *                          Any two copies reside on different DB-Servers. Replication between them is synchronous,
     *                          that is, every write operation to the “leader” copy will be replicated to all “follower”
     *                          replicas, before the write operation is reported successful.
     *                          <p>
     *                          If a server fails, this is detected automatically and one of the servers holding copies
     *                          take over, usually without an error being reported.
     * @return this
     */
    public CollectionPropertiesOptions replicationFactor(final ReplicationFactor replicationFactor) {
        this.replicationFactor = replicationFactor;
        return this;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public CollectionSchema getSchema() {
        return schema;
    }

    /**
     * @param schema object that specifies the collection level schema for documents
     * @return this
     * @since ArangoDB 3.7
     */
    public CollectionPropertiesOptions schema(final CollectionSchema schema) {
        this.schema = schema;
        return this;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync If true then creating or changing a document will wait until the data has been synchronized
     *                    to disk.
     * @return this
     */
    public CollectionPropertiesOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    /**
     * @param writeConcern Determines how many copies of each shard are required to be in sync on the different
     *                     DB-Servers. If there are less than these many copies in the cluster, a shard refuses to
     *                     write. Writes to shards with enough up-to-date copies succeed at the same time, however.
     *                     The value of writeConcern cannot be greater than replicationFactor.
     *                     <p>
     *                     If distributeShardsLike is set, the default writeConcern is that of the prototype collection.
     *                     For SatelliteCollections, the writeConcern is automatically controlled to equal the number of
     *                     DB-Servers and has a value of 0. Otherwise, the default value is controlled by the current
     *                     database’s default writeConcern, which uses the --cluster.write-concern startup option as
     *                     default, which defaults to 1. (cluster only)
     * @return this
     */
    public CollectionPropertiesOptions writeConcern(final Integer writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

}

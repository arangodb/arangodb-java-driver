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

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.6.0
 */
public final class DatabaseOptions {

    private ReplicationFactor replicationFactor;
    private Integer writeConcern;
    private String sharding;

    public DatabaseOptions() {
        super();
    }

    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    public String getSharding() {
        return sharding;
    }

    /**
     * @param replicationFactor the default replication factor for collections in this database
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions replicationFactor(final ReplicationFactor replicationFactor) {
        this.replicationFactor = replicationFactor;
        return this;
    }

    public DatabaseOptions replicationFactor(int replicationFactor) {
        this.replicationFactor = ReplicationFactor.of(replicationFactor);
        return this;
    }

    /**
     * Default write concern for new collections created in this database. It determines how many copies of each shard
     * are required to be in sync on the different DBServers. If there are less then these many copies in the cluster a
     * shard will refuse to write. Writes to shards with enough up-to-date copies will succeed at the same time however.
     * The value of writeConcern can not be larger than replicationFactor. (cluster only)
     *
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions writeConcern(final Integer writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    /**
     * @param sharding The sharding method to use for new collections in this database.
     *                 Valid values are: “”, “flexible”, or “single”. The first two are equivalent.
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions sharding(String sharding) {
        this.sharding = sharding;
        return this;
    }

}

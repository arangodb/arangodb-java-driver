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

import com.arangodb.entity.MinReplicationFactor;
import com.arangodb.entity.ReplicationFactor;

/**
 * @author Michele Rastelli
 * @since ArangoDB 3.6.0
 */
public class DatabaseOptions {

    private final ReplicationFactor replicationFactor;
    private final MinReplicationFactor minReplicationFactor;
    private String sharding;

    public DatabaseOptions() {
        super();
        replicationFactor = new ReplicationFactor();
        minReplicationFactor = new MinReplicationFactor();
    }

    public Integer getReplicationFactor() {
        return replicationFactor.getReplicationFactor();
    }

    public Integer getMinReplicationFactor() {
        return minReplicationFactor.getMinReplicationFactor();
    }

    public Boolean getSatellite() {
        return this.replicationFactor.getSatellite();
    }

    public String getSharding() {
        return sharding;
    }

    /**
     * @param replicationFactor the default replication factor for collections in this database
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions replicationFactor(final Integer replicationFactor) {
        this.replicationFactor.setReplicationFactor(replicationFactor);
        return this;
    }

    /**
     * @param minReplicationFactor (optional, default is 1): in a cluster, this attribute determines how many desired copies of each
     *                             shard are kept on different DBServers. The value 1 means that only one copy (no synchronous
     *                             replication) is kept. A value of k means that desired k-1 replicas are kept. If in a failover scenario
     *                             a shard of a collection has less than minReplicationFactor many insync followers it will go into
     *                             "read-only" mode and will reject writes until enough followers are insync again. In more detail:
     *                             Having `minReplicationFactor == 1` means as soon as a "master-copy" is available of the data writes
     *                             are allowed. Having `minReplicationFactor > 1` requires additional insync copies on follower servers
     *                             to allow writes.
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions minReplicationFactor(final Integer minReplicationFactor) {
        this.minReplicationFactor.setMinReplicationFactor(minReplicationFactor);
        return this;
    }

    /**
     * @param satellite whether the collection is a satellite collection. Only in an enterprise cluster setup (else
     *                  returning null).
     * @return options
     * @since ArangoDB 3.6.0
     */
    public DatabaseOptions satellite(final Boolean satellite) {
        this.replicationFactor.setSatellite(satellite);
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

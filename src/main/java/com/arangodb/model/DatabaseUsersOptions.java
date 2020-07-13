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

import java.util.Map;

/**
 * @author Michele Rastelli
 */
public class DatabaseUsersOptions {

    private String username;
    private ReplicationFactor replicationFactor;
    private Map<String, Object> extra;
    private String passwd;
    private Integer writeConcern;
    private Boolean active;
    private String sharding;

    public DatabaseUsersOptions() {
        replicationFactor = new ReplicationFactor();
    }

    public String getUsername() {
        return username;
    }

    /**
     * @param username Login name of the user to be created
     * @return options
     */
    public DatabaseUsersOptions username(final String username) {
        this.username = username;
        return this;
    }

    public Integer getReplicationFactor() {
        return replicationFactor.getReplicationFactor();
    }

    /**
     * @param replicationFactor Default replication factor for new collections created in this database.
     * @return options
     */
    public DatabaseUsersOptions replicationFactor(final Integer replicationFactor) {
        this.replicationFactor.setReplicationFactor(replicationFactor);
        return this;
    }

    public Boolean getSatellite() {
        return this.replicationFactor.getSatellite();
    }

    public DatabaseUsersOptions satellite(final Boolean satellite) {
        this.replicationFactor.setSatellite(satellite);
        return this;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * @param extra extra user information. The data contained in extra
     *              will be stored for the user but not be interpreted further by ArangoDB.
     * @return options
     */
    public DatabaseUsersOptions extra(final Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }

    public String getPasswd() {
        return passwd;
    }

    /**
     * @param passwd The user password as a string. If not specified, it will default to an empty string.
     * @return options
     */
    public DatabaseUsersOptions passwd(final String passwd) {
        this.passwd = passwd;
        return this;
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    /**
     * @param writeConcern Default write concern for new collections created in this database.
     *                     It determines how many copies of each shard are required to be
     *                     in sync on the different DBServers. If there are less then these many copies
     *                     in the cluster a shard will refuse to write. Writes to shards with enough
     *                     up-to-date copies will succeed at the same time however. The value of
     *                     writeConcern can not be larger than replicationFactor. (cluster only)
     * @return options
     */
    public DatabaseUsersOptions writeConcern(final Integer writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    /**
     * @param active A flag indicating whether the user account should be activated or not.
     *               The default value is true. If set to false, the user won't be able to
     *               log into the database.
     * @return options
     */
    public DatabaseUsersOptions active(final Boolean active) {
        this.active = active;
        return this;
    }

    public String getSharding() {
        return sharding;
    }

    /**
     * @param sharding The sharding method to use for new collections in this database. Valid values
     *                 are: "", "flexible", or "single". The first two are equivalent. (cluster only)
     * @return options
     */
    public DatabaseUsersOptions sharding(final String sharding) {
        this.sharding = sharding;
        return this;
    }

}

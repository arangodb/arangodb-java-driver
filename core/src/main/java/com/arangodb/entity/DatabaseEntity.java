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

import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class DatabaseEntity {

    private String id;
    private String name;
    private String path;
    private Boolean isSystem;
    private ReplicationFactor replicationFactor;
    private Integer writeConcern;
    private String sharding;

    public DatabaseEntity() {
        super();
    }

    /**
     * @return the id of the database
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name of the database
     */
    public String getName() {
        return name;
    }

    /**
     * @return the filesystem path of the database
     */
    public String getPath() {
        return path;
    }

    /**
     * @return whether or not the database is the _system database
     */
    public Boolean getIsSystem() {
        return isSystem;
    }

    /**
     * @return the default replication factor for collections in this database
     * @since ArangoDB 3.6.0
     */
    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    /**
     * Default write concern for new collections created in this database. It determines how many copies of each shard
     * are required to be in sync on the different DBServers. If there are less then these many copies in the cluster a
     * shard will refuse to write. Writes to shards with enough up-to-date copies will succeed at the same time however.
     * The value of writeConcern can not be larger than replicationFactor. (cluster only)
     *
     * @since ArangoDB 3.6.0
     */
    public Integer getWriteConcern() {
        return writeConcern;
    }

    /**
     * @return information about the default sharding method for collections created in this database
     * @since ArangoDB 3.6.0
     */
    public String getSharding() {
        return sharding;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DatabaseEntity)) return false;
        DatabaseEntity that = (DatabaseEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(path, that.path) && Objects.equals(isSystem, that.isSystem) && Objects.equals(replicationFactor, that.replicationFactor) && Objects.equals(writeConcern, that.writeConcern) && Objects.equals(sharding, that.sharding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, path, isSystem, replicationFactor, writeConcern, sharding);
    }
}

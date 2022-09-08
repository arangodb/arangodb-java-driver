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

import com.arangodb.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
 * Documentation</a>
 */
public final class CollectionCreateOptions {

    private String name;
    private ReplicationFactor replicationFactor;
    private Integer writeConcern;
    private KeyOptions keyOptions;
    private Boolean waitForSync;
    private List<ComputedValue> computedValues = new ArrayList<>();
    private Collection<String> shardKeys;
    private Integer numberOfShards;
    private Boolean isSystem;
    private CollectionType type;
    private String distributeShardsLike;

    private String shardingStrategy; // cluster option
    private String smartJoinAttribute; // enterprise option

    private CollectionSchema schema;

    public CollectionCreateOptions() {
        super();
    }

    public String getName() {
        return name;
    }

    /**
     * @param name The name of the collection
     * @return options
     */
    CollectionCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    /**
     * @param replicationFactor (The default is 1): in a cluster, this attribute determines how many copies of each
     *                          shard are kept on
     *                          different DBServers. The value 1 means that only one copy (no synchronous
     *                          replication) is kept. A
     *                          value of k means that k-1 replicas are kept. Any two copies reside on different
     *                          DBServers. Replication
     *                          between them is synchronous, that is, every write operation to the "leader" copy will
     *                          be replicated to
     *                          all "follower" replicas, before the write operation is reported successful. If a
     *                          server fails, this is
     *                          detected automatically and one of the servers holding copies take over, usually
     *                          without an error being
     *                          reported.
     * @return options
     */
    public CollectionCreateOptions replicationFactor(final ReplicationFactor replicationFactor) {
        this.replicationFactor = replicationFactor;
        return this;
    }

    public CollectionCreateOptions replicationFactor(int replicationFactor) {
        this.replicationFactor = ReplicationFactor.of(replicationFactor);
        return this;
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    /**
     * @param writeConcern write concern for this collection (default: 1).
     *                     It determines how many copies of each shard are required to be in sync on the different
     *                     DB-Servers. If there are less then these many copies in the cluster a shard will refuse to
     *                     write. Writes to shards with enough up-to-date copies will succeed at the same time however.
     *                     The value of writeConcern can not be larger than replicationFactor. (cluster only)
     * @return options
     */
    public CollectionCreateOptions writeConcern(final Integer writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }

    public KeyOptions getKeyOptions() {
        return keyOptions;
    }

    /**
     * @param allowUserKeys if set to true, then it is allowed to supply own key values in the _key attribute of a
     *                      document. If
     *                      set to false, then the key generator will solely be responsible for generating keys and
     *                      supplying own
     *                      key values in the _key attribute of documents is considered an error.
     * @param type          specifies the type of the key generator. The currently available generators are
     *                      traditional and
     *                      autoincrement.
     * @param increment     increment value for autoincrement key generator. Not used for other key generator types.
     * @param offset        Initial offset value for autoincrement key generator. Not used for other key generator
     *                      types.
     * @return options
     */
    public CollectionCreateOptions keyOptions(
            final Boolean allowUserKeys,
            final KeyType type,
            final Integer increment,
            final Integer offset) {
        this.keyOptions = new KeyOptions(allowUserKeys, type, increment, offset);
        return this;
    }

    public Boolean getWaitForSync() {
        return waitForSync;
    }

    /**
     * @param waitForSync If true then the data is synchronized to disk before returning from a document create,
     *                    update, replace
     *                    or removal operation. (default: false)
     * @return options
     */
    public CollectionCreateOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Collection<String> getShardKeys() {
        return shardKeys;
    }

    /**
     * @param shardKeys (The default is [ "_key" ]): in a cluster, this attribute determines which document
     *                  attributes are
     *                  used to determine the target shard for documents. Documents are sent to shards based on the
     *                  values of
     *                  their shard key attributes. The values of all shard key attributes in a document are hashed,
     *                  and the
     *                  hash value is used to determine the target shard. Note: Values of shard key attributes cannot be
     *                  changed once set. This option is meaningless in a single server setup.
     * @return options
     */
    public CollectionCreateOptions shardKeys(final String... shardKeys) {
        this.shardKeys = Arrays.asList(shardKeys);
        return this;
    }

    /**
     * @param smartJoinAttribute
     * @return options
     */
    public CollectionCreateOptions smartJoinAttribute(final String smartJoinAttribute) {
        this.smartJoinAttribute = smartJoinAttribute;
        return this;
    }

    public String getSmartJoinAttribute() {
        return smartJoinAttribute;
    }

    /**
     * @param shardingStrategy
     * @return options
     */
    public CollectionCreateOptions shardingStrategy(final String shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
        return this;
    }

    public String getShardingStrategy() {
        return shardingStrategy;
    }

    /**
     * @param numberOfShards (The default is 1): in a cluster, this value determines the number of shards to create
     *                       for the
     *                       collection. In a single server setup, this option is meaningless.
     * @return options
     */
    public CollectionCreateOptions numberOfShards(final Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
        return this;
    }

    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    /**
     * @param isSystem If true, create a system collection. In this case collection-name should start with an
     *                 underscore. End
     *                 users should normally create non-system collections only. API implementors may be required to
     *                 create
     *                 system collections in very special occasions, but normally a regular collection will do. (The
     *                 default
     *                 is false)
     * @return options
     */
    public CollectionCreateOptions isSystem(final Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }

    public CollectionType getType() {
        return type;
    }

    /**
     * @param type (The default is {@link CollectionType#DOCUMENT}): the type of the collection to create.
     * @return options
     */
    public CollectionCreateOptions type(final CollectionType type) {
        this.type = type;
        return this;
    }

    public String getDistributeShardsLike() {
        return distributeShardsLike;
    }

    /**
     * @param distributeShardsLike (The default is ""): in an enterprise cluster, this attribute binds the specifics
     *                             of sharding for the
     *                             newly created collection to follow that of a specified existing collection. Note:
     *                             Using this parameter
     *                             has consequences for the prototype collection. It can no longer be dropped, before
     *                             sharding imitating
     *                             collections are dropped. Equally, backups and restores of imitating collections
     *                             alone will generate
     *                             warnings, which can be overridden, about missing sharding prototype.
     * @return options
     */
    public CollectionCreateOptions distributeShardsLike(final String distributeShardsLike) {
        this.distributeShardsLike = distributeShardsLike;
        return this;
    }

    public CollectionSchema getSchema() {
        return schema;
    }

    /**
     * @param schema object that specifies the collection level schema for documents
     * @return options
     * @since ArangoDB 3.7
     */
    public CollectionCreateOptions schema(final CollectionSchema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * @param computedValues An optional list of computed values.
     * @return options
     * @since ArangoDB 3.10
     */
    public CollectionCreateOptions computedValues(final ComputedValue... computedValues) {
        Collections.addAll(this.computedValues, computedValues);
        return this;
    }

    public List<ComputedValue> getComputedValues() {
        return computedValues;
    }
}

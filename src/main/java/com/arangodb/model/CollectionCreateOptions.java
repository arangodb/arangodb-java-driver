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

/**
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
 * Documentation</a>
 */
public class CollectionCreateOptions {

    private String name;
    private Long journalSize;
    private final ReplicationFactor replicationFactor;
    private final MinReplicationFactor minReplicationFactor;
    private KeyOptions keyOptions;
    private Boolean waitForSync;
    private Boolean doCompact;
    private Boolean isVolatile;
    private String[] shardKeys;
    private Integer numberOfShards;
    private Boolean isSystem;
    private CollectionType type;
    private Integer indexBuckets;
    private String distributeShardsLike;

    private String shardingStrategy; // cluster option
    private String smartJoinAttribute; // enterprise option

    public CollectionCreateOptions() {
        super();
        replicationFactor = new ReplicationFactor();
        minReplicationFactor = new MinReplicationFactor();
    }

    protected String getName() {
        return name;
    }

    /**
     * @param name The name of the collection
     * @return options
     */
    protected CollectionCreateOptions name(final String name) {
        this.name = name;
        return this;
    }

    public Long getJournalSize() {
        return journalSize;
    }

    /**
     * @param journalSize The maximal size of a journal or datafile in bytes. The value must be at least 1048576 (1 MiB).
     * @return options
     */
    public CollectionCreateOptions journalSize(final Long journalSize) {
        this.journalSize = journalSize;
        return this;
    }

    public Integer getReplicationFactor() {
        return replicationFactor.getReplicationFactor();
    }

    public Integer getMinReplicationFactor() {
        return minReplicationFactor.getMinReplicationFactor();
    }

    /**
     * @param replicationFactor (The default is 1): in a cluster, this attribute determines how many copies of each shard are kept on
     *                          different DBServers. The value 1 means that only one copy (no synchronous replication) is kept. A
     *                          value of k means that k-1 replicas are kept. Any two copies reside on different DBServers. Replication
     *                          between them is synchronous, that is, every write operation to the "leader" copy will be replicated to
     *                          all "follower" replicas, before the write operation is reported successful. If a server fails, this is
     *                          detected automatically and one of the servers holding copies take over, usually without an error being
     *                          reported.
     * @return options
     */
    public CollectionCreateOptions replicationFactor(final Integer replicationFactor) {
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
     */
    public CollectionCreateOptions minReplicationFactor(final Integer minReplicationFactor) {
        this.minReplicationFactor.setMinReplicationFactor(minReplicationFactor);
        return this;
    }

    public Boolean getSatellite() {
        return replicationFactor.getSatellite();
    }

    /**
     * @param satellite If the true the collection is created as a satellite collection. In this case
     *                  {@link #replicationFactor(Integer)} is ignored.
     * @return options
     */
    public CollectionCreateOptions satellite(final Boolean satellite) {
        this.replicationFactor.setSatellite(satellite);
        return this;
    }

    public KeyOptions getKeyOptions() {
        return keyOptions;
    }

    /**
     * @param allowUserKeys if set to true, then it is allowed to supply own key values in the _key attribute of a document. If
     *                      set to false, then the key generator will solely be responsible for generating keys and supplying own
     *                      key values in the _key attribute of documents is considered an error.
     * @param type          specifies the type of the key generator. The currently available generators are traditional and
     *                      autoincrement.
     * @param increment     increment value for autoincrement key generator. Not used for other key generator types.
     * @param offset        Initial offset value for autoincrement key generator. Not used for other key generator types.
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
     * @param waitForSync If true then the data is synchronized to disk before returning from a document create, update, replace
     *                    or removal operation. (default: false)
     * @return options
     */
    public CollectionCreateOptions waitForSync(final Boolean waitForSync) {
        this.waitForSync = waitForSync;
        return this;
    }

    public Boolean getDoCompact() {
        return doCompact;
    }

    /**
     * @param doCompact whether or not the collection will be compacted (default is true)
     * @return options
     */
    public CollectionCreateOptions doCompact(final Boolean doCompact) {
        this.doCompact = doCompact;
        return this;
    }

    public Boolean getIsVolatile() {
        return isVolatile;
    }

    /**
     * @param isVolatile If true then the collection data is kept in-memory only and not made persistent. Unloading the
     *                   collection will cause the collection data to be discarded. Stopping or re-starting the server will
     *                   also cause full loss of data in the collection. Setting this option will make the resulting collection
     *                   be slightly faster than regular collections because ArangoDB does not enforce any synchronization to
     *                   disk and does not calculate any CRC checksums for datafiles (as there are no datafiles). This option
     *                   should therefore be used for cache-type collections only, and not for data that cannot be re-created
     *                   otherwise. (The default is false)
     * @return options
     */
    public CollectionCreateOptions isVolatile(final Boolean isVolatile) {
        this.isVolatile = isVolatile;
        return this;
    }

    public String[] getShardKeys() {
        return shardKeys;
    }

    /**
     * @param shardKeys (The default is [ "_key" ]): in a cluster, this attribute determines which document attributes are
     *                  used to determine the target shard for documents. Documents are sent to shards based on the values of
     *                  their shard key attributes. The values of all shard key attributes in a document are hashed, and the
     *                  hash value is used to determine the target shard. Note: Values of shard key attributes cannot be
     *                  changed once set. This option is meaningless in a single server setup.
     * @return options
     */
    public CollectionCreateOptions shardKeys(final String... shardKeys) {
        this.shardKeys = shardKeys;
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
     * @param numberOfShards (The default is 1): in a cluster, this value determines the number of shards to create for the
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
     * @param isSystem If true, create a system collection. In this case collection-name should start with an underscore. End
     *                 users should normally create non-system collections only. API implementors may be required to create
     *                 system collections in very special occasions, but normally a regular collection will do. (The default
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

    public Integer getIndexBuckets() {
        return indexBuckets;
    }

    /**
     * @param indexBuckets The number of buckets into which indexes using a hash table are split. The default is 16 and this
     *                     number has to be a power of 2 and less than or equal to 1024. For very large collections one should
     *                     increase this to avoid long pauses when the hash table has to be initially built or resized, since
     *                     buckets are resized individually and can be initially built in parallel. For example, 64 might be a
     *                     sensible value for a collection with 100 000 000 documents. Currently, only the edge index respects
     *                     this value, but other index types might follow in future ArangoDB versions. Changes (see below) are
     *                     applied when the collection is loaded the next time.
     * @return options
     */
    public CollectionCreateOptions indexBuckets(final Integer indexBuckets) {
        this.indexBuckets = indexBuckets;
        return this;
    }

    public String getDistributeShardsLike() {
        return distributeShardsLike;
    }

    /**
     * @param distributeShardsLike (The default is ""): in an enterprise cluster, this attribute binds the specifics of sharding for the
     *                             newly created collection to follow that of a specified existing collection. Note: Using this parameter
     *                             has consequences for the prototype collection. It can no longer be dropped, before sharding imitating
     *                             collections are dropped. Equally, backups and restores of imitating collections alone will generate
     *                             warnings, which can be overridden, about missing sharding prototype.
     * @return options
     */
    public CollectionCreateOptions distributeShardsLike(final String distributeShardsLike) {
        this.distributeShardsLike = distributeShardsLike;
        return this;
    }

}

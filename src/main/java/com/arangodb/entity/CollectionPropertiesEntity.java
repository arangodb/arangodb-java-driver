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

import java.util.Collection;

/**
 * @author Mark Vollmary
 * @see <a href= "https://www.arangodb.com/docs/stable/http/collection-getting.html#read-properties-of-a-collection">API
 * Documentation</a>
 */
public class CollectionPropertiesEntity extends CollectionEntity {

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    private Boolean doCompact;
    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    private Long journalSize;
    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    private Integer indexBuckets;
    private KeyOptions keyOptions;
    private Long count;
    private Integer numberOfShards;
    private Collection<String> shardKeys;
    private final ReplicationFactor replicationFactor;
    private final MinReplicationFactor minReplicationFactor;
    private Integer writeConcern;
    private String shardingStrategy; // cluster option
    private String smartJoinAttribute; // enterprise option

    public CollectionPropertiesEntity() {
        super();
        replicationFactor = new ReplicationFactor();
        minReplicationFactor = new MinReplicationFactor();
    }

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public Boolean getDoCompact() {
        return doCompact;
    }

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public void setDoCompact(final Boolean doCompact) {
        this.doCompact = doCompact;
    }

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public Long getJournalSize() {
        return journalSize;
    }

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public void setJournalSize(final Long journalSize) {
        this.journalSize = journalSize;
    }


    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public Integer getIndexBuckets() {
        return indexBuckets;
    }

    /**
     * @deprecated MMFiles only
     */
    @Deprecated
    public void setIndexBuckets(final Integer indexBuckets) {
        this.indexBuckets = indexBuckets;
    }

    public KeyOptions getKeyOptions() {
        return keyOptions;
    }

    public void setKeyOptions(final KeyOptions keyOptions) {
        this.keyOptions = keyOptions;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(final Long count) {
        this.count = count;
    }

    /**
     * @return the number of shards of the collection. Only in a cluster setup (else returning null).
     */
    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public void setNumberOfShards(final Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
    }

    /**
     * @return the names of document attributes that are used to determine the target shard for documents.
     * Only in a cluster setup (else returning null).
     */
    public Collection<String> getShardKeys() {
        return shardKeys;
    }

    public void setShardKeys(final Collection<String> shardKeys) {
        this.shardKeys = shardKeys;
    }

    public Integer getReplicationFactor() {
        return replicationFactor.getReplicationFactor();
    }

    public void setReplicationFactor(final Integer replicationFactor) {
        this.replicationFactor.setReplicationFactor(replicationFactor);
    }

    /**
     * @deprecated use {@link #getWriteConcern()} instead
     */
    @Deprecated
    public Integer getMinReplicationFactor() {
        return minReplicationFactor.getMinReplicationFactor();
    }

    /**
     * @deprecated use {@link #setWriteConcern(Integer)} instead
     */
    @Deprecated
    public void setMinReplicationFactor(final Integer minReplicationFactor) {
        this.minReplicationFactor.setMinReplicationFactor(minReplicationFactor);
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(final Integer writeConcern) {
        this.writeConcern = writeConcern;
    }

    /**
     * @return whether the collection is a satellite collection. Only in an enterprise cluster setup (else returning null).
     */
    public Boolean getSatellite() {
        return this.replicationFactor.getSatellite();
    }

    public void setSatellite(final Boolean satellite) {
        this.replicationFactor.setSatellite(satellite);
    }

    public String getShardingStrategy() {
        return shardingStrategy;
    }

    public void setShardingStrategy(String shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }

    public String getSmartJoinAttribute() {
        return smartJoinAttribute;
    }

    public void setSmartJoinAttribute(String smartJoinAttribute) {
        this.smartJoinAttribute = smartJoinAttribute;
    }

}

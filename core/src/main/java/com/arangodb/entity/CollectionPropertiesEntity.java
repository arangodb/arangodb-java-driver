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
import java.util.Objects;

/**
 * @author Mark Vollmary
 */
public final class CollectionPropertiesEntity extends CollectionEntity {

    private Boolean cacheEnabled;
    private String distributeShardsLike;
    private Boolean isDisjoint;
    private Boolean isSmart;
    private KeyOptions keyOptions;
    private Integer numberOfShards;
    private ReplicationFactor replicationFactor;
    private Collection<String> shardKeys;
    private String shardingStrategy; // cluster option
    private String smartGraphAttribute;
    private String smartJoinAttribute;
    private Integer writeConcern;
    private Long count;

    public CollectionPropertiesEntity() {
        super();
    }

    public Boolean getCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(Boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public String getDistributeShardsLike() {
        return distributeShardsLike;
    }

    public void setDistributeShardsLike(String distributeShardsLike) {
        this.distributeShardsLike = distributeShardsLike;
    }

    public Boolean getDisjoint() {
        return isDisjoint;
    }

    public void setDisjoint(Boolean disjoint) {
        isDisjoint = disjoint;
    }

    public Boolean getSmart() {
        return isSmart;
    }

    public void setSmart(Boolean smart) {
        isSmart = smart;
    }

    public KeyOptions getKeyOptions() {
        return keyOptions;
    }

    public void setKeyOptions(KeyOptions keyOptions) {
        this.keyOptions = keyOptions;
    }

    public Integer getNumberOfShards() {
        return numberOfShards;
    }

    public void setNumberOfShards(Integer numberOfShards) {
        this.numberOfShards = numberOfShards;
    }

    public ReplicationFactor getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(ReplicationFactor replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Collection<String> getShardKeys() {
        return shardKeys;
    }

    public void setShardKeys(Collection<String> shardKeys) {
        this.shardKeys = shardKeys;
    }

    public String getShardingStrategy() {
        return shardingStrategy;
    }

    public void setShardingStrategy(String shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }

    public String getSmartGraphAttribute() {
        return smartGraphAttribute;
    }

    public void setSmartGraphAttribute(String smartGraphAttribute) {
        this.smartGraphAttribute = smartGraphAttribute;
    }

    public String getSmartJoinAttribute() {
        return smartJoinAttribute;
    }

    public void setSmartJoinAttribute(String smartJoinAttribute) {
        this.smartJoinAttribute = smartJoinAttribute;
    }

    public Integer getWriteConcern() {
        return writeConcern;
    }

    public void setWriteConcern(Integer writeConcern) {
        this.writeConcern = writeConcern;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectionPropertiesEntity)) return false;
        if (!super.equals(o)) return false;
        CollectionPropertiesEntity that = (CollectionPropertiesEntity) o;
        return Objects.equals(cacheEnabled, that.cacheEnabled) && Objects.equals(distributeShardsLike, that.distributeShardsLike) && Objects.equals(isDisjoint, that.isDisjoint) && Objects.equals(isSmart, that.isSmart) && Objects.equals(keyOptions, that.keyOptions) && Objects.equals(numberOfShards, that.numberOfShards) && Objects.equals(replicationFactor, that.replicationFactor) && Objects.equals(shardKeys, that.shardKeys) && Objects.equals(shardingStrategy, that.shardingStrategy) && Objects.equals(smartGraphAttribute, that.smartGraphAttribute) && Objects.equals(smartJoinAttribute, that.smartJoinAttribute) && Objects.equals(writeConcern, that.writeConcern) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cacheEnabled, distributeShardsLike, isDisjoint, isSmart, keyOptions, numberOfShards, replicationFactor, shardKeys, shardingStrategy, smartGraphAttribute, smartJoinAttribute, writeConcern, count);
    }
}

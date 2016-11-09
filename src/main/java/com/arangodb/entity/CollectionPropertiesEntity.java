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
 * @author Mark - mark at arangodb.com
 *
 * @see <a href= "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#read-properties-of-a-collection">API
 *      Documentation</a>
 */
public class CollectionPropertiesEntity extends CollectionEntity {

	private Boolean doCompact;
	private Long journalSize;
	private Integer indexBuckets;
	private KeyOptions keyOptions;
	private Long count;
	private Integer numberOfShards;
	private Collection<String> shardKeys;
	private Integer replicationFactor;

	public CollectionPropertiesEntity() {
		super();
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public void setDoCompact(final Boolean doCompact) {
		this.doCompact = doCompact;
	}

	public Long getJournalSize() {
		return journalSize;
	}

	public void setJournalSize(final Long journalSize) {
		this.journalSize = journalSize;
	}

	public Integer getIndexBuckets() {
		return indexBuckets;
	}

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
	 * @return contains the names of document attributes that are used to determine the target shard for documents. Only
	 *         in a cluster setup
	 */
	public Integer getNumberOfShards() {
		return numberOfShards;
	}

	public void setNumberOfShards(final Integer numberOfShards) {
		this.numberOfShards = numberOfShards;
	}

	/**
	 * @return the number of shards of the collection. Only in a cluster setup.
	 */
	public Collection<String> getShardKeys() {
		return shardKeys;
	}

	public void setShardKeys(final Collection<String> shardKeys) {
		this.shardKeys = shardKeys;
	}

	public Integer getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(final Integer replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

}

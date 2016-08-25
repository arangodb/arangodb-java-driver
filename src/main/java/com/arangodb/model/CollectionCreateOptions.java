package com.arangodb.model;

import com.arangodb.entity.CollectionType;
import com.arangodb.entity.KeyOptions;
import com.arangodb.entity.KeyType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionCreateOptions {

	private String name;
	private Long journalSize;
	private KeyOptions keyOptions;
	private Boolean waitForSync;
	private Boolean doCompact;
	private Boolean isVolatile;
	private String[] shardKeys;
	private Integer numberOfShards;
	private Boolean isSystem;
	private CollectionType type;
	private Integer indexBuckets;

	protected String getName() {
		return name;
	}

	protected CollectionCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

	public Long getJournalSize() {
		return journalSize;
	}

	public CollectionCreateOptions journalSize(final Long journalSize) {
		this.journalSize = journalSize;
		return this;
	}

	public KeyOptions getKeyOptions() {
		return keyOptions;
	}

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

	public CollectionCreateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public CollectionCreateOptions doCompact(final Boolean doCompact) {
		this.doCompact = doCompact;
		return this;
	}

	public Boolean getIsVolatile() {
		return isVolatile;
	}

	public CollectionCreateOptions isVolatile(final Boolean isVolatile) {
		this.isVolatile = isVolatile;
		return this;
	}

	public String[] getShardKeys() {
		return shardKeys;
	}

	public CollectionCreateOptions shardKeys(final String[] shardKeys) {
		this.shardKeys = shardKeys;
		return this;
	}

	public Integer getNumberOfShards() {
		return numberOfShards;
	}

	public CollectionCreateOptions numberOfShards(final Integer numberOfShards) {
		this.numberOfShards = numberOfShards;
		return this;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public CollectionCreateOptions isSystem(final Boolean isSystem) {
		this.isSystem = isSystem;
		return this;
	}

	public CollectionType getType() {
		return type;
	}

	public CollectionCreateOptions type(final CollectionType type) {
		this.type = type;
		return this;
	}

	public Integer getIndexBuckets() {
		return indexBuckets;
	}

	public CollectionCreateOptions indexBuckets(final Integer indexBuckets) {
		this.indexBuckets = indexBuckets;
		return this;
	}

}

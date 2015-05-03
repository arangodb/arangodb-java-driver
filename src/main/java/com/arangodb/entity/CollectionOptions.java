package com.arangodb.entity;

import java.util.List;

/**
 *
 * Convenience object for collection creation.
 *
 * @see com.arangodb.entity.CollectionEntity author fbartels -
 *      f.bartels@triagens.de
 */
public class CollectionOptions {

	/**
	 * If true each write operation is synchronised to disk before the server
	 * sends a response
	 */
	private Boolean waitForSync;

	/**
	 * Whether or not the collection will be compacted.
	 */
	private Boolean doCompact;

	/**
	 * The maximal size setting for journals / datafiles.
	 */
	private Integer journalSize;

	/**
	 * If true the collection is a system collection
	 */
	private Boolean isSystem;

	/**
	 * If true then the collection data will be kept in memory only and ArangoDB
	 * will not write or sync the data to disk.
	 */
	private Boolean isVolatile;

	/**
	 * The collections type, either EDGE or DOCUMENT
	 */
	private CollectionType type;

	/**
	 * The collection key options
	 * 
	 * @see com.arangodb.entity.CollectionKeyOption
	 */
	private CollectionKeyOption keyOptions;

	/**
	 * in a cluster, this value determines the number of shards to create for
	 * the collection. In a single server setup, this option is meaningless.
	 */
	private int numberOfShards;

	/**
	 * in a cluster, this attribute determines which document attributes are
	 * used to determine the target shard for documents. Documents are sent to
	 * shards based on the values of their shard key attributes. The values of
	 * all shard key attributes in a document are hashed, and the hash value is
	 * used to determine the target shard.
	 */
	private List<String> shardKeys;

	public CollectionOptions() {
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public CollectionOptions setWaitForSync(Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public CollectionOptions setDoCompact(Boolean doCompact) {
		this.doCompact = doCompact;
		return this;
	}

	public Integer getJournalSize() {
		return journalSize;
	}

	public CollectionOptions setJournalSize(Integer journalSize) {
		this.journalSize = journalSize;
		return this;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public CollectionOptions setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
		return this;
	}

	public Boolean getIsVolatile() {
		return isVolatile;
	}

	public CollectionOptions setIsVolatile(Boolean isVolatile) {
		this.isVolatile = isVolatile;
		return this;
	}

	public CollectionType getType() {
		return type;
	}

	public CollectionOptions setType(CollectionType type) {
		this.type = type;
		return this;
	}

	public CollectionKeyOption getKeyOptions() {
		return keyOptions;
	}

	public CollectionOptions setKeyOptions(CollectionKeyOption keyOptions) {
		this.keyOptions = keyOptions;
		return this;
	}

	public int getNumberOfShards() {
		return numberOfShards;
	}

	public CollectionOptions setNumberOfShards(int numberOfShards) {
		this.numberOfShards = numberOfShards;
		return this;
	}

	public List<String> getShardKeys() {
		return shardKeys;
	}

	public CollectionOptions setShardKeys(List<String> shardKeys) {
		this.shardKeys = shardKeys;
		return this;
	}

}

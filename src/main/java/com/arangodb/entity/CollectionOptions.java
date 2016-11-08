package com.arangodb.entity;

import java.util.List;

/**
 *
 * Convenience object for collection creation.
 *
 * @see com.arangodb.entity.CollectionEntity
 * 
 * @author Florian Bartels
 * 
 */
public class CollectionOptions {

	/**
	 * If true each write operation is synchronised to disk before the server sends a response
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
	 * If true then the collection data will be kept in memory only and ArangoDB will not write or sync the data to
	 * disk.
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
	 * in a cluster, this value determines the number of shards to create for the collection. In a single server setup,
	 * this option is meaningless.
	 */
	private int numberOfShards;

	/**
	 * in a cluster, this attribute determines which document attributes are used to determine the target shard for
	 * documents. Documents are sent to shards based on the values of their shard key attributes. The values of all
	 * shard key attributes in a document are hashed, and the hash value is used to determine the target shard.
	 */
	private List<String> shardKeys;

	/**
	 * (The default is 1): in a cluster, this attribute determines how many copies of each shard are kept on different
	 * DBServers. The value 1 means that only one copy (no synchronous replication) is kept. A value of k means that k-1
	 * replicas are kept. Any two copies reside on different DBServers. Replication between them is synchronous, that
	 * is, every write operation to the "leader" copy will be replicated to all "follower" replicas, before the write
	 * operation is reported successful. If a server fails, this is detected automatically and one of the servers
	 * holding copies take over, usually without an error being reported.
	 */
	private Integer replicationFactor;

	public CollectionOptions() {
		// do nothing here
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public CollectionOptions setWaitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getDoCompact() {
		return doCompact;
	}

	public CollectionOptions setDoCompact(final Boolean doCompact) {
		this.doCompact = doCompact;
		return this;
	}

	public Integer getJournalSize() {
		return journalSize;
	}

	public CollectionOptions setJournalSize(final Integer journalSize) {
		this.journalSize = journalSize;
		return this;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public CollectionOptions setIsSystem(final Boolean isSystem) {
		this.isSystem = isSystem;
		return this;
	}

	public Boolean getIsVolatile() {
		return isVolatile;
	}

	public CollectionOptions setIsVolatile(final Boolean isVolatile) {
		this.isVolatile = isVolatile;
		return this;
	}

	public CollectionType getType() {
		return type;
	}

	public CollectionOptions setType(final CollectionType type) {
		this.type = type;
		return this;
	}

	public CollectionKeyOption getKeyOptions() {
		return keyOptions;
	}

	public CollectionOptions setKeyOptions(final CollectionKeyOption keyOptions) {
		this.keyOptions = keyOptions;
		return this;
	}

	public int getNumberOfShards() {
		return numberOfShards;
	}

	public CollectionOptions setNumberOfShards(final int numberOfShards) {
		this.numberOfShards = numberOfShards;
		return this;
	}

	public List<String> getShardKeys() {
		return shardKeys;
	}

	public CollectionOptions setShardKeys(final List<String> shardKeys) {
		this.shardKeys = shardKeys;
		return this;
	}

	public Integer getReplicationFactor() {
		return replicationFactor;
	}

	public CollectionOptions setReplicationFactor(final Integer replicationFactor) {
		this.replicationFactor = replicationFactor;
		return this;
	}

}

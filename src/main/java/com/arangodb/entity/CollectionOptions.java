package com.arangodb.entity;

import java.util.List;

/**
 *
 * Convenience object for collection creation.
 *
 * @see com.arangodb.entity.CollectionEntity
 * author fbartels - f.bartels@triagens.de
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
   * If true then the collection data will be kept in memory only and ArangoDB will not write or sync the data to disk.
   */
  private Boolean isVolatile;

  /**
   * The collections type, either EDGE or DOCUMENT
   */
  private CollectionType type;

  /**
   * The collection key options
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


  public CollectionOptions() {
  }

  public Boolean getWaitForSync() {
    return waitForSync;
  }

  public void setWaitForSync(Boolean waitForSync) {
    this.waitForSync = waitForSync;
  }

  public Boolean getDoCompact() {
    return doCompact;
  }

  public void setDoCompact(Boolean doCompact) {
    this.doCompact = doCompact;
  }

  public Integer getJournalSize() {
    return journalSize;
  }

  public void setJournalSize(Integer journalSize) {
    this.journalSize = journalSize;
  }

  public Boolean getIsSystem() {
    return isSystem;
  }

  public void setIsSystem(Boolean isSystem) {
    this.isSystem = isSystem;
  }

  public Boolean getIsVolatile() {
    return isVolatile;
  }

  public void setIsVolatile(Boolean isVolatile) {
    this.isVolatile = isVolatile;
  }

  public CollectionType getType() {
    return type;
  }

  public void setType(CollectionType type) {
    this.type = type;
  }

  public CollectionKeyOption getKeyOptions() {
    return keyOptions;
  }

  public void setKeyOptions(CollectionKeyOption keyOptions) {
    this.keyOptions = keyOptions;
  }

  public int getNumberOfShards() {
    return numberOfShards;
  }

  public void setNumberOfShards(int numberOfShards) {
    this.numberOfShards = numberOfShards;
  }

  public List<String> getShardKeys() {
    return shardKeys;
  }

  public void setShardKeys(List<String> shardKeys) {
    this.shardKeys = shardKeys;
  }

}

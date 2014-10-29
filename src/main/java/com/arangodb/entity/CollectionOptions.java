package com.arangodb.entity;

import java.util.List;

/**
 * Created by fbartels on 10/28/14.
 */
public class CollectionOptions {

  private Boolean waitForSync;

  private Boolean doCompact;

  private Integer journalSize;

  private Boolean isSystem;

  private Boolean isVolatile;

  private CollectionType type;

  private CollectionKeyOption keyOptions;

  private int numberOfShards;

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

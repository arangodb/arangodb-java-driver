/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.entity;

import java.io.Serializable;

/**
 * A representation of an ArangoDB collection
 *
 * @author tamtam180 - kirscheless at gmail.com
 * 
 */
public class CollectionEntity extends BaseEntity {

  /**
   * The collections name
   */
  String name;

  /**
   * The unique id of the collection
   */
  long id;

  /**
   * The collections type, either EDGE or DOCUMENT
   */
  CollectionType type;

  /**
   * The state of the collection
   */
  CollectionStatus status;

  /**
   * If true each write operation is synchronised to disk before the server sends a response
   */
  Boolean waitForSync;

  /**
   * If true the collection is a system collection
   */
  Boolean isSystem;

  /**
   * If true then the collection data will be kept in memory only and ArangoDB will not write or sync the data to disk.
   */
  Boolean isVolatile;

  /**
   * The maximal size setting for journals / datafiles.
   */
  long journalSize;

  /**
   * The amount of documents in the collection
   */
  long count;

  /**
   * The collections revision
   */
  long revision;

  /**
   * The collection figures
   * @see com.arangodb.entity.CollectionEntity.Figures
   * @see com.arangodb.ArangoDriver#getCollectionFigures(long)
   * @see com.arangodb.ArangoDriver#getCollectionFigures(String)
   */
  Figures figures;

  /**
   * The collection key options
   * @see com.arangodb.entity.CollectionKeyOption
   */
  CollectionKeyOption keyOptions;

  /**
   * The checksum of the collection
   * @see com.arangodb.ArangoDriver#getCollectionChecksum(String, Boolean, Boolean)
   */
  long checksum;

  /**
   * Whether or not the collection will be compacted.
   */
  Boolean doCompact;

  public String getName() {
    return name;
  }

  public long getId() {
    return id;
  }

  public CollectionType getType() {
    return type;
  }

  public CollectionStatus getStatus() {
    return status;
  }

  public long getChecksum() {
    return checksum;
  }

  public void setIsSystem(Boolean isSystem) {
    this.isSystem = isSystem;
  }

  public void setIsVolatile(Boolean isVolatile) {
    this.isVolatile = isVolatile;
  }

  public void setChecksum(long checksum) {
    this.checksum = checksum;
  }

  public Boolean getWaitForSync() {
    return waitForSync;
  }

  public Boolean getIsSystem() {
    return isSystem;
  }

  public Boolean getIsVolatile() {
    return isVolatile;
  }

  public long getJournalSize() {
    return journalSize;
  }

  public long getCount() {
    return count;
  }

  public Figures getFigures() {
    return figures;
  }

  public CollectionKeyOption getKeyOptions() {
    return keyOptions;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setType(CollectionType type) {
    this.type = type;
  }

  public void setStatus(CollectionStatus status) {
    this.status = status;
  }

  public void setWaitForSync(Boolean waitForSync) {
    this.waitForSync = waitForSync;
  }

  public void setSystem(Boolean isSystem) {
    this.isSystem = isSystem;
  }

  public void setVolatile(Boolean isVolatile) {
    this.isVolatile = isVolatile;
  }

  public void setJournalSize(long journalSize) {
    this.journalSize = journalSize;
  }

  public void setCount(long count) {
    this.count = count;
  }

  public void setFigures(Figures figures) {
    this.figures = figures;
  }

  public void setKeyOptions(CollectionKeyOption keyOptions) {
    this.keyOptions = keyOptions;
  }

  public long getRevision() {
    return revision;
  }

  public void setRevision(long revision) {
    this.revision = revision;
  }

  public Boolean getDoCompact() {
    return doCompact;
  }

  public void setDoCompact(Boolean doCompact) {
    this.doCompact = doCompact;
  }

  /**
   * additional statistical information about the collection.
   */
  public static class Figures implements Serializable {

    /**
     * The number of curretly active documents in all datafiles and journals of the collection. Documents that are
     * contained in the write-ahead log only are not reported in this figure.
     */
    long aliveCount;

    /**
     * The total size in bytes used by all active documents of the collection. Documents that are contained in the
     * write-ahead log only are not reported in this figure.
     */
    long aliveSize;

    /**
     * The number of dead documents. This includes document versions that have been deleted or replaced by a newer
     * version. Documents deleted or replaced that are contained the write-ahead log only are not reported in this figure.
     */
    long deadCount;

    /**
     * The total size in bytes used by all dead documents.
     */
    long deadSize;

    /**
     * The total number of deletion markers. Deletion markers only contained in the write-ahead log are not reporting
     * in this figure.
     */
    long deadDeletion;

    /**
     * The number of datafiles.
     */
    long datafileCount;

    /**
     * The total filesize of datafiles (in bytes).
     */
    long datafileFileSize;

    /**
     * The number of journal files.
     */
    long journalsCount;

    /**
     * The total filesize of all journal files (in bytes).
     */
    long journalsFileSize;

    /**
     * The number of compactor files.
     */
    long compactorsCount;

    /**
     * The total filesize of all compactor files (in bytes).
     */
    long compactorsFileSize;

    /**
     * The number of shape files. This value is deprecated and kept for compatibility reasons only. The value will
     * always be 0 since ArangoDB 2.0 and higher. *figures.shapefiles.fileSize: The total filesize of the shape files.
     * This value is deprecated and kept for compatibility reasons only. The value will always be 0 in ArangoDB 2.0 and
     * higher.
     */
    long shapefilesCount;

    /**
     * The total size of all shapes (in bytes). This includes shapes that are not in use anymore. Shapes that are
     * contained in the write-ahead log only are not reported in this figure.
     */
    long shapefilesFileSize;

    /**
     * The total number of shapes used in the collection. This includes shapes that are not in use anymore. Shapes that
     * are contained in the write-ahead log only are not reported in this figure.
     */
    long shapesCount;

    /**
     * The total number of attributes used in the collection. Note: the value includes data of attributes that are not
     * in use anymore. Attributes that are contained in the write-ahead log only are not reported in this figure.
     */
    long attributesCount;

    /**
     * The total number of indexes defined for the collection, including the pre-defined indexes (e.g. primary index).
     */
    long indexesCount;

    /**
     * The total memory allocated for indexes in bytes.
     */
    long indexesSize;

    /**
     * The tick of the last marker that was stored in a journal of the collection. This might be 0 if the collection
     * does not yet have a journal.
     */
    long lastTick;

    /**
     * The number of markers in the write-ahead log for this collection that have not been transferred to journals or
     * datafiles.
     */
    long uncollectedLogfileEntries;

    public long getAliveCount() {
      return aliveCount;
    }

    public long getAliveSize() {
      return aliveSize;
    }

    public long getDeadCount() {
      return deadCount;
    }

    public long getDeadSize() {
      return deadSize;
    }

    public long getDeadDeletion() {
      return deadDeletion;
    }

    public long getDatafileCount() {
      return datafileCount;
    }

    public long getDatafileFileSize() {
      return datafileFileSize;
    }

    public long getJournalsCount() {
      return journalsCount;
    }

    public long getJournalsFileSize() {
      return journalsFileSize;
    }

    public long getCompactorsCount() {
      return compactorsCount;
    }

    public long getCompactorsFileSize() {
      return compactorsFileSize;
    }

    public long getShapefilesCount() {
      return shapefilesCount;
    }

    public long getShapefilesFileSize() {
      return shapefilesFileSize;
    }

    public long getShapesCount() {
      return shapesCount;
    }

    public long getAttributesCount() {
      return attributesCount;
    }

    public void setAliveCount(long aliveCount) {
      this.aliveCount = aliveCount;
    }

    public void setAliveSize(long aliveSize) {
      this.aliveSize = aliveSize;
    }

    public void setDeadCount(long deadCount) {
      this.deadCount = deadCount;
    }

    public void setDeadSize(long deadSize) {
      this.deadSize = deadSize;
    }

    public void setDeadDeletion(long deadDeletion) {
      this.deadDeletion = deadDeletion;
    }

    public void setDatafileCount(long datafileCount) {
      this.datafileCount = datafileCount;
    }

    public void setDatafileFileSize(long datafileFileSize) {
      this.datafileFileSize = datafileFileSize;
    }

    public void setJournalsCount(long journalsCount) {
      this.journalsCount = journalsCount;
    }

    public void setJournalsFileSize(long journalsFileSize) {
      this.journalsFileSize = journalsFileSize;
    }

    public void setCompactorsCount(long compactorsCount) {
      this.compactorsCount = compactorsCount;
    }

    public void setCompactorsFileSize(long compactorsFileSize) {
      this.compactorsFileSize = compactorsFileSize;
    }

    public void setShapefilesCount(long shapefilesCount) {
      this.shapefilesCount = shapefilesCount;
    }

    public void setShapefilesFileSize(long shapefilesFileSize) {
      this.shapefilesFileSize = shapefilesFileSize;
    }

    public void setShapesCount(long shapesCount) {
      this.shapesCount = shapesCount;
    }

    public void setAttributesCount(long attributesCount) {
      this.attributesCount = attributesCount;
    }

    public long getIndexesCount() {
      return indexesCount;
    }

    public void setIndexesCount(long indexesCount) {
      this.indexesCount = indexesCount;
    }

    public long getIndexesSize() {
      return indexesSize;
    }

    public void setIndexesSize(long indexesSize) {
      this.indexesSize = indexesSize;
    }

    public long getLastTick() {
      return lastTick;
    }

    public void setLastTick(long lastTick) {
      this.lastTick = lastTick;
    }

    public long getUncollectedLogfileEntries() {
      return uncollectedLogfileEntries;
    }

    public void setUncollectedLogfileEntries(long uncollectedLogfileEntries) {
      this.uncollectedLogfileEntries = uncollectedLogfileEntries;
    }

  }

}

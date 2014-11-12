/*
 * Copyright (C) 2012,2013 tamtam180
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

import java.util.List;

/**
 * Result of a full data synchronization from a remote endpoint into the local ArangoDB database.
 *
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ReplicationSyncEntity extends BaseEntity {

  /**
   * a list of collections that were transferred from the endpoint
   */
  List<CollectionEntity> collections;

  /**
   *  the last log tick on the endpoint at the time the transfer was started. Use this value as the from value when
   *  starting the continuous synchronization later.
   */
  long lastLogTick;
  
  public List<CollectionEntity> getCollections() {
    return collections;
  }
  public long getLastLogTick() {
    return lastLogTick;
  }
  public void setCollections(List<CollectionEntity> collections) {
    this.collections = collections;
  }
  public void setLastLogTick(long lastLogTick) {
    this.lastLogTick = lastLogTick;
  }
  
}

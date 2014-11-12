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

import java.util.ArrayList;
import java.util.List;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class TransactionEntity extends BaseEntity {

  public class ReadWriteCollections {

    List<String> read = new ArrayList<String>();

    List<String> write = new ArrayList<String>();
  }

  ReadWriteCollections collections = new ReadWriteCollections();

  String action;

  Boolean waitForSync;

  int lockTimeout;

  Object params;

  public TransactionEntity(String action) {
    this.action = action;
  }

  public ReadWriteCollections getCollections() {
    return collections;
  }

  public void setCollections(ReadWriteCollections collections) {
    this.collections = collections;
  }

  public void addReadCollection(String collection) {
    this.collections.read.add(collection);
  }

  public void addWriteCollection(String collection) {
    this.collections.write.add(collection);
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Boolean getWaitForSync() {
    return waitForSync;
  }

  public void setWaitForSync(Boolean waitForSync) {
    this.waitForSync = waitForSync;
  }

  public int getLockTimeout() {
    return lockTimeout;
  }

  public void setLockTimeout(int lockTimeout) {
    this.lockTimeout = lockTimeout;
  }

  public Object getParams() {
    return params;
  }

  public void setParams(Object params) {
    this.params = params;
  }
}

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

package com.arangodb.impl;

import com.arangodb.ArangoConfigure;
import com.arangodb.InternalCursorDriver;
import com.arangodb.InternalKVSDriver;
import com.arangodb.InternalTransactionDriver;
import com.arangodb.http.HttpManager;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ImplFactory {
  public static InternalCursorDriverImpl createCursorDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalCursorDriverImpl(configure, httpManager);
  }
  public static InternalCollectionDriverImpl createCollectionDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalCollectionDriverImpl(configure, httpManager);
  }
  public static InternalDocumentDriverImpl createDocumentDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalDocumentDriverImpl(configure, httpManager);
  }
  public static InternalJobsDriverImpl createJobsDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalJobsDriverImpl(configure, httpManager);
  }
  public static InternalTransactionDriver createTransactionDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalTransactionDriverImpl(configure, httpManager);
  }
  public static InternalKVSDriver createKVSDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalKVSDriverImpl(configure, httpManager);
  }
  public static InternalSimpleDriverImpl createSimpleDriver(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
    return new InternalSimpleDriverImpl(configure, cursorDriver, httpManager);
  }
  public static InternalIndexDriverImpl createIndexDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalIndexDriverImpl(configure, httpManager);
  }
  public static InternalAdminDriverImpl createAdminDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalAdminDriverImpl(configure, httpManager);
  }
  public static InternalAqlFunctionsDriverImpl createAqlFunctionsDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalAqlFunctionsDriverImpl(configure, httpManager);
  }
  public static InternalBatchDriverImpl createBatchDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalBatchDriverImpl(configure, httpManager);
  }
  public static InternalUsersDriverImpl createUsersDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalUsersDriverImpl(configure, httpManager);
  }
  public static InternalImportDriverImpl createImportDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalImportDriverImpl(configure, httpManager);
  }
  public static InternalDatabaseDriverImpl createDatabaseDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalDatabaseDriverImpl(configure, httpManager);
  }
  public static InternalEndpointDriverImpl createEndpointDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalEndpointDriverImpl(configure, httpManager);
  }
  public static InternalReplicationDriverImpl createReplicationDriver(ArangoConfigure configure, HttpManager httpManager) {
    return new InternalReplicationDriverImpl(configure, httpManager);
  }
  public static InternalGraphDriverImpl createGraphDriver(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
    return new InternalGraphDriverImpl(configure, cursorDriver, httpManager);
  }
  public static InternalEdgeDriverImpl createEdgeDriver(ArangoConfigure configure, InternalCursorDriver cursorDriver, HttpManager httpManager) {
    return new InternalEdgeDriverImpl(configure, cursorDriver, httpManager);
  }
}

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

package com.arangodb;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.arangodb.entity.AdminLogEntity;
import com.arangodb.entity.AqlFunctionsEntity;
import com.arangodb.entity.ArangoUnixTime;
import com.arangodb.entity.ArangoVersion;
import com.arangodb.entity.BatchResponseEntity;
import com.arangodb.entity.BooleanResultEntity;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionsEntity;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.Direction;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.DocumentResultEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.Endpoint;
import com.arangodb.entity.FilterCondition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.entity.ImportResultEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;
import com.arangodb.entity.Policy;
import com.arangodb.entity.ReplicationApplierConfigEntity;
import com.arangodb.entity.ReplicationApplierStateEntity;
import com.arangodb.entity.ReplicationInventoryEntity;
import com.arangodb.entity.ReplicationLoggerConfigEntity;
import com.arangodb.entity.ReplicationLoggerStateEntity;
import com.arangodb.entity.ReplicationSyncEntity;
import com.arangodb.entity.RestrictType;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.entity.StatisticsDescriptionEntity;
import com.arangodb.entity.StatisticsEntity;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.http.BatchHttpManager;
import com.arangodb.http.BatchPart;
import com.arangodb.http.HttpManager;
import com.arangodb.http.InvocationHandlerImpl;
import com.arangodb.impl.ImplFactory;
import com.arangodb.impl.InternalBatchDriverImpl;
import com.arangodb.util.DumpHandler;
import com.arangodb.util.ResultSetUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 */
public class ArangoDriver extends BaseArangoDriver {

  // TODO Cas Operation as eTAG
  // TODO Should fixed a Double check args.
  // TODO Null check httpResponse.

  // TODO コマンド式の実装に変更する。引数が増える度にメソッド数が爆発するのと、そうしないとバッチ処理も上手く書けないため。
  // driver.execute(createDocumentCommand)
  // class createDocumentCommand extends Command { }

  private ArangoConfigure configure;
  private BatchHttpManager httpManager;
  private String baseUrl;

  private InternalCursorDriver cursorDriver;
  private InternalBatchDriverImpl batchDriver;
  private InternalCollectionDriver collectionDriver;
  private InternalDocumentDriver documentDriver;
  // private InternalKVSDriverImpl kvsDriver;
  private InternalIndexDriver indexDriver;
  // private InternalEdgeDriverImpl edgeDriver;
  private InternalAdminDriver adminDriver;
  private InternalJobsDriver jobsDriver;
  private InternalAqlFunctionsDriver aqlFunctionsDriver;
  private InternalSimpleDriver simpleDriver;
  private InternalUsersDriver usersDriver;
  private InternalImportDriver importDriver;
  private InternalDatabaseDriver databaseDriver;
  private InternalEndpointDriver endpointDriver;
  private InternalReplicationDriver replicationDriver;
  private InternalGraphDriver graphDriver;

  private String database;

  public ArangoDriver(ArangoConfigure configure) {
    this(configure, null);
  }

  public ArangoDriver(ArangoConfigure configure, String database) {

    this.database = configure.getDefaultDatabase();
    if (database != null) {
      this.database = database;
    }

    this.configure = configure;
    this.httpManager = configure.getHttpManager();
    this.createModuleDrivers(false);
    this.baseUrl = configure.getBaseUrl();
  }

  private void createModuleDrivers(boolean createProxys) {
    if (!createProxys) {
      this.cursorDriver = ImplFactory.createCursorDriver(configure, this.httpManager);
      this.batchDriver = ImplFactory.createBatchDriver(configure, this.httpManager);
      this.collectionDriver = ImplFactory.createCollectionDriver(configure, this.httpManager);
      this.documentDriver = ImplFactory.createDocumentDriver(configure, this.httpManager);
      this.indexDriver = ImplFactory.createIndexDriver(configure, this.httpManager);
      this.adminDriver = ImplFactory.createAdminDriver(configure, this.httpManager);
      this.aqlFunctionsDriver = ImplFactory.createAqlFunctionsDriver(configure, this.httpManager);
      this.simpleDriver = ImplFactory.createSimpleDriver(configure, cursorDriver, this.httpManager);
      this.usersDriver = ImplFactory.createUsersDriver(configure, this.httpManager);
      this.importDriver = ImplFactory.createImportDriver(configure, this.httpManager);
      this.databaseDriver = ImplFactory.createDatabaseDriver(configure, this.httpManager);
      this.endpointDriver = ImplFactory.createEndpointDriver(configure, this.httpManager);
      this.replicationDriver = ImplFactory.createReplicationDriver(configure, this.httpManager);
      this.graphDriver = ImplFactory.createGraphDriver(configure, cursorDriver, this.httpManager);
    } else {
      this.cursorDriver = (InternalCursorDriver) Proxy.newProxyInstance(
        InternalCursorDriver.class.getClassLoader(),
        new Class<?>[] { InternalCursorDriver.class },
        new InvocationHandlerImpl(this.cursorDriver));
      this.collectionDriver = (InternalCollectionDriver) Proxy.newProxyInstance(
        InternalCollectionDriver.class.getClassLoader(),
        new Class<?>[] { InternalCollectionDriver.class },
        new InvocationHandlerImpl(this.collectionDriver));
      this.documentDriver = (InternalDocumentDriver) Proxy.newProxyInstance(
        InternalDocumentDriver.class.getClassLoader(),
        new Class<?>[] { InternalDocumentDriver.class },
        new InvocationHandlerImpl(this.documentDriver));
      this.indexDriver = (InternalIndexDriver) Proxy.newProxyInstance(
        InternalIndexDriver.class.getClassLoader(),
        new Class<?>[] { InternalIndexDriver.class },
        new InvocationHandlerImpl(this.indexDriver));
      this.adminDriver = (InternalAdminDriver) Proxy.newProxyInstance(
        InternalAdminDriver.class.getClassLoader(),
        new Class<?>[] { InternalAdminDriver.class },
        new InvocationHandlerImpl(this.adminDriver));
      this.aqlFunctionsDriver = (InternalAqlFunctionsDriver) Proxy.newProxyInstance(
        InternalAqlFunctionsDriver.class.getClassLoader(),
        new Class<?>[] { InternalAqlFunctionsDriver.class },
        new InvocationHandlerImpl(this.aqlFunctionsDriver));
      this.simpleDriver = (InternalSimpleDriver) Proxy.newProxyInstance(
        InternalSimpleDriver.class.getClassLoader(),
        new Class<?>[] { InternalSimpleDriver.class },
        new InvocationHandlerImpl(this.simpleDriver));
      this.usersDriver = (InternalUsersDriver) Proxy.newProxyInstance(
        InternalUsersDriver.class.getClassLoader(),
        new Class<?>[] { InternalUsersDriver.class },
        new InvocationHandlerImpl(this.usersDriver));
      this.importDriver = (InternalImportDriver) Proxy.newProxyInstance(
        InternalImportDriver.class.getClassLoader(),
        new Class<?>[] { InternalImportDriver.class },
        new InvocationHandlerImpl(this.importDriver));
      this.databaseDriver = (InternalDatabaseDriver) Proxy.newProxyInstance(
        InternalDatabaseDriver.class.getClassLoader(),
        new Class<?>[] { InternalDatabaseDriver.class },
        new InvocationHandlerImpl(this.databaseDriver));
      this.endpointDriver = (InternalEndpointDriver) Proxy.newProxyInstance(
        InternalEndpointDriver.class.getClassLoader(),
        new Class<?>[] { InternalEndpointDriver.class },
        new InvocationHandlerImpl(this.endpointDriver));
      this.replicationDriver = (InternalReplicationDriver) Proxy.newProxyInstance(
        InternalReplicationDriver.class.getClassLoader(),
        new Class<?>[] { InternalReplicationDriver.class },
        new InvocationHandlerImpl(this.replicationDriver));
      this.graphDriver = (InternalGraphDriver) Proxy.newProxyInstance(
        InternalGraphDriver.class.getClassLoader(),
        new Class<?>[] { InternalGraphDriver.class },
        new InvocationHandlerImpl(this.graphDriver));
    }
  }

  public void startBatchMode() throws ArangoException {
    if (this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is already active.");
    }
    this.httpManager.setBatchModeActive(true);
    this.createModuleDrivers(true);

  }

  public void startAsyncMode(boolean fireAndForget) throws ArangoException {
    if (this.httpManager.getHttpMode().equals(HttpManager.HttpMode.ASYNC)
        || this.httpManager.getHttpMode().equals(HttpManager.HttpMode.FIREANDFORGET)) {
      throw new ArangoException("Arango driver already set to asynchronous mode.");
    }
    HttpManager.HttpMode mode = fireAndForget ? HttpManager.HttpMode.FIREANDFORGET : HttpManager.HttpMode.ASYNC;
    this.httpManager.setHttpMode(mode);
  }

  public void stopAsyncMode() throws ArangoException {
    if (this.httpManager.getHttpMode().equals(HttpManager.HttpMode.SYNC)) {
      throw new ArangoException("Arango driver already set to synchronous mode.");
    }
    this.httpManager.setHttpMode(HttpManager.HttpMode.SYNC);
  }

  public DefaultEntity executeBatch() throws ArangoException {
    if (!this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is not active.");
    }
    List<BatchPart> callStack = this.httpManager.getCallStack();
    this.cancelBatchMode();
    DefaultEntity result = this.batchDriver.executeBatch(callStack, this.getDefaultDatabase());
    return result;
  }

  public <T> T getBatchResponseByRequestId(String requestId) throws ArangoException {
    BatchResponseEntity batchResponseEntity = this.batchDriver.getBatchResponseListEntity().getResponseFromRequestId(
      requestId);
    try {
      this.httpManager.setPreDefinedResponse(batchResponseEntity.getHttpResponseEntity());
      return (T) batchResponseEntity
          .getInvocationObject()
          .getMethod()
          .invoke(
            batchResponseEntity.getInvocationObject().getArangoDriver(),
            batchResponseEntity.getInvocationObject().getArgs());
    } catch (Exception e) {
      throw new ArangoException(e);
    }
  }

  public void cancelBatchMode() throws ArangoException {
    if (!this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is not active.");
    }
    this.httpManager.setBatchModeActive(false);
    this.createModuleDrivers(false);
  }

  public String getDefaultDatabase() {
    return database;
  }

  public void setDefaultDatabase(String database) {
    this.database = database;
  }

  // ---------------------------------------- start of collection
  // ----------------------------------------

  public CollectionEntity createCollection(String name) throws ArangoException {
    return collectionDriver.createCollection(getDefaultDatabase(), name, new CollectionOptions());
  }

  public CollectionEntity createCollection(String name, CollectionOptions collectionOptions) throws ArangoException {
    return collectionDriver.createCollection(getDefaultDatabase(), name, collectionOptions);
  }

  public CollectionEntity getCollection(long id) throws ArangoException {
    return getCollection(String.valueOf(id));
  }

  public CollectionEntity getCollection(String name) throws ArangoException {
    return collectionDriver.getCollection(getDefaultDatabase(), name);
  }

  public CollectionEntity getCollectionProperties(long id) throws ArangoException {
    return getCollectionProperties(String.valueOf(id));
  }

  public CollectionEntity getCollectionProperties(String name) throws ArangoException {
    return collectionDriver.getCollectionProperties(getDefaultDatabase(), name);
  }

  public CollectionEntity getCollectionRevision(long id) throws ArangoException {
    return getCollectionRevision(String.valueOf(id));
  }

  public CollectionEntity getCollectionRevision(String name) throws ArangoException {
    return collectionDriver.getCollectionRevision(getDefaultDatabase(), name);
  }

  public CollectionEntity getCollectionCount(long id) throws ArangoException {
    return getCollectionCount(String.valueOf(id));
  }

  public CollectionEntity getCollectionCount(String name) throws ArangoException {
    return collectionDriver.getCollectionCount(getDefaultDatabase(), name);
  }

  public CollectionEntity getCollectionFigures(long id) throws ArangoException {
    return getCollectionFigures(String.valueOf(id));
  }

  public CollectionEntity getCollectionFigures(String name) throws ArangoException {
    return collectionDriver.getCollectionFigures(getDefaultDatabase(), name);
  }

  public CollectionEntity getCollectionChecksum(String name, Boolean withRevisions, Boolean withData)
      throws ArangoException {
    return collectionDriver.getCollectionChecksum(getDefaultDatabase(), name, withRevisions, withData);
  }

  public CollectionsEntity getCollections() throws ArangoException {
    return collectionDriver.getCollections(getDefaultDatabase(), null);
  }

  public CollectionsEntity getCollections(Boolean excludeSystem) throws ArangoException {
    return collectionDriver.getCollections(getDefaultDatabase(), excludeSystem);
  }

  public CollectionEntity loadCollection(long id) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), null);
  }

  public CollectionEntity loadCollection(String name) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), name, null);
  }

  public CollectionEntity loadCollection(long id, Boolean count) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), count);
  }

  public CollectionEntity loadCollection(String name, Boolean count) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), name, count);
  }

  public CollectionEntity unloadCollection(long id) throws ArangoException {
    return unloadCollection(String.valueOf(id));
  }

  public CollectionEntity unloadCollection(String name) throws ArangoException {
    return collectionDriver.unloadCollection(getDefaultDatabase(), name);
  }

  public CollectionEntity truncateCollection(long id) throws ArangoException {
    return truncateCollection(String.valueOf(id));
  }

  public CollectionEntity truncateCollection(String name) throws ArangoException {
    return collectionDriver.truncateCollection(getDefaultDatabase(), name);
  }

  public CollectionEntity setCollectionProperties(long id, Boolean newWaitForSync, Long journalSize)
      throws ArangoException {
    return collectionDriver.setCollectionProperties(
      getDefaultDatabase(),
      String.valueOf(id),
      newWaitForSync,
      journalSize);
  }

  public CollectionEntity setCollectionProperties(String name, Boolean newWaitForSync, Long journalSize)
      throws ArangoException {
    return collectionDriver.setCollectionProperties(getDefaultDatabase(), name, newWaitForSync, journalSize);
  }

  public CollectionEntity renameCollection(long id, String newName) throws ArangoException {
    return renameCollection(String.valueOf(id), newName);
  }

  public CollectionEntity renameCollection(String name, String newName) throws ArangoException {
    return collectionDriver.renameCollection(getDefaultDatabase(), name, newName);
  }

  public CollectionEntity deleteCollection(long id) throws ArangoException {
    return deleteCollection(String.valueOf(id));
  }

  public CollectionEntity deleteCollection(String name) throws ArangoException {
    return collectionDriver.deleteCollection(getDefaultDatabase(), name);
  }

  // ---------------------------------------- end of collection
  // ----------------------------------------

  // ---------------------------------------- start of document
  // ----------------------------------------

  public DocumentEntity<?> createDocument(long collectionId, Object value) throws ArangoException {
    return createDocument(String.valueOf(collectionId), value, null, null);
  }

  public <T> DocumentEntity<T> createDocument(String collectionName, Object value) throws ArangoException {
    return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, null, null);
  }

  public DocumentEntity<?> createDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return createDocument(String.valueOf(collectionId), documentKey, value, null, null);
  }

  public <T> DocumentEntity<T> createDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, null, null);
  }

  public DocumentEntity<?>
      createDocument(long collectionId, Object value, Boolean createCollection, Boolean waitForSync)
          throws ArangoException {
    return createDocument(String.valueOf(collectionId), value, createCollection, waitForSync);
  }

  public <T> DocumentEntity<T> createDocument(
    String collectionName,
    Object value,
    Boolean createCollection,
    Boolean waitForSync) throws ArangoException {
    return documentDriver.createDocument(
      getDefaultDatabase(),
      collectionName,
      null,
      value,
      createCollection,
      waitForSync);
  }

  public DocumentEntity<?> createDocument(
    long collectionId,
    String documentKey,
    Object value,
    Boolean createCollection,
    Boolean waitForSync) throws ArangoException {
    return createDocument(String.valueOf(collectionId), documentKey, value, createCollection, waitForSync);
  }

  public <T> DocumentEntity<T> createDocument(
    String collectionName,
    String documentKey,
    Object value,
    Boolean createCollection,
    Boolean waitForSync) throws ArangoException {
    return documentDriver.createDocument(
      getDefaultDatabase(),
      collectionName,
      documentKey,
      value,
      createCollection,
      waitForSync);
  }

  public DocumentEntity<?> replaceDocument(long collectionId, long documentId, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null);
  }

  public DocumentEntity<?> replaceDocument(String collectionName, long documentId, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null, null);
  }

  public DocumentEntity<?> replaceDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null);
  }

  public DocumentEntity<?> replaceDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null);
  }

  public <T> DocumentEntity<T> replaceDocument(String documentHandle, Object value) throws ArangoException {
    return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, null, null, null);
  }

  public DocumentEntity<?> replaceDocument(
    long collectionId,
    long documentId,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(
      createDocumentHandle(collectionId, String.valueOf(documentId)),
      value,
      rev,
      policy,
      waitForSync);
  }

  public DocumentEntity<?> replaceDocument(
    String collectionName,
    long documentId,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(
      createDocumentHandle(collectionName, String.valueOf(documentId)),
      value,
      rev,
      policy,
      waitForSync);
  }

  public DocumentEntity<?> replaceDocument(
    long collectionId,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, documentKey), value, rev, policy, waitForSync);
  }

  public DocumentEntity<?> replaceDocument(
    String collectionName,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, documentKey), value, rev, policy, waitForSync);
  }

  public <T> DocumentEntity<T> replaceDocument(
    String documentHandle,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, rev, policy, waitForSync);
  }

  public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null, null);
  }

  public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value) throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionName, String.valueOf(documentId)),
      value,
      null,
      null,
      null,
      null);
  }

  public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null, null);
  }

  public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null, null);
  }

  public <T> DocumentEntity<T> updateDocument(String documentHandle, Object value) throws ArangoException {
    return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, null, null);
  }

  public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value, Boolean keepNull)
      throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionId, String.valueOf(documentId)),
      value,
      null,
      null,
      null,
      keepNull);
  }

  public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value, Boolean keepNull)
      throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionName, String.valueOf(documentId)),
      value,
      null,
      null,
      null,
      keepNull);
  }

  public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value, Boolean keepNull)
      throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null, keepNull);
  }

  public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value, Boolean keepNull)
      throws ArangoException {
    return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null, keepNull);
  }

  public <T> DocumentEntity<T> updateDocument(String documentHandle, Object value, Boolean keepNull)
      throws ArangoException {
    return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, null, keepNull);
  }

  public DocumentEntity<?> updateDocument(
    long collectionId,
    long documentId,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync,
    Boolean keepNull) throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionId, String.valueOf(documentId)),
      value,
      rev,
      policy,
      waitForSync,
      keepNull);
  }

  public DocumentEntity<?> updateDocument(
    String collectionName,
    long documentId,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync,
    Boolean keepNull) throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionName, String.valueOf(documentId)),
      value,
      rev,
      policy,
      waitForSync,
      keepNull);
  }

  public DocumentEntity<?> updateDocument(
    long collectionId,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync,
    Boolean keepNull) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, documentKey), value, rev, policy, waitForSync, keepNull);
  }

  public DocumentEntity<?> updateDocument(
    String collectionName,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync,
    Boolean keepNull) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionName, documentKey), value, rev, policy, waitForSync, keepNull);
  }

  public <T> DocumentEntity<T> updateDocument(
    String documentHandle,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync,
    Boolean keepNull) throws ArangoException {
    return documentDriver.updateDocument(
      getDefaultDatabase(),
      documentHandle,
      value,
      rev,
      policy,
      waitForSync,
      keepNull);
  }

  public List<String> getDocuments(long collectionId) throws ArangoException {
    return getDocuments(String.valueOf(collectionId), false);
  }

  public List<String> getDocuments(String collectionName) throws ArangoException {
    return documentDriver.getDocuments(getDefaultDatabase(), collectionName, false);
  }

  public List<String> getDocuments(long collectionId, boolean handleConvert) throws ArangoException {
    return getDocuments(String.valueOf(collectionId), handleConvert);
  }

  public List<String> getDocuments(String collectionName, boolean handleConvert) throws ArangoException {
    return documentDriver.getDocuments(getDefaultDatabase(), collectionName, handleConvert);
  }

  public long checkDocument(long collectionId, long documentId) throws ArangoException {
    return checkDocument(createDocumentHandle(collectionId, String.valueOf(documentId)));
  }

  public long checkDocument(String collectionName, long documentId) throws ArangoException {
    return checkDocument(createDocumentHandle(collectionName, String.valueOf(documentId)));
  }

  public long checkDocument(long collectionId, String documentKey) throws ArangoException {
    return checkDocument(createDocumentHandle(collectionId, documentKey));
  }

  public long checkDocument(String collectionName, String documentKey) throws ArangoException {
    return checkDocument(createDocumentHandle(collectionName, documentKey));
  }

  public long checkDocument(String documentHandle) throws ArangoException {
    return documentDriver.checkDocument(getDefaultDatabase(), documentHandle);
  }

  public <T> DocumentEntity<T> getDocument(long collectionId, long documentId, Class<?> clazz) throws ArangoException {
    return getDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), clazz);
  }

  public <T> DocumentEntity<T> getDocument(String collectionName, long documentId, Class<?> clazz)
      throws ArangoException {
    return getDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), clazz);
  }

  public <T> DocumentEntity<T> getDocument(long collectionId, String documentKey, Class<?> clazz)
      throws ArangoException {
    return getDocument(createDocumentHandle(collectionId, documentKey), clazz);
  }

  public <T> DocumentEntity<T> getDocument(String collectionName, String documentKey, Class<?> clazz)
      throws ArangoException {
    return getDocument(createDocumentHandle(collectionName, documentKey), clazz);
  }

  public <T> DocumentEntity<T> getDocument(String documentHandle, Class<?> clazz) throws ArangoException {
    return documentDriver.getDocument(getDefaultDatabase(), documentHandle, clazz, null, null);
  }

  public <T> DocumentEntity<T> getDocument(
    String documentHandle,
    Class<?> clazz,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException {
    return documentDriver
        .getDocument(getDefaultDatabase(), documentHandle, clazz, ifNoneMatchRevision, ifMatchRevision);
  }

  public DocumentEntity<?> deleteDocument(long collectionId, long documentId) throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), null, null);
  }

  public DocumentEntity<?> deleteDocument(String collectionName, long documentId) throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), null, null);
  }

  public DocumentEntity<?> deleteDocument(long collectionId, String documentKey) throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionId, documentKey), null, null);
  }

  public DocumentEntity<?> deleteDocument(String collectionName, String documentKey) throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionName, documentKey), null, null);
  }

  public DocumentEntity<?> deleteDocument(String documentHandle) throws ArangoException {
    return documentDriver.deleteDocument(getDefaultDatabase(), documentHandle, null, null);
  }

  public DocumentEntity<?> deleteDocument(long collectionId, long documentId, Long rev, Policy policy)
      throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), rev, policy);
  }

  public DocumentEntity<?> deleteDocument(String collectionName, long documentId, Long rev, Policy policy)
      throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), rev, policy);
  }

  public DocumentEntity<?> deleteDocument(long collectionId, String documentKey, Long rev, Policy policy)
      throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionId, documentKey), rev, policy);
  }

  public DocumentEntity<?> deleteDocument(String collectionName, String documentKey, Long rev, Policy policy)
      throws ArangoException {
    return deleteDocument(createDocumentHandle(collectionName, documentKey), rev, policy);
  }

  public DocumentEntity<?> deleteDocument(String documentHandle, Long rev, Policy policy) throws ArangoException {
    return documentDriver.deleteDocument(getDefaultDatabase(), documentHandle, rev, policy);
  }

  // ---------------------------------------- end of document
  // ----------------------------------------

  // ---------------------------------------- start of cursor
  // ----------------------------------------

  public CursorEntity<?> validateQuery(String query) throws ArangoException {
    return cursorDriver.validateQuery(getDefaultDatabase(), query);
  }

  public <T> CursorEntity<T> executeQuery(
    String query,
    Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount,
    Integer batchSize) throws ArangoException {

    return cursorDriver.executeQuery(getDefaultDatabase(), query, bindVars, clazz, calcCount, batchSize);

  }

  public <T> CursorEntity<T> continueQuery(long cursorId, Class<?>... clazz) throws ArangoException {
    return cursorDriver.continueQuery(getDefaultDatabase(), cursorId, clazz);
  }

  public DefaultEntity finishQuery(long cursorId) throws ArangoException {
    return cursorDriver.finishQuery(getDefaultDatabase(), cursorId);
  }

  public <T> CursorResultSet<T> executeQueryWithResultSet(
    String query,
    Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount,
    Integer batchSize) throws ArangoException {
    return cursorDriver.executeQueryWithResultSet(getDefaultDatabase(), query, bindVars, clazz, calcCount, batchSize);
  }

  public IndexEntity createIndex(long collectionId, IndexType type, boolean unique, String... fields)
      throws ArangoException {
    return createIndex(String.valueOf(collectionId), type, unique, fields);
  }

  public IndexEntity createIndex(String collectionName, IndexType type, boolean unique, String... fields)
      throws ArangoException {
    return indexDriver.createIndex(getDefaultDatabase(), collectionName, type, unique, fields);
  }

  public IndexEntity createHashIndex(String collectionName, boolean unique, String... fields) throws ArangoException {
    return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.HASH, unique, fields);
  }

  public IndexEntity createGeoIndex(String collectionName, boolean unique, String... fields) throws ArangoException {
    return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.GEO, unique, fields);
  }

  public IndexEntity createSkipListIndex(String collectionName, boolean unique, String... fields)
      throws ArangoException {
    return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.SKIPLIST, unique, fields);
  }

  public IndexEntity createCappedIndex(long collectionId, int size) throws ArangoException {
    return createCappedIndex(String.valueOf(collectionId), size);
  }

  public IndexEntity createCappedIndex(String collectionName, int size) throws ArangoException {
    return indexDriver.createCappedIndex(getDefaultDatabase(), collectionName, size);
  }

  public IndexEntity createFulltextIndex(long collectionId, String... fields) throws ArangoException {
    return createFulltextIndex(String.valueOf(collectionId), null, fields);
  }

  public IndexEntity createFulltextIndex(String collectionName, String... fields) throws ArangoException {
    return createFulltextIndex(collectionName, null, fields);
  }

  public IndexEntity createFulltextIndex(long collectionId, Integer minLength, String... fields) throws ArangoException {
    return createFulltextIndex(String.valueOf(collectionId), minLength, fields);
  }

  public IndexEntity createFulltextIndex(String collectionName, Integer minLength, String... fields)
      throws ArangoException {
    return indexDriver.createFulltextIndex(getDefaultDatabase(), collectionName, minLength, fields);
  }

  public IndexEntity deleteIndex(String indexHandle) throws ArangoException {
    return indexDriver.deleteIndex(getDefaultDatabase(), indexHandle);
  }

  public IndexEntity getIndex(String indexHandle) throws ArangoException {
    return indexDriver.getIndex(getDefaultDatabase(), indexHandle);
  }

  public IndexesEntity getIndexes(long collectionId) throws ArangoException {
    return getIndexes(String.valueOf(collectionId));
  }

  public IndexesEntity getIndexes(String collectionName) throws ArangoException {
    return indexDriver.getIndexes(getDefaultDatabase(), collectionName);
  }

  public AdminLogEntity getServerLog(
    Integer logLevel,
    Boolean logLevelUpTo,
    Integer start,
    Integer size,
    Integer offset,
    Boolean sortAsc,
    String text) throws ArangoException {
    return adminDriver.getServerLog(logLevel, logLevelUpTo, start, size, offset, sortAsc, text);
  }

  public StatisticsEntity getStatistics() throws ArangoException {
    return adminDriver.getStatistics();
  }

  public StatisticsDescriptionEntity getStatisticsDescription() throws ArangoException {
    return adminDriver.getStatisticsDescription();
  }

  public ArangoVersion getVersion() throws ArangoException {
    return adminDriver.getVersion();
  }

  public ArangoUnixTime getTime() throws ArangoException {
    return adminDriver.getTime();
  }

  public DefaultEntity reloadRouting() throws ArangoException {
    return adminDriver.reloadRouting();
  }

  public DefaultEntity executeScript(String jsCode) throws ArangoException {
    return adminDriver.executeScript(getDefaultDatabase(), jsCode);
  }

  // ---------------------------------------- end of admin
  // ----------------------------------------

  // ---------------------------------------- start of simple
  // ----------------------------------------

  public <T> CursorEntity<T> executeSimpleByExample(
    String collectionName,
    Map<String, Object> example,
    int skip,
    int limit,
    Class<T> clazz) throws ArangoException {
    return simpleDriver.executeSimpleByExample(getDefaultDatabase(), collectionName, example, skip, limit, clazz);
  }

  public <T> CursorResultSet<T> executeSimpleByExampleWithResusltSet(
    String collectionName,
    Map<String, Object> example,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleByExampleWithResultSet(
      getDefaultDatabase(),
      collectionName,
      example,
      skip,
      limit,
      clazz);
  }

  public <T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(
    String collectionName,
    Map<String, Object> example,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleByExampleWithDocument(
      getDefaultDatabase(),
      collectionName,
      example,
      skip,
      limit,
      clazz);
  }

  public <T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResusltSet(
    String collectionName,
    Map<String, Object> example,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleByExampleWithDocumentResultSet(
      getDefaultDatabase(),
      collectionName,
      example,
      skip,
      limit,
      clazz);
  }

  public <T> CursorEntity<T> executeSimpleAll(String collectionName, int skip, int limit, Class<?> clazz)
      throws ArangoException {
    return simpleDriver.executeSimpleAll(getDefaultDatabase(), collectionName, skip, limit, clazz);
  }

  public <T> CursorResultSet<T>
      executeSimpleAllWithResultSet(String collectionName, int skip, int limit, Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleAllWithResultSet(getDefaultDatabase(), collectionName, skip, limit, clazz);
  }

  public <T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(
    String collectionName,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleAllWithDocument(getDefaultDatabase(), collectionName, skip, limit, clazz);
  }

  public <T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(
    String collectionName,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleAllWithDocumentResultSet(getDefaultDatabase(), collectionName, skip, limit, clazz);
  }

  public <T> ScalarExampleEntity<T> executeSimpleFirstExample(
    String collectionName,
    Map<String, Object> example,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleFirstExample(getDefaultDatabase(), collectionName, example, clazz);
  }

  public <T> ScalarExampleEntity<T> executeSimpleAny(String collectionName, Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleAny(getDefaultDatabase(), collectionName, clazz);
  }

  public <T> CursorEntity<T> executeSimpleRange(
    String collectionName,
    String attribute,
    Object left,
    Object right,
    Boolean closed,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleRange(
      getDefaultDatabase(),
      collectionName,
      attribute,
      left,
      right,
      closed,
      skip,
      limit,
      clazz);
  }

  public <T> CursorResultSet<T> executeSimpleRangeWithResultSet(
    String collectionName,
    String attribute,
    Object left,
    Object right,
    Boolean closed,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleRangeWithResultSet(
      getDefaultDatabase(),
      collectionName,
      attribute,
      left,
      right,
      closed,
      skip,
      limit,
      clazz);
  }

  public <T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
    String collectionName,
    String attribute,
    Object left,
    Object right,
    Boolean closed,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleRangeWithDocument(
      getDefaultDatabase(),
      collectionName,
      attribute,
      left,
      right,
      closed,
      skip,
      limit,
      clazz);
  }

  public <T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
    String collectionName,
    String attribute,
    Object left,
    Object right,
    Boolean closed,
    int skip,
    int limit,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleRangeWithDocumentResultSet(
      getDefaultDatabase(),
      collectionName,
      attribute,
      left,
      right,
      closed,
      skip,
      limit,
      clazz);
  }

  public <T> CursorEntity<T> executeSimpleFulltext(
    String collectionName,
    String attribute,
    String query,
    int skip,
    int limit,
    String index,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleFulltext(
      getDefaultDatabase(),
      collectionName,
      attribute,
      query,
      skip,
      limit,
      index,
      clazz);
  }

  public <T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
    String collectionName,
    String attribute,
    String query,
    int skip,
    int limit,
    String index,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleFulltextWithResultSet(
      getDefaultDatabase(),
      collectionName,
      attribute,
      query,
      skip,
      limit,
      index,
      clazz);
  }

  public <T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
    String collectionName,
    String attribute,
    String query,
    int skip,
    int limit,
    String index,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleFulltextWithDocument(
      getDefaultDatabase(),
      collectionName,
      attribute,
      query,
      skip,
      limit,
      index,
      clazz);
  }

  public <T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
    String collectionName,
    String attribute,
    String query,
    int skip,
    int limit,
    String index,
    Class<?> clazz) throws ArangoException {
    return simpleDriver.executeSimpleFulltextWithDocumentResultSet(
      getDefaultDatabase(),
      collectionName,
      attribute,
      query,
      skip,
      limit,
      index,
      clazz);
  }

  public SimpleByResultEntity executeSimpleRemoveByExample(
    String collectionName,
    Map<String, Object> example,
    Boolean waitForSync,
    Integer limit) throws ArangoException {
    return simpleDriver.executeSimpleRemoveByExample(getDefaultDatabase(), collectionName, example, waitForSync, limit);
  }

  public SimpleByResultEntity executeSimpleReplaceByExample(
    String collectionName,
    Map<String, Object> example,
    Map<String, Object> newValue,
    Boolean waitForSync,
    Integer limit) throws ArangoException {
    return simpleDriver.executeSimpleReplaceByExample(
      getDefaultDatabase(),
      collectionName,
      example,
      newValue,
      waitForSync,
      limit);
  }

  public SimpleByResultEntity executeSimpleUpdateByExample(
    String collectionName,
    Map<String, Object> example,
    Map<String, Object> newValue,
    Boolean keepNull,
    Boolean waitForSync,
    Integer limit) throws ArangoException {
    return simpleDriver.executeSimpleUpdateByExample(
      getDefaultDatabase(),
      collectionName,
      example,
      newValue,
      keepNull,
      waitForSync,
      limit);
  }

  /**
   * @param collectionName
   * @param count
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentResultEntity<T> executeSimpleFirst(String collectionName, Integer count, Class<?> clazz)
      throws ArangoException {
    return simpleDriver.executeSimpleFirst(getDefaultDatabase(), collectionName, count, clazz);
  }

  /**
   * @param collectionName
   * @param count
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentResultEntity<T> executeSimpleLast(String collectionName, Integer count, Class<?> clazz)
      throws ArangoException {
    return simpleDriver.executeSimpleLast(getDefaultDatabase(), collectionName, count, clazz);
  }

  // ---------------------------------------- end of simple
  // ----------------------------------------

  // ---------------------------------------- start of users
  // ----------------------------------------

  public DefaultEntity createUser(String username, String passwd, Boolean active, Map<String, Object> extra)
      throws ArangoException {
    return usersDriver.createUser(getDefaultDatabase(), username, passwd, active, extra);
  }

  public DefaultEntity replaceUser(String username, String passwd, Boolean active, Map<String, Object> extra)
      throws ArangoException {
    return usersDriver.replaceUser(getDefaultDatabase(), username, passwd, active, extra);
  }

  public DefaultEntity updateUser(String username, String passwd, Boolean active, Map<String, Object> extra)
      throws ArangoException {
    return usersDriver.updateUser(getDefaultDatabase(), username, passwd, active, extra);
  }

  public DefaultEntity deleteUser(String username) throws ArangoException {
    return usersDriver.deleteUser(getDefaultDatabase(), username);
  }

  public UserEntity getUser(String username) throws ArangoException {
    return usersDriver.getUser(getDefaultDatabase(), username);
  }

  // Original (ArangoDB does not implements this API)
  public List<DocumentEntity<UserEntity>> getUsersDocument() throws ArangoException {

    CursorResultSet<DocumentEntity<UserEntity>> rs = executeSimpleAllWithDocumentResultSet(
      "_users",
      0,
      0,
      UserEntity.class);
    return ResultSetUtils.toList(rs);

  }

  // Original (ArangoDB does not implements this API)
  public List<UserEntity> getUsers() throws ArangoException {

    CursorResultSet<UserEntity> rs = executeSimpleAllWithResultSet("_users", 0, 0, UserEntity.class);
    return ResultSetUtils.toList(rs);

  }

  // ---------------------------------------- end of users
  // ----------------------------------------

  // ---------------------------------------- start of import
  // ----------------------------------------

  public ImportResultEntity importDocuments(String collection, Boolean createCollection, Collection<?> values)
      throws ArangoException {
    return importDriver.importDocuments(getDefaultDatabase(), collection, createCollection, values);
  }

  // public void importDocuments(String collection, Boolean createCollection,
  // Iterator<?> itr) throws ArangoException {
  // importDriver.importDocuments(collection, createCollection, itr);
  // }

  public ImportResultEntity importDocumentsByHeaderValues(
    String collection,
    Boolean createCollection,
    Collection<? extends Collection<?>> headerValues) throws ArangoException {
    return importDriver.importDocumentsByHeaderValues(getDefaultDatabase(), collection, createCollection, headerValues);
  }

  // ---------------------------------------- end of import
  // ----------------------------------------

  // ---------------------------------------- start of database
  // ----------------------------------------

  /**
   * @return
   * @throws ArangoException
   * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#
   *      HttpDatabaseCurrent
   * @since 1.4.0
   */
  public DatabaseEntity getCurrentDatabase() throws ArangoException {
    return databaseDriver.getCurrentDatabase();
  }

  /**
   * @return
   * @throws ArangoException
   * @see http 
   *      ://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseList2
   * @since 1.4.0
   */
  public StringsResultEntity getDatabases() throws ArangoException {
    return getDatabases(false);
  }

  /**
   * @param currentUserAccessableOnly
   * @return
   * @throws ArangoException
   * @see http 
   *      ://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseList
   * @since 1.4.1
   */
  public StringsResultEntity getDatabases(boolean currentUserAccessableOnly) throws ArangoException {
    return databaseDriver.getDatabases(currentUserAccessableOnly, null, null);
  }

  /**
   * @param username
   * @param password
   * @return
   * @throws ArangoException
   * @since 1.4.1
   */
  public StringsResultEntity getDatabases(String username, String password) throws ArangoException {
    return databaseDriver.getDatabases(true, username, password);
  }

  /**
   * @param database
   * @param users
   * @return
   * @throws ArangoException
   * @see http 
   *      ://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseCreate
   * @since 1.4.0
   */
  public BooleanResultEntity createDatabase(String database, UserEntity... users) throws ArangoException {
    return databaseDriver.createDatabase(database, users);
  }

  /**
   * @param database
   * @return
   * @throws ArangoException
   * @see http 
   *      ://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseDelete
   * @since 1.4.0
   */
  public BooleanResultEntity deleteDatabase(String database) throws ArangoException {
    return databaseDriver.deleteDatabase(database);
  }

  // ---------------------------------------- end of database
  // ----------------------------------------

  // ---------------------------------------- start of endpoint
  // ----------------------------------------

  /**
   * @param endpoint
   * @param databases
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public BooleanResultEntity createEndpoint(String endpoint, String... databases) throws ArangoException {
    return endpointDriver.createEndpoint(endpoint, databases);
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public List<Endpoint> getEndpoints() throws ArangoException {
    return endpointDriver.getEndpoints();
  }

  /**
   * @param endpoint
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException {
    return endpointDriver.deleteEndpoint(endpoint);
  }

  // ---------------------------------------- end of endpoint
  // ----------------------------------------

  // ---------------------------------------- start of replication
  // ----------------------------------------

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationInventoryEntity getReplicationInventory() throws ArangoException {
    return replicationDriver.getReplicationInventory(getDefaultDatabase(), null);
  }

  /**
   * @param includeSystem
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationInventoryEntity getReplicationInventory(boolean includeSystem) throws ArangoException {
    return replicationDriver.getReplicationInventory(getDefaultDatabase(), includeSystem);
  }

  /**
   * @param collectionName
   * @param from
   * @param to
   * @param chunkSize
   * @param ticks
   * @param clazz
   * @param handler
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> void getReplicationDump(
    String collectionName,
    Long from,
    Long to,
    Integer chunkSize,
    Boolean ticks,
    Class<T> clazz,
    DumpHandler<T> handler) throws ArangoException {

    replicationDriver.getReplicationDump(
      getDefaultDatabase(),
      collectionName,
      from,
      to,
      chunkSize,
      ticks,
      clazz,
      handler);

  }

  /**
   * @param endpoint
   * @param database
   * @param username
   * @param password
   * @param restrictType
   * @param restrictCollections
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationSyncEntity syncReplication(
    String endpoint,
    String database,
    String username,
    String password,
    RestrictType restrictType,
    String... restrictCollections) throws ArangoException {
    return replicationDriver.syncReplication(
      getDefaultDatabase(),
      endpoint,
      database,
      username,
      password,
      restrictType,
      restrictCollections);
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public String getReplicationServerId() throws ArangoException {
    return replicationDriver.getReplicationServerId();
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public boolean startReplicationLogger() throws ArangoException {
    return replicationDriver.startReplicationLogger(getDefaultDatabase());
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public boolean stopReplicationLogger() throws ArangoException {
    return replicationDriver.stopReplicationLogger(getDefaultDatabase());
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationLoggerStateEntity getReplicationLoggerState() throws ArangoException {
    return replicationDriver.getReplicationLoggerState(getDefaultDatabase());
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationLoggerConfigEntity getReplicationLoggerConfig() throws ArangoException {
    return replicationDriver.getReplicationLoggerConfig(getDefaultDatabase());
  }

  /**
   * @param autoStart
   * @param logRemoteChanges
   * @param maxEvents
   * @param maxEventsSize
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationLoggerConfigEntity setReplicationLoggerConfig(
    Boolean autoStart,
    Boolean logRemoteChanges,
    Long maxEvents,
    Long maxEventsSize) throws ArangoException {
    return replicationDriver.setReplicationLoggerConfig(
      getDefaultDatabase(),
      autoStart,
      logRemoteChanges,
      maxEvents,
      maxEventsSize);
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierConfigEntity getReplicationApplierConfig() throws ArangoException {
    return replicationDriver.getReplicationApplierConfig(getDefaultDatabase());
  }

  /**
   * @param endpoint
   * @param database
   * @param username
   * @param password
   * @param maxConnectRetries
   * @param connectTimeout
   * @param requestTimeout
   * @param chunkSize
   * @param autoStart
   * @param adaptivePolling
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierConfigEntity setReplicationApplierConfig(
    String endpoint,
    String database,
    String username,
    String password,
    Integer maxConnectRetries,
    Integer connectTimeout,
    Integer requestTimeout,
    Integer chunkSize,
    Boolean autoStart,
    Boolean adaptivePolling) throws ArangoException {
    return replicationDriver.setReplicationApplierConfig(
      getDefaultDatabase(),
      endpoint,
      database,
      username,
      password,
      maxConnectRetries,
      connectTimeout,
      requestTimeout,
      chunkSize,
      autoStart,
      adaptivePolling);
  }

  /**
   * @param param
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierConfigEntity setReplicationApplierConfig(ReplicationApplierConfigEntity param)
      throws ArangoException {
    return replicationDriver.setReplicationApplierConfig(getDefaultDatabase(), param);
  }

  /**
   * @param from
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierStateEntity startReplicationApplier(Long from) throws ArangoException {
    return replicationDriver.startReplicationApplier(getDefaultDatabase(), from);
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierStateEntity stopReplicationApplier() throws ArangoException {
    return replicationDriver.stopReplicationApplier(getDefaultDatabase());
  }

  /**
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public ReplicationApplierStateEntity getReplicationApplierState() throws ArangoException {
    return replicationDriver.getReplicationApplierState(getDefaultDatabase());
  }

  // ---------------------------------------- end of replication
  // ----------------------------------------
  // ---------------------------------------- start of graph
  // ----------------------------------------

  /**
   * @param documentKey
   * @param vertices
   * @param edges
   * @param waitForSync
   * @return
   * @throws ArangoException
   * @since 1.4.0
   * @deprecated
   */
  @Deprecated
  public GraphEntity createGraph(String documentKey, String vertices, String edges, Boolean waitForSync)
      throws ArangoException {
    return graphDriver.createGraph(getDefaultDatabase(), documentKey, vertices, edges, waitForSync);
  }

  /**
   * creates a graph
   * 
   * @param graphName
   * @param edgeDefinitions
   * @param orphanCollections
   * @param waitForSync
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity createGraph(
    String graphName,
    List<EdgeDefinitionEntity> edgeDefinitions,
    List<String> orphanCollections,
    Boolean waitForSync) throws ArangoException {
    return graphDriver.createGraph(getDefaultDatabase(), graphName, edgeDefinitions, orphanCollections, waitForSync);
  }

  /**
   * creates an empty graph
   *
   * @param graphName
   * @param waitForSync
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity createGraph(String graphName, Boolean waitForSync) throws ArangoException {
    return graphDriver.createGraph(getDefaultDatabase(), graphName, waitForSync);
  }

  /**
   * list all Graphs of the default database
   *
   * @return GraphsEntity
   * @throws ArangoException
   */
  public GraphsEntity getGraphs() throws ArangoException {
    return graphDriver.getGraphs(getDefaultDatabase());
  }

  /**
   * get graph object by name
   *
   * @param graphName
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity getGraph(String graphName) throws ArangoException {
    return graphDriver.getGraph(getDefaultDatabase(), graphName);
  }

  /**
   * Delete a graph by its name. The collections of the graph will not be
   * deleted.
   *
   * @param graphName
   * @throws ArangoException
   */
  public void deleteGraph(String graphName) throws ArangoException {
    graphDriver.deleteGraph(getDefaultDatabase(), graphName, false);
  }

  /**
   * Delete a graph by its name. If dropCollections is true, all collections of
   * the graph will be deleted, if they are not used in another graph.
   * 
   * @param graphName
   * @param dropCollections
   * @throws ArangoException
   */
  public void deleteGraph(String graphName, Boolean dropCollections) throws ArangoException {
    graphDriver.deleteGraph(getDefaultDatabase(), graphName, dropCollections);
  }

  /**
   * Returns a list of all vertex collection of a graph that are defined in the
   * graphs edgeDefinitions (in "from", "to", and "orphanCollections")
   *
   * @param graphName
   * @return List<String>
   * @throws ArangoException
   */
  public List<String> graphGetVertexCollections(String graphName) throws ArangoException {
    return graphDriver.getVertexCollections(getDefaultDatabase(), graphName);
  }

  /**
   * Removes a vertex collection from the graph and optionally deletes the
   * collection, if it is not used in any other graph.
   *
   * @param graphName
   * @param collectionName
   * @param dropCollection
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertexCollection(String graphName, String collectionName, Boolean dropCollection)
      throws ArangoException {
    return graphDriver.deleteVertexCollection(getDefaultDatabase(), graphName, collectionName, dropCollection);
  }

  /**
   * Creates a vertex collection
   *
   * @param graphName
   * @param collectionName
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity graphCreateVertexCollection(String graphName, String collectionName) throws ArangoException {
    return graphDriver.createVertexCollection(getDefaultDatabase(), graphName, collectionName);
  }

  /**
   * Returns a list of all edge collection of a graph that are defined in the
   * graphs edgeDefinitions
   *
   * @param graphName
   * @return List<String>
   * @throws ArangoException
   */
  public List<String> graphGetEdgeCollections(String graphName) throws ArangoException {
    return graphDriver.getEdgeCollections(getDefaultDatabase(), graphName);
  }

  /**
   * Adds a new edge definition to an existing graph
   *
   * @param graphName
   * @param edgeDefinition
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity graphCreateEdgeDefinition(String graphName, EdgeDefinitionEntity edgeDefinition)
      throws ArangoException {
    return graphDriver.createNewEdgeDefinition(getDefaultDatabase(), graphName, edgeDefinition);
  }

  /**
   * Replaces an existing edge definition to an existing graph. This will also
   * change the edge definitions of all other graphs using this definition as
   * well.
   *
   * @param graphName
   * @param edgeCollectionName
   * @param edgeDefinition
   * @return GraphEntity
   * @throws ArangoException
   */
  public GraphEntity graphReplaceEdgeDefinition(
    String graphName,
    String edgeCollectionName,
    EdgeDefinitionEntity edgeDefinition) throws ArangoException {
    return graphDriver.replaceEdgeDefinition(getDefaultDatabase(), graphName, edgeCollectionName, edgeDefinition);
  }

  /**
   * Removes an existing edge definition from this graph. All data stored in the
   * collections is dropped as well as long as it is not used in other graphs.
   *
   * @param graphName
   * @param edgeCollectionName
   * @return
   */
  public GraphEntity graphDeleteEdgeDefinition(String graphName, String edgeCollectionName, Boolean dropCollection)
      throws ArangoException {
    return graphDriver.deleteEdgeDefinition(getDefaultDatabase(), graphName, edgeCollectionName, dropCollection);
  }

  /**
   * Stores a new vertex with the information contained within the document into
   * the given collection.
   *
   * @param graphName
   * @param vertex
   * @param waitForSync
   * @return
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> graphCreateVertex(
    String graphName,
    String collectionName,
    Object vertex,
    Boolean waitForSync) throws ArangoException {
    return graphDriver.createVertex(getDefaultDatabase(), graphName, collectionName, vertex, waitForSync);
  }

  public <T> DocumentEntity<T> graphGetVertex(String graphName, String collectionName, String key, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getVertex(getDefaultDatabase(), graphName, collectionName, key, clazz, null, null, null);
  }

  public <T> DocumentEntity<T> graphGetVertex(
    String graphName,
    String collectionName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.getVertex(
      getDefaultDatabase(),
      graphName,
      collectionName,
      key,
      clazz,
      rev,
      ifNoneMatchRevision,
      ifMatchRevision);
  }

  /**
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertex(String graphName, String collectionName, String key) throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, null, null, null);
  }

  /**
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param waitForSync
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertex(String graphName, String collectionName, String key, Boolean waitForSync)
      throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, waitForSync, null, null);
  }

  /**
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertex(
    String graphName,
    String collectionName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.deleteVertex(
      getDefaultDatabase(),
      graphName,
      collectionName,
      key,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  public <T> DocumentEntity<T> graphReplaceVertex(String graphName, String collectionName, String key, Object vertex)
      throws ArangoException {
    return graphDriver.replaceVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, null, null, null);
  }

  public <T> DocumentEntity<T> graphReplaceVertex(
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.replaceVertex(
      getDefaultDatabase(),
      graphName,
      collectionName,
      key,
      vertex,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  public <T> DocumentEntity<T> graphUpdateVertex(
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean keepNull) throws ArangoException {
    return graphDriver.updateVertex(
      getDefaultDatabase(),
      graphName,
      collectionName,
      key,
      vertex,
      keepNull,
      null,
      null,
      null);
  }

  public <T> DocumentEntity<T> graphUpdateVertex(
    String graphName,
    String collectionName,
    String key,
    Object vertex,
    Boolean keepNull,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.updateVertex(
      getDefaultDatabase(),
      graphName,
      collectionName,
      key,
      vertex,
      keepNull,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  // *****************************************************************************

  /**
   * 
   * @param graphName
   * @param vertex
   * @param waitForSync
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> createVertex(String graphName, Object vertex, Boolean waitForSync)
      throws ArangoException {
    return graphDriver.createVertex(getDefaultDatabase(), graphName, vertex, waitForSync);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param clazz
   * @return
   */
  public <T> DocumentEntity<T> getVertex(String graphName, String key, Class<?> clazz) throws ArangoException {
    return graphDriver.getVertex(getDefaultDatabase(), graphName, key, clazz, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param clazz
   * @param rev
   * @param IfNoneMatchRevision
   * @param IfMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> getVertex(
    String graphName,
    String key,
    Class<?> clazz,
    Long rev,
    Long IfNoneMatchRevision,
    Long IfMatchRevision) throws ArangoException {
    return graphDriver
        .getVertex(getDefaultDatabase(), graphName, key, clazz, rev, IfNoneMatchRevision, IfMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param key
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteVertex(String graphName, String key) throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, key, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param waitForSync
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteVertex(String graphName, String key, Boolean waitForSync) throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, key, waitForSync, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteVertex(String graphName, String key, Boolean waitForSync, Long rev, Long ifMatchRevision)
      throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, key, waitForSync, rev, ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param vertex
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> replaceVertex(String graphName, String key, Object vertex) throws ArangoException {
    return graphDriver.replaceVertex(getDefaultDatabase(), graphName, key, vertex, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param vertex
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> replaceVertex(
    String graphName,
    String key,
    Object vertex,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.replaceVertex(getDefaultDatabase(), graphName, key, vertex, waitForSync, rev, ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param vertex
   * @param keepNull
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> updateVertex(String graphName, String key, Object vertex, Boolean keepNull)
      throws ArangoException {
    return graphDriver.updateVertex(getDefaultDatabase(), graphName, key, vertex, keepNull, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param vertex
   * @param keepNull
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> DocumentEntity<T> updateVertex(
    String graphName,
    String key,
    Object vertex,
    Boolean keepNull,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.updateVertex(
      getDefaultDatabase(),
      graphName,
      key,
      vertex,
      keepNull,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<DocumentEntity<T>> getVertices(String graphName, Class<?> clazz) throws ArangoException {
    return graphDriver.getVertices(getDefaultDatabase(), graphName, null, clazz, null, null, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<DocumentEntity<T>> getVertices(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    FilterCondition... properties) throws ArangoException {
    return graphDriver.getVertices(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      batchSize,
      limit,
      count,
      null,
      null,
      properties);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<DocumentEntity<T>> getVertices(String graphName, String vertexKey, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getVertices(getDefaultDatabase(), graphName, vertexKey, clazz, null, null, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param edgeDirection
   * @param edgeLabels
   * @param edgeProperties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<DocumentEntity<T>> getVertices(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction edgeDirection,
    Collection<String> edgeLabels,
    FilterCondition... edgeProperties) throws ArangoException {
    return graphDriver.getVertices(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      batchSize,
      limit,
      count,
      edgeDirection,
      edgeLabels,
      edgeProperties);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(String graphName, Class<?> clazz)
      throws ArangoException {

    return graphDriver.getVerticesWithResultSet(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      null,
      null,
      null,
      null,
      null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param direction
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    FilterCondition... properties) throws ArangoException {

    return graphDriver.getVerticesWithResultSet(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      batchSize,
      limit,
      count,
      null,
      null,
      properties);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
    String graphName,
    String vertexKey,
    Class<?> clazz) throws ArangoException {

    return graphDriver.getVerticesWithResultSet(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      null,
      null,
      null,
      null,
      null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param direction
   * @param labels
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException {

    return graphDriver.getVerticesWithResultSet(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      batchSize,
      limit,
      count,
      direction,
      labels,
      properties);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @param value
   * @param label
   * @param waitForSync
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> createEdge(
    String graphName,
    String key,
    String fromHandle,
    String toHandle,
    Object value,
    String label,
    Boolean waitForSync) throws ArangoException {
    return graphDriver
        .createEdge(getDefaultDatabase(), graphName, key, fromHandle, toHandle, value, label, waitForSync);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> createEdge(String graphName, String key, String fromHandle, String toHandle)
      throws ArangoException {
    return graphDriver.createEdge(getDefaultDatabase(), graphName, key, fromHandle, toHandle, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @param value
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> createEdge(String graphName, String key, String fromHandle, String toHandle, Object value)
      throws ArangoException {
    return graphDriver.createEdge(getDefaultDatabase(), graphName, key, fromHandle, toHandle, value, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param clazz
   * @param rev
   * @param ifNoneMatchRevision
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> getEdge(
    String graphName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.getEdge(getDefaultDatabase(), graphName, key, clazz, rev, ifNoneMatchRevision, ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> getEdge(String graphName, String key, Class<?> clazz) throws ArangoException {
    return graphDriver.getEdge(getDefaultDatabase(), graphName, key, clazz, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteEdge(String graphName, String key) throws ArangoException {
    return graphDriver.deleteEdge(getDefaultDatabase(), graphName, key, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param waitForSync
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteEdge(String graphName, String key, Boolean waitForSync) throws ArangoException {
    return graphDriver.deleteEdge(getDefaultDatabase(), graphName, key, waitForSync, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public DeletedEntity deleteEdge(String graphName, String key, Boolean waitForSync, Long rev, Long ifMatchRevision)
      throws ArangoException {
    return graphDriver.deleteEdge(getDefaultDatabase(), graphName, key, waitForSync, rev, ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param value
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> replaceEdge(String graphName, String key, Object value) throws ArangoException {
    return graphDriver.replaceEdge(getDefaultDatabase(), graphName, key, value, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param key
   * @param value
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> EdgeEntity<T> replaceEdge(
    String graphName,
    String key,
    Object value,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.replaceEdge(getDefaultDatabase(), graphName, key, value, waitForSync, rev, ifMatchRevision);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(String graphName, Class<?> clazz) throws ArangoException {
    return graphDriver.getEdges(getDefaultDatabase(), graphName, null, clazz, null, null, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count) throws ArangoException {
    return graphDriver.getEdges(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param labels
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException {
    return graphDriver.getEdges(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      batchSize,
      limit,
      count,
      null,
      labels,
      properties);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(String graphName, Class<?> clazz)
      throws ArangoException {
    return graphDriver
        .getEdgesWithResultSet(getDefaultDatabase(), graphName, null, clazz, null, null, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count) throws ArangoException {
    return graphDriver.getEdgesWithResultSet(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      batchSize,
      limit,
      count,
      null,
      null);
  }

  /**
   *
   * @param graphName
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param labels
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
    String graphName,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException {
    return graphDriver.getEdgesWithResultSet(
      getDefaultDatabase(),
      graphName,
      null,
      clazz,
      batchSize,
      limit,
      count,
      null,
      labels,
      properties);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(String graphName, String vertexKey, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getEdges(getDefaultDatabase(), graphName, vertexKey, clazz, null, null, null, null, null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count) throws ArangoException {
    return graphDriver.getEdges(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, null, null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param direction
   * @param labels
   * @param properties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorEntity<EdgeEntity<T>> getEdges(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction direction,
    Collection<String> labels,
    FilterCondition... properties) throws ArangoException {
    return graphDriver.getEdges(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      batchSize,
      limit,
      count,
      direction,
      labels,
      properties);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(String graphName, String vertexKey, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getEdgesWithResultSet(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      null,
      null,
      null,
      null,
      null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count) throws ArangoException {
    return graphDriver.getEdgesWithResultSet(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      batchSize,
      limit,
      count,
      null,
      null);
  }

  /**
   *
   * @param graphName
   * @param vertexKey
   * @param clazz
   * @param batchSize
   * @param limit
   * @param count
   * @param edgeDirection
   * @param edgeLabels
   * @param edgeProperties
   * @return
   * @throws ArangoException
   * @since 1.4.0
   */
  public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
    String graphName,
    String vertexKey,
    Class<?> clazz,
    Integer batchSize,
    Integer limit,
    Boolean count,
    Direction edgeDirection,
    Collection<String> edgeLabels,
    FilterCondition... edgeProperties) throws ArangoException {
    return graphDriver.getEdgesWithResultSet(
      getDefaultDatabase(),
      graphName,
      vertexKey,
      clazz,
      batchSize,
      limit,
      count,
      edgeDirection,
      edgeLabels,
      edgeProperties);
  }

  public DefaultEntity createAqlFunction(String name, String code) throws ArangoException {
    return aqlFunctionsDriver.createAqlFunction(name, code);
  }

  public AqlFunctionsEntity getAqlFunctions(String namespace) throws ArangoException {
    return aqlFunctionsDriver.getAqlFunctions(namespace);
  }

  public DefaultEntity deleteAqlFunction(String name, boolean isNameSpace) throws ArangoException {
    return aqlFunctionsDriver.deleteAqlFunction(name, isNameSpace);
  }
}

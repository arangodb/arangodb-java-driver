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
 * 
 */

package com.arangodb;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.arangodb.entity.*;
import com.arangodb.http.BatchHttpManager;
import com.arangodb.http.BatchPart;
import com.arangodb.http.HttpManager;
import com.arangodb.http.InvocationHandlerImpl;
import com.arangodb.impl.ImplFactory;
import com.arangodb.impl.InternalBatchDriverImpl;
import com.arangodb.util.DumpHandler;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ResultSetUtils;

/**
 * ArangoDB driver. All of the functionality to use ArangoDB is provided via
 * this class.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab - g.schwab@triagens.de
 * @author fbartels - f.bartels@triagens.de
 * @version 2.2.
 */
public class ArangoDriver extends BaseArangoDriver {


  private ArangoConfigure configure;
  private BatchHttpManager httpManager;
  private String baseUrl;

  private InternalCursorDriver cursorDriver;
  private InternalBatchDriverImpl batchDriver;
  private InternalCollectionDriver collectionDriver;
  private InternalDocumentDriver documentDriver;
  private InternalIndexDriver indexDriver;
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
  private InternalTransactionDriver transactionDriver;

  private String database;

  /**
   * Constructor to create an instance of the driver that uses the
   * default database.
   * 
   * @param ArangoConfigure configure - A configuration object.
   */
  public ArangoDriver(ArangoConfigure configure) {
    this(configure, null);
  }

  /**
   * Constructor to create an instance of the driver that uses the
   * provided database.
   *
   * @param ArangoConfigure configure - A configuration object.
   * @param String  database - the database that will be used.
   */
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
      this.jobsDriver = ImplFactory.createJobsDriver(configure, this.httpManager);
      this.transactionDriver = ImplFactory.createTransactionDriver(configure, this.httpManager);
    } else {
      this.transactionDriver = (InternalTransactionDriver) Proxy.newProxyInstance(
        InternalTransactionDriver.class.getClassLoader(),
        new Class<?>[] { InternalTransactionDriver.class },
        new InvocationHandlerImpl(this.transactionDriver));
      this.jobsDriver = (InternalJobsDriver) Proxy.newProxyInstance(
        InternalJobsDriver.class.getClassLoader(),
        new Class<?>[] { InternalJobsDriver.class },
        new InvocationHandlerImpl(this.jobsDriver));
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


  /**
   * This method enables batch execution. Until 'cancelBatchMode' or 'executeBatch' is called
   * every other call is stacked and will be either executed or discarded when the batch mode is canceled.
   * Each call will return a 'requestId' in the http response, that can be used to select the matching result from the
   * batch execution.
   *
   * @see com.arangodb.ArangoDriver#cancelBatchMode()
   * @see ArangoDriver#executeBatch()
   * @see ArangoDriver#getBatchResponseByRequestId(String)
   * @throws com.arangodb.ArangoException
   */
  public void startBatchMode() throws ArangoException {
    if (this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is already active.");
    }
    this.httpManager.setBatchModeActive(true);
    this.createModuleDrivers(true);

  }

  /**
   * This method sets the driver to asynchronous execution. If the parameter 'fireAndforget' is set to true
   * each call to ArangoDB will be send without a return value. If set to false the return value will be the 'job id'.
   * Each job result can be received by the method 'getJobResult'.
   *
   * @param fireAndForget - if set to true the asynchronous mode is set to 'fire and forget'.
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState, int)
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   * @throws com.arangodb.ArangoException
   */
  public void startAsyncMode(boolean fireAndForget) throws ArangoException {
    if (this.httpManager.getHttpMode().equals(HttpManager.HttpMode.ASYNC)
        || this.httpManager.getHttpMode().equals(HttpManager.HttpMode.FIREANDFORGET)) {
      throw new ArangoException("Arango driver already set to asynchronous mode.");
    }
    HttpManager.HttpMode mode = fireAndForget ? HttpManager.HttpMode.FIREANDFORGET : HttpManager.HttpMode.ASYNC;
    this.httpManager.setHttpMode(mode);
    this.createModuleDrivers(true);
    this.httpManager.resetJobs();
  }

  /**
   * This method sets the driver back to synchronous execution.
   *
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState, int)
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   * @throws com.arangodb.ArangoException
   */
  public void stopAsyncMode() throws ArangoException {
    if (this.httpManager.getHttpMode().equals(HttpManager.HttpMode.SYNC)) {
      throw new ArangoException("Arango driver already set to synchronous mode.");
    }
    this.httpManager.setHttpMode(HttpManager.HttpMode.SYNC);
    this.createModuleDrivers(false);
  }

  /**
   * Returns the id of the last asynchronous executed job.
   *
   * @return String
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState, int)
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public String getLastJobId() {
    return this.httpManager.getLastJobId();
  }

  /**
   * Returns a list of all job ids of asynchronous executed jobs.
   *
   * @return List<String>
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState, int)
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public List<String> getJobIds() {
    return this.httpManager.getJobIds();
  }

  /**
   * Returns a list of all job ids of asynchronous executed jobs, filtered by job state.
   *
   * @param jobState -  the job state as a filter.
   * @param count - a limit for the result set.
   * @return List<String>
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public List<String> getJobs(JobsEntity.JobState jobState, int count) throws ArangoException {
    return this.jobsDriver.getJobs(getDefaultDatabase(), jobState, count);
  }

  /**
   * Returns a list of all job ids of asynchronous executed jobs, filtered by job state.
   *
   * @param jobState -  the job state as a filter.
   * @return List<String>
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public List<String> getJobs(JobsEntity.JobState jobState) throws ArangoException {
    return this.jobsDriver.getJobs(getDefaultDatabase(), jobState);
  }

  /**
   * Deletes all job from ArangoDB.
   *
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public void deleteAllJobs() throws ArangoException {
    this.jobsDriver.deleteAllJobs(getDefaultDatabase());
    this.httpManager.resetJobs();
  }

  /**
   * Deletes a job from ArangoDB.
   *
   * @param jobId - the id of the job
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public void deleteJobById(String JobId) throws ArangoException {
    this.jobsDriver.deleteJobById(getDefaultDatabase(), JobId);
  }

  /**
   * Deletes all jobs by a provided expiration date.
   *
   * @param int timeStamp - a unix timestamp, every older job is deleted.
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public void deleteExpiredJobs(int timeStamp) throws ArangoException {
    this.jobsDriver.deleteExpiredJobs(getDefaultDatabase(), timeStamp);
  }

  /**
   * Returns the job result for a given job id.
   *
   * @param jobId -  the job id.
   * @return <T>
   * @see ArangoDriver#startAsyncMode(boolean)
   * @see ArangoDriver#stopAsyncMode()
   * @see com.arangodb.ArangoDriver#getJobResult(String)
   * @see com.arangodb.ArangoDriver#getJobIds()
   * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
   * @see ArangoDriver#getLastJobId()
   */
  public <T> T getJobResult(String jobId) throws ArangoException {
    return this.jobsDriver.getJobResult(getDefaultDatabase(), jobId);
  }

  /**
   * This method sends all stacked requests as batch to ArangoDB.
   *
   * @see ArangoDriver#startBatchMode()
   * @see com.arangodb.ArangoDriver#cancelBatchMode()
   * @throws com.arangodb.ArangoException
   */
  public DefaultEntity executeBatch() throws ArangoException {
    if (!this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is not active.");
    }
    List<BatchPart> callStack = this.httpManager.getCallStack();
    this.cancelBatchMode();
    DefaultEntity result = this.batchDriver.executeBatch(callStack, this.getDefaultDatabase());
    return result;
  }

  /**
   * This method returns the result of a call to ArangoDB executed within a batch request.
   *
   * @param requestId - the id of a request.
   * @return <T>
   * @see ArangoDriver#startBatchMode()
   * @see ArangoDriver#executeBatch()
   * @see com.arangodb.ArangoDriver#cancelBatchMode()
   * @throws com.arangodb.ArangoException
   */
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

  /**
   * This method cancels the batch execution mode. All stacked calls are discarded.
   *
   * @see ArangoDriver#startBatchMode()
   * @see ArangoDriver#executeBatch()
   * @throws com.arangodb.ArangoException
   */
  public void cancelBatchMode() throws ArangoException {
    if (!this.httpManager.isBatchModeActive()) {
      throw new ArangoException("BatchMode is not active.");
    }
    this.httpManager.setBatchModeActive(false);
    this.createModuleDrivers(false);
  }


  /**
   * Returns the default database.
   *
   * @return String
   */
  public String getDefaultDatabase() {
    return database;
  }

  /**
   * Sets the default database.
   *
   * @param database
   */
  public void setDefaultDatabase(String database) {
    this.database = database;
  }

  /**
   * Creates a new collection.
   *
   * @param name - the name of the collection
   * @return CollectionEntity - the created collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity createCollection(String name) throws ArangoException {
    return collectionDriver.createCollection(getDefaultDatabase(), name, new CollectionOptions());
  }

  /**
   * Creates a new collection.
   *
   * @param name - the name of the collection
   * @param collectionOptions - an object containing the various options.
   * @return CollectionEntity - the created collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity createCollection(String name, CollectionOptions collectionOptions) throws ArangoException {
    return collectionDriver.createCollection(getDefaultDatabase(), name, collectionOptions);
  }

  /**
   * Returns a collection from ArangoDB by id
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollection(long id) throws ArangoException {
    return getCollection(String.valueOf(id));
  }

  /**
   * Returns a collection from ArangoDB by name
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollection(String name) throws ArangoException {
    return collectionDriver.getCollection(getDefaultDatabase(), name);
  }

  /**
   * Returns a collection from ArangoDB including all properties by id
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionProperties(long id) throws ArangoException {
    return getCollectionProperties(String.valueOf(id));
  }

  /**
   * Returns a collection from ArangoDB including all properties by name
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionProperties(String name) throws ArangoException {
    return collectionDriver.getCollectionProperties(getDefaultDatabase(), name);
  }

  /**
   * Returns a collection from ArangoDB including revision by id
   *
   * @param name - the id of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionRevision(long id) throws ArangoException {
    return getCollectionRevision(String.valueOf(id));
  }

  /**
   * Returns a collection from ArangoDB including revision by name
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionRevision(String name) throws ArangoException {
    return collectionDriver.getCollectionRevision(getDefaultDatabase(), name);
  }

  /**
   * Returns a collection from ArangoDB by id including the document count
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionCount(long id) throws ArangoException {
    return getCollectionCount(String.valueOf(id));
  }

  /**
   * Returns a collection from ArangoDB by name including the document count
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionCount(String name) throws ArangoException {
    return collectionDriver.getCollectionCount(getDefaultDatabase(), name);
  }

  /**
   * Returns a collection from ArangoDB by id including the collection figures
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionFigures(long id) throws ArangoException {
    return getCollectionFigures(String.valueOf(id));
  }

  /**
   * Returns a collection from ArangoDB by name including the collection figures
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionFigures(String name) throws ArangoException {
    return collectionDriver.getCollectionFigures(getDefaultDatabase(), name);
  }

  /**
   * Returns a collection from ArangoDB by name including the collection checksum
   *
   * @param name - the id of the collection.
   * @pramam withRevisions - includes the revision into the checksum calculation
   * @pramam withData - includes the collections data into the checksum calculation
   * @return CollectionEntity - the requested collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity getCollectionChecksum(String name, Boolean withRevisions, Boolean withData)
      throws ArangoException {
    return collectionDriver.getCollectionChecksum(getDefaultDatabase(), name, withRevisions, withData);
  }

  /**
   * Returns all collections from ArangoDB
   *
   * @return CollectionsEntity - the CollectionsEntity.
   * @throws ArangoException
   */
  public CollectionsEntity getCollections() throws ArangoException {
    return collectionDriver.getCollections(getDefaultDatabase(), null);
  }

  /**
   * Returns all collections from ArangoDB
   *
   * @param excludeSystem - if set to true system collections will not be added to the result
   * @return CollectionsEntity - the CollectionsEntity.
   * @throws ArangoException
   */
  public CollectionsEntity getCollections(Boolean excludeSystem) throws ArangoException {
    return collectionDriver.getCollections(getDefaultDatabase(), excludeSystem);
  }

  /**
   * Returns the collection and loads it into memory.
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity loadCollection(long id) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), null);
  }

  /**
   * Returns the collection and loads it into memory.
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity loadCollection(String name) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), name, null);
  }

  /**
   * Returns the collection and loads it into memory.
   *
   * @param id - the id of the collection.
   * @param count - if set to true the documents count is returned.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity loadCollection(long id, Boolean count) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), count);
  }

  /**
   * Returns the collection and loads it into memory.
   *
   * @param name - the name of the collection.
   * @param count - if set to true the documents count is returned.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity loadCollection(String name, Boolean count) throws ArangoException {
    return collectionDriver.loadCollection(getDefaultDatabase(), name, count);
  }


  /**
   * Returns the collection and deletes it from memory.
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity unloadCollection(long id) throws ArangoException {
    return unloadCollection(String.valueOf(id));
  }

  /**
   * Returns the collection and deletes it from memory.
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity unloadCollection(String name) throws ArangoException {
    return collectionDriver.unloadCollection(getDefaultDatabase(), name);
  }

  /**
   * Returns the collection and deletes all documents.
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity truncateCollection(long id) throws ArangoException {
    return truncateCollection(String.valueOf(id));
  }

  /**
   * Returns the collection and deletes all documents.
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity truncateCollection(String name) throws ArangoException {
    return collectionDriver.truncateCollection(getDefaultDatabase(), name);
  }

  /**
   * Returns the collection and changes it's journalSize and waitForSync.
   *
   * @param id - the id of the collection.
   * @param newWaitForSync - a new value for the waitForSyncProperty
   * @param journalSize - a new value for the collections journalSize
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity setCollectionProperties(long id, Boolean newWaitForSync, Long journalSize)
      throws ArangoException {
    return collectionDriver.setCollectionProperties(
      getDefaultDatabase(),
      String.valueOf(id),
      newWaitForSync,
      journalSize);
  }

  /**
   * Returns the collection and changes it's journalSize and waitForSync.
   *
   * @param name - the name of the collection.
   * @param newWaitForSync - a new value for the waitForSyncProperty
   * @param journalSize - a new value for the collections journalSize
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity setCollectionProperties(String name, Boolean newWaitForSync, Long journalSize)
      throws ArangoException {
    return collectionDriver.setCollectionProperties(getDefaultDatabase(), name, newWaitForSync, journalSize);
  }

  /**
   * Returns the collection and changes it's name.
   *
   * @param id - the id of the collection.
   * @param newName - the new name for the collection
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity renameCollection(long id, String newName) throws ArangoException {
    return renameCollection(String.valueOf(id), newName);
  }

  /**
   * Returns the collection and changes it's name.
   *
   * @param name - the name of the collection.
   * @param newName - the new name for the collection
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity renameCollection(String name, String newName) throws ArangoException {
    return collectionDriver.renameCollection(getDefaultDatabase(), name, newName);
  }

  /**
   * Deletes a collection by id.
   *
   * @param id - the id of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity deleteCollection(long id) throws ArangoException {
    return deleteCollection(String.valueOf(id));
  }

  /**
   * Deletes a collection by name.
   *
   * @param name - the name of the collection.
   * @return CollectionEntity - the collectionEntity.
   * @throws ArangoException
   */
  public CollectionEntity deleteCollection(String name) throws ArangoException {
    return collectionDriver.deleteCollection(getDefaultDatabase(), name);
  }


  /**
   * Creates a document in the collection defined by the collection's id
   *
   * @param collectionId - The id of the collection
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> createDocument(long collectionId, Object value) throws ArangoException {
    return createDocument(String.valueOf(collectionId), value, null, null);
  }

  /**
   * Creates a document in the collection defined by the collection's name
   *
   * @param collectionName - The name of the collection
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> createDocument(String collectionName, Object value) throws ArangoException {
    return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, null, null);
  }

  /**
   * Creates a document in the collection defined by the collection's name. This method allows
   * to define to documents key. Note that the collection's property CollectionKeyOption.allowUserKeys
   * has to be set accordingly.
   *
   * @param collectionId - The id of the collection
   * @param documentKey - the desired document key
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
  public DocumentEntity<?> createDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return createDocument(String.valueOf(collectionId), documentKey, value, null, null);
  }

  /**
   * Creates a document in the collection defined by the collection's name. This method allows
   * to define to documents key. Note that the collection's property CollectionKeyOption.allowUserKeys
   * has to be set accordingly.
   *
   * @param collectionName - The name of the collection
   * @param documentKey - the desired document key
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
  public <T> DocumentEntity<T> createDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, null, null);
  }


  /**
   * Creates a document in the collection defined by the collection's id.
   *
   * @param collectionId - The id of the collection
   * @param value - An object containing the documents attributes
   * @param  createCollection - if set to true the collection is created if it does not exist
   * @param  waitForSync - if set to true the response is returned when the server has finished.
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
  public DocumentEntity<?>
      createDocument(long collectionId, Object value, Boolean createCollection, Boolean waitForSync)
          throws ArangoException {
    return createDocument(String.valueOf(collectionId), value, createCollection, waitForSync);
  }

  /**
   * Creates a document in the collection defined by the collection's name.
   *
   * @param collectionName - The name of the collection
   * @param value - An object containing the documents attributes
   * @param  createCollection - if set to true the collection is created if it does not exist
   * @param  waitForSync - if set to true the response is returned when the server has finished.
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
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

  /**
   * Creates a document in the collection defined by the collection's id. This method allows
   * to define to documents key. Note that the collection's property CollectionKeyOption.allowUserKeys
   * has to be set accordingly.
   *
   * @param collectionId - The id of the collection
   * @param documentKey - the desired document key
   * @param value - An object containing the documents attributes
   * @param  createCollection - if set to true the collection is created if it does not exist
   * @param  waitForSync - if set to true the response is returned when the server has finished.
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
  public DocumentEntity<?> createDocument(
    long collectionId,
    String documentKey,
    Object value,
    Boolean createCollection,
    Boolean waitForSync) throws ArangoException {
    return createDocument(String.valueOf(collectionId), documentKey, value, createCollection, waitForSync);
  }

  /**
   * Creates a document in the collection defined by the collection's name. This method allows
   * to define to documents key. Note that the collection's property CollectionKeyOption.allowUserKeys
   * has to be set accordingly.
   *
   * @param collectionName - The name of the collection
   * @param documentKey - the desired document key
   * @param value - An object containing the documents attributes
   * @param  createCollection - if set to true the collection is created if it does not exist
   * @param  waitForSync - if set to true the response is returned when the server has finished.
   * @return DocumentEntity<?>
   * @throws ArangoException
   * @see CollectionKeyOption#allowUserKeys
   */
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

  /**
   * This method replaces the content of the document defined by documentId.
   *
   * @param collectionId - The collection's id.
   * @param documentId - The document's id.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(long collectionId, long documentId, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null);
  }

  /**
   * This method replaces the content of the document defined by documentId.
   *
   * @param collectionName - The collection's name.
   * @param documentId - The document's id.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(String collectionName, long documentId, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null, null);
  }

  /**
   * This method replaces the content of the document defined by documentKey.
   *
   * @param collectionId - The collection's id.
   * @param documentKey - The document's key.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null);
  }

  /**
   * This method replaces the content of the document defined by documentKey.
   *
   * @param collectionName - The collection's name.
   * @param documentKey - The document's key.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null);
  }

  /**
   * This method replaces the content of the document defined by documentHandle.
   *
   * @param documentHandle - The document handle.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> replaceDocument(String documentHandle, Object value) throws ArangoException {
    return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, null, null, null);
  }

  /**
   *  This method replaces the content of the document defined by documentId.
   *  This method offers a parameter rev (revision). If the revision of the document on the server
   *  does not match the given revision the policy parameter is used. If it is set to *last* the operation is performed
   *  anyway. if it is set to *error* an error is thrown.
   *
   * @param collectionId - The collection's id.
   * @param documentId - The document's id.
   * @param value - An object containing the new attributes of the document.
   * @param rev - the desired revision.
   * @param policy - The update policy
   * @param waitForSync - if set to true the response is returned when the server has finished.
   * @return
   * @throws ArangoException
   */
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

  /**
   *  This method replaces the content of the document defined by documentId.
   *  This method offers a parameter rev (revision). If the revision of the document on the server
   *  does not match the given revision the policy parameter is used. If it is set to *last* the operation is performed
   *  anyway. if it is set to *error* an error is thrown.
   *
   * @param collectionName - The collection's name.
   * @param documentId - The document's id.
   * @param value - An object containing the new attributes of the document.
   * @param rev - the desired revision.
   * @param policy - The update policy
   * @param waitForSync - if set to true the response is returned when the server has finished.
   * @return
   * @throws ArangoException
   */
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

  /**
   *  This method replaces the content of the document defined by documentKey.
   *  This method offers a parameter rev (revision). If the revision of the document on the server
   *  does not match the given revision the policy parameter is used. If it is set to *last* the operation is performed
   *  anyway. if it is set to *error* an error is thrown.
   *
   * @param collectionId - The collection's id.
   * @param documentKey - The document's key.
   * @param value - An object containing the new attributes of the document.
   * @param rev - the desired revision.
   * @param policy - The update policy
   * @param waitForSync - if set to true the response is returned when the server has finished.
   * @return
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(
    long collectionId,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionId, documentKey), value, rev, policy, waitForSync);
  }

  /**
   *  This method replaces the content of the document defined by documentKey.
   *  This method offers a parameter rev (revision). If the revision of the document on the server
   *  does not match the given revision the policy parameter is used. If it is set to *last* the operation is performed
   *  anyway. if it is set to *error* an error is thrown.
   *
   * @param collectionName - The collection's name.
   * @param documentKey - The document's key.
   * @param value - An object containing the new attributes of the document.
   * @param rev - the desired revision.
   * @param policy - The update policy
   * @param waitForSync - if set to true the response is returned when the server has finished.
   * @return
   * @throws ArangoException
   */
  public DocumentEntity<?> replaceDocument(
    String collectionName,
    String documentKey,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return replaceDocument(createDocumentHandle(collectionName, documentKey), value, rev, policy, waitForSync);
  }

  /**
   *  This method replaces the content of the document defined by documentHandle.
   *  This method offers a parameter rev (revision). If the revision of the document on the server
   *  does not match the given revision the policy parameter is used. If it is set to *last* the operation is performed
   *  anyway. if it is set to *error* an error is thrown.
   *
   * @param documentHandle - The document's handle.
   * @param value - An object containing the new attributes of the document.
   * @param rev - the desired revision.
   * @param policy - The update policy
   * @param waitForSync - if set to true the response is returned when the server has finished.
   * @return
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> replaceDocument(
    String documentHandle,
    Object value,
    Long rev,
    Policy policy,
    Boolean waitForSync) throws ArangoException {
    return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, rev, policy, waitForSync);
  }

  /**
   * This method updates a document defined by documentId.
   *
   * @param collectionId - The collection's id.
   * @param documentId - The document's id.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null, null);
  }

  /**
   * This method updates a document defined by documentId.
   *
   * @param collectionName - The collection's name.
   * @param documentId - The document's id.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value) throws ArangoException {
    return updateDocument(
      createDocumentHandle(collectionName, String.valueOf(documentId)),
      value,
      null,
      null,
      null,
      null);
  }

  /**
   * This method updates a document defined by documentKey.
   *
   * @param collectionId - The collection's id.
   * @param documentKey - The document's key.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value) throws ArangoException {
    return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null, null);
  }

  /**
   * This method updates a document defined by documentKey.
   *
   * @param collectionName - The collection's name.
   * @param documentKey - The document's key.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
  public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value)
      throws ArangoException {
    return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null, null);
  }

  /**
   * This method updates a document defined by documentHandle.
   *
   * @param documentHandle - The document's handle.
   * @param value - An object containing the documents attributes
   * @return DocumentEntity<?>
   * @throws ArangoException
   */
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

  // ***************************************
  // *** end of document *******************
  // ***************************************

  // ***************************************
  // *** start of cursor *******************
  // ***************************************

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

  // ***************************************
  // *** end of admin **********************
  // ***************************************

  // ***************************************
  // *** start of simple *******************
  // ***************************************

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

  // ***************************************
  // *** end of simple *********************
  // ***************************************

  // ***************************************
  // *** start of users ********************
  // ***************************************

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

  // ***************************************
  // *** end of users **********************
  // ***************************************

  // ***************************************
  // *** start of import *******************
  // ***************************************

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

  // ***************************************
  // *** end of import *********************
  // ***************************************

  // ***************************************
  // *** start of database *****************
  // ***************************************

  /**
   * @return
   * @throws ArangoException
   * @see http://www.arangodb.com/manuals/current/HttpDatabase.html#
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
   *      ://www.arangodb.com/manuals/current/HttpDatabase.html#HttpDatabaseList2
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
   *      ://www.arangodb.com/manuals/current/HttpDatabase.html#HttpDatabaseList
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
   *      ://www.arangodb.com/manuals/current/HttpDatabase.html#HttpDatabaseCreate
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
   *      ://www.arangodb.com/manuals/current/HttpDatabase.html#HttpDatabaseDelete
   * @since 1.4.0
   */
  public BooleanResultEntity deleteDatabase(String database) throws ArangoException {
    return databaseDriver.deleteDatabase(database);
  }

  // ***************************************
  // *** end of database *******************
  // ***************************************

  // ***************************************
  // *** start of endpoint *****************
  // ***************************************

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

  // ***************************************
  // *** end of endpoint *******************
  // ***************************************

  // ***************************************
  // *** start of replication **************
  // ***************************************

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

  // ***************************************
  // *** end of replication ****************
  // ***************************************

  // ***************************************
  // *** start of graph ********************
  // ***************************************

  /**
   * Returns a GraphsEntity containing all graph as GraphEntity object of the
   * default database.
   *
   * @return GraphsEntity Object containing all graphs of the database.
   * @throws ArangoException
   */
  public GraphsEntity getGraphs() throws ArangoException {
    return graphDriver.getGraphs(getDefaultDatabase());
  }

  /**
   * Creates a list of the names of all available graphs of the default
   * database.
   *
   * @return List<String> All graph names of the database.
   * @throws ArangoException
   */
  public List<String> getGraphList() throws ArangoException {
    return graphDriver.getGraphList(getDefaultDatabase());
  }

  /**
   * Creates a graph.
   * 
   * @param graphName
   *          The name of the graph to be created.
   * @param edgeDefinitions
   *          List of the graphs edge definitions.
   * @param orphanCollections
   *          List of the graphs orphan collections.
   * @param waitForSync
   *          Wait for sync.
   * @return GraphEntity The new graph.
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
   * Creates an empty graph.
   *
   * @param graphName
   *          The name of the graph to be created.
   * @param waitForSync
   *          Wait for sync.
   * @return GraphEntity The new graph.
   * @throws ArangoException
   */
  public GraphEntity createGraph(String graphName, Boolean waitForSync) throws ArangoException {
    return graphDriver.createGraph(getDefaultDatabase(), graphName, waitForSync);
  }

  /**
   * Get graph object by name, including its edge definitions and vertex
   * collections.
   *
   * @param graphName
   *          The name of the graph.
   * @return GraphEntity The graph.
   * @throws ArangoException
   */
  public GraphEntity getGraph(String graphName) throws ArangoException {
    return graphDriver.getGraph(getDefaultDatabase(), graphName);
  }

  /**
   * Delete a graph by its name. The collections of the graph will not be
   * dropped.
   *
   * @param graphName
   *          Name of the graph to be deleted.
   * @return DeletedEntity
   * @throws ArangoException
   */
  public DeletedEntity deleteGraph(String graphName) throws ArangoException {
    return graphDriver.deleteGraph(getDefaultDatabase(), graphName, false);
  }

  /**
   * Delete a graph by its name. If dropCollections is true, all collections of
   * the graph will be dropped, if they are not used in another graph.
   * 
   * @param graphName
   *          Name of the graph to be deleted.
   * @param dropCollections
   *          Indicates if the collections of the graph will be dropped
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
   *          The graph name.
   * @return List<String> List of the names of the vertex collections
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
   *          The graph name.
   * @param collectionName
   *          The name of the vertex collection to be removed from the graph.
   * @param dropCollection
   *          Indicates if the collection will be dropped
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
   *          The graph name.
   * @param collectionName
   *          The name of the collection to be created.
   * @return GraphEntity The graph, including the new collection.
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
   *          The graph name.
   * @return List<String> List of the names of all edge collections of the
   *         graph.
   * @throws ArangoException
   */
  public List<String> graphGetEdgeCollections(String graphName) throws ArangoException {
    return graphDriver.getEdgeCollections(getDefaultDatabase(), graphName);
  }

  /**
   * Adds a new edge definition to an existing graph
   *
   * @param graphName
   *          The graph name.
   * @param edgeDefinition
   *          The edge definition to be added.
   * @return GraphEntity The graph, including the new edge definition.
   * @throws ArangoException
   */
  public GraphEntity graphCreateEdgeDefinition(String graphName, EdgeDefinitionEntity edgeDefinition)
      throws ArangoException {
    return graphDriver.createEdgeDefinition(getDefaultDatabase(), graphName, edgeDefinition);
  }

  /**
   * Replaces an existing edge definition to an existing graph. This will also
   * change the edge definitions of all other graphs using this definition as
   * well.
   *
   * @param graphName
   *          The name of the graph.
   * @param edgeCollectionName
   *          The name of the edge collection of the edge definition that has to
   *          be replaced.
   * @param edgeDefinition
   *          The new edge definition.
   * @return GraphEntity The graph, including the changed edge definition.
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
   *          The name of the graph.
   * @param edgeCollectionName
   *          The name of edge collection of the edge definition which has to be
   *          deleted.
   * @return The graph, excluding the deleted edge definition.
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
   *          The name of the graph.
   * @param vertex
   *          The vertex object to be stored
   * @param waitForSync
   *          Wait for sync.
   * @return <T> DocumentEntity<T> The resulting DocumentEntity containing the
   *         vertex document.
   * @throws ArangoException
   */
  public <T> DocumentEntity<T>
      graphCreateVertex(String graphName, String collectionName, T vertex, Boolean waitForSync) throws ArangoException {
    return graphDriver.createVertex(getDefaultDatabase(), graphName, collectionName, vertex, waitForSync);
  }

  /**
   * Gets a vertex with the given key if it is contained within your graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param clazz
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> graphGetVertex(String graphName, String collectionName, String key, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getVertex(getDefaultDatabase(), graphName, collectionName, key, clazz, null, null, null);
  }

  /**
   * Gets a vertex with the given key if it is contained within your graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param clazz
   * @param rev
   * @param ifNoneMatchRevision
   * @param ifMatchRevision
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
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
   * Deletes a vertex with the given key, if it is contained within the graph.
   * Furthermore all edges connected to this vertex will be deleted.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @return DeletedEntity
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertex(String graphName, String collectionName, String key) throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, null, null, null);
  }

  /**
   * Deletes a vertex with the given key, if it is contained within the graph.
   * Furthermore all edges connected to this vertex will be deleted.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param waitForSync
   * @return DeletedEntity
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteVertex(String graphName, String collectionName, String key, Boolean waitForSync)
      throws ArangoException {
    return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, waitForSync, null, null);
  }

  /**
   * Deletes a vertex with the given key, if it is contained within the graph.
   * Furthermore all edges connected to this vertex will be deleted.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return DeletedEntity
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

  /**
   * Replaces a vertex with the given key by the content in the body. This will
   * only run successfully if the vertex is contained within the graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
  public <T> DocumentEntity<T> graphReplaceVertex(String graphName, String collectionName, String key, Object vertex)
      throws ArangoException {
    return graphDriver.replaceVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, null, null, null);
  }

  /**
   * Replaces a vertex with the given key by the content in the body. This will
   * only run successfully if the vertex is contained within the graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
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

  /**
   * Updates a vertex with the given key by adding the content in the body. This
   * will only run successfully if the vertex is contained within the graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @param keepNull
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
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

  /**
   * Updates a vertex with the given key by adding the content in the body. This
   * will only run successfully if the vertex is contained within the graph.
   * 
   * @param graphName
   * @param collectionName
   * @param key
   * @param vertex
   * @param keepNull
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return <T> DocumentEntity<T>
   * @throws ArangoException
   */
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

  /**
   * Stores a new edge with the information contained within the body into the
   * given collection.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @param value
   * @param label
   * @param waitForSync
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphCreateEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    String fromHandle,
    String toHandle,
    Object value,
    Boolean waitForSync) throws ArangoException {
    return graphDriver.createEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      fromHandle,
      toHandle,
      value,
      waitForSync);
  }

  /**
   * Stores a new edge with the information contained within the body into the
   * given collection.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param fromHandle
   * @param toHandle
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphCreateEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    String fromHandle,
    String toHandle) throws ArangoException {
    return graphDriver.createEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      fromHandle,
      toHandle,
      null,
      null);
  }

  /**
   * Loads an edge with the given key if it is contained within your graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param clazz
   * @param rev
   * @param ifNoneMatchRevision
   * @param ifMatchRevision
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphGetEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    Class<?> clazz,
    Long rev,
    Long ifNoneMatchRevision,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.getEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      clazz,
      rev,
      ifNoneMatchRevision,
      ifMatchRevision);
  }

  /**
   * Loads an edge with the given key if it is contained within your graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param clazz
   * @return <T> EdgeEntity<T>
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphGetEdge(String graphName, String edgeCollectionName, String key, Class<?> clazz)
      throws ArangoException {
    return graphDriver.getEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, clazz, null, null, null);
  }

  /**
   * Deletes an edge with the given id, if it is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteEdge(String graphName, String edgeCollectionName, String key) throws ArangoException {
    return graphDriver.deleteEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, null, null, null);
  }

  /**
   * Deletes an edge with the given id, if it is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param waitForSync
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteEdge(String graphName, String edgeCollectionName, String key, Boolean waitForSync)
      throws ArangoException {
    return graphDriver.deleteEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, waitForSync, null, null);
  }

  /**
   * Deletes an edge with the given id, if it is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   */
  public DeletedEntity graphDeleteEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.deleteEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  /**
   * Replaces an edge with the given key by the content in the body. This will
   * only run successfully if the edge is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @return
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphReplaceEdge(String graphName, String edgeCollectionName, String key, Object value)
      throws ArangoException {
    return graphDriver.replaceEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, value, null, null, null);
  }

  /**
   * Replaces an edge with the given key by the content in the body. This will
   * only run successfully if the edge is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @param waitForSync
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphReplaceEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    Object value,
    Boolean waitForSync,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.replaceEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      value,
      waitForSync,
      rev,
      ifMatchRevision);
  }

  /**
   * Updates an edge with the given key by adding the content in the body. This
   * will only run successfully if the edge is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @param keepNull
   * @return
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphUpdateEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    Object value,
    Boolean keepNull) throws ArangoException {
    return graphDriver.updateEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      value,
      null,
      keepNull,
      null,
      null);
  }

  /**
   * Updates an edge with the given key by adding the content in the body. This
   * will only run successfully if the edge is contained within the graph.
   * 
   * @param graphName
   * @param edgeCollectionName
   * @param key
   * @param value
   * @param waitForSync
   * @param keepNull
   * @param rev
   * @param ifMatchRevision
   * @return
   * @throws ArangoException
   */
  public <T> EdgeEntity<T> graphUpdateEdge(
    String graphName,
    String edgeCollectionName,
    String key,
    Object value,
    Boolean waitForSync,
    Boolean keepNull,
    Long rev,
    Long ifMatchRevision) throws ArangoException {
    return graphDriver.updateEdge(
      getDefaultDatabase(),
      graphName,
      edgeCollectionName,
      key,
      value,
      waitForSync,
      keepNull,
      rev,
      ifMatchRevision);
  }

  // Some methods not using the graph api

  /**
   * Returns all Edges of a graph, each edge as a PlainEdgeEntity.
   * 
   * @param graphName
   * @return CursorEntity<PlainEdgeEntity>
   * @throws ArangoException
   */
  public CursorEntity<PlainEdgeEntity> graphGetEdges(String graphName) throws ArangoException {

    validateCollectionName(graphName);
    String query = "for i in graph_edges(@graphName, null) return i";
    Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName).get();

    CursorEntity<PlainEdgeEntity> result = this.executeQuery(query, bindVars, PlainEdgeEntity.class, true, 20);

    return result;

  }

  /**
   * Returns all Edges of a given vertex.
   * 
   * @param graphName
   * @param clazz
   * @param vertexDocumentHandle
   * @return <T> CursorEntity<T>
   * @throws ArangoException
   */
  public <T> CursorEntity<T> graphGetEdges(String graphName, Class<T> clazz, String vertexDocumentHandle)
      throws ArangoException {

    validateCollectionName(graphName);
    String query = "for i in graph_edges(@graphName, @vertexDocumentHandle) return i";
    Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName)
        .put("vertexDocumentHandle", vertexDocumentHandle).get();

    CursorEntity<T> result = this.executeQuery(query, bindVars, clazz, true, 20);

    return result;

  }

  /**
   * Returns all Edges of vertices matching the example object (non-primitive
   * set to null will not be used for comparing).
   * 
   * @param graphName
   * @param clazzT
   * @param vertexExample
   * @return <T> CursorEntity<T>
   * @throws ArangoException
   */
  public <T, S> CursorEntity<T> graphGetEdgesByExampleObject(String graphName, Class<T> clazzT, S vertexExample)
      throws ArangoException {
    validateCollectionName(graphName);
    String query = "for i in graph_edges(@graphName, @vertexExample) return i";

    Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName).put("vertexExample", vertexExample)
        .get();

    CursorEntity<T> result = this.executeQuery(query, bindVars, clazzT, true, 20);

    return result;
  }

  /**
   * Returns all Edges of vertices matching the map.
   * 
   * @param graphName
   * @param clazzT
   * @param vertexExample
   * @return <T> CursorEntity<T>
   * @throws ArangoException
   */
  public <T> CursorEntity<T> graphGetEdgesByExampleMap(
    String graphName,
    Class<T> clazzT,
    Map<String, Object> vertexExample) throws ArangoException {
    validateCollectionName(graphName);
    String query = "for i in graph_edges(@graphName, @vertexExample) return i";

    Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName).put("vertexExample", vertexExample)
        .get();

    CursorEntity<T> result = this.executeQuery(query, bindVars, clazzT, true, 20);

    return result;
  }

  // public <T, S> CursorEntity<EdgeEntity<T>> graphGetEdgesByExampleObject1(
  // String graphName,
  // Class<T> clazzT,
  // S vertexExample) throws ArangoException {
  // validateCollectionName(graphName);
  // String query = "for i in graph_edges(@graphName, @vertexExample) return i";
  //
  // Map<String, Object> bindVars = new MapBuilder().put("graphName",
  // graphName).put("vertexExample", vertexExample)
  // .get();
  //
  // CursorEntity<EdgeEntity<T>> result = this.executeQuery(query, bindVars,
  // EdgeEntity<T>.class, true, 20);
  //
  // return null;
  // }

  // public <T> CursorEntity<EdgeEntity<T>> graphGetEdgesWithData(
  // String graphName,
  // Class<T> clazz,
  // String vertexDocumentHandle,
  // int i) throws ArangoException {
  //
  // validateCollectionName(graphName);
  // String query =
  // "for i in graph_edges(@graphName, @vertexDocumentHandle) return i";
  // Map<String, Object> bindVars = new MapBuilder().put("graphName", graphName)
  // .put("vertexDocumentHandle", vertexDocumentHandle).get();
  //
  // CursorEntity<T> result = this.executeQuery(query, bindVars, clazz, true,
  // 20);
  //
  // return (CursorEntity<EdgeEntity<T>>) result;
  //
  // }

  // ***************************************
  // *** end of graph **********************
  // ***************************************

  public DefaultEntity createAqlFunction(String name, String code) throws ArangoException {
    return aqlFunctionsDriver.createAqlFunction(name, code);
  }

  public AqlFunctionsEntity getAqlFunctions(String namespace) throws ArangoException {
    return aqlFunctionsDriver.getAqlFunctions(namespace);
  }

  public DefaultEntity deleteAqlFunction(String name, boolean isNameSpace) throws ArangoException {
    return aqlFunctionsDriver.deleteAqlFunction(name, isNameSpace);
  }

  public TransactionEntity createTransaction(String action) {
    return this.transactionDriver.createTransaction(action);
  };

  public TransactionResultEntity executeTransaction(TransactionEntity transactionEntity) throws ArangoException {
    return this.transactionDriver.executeTransaction(getDefaultDatabase(), transactionEntity);
  };
}

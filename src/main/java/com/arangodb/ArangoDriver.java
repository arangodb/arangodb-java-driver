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

import java.lang.reflect.InvocationTargetException;
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
import com.arangodb.entity.CollectionKeyOption;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionsEntity;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DatabaseEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DeletedEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeDefinitionEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.Endpoint;
import com.arangodb.entity.GraphEntity;
import com.arangodb.entity.GraphsEntity;
import com.arangodb.entity.ImportResultEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;
import com.arangodb.entity.JobsEntity;
import com.arangodb.entity.PlainEdgeEntity;
import com.arangodb.entity.QueriesResultEntity;
import com.arangodb.entity.QueryCachePropertiesEntity;
import com.arangodb.entity.QueryTrackingPropertiesEntity;
import com.arangodb.entity.ReplicationApplierConfigEntity;
import com.arangodb.entity.ReplicationApplierStateEntity;
import com.arangodb.entity.ReplicationInventoryEntity;
import com.arangodb.entity.ReplicationLoggerConfigEntity;
import com.arangodb.entity.ReplicationLoggerStateEntity;
import com.arangodb.entity.ReplicationSyncEntity;
import com.arangodb.entity.RestrictType;
import com.arangodb.entity.ScalarExampleEntity;
import com.arangodb.entity.ShortestPathEntity;
import com.arangodb.entity.SimpleByResultEntity;
import com.arangodb.entity.StatisticsDescriptionEntity;
import com.arangodb.entity.StatisticsEntity;
import com.arangodb.entity.StringsResultEntity;
import com.arangodb.entity.TransactionEntity;
import com.arangodb.entity.TransactionResultEntity;
import com.arangodb.entity.TraversalEntity;
import com.arangodb.entity.UserEntity;
import com.arangodb.entity.marker.VertexEntity;
import com.arangodb.http.BatchHttpManager;
import com.arangodb.http.BatchPart;
import com.arangodb.http.HttpManager;
import com.arangodb.http.InvocationHandlerImpl;
import com.arangodb.impl.ImplFactory;
import com.arangodb.impl.InternalBatchDriverImpl;
import com.arangodb.util.AqlQueryOptions;
import com.arangodb.util.DumpHandler;
import com.arangodb.util.GraphEdgesOptions;
import com.arangodb.util.GraphQueryUtil;
import com.arangodb.util.GraphVerticesOptions;
import com.arangodb.util.ImportOptions;
import com.arangodb.util.ImportOptionsRaw;
import com.arangodb.util.MapBuilder;
import com.arangodb.util.ShortestPathOptions;
import com.arangodb.util.TraversalQueryOptions;

/**
 * ArangoDB driver. All of the functionality to use ArangoDB is provided via
 * this class.
 * 
 * @author tamtam180 - kirscheless at gmail.com
 * @author gschwab
 * @author fbartels
 * @author a-brandt
 * 
 * @version 2.2.
 * 
 * @see <a href="https://www.arangodb.com/documentation">ArangoDB
 *      documentation</a>
 */
public class ArangoDriver extends BaseArangoDriver {

	private static final String DATABASE_SYSTEM = "_system";
	private static final String COLLECTION_USERS = "_users";
	private final ArangoConfigure configure;
	private final BatchHttpManager httpManager;

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
	private InternalTraversalDriver traversalDriver;
	private InternalQueryCacheDriver queryCacheDriver;

	private String database;

	/**
	 * Constructor to create an instance of the driver that uses the default
	 * database.
	 * 
	 * @param configure
	 *            A configuration object.
	 */
	public ArangoDriver(final ArangoConfigure configure) {
		this(configure, null);
	}

	/**
	 * Constructor to create an instance of the driver that uses the provided
	 * database.
	 *
	 * @param configure
	 *            A configuration object.
	 * @param database
	 *            the name of the database that will be used.
	 */
	public ArangoDriver(final ArangoConfigure configure, final String database) {

		this.database = configure.getDefaultDatabase();
		if (database != null) {
			this.database = database;
		}

		this.configure = configure;
		this.httpManager = configure.getHttpManager();
		this.createModuleDrivers(false);
	}

	/**
	 * This method enables batch execution. Until 'cancelBatchMode' or
	 * 'executeBatch' is called every other call is stacked and will be either
	 * executed or discarded when the batch mode is canceled. Each call will
	 * return a 'requestId' in the HTTP response, that can be used to select the
	 * matching result from the batch execution.
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
		this.httpManager.emptyCallStack();
		this.httpManager.setBatchModeActive(true);
		this.createModuleDrivers(true);

	}

	/**
	 * This method sets the driver to asynchronous execution. If the parameter
	 * 'fireAndforget' is set to true each call to ArangoDB will be send without
	 * a return value. If set to false the return value will be the 'job id'.
	 * Each job result can be received by the method 'getJobResult'.
	 *
	 * @param fireAndForget
	 *            if set to true the asynchronous mode is set to 'fire and
	 *            forget'.
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState,
	 *      int)
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 * @throws com.arangodb.ArangoException
	 */
	public void startAsyncMode(final boolean fireAndForget) throws ArangoException {
		if (this.httpManager.getHttpMode().equals(HttpManager.HttpMode.ASYNC)
				|| this.httpManager.getHttpMode().equals(HttpManager.HttpMode.FIREANDFORGET)) {
			throw new ArangoException("Arango driver already set to asynchronous mode.");
		}
		final HttpManager.HttpMode mode = fireAndForget ? HttpManager.HttpMode.FIREANDFORGET
				: HttpManager.HttpMode.ASYNC;
		this.httpManager.setHttpMode(mode);
		this.createModuleDrivers(true);
		this.httpManager.resetJobs();
	}

	/**
	 * This method sets the driver back to synchronous execution.
	 *
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState,
	 *      int)
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
	 * Returns the identifier of the last asynchronous executed job.
	 *
	 * @return String the identifier
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState,
	 *      int)
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public String getLastJobId() {
		return this.httpManager.getLastJobId();
	}

	/**
	 * Returns a list of all job identifiers of asynchronous executed jobs.
	 *
	 * @return List<String> the list of all job identifiers
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobs(com.arangodb.entity.JobsEntity.JobState,
	 *      int)
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public List<String> getJobIds() {
		return this.httpManager.getJobIds();
	}

	/**
	 * Returns a list of all job identifiers of asynchronous executed jobs,
	 * filtered by job state.
	 *
	 * @param jobState
	 *            the job state as a filter.
	 * @param count
	 *            a limit for the result set.
	 * @return List<String> list of all job identifiers
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobIds()
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public List<String> getJobs(final JobsEntity.JobState jobState, final int count) throws ArangoException {
		return this.jobsDriver.getJobs(getDefaultDatabase(), jobState, count);
	}

	/**
	 * Returns a list of all job identifiers of asynchronous executed jobs,
	 * filtered by job state.
	 *
	 * 
	 * @param jobState
	 *            the job state as a filter.
	 * @return List<String> list of all job identifiers
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobIds()
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public List<String> getJobs(final JobsEntity.JobState jobState) throws ArangoException {
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
	 * @param jobId
	 *            the identifier of the job
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobIds()
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public void deleteJobById(final String jobId) throws ArangoException {
		this.jobsDriver.deleteJobById(getDefaultDatabase(), jobId);
	}

	/**
	 * Deletes all jobs by a provided expiration date.
	 *
	 * @param timeStamp
	 *            a unix timestamp, every older job is deleted.
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobIds()
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public void deleteExpiredJobs(final int timeStamp) throws ArangoException {
		this.jobsDriver.deleteExpiredJobs(getDefaultDatabase(), timeStamp);
	}

	/**
	 * Returns the job result for a given job id.
	 *
	 * @param jobId
	 *            the job id.
	 * @return <T> - A generic return value, containing the job result
	 * @see ArangoDriver#startAsyncMode(boolean)
	 * @see ArangoDriver#stopAsyncMode()
	 * @see com.arangodb.ArangoDriver#getJobResult(String)
	 * @see com.arangodb.ArangoDriver#getJobIds()
	 * @see com.arangodb.ArangoDriver#deleteExpiredJobs(int)
	 * @see ArangoDriver#getLastJobId()
	 */
	public <T> T getJobResult(final String jobId) throws ArangoException {
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
		final List<BatchPart> callStack = this.httpManager.getCallStack();
		this.cancelBatchMode();
		return this.batchDriver.executeBatch(callStack, this.getDefaultDatabase());
	}

	/**
	 * This method returns the result of a call to ArangoDB executed within a
	 * batch request.
	 *
	 * @param requestId
	 *            the id of a request.
	 * @return <T> - A generic return value, containing the result.
	 * @see ArangoDriver#startBatchMode()
	 * @see ArangoDriver#executeBatch()
	 * @see com.arangodb.ArangoDriver#cancelBatchMode()
	 * @throws com.arangodb.ArangoException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBatchResponseByRequestId(final String requestId) throws ArangoException {
		final BatchResponseEntity batchResponseEntity = this.batchDriver.getBatchResponseListEntity()
				.getResponseFromRequestId(requestId);
		try {
			this.httpManager.setPreDefinedResponse(batchResponseEntity.getHttpResponseEntity());

			final T result = (T) batchResponseEntity.getInvocationObject().getMethod().invoke(
				batchResponseEntity.getInvocationObject().getArangoDriver(),
				batchResponseEntity.getInvocationObject().getArgs());
			this.httpManager.setPreDefinedResponse(null);
			return result;
		} catch (final InvocationTargetException e) {
			final T result = (T) createEntity(batchResponseEntity.getHttpResponseEntity(), DefaultEntity.class);
			this.httpManager.setPreDefinedResponse(null);
			return result;
		} catch (final Exception e) {
			this.httpManager.setPreDefinedResponse(null);
			throw new ArangoException(e);
		}
	}

	/**
	 * This method cancels the batch execution mode. All stacked calls are
	 * discarded.
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
		this.httpManager.emptyCallStack();
		this.httpManager.setPreDefinedResponse(null);
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
	public void setDefaultDatabase(final String database) {
		this.database = database;
	}

	/**
	 * Creates a new collection.
	 *
	 * @param name
	 *            the name of the collection
	 * @return CollectionEntity - the created collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity createCollection(final String name) throws ArangoException {
		return collectionDriver.createCollection(getDefaultDatabase(), name, new CollectionOptions());
	}

	/**
	 * Creates a new collection.
	 *
	 * @param name
	 *            the name of the collection
	 * @param collectionOptions
	 *            an object containing the various options.
	 * @return CollectionEntity - the created collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity createCollection(final String name, final CollectionOptions collectionOptions)
			throws ArangoException {
		return collectionDriver.createCollection(getDefaultDatabase(), name, collectionOptions);
	}

	/**
	 * Returns a collection from ArangoDB by id
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollection(final long id) throws ArangoException {
		return getCollection(String.valueOf(id));
	}

	/**
	 * Returns a collection from ArangoDB by name
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollection(final String name) throws ArangoException {
		return collectionDriver.getCollection(getDefaultDatabase(), name);
	}

	/**
	 * Returns a collection from ArangoDB including all properties by id
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionProperties(final long id) throws ArangoException {
		return getCollectionProperties(String.valueOf(id));
	}

	/**
	 * Returns a collection from ArangoDB including all properties by name
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionProperties(final String name) throws ArangoException {
		return collectionDriver.getCollectionProperties(getDefaultDatabase(), name);
	}

	/**
	 * Returns a collection from ArangoDB including revision by id
	 *
	 * @param id
	 *            the identifier of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionRevision(final long id) throws ArangoException {
		return getCollectionRevision(String.valueOf(id));
	}

	/**
	 * Returns a collection from ArangoDB including revision by name
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionRevision(final String name) throws ArangoException {
		return collectionDriver.getCollectionRevision(getDefaultDatabase(), name);
	}

	/**
	 * Returns a collection from ArangoDB by id including the document count
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionCount(final long id) throws ArangoException {
		return getCollectionCount(String.valueOf(id));
	}

	/**
	 * Returns a collection from ArangoDB by name including the document count
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionCount(final String name) throws ArangoException {
		return collectionDriver.getCollectionCount(getDefaultDatabase(), name);
	}

	/**
	 * Returns a collection from ArangoDB by id including the collection figures
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionFigures(final long id) throws ArangoException {
		return getCollectionFigures(String.valueOf(id));
	}

	/**
	 * Returns a collection from ArangoDB by name including the collection
	 * figures
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionFigures(final String name) throws ArangoException {
		return collectionDriver.getCollectionFigures(getDefaultDatabase(), name);
	}

	/**
	 * Returns a collection from ArangoDB by name including the collection
	 * checksum
	 *
	 * @param name
	 *            the id of the collection.
	 * @param withRevisions
	 *            includes the revision into the checksum calculation
	 * @param withData
	 *            includes the collections data into the checksum calculation
	 * @return CollectionEntity - the requested collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity getCollectionChecksum(
		final String name,
		final Boolean withRevisions,
		final Boolean withData) throws ArangoException {
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
	 * @param excludeSystem
	 *            if set to true system collections will not be added to the
	 *            result
	 * @return CollectionsEntity - the CollectionsEntity.
	 * @throws ArangoException
	 */
	public CollectionsEntity getCollections(final Boolean excludeSystem) throws ArangoException {
		return collectionDriver.getCollections(getDefaultDatabase(), excludeSystem);
	}

	/**
	 * Returns the collection and loads it into memory.
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity loadCollection(final long id) throws ArangoException {
		return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), null);
	}

	/**
	 * Returns the collection and loads it into memory.
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity loadCollection(final String name) throws ArangoException {
		return collectionDriver.loadCollection(getDefaultDatabase(), name, null);
	}

	/**
	 * Returns the collection and loads it into memory.
	 *
	 * @param id
	 *            the id of the collection.
	 * @param count
	 *            if set to true the documents count is returned.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity loadCollection(final long id, final Boolean count) throws ArangoException {
		return collectionDriver.loadCollection(getDefaultDatabase(), String.valueOf(id), count);
	}

	/**
	 * Returns the collection and loads it into memory.
	 *
	 * @param name
	 *            the name of the collection.
	 * @param count
	 *            if set to true the documents count is returned.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity loadCollection(final String name, final Boolean count) throws ArangoException {
		return collectionDriver.loadCollection(getDefaultDatabase(), name, count);
	}

	/**
	 * Returns the collection and deletes it from memory.
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity unloadCollection(final long id) throws ArangoException {
		return unloadCollection(String.valueOf(id));
	}

	/**
	 * Returns the collection and deletes it from memory.
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity unloadCollection(final String name) throws ArangoException {
		return collectionDriver.unloadCollection(getDefaultDatabase(), name);
	}

	/**
	 * Returns the collection and deletes all documents.
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity truncateCollection(final long id) throws ArangoException {
		return truncateCollection(String.valueOf(id));
	}

	/**
	 * Returns the collection and deletes all documents.
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity truncateCollection(final String name) throws ArangoException {
		return collectionDriver.truncateCollection(getDefaultDatabase(), name);
	}

	/**
	 * Returns the collection and changes it's journalSize and waitForSync.
	 *
	 * @param id
	 *            the id of the collection.
	 * @param newWaitForSync
	 *            a new value for the waitForSyncProperty
	 * @param journalSize
	 *            a new value for the collections journalSize
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity setCollectionProperties(final long id, final Boolean newWaitForSync, final Long journalSize)
			throws ArangoException {
		return collectionDriver.setCollectionProperties(getDefaultDatabase(), String.valueOf(id), newWaitForSync,
			journalSize);
	}

	/**
	 * Returns the collection and changes it's journalSize and waitForSync.
	 *
	 * @param name
	 *            the name of the collection.
	 * @param newWaitForSync
	 *            a new value for the waitForSyncProperty
	 * @param journalSize
	 *            a new value for the collections journalSize
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity setCollectionProperties(
		final String name,
		final Boolean newWaitForSync,
		final Long journalSize) throws ArangoException {
		return collectionDriver.setCollectionProperties(getDefaultDatabase(), name, newWaitForSync, journalSize);
	}

	/**
	 * Returns the collection and changes it's name.
	 *
	 * @param id
	 *            the id of the collection.
	 * @param newName
	 *            the new name for the collection
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity renameCollection(final long id, final String newName) throws ArangoException {
		return renameCollection(String.valueOf(id), newName);
	}

	/**
	 * Returns the collection and changes it's name.
	 *
	 * @param name
	 *            the name of the collection.
	 * @param newName
	 *            the new name for the collection
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity renameCollection(final String name, final String newName) throws ArangoException {
		return collectionDriver.renameCollection(getDefaultDatabase(), name, newName);
	}

	/**
	 * Deletes a collection by id.
	 *
	 * @param id
	 *            the id of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity deleteCollection(final long id) throws ArangoException {
		return deleteCollection(String.valueOf(id));
	}

	/**
	 * Deletes a collection by name.
	 *
	 * @param name
	 *            the name of the collection.
	 * @return CollectionEntity - the collectionEntity.
	 * @throws ArangoException
	 */
	public CollectionEntity deleteCollection(final String name) throws ArangoException {
		return collectionDriver.deleteCollection(getDefaultDatabase(), name);
	}

	/**
	 * Creates a document in the collection defined by The collection id
	 *
	 * @param collectionId
	 *            The id of the collection
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> createDocument(final long collectionId, final Object value) throws ArangoException {
		return createDocument(String.valueOf(collectionId), value, null);
	}

	/**
	 * Creates a document in the collection defined by the collection's name
	 *
	 * @param collectionName
	 *            The name of the collection
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> createDocument(final String collectionName, final T value) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, null);
	}

	/**
	 * Creates a document in the collection defined by the collection's name.
	 * This method allows to define to documents key. Note that the collection's
	 * property CollectionKeyOption.allowUserKeys has to be set accordingly.
	 *
	 * @param collectionId
	 *            The id of the collection
	 * @param documentKey
	 *            the desired document key
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public DocumentEntity<?> createDocument(final long collectionId, final String documentKey, final Object value)
			throws ArangoException {
		return createDocument(String.valueOf(collectionId), documentKey, value, null);
	}

	/**
	 * Creates a document in the collection defined by the collection's name.
	 * This method allows to define to documents key. Note that the collection's
	 * property CollectionKeyOption.allowUserKeys has to be set accordingly.
	 *
	 * @param collectionName
	 *            The name of the collection
	 * @param documentKey
	 *            the desired document key
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public <T> DocumentEntity<T> createDocument(final String collectionName, final String documentKey, final T value)
			throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, null);
	}

	/**
	 * Creates a document in the collection defined by The collection id.
	 *
	 * @param collectionId
	 *            The id of the collection
	 * @param value
	 *            An object containing the documents attributes
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public <T> DocumentEntity<T> createDocument(final long collectionId, final T value, final Boolean waitForSync)
			throws ArangoException {
		return createDocument(String.valueOf(collectionId), value, waitForSync);
	}

	/**
	 * Creates a document in the collection defined by the collection's name.
	 *
	 * @param collectionName
	 *            The name of the collection
	 * @param value
	 *            An object containing the documents attributes
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public <T> DocumentEntity<T> createDocument(final String collectionName, final T value, final Boolean waitForSync)
			throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, waitForSync);
	}

	/**
	 * Creates a document in the collection defined by the collection's id. This
	 * method allows to define to documents key. Note that the collection's
	 * property CollectionKeyOption.allowUserKeys has to be set accordingly.
	 *
	 * @param collectionId
	 *            The id of the collection
	 * @param documentKey
	 *            the desired document key
	 * @param value
	 *            An object containing the documents attributes
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public DocumentEntity<?> createDocument(
		final long collectionId,
		final String documentKey,
		final Object value,
		final Boolean waitForSync) throws ArangoException {
		return createDocument(String.valueOf(collectionId), documentKey, value, waitForSync);
	}

	/**
	 * Creates a document in the collection defined by the collection's name.
	 * This method allows to define to documents key. Note that the collection's
	 * property CollectionKeyOption.allowUserKeys has to be set accordingly.
	 *
	 * @param collectionName
	 *            The name of the collection
	 * @param documentKey
	 *            the desired document key
	 * @param value
	 *            An object containing the documents attributes
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 * @see CollectionKeyOption#allowUserKeys
	 */
	public <T> DocumentEntity<T> createDocument(
		final String collectionName,
		final String documentKey,
		final T value,
		final Boolean waitForSync) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, waitForSync);
	}

	/**
	 * This method replaces the content of the document defined by documentId.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> replaceDocument(final long collectionId, final long documentId, final Object value)
			throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null);
	}

	/**
	 * This method replaces the content of the document defined by documentId.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(final String collectionName, final long documentId, final T value)
			throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null);
	}

	/**
	 * This method replaces the content of the document defined by documentKey.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(final long collectionId, final String documentKey, final T value)
			throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, documentKey), value, null, null);
	}

	/**
	 * This method replaces the content of the document defined by documentKey.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(final String collectionName, final String documentKey, final T value)
			throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, documentKey), value, null, null);
	}

	/**
	 * This method replaces the content of the document defined by
	 * documentHandle.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(final String documentHandle, final T value) throws ArangoException {
		return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, null, null);
	}

	/**
	 * This method replaces the content of the document defined by documentId.
	 * This method offers a parameter rev (revision). If the revision of the
	 * document on the server does not match the given revision an error is
	 * thrown.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the new attributes of the document.
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<T> a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(
		final long collectionId,
		final long documentId,
		final T value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, rev, waitForSync);
	}

	/**
	 * This method replaces the content of the document defined by documentId.
	 * This method offers a parameter rev (revision). If the revision of the
	 * document on the server does not match the given revision an error is
	 * thrown.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the new attributes of the document.
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> replaceDocument(
		final String collectionName,
		final long documentId,
		final Object value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, rev,
			waitForSync);
	}

	/**
	 * This method replaces the content of the document defined by documentKey.
	 * This method offers a parameter rev (revision). If the revision of the
	 * document on the server does not match the given revision an error is
	 * thrown.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the new attributes of the document.
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> replaceDocument(
		final long collectionId,
		final String documentKey,
		final Object value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, documentKey), value, rev, waitForSync);
	}

	/**
	 * This method replaces the content of the document defined by documentKey.
	 * This method offers a parameter rev (revision). If the revision of the
	 * document on the server does not match the given revision an error is
	 * thrown.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the new attributes of the document.
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> replaceDocument(
		final String collectionName,
		final String documentKey,
		final Object value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, documentKey), value, rev, waitForSync);
	}

	/**
	 * This method replaces the content of the document defined by
	 * documentHandle. This method offers a parameter rev (revision). If the
	 * revision of the document on the server does not match the given revision
	 * an error is thrown.
	 *
	 * @param documentHandle
	 *            The document's handle.
	 * @param value
	 *            An object containing the new attributes of the document.
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> replaceDocument(
		final String documentHandle,
		final T value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, rev, waitForSync);
	}

	/**
	 * This method updates a document defined by documentId.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(final long collectionId, final long documentId, final Object value)
			throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null);
	}

	/**
	 * This method updates a document defined by documentId.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentId
	 *            The document's id.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(final String collectionName, final long documentId, final Object value)
			throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null,
			null);
	}

	/**
	 * This method updates a document defined by documentKey.
	 *
	 * @param collectionId
	 *            The collection's id.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(final long collectionId, final String documentKey, final Object value)
			throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null);
	}

	/**
	 * This method updates a document defined by documentKey.
	 *
	 * @param collectionName
	 *            The collection's name.
	 * @param documentKey
	 *            The document's key.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(final String collectionName, final String documentKey, final Object value)
			throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null);
	}

	/**
	 * This method updates a document defined by documentHandle.
	 *
	 * @param documentHandle
	 *            The document's handle.
	 * @param value
	 *            An object containing the documents attributes
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> updateDocument(final String documentHandle, final T value) throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, null);
	}

	/**
	 * This method updates a document defined by documentId.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id.
	 * @param value
	 *            An object containing the documents attributes
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final long collectionId,
		final long documentId,
		final Object value,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null,
			keepNull);
	}

	/**
	 * This method updates a document defined by documentId.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id.
	 * @param value
	 *            An object containing the documents attributes
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final String collectionName,
		final long documentId,
		final Object value,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null,
			keepNull);
	}

	/**
	 * This method updates a document defined by documentKey.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key.
	 * @param value
	 *            An object containing the documents attributes
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final long collectionId,
		final String documentKey,
		final Object value,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, keepNull);
	}

	/**
	 * This method updates a document defined by documentKey.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key.
	 * @param value
	 *            An object containing the documents attributes
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final String collectionName,
		final String documentKey,
		final Object value,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, keepNull);
	}

	/**
	 * This method updates a document defined by documentKey.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @param value
	 *            An object containing the documents attributes
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> updateDocument(final String documentHandle, final T value, final Boolean keepNull)
			throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, keepNull);
	}

	/**
	 * This method updates a document defined by documentId. This method offers
	 * a parameter rev (revision). If the revision of the document on the server
	 * does not match the given revision an error is thrown.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id.
	 * @param value
	 *            An object containing the documents attributes
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final long collectionId,
		final long documentId,
		final Object value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, rev, waitForSync,
			keepNull);
	}

	/**
	 * This method updates a document defined by documentId. This method offers
	 * a parameter rev (revision). If the revision of the document on the server
	 * does not match the given revision an error is thrown.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id.
	 * @param value
	 *            An object containing the documents attributes
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final String collectionName,
		final long documentId,
		final Object value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, rev, waitForSync,
			keepNull);
	}

	/**
	 * This method updates a document defined by documentKey. This method offers
	 * a parameter rev (revision). If the revision of the document on the server
	 * does not match the given revision an error is thrown.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key.
	 * @param value
	 *            An object containing the documents attributes
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final long collectionId,
		final String documentKey,
		final Object value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, rev, waitForSync, keepNull);
	}

	/**
	 * This method updates a document defined by documentKey. This method offers
	 * a parameter rev (revision). If the revision of the document on the server
	 * does not match the given revision an error is thrown.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key.
	 * @param value
	 *            An object containing the documents attributes
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> updateDocument(
		final String collectionName,
		final String documentKey,
		final Object value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, rev, waitForSync, keepNull);
	}

	/**
	 * This method updates a document defined by documentHandle. This method
	 * offers a parameter rev (revision). If the revision of the document on the
	 * server does not match the given revision an error is thrown.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @param value
	 *            An object containing the documents attributes
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<?>
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> updateDocument(
		final String documentHandle,
		final T value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, rev, waitForSync, keepNull);
	}

	/**
	 * This method returns all document handles from a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @return List<String> - The list of document handles
	 * @throws ArangoException
	 */
	public List<String> getDocuments(final String collectionName) throws ArangoException {
		return documentDriver.getDocuments(getDefaultDatabase(), collectionName);
	}

	/**
	 * The exists method determines whether a document exists given its
	 * identifier. Instead of returning the found document or an error, this
	 * method will return either true or false. It can thus be used for easy
	 * existence checks.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id
	 * @return true, if the document exists
	 * @throws ArangoException
	 */
	public boolean exists(final long collectionId, final long documentId) throws ArangoException {
		return exists(createDocumentHandle(collectionId, String.valueOf(documentId)));
	}

	/**
	 * The exists method determines whether a document exists given its
	 * identifier. Instead of returning the found document or an error, this
	 * method will return either true or false. It can thus be used for easy
	 * existence checks.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id
	 * @return true, if the document exists
	 * @throws ArangoException
	 */
	public boolean exists(final String collectionName, final long documentId) throws ArangoException {
		return exists(createDocumentHandle(collectionName, String.valueOf(documentId)));
	}

	/**
	 * The exists method determines whether a document exists given its
	 * identifier. Instead of returning the found document or an error, this
	 * method will return either true or false. It can thus be used for easy
	 * existence checks.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key
	 * @return true, if the document exists
	 * @throws ArangoException
	 */
	public boolean exists(final long collectionId, final String documentKey) throws ArangoException {
		return exists(createDocumentHandle(collectionId, documentKey));
	}

	/**
	 * The exists method determines whether a document exists given its
	 * identifier. Instead of returning the found document or an error, this
	 * method will return either true or false. It can thus be used for easy
	 * existence checks.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key
	 * @return true, if the document exists
	 * @throws ArangoException
	 */
	public boolean exists(final String collectionName, final String documentKey) throws ArangoException {
		return exists(createDocumentHandle(collectionName, documentKey));
	}

	/**
	 * The exists method determines whether a document exists given its
	 * identifier. Instead of returning the found document or an error, this
	 * method will return either true or false. It can thus be used for easy
	 * existence checks.
	 *
	 * @param documentHandle
	 *            The document handle
	 * @return true, if the document exists
	 * @throws ArangoException
	 */
	public boolean exists(final String documentHandle) throws ArangoException {
		try {
			documentDriver.checkDocument(getDefaultDatabase(), documentHandle);
		} catch (final ArangoException e) {
			if (e.getCode() == ErrorNums.ERROR_HTTP_NOT_FOUND) {
				return false;
			}
			throw e;
		}
		return true;
	}

	/**
	 * This method returns the current revision of a document.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id
	 * @return the document revision number
	 * @throws ArangoException
	 */
	public long checkDocument(final long collectionId, final long documentId) throws ArangoException {
		return checkDocument(createDocumentHandle(collectionId, String.valueOf(documentId)));
	}

	/**
	 * This method returns the current revision of a document.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id
	 * @return the document revision number
	 * @throws ArangoException
	 */
	public long checkDocument(final String collectionName, final long documentId) throws ArangoException {
		return checkDocument(createDocumentHandle(collectionName, String.valueOf(documentId)));
	}

	/**
	 * This method returns the current revision of a document.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key
	 * @return the document revision number
	 * @throws ArangoException
	 */
	public long checkDocument(final long collectionId, final String documentKey) throws ArangoException {
		return checkDocument(createDocumentHandle(collectionId, documentKey));
	}

	/**
	 * This method returns the current revision of a document.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key
	 * @return the document revision number
	 * @throws ArangoException
	 */
	public long checkDocument(final String collectionName, final String documentKey) throws ArangoException {
		return checkDocument(createDocumentHandle(collectionName, documentKey));
	}

	/**
	 * This method returns the current revision of a document.
	 *
	 * @param documentHandle
	 *            The document handle
	 * @return the document revision number
	 * @throws ArangoException
	 */
	public long checkDocument(final String documentHandle) throws ArangoException {
		return documentDriver.checkDocument(getDefaultDatabase(), documentHandle);
	}

	/**
	 * Returns a document entity.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(final long collectionId, final long documentId, final Class<T> clazz)
			throws ArangoException {
		return getDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), clazz);
	}

	/**
	 * Returns a document entity.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(final String collectionName, final long documentId, final Class<T> clazz)
			throws ArangoException {
		return getDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), clazz);
	}

	/**
	 * Returns a document entity.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(final long collectionId, final String documentKey, final Class<T> clazz)
			throws ArangoException {
		return getDocument(createDocumentHandle(collectionId, documentKey), clazz);
	}

	/**
	 * Returns a document entity.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(
		final String collectionName,
		final String documentKey,
		final Class<T> clazz) throws ArangoException {
		return getDocument(createDocumentHandle(collectionName, documentKey), clazz);
	}

	/**
	 * Returns a document entity.
	 *
	 * @param documentHandle
	 *            The document handle
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(final String documentHandle, final Class<T> clazz) throws ArangoException {
		return documentDriver.getDocument(getDefaultDatabase(), documentHandle, clazz, null, null);
	}

	/**
	 * Returns a document entity. Note that the *ifNoneMatchRevision* and
	 * *ifMatchRevision* can not be used at the same time, one of these two has
	 * to be null.
	 *
	 * @param documentHandle
	 *            The document handle
	 * @param clazz
	 *            The expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @param ifNoneMatchRevision
	 *            if set the document is only returned id it has a different
	 *            revision.
	 * @param ifMatchRevision
	 *            if set the document is only returned id it has the same
	 *            revision.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> DocumentEntity<T> getDocument(
		final String documentHandle,
		final Class<T> clazz,
		final Long ifNoneMatchRevision,
		final Long ifMatchRevision) throws ArangoException {
		return documentDriver.getDocument(getDefaultDatabase(), documentHandle, clazz, ifNoneMatchRevision,
			ifMatchRevision);
	}

	/**
	 * Deletes a document from the database.
	 * 
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final long collectionId, final long documentId) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, String.valueOf(documentId)));
	}

	/**
	 * Deletes a document from the database.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String collectionName, final long documentId) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, String.valueOf(documentId)));
	}

	/**
	 * Deletes a document from the database.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final long collectionId, final String documentKey) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, documentKey));
	}

	/**
	 * Deletes a document from the database.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String collectionName, final String documentKey)
			throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, documentKey));
	}

	/**
	 * Deletes a document from the database.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String documentHandle) throws ArangoException {
		return documentDriver.deleteDocument(getDefaultDatabase(), documentHandle, null);
	}

	/**
	 * Deletes a document from the database. This method offers a parameter rev
	 * (revision). If the revision of the document on the server does not match
	 * the given revision an error is thrown.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentId
	 *            The document id.
	 * @param rev
	 *            The desired revision
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final long collectionId, final long documentId, final Long rev)
			throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), rev);
	}

	/**
	 * Deletes a document from the database. This method offers a parameter rev
	 * (revision). If the revision of the document on the server does not match
	 * the given revision an error is thrown.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentId
	 *            The document id.
	 * @param rev
	 *            The desired revision
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String collectionName, final long documentId, final Long rev)
			throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), rev);
	}

	/**
	 * Deletes a document from the database. This method offers a parameter rev
	 * (revision). If the revision of the document on the server does not match
	 * the given revision an error is thrown.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param documentKey
	 *            The document key.
	 * @param rev
	 *            The desired revision
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final long collectionId, final String documentKey, final Long rev)
			throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, documentKey), rev);
	}

	/**
	 * Deletes a document from the database. This method offers a parameter rev
	 * (revision). If the revision of the document on the server does not match
	 * the given revision an error is thrown.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param documentKey
	 *            The document key.
	 * @param rev
	 *            The desired revision
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String collectionName, final String documentKey, final Long rev)
			throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, documentKey), rev);
	}

	/**
	 * Deletes a document from the database. This method offers a parameter rev
	 * (revision). If the revision of the document on the server does not match
	 * the given revision an error is thrown.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @param rev
	 *            The desired revision
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public DocumentEntity<?> deleteDocument(final String documentHandle, final Long rev) throws ArangoException {
		return documentDriver.deleteDocument(getDefaultDatabase(), documentHandle, rev);
	}

	/**
	 * This method validates a given AQL query string and returns a CursorEntity
	 *
	 * @param query
	 *            an AQL query as string
	 * @return a CursorEntity object
	 * @throws ArangoException
	 */
	public CursorEntity<?> validateQuery(final String query) throws ArangoException {
		return cursorDriver.validateQuery(getDefaultDatabase(), query);
	}

	/**
	 * Continues data retrieval for an existing cursor
	 *
	 * @param cursorId
	 *            The id of a cursor.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a CursorEntity object
	 * @throws ArangoException
	 */
	public <T> CursorEntity<T> continueQuery(final long cursorId, final Class<?>... clazz) throws ArangoException {
		return cursorDriver.continueQuery(getDefaultDatabase(), cursorId, clazz);
	}

	/**
	 * Deletes a cursor from the database.
	 *
	 * @param cursorId
	 *            The id of a cursor.
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity finishQuery(final long cursorId) throws ArangoException {
		return cursorDriver.finishQuery(getDefaultDatabase(), cursorId);
	}

	/**
	 * This method executes an AQL query and returns a CursorEntity.
	 *
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @param calcCount
	 *            if set to true the result count is returned
	 * @param batchSize
	 *            the batch size of the result cursor (The batch size has to be
	 *            greater than 0)
	 * @param fullCount
	 *            if set to true, then all results before the final LIMIT will
	 *            be counted
	 * @return a CursorEntity object
	 * @throws ArangoException
	 */
	public <T> CursorEntity<T> executeCursorEntityQuery(
		final String query,
		final Map<String, Object> bindVars,
		final Boolean calcCount,
		final Integer batchSize,
		final Boolean fullCount,
		final Class<?>... clazz) throws ArangoException {

		final AqlQueryOptions aqlQueryOptions = new AqlQueryOptions().setCount(calcCount).setBatchSize(batchSize)
				.setFullCount(fullCount);

		return cursorDriver.executeCursorEntityQuery(getDefaultDatabase(), query, bindVars, aqlQueryOptions, clazz);
	}

	/**
	 * Creates a default AqlQueryOptions object
	 * 
	 * @return default AqlQueryOptions object
	 */
	public AqlQueryOptions getDefaultAqlQueryOptions() {
		return new AqlQueryOptions().setBatchSize(configure.getBatchSize()).setCount(false).setFullCount(false);
	}

	/**
	 * This method executes an AQL query and returns a DocumentCursor
	 *
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param aqlQueryOptions
	 *            AQL query options (null for default values)
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursor<T>
	 * @throws ArangoException
	 */
	public <T> DocumentCursor<T> executeDocumentQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<T> clazz) throws ArangoException {

		@SuppressWarnings("unchecked")
		final DocumentCursorResult<T, DocumentEntity<T>> baseCursor = cursorDriver.executeBaseCursorQuery(
			getDefaultDatabase(), query, bindVars, getAqlQueryOptions(aqlQueryOptions), DocumentEntity.class, clazz);
		return new DocumentCursor<T>(baseCursor);
	}

	/**
	 * This method executes an AQL query and returns a CursorResult
	 *
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return CursorResult<T>
	 * @throws ArangoException
	 */
	public <T> CursorResult<T> executeAqlQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<T> clazz) throws ArangoException {

		return cursorDriver.executeAqlQuery(getDefaultDatabase(), query, bindVars, getAqlQueryOptions(aqlQueryOptions),
			clazz);
	}

	/**
	 * Executes an AQL query and returns the raw JSON response
	 * 
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @return A JSON string with the results from server
	 * @throws ArangoException
	 */
	public String executeAqlQueryJSON(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		return cursorDriver.executeAqlQueryJSON(getDefaultDatabase(), query, bindVars,
			getAqlQueryOptions(aqlQueryOptions));
	}

	/**
	 * This method executes an AQL query and returns a DocumentCursorResult
	 *
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @param classDocumentEntity
	 *            the class the expected class is wrapped in (the class has to
	 *            extend DocumentEntity)
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursorResult<T, S>
	 * @throws ArangoException
	 */
	public <T, S extends DocumentEntity<T>> DocumentCursorResult<T, S> executeAqlQueryWithDocumentCursorResult(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<S> classDocumentEntity,
		final Class<T> clazz) throws ArangoException {

		return cursorDriver.executeBaseCursorQuery(getDefaultDatabase(), query, bindVars,
			getAqlQueryOptions(aqlQueryOptions), classDocumentEntity, clazz);
	}

	/**
	 * This method creates an index for a collection.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param type
	 *            the index type.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createIndex(
		final long collectionId,
		final IndexType type,
		final boolean unique,
		final String... fields) throws ArangoException {
		return createIndex(String.valueOf(collectionId), type, unique, fields);
	}

	/**
	 * This method creates an index for a collection.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param type
	 *            the index type.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param sparse
	 *            if set to true the index will be sparse
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createIndex(
		final long collectionId,
		final IndexType type,
		final boolean unique,
		final boolean sparse,
		final String... fields) throws ArangoException {
		return createIndex(String.valueOf(collectionId), type, unique, sparse, fields);
	}

	/**
	 * This method creates an index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param type
	 *            the index type.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createIndex(
		final String collectionName,
		final IndexType type,
		final boolean unique,
		final String... fields) throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, type, unique, fields);
	}

	/**
	 * This method creates an index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param type
	 *            the index type.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param sparse
	 *            if set to true the index will be sparse
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createIndex(
		final String collectionName,
		final IndexType type,
		final boolean unique,
		final boolean sparse,
		final String... fields) throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, type, unique, sparse, fields);
	}

	/**
	 * This method creates a hash index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createHashIndex(final String collectionName, final boolean unique, final String... fields)
			throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.HASH, unique, fields);
	}

	/**
	 * This method creates a hash index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param sparse
	 *            if set to true the index will be sparse
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createHashIndex(
		final String collectionName,
		final boolean unique,
		final boolean sparse,
		final String... fields) throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.HASH, unique, sparse, fields);
	}

	/**
	 * This method creates a geo index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createGeoIndex(final String collectionName, final boolean unique, final String... fields)
			throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.GEO, unique, fields);
	}

	/**
	 * This method creates a skip list index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createSkipListIndex(final String collectionName, final boolean unique, final String... fields)
			throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.SKIPLIST, unique, fields);
	}

	/**
	 * This method creates a skip list index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param unique
	 *            if set to true the index will be a unique index
	 * @param sparse
	 *            if set to true the index will be sparse
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createSkipListIndex(
		final String collectionName,
		final boolean unique,
		final boolean sparse,
		final String... fields) throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, IndexType.SKIPLIST, unique, sparse,
			fields);
	}

	/**
	 * This method creates a full text index for a collection.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createFulltextIndex(final long collectionId, final String... fields) throws ArangoException {
		return createFulltextIndex(String.valueOf(collectionId), null, fields);
	}

	/**
	 * This method creates a full text index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createFulltextIndex(final String collectionName, final String... fields) throws ArangoException {
		return createFulltextIndex(collectionName, null, fields);
	}

	/**
	 * This method creates a full text index for a collection.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @param minLength
	 *            Minimum character length of words to index.
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createFulltextIndex(final long collectionId, final Integer minLength, final String... fields)
			throws ArangoException {
		return createFulltextIndex(String.valueOf(collectionId), minLength, fields);
	}

	/**
	 * This method creates a full text index for a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param minLength
	 *            Minimum character length of words to index.
	 * @param fields
	 *            the fields (document attributes) the index is created on
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity createFulltextIndex(final String collectionName, final Integer minLength, final String... fields)
			throws ArangoException {
		return indexDriver.createFulltextIndex(getDefaultDatabase(), collectionName, minLength, fields);
	}

	/**
	 * Deletes an index from a collection
	 *
	 * @param indexHandle
	 *            the index handle
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity deleteIndex(final String indexHandle) throws ArangoException {
		return indexDriver.deleteIndex(getDefaultDatabase(), indexHandle);
	}

	/**
	 * Returns an index from a collection.
	 *
	 * @param indexHandle
	 *            the index handle
	 * @return IndexEntity
	 * @throws ArangoException
	 */
	public IndexEntity getIndex(final String indexHandle) throws ArangoException {
		return indexDriver.getIndex(getDefaultDatabase(), indexHandle);
	}

	/**
	 * Returns all indices from a collection.
	 *
	 * @param collectionId
	 *            The collection id.
	 * @return IndexesEntity
	 * @throws ArangoException
	 */
	public IndexesEntity getIndexes(final long collectionId) throws ArangoException {
		return getIndexes(String.valueOf(collectionId));
	}

	/**
	 * Returns all indices from a collection.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @return IndexesEntity
	 * @throws ArangoException
	 */
	public IndexesEntity getIndexes(final String collectionName) throws ArangoException {
		return indexDriver.getIndexes(getDefaultDatabase(), collectionName);
	}

	/**
	 * Returns the server log, for the parameters *logLevel* and *logLevelUpTo*
	 * please note the following: fatal or 0 error or 1 warning or 2 info or 3
	 * debug or 4 The default value is info.
	 *
	 * @param logLevel
	 *            if set only logs with this *logLevel* are returned
	 * @param logLevelUpTo
	 *            if set all logs up to the *logLevelUpTo* are returned
	 * @param start
	 *            Returns all log entries such that their log entry identifier
	 *            (lid value) is greater or equal to start.
	 * @param size
	 *            Restricts the result to at most size log entries.
	 * @param offset
	 *            Starts to return log entries skipping the first offset log
	 *            entries.
	 * @param sortAsc
	 *            if set to true the default sort order (descending) is reverted
	 *            to ascending
	 * @param text
	 *            Only return the log entries containing the text specified in
	 *            text.
	 * @return a AdminLogEntity object
	 * @throws ArangoException
	 */
	public AdminLogEntity getServerLog(
		final Integer logLevel,
		final Boolean logLevelUpTo,
		final Integer start,
		final Integer size,
		final Integer offset,
		final Boolean sortAsc,
		final String text) throws ArangoException {
		return adminDriver.getServerLog(logLevel, logLevelUpTo, start, size, offset, sortAsc, text);
	}

	/**
	 * Returns the current statistics
	 *
	 * @return StatisticsEntity
	 * @throws ArangoException
	 */
	public StatisticsEntity getStatistics() throws ArangoException {
		return adminDriver.getStatistics();
	}

	/**
	 * Returns the statistics description
	 *
	 * @return StatisticsDescriptionEntity
	 * @throws ArangoException
	 */
	public StatisticsDescriptionEntity getStatisticsDescription() throws ArangoException {
		return adminDriver.getStatisticsDescription();
	}

	/**
	 * Returns the database version
	 *
	 * @return ArangoVersion
	 * @throws ArangoException
	 */
	public ArangoVersion getVersion() throws ArangoException {
		return adminDriver.getVersion();
	}

	/**
	 * Returns the current server time
	 *
	 * @return ArangoUnixTime
	 * @throws ArangoException
	 */
	public ArangoUnixTime getTime() throws ArangoException {
		return adminDriver.getTime();
	}

	/**
	 * Triggers the routes reloading in ArangoDB
	 *
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity reloadRouting() throws ArangoException {
		return adminDriver.reloadRouting();
	}

	/**
	 * Executes a javascript code.
	 *
	 * @param jsCode
	 *            a javascript function as string
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity executeScript(final String jsCode) throws ArangoException {
		return adminDriver.executeScript(getDefaultDatabase(), jsCode);
	}

	/**
	 * This will find all documents matching a given example.
	 *
	 * @param collectionName
	 *            - The collection name.
	 * @param example
	 *            The example as a map.
	 * @param skip
	 *            The number of documents to skip in the query.
	 * @param limit
	 *            The maximal amount of documents to return. The skip is applied
	 *            before the limit restriction.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursor<T>
	 * @throws ArangoException
	 */
	public <T> DocumentCursor<T> executeSimpleByExampleDocuments(
		final String collectionName,
		final Map<String, Object> example,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleByExampleDocuments(getDefaultDatabase(), collectionName, example, skip, limit,
			clazz);
	}

	/**
	 * Returns all documents of a collections.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param skip
	 *            The number of documents to skip in the query.
	 * @param limit
	 *            The maximal amount of documents to return. The skip is applied
	 *            before the limit restriction.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursor<T>
	 * @throws ArangoException
	 */
	public <T> DocumentCursor<T> executeSimpleAllDocuments(
		final String collectionName,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAllDocuments(getDefaultDatabase(), collectionName, skip, limit, clazz);
	}

	/**
	 * Returns the first document matching the example
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param example
	 *            The example as a map.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a ScalarExampleEntity object
	 * @throws ArangoException
	 */
	public <T> ScalarExampleEntity<T> executeSimpleFirstExample(
		final String collectionName,
		final Map<String, Object> example,
		final Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleFirstExample(getDefaultDatabase(), collectionName, example, clazz);
	}

	/**
	 * Returns a random document from the collection
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return a ScalarExampleEntity object
	 * @throws ArangoException
	 */
	public <T> ScalarExampleEntity<T> executeSimpleAny(final String collectionName, final Class<T> clazz)
			throws ArangoException {
		return simpleDriver.executeSimpleAny(getDefaultDatabase(), collectionName, clazz);
	}

	/**
	 * This will find all documents within a given range.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param attribute
	 *            The attribute path to check.
	 * @param left
	 *            The lower bound
	 * @param right
	 *            The upper bound
	 * @param closed
	 *            If true, use interval including left and right, otherwise
	 *            exclude right, but include left.
	 * @param skip
	 *            The number of documents to skip in the query.
	 * @param limit
	 *            The maximal amount of documents to return. The skip is applied
	 *            before the limit restriction.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursor<T>
	 * @throws ArangoException
	 */
	public <T> DocumentCursor<T> executeSimpleRangeWithDocuments(
		final String collectionName,
		final String attribute,
		final Object left,
		final Object right,
		final Boolean closed,
		final int skip,
		final int limit,
		final Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleRangeWithDocuments(getDefaultDatabase(), collectionName, attribute, left,
			right, closed, skip, limit, clazz);
	}

	/**
	 * This will find all documents from the collection that match the fulltext
	 * query specified in query. In order to use the fulltext operator, a
	 * fulltext index must be defined for the collection and the specified
	 * attribute.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param attribute
	 *            The attribute path to check.
	 * @param query
	 *            The fulltext query as string.
	 * @param skip
	 *            The number of documents to skip in the query.
	 * @param limit
	 *            The maximal amount of documents to return. The skip is applied
	 *            before the limit restriction.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @return DocumentCursor<T>
	 * @throws ArangoException
	 */
	public <T> DocumentCursor<T> executeSimpleFulltextWithDocuments(
		final String collectionName,
		final String attribute,
		final String query,
		final int skip,
		final int limit,
		final String index,
		final Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleFulltextWithDocuments(getDefaultDatabase(), collectionName, attribute, query,
			skip, limit, index, clazz);
	}

	/**
	 * This will remove all documents in the collection that match the specified
	 * example object.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param example
	 *            The example as a map.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param limit
	 *            limits the amount of documents which will be deleted.
	 * @return SimpleByResultEntity
	 * @throws ArangoException
	 */
	public SimpleByResultEntity executeSimpleRemoveByExample(
		final String collectionName,
		final Map<String, Object> example,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {
		return simpleDriver.executeSimpleRemoveByExample(getDefaultDatabase(), collectionName, example, waitForSync,
			limit);
	}

	/**
	 * This will replace all documents in the collection that match the
	 * specified example object.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param example
	 *            The example as a map.
	 * @param newValue
	 *            The new values.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param limit
	 *            limits the amount of documents which will be replaced.
	 * @return SimpleByResultEntity
	 * @throws ArangoException
	 */
	public SimpleByResultEntity executeSimpleReplaceByExample(
		final String collectionName,
		final Map<String, Object> example,
		final Map<String, Object> newValue,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {
		return simpleDriver.executeSimpleReplaceByExample(getDefaultDatabase(), collectionName, example, newValue,
			waitForSync, limit);
	}

	/**
	 * This will update all documents in the collection that match the specified
	 * example object.
	 *
	 * @param collectionName
	 *            The collection name.
	 * @param example
	 *            The example as a map.
	 * @param newValue
	 *            The new values.
	 * @param keepNull
	 *            - If true null values are kept.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param limit
	 *            limits the amount of documents which will be updated.
	 * @return SimpleByResultEntity
	 * @throws ArangoException
	 */
	public SimpleByResultEntity executeSimpleUpdateByExample(
		final String collectionName,
		final Map<String, Object> example,
		final Map<String, Object> newValue,
		final Boolean keepNull,
		final Boolean waitForSync,
		final Integer limit) throws ArangoException {
		return simpleDriver.executeSimpleUpdateByExample(getDefaultDatabase(), collectionName, example, newValue,
			keepNull, waitForSync, limit);
	}

	/**
	 * Creates a database user.
	 *
	 * @param username
	 *            the username as string
	 * @param passwd
	 *            the password as string
	 * @param active
	 *            if true the user is active
	 * @param extra
	 *            additional user data
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity createUser(
		final String username,
		final String passwd,
		final Boolean active,
		final Map<String, Object> extra) throws ArangoException {
		return usersDriver.createUser(username, passwd, active, extra);
	}

	/**
	 * Replaces the data of a database user.
	 *
	 * @param username
	 *            the username as string
	 * @param passwd
	 *            the password as string
	 * @param active
	 *            if true the user is active
	 * @param extra
	 *            additional user data
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity replaceUser(
		final String username,
		final String passwd,
		final Boolean active,
		final Map<String, Object> extra) throws ArangoException {
		return usersDriver.replaceUser(username, passwd, active, extra);
	}

	/**
	 * Updates the data of a database user.
	 *
	 * @param username
	 *            the username as string
	 * @param passwd
	 *            the password as string
	 * @param active
	 *            if true the user is active
	 * @param extra
	 *            additional user data
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity updateUser(
		final String username,
		final String passwd,
		final Boolean active,
		final Map<String, Object> extra) throws ArangoException {
		return usersDriver.updateUser(username, passwd, active, extra);
	}

	/**
	 * Deletes a database user.
	 *
	 * @param username
	 *            the username as string
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity deleteUser(final String username) throws ArangoException {
		return usersDriver.deleteUser(username);
	}

	/**
	 * Returns a database user.
	 *
	 * @param username
	 *            the username as string
	 * @return UserEntity
	 * @throws ArangoException
	 */
	public UserEntity getUser(final String username) throws ArangoException {
		return usersDriver.getUser(username);
	}

	/**
	 * Grants the User access to the given database.
	 * 
	 * @param username
	 *            the username as string
	 * @param database
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity grantDatabaseAccess(String username, String database) throws ArangoException {
		return usersDriver.grantDatabaseAccess(username, database);
	}

	/**
	 * Returns all database user as document.
	 *
	 * @return List<DocumentEntity<UserEntity>>
	 * @throws ArangoException
	 */
	public List<DocumentEntity<UserEntity>> getUsersDocument() throws ArangoException {
		final DocumentCursor<UserEntity> documentCursor = simpleDriver.executeSimpleAllDocuments(DATABASE_SYSTEM,
			COLLECTION_USERS, 0, 0, UserEntity.class);
		return documentCursor.asList();
	}

	/**
	 * Returns all database user.
	 *
	 * @return List<UserEntity>
	 * @throws ArangoException
	 */
	public List<UserEntity> getUsers() throws ArangoException {
		final DocumentCursor<UserEntity> documentCursor = simpleDriver.executeSimpleAllDocuments(DATABASE_SYSTEM,
			COLLECTION_USERS, 0, 0, UserEntity.class);
		return documentCursor.asEntityList();
	}

	/**
	 * Creates documents in a collection.
	 *
	 * @param collection
	 *            the collection as a string
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @return ImportResultEntity
	 * @throws ArangoException
	 */
	public ImportResultEntity importDocuments(final String collection, final Collection<?> values)
			throws ArangoException {
		return importDriver.importDocuments(getDefaultDatabase(), collection, values, new ImportOptions());
	}

	/**
	 * Creates documents in a collection.
	 *
	 * @param collection
	 *            the collection as a string
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @param importOptions
	 *            options for importing documents
	 * @return ImportResultEntity
	 * @throws ArangoException
	 */
	public ImportResultEntity importDocuments(
		final String collection,
		final Collection<?> values,
		final ImportOptions importOptions) throws ArangoException {
		return importDriver.importDocuments(getDefaultDatabase(), collection, values, importOptions);
	}

	/**
	 * Creates documents in a collection.
	 *
	 * @param collection
	 *            the collection as a string
	 * @param values
	 *            a raw string containing JSON data
	 * @param importOptions
	 *            options for importing documents
	 * @return ImportResultEntity
	 * @throws ArangoException
	 */
	public ImportResultEntity importDocumentsRaw(
		final String collection,
		final String values,
		final ImportOptionsRaw importOptionsRaw) throws ArangoException {
		return importDriver.importDocumentsRaw(getDefaultDatabase(), collection, values, importOptionsRaw);
	}

	/**
	 * Creates documents in the collection.
	 *
	 * @param collection
	 *            the collection as a string
	 * @param headerValues
	 *            a list of lists that will be stored as documents
	 * @return ImportResultEntity
	 * @throws ArangoException
	 */
	public ImportResultEntity importDocumentsByHeaderValues(
		final String collection,
		final Collection<? extends Collection<?>> headerValues) throws ArangoException {
		return importDriver.importDocumentsByHeaderValues(getDefaultDatabase(), collection, headerValues);
	}

	/**
	 * Returns the current database
	 *
	 * @return DatabaseEntity
	 * @throws ArangoException
	 */
	public DatabaseEntity getCurrentDatabase() throws ArangoException {
		return databaseDriver.getCurrentDatabase();
	}

	/**
	 * Returns all databases
	 *
	 * @return StringsResultEntity
	 * @throws ArangoException
	 */
	public StringsResultEntity getDatabases() throws ArangoException {
		return getDatabases(false);
	}

	/**
	 * Returns all databases
	 *
	 * @param currentUserAccessableOnly
	 *            If true only the databases are returned that the current user
	 *            can access
	 * @return StringsResultEntity
	 * @throws ArangoException
	 */
	public StringsResultEntity getDatabases(final boolean currentUserAccessableOnly) throws ArangoException {
		return databaseDriver.getDatabases(currentUserAccessableOnly, null, null);
	}

	/**
	 * Returns all databases the user identified by his credentials can access
	 *
	 * @param username
	 *            the username as string
	 * @param password
	 *            the password as string
	 * @return StringsResultEntity
	 * @throws ArangoException
	 */
	public StringsResultEntity getDatabases(final String username, final String password) throws ArangoException {
		return databaseDriver.getDatabases(true, username, password);
	}

	/**
	 * This method creates a database
	 *
	 * @param database
	 *            the database name as a string
	 * @param users
	 *            a list of users which are supposed to have access to the
	 *            database
	 * @return BooleanResultEntity
	 * @throws ArangoException
	 */
	public BooleanResultEntity createDatabase(final String database, final UserEntity... users) throws ArangoException {
		return databaseDriver.createDatabase(database, users);
	}

	/**
	 * This method deletes a database
	 *
	 * @param database
	 *            the database name as a string
	 * @return BooleanResultEntity
	 * @throws ArangoException
	 */
	public BooleanResultEntity deleteDatabase(final String database) throws ArangoException {
		return databaseDriver.deleteDatabase(database);
	}

	/**
	 * This method creates an endpoint.
	 *
	 * @param endpoint
	 *            the endpoint as string
	 * @param databases
	 *            a list of databases that are allowed on this endpoint
	 * @return BooleanResultEntity
	 */
	public BooleanResultEntity createEndpoint(final String endpoint, final String... databases) throws ArangoException {
		return endpointDriver.createEndpoint(endpoint, databases);
	}

	/**
	 * This method returns all endpoints.
	 *
	 * @return List<Endpoint>
	 */
	public List<Endpoint> getEndpoints() throws ArangoException {
		return endpointDriver.getEndpoints();
	}

	/**
	 * This method deletes an endpoint
	 *
	 * @param endpoint
	 *            the endpoint as string
	 * @return BooleanResultEntity
	 */
	public BooleanResultEntity deleteEndpoint(final String endpoint) throws ArangoException {
		return endpointDriver.deleteEndpoint(endpoint);
	}

	/**
	 * Returns the list of collections and indexes available on the server. This
	 * list can be used by replication clients to initiate an initial sync with
	 * the server.
	 *
	 * @return ReplicationInventoryEntity
	 * @throws ArangoException
	 */
	public ReplicationInventoryEntity getReplicationInventory() throws ArangoException {
		return replicationDriver.getReplicationInventory(getDefaultDatabase(), null);
	}

	/**
	 * Returns the list of collections and indexes available on the server. This
	 * list can be used by replication clients to initiate an initial sync with
	 * the server.
	 *
	 * @param includeSystem
	 *            if true the system collections are included into the result
	 * @return ReplicationInventoryEntity
	 * @throws ArangoException
	 */
	public ReplicationInventoryEntity getReplicationInventory(final boolean includeSystem) throws ArangoException {
		return replicationDriver.getReplicationInventory(getDefaultDatabase(), includeSystem);
	}

	/**
	 * Returns the data from the collection for the requested range.
	 *
	 * @param collectionName
	 *            the collection name
	 * @param from
	 *            Lower bound tick value for results.
	 * @param to
	 *            Upper bound tick value for results.
	 * @param chunkSize
	 *            Approximate maximum size of the returned result.
	 * @param ticks
	 *            Whether or not to include tick values in the dump. Default
	 *            value is true.
	 * @param clazz
	 *            the expected class, the result from the server request is
	 *            deserialized to an instance of this class.
	 * @param handler
	 *            a handler object that processes the dump
	 * @throws ArangoException
	 */
	public <T> void getReplicationDump(
		final String collectionName,
		final Long from,
		final Long to,
		final Integer chunkSize,
		final Boolean ticks,
		final Class<T> clazz,
		final DumpHandler<T> handler) throws ArangoException {

		replicationDriver.getReplicationDump(getDefaultDatabase(), collectionName, from, to, chunkSize, ticks, clazz,
			handler);

	}

	/**
	 * Starts a full data synchronization from a remote endpoint into the local
	 * ArangoDB database.
	 *
	 * @param endpoint
	 *            the endpoint as string
	 * @param database
	 *            the database name as a string
	 * @param username
	 *            the username as string
	 * @param password
	 *            the password as string
	 * @param restrictType
	 *            collection filtering. When specified, the allowed values are
	 *            include or exclude.
	 * @param restrictCollections
	 *            If restrictType is include, only the specified collections
	 *            will be sychronised. If restrictType is exclude, all but the
	 *            specified collections will be synchronized.
	 * @return ReplicationSyncEntity
	 * @throws ArangoException
	 */
	public ReplicationSyncEntity syncReplication(
		final String endpoint,
		final String database,
		final String username,
		final String password,
		final RestrictType restrictType,
		final String... restrictCollections) throws ArangoException {
		return replicationDriver.syncReplication(getDefaultDatabase(), endpoint, database, username, password,
			restrictType, restrictCollections);
	}

	/**
	 * Returns the servers id. The id is also returned by other replication API
	 * methods, and this method is an easy means of determining a server's id.
	 *
	 * @return String
	 * @throws ArangoException
	 */
	public String getReplicationServerId() throws ArangoException {
		return replicationDriver.getReplicationServerId();
	}

	/**
	 * Starts the replication logger
	 *
	 * @return boolean
	 * @throws ArangoException
	 */
	public boolean startReplicationLogger() throws ArangoException {
		return replicationDriver.startReplicationLogger(getDefaultDatabase());
	}

	/**
	 * Stops the replication logger
	 *
	 * @return boolean
	 * @throws ArangoException
	 */
	public boolean stopReplicationLogger() throws ArangoException {
		return replicationDriver.stopReplicationLogger(getDefaultDatabase());
	}

	/**
	 * Returns the current state of the server's replication logger. The state
	 * will include information about whether the logger is running and about
	 * the last logged tick value.
	 *
	 * @return ReplicationLoggerStateEntity
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationLoggerStateEntity getReplicationLoggerState() throws ArangoException {
		return replicationDriver.getReplicationLoggerState(getDefaultDatabase());
	}

	/**
	 * Returns the configuration of the replication logger
	 *
	 * @return ReplicationLoggerConfigEntity
	 * @throws ArangoException
	 */
	public ReplicationLoggerConfigEntity getReplicationLoggerConfig() throws ArangoException {
		return replicationDriver.getReplicationLoggerConfig(getDefaultDatabase());
	}

	/**
	 * Sets the replication logger configuration
	 *
	 * @param autoStart
	 *            if true autoStart is activated
	 * @param logRemoteChanges
	 *            if true remote changes are logged
	 * @param maxEvents
	 *            the maximum amount of events to log
	 * @param maxEventsSize
	 *            the maximum size of events
	 * @return ReplicationLoggerConfigEntity
	 * @throws ArangoException
	 */
	public ReplicationLoggerConfigEntity setReplicationLoggerConfig(
		final Boolean autoStart,
		final Boolean logRemoteChanges,
		final Long maxEvents,
		final Long maxEventsSize) throws ArangoException {
		return replicationDriver.setReplicationLoggerConfig(getDefaultDatabase(), autoStart, logRemoteChanges,
			maxEvents, maxEventsSize);
	}

	/**
	 * Returns the configuration of the replication applier.
	 *
	 * @return ReplicationApplierConfigEntity
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierConfigEntity getReplicationApplierConfig() throws ArangoException {
		return replicationDriver.getReplicationApplierConfig(getDefaultDatabase());
	}

	/**
	 * Sets the configuration of the replication applier.
	 *
	 * @param endpoint
	 *            the logger server to connect to (e.g.
	 *            "tcp://192.168.173.13:8529").
	 * @param database
	 *            the name of the database on the endpoint.
	 * @param username
	 *            an optional ArangoDB username to use when connecting to the
	 *            endpoint
	 * @param password
	 *            the password to use when connecting to the endpoint.
	 * @param maxConnectRetries
	 *            the maximum number of connection attempts the applier will
	 *            make in a row. If the applier cannot establish a connection to
	 *            the endpoint in this number of attempts, it will stop itself.
	 * @param connectTimeout
	 *            the timeout (in seconds) when attempting to connect to the
	 *            endpoint. This value is used for each connection attempt.
	 * @param requestTimeout
	 *            the timeout (in seconds) for individual requests to the
	 *            endpoint.
	 * @param chunkSize
	 *            the requested maximum size for log transfer packets that is
	 *            used when the endpoint is contacted.
	 * @param autoStart
	 *            whether or not to auto-start the replication applier on (next
	 *            and following) server starts
	 * @param adaptivePolling
	 *            if set to true, the replication applier will fall to sleep for
	 *            an increasingly long period in case the logger server at the
	 *            endpoint does not have any more replication events to apply.
	 * @return ReplicationApplierConfigEntity
	 * @throws ArangoException
	 */
	public ReplicationApplierConfigEntity setReplicationApplierConfig(
		final String endpoint,
		final String database,
		final String username,
		final String password,
		final Integer maxConnectRetries,
		final Integer connectTimeout,
		final Integer requestTimeout,
		final Integer chunkSize,
		final Boolean autoStart,
		final Boolean adaptivePolling) throws ArangoException {
		return replicationDriver.setReplicationApplierConfig(getDefaultDatabase(), endpoint, database, username,
			password, maxConnectRetries, connectTimeout, requestTimeout, chunkSize, autoStart, adaptivePolling);
	}

	/**
	 * Sets the configuration of the replication applier.
	 *
	 * @param replicationApplierConfigEntity
	 *            an instance of ReplicationApplierConfigEntity containing the
	 *            complete config
	 * @return ReplicationApplierConfigEntity
	 * @throws ArangoException
	 */
	public ReplicationApplierConfigEntity setReplicationApplierConfig(
		final ReplicationApplierConfigEntity replicationApplierConfigEntity) throws ArangoException {
		return replicationDriver.setReplicationApplierConfig(getDefaultDatabase(), replicationApplierConfigEntity);
	}

	/**
	 * Starts the replication applier. This will return immediately if the
	 * replication applier is already running.
	 *
	 * @param from
	 *            The remote lastLogTick value from which to start applying.
	 * @return ReplicationApplierStateEntity
	 * @throws ArangoException
	 */
	public ReplicationApplierStateEntity startReplicationApplier(final Long from) throws ArangoException {
		return replicationDriver.startReplicationApplier(getDefaultDatabase(), from);
	}

	/**
	 * Stops the replication applier. This will return immediately if the
	 * replication applier is not running.
	 *
	 * @return ReplicationApplierStateEntity
	 * @throws ArangoException
	 */
	public ReplicationApplierStateEntity stopReplicationApplier() throws ArangoException {
		return replicationDriver.stopReplicationApplier(getDefaultDatabase());
	}

	/**
	 * Returns the state of the replication applier, regardless of whether the
	 * applier is currently running or not.
	 *
	 * @return ReplicationApplierStateEntity
	 * @throws ArangoException
	 */
	public ReplicationApplierStateEntity getReplicationApplierState() throws ArangoException {
		return replicationDriver.getReplicationApplierState(getDefaultDatabase());
	}

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
	 *            The name of the graph to be created.
	 * @param edgeDefinitions
	 *            List of the graphs edge definitions.
	 * @param orphanCollections
	 *            List of the graphs orphan collections.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return GraphEntity The new graph.
	 * @throws ArangoException
	 */
	public GraphEntity createGraph(
		final String graphName,
		final List<EdgeDefinitionEntity> edgeDefinitions,
		final List<String> orphanCollections,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.createGraph(getDefaultDatabase(), graphName, edgeDefinitions, orphanCollections,
			waitForSync);
	}

	/**
	 * Creates a graph.
	 * 
	 * @param graph
	 *            The graph objet to be persistet.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return GraphEntity The new graph.
	 * @throws ArangoException
	 */
	public GraphEntity createGraph(final GraphEntity graph, final Boolean waitForSync) throws ArangoException {
		final String graphName = graph.getName();
		final List<EdgeDefinitionEntity> edgeDefinitions = graph.getEdgeDefinitions();
		final List<String> orphanCollections = graph.getOrphanCollections();
		return graphDriver.createGraph(getDefaultDatabase(), graphName, edgeDefinitions, orphanCollections,
			waitForSync);
	}

	/**
	 * Creates an empty graph.
	 *
	 * @param graphName
	 *            The name of the graph to be created.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return GraphEntity The new graph.
	 * @throws ArangoException
	 */
	public GraphEntity createGraph(final String graphName, final Boolean waitForSync) throws ArangoException {
		return graphDriver.createGraph(getDefaultDatabase(), graphName, waitForSync);
	}

	/**
	 * Get graph object by name, including its edge definitions and vertex
	 * collections.
	 *
	 * @param graphName
	 *            The name of the graph.
	 * @return GraphEntity The graph.
	 * @throws ArangoException
	 */
	public GraphEntity getGraph(final String graphName) throws ArangoException {
		return graphDriver.getGraph(getDefaultDatabase(), graphName);
	}

	/**
	 * Delete a graph by its name. The collections of the graph will not be
	 * dropped.
	 *
	 * @param graphName
	 *            Name of the graph to be deleted.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity deleteGraph(final String graphName) throws ArangoException {
		return graphDriver.deleteGraph(getDefaultDatabase(), graphName, false);
	}

	/**
	 * Delete a graph by its name. If dropCollections is true, all collections
	 * of the graph will be dropped, if they are not used in another graph.
	 * 
	 * @param graphName
	 *            Name of the graph to be deleted.
	 * @param dropCollections
	 *            Indicates if the collections of the graph will be dropped
	 * @throws ArangoException
	 */
	public void deleteGraph(final String graphName, final Boolean dropCollections) throws ArangoException {
		graphDriver.deleteGraph(getDefaultDatabase(), graphName, dropCollections);
	}

	/**
	 * Returns a list of all vertex collection of a graph that are defined in
	 * the graphs edgeDefinitions (in "from", "to", and "orphanCollections")
	 *
	 * @param graphName
	 *            The graph name.
	 * @return List<String> List of the names of the vertex collections
	 * @throws ArangoException
	 */
	public List<String> graphGetVertexCollections(final String graphName) throws ArangoException {
		return graphGetVertexCollections(graphName, false);
	}

	/**
	 * Returns a list of all vertex collection of a graph that are defined in
	 * the graphs edgeDefinitions (in "from", "to", and "orphanCollections")
	 *
	 * @param graphName
	 *            The graph name.
	 * @param excludeOrphan
	 * @return List<String> List of the names of the vertex collections
	 * @throws ArangoException
	 */
	public List<String> graphGetVertexCollections(final String graphName, final boolean excludeOrphan)
			throws ArangoException {
		return graphDriver.getVertexCollections(getDefaultDatabase(), graphName, excludeOrphan);
	}

	/**
	 * Removes a vertex collection from the graph and optionally deletes the
	 * collection, if it is not used in any other graph.
	 *
	 * @param graphName
	 *            The graph name.
	 * @param collectionName
	 *            The name of the vertex collection to be removed from the
	 *            graph.
	 * @param dropCollection
	 *            Indicates if the collection will be dropped
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteVertexCollection(
		final String graphName,
		final String collectionName,
		final Boolean dropCollection) throws ArangoException {
		return graphDriver.deleteVertexCollection(getDefaultDatabase(), graphName, collectionName, dropCollection);
	}

	/**
	 * Creates a vertex collection
	 *
	 * @param graphName
	 *            The graph name.
	 * @param collectionName
	 *            The name of the collection to be created.
	 * @return GraphEntity The graph, including the new collection.
	 * @throws ArangoException
	 */
	public GraphEntity graphCreateVertexCollection(final String graphName, final String collectionName)
			throws ArangoException {
		return graphDriver.createVertexCollection(getDefaultDatabase(), graphName, collectionName);
	}

	/**
	 * Returns a list of all edge collection of a graph that are defined in the
	 * graphs edgeDefinitions
	 *
	 * @param graphName
	 *            The graph name.
	 * @return List<String> List of the names of all edge collections of the
	 *         graph.
	 * @throws ArangoException
	 */
	public List<String> graphGetEdgeCollections(final String graphName) throws ArangoException {
		return graphDriver.getEdgeCollections(getDefaultDatabase(), graphName);
	}

	/**
	 * Adds a new edge definition to an existing graph
	 *
	 * @param graphName
	 *            The graph name.
	 * @param edgeDefinition
	 *            The edge definition to be added.
	 * @return GraphEntity The graph, including the new edge definition.
	 * @throws ArangoException
	 */
	public GraphEntity graphCreateEdgeDefinition(final String graphName, final EdgeDefinitionEntity edgeDefinition)
			throws ArangoException {
		return graphDriver.createEdgeDefinition(getDefaultDatabase(), graphName, edgeDefinition);
	}

	/**
	 * Replaces an existing edge definition to an existing graph. This will also
	 * change the edge definitions of all other graphs using this definition as
	 * well.
	 *
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the edge collection of the edge definition that
	 *            has to be replaced.
	 * @param edgeDefinition
	 *            The new edge definition.
	 * @return GraphEntity The graph, including the changed edge definition.
	 * @throws ArangoException
	 */
	public GraphEntity graphReplaceEdgeDefinition(
		final String graphName,
		final String edgeCollectionName,
		final EdgeDefinitionEntity edgeDefinition) throws ArangoException {
		return graphDriver.replaceEdgeDefinition(getDefaultDatabase(), graphName, edgeCollectionName, edgeDefinition);
	}

	/**
	 * Removes an existing edge definition from this graph. All data stored in
	 * the collections is dropped as well as long as it is not used in other
	 * graphs.
	 *
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of edge collection of the edge definition which has
	 *            to be deleted.
	 * @return The graph, excluding the deleted edge definition.
	 */
	public GraphEntity graphDeleteEdgeDefinition(
		final String graphName,
		final String edgeCollectionName,
		final Boolean dropCollection) throws ArangoException {
		return graphDriver.deleteEdgeDefinition(getDefaultDatabase(), graphName, edgeCollectionName, dropCollection);
	}

	/**
	 * Stores a new vertex with the information contained within the document
	 * into the given collection.
	 *
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The name of the collection, where the vertex will be created.
	 * @param vertex
	 *            The vertex object to be stored
	 * @param waitForSync
	 *            Wait for sync.
	 * @return <T> DocumentEntity<T> The resulting DocumentEntity containing the
	 *         vertex document.
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphCreateVertex(
		final String graphName,
		final String collectionName,
		final T vertex,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.createVertex(getDefaultDatabase(), graphName, collectionName, vertex, waitForSync);
	}

	/**
	 * Stores a new vertex with the information contained within the document
	 * into the given collection.
	 *
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The name of the collection, where the vertex will be created.
	 * @param key
	 *            The vertex key.
	 * @param vertex
	 *            The vertex object to be stored
	 * @param waitForSync
	 *            Wait for sync.
	 * @return <T> DocumentEntity<T> The resulting DocumentEntity containing the
	 *         vertex document.
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphCreateVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.createVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, waitForSync);
	}

	/**
	 * Gets a vertex with the given key if it is contained within your graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to get.
	 * @param key
	 *            The key (document handle) of the vertex to get.
	 * @param clazz
	 *            The class of the vertex to get.
	 * @return <T> DocumentEntity<T> The resulting DocumentEntity containing the
	 *         vertex document.
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphGetVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final Class<T> clazz) throws ArangoException {
		return graphDriver.getVertex(getDefaultDatabase(), graphName, collectionName, key, clazz, null, null);
	}

	/**
	 * Gets a vertex with the given key if it is contained within your graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to get.
	 * @param key
	 *            The key (document handle) of the vertex to get.
	 * @param clazz
	 *            The class of the vertex to get.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to return a document.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to return a document.
	 * @return <T> DocumentEntity<T> The resulting DocumentEntity containing the
	 *         vertex document.
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphGetVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final Class<T> clazz,
		final Long ifNoneMatchRevision,
		final Long ifMatchRevision) throws ArangoException {
		return graphDriver.getVertex(getDefaultDatabase(), graphName, collectionName, key, clazz, ifMatchRevision,
			ifNoneMatchRevision);
	}

	/**
	 * Deletes a vertex with the given key, if it is contained within the graph.
	 * Furthermore all edges connected to this vertex will be deleted.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to delete.
	 * @param key
	 *            The key (document handle) of the vertex to delete.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteVertex(final String graphName, final String collectionName, final String key)
			throws ArangoException {
		return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, null, null, null);
	}

	/**
	 * Deletes a vertex with the given key, if it is contained within the graph.
	 * Furthermore all edges connected to this vertex will be deleted.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to delete.
	 * @param key
	 *            The key (document handle) of the vertex to delete.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, waitForSync, null, null);
	}

	/**
	 * Deletes a vertex with the given key, if it is contained within the graph.
	 * Furthermore all edges connected to this vertex will be deleted.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to delete.
	 * @param key
	 *            The key (document handle) of the vertex to delete.
	 * @param waitForSync
	 *            Wait for sync.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to return a document.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to return a document.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final Boolean waitForSync,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.deleteVertex(getDefaultDatabase(), graphName, collectionName, key, waitForSync,
			ifMatchRevision, ifNoneMatchRevision);
	}

	/**
	 * Replaces a vertex with the given key by the content in the body. This
	 * will only run successfully if the vertex is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to replace.
	 * @param key
	 *            The key (document handle) of the vertex to replace.
	 * @param vertex
	 *            The object to replace the existing vertex.
	 * @return DocumentEntity<T>
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphReplaceVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex) throws ArangoException {
		return graphDriver.replaceVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, null, null,
			null);
	}

	/**
	 * Replaces a vertex with the given key by the content in the body. This
	 * will only run successfully if the vertex is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to replace.
	 * @param key
	 *            The key (document handle) of the vertex to replace.
	 * @param vertex
	 *            The object to replace the existing vertex.
	 * @param waitForSync
	 *            Wait for sync.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to replace the document.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to replace the document.
	 * @return a VertexEntity object
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphReplaceVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean waitForSync,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.replaceVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, waitForSync,
			ifMatchRevision, ifNoneMatchRevision);
	}

	/**
	 * Updates a vertex with the given key by adding the content in the body.
	 * This will only run successfully if the vertex is contained within the
	 * graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to update.
	 * @param key
	 *            The key (document handle) of the vertex to be updated.
	 * @param vertex
	 *            The object to update the existing vertex.
	 * @param keepNull
	 *            True if the update should keep null values
	 * @return a DocumentEntity object
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphUpdateVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean keepNull) throws ArangoException {
		return graphDriver.updateVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, keepNull, null,
			null, null);
	}

	/**
	 * Updates a vertex with the given key by adding the content in the body.
	 * This will only run successfully if the vertex is contained within the
	 * graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param collectionName
	 *            The collection, containing the vertex to update.
	 * @param key
	 *            The key (document handle) of the vertex to be updated.
	 * @param vertex
	 *            The object to update the existing vertex.
	 * @param keepNull
	 *            True if the update should keep null values
	 * @param waitForSync
	 *            Wait for sync.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to update the document.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to update the document.
	 * @return DocumentEntity<T>
	 * @throws ArangoException
	 */
	public <T> VertexEntity<T> graphUpdateVertex(
		final String graphName,
		final String collectionName,
		final String key,
		final T vertex,
		final Boolean keepNull,
		final Boolean waitForSync,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.updateVertex(getDefaultDatabase(), graphName, collectionName, key, vertex, keepNull,
			waitForSync, ifMatchRevision, ifNoneMatchRevision);
	}

	/**
	 * Stores a new edge with the information contained within the body into the
	 * given collection.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection where the edge will be created.
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from.
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to.
	 * @param value
	 *            Object to be stored with edge.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return <T> EdgeEntity<T>
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphCreateEdge(
		final String graphName,
		final String edgeCollectionName,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.createEdge(getDefaultDatabase(), graphName, edgeCollectionName, null, fromHandle, toHandle,
			value, waitForSync);
	}

	/**
	 * Stores a new edge with the information contained within the body into the
	 * given collection.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection where the edge will be created.
	 * @param key
	 *            The key of the edge to create (has to be unique).
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from.
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to.
	 * @param value
	 *            Object to be stored with edge.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphCreateEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.createEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			value, waitForSync);
	}

	/**
	 * Stores a new edge with no further information into the given collection.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection where the edge will be created.
	 * @param key
	 *            The key of the edge to create (has to be unique).
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from.
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphCreateEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle) throws ArangoException {
		return graphDriver.createEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			null, null);
	}

	/**
	 * Loads an edge with the given key if it is contained within your graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to get.
	 * @param key
	 *            The key of the edge to get.
	 * @param clazz
	 *            The class of the edge to get.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to load the edge.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to load the edge.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphGetEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Class<T> clazz,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.getEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, clazz, ifMatchRevision,
			ifNoneMatchRevision);
	}

	/**
	 * Loads an edge with the given key if it is contained within your graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to get.
	 * @param key
	 *            The key of the edge to get.
	 * @param clazz
	 *            The class of the edge to get.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphGetEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Class<T> clazz) throws ArangoException {
		return graphDriver.getEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, clazz, null, null);
	}

	/**
	 * Deletes an edge with the given id, if it is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to delete.
	 * @param key
	 *            The key of the edge to delete.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteEdge(final String graphName, final String edgeCollectionName, final String key)
			throws ArangoException {
		return graphDriver.deleteEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, null, null, null);
	}

	/**
	 * Deletes an edge with the given id, if it is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to delete.
	 * @param key
	 *            The key of the edge to delete.
	 * @param waitForSync
	 *            Wait for sync.
	 * @return a DeletedEntity object
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Boolean waitForSync) throws ArangoException {
		return graphDriver.deleteEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, waitForSync, null,
			null);
	}

	/**
	 * Deletes an edge with the given id, if it is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to delete.
	 * @param key
	 *            The key of the edge to delete.
	 * @param waitForSync
	 *            Wait for sync.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to delete the edge.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to delete the edge.
	 * @return DeletedEntity
	 * @throws ArangoException
	 */
	public DeletedEntity graphDeleteEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final Boolean waitForSync,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.deleteEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, waitForSync,
			ifMatchRevision, ifNoneMatchRevision);
	}

	/**
	 * Replaces an edge with the given key by the content in the body. This will
	 * only run successfully if the edge is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to replace.
	 * @param key
	 *            The key of the edge to replace.
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from. (can be
	 *            null if value contains "_from" attribute)
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to. (can be
	 *            null if value contains "_to" attribute)
	 * @param value
	 *            The object to replace the existing edge.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphReplaceEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value) throws ArangoException {
		return graphDriver.replaceEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			value, null, null, null);
	}

	/**
	 * Replaces an edge with the given key by the content in the body. This will
	 * only run successfully if the edge is contained within the graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to replace.
	 * @param key
	 *            The key of the edge to replace.
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from. (can be
	 *            null if value contains "_from" attribute)
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to. (can be
	 *            null if value contains "_to" attribute)
	 * @param value
	 *            The object to replace the existing edge. Since ArangoDB 3.X
	 *            the replacement should contain "_from" and "_to".
	 * @param waitForSync
	 *            Wait for sync.
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to replace the edge.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to replace the edge.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphReplaceEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.replaceEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			value, waitForSync, ifMatchRevision, ifNoneMatchRevision);
	}

	/**
	 * Updates an edge with the given key by adding the content in the body.
	 * This will only run successfully if the edge is contained within the
	 * graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to update.
	 * @param key
	 *            The key of the edge to update.
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from. (can be
	 *            null if value contains "_from" attribute)
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to. (can be
	 *            null if value contains "_to" attribute)
	 * @param value
	 *            The object to update the existing edge.
	 * @param keepNull
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphUpdateEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean keepNull) throws ArangoException {
		return graphDriver.updateEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			value, null, keepNull, null, null);
	}

	/**
	 * Updates an edge with the given key by adding the content in the body.
	 * This will only run successfully if the edge is contained within the
	 * graph.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @param edgeCollectionName
	 *            The name of the collection containing edge to update.
	 * @param key
	 *            The key of the edge to update.
	 * @param value
	 *            The object to update the existing edge.
	 * @param fromHandle
	 *            Document handle of vertex, where the edge comes from. (can be
	 *            null if value contains "_from" attribute)
	 * @param toHandle
	 *            Document handle of vertex, where the edge goes to. (can be
	 *            null if value contains "_to" attribute)
	 * @param waitForSync
	 *            Wait for sync.
	 * @param keepNull
	 * @param ifMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be equal to update the edge.
	 * @param ifNoneMatchRevision
	 *            If not null the revision of the vertex in the database has to
	 *            be different to update the edge.
	 * @return a EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> graphUpdateEdge(
		final String graphName,
		final String edgeCollectionName,
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value,
		final Boolean waitForSync,
		final Boolean keepNull,
		final Long ifMatchRevision,
		final Long ifNoneMatchRevision) throws ArangoException {
		return graphDriver.updateEdge(getDefaultDatabase(), graphName, edgeCollectionName, key, fromHandle, toHandle,
			value, waitForSync, keepNull, ifMatchRevision, ifNoneMatchRevision);
	}

	// Some methods not using the graph api

	/**
	 * Returns edges as an EdgeCursor by a given query
	 * 
	 * @param query
	 *            the query
	 * @param bindVars
	 *            the variables
	 * @param aqlQueryOptions
	 *            AQL query options (null for default values)
	 * @param clazz
	 *            the result class
	 * @return EdgeCursor<T>
	 * @throws ArangoException
	 */
	public <T> EdgeCursor<T> executeEdgeQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<T> clazz) throws ArangoException {

		@SuppressWarnings("unchecked")
		final DocumentCursorResult<T, EdgeEntity<T>> baseCursor = cursorDriver.executeBaseCursorQuery(
			getDefaultDatabase(), query, bindVars, getAqlQueryOptions(aqlQueryOptions), EdgeEntity.class, clazz);
		return new EdgeCursor<T>(baseCursor);
	}

	/**
	 * Returns vertices as a VertexCursor by a given query
	 * 
	 * @param query
	 *            the query
	 * @param bindVars
	 *            the variables
	 * @param aqlQueryOptions
	 *            AQL query options (null for default values)
	 * @param clazz
	 *            the result class
	 * @return VertexCursor<T>
	 * @throws ArangoException
	 */
	public <T> VertexCursor<T> executeVertexQuery(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions,
		final Class<T> clazz) throws ArangoException {

		@SuppressWarnings("unchecked")
		final DocumentCursorResult<T, VertexEntity<T>> baseCursor = cursorDriver.executeBaseCursorQuery(
			getDefaultDatabase(), query, bindVars, getAqlQueryOptions(aqlQueryOptions), VertexEntity.class, clazz);
		return new VertexCursor<T>(baseCursor);
	}

	/**
	 * Returns an EdgeCursor by a given vertex example and some options
	 * 
	 * @deprecated use AQL instead
	 * @param graphName
	 *            The name of the graph.
	 * @param clazz
	 * @param vertexExample
	 *            An example for the desired vertices
	 * @param graphEdgesOptions
	 *            An object containing the options
	 * @param aqlQueryOptions
	 *            AQL query options (null for default values (count = true))
	 * @return EdgeCursor<T>
	 * @throws ArangoException
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> EdgeCursor<T> graphGetEdgeCursor(
		final String graphName,
		final Class<T> clazz,
		final Object vertexExample,
		final GraphEdgesOptions graphEdgesOptions,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		GraphEdgesOptions tmpGraphEdgesOptions = graphEdgesOptions;
		if (tmpGraphEdgesOptions == null) {
			tmpGraphEdgesOptions = new GraphEdgesOptions();
		}

		validateCollectionName(graphName);

		final MapBuilder mapBuilder = new MapBuilder();
		final String query = GraphQueryUtil.createEdgeQuery(this, graphName, vertexExample, tmpGraphEdgesOptions,
			mapBuilder);
		final Map<String, Object> bindVars = mapBuilder.get();

		AqlQueryOptions tmpAqlQueryOptions = aqlQueryOptions;
		if (tmpAqlQueryOptions == null) {
			tmpAqlQueryOptions = getDefaultAqlQueryOptions().setCount(true);
		}

		DocumentCursorResult<T, EdgeEntity<T>> cursor = executeAqlQueryWithDocumentCursorResult(query, bindVars,
			tmpAqlQueryOptions, EdgeEntity.class, clazz);

		return new EdgeCursor<T>(cursor);
	}

	/**
	 * Returns a VertexCursor by a given vertex example and some options
	 * 
	 * @deprecated use AQL instead
	 * @param graphName
	 *            The name of the graph.
	 * @param clazz
	 * @param vertexExample
	 *            An example for the desired vertices
	 * @param graphVerticesOptions
	 *            An object containing the options
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @return VertexCursor<T>
	 * @throws ArangoException
	 */
	@Deprecated
	public <T> VertexCursor<T> graphGetVertexCursor(
		final String graphName,
		final Class<T> clazz,
		final Object vertexExample,
		final GraphVerticesOptions graphVerticesOptions,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		validateCollectionName(graphName);

		GraphVerticesOptions tmpGraphVerticesOptions = graphVerticesOptions;
		if (tmpGraphVerticesOptions == null) {
			tmpGraphVerticesOptions = new GraphVerticesOptions();
		}

		final MapBuilder mapBuilder = new MapBuilder();
		final String query = GraphQueryUtil.createVerticesQuery(this, graphName, vertexExample, tmpGraphVerticesOptions,
			mapBuilder);
		final Map<String, Object> bindVars = mapBuilder.get();

		return executeVertexQuery(query, bindVars, aqlQueryOptions, clazz);
	}

	/**
	 * Returns all Edges of a graph, each edge as a PlainEdgeEntity.
	 * 
	 * @param graphName
	 *            The name of the graph.
	 * @return EdgeCursor<PlainEdgeEntity>
	 * @throws ArangoException
	 */
	public EdgeCursor<PlainEdgeEntity> graphGetEdgeCursor(final String graphName) throws ArangoException {
		validateCollectionName(graphName);

		return graphGetEdgeCursor(graphName, PlainEdgeEntity.class, null, new GraphEdgesOptions(), null);
	}

	/**
	 * Returns all Edges of a given vertex.
	 * 
	 * @param graphName
	 * @param clazz
	 * @param vertexExample
	 *            a vertex example or a document handle
	 * @return EdgeCursor<T>
	 * @throws ArangoException
	 */
	public <T> EdgeCursor<T> graphGetEdgeCursorByExample(
		final String graphName,
		final Class<T> clazz,
		final Object vertexExample) throws ArangoException {

		return graphGetEdgeCursor(graphName, clazz, vertexExample, new GraphEdgesOptions(), null);
	}

	/**
	 * @deprecated use AQL instead
	 */
	@Deprecated
	public <V, E> ShortestPathEntity<V, E> graphGetShortestPath(
		final String graphName,
		final Object startVertexExample,
		final Object endVertexExample,
		final ShortestPathOptions shortestPathOptions,
		final Class<V> vertexClass,
		final Class<E> edgeClass) throws ArangoException {

		ShortestPathOptions tmpShortestPathOptions = shortestPathOptions;
		if (tmpShortestPathOptions == null) {
			tmpShortestPathOptions = new ShortestPathOptions();
		}

		return cursorDriver.getShortestPath(getDefaultDatabase(), graphName, startVertexExample, endVertexExample,
			tmpShortestPathOptions, getDefaultAqlQueryOptions(), vertexClass, edgeClass, this);
	}

	/**
	 * Creates an AQL Function
	 *
	 * @param name
	 *            the name of the function as string
	 * @param code
	 *            the function as javascript string
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity createAqlFunction(final String name, final String code) throws ArangoException {
		return aqlFunctionsDriver.createAqlFunction(name, code);
	}

	/**
	 * Gets all AQL functions whithin a given namespace
	 *
	 * @param namespace
	 *            the namespace
	 * @return AqlFunctionsEntity
	 * @throws ArangoException
	 */
	public AqlFunctionsEntity getAqlFunctions(final String namespace) throws ArangoException {
		return aqlFunctionsDriver.getAqlFunctions(namespace);
	}

	/**
	 * Delete an AQL function. If *isNameSpace* is set to true all functions
	 * within the namespace *name* are deleted.
	 *
	 * @param name
	 *            This is either the name of a function or a namespace
	 * @param isNameSpace
	 *            If set to true the param *name* is treated as a namespace
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity deleteAqlFunction(final String name, final boolean isNameSpace) throws ArangoException {
		return aqlFunctionsDriver.deleteAqlFunction(name, isNameSpace);
	}

	/**
	 * Creates a transaction entity.
	 *
	 * @param action
	 *            the transaction as javascript code
	 * @return TransactionEntity
	 */
	public TransactionEntity createTransaction(final String action) {
		return this.transactionDriver.createTransaction(action);
	}

	/**
	 * Executes the transaction on the database server.
	 *
	 * @param transactionEntity
	 *            The configuration object containing all data for the
	 *            transaction
	 * @return TransactionResultEntity
	 * @throws ArangoException
	 */
	public TransactionResultEntity executeTransaction(final TransactionEntity transactionEntity)
			throws ArangoException {
		return this.transactionDriver.executeTransaction(getDefaultDatabase(), transactionEntity);
	}

	/**
	 * Create an edge in an edge collection.
	 *
	 * @param collectionName
	 *            name of the edge collection
	 * @param value
	 *            the edge object
	 * @param fromHandle
	 *            id of document 'from'
	 * @param toHandle
	 *            id of document 'to'
	 * @param waitForSync
	 *            wait for sync
	 * @return the new created EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> createEdge(
		final String collectionName,
		final T value,
		final String fromHandle,
		final String toHandle,
		final Boolean waitForSync) throws ArangoException {

		return createEdge(collectionName, null, value, fromHandle, toHandle, waitForSync);
	}

	/**
	 * Create an edge in an edge collection. This method allows to define to
	 * documents key. Note that the collection's property
	 * CollectionKeyOption.allowUserKeys has to be set accordingly.
	 * 
	 * @param collectionName
	 *            name of the edge collection
	 * @param documentKey
	 *            the desired document key
	 * @param value
	 *            the edge object
	 * @param fromHandle
	 *            id of document 'from'
	 * @param toHandle
	 *            id of document 'to'
	 * @param waitForSync
	 *            wait for sync
	 * @return the new created EdgeEntity object
	 * @throws ArangoException
	 */
	public <T> EdgeEntity<T> createEdge(
		final String collectionName,
		final String documentKey,
		final T value,
		final String fromHandle,
		final String toHandle,
		final Boolean waitForSync) throws ArangoException {

		return documentDriver.createEdge(getDefaultDatabase(), collectionName, documentKey, value, fromHandle, toHandle,
			waitForSync);
	}

	/**
	 * Do a graph traversal.
	 * 
	 * See API documentation of Traversals
	 * 
	 * @param traversalQueryOptions
	 *            the traversal options
	 * @param vertexClazz
	 *            Class of returned vertex documents.
	 * @param edgeClazz
	 *            Class of returned edge documents.
	 * @return a TraversalEntity object
	 * @throws ArangoException
	 */
	public <V, E> TraversalEntity<V, E> getTraversal(
		final TraversalQueryOptions traversalQueryOptions,
		final Class<V> vertexClazz,
		final Class<E> edgeClazz) throws ArangoException {

		return this.traversalDriver.getTraversal(getDefaultDatabase(), traversalQueryOptions, vertexClazz, edgeClazz);
	}

	/**
	 * Clears the AQL query cache (since ArangoDB 2.7)
	 *
	 * @return DefaultEntity
	 * @throws ArangoException
	 */
	public DefaultEntity deleteQueryCache() throws ArangoException {
		return queryCacheDriver.deleteQueryCache();
	}

	/**
	 * Returns the global configuration for the AQL query cache (since ArangoDB
	 * 2.7)
	 *
	 * @return QueryCachePropertiesEntity
	 * @throws ArangoException
	 */
	public QueryCachePropertiesEntity getQueryCacheProperties() throws ArangoException {
		return queryCacheDriver.getQueryCacheProperties();
	}

	/**
	 * Changes the configuration for the AQL query cache (since ArangoDB 2.7)
	 *
	 * @return QueryCachePropertiesEntity
	 * @throws ArangoException
	 */
	public QueryCachePropertiesEntity setQueryCacheProperties(final QueryCachePropertiesEntity properties)
			throws ArangoException {
		return queryCacheDriver.setQueryCacheProperties(properties);
	}

	/**
	 * Returns the configuration for the AQL query tracking
	 * 
	 * @return the configuration
	 * @throws ArangoException
	 */
	public QueryTrackingPropertiesEntity getQueryTrackingProperties() throws ArangoException {
		return this.cursorDriver.getQueryTrackingProperties(getDefaultDatabase());
	}

	/**
	 * Changes the configuration for the AQL query tracking
	 * 
	 * @param properties
	 *            the configuration
	 * @return the configuration
	 * @throws ArangoException
	 */
	public QueryTrackingPropertiesEntity setQueryTrackingProperties(final QueryTrackingPropertiesEntity properties)
			throws ArangoException {
		return this.cursorDriver.setQueryTrackingProperties(getDefaultDatabase(), properties);
	}

	/**
	 * Returns a list of currently running AQL queries of the default database
	 * 
	 * @return a list of currently running AQL queries
	 * @throws ArangoException
	 */
	public QueriesResultEntity getCurrentlyRunningQueries() throws ArangoException {
		return this.cursorDriver.getCurrentlyRunningQueries(getDefaultDatabase());
	}

	/**
	 * Returns a list of currently running AQL queries of a database
	 * 
	 * @param database
	 *            the database name or null
	 * @return a list of currently running AQL queries
	 * @throws ArangoException
	 */
	public QueriesResultEntity getCurrentlyRunningQueries(final String database) throws ArangoException {
		return this.cursorDriver.getCurrentlyRunningQueries(database);
	}

	/**
	 * Returns a list of slow running AQL queries of the default database
	 * 
	 * @return a list of slow running AQL queries
	 * @throws ArangoException
	 */
	public QueriesResultEntity getSlowQueries() throws ArangoException {
		return this.cursorDriver.getSlowQueries(getDefaultDatabase());
	}

	/**
	 * Returns a list of slow running AQL queries of a database
	 * 
	 * @param database
	 *            the database name or null
	 * @return a list of slow running AQL queries
	 * @throws ArangoException
	 */
	public QueriesResultEntity getSlowQueries(final String database) throws ArangoException {
		return this.cursorDriver.getSlowQueries(database);
	}

	/**
	 * Clears the list of slow AQL queries of the default database
	 * 
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity deleteSlowQueries() throws ArangoException {
		return this.cursorDriver.deleteSlowQueries(getDefaultDatabase());
	}

	/**
	 * Clears the list of slow AQL queries of the default database
	 * 
	 * @param database
	 *            the database name or null
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity deleteSlowQueries(final String database) throws ArangoException {
		return this.cursorDriver.deleteSlowQueries(database);
	}

	/**
	 * Kills an AQL query
	 * 
	 * @param id
	 *            the identifier of a query
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity killQuery(final String id) throws ArangoException {
		return this.cursorDriver.killQuery(getDefaultDatabase(), id);
	}

	/**
	 * Kills an AQL query
	 * 
	 * @param id
	 *            the identifier of a query
	 * @param database
	 *            the database name or null
	 * @return a DefaultEntity object
	 * @throws ArangoException
	 */
	public DefaultEntity killQuery(final String database, final String id) throws ArangoException {
		return this.cursorDriver.killQuery(database, id);
	}

	/**
	 * Returns the HTTP manager of the driver
	 * 
	 * @return httpManager
	 */
	public HttpManager getHttpManager() {
		return httpManager;
	}

	/**
	 * Creates a document in the collection defined by the collection's name
	 *
	 * @param collectionName
	 *            The name of the collection
	 * @param rawJsonString
	 *            A string containing a JSON object
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<String>
	 * @throws ArangoException
	 */
	public DocumentEntity<String> createDocumentRaw(
		final String collectionName,
		final String rawJsonString,
		final Boolean waitForSync) throws ArangoException {
		return documentDriver.createDocumentRaw(getDefaultDatabase(), collectionName, rawJsonString, waitForSync);
	}

	/**
	 * Returns the document as a JSON string. Note that the
	 * *ifNoneMatchRevision* and *ifMatchRevision* can not be used at the same
	 * time, one of these two has to be null.
	 *
	 * Throws ArangoException if the requested document is not available.
	 *
	 * @param documentHandle
	 *            The document handle
	 * @param ifNoneMatchRevision
	 *            if set the document is only returned id it has a different
	 *            revision.
	 * @param ifMatchRevision
	 *            if set the document is only returned id it has the same
	 *            revision.
	 * @return a String
	 * @throws ArangoException
	 */
	public String getDocumentRaw(
		final String documentHandle,
		final Long ifNoneMatchRevision,
		final Long ifMatchRevision) throws ArangoException {
		return documentDriver.getDocumentRaw(getDefaultDatabase(), documentHandle, ifNoneMatchRevision,
			ifMatchRevision);
	}

	/**
	 * This method executes an AQL query and returns a CursorRawResult.
	 * 
	 * Use CursorRawResult.iterator() to get the raw JSON strings.
	 *
	 * @param query
	 *            an AQL query as string
	 * @param bindVars
	 *            a map containing all bind variables,
	 * @param aqlQueryOptions
	 *            AQL query options
	 * @return CursorRawResult
	 * @throws ArangoException
	 */
	public CursorRawResult executeAqlQueryRaw(
		final String query,
		final Map<String, Object> bindVars,
		final AqlQueryOptions aqlQueryOptions) throws ArangoException {

		return cursorDriver.executeAqlQueryRaw(getDefaultDatabase(), query, bindVars,
			getAqlQueryOptions(aqlQueryOptions));
	}

	/**
	 * This method replaces the content of the document defined by
	 * documentHandle. This method offers a parameter rev (revision). If the
	 * revision of the document on the server does not match the given revision
	 * an error is thrown.
	 *
	 * @param documentHandle
	 *            The document's handle.
	 * @param rawJsonString
	 *            A string containing a JSON object
	 * @param rev
	 *            the desired revision.
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @return DocumentEntity<String>
	 * @throws ArangoException
	 */
	public DocumentEntity<String> replaceDocumentRaw(
		final String documentHandle,
		final String rawJsonString,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {
		return documentDriver.replaceDocumentRaw(getDefaultDatabase(), documentHandle, rawJsonString, rev, waitForSync);
	}

	/**
	 * This method updates a document defined by documentHandle. This method
	 * offers a parameter rev (revision). If the revision of the document on the
	 * server does not match the given revision an error is thrown.
	 *
	 * @param documentHandle
	 *            The document handle.
	 * @param rawJsonString
	 *            A string containing a JSON object
	 * @param rev
	 *            The desired revision
	 * @param waitForSync
	 *            if set to true the response is returned when the server has
	 *            finished.
	 * @param keepNull
	 *            If true null values are kept.
	 * @return DocumentEntity<String>
	 * @throws ArangoException
	 */
	public DocumentEntity<String> updateDocumentRaw(
		final String documentHandle,
		final String rawJsonString,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {
		return documentDriver.updateDocumentRaw(getDefaultDatabase(), documentHandle, rawJsonString, rev, waitForSync,
			keepNull);
	}

	//
	// private functions
	//

	private void createModuleDrivers(final boolean createProxys) {
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
			this.traversalDriver = ImplFactory.createTraversalDriver(configure, httpManager);
			this.queryCacheDriver = ImplFactory.createQueryCacheDriver(configure, httpManager);
		} else {
			this.transactionDriver = (InternalTransactionDriver) Proxy.newProxyInstance(
				InternalTransactionDriver.class.getClassLoader(), new Class<?>[] { InternalTransactionDriver.class },
				new InvocationHandlerImpl(this.transactionDriver));
			this.jobsDriver = (InternalJobsDriver) Proxy.newProxyInstance(InternalJobsDriver.class.getClassLoader(),
				new Class<?>[] { InternalJobsDriver.class }, new InvocationHandlerImpl(this.jobsDriver));
			this.cursorDriver = (InternalCursorDriver) Proxy.newProxyInstance(
				InternalCursorDriver.class.getClassLoader(), new Class<?>[] { InternalCursorDriver.class },
				new InvocationHandlerImpl(this.cursorDriver));
			this.collectionDriver = (InternalCollectionDriver) Proxy.newProxyInstance(
				InternalCollectionDriver.class.getClassLoader(), new Class<?>[] { InternalCollectionDriver.class },
				new InvocationHandlerImpl(this.collectionDriver));
			this.documentDriver = (InternalDocumentDriver) Proxy.newProxyInstance(
				InternalDocumentDriver.class.getClassLoader(), new Class<?>[] { InternalDocumentDriver.class },
				new InvocationHandlerImpl(this.documentDriver));
			this.indexDriver = (InternalIndexDriver) Proxy.newProxyInstance(InternalIndexDriver.class.getClassLoader(),
				new Class<?>[] { InternalIndexDriver.class }, new InvocationHandlerImpl(this.indexDriver));
			this.adminDriver = (InternalAdminDriver) Proxy.newProxyInstance(InternalAdminDriver.class.getClassLoader(),
				new Class<?>[] { InternalAdminDriver.class }, new InvocationHandlerImpl(this.adminDriver));
			this.aqlFunctionsDriver = (InternalAqlFunctionsDriver) Proxy.newProxyInstance(
				InternalAqlFunctionsDriver.class.getClassLoader(), new Class<?>[] { InternalAqlFunctionsDriver.class },
				new InvocationHandlerImpl(this.aqlFunctionsDriver));
			this.simpleDriver = (InternalSimpleDriver) Proxy.newProxyInstance(
				InternalSimpleDriver.class.getClassLoader(), new Class<?>[] { InternalSimpleDriver.class },
				new InvocationHandlerImpl(this.simpleDriver));
			this.usersDriver = (InternalUsersDriver) Proxy.newProxyInstance(InternalUsersDriver.class.getClassLoader(),
				new Class<?>[] { InternalUsersDriver.class }, new InvocationHandlerImpl(this.usersDriver));
			this.importDriver = (InternalImportDriver) Proxy.newProxyInstance(
				InternalImportDriver.class.getClassLoader(), new Class<?>[] { InternalImportDriver.class },
				new InvocationHandlerImpl(this.importDriver));
			this.databaseDriver = (InternalDatabaseDriver) Proxy.newProxyInstance(
				InternalDatabaseDriver.class.getClassLoader(), new Class<?>[] { InternalDatabaseDriver.class },
				new InvocationHandlerImpl(this.databaseDriver));
			this.endpointDriver = (InternalEndpointDriver) Proxy.newProxyInstance(
				InternalEndpointDriver.class.getClassLoader(), new Class<?>[] { InternalEndpointDriver.class },
				new InvocationHandlerImpl(this.endpointDriver));
			this.replicationDriver = (InternalReplicationDriver) Proxy.newProxyInstance(
				InternalReplicationDriver.class.getClassLoader(), new Class<?>[] { InternalReplicationDriver.class },
				new InvocationHandlerImpl(this.replicationDriver));
			this.graphDriver = (InternalGraphDriver) Proxy.newProxyInstance(InternalGraphDriver.class.getClassLoader(),
				new Class<?>[] { InternalGraphDriver.class }, new InvocationHandlerImpl(this.graphDriver));
			this.traversalDriver = (InternalTraversalDriver) Proxy.newProxyInstance(
				InternalTraversalDriver.class.getClassLoader(), new Class<?>[] { InternalTraversalDriver.class },
				new InvocationHandlerImpl(this.traversalDriver));
			this.queryCacheDriver = (InternalQueryCacheDriver) Proxy.newProxyInstance(
				InternalQueryCacheDriver.class.getClassLoader(), new Class<?>[] { InternalQueryCacheDriver.class },
				new InvocationHandlerImpl(this.queryCacheDriver));
		}
	}

	private AqlQueryOptions getAqlQueryOptions(final AqlQueryOptions aqlQueryOptions) {
		if (aqlQueryOptions == null) {
			return getDefaultAqlQueryOptions();
		}
		return aqlQueryOptions;
	}
}

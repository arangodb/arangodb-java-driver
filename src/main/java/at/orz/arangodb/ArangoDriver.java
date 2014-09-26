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

package at.orz.arangodb;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import at.orz.arangodb.entity.AdminLogEntity;
import at.orz.arangodb.entity.ArangoUnixTime;
import at.orz.arangodb.entity.ArangoVersion;
import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.CollectionKeyOption;
import at.orz.arangodb.entity.CollectionType;
import at.orz.arangodb.entity.CollectionsEntity;
import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DatabaseEntity;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.entity.DeletedEntity;
import at.orz.arangodb.entity.Direction;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.DocumentResultEntity;
import at.orz.arangodb.entity.EdgeEntity;
import at.orz.arangodb.entity.Endpoint;
import at.orz.arangodb.entity.ExplainEntity;
import at.orz.arangodb.entity.FilterCondition;
import at.orz.arangodb.entity.GraphEntity;
import at.orz.arangodb.entity.GraphsEntity;
import at.orz.arangodb.entity.ImportResultEntity;
import at.orz.arangodb.entity.IndexEntity;
import at.orz.arangodb.entity.IndexType;
import at.orz.arangodb.entity.IndexesEntity;
import at.orz.arangodb.entity.Policy;
import at.orz.arangodb.entity.ReplicationApplierConfigEntity;
import at.orz.arangodb.entity.ReplicationApplierStateEntity;
import at.orz.arangodb.entity.ReplicationInventoryEntity;
import at.orz.arangodb.entity.ReplicationLoggerConfigEntity;
import at.orz.arangodb.entity.ReplicationLoggerStateEntity;
import at.orz.arangodb.entity.ReplicationSyncEntity;
import at.orz.arangodb.entity.RestrictType;
import at.orz.arangodb.entity.ScalarExampleEntity;
import at.orz.arangodb.entity.SimpleByResultEntity;
import at.orz.arangodb.entity.StatisticsDescriptionEntity;
import at.orz.arangodb.entity.StatisticsEntity;
import at.orz.arangodb.entity.StringsResultEntity;
import at.orz.arangodb.entity.UserEntity;
import at.orz.arangodb.http.HttpManager;
import at.orz.arangodb.impl.ImplFactory;
import at.orz.arangodb.impl.InternalAdminDriverImpl;
import at.orz.arangodb.impl.InternalCollectionDriverImpl;
import at.orz.arangodb.impl.InternalCursorDriverImpl;
import at.orz.arangodb.impl.InternalDatabaseDriverImpl;
import at.orz.arangodb.impl.InternalDocumentDriverImpl;
import at.orz.arangodb.impl.InternalEndpointDriverImpl;
import at.orz.arangodb.impl.InternalGraphDriverImpl;
import at.orz.arangodb.impl.InternalImportDriverImpl;
import at.orz.arangodb.impl.InternalIndexDriverImpl;
import at.orz.arangodb.impl.InternalReplicationDriverImpl;
import at.orz.arangodb.impl.InternalSimpleDriverImpl;
import at.orz.arangodb.impl.InternalUsersDriverImpl;
import at.orz.arangodb.util.DumpHandler;
import at.orz.arangodb.util.ResultSetUtils;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriver extends BaseArangoDriver {
	
	// TODO Cas Operation as eTAG
	// TODO Should fixed a Double check args.
	// TODO Null check httpResponse.
	
	// TODO コマンド式の実装に変更する。引数が増える度にメソッド数が爆発するのと、そうしないとバッチ処理も上手く書けないため。
	// driver.execute(createDocumentCommand)
	// class createDocumentCommand extends Command {  }
	
	private ArangoConfigure configure;
	private HttpManager httpManager;
	private String baseUrl;
	
	private InternalCursorDriverImpl cursorDriver;
	private InternalCollectionDriverImpl collectionDriver;
	private InternalDocumentDriverImpl documentDriver;
	//private InternalKVSDriverImpl kvsDriver;
	private InternalIndexDriverImpl indexDriver;
	//private InternalEdgeDriverImpl edgeDriver;
	private InternalAdminDriverImpl adminDriver;
	private InternalSimpleDriverImpl simpleDriver;
	private InternalUsersDriverImpl usersDriver;
	private InternalImportDriverImpl importDriver;
	private InternalDatabaseDriverImpl databaseDriver;
	private InternalEndpointDriverImpl endpointDriver;
	private InternalReplicationDriverImpl replicationDriver;
	private InternalGraphDriverImpl graphDriver;
	
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
		this.baseUrl = configure.getBaseUrl();
		
		this.cursorDriver = ImplFactory.createCursorDriver(configure);
		this.collectionDriver = ImplFactory.createCollectionDriver(configure);
		this.documentDriver = ImplFactory.createDocumentDriver(configure);
		//this.kvsDriver = ImplFactory.createKVSDriver(configure);
		this.indexDriver = ImplFactory.createIndexDriver(configure);
		//this.edgeDriver = ImplFactory.createEdgeDriver(configure);
		this.adminDriver = ImplFactory.createAdminDriver(configure);
		this.simpleDriver = ImplFactory.createSimpleDriver(configure, cursorDriver);
		this.usersDriver = ImplFactory.createUsersDriver(configure);
		this.importDriver = ImplFactory.createImportDriver(configure);
		this.databaseDriver = ImplFactory.createDatabaseDriver(configure);

		this.endpointDriver = ImplFactory.createEndpointDriver(configure);
		this.replicationDriver = ImplFactory.createReplicationDriver(configure);
		
		this.graphDriver = ImplFactory.createGraphDriver(configure, cursorDriver);
		
	}
	
	public String getDefaultDatabase() {
		return database;
	}
	
	public void setDefaultDatabase(String database) {
		this.database = database;
	}
	
	// ---------------------------------------- start of collection ----------------------------------------
	
	public CollectionEntity createCollection(String name) throws ArangoException {
		return collectionDriver.createCollection(getDefaultDatabase(), name, null, null, null, null, null, null, null);
	}

	public CollectionEntity createCollection(String name, Boolean waitForSync, Boolean doCompact,
			Integer journalSize, Boolean isSystem, Boolean isVolatile, CollectionType type) throws ArangoException {
		return collectionDriver.createCollection(getDefaultDatabase(), name, waitForSync, doCompact, journalSize, isSystem, isVolatile, type, null);
	}

	public CollectionEntity createCollection(String name, Boolean waitForSync, Boolean doCompact,
			Integer journalSize, Boolean isSystem, Boolean isVolatile, CollectionType type, CollectionKeyOption keyOptions) throws ArangoException {
		return collectionDriver.createCollection(getDefaultDatabase(), name, waitForSync, doCompact, journalSize, isSystem, isVolatile, type, keyOptions);
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
	
	/**
	 * 
	 * @param name
	 * @param withRevisions
	 * @param withData
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public CollectionEntity getCollectionChecksum(String name, Boolean withRevisions, Boolean withData) throws ArangoException {
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
	
	public CollectionEntity setCollectionProperties(long id, Boolean newWaitForSync, Long journalSize) throws ArangoException {
		return collectionDriver.setCollectionProperties(getDefaultDatabase(), String.valueOf(id), newWaitForSync, journalSize);
	}
	public CollectionEntity setCollectionProperties(String name, Boolean newWaitForSync, Long journalSize) throws ArangoException {
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
	
	// ---------------------------------------- end of collection ----------------------------------------

	
	// ---------------------------------------- start of document ----------------------------------------

	public DocumentEntity<?> createDocument(long collectionId, Object value) throws ArangoException {
		return createDocument(String.valueOf(collectionId), value, null, null);
	}
	public <T> DocumentEntity<T> createDocument(String collectionName, Object value) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, null, null);
	}

	public DocumentEntity<?> createDocument(long collectionId, String documentKey, Object value) throws ArangoException {
		return createDocument(String.valueOf(collectionId), documentKey, value, null, null);
	}
	public <T> DocumentEntity<T> createDocument(String collectionName, String documentKey, Object value) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, null, null);
	}

	public DocumentEntity<?> createDocument(long collectionId, Object value, Boolean createCollection, Boolean waitForSync) throws ArangoException {
		return createDocument(String.valueOf(collectionId), value, createCollection, waitForSync);
	}
	public <T> DocumentEntity<T> createDocument(String collectionName, Object value, Boolean createCollection, Boolean waitForSync) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, null, value, createCollection, waitForSync);
	}
	public DocumentEntity<?> createDocument(long collectionId, String documentKey, Object value, Boolean createCollection, Boolean waitForSync) throws ArangoException {
		return createDocument(String.valueOf(collectionId), documentKey, value, createCollection, waitForSync);
	}
	public <T> DocumentEntity<T> createDocument(String collectionName, String documentKey, Object value, Boolean createCollection, Boolean waitForSync) throws ArangoException {
		return documentDriver.createDocument(getDefaultDatabase(), collectionName, documentKey, value, createCollection, waitForSync);
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
	public DocumentEntity<?> replaceDocument(String collectionName, String documentKey, Object value) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null);
	}
	public <T> DocumentEntity<T> replaceDocument(String documentHandle, Object value) throws ArangoException {
		return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, null, null, null);
	}

	
	public DocumentEntity<?> replaceDocument(long collectionId, long documentId, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, rev, policy, waitForSync);
	}
	public DocumentEntity<?> replaceDocument(String collectionName, long documentId, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, rev, policy, waitForSync);
	}
	public DocumentEntity<?> replaceDocument(long collectionId, String documentKey, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionId, documentKey), value, rev, policy, waitForSync);
	}
	public DocumentEntity<?> replaceDocument(String collectionName, String documentKey, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException {
		return replaceDocument(createDocumentHandle(collectionName, documentKey), value, rev, policy, waitForSync);
	}
	public <T> DocumentEntity<T> replaceDocument(String documentHandle, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException {
		return documentDriver.replaceDocument(getDefaultDatabase(), documentHandle, value, rev, policy, waitForSync);
	}


	public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null, null);
	}
	public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null, null, null);
	}
	public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null, null);
	}
	public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null, null);
	}
	public <T> DocumentEntity<T> updateDocument(String documentHandle, Object value) throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, null, null);
	}

	
	public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, null, null, null, keepNull);
	}
	public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, null, null, null, keepNull);
	}
	public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, null, null, null, keepNull);
	}
	public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, null, null, null, keepNull);
	}
	public <T> DocumentEntity<T> updateDocument(String documentHandle, Object value, Boolean keepNull) throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, null, null, null, keepNull);
	}

	
	public DocumentEntity<?> updateDocument(long collectionId, long documentId, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), value, rev, policy, waitForSync, keepNull);
	}
	public DocumentEntity<?> updateDocument(String collectionName, long documentId, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), value, rev, policy, waitForSync, keepNull);
	}
	public DocumentEntity<?> updateDocument(long collectionId, String documentKey, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionId, documentKey), value, rev, policy, waitForSync, keepNull);
	}
	public DocumentEntity<?> updateDocument(String collectionName, String documentKey, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException {
		return updateDocument(createDocumentHandle(collectionName, documentKey), value, rev, policy, waitForSync, keepNull);
	}
	public <T> DocumentEntity<T> updateDocument(String documentHandle, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException {
		return documentDriver.updateDocument(getDefaultDatabase(), documentHandle, value, rev, policy, waitForSync, keepNull);
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
	public <T> DocumentEntity<T> getDocument(String collectionName, long documentId, Class<?> clazz) throws ArangoException {
		return getDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), clazz);
	}
	public <T> DocumentEntity<T> getDocument(long collectionId, String documentKey, Class<?> clazz) throws ArangoException {
		return getDocument(createDocumentHandle(collectionId, documentKey), clazz);
	}
	public <T> DocumentEntity<T> getDocument(String collectionName, String documentKey, Class<?> clazz) throws ArangoException {
		return getDocument(createDocumentHandle(collectionName, documentKey), clazz);
	}
	public <T> DocumentEntity<T> getDocument(String documentHandle, Class<?> clazz) throws ArangoException {
		return documentDriver.getDocument(getDefaultDatabase(), documentHandle, clazz, null, null);
	}

//	public <T> DocumentEntity<T> getDocument(long collectionId, long documentId, Class<?> clazz, Long ifNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
//		return getDocument(createDocumentHandle(collectionId, documentId), clazz, ifNoneMatchRevision, ifMatchRevision);
//	}
//	public <T> DocumentEntity<T> getDocument(String collectionName, long documentId, Class<?> clazz, Long ifNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
//		return getDocument(createDocumentHandle(collectionName, documentId), clazz, ifNoneMatchRevision, ifMatchRevision);
//	}
//	public <T> DocumentEntity<T> getDocument(String documentHandle, Class<?> clazz, Long ifNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
//		return documentDriver.getDocument(getDefaultDatabase(), documentHandle, clazz, ifNoneMatchRevision, ifMatchRevision);
//	}

	
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
	
	public DocumentEntity<?> deleteDocument(long collectionId, long documentId, Long rev, Policy policy) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, String.valueOf(documentId)), rev, policy);
	}
	public DocumentEntity<?> deleteDocument(String collectionName, long documentId, Long rev, Policy policy) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, String.valueOf(documentId)), rev, policy);
	}
	public DocumentEntity<?> deleteDocument(long collectionId, String documentKey, Long rev, Policy policy) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionId, documentKey), rev, policy);
	}
	public DocumentEntity<?> deleteDocument(String collectionName, String documentKey, Long rev, Policy policy) throws ArangoException {
		return deleteDocument(createDocumentHandle(collectionName, documentKey), rev, policy);
	}
	public DocumentEntity<?> deleteDocument(String documentHandle, Long rev, Policy policy) throws ArangoException {
		return documentDriver.deleteDocument(getDefaultDatabase(), documentHandle, rev, policy);
	}

	
	
	// ---------------------------------------- end of document ----------------------------------------
	

	// ---------------------------------------- start of cursor ----------------------------------------

	public CursorEntity<?> validateQuery(String query) throws ArangoException {
		return cursorDriver.validateQuery(getDefaultDatabase(), query);
	}
	
	public ExplainEntity explainQuery(String query, Map<String, Object> bindVars) throws ArangoException {
		return cursorDriver.explainQuery(getDefaultDatabase(), query, bindVars);
	}
	
	public <T> CursorEntity<T> executeQuery(
			String query, Map<String, Object> bindVars,
			Class<T> clazz,
			Boolean calcCount, Integer batchSize) throws ArangoException {
		
		return cursorDriver.executeQuery(getDefaultDatabase(), query, bindVars, clazz, calcCount, batchSize);
		
	}
	
	public <T> CursorEntity<T> continueQuery(long cursorId, Class<?>... clazz) throws ArangoException {
		return cursorDriver.continueQuery(getDefaultDatabase(), cursorId, clazz);
	}
	
	public DefaultEntity finishQuery(long cursorId) throws ArangoException {
		return cursorDriver.finishQuery(getDefaultDatabase(), cursorId);
	}
	
	public <T> CursorResultSet<T> executeQueryWithResultSet(
			String query, Map<String, Object> bindVars,
			Class<T> clazz,
			Boolean calcCount, Integer batchSize) throws ArangoException {
		return cursorDriver.executeQueryWithResultSet(getDefaultDatabase(), query, bindVars, clazz, calcCount, batchSize);
	}
	
	// ---------------------------------------- end of cursor ----------------------------------------

	// ---------------------------------------- start of kvs ----------------------------------------
	
//	public KeyValueEntity createKeyValue(
//			String collectionName, String key, Object value, 
//			Map<String, Object> attributes, Date expiredDate
//			) throws ArangoException {
//		return kvsDriver.createKeyValue(getDefaultDatabase(), collectionName, key, value, attributes, expiredDate);
//	}
//	
//	public KeyValueEntity updateKeyValue(
//			String collectionName, String key, Object value, 
//			Map<String, Object> attributes, Date expiredDate,
//			boolean create
//			) throws ArangoException {
//		return kvsDriver.updateKeyValue(getDefaultDatabase(), collectionName, key, value, attributes, expiredDate, create);
//	}
//	
//	// TODO 全部実装されていないので実装する。ただ、1.1.1の段階ではドキュメントが工事中なんだが。
	
	// ---------------------------------------- end of kvs ----------------------------------------

	
	// ---------------------------------------- start of index ----------------------------------------

	public IndexEntity createIndex(long collectionId, IndexType type, boolean unique, String... fields) throws ArangoException {
		return createIndex(String.valueOf(collectionId), type, unique, fields);
	}
	public IndexEntity createIndex(String collectionName, IndexType type, boolean unique, String... fields) throws ArangoException {
		return indexDriver.createIndex(getDefaultDatabase(), collectionName, type, unique, fields);
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
	public IndexEntity createFulltextIndex(String collectionName, Integer minLength, String... fields) throws ArangoException {
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
	
//	public IndexEntity deleteIndexByFields(long collectionId, String... fields) throws ArangoException {
//	}
//	public IndexEntity deleteIndexByFields(String collectionName, String... fields) throws ArangoException {
//		
//	}
	
	// ---------------------------------------- end of index ----------------------------------------

	// ---------------------------------------- start of edge ----------------------------------------

//	public <T> EdgeEntity<T> createEdge(
//			long collectionId, 
//			String fromHandle, String toHandle, 
//			T attribute) throws ArangoException {
//		return createEdge(String.valueOf(collectionId), fromHandle, toHandle, attribute);
//	}
//	
//	public <T> EdgeEntity<T> createEdge(
//			String collectionName, 
//			String fromHandle, String toHandle, 
//			T attribute) throws ArangoException {
//		
//		return edgeDriver.createEdge(getDefaultDatabase(), collectionName, fromHandle, toHandle, attribute);
//	}
//
//	// TODO UpdateEdge
//	public <T> EdgeEntity<T> updateEdge(
//			String collectionName, 
//			String fromHandle, String toHandle, 
//			T attribute) throws ArangoException {
//		return edgeDriver.updateEdge(getDefaultDatabase(), collectionName, fromHandle, toHandle, attribute);
//	}
//	
//	public long checkEdge(String edgeHandle) throws ArangoException {
//		return edgeDriver.checkEdge(getDefaultDatabase(), edgeHandle);
//	}
//	
//	/**
//	 * エッジハンドルを指定して、エッジの情報を取得する。
//	 * @param edgeHandle
//	 * @param attributeClass
//	 * @return
//	 * @throws ArangoException
//	 */
//	public <T> EdgeEntity<T> getEdge(String edgeHandle, Class<T> attributeClass) throws ArangoException {
//		return edgeDriver.getEdge(getDefaultDatabase(), edgeHandle, attributeClass);
//	}
//
//	public EdgeEntity<?> deleteEdge(long collectionId, String edgeHandle) throws ArangoException {
//		return deleteEdge(String.valueOf(collectionId), edgeHandle);
//	}
//	public EdgeEntity<?> deleteEdge(String collectionName, String edgeHandle) throws ArangoException {
//		return edgeDriver.deleteEdge(getDefaultDatabase(), collectionName, edgeHandle);
//	}
//	
//	public <T> EdgesEntity<T> getEdges(String collectionName, String vertexHandle, Direction direction, Class<T> edgeAttributeClass) throws ArangoException {
//		return edgeDriver.getEdges(getDefaultDatabase(), collectionName, vertexHandle, direction, edgeAttributeClass);
//	}
	
	
	// ---------------------------------------- end of edge ----------------------------------------

	
	// ---------------------------------------- start of admin ----------------------------------------

	public AdminLogEntity getServerLog(
			Integer logLevel, Boolean logLevelUpTo,
			Integer start,
			Integer size, Integer offset,
			Boolean sortAsc,
			String text
			) throws ArangoException {
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
	
	public DefaultEntity flushModules() throws ArangoException {
		return adminDriver.flushModules();
	}

	public DefaultEntity reloadRouting() throws ArangoException {
		return adminDriver.reloadRouting();
	}

	public DefaultEntity executeScript(String jsCode) throws ArangoException {
		return adminDriver.executeScript(getDefaultDatabase(), jsCode);
	}

	// ---------------------------------------- end of admin ----------------------------------------


	// ---------------------------------------- start of simple ----------------------------------------
	
	public <T> CursorEntity<T> executeSimpleByExample(String collectionName, Map<String, Object> example, int skip, int limit, Class<T> clazz) throws ArangoException {
		return simpleDriver.executeSimpleByExample(getDefaultDatabase(), collectionName, example, skip, limit, clazz);
	}

	public <T> CursorResultSet<T> executeSimpleByExampleWithResusltSet(String collectionName, Map<String, Object> example, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleByExampleWithResultSet(getDefaultDatabase(), collectionName, example, skip, limit, clazz);
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(String collectionName, Map<String, Object> example, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleByExampleWithDocument(getDefaultDatabase(), collectionName, example, skip, limit, clazz);
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResusltSet(String collectionName, Map<String, Object> example, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleByExampleWithDocumentResultSet(getDefaultDatabase(), collectionName, example, skip, limit, clazz);
	}
	
	public <T> CursorEntity<T> executeSimpleAll(String collectionName, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAll(getDefaultDatabase(), collectionName, skip, limit, clazz);
	}

	public <T> CursorResultSet<T> executeSimpleAllWithResultSet(String collectionName, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAllWithResultSet(getDefaultDatabase(), collectionName, skip, limit, clazz);
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(String collectionName, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAllWithDocument(getDefaultDatabase(), collectionName, skip, limit, clazz);
	}

	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(String collectionName, int skip, int limit, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAllWithDocumentResultSet(getDefaultDatabase(), collectionName, skip, limit, clazz);
	}

	public <T> ScalarExampleEntity<T> executeSimpleFirstExample(String collectionName, Map<String, Object> example, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleFirstExample(getDefaultDatabase(), collectionName, example, clazz);
	}
	
	public <T> ScalarExampleEntity<T> executeSimpleAny(String collectionName, Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleAny(getDefaultDatabase(), collectionName, clazz);
	}
	
	public <T> CursorEntity<T> executeSimpleRange(
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleRange(getDefaultDatabase(), collectionName, attribute, left, right, closed, skip, limit, clazz);
	}
	
	public <T> CursorResultSet<T> executeSimpleRangeWithResultSet(
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleRangeWithResultSet(getDefaultDatabase(), collectionName, attribute, left, right, closed, skip, limit, clazz);
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleRangeWithDocument(getDefaultDatabase(), collectionName, attribute, left, right, closed, skip, limit, clazz);
	}
	
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
			String collectionName,
			String attribute,
			Object left, Object right, Boolean closed,
			int skip, int limit,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleRangeWithDocumentResultSet(getDefaultDatabase(), collectionName, attribute, left, right, closed, skip, limit, clazz);
	}
	
	public <T> CursorEntity<T> executeSimpleFulltext(
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleFulltext(getDefaultDatabase(), collectionName, attribute, query, skip, limit, index, clazz);
	}
	
	public <T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleFulltextWithResultSet(getDefaultDatabase(), collectionName, attribute, query, skip, limit, index, clazz);
	}

	public <T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleFulltextWithDocument(getDefaultDatabase(), collectionName, attribute, query, skip, limit, index, clazz);
	}
	
	public <T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
			String collectionName,
			String attribute, String query, 
			int skip, int limit,
			String index,
			Class<?> clazz
			) throws ArangoException {
		return simpleDriver.executeSimpleFulltextWithDocumentResultSet(getDefaultDatabase(), collectionName, attribute, query, skip, limit, index, clazz);
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
		return simpleDriver.executeSimpleReplaceByExample(getDefaultDatabase(), collectionName, example, newValue, waitForSync, limit);
	}

	public SimpleByResultEntity executeSimpleUpdateByExample(
			String collectionName,
			Map<String, Object> example,
			Map<String, Object> newValue,
			Boolean keepNull,
			Boolean waitForSync,
			Integer limit) throws ArangoException {
		return simpleDriver.executeSimpleUpdateByExample(getDefaultDatabase(), collectionName, example, newValue, keepNull, waitForSync, limit);
	}

	/**
	 * 
	 * @param collectionName
	 * @param count
	 * @param clazz
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> DocumentResultEntity<T> executeSimpleFirst(
			String collectionName,
			Integer count,
			Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleFirst(getDefaultDatabase(), collectionName, count, clazz);
	}

	/**
	 * 
	 * @param collectionName
	 * @param count
	 * @param clazz
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> DocumentResultEntity<T> executeSimpleLast(
			String collectionName,
			Integer count,
			Class<?> clazz) throws ArangoException {
		return simpleDriver.executeSimpleLast(getDefaultDatabase(), collectionName, count, clazz);
	}

	// ---------------------------------------- end of simple ----------------------------------------

	// ---------------------------------------- start of users ----------------------------------------
	
	public DefaultEntity createUser(String username, String passwd, Boolean active, Map<String, Object> extra) throws ArangoException {
		return usersDriver.createUser(getDefaultDatabase(), username, passwd, active, extra);
	}
	
	public DefaultEntity replaceUser(String username, String passwd, Boolean active, Map<String, Object> extra) throws ArangoException {
		return usersDriver.replaceUser(getDefaultDatabase(), username, passwd, active, extra);
	}
	
	public DefaultEntity updateUser(String username, String passwd, Boolean active, Map<String, Object> extra) throws ArangoException {
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
		
		CursorResultSet<DocumentEntity<UserEntity>> rs = executeSimpleAllWithDocumentResultSet("_users", 0, 0, UserEntity.class);
		return ResultSetUtils.toList(rs);
		
	}
	
	// Original (ArangoDB does not implements this API)
	public List<UserEntity> getUsers() throws ArangoException {
		
		CursorResultSet<UserEntity> rs = executeSimpleAllWithResultSet("_users", 0, 0, UserEntity.class);
		return ResultSetUtils.toList(rs);
		
	}

	// ---------------------------------------- end of users ----------------------------------------

	// ---------------------------------------- start of import ----------------------------------------
	
	public ImportResultEntity importDocuments(String collection, Boolean createCollection, Collection<?> values) throws ArangoException {
		return importDriver.importDocuments(getDefaultDatabase(), collection, createCollection, values);
	}
	
//	public void importDocuments(String collection, Boolean createCollection, Iterator<?> itr) throws ArangoException {
//		importDriver.importDocuments(collection, createCollection, itr);
//	}

	public ImportResultEntity importDocumentsByHeaderValues(String collection, Boolean createCollection, Collection<? extends Collection<?>> headerValues) throws ArangoException {
		return importDriver.importDocumentsByHeaderValues(getDefaultDatabase(), collection, createCollection, headerValues);
	}

	// ---------------------------------------- end of import ----------------------------------------

	// ---------------------------------------- start of database ----------------------------------------
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseCurrent
	 */
	public DatabaseEntity getCurrentDatabase() throws ArangoException {
		return databaseDriver.getCurrentDatabase();
	}
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseList
	 */
	public StringsResultEntity getDatabases() throws ArangoException {
		return getDatabases(false);
	}

	/**
	 * 
	 * @param currentUserAccessableOnly
	 * @return
	 * @throws ArangoException
	 * @since 1.4.1
	 * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseList
	 */
	public StringsResultEntity getDatabases(boolean currentUserAccessableOnly) throws ArangoException {
		return databaseDriver.getDatabases(currentUserAccessableOnly, null, null);
	}

	/**
	 * 
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
	 * 
	 * @param database
	 * @param users
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseCreate
	 */
	public BooleanResultEntity createDatabase(String database, UserEntity...users) throws ArangoException {
		return databaseDriver.createDatabase(database, users);
	}

	/**
	 * 
	 * @param database
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 * @see http://www.arangodb.org/manuals/current/HttpDatabase.html#HttpDatabaseDelete
	 */
	public BooleanResultEntity deleteDatabase(String database) throws ArangoException {
		return databaseDriver.deleteDatabase(database);
	}
	// ---------------------------------------- end of database ----------------------------------------


	// ---------------------------------------- start of endpoint ----------------------------------------

	/**
	 * 
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
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public List<Endpoint> getEndpoints() throws ArangoException {
		return endpointDriver.getEndpoints();
	}
	
	/**
	 * 
	 * @param endpoint
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public BooleanResultEntity deleteEndpoint(String endpoint) throws ArangoException {
		return endpointDriver.deleteEndpoint(endpoint);
	}

	
	// ---------------------------------------- end of endpoint ----------------------------------------


	// ---------------------------------------- start of replication ----------------------------------------

	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationInventoryEntity getReplicationInventory() throws ArangoException {
		return replicationDriver.getReplicationInventory(getDefaultDatabase(), null);
	}
	
	/**
	 * 
	 * @param includeSystem
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationInventoryEntity getReplicationInventory(boolean includeSystem) throws ArangoException {
		return replicationDriver.getReplicationInventory(getDefaultDatabase(), includeSystem);
	}

	/**
	 * 
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
			Long from, Long to, Integer chunkSize, Boolean ticks,
			Class<T> clazz, DumpHandler<T> handler) throws ArangoException {

		replicationDriver.getReplicationDump(getDefaultDatabase(), collectionName, from, to, chunkSize, ticks, clazz, handler);
		
	}

	/**
	 * 
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
			String endpoint, String database, 
			String username, String password, 
			RestrictType restrictType, String... restrictCollections
			) throws ArangoException {
		return replicationDriver.syncReplication(getDefaultDatabase(), endpoint, database, username, password, restrictType, restrictCollections);
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public String getReplicationServerId() throws ArangoException {
		return replicationDriver.getReplicationServerId();
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public boolean startReplicationLogger() throws ArangoException {
		return replicationDriver.startReplicationLogger(getDefaultDatabase());
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public boolean stopReplicationLogger() throws ArangoException {
		return replicationDriver.stopReplicationLogger(getDefaultDatabase());
	}

	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationLoggerStateEntity getReplicationLoggerState() throws ArangoException {
		return replicationDriver.getReplicationLoggerState(getDefaultDatabase());
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationLoggerConfigEntity getReplicationLoggerConfig() throws ArangoException {
		return replicationDriver.getReplicationLoggerConfig(getDefaultDatabase());
	}
	
	/**
	 * 
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
			Long maxEventsSize
			) throws ArangoException {
		return replicationDriver.setReplicationLoggerConfig(getDefaultDatabase(), autoStart, logRemoteChanges, maxEvents, maxEventsSize);
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierConfigEntity getReplicationApplierConfig() throws ArangoException {
		return replicationDriver.getReplicationApplierConfig(getDefaultDatabase());
	}
	
	/**
	 * 
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
			Boolean adaptivePolling
			) throws ArangoException {
		return replicationDriver.setReplicationApplierConfig(getDefaultDatabase(), endpoint, database, username, password, maxConnectRetries, connectTimeout, requestTimeout, chunkSize, autoStart, adaptivePolling);
	}
	

	/**
	 * 
	 * @param param
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierConfigEntity setReplicationApplierConfig(
			ReplicationApplierConfigEntity param
			) throws ArangoException {
		return replicationDriver.setReplicationApplierConfig(getDefaultDatabase(), param);
	}

	/**
	 * 
	 * @param from
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierStateEntity startReplicationApplier(Long from) throws ArangoException {
		return replicationDriver.startReplicationApplier(getDefaultDatabase(), from);
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierStateEntity stopReplicationApplier() throws ArangoException {
		return replicationDriver.stopReplicationApplier(getDefaultDatabase());
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public ReplicationApplierStateEntity getReplicationApplierState() throws ArangoException {
		return replicationDriver.getReplicationApplierState(getDefaultDatabase());
	}
	
	// ---------------------------------------- end of replication ----------------------------------------

	// ---------------------------------------- start of graph ----------------------------------------
	
	/**
	 * 
	 * @param documentKey
	 * @param vertices
	 * @param edges
	 * @param waitForSync
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public GraphEntity createGraph(
			String documentKey, String vertices, String edges,
			Boolean waitForSync) throws ArangoException {
		return graphDriver.createGraph(getDefaultDatabase(), documentKey, vertices, edges, waitForSync);
	}
	
	/**
	 * 
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public GraphsEntity getGraphs() throws ArangoException {
		return graphDriver.getGraphs(getDefaultDatabase());
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public GraphEntity getGraph(String name) throws ArangoException {
		return graphDriver.getGraph(getDefaultDatabase(), name, null, null);
	}

	/**
	 * 
	 * @param name
	 * @param IfNoneMatchRevision
	 * @param ifMatchRevision
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public GraphEntity getGraph(String name, Long IfNoneMatchRevision, Long ifMatchRevision) throws ArangoException {
		return graphDriver.getGraph(getDefaultDatabase(), name, IfNoneMatchRevision, ifMatchRevision);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public DeletedEntity deleteGraph(String name) throws ArangoException {
		return graphDriver.deleteGraph(getDefaultDatabase(), name, null);
	}

	/**
	 * 
	 * @param name
	 * @param ifMatchRevision
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public DeletedEntity deleteGraph(String name, Long ifMatchRevision) throws ArangoException {
		return graphDriver.deleteGraph(getDefaultDatabase(), name, ifMatchRevision);
	}
	
	/**
	 * 
	 * @param graphName
	 * @param vertex
	 * @param waitForSync
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> DocumentEntity<T> createVertex(String graphName, Object vertex, Boolean waitForSync) throws ArangoException {
		return graphDriver.createVertex(getDefaultDatabase(), graphName, vertex, waitForSync);
	}

	/**
	 * 
	 * @param graphName
	 * @param key
	 * @param clazz
	 * @return
	 */
	public <T> DocumentEntity<T> getVertex(
			String graphName, String key, Class<?> clazz
			) throws ArangoException {
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
			String graphName, String key, Class<?> clazz, 
			Long rev, Long IfNoneMatchRevision, Long IfMatchRevision) throws ArangoException {
		return graphDriver.getVertex(getDefaultDatabase(), graphName, key, clazz, rev, IfNoneMatchRevision, IfMatchRevision);
	}

	/**
	 * 
	 * @param graphName
	 * @param key
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public DeletedEntity deleteVertex(
			String graphName, String key
			) throws ArangoException {
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
	public DeletedEntity deleteVertex(
			String graphName, String key,
			Boolean waitForSync
			) throws ArangoException {
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
	public DeletedEntity deleteVertex(
			String graphName, String key,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
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
	public <T> DocumentEntity<T> replaceVertex(
			String graphName, String key, Object vertex
			) throws ArangoException {
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
			String graphName, String key, Object vertex,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
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
	public <T> DocumentEntity<T> updateVertex(
			String graphName, String key, Object vertex, Boolean keepNull
			) throws ArangoException {
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
			String graphName, String key, Object vertex, Boolean keepNull, 
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
		return graphDriver.updateVertex(getDefaultDatabase(), graphName, key, vertex, keepNull, waitForSync, rev, ifMatchRevision);
	}

	/**
	 * 
	 * @param graphName
	 * @param clazz
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> CursorEntity<DocumentEntity<T>> getVertices(
			String graphName, Class<?> clazz
			) throws ArangoException {
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			FilterCondition... properties
			) throws ArangoException {
		return graphDriver.getVertices(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, null, properties);
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
	public <T> CursorEntity<DocumentEntity<T>> getVertices(
			String graphName, String vertexKey, Class<?> clazz
			) throws ArangoException {
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction edgeDirection, Collection<String> edgeLabels, FilterCondition... edgeProperties
			) throws ArangoException {
		return graphDriver.getVertices(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, edgeDirection, edgeLabels, edgeProperties);
	}

	/**
	 * 
	 * @param graphName
	 * @param clazz
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> CursorResultSet<DocumentEntity<T>> getVerticesWithResultSet(
			String graphName, Class<?> clazz
			) throws ArangoException {
		
		return graphDriver.getVerticesWithResultSet(getDefaultDatabase(), graphName, null, clazz, null, null, null, null, null);
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			FilterCondition... properties
			) throws ArangoException {
		
		return graphDriver.getVerticesWithResultSet(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, null, properties);
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
			String graphName, String vertexKey, Class<?> clazz
			) throws ArangoException {
		
		return graphDriver.getVerticesWithResultSet(getDefaultDatabase(), graphName, vertexKey, clazz, null, null, null, null, null);
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		
		return graphDriver.getVerticesWithResultSet(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, direction, labels, properties);
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
			String graphName, String key, String fromHandle, String toHandle, 
			Object value, String label, Boolean waitForSync
			) throws ArangoException {
		return graphDriver.createEdge(getDefaultDatabase(), graphName, key, fromHandle, toHandle, value, label, waitForSync);
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
	public <T> EdgeEntity<T> createEdge(
			String graphName, String key, String fromHandle, String toHandle
			) throws ArangoException {
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
	public <T> EdgeEntity<T> createEdge(
			String graphName, String key, String fromHandle, String toHandle, 
			Object value
			) throws ArangoException {
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
			String graphName, String key, Class<?> clazz,
			Long rev, Long ifNoneMatchRevision, Long ifMatchRevision
			) throws ArangoException {
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
	public <T> EdgeEntity<T> getEdge(
			String graphName, String key, Class<?> clazz
			) throws ArangoException {
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
	public DeletedEntity deleteEdge(
			String graphName, String key
			) throws ArangoException {
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
	public DeletedEntity deleteEdge(
			String graphName, String key,
			Boolean waitForSync
			) throws ArangoException {
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
	public DeletedEntity deleteEdge(
			String graphName, String key,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
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
	public <T> EdgeEntity<T> replaceEdge(
			String graphName, String key, Object value
			) throws ArangoException {
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
			String graphName, String key,
			Object value,
			Boolean waitForSync, Long rev, Long ifMatchRevision
			) throws ArangoException {
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
	public <T> CursorEntity<EdgeEntity<T>> getEdges(
			String graphName, Class<?> clazz
			) throws ArangoException {
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count
			) throws ArangoException {
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		return graphDriver.getEdges(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, labels, properties);
	}
	
	/**
	 * 
	 * @param graphName
	 * @param clazz
	 * @return
	 * @throws ArangoException
	 * @since 1.4.0
	 */
	public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
			String graphName, Class<?> clazz
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, null, clazz, null, null, null, null, null);
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, null);
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
			String graphName, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, null, clazz, batchSize, limit, count, null, labels, properties);
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
	public <T> CursorEntity<EdgeEntity<T>> getEdges(
			String graphName, String vertexKey, Class<?> clazz
			) throws ArangoException {
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count
			) throws ArangoException {
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction direction, Collection<String> labels, FilterCondition... properties
			) throws ArangoException {
		return graphDriver.getEdges(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, direction, labels, properties);
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
	public <T> CursorResultSet<EdgeEntity<T>> getEdgesWithResultSet(
			String graphName, String vertexKey, Class<?> clazz
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, vertexKey, clazz, null, null, null, null, null);
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, null, null);
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
			String graphName, String vertexKey, Class<?> clazz,
			Integer batchSize, Integer limit, Boolean count,
			Direction edgeDirection, Collection<String> edgeLabels, FilterCondition... edgeProperties
			) throws ArangoException {
		return graphDriver.getEdgesWithResultSet(getDefaultDatabase(), graphName, vertexKey, clazz, batchSize, limit, count, edgeDirection, edgeLabels, edgeProperties);
	}

	
	// ---------------------------------------- start of xxx ----------------------------------------

	// ---------------------------------------- end of xxx ----------------------------------------

}

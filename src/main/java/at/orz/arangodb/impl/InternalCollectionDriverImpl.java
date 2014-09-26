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

package at.orz.arangodb.impl;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.CollectionKeyOption;
import at.orz.arangodb.entity.CollectionType;
import at.orz.arangodb.entity.CollectionsEntity;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalCollectionDriverImpl extends BaseArangoDriverImpl {

	InternalCollectionDriverImpl(ArangoConfigure configure) {
		super(configure);
	}

	public CollectionEntity createCollection(
			String database,
			String name, 
			Boolean waitForSync, 
			Boolean doCompact,
			Integer journalSize, 
			Boolean isSystem, 
			Boolean isVolatile,
			CollectionType type,
			CollectionKeyOption keyOptions
			) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPost(
				createEndpointUrl(baseUrl, database, "/_api/collection"),
				null,
				EntityFactory.toJsonString(new MapBuilder()
					.put("name", name)
					.put("waitForSync", waitForSync)
					.put("doCompact", doCompact)
					.put("journalSize", journalSize)
					.put("isSystem", isSystem)
					.put("isVolatile", isVolatile)
					.put("keyOptions", keyOptions)
					.put("type", type == null ? null : type.getType())
					.get())
					);
		
		return createEntity(res, CollectionEntity.class);
		
	}
	
	public CollectionEntity getCollection(String database, String name) throws ArangoException {
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name),
				null);
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
	}
	
	public CollectionEntity getCollectionRevision(String database, String name) throws ArangoException {
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/revision"),
				null);
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
	}
	
	public CollectionEntity getCollectionProperties(String database, String name) throws ArangoException {
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/properties"),
				null);
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
	}
	
	public CollectionEntity getCollectionCount(String database, String name) throws ArangoException {
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/count"),
				null);
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}

	}
	
	public CollectionEntity getCollectionFigures(String database, String name) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/figures"),
				null);

		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}

	}

	public CollectionEntity getCollectionChecksum(String database, String name, Boolean withRevisions, Boolean withData) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/checksum"),
				new MapBuilder()
				.put("withRevisions", withRevisions)
				.put("withData", withData)
				.get());
		
		return createEntity(res, CollectionEntity.class);
		
	}


	public CollectionsEntity getCollections(String database, Boolean excludeSystem) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/collection"),
				null,
				new MapBuilder().put("excludeSystem", excludeSystem).get()
				);
		
		return createEntity(res, CollectionsEntity.class);
		
	}


	public CollectionEntity loadCollection(String database, String name, Boolean count) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/load"), 
				null,
				EntityFactory.toJsonString(
					new MapBuilder("count", count).get()
				));
		
		return createEntity(res, CollectionEntity.class);
		
	}
	
	public CollectionEntity unloadCollection(String database, String name) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/collection/", name, "/unload"),
				null, 
				null);
		
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}
	
	public CollectionEntity truncateCollection(String database, String name) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/truncate"), 
				null, null);
		
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}
	
	public CollectionEntity setCollectionProperties(String database, String name, Boolean newWaitForSync, Long journalSize) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/properties"),
				null,
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("waitForSync", newWaitForSync)
						.put("journalSize", journalSize)
						.get()
				)
		);
		
		return createEntity(res, CollectionEntity.class);
		
	}
	
	public CollectionEntity renameCollection(String database, String name, String newName) throws ArangoException {
		
		validateCollectionName(newName);
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/collection", name, "/rename"), 
				null,
				EntityFactory.toJsonString(
						new MapBuilder("name", newName).get()
				)
		);
		
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}
	
	public CollectionEntity deleteCollection(String database, String name) throws ArangoException {
		
		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doDelete(
				createEndpointUrl(baseUrl, database, "/_api/collection", name),
				null);
		
		try {
			return createEntity(res, CollectionEntity.class);
		} catch (ArangoException e) {
			throw e;
		}
		
	}

	
}

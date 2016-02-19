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
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionOptions;
import com.arangodb.entity.CollectionsEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalCollectionDriverImpl extends BaseArangoDriverImpl
		implements com.arangodb.InternalCollectionDriver {

	InternalCollectionDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public CollectionEntity createCollection(String database, String name, CollectionOptions collectionOptions)
			throws ArangoException {
		if (collectionOptions == null) {
			collectionOptions = new CollectionOptions();
		}
		HttpResponseEntity res = httpManager
				.doPost(createEndpointUrl(database, "/_api/collection"), null, EntityFactory
						.toJsonString(
							new MapBuilder().put("name", name).put("waitForSync", collectionOptions.getWaitForSync())
									.put("doCompact", collectionOptions.getDoCompact())
									.put("journalSize", collectionOptions.getJournalSize())
									.put("isSystem", collectionOptions.getIsSystem())
									.put("isVolatile", collectionOptions.getIsVolatile())
									.put("keyOptions", collectionOptions.getKeyOptions())
									.put("numberOfShards", collectionOptions.getNumberOfShards()).put("shardKeys",
										collectionOptions.getShardKeys())
						.put("type", collectionOptions.getType() == null ? null : collectionOptions.getType().getType())
						.get()));

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionEntity getCollection(String database, String name) throws ArangoException {
		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name), null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionRevision(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name, "/revision"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionProperties(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name, "/properties"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionCount(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name, "/count"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionFigures(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name, "/figures"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionChecksum(String database, String name, Boolean withRevisions, Boolean withData)
			throws ArangoException {

		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection", name, "/checksum"),
			new MapBuilder().put("withRevisions", withRevisions).put("withData", withData).get());

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionsEntity getCollections(String database, Boolean excludeSystem) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/collection"), null,
			new MapBuilder().put("excludeSystem", excludeSystem).get());

		return createEntity(res, CollectionsEntity.class);

	}

	@Override
	public CollectionEntity loadCollection(String database, String name, Boolean count) throws ArangoException {

		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/collection", name, "/load"), null,
			EntityFactory.toJsonString(new MapBuilder("count", count).get()));

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionEntity unloadCollection(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/collection/", name, "/unload"),
			null, null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity truncateCollection(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/collection", name, "/truncate"),
			null, null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity setCollectionProperties(
		String database,
		String name,
		Boolean newWaitForSync,
		Long journalSize) throws ArangoException {

		validateCollectionName(name);
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/collection", name, "/properties"),
			null, EntityFactory.toJsonString(
				new MapBuilder().put("waitForSync", newWaitForSync).put("journalSize", journalSize).get()));

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity renameCollection(String database, String name, String newName) throws ArangoException {

		validateCollectionName(newName);

		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/collection", name, "/rename"),
			null, EntityFactory.toJsonString(new MapBuilder("name", newName).get()));

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity deleteCollection(String database, String name) throws ArangoException {

		validateCollectionName(name);

		HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/collection", name), null);

		return createEntity(res, CollectionEntity.class);
	}

}

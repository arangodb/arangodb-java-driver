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

	private static final String API_COLLECTION = "/_api/collection";

	InternalCollectionDriverImpl(final ArangoConfigure configure, final HttpManager httpManager) {
		super(configure, httpManager);
	}

	@Override
	public CollectionEntity createCollection(
		final String database,
		final String name,
		final CollectionOptions collectionOptions) throws ArangoException {
		CollectionOptions tmpCollectionOptions = collectionOptions;
		if (tmpCollectionOptions == null) {
			tmpCollectionOptions = new CollectionOptions();
		}
		final HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, API_COLLECTION), null,
			EntityFactory.toJsonString(
				new MapBuilder().put("name", name).put("waitForSync", tmpCollectionOptions.getWaitForSync())
						.put("doCompact", tmpCollectionOptions.getDoCompact())
						.put("journalSize", tmpCollectionOptions.getJournalSize())
						.put("isSystem", tmpCollectionOptions.getIsSystem())
						.put("isVolatile", tmpCollectionOptions.getIsVolatile())
						.put("keyOptions", tmpCollectionOptions.getKeyOptions())
						.put("numberOfShards", tmpCollectionOptions.getNumberOfShards())
						.put("replicationFactor", tmpCollectionOptions.getReplicationFactor())
						.put("shardKeys", tmpCollectionOptions.getShardKeys())
						.put("type", tmpCollectionOptions.getType() == null ? null
								: tmpCollectionOptions.getType().getType())
						.get()));

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionEntity getCollection(final String database, final String name) throws ArangoException {
		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION, name), null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionRevision(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION, name, "/revision"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionProperties(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager
				.doGet(createEndpointUrl(database, API_COLLECTION, name, "/properties"), null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionCount(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION, name, "/count"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionFigures(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION, name, "/figures"),
			null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity getCollectionChecksum(
		final String database,
		final String name,
		final Boolean withRevisions,
		final Boolean withData) throws ArangoException {

		validateCollectionName(name);
		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION, name, "/checksum"),
			new MapBuilder().put("withRevisions", withRevisions).put("withData", withData).get());

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionsEntity getCollections(final String database, final Boolean excludeSystem) throws ArangoException {

		final HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, API_COLLECTION), null,
			new MapBuilder().put("excludeSystem", excludeSystem).get());

		return createEntity(res, CollectionsEntity.class);

	}

	@Override
	public CollectionEntity loadCollection(final String database, final String name, final Boolean count)
			throws ArangoException {

		validateCollectionName(name);
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, API_COLLECTION, name, "/load"),
			null, EntityFactory.toJsonString(new MapBuilder("count", count).get()));

		return createEntity(res, CollectionEntity.class);

	}

	@Override
	public CollectionEntity unloadCollection(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager
				.doPut(createEndpointUrl(database, "/_api/collection/", name, "/unload"), null, null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity truncateCollection(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, API_COLLECTION, name, "/truncate"),
			null, null);

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity setCollectionProperties(
		final String database,
		final String name,
		final Boolean newWaitForSync,
		final Long journalSize) throws ArangoException {

		validateCollectionName(name);
		final HttpResponseEntity res = httpManager.doPut(
			createEndpointUrl(database, API_COLLECTION, name, "/properties"), null, EntityFactory.toJsonString(
				new MapBuilder().put("waitForSync", newWaitForSync).put("journalSize", journalSize).get()));

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity renameCollection(final String database, final String name, final String newName)
			throws ArangoException {

		validateCollectionName(newName);

		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, API_COLLECTION, name, "/rename"),
			null, EntityFactory.toJsonString(new MapBuilder("name", newName).get()));

		return createEntity(res, CollectionEntity.class);
	}

	@Override
	public CollectionEntity deleteCollection(final String database, final String name) throws ArangoException {

		validateCollectionName(name);

		final HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, API_COLLECTION, name), null);

		return createEntity(res, CollectionEntity.class);
	}

}

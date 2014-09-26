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

package at.orz.arangodb.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import at.orz.arangodb.ArangoConfigure;
import at.orz.arangodb.ArangoException;
import at.orz.arangodb.entity.EntityFactory;
import at.orz.arangodb.entity.MapAsEntity;
import at.orz.arangodb.entity.ReplicationApplierConfigEntity;
import at.orz.arangodb.entity.ReplicationApplierStateEntity;
import at.orz.arangodb.entity.ReplicationDumpHeader;
import at.orz.arangodb.entity.ReplicationDumpRecord;
import at.orz.arangodb.entity.ReplicationInventoryEntity;
import at.orz.arangodb.entity.ReplicationLoggerConfigEntity;
import at.orz.arangodb.entity.ReplicationLoggerStateEntity;
import at.orz.arangodb.entity.ReplicationSyncEntity;
import at.orz.arangodb.entity.RestrictType;
import at.orz.arangodb.entity.StreamEntity;
import at.orz.arangodb.http.HttpResponseEntity;
import at.orz.arangodb.util.DumpHandler;
import at.orz.arangodb.util.IOUtils;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalReplicationDriverImpl extends BaseArangoDriverImpl {

	InternalReplicationDriverImpl(ArangoConfigure configure) {
		super(configure);
	}
	
	public ReplicationInventoryEntity getReplicationInventory(String database, Boolean includeSystem) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/inventory"), 
				new MapBuilder().put("includeSystem", includeSystem).get());
		
		return createEntity(res, ReplicationInventoryEntity.class);
		
	}
	
	public <T> void getReplicationDump(
			String database, 
			String collectionName,
			Long from, Long to, Integer chunkSize, Boolean ticks,
			Class<T> clazz, DumpHandler<T> handler) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/dump"),
				new MapBuilder()
				.put("collection", collectionName)
				.put("from", from)
				.put("to", to)
				.put("chunkSize", chunkSize)
				.put("ticks", ticks)
				.get()
				);

		ReplicationDumpHeader header = toReplicationDumpHeader(res);
		boolean cont = handler.head(header);
		
		StreamEntity entity = createEntity(res, StreamEntity.class);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(entity.getStream(), "utf-8"));
			String line = null;
			while (cont && (line = reader.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				cont = handler.handle(createEntity(line, ReplicationDumpRecord.class, clazz));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // not arnago-exception: because encoding error is cause of system.
		} catch (IOException e) {
			throw new ArangoException(e);
		} finally {
			IOUtils.close(reader);
		}
		
	}

	public ReplicationSyncEntity syncReplication(
			String _database,
			String endpoint, String database, 
			String username, String password, 
			RestrictType restrictType, String... restrictCollections
			) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, _database, "/_api/replication/sync"), 
				null, 
				EntityFactory.toJsonString(
					new MapBuilder()
					.put("endpoint", endpoint)
					.put("database", database)
					.put("username", username)
					.put("password", password)
					.put("restrictType", restrictType == null ? null : restrictType.name().toLowerCase(Locale.US))
					.put("restrictCollections", restrictCollections == null || restrictCollections.length == 0 ? null : restrictCollections)
					.get()
				)
				);
		
		return createEntity(res, ReplicationSyncEntity.class);
		
	}
	
	public String getReplicationServerId() throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, null, "/_api/replication/server-id")); // MEMO: not use database, because same value each database.
		
		MapAsEntity entity = createEntity(res, MapAsEntity.class);
		return (String) entity.getMap().get("serverId");
		
	}
	
	public boolean startReplicationLogger(String database) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/logger-start"), 
				null, null);

		MapAsEntity entity = createEntity(res, MapAsEntity.class);
		return (Boolean) entity.getMap().get("running");

	}

	public boolean stopReplicationLogger(String database) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/logger-stop"), 
				null, null);

		MapAsEntity entity = createEntity(res, MapAsEntity.class);
		return (Boolean) entity.getMap().get("running");

	}

	public ReplicationLoggerConfigEntity getReplicationLoggerConfig(String database) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/logger-config"));
		
		return createEntity(res, ReplicationLoggerConfigEntity.class);
		
	}

	public ReplicationLoggerConfigEntity setReplicationLoggerConfig(
			String database,
			Boolean autoStart,
			Boolean logRemoteChanges,
			Long maxEvents,
			Long maxEventsSize
			) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/logger-config"),
				null, 
				EntityFactory.toJsonString(
						new MapBuilder()
						.put("autoStart", autoStart)
						.put("logRemoteChanges", logRemoteChanges)
						.put("maxEvents", maxEvents)
						.put("maxEventsSize", maxEventsSize)
						.get()));
		
		return createEntity(res, ReplicationLoggerConfigEntity.class);
		
	}
	
	public ReplicationLoggerStateEntity getReplicationLoggerState(String database) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/logger-state"));
		
		return createEntity(res, ReplicationLoggerStateEntity.class);

	}

	public ReplicationApplierConfigEntity getReplicationApplierConfig(String database) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/applier-config"));
		
		return createEntity(res, ReplicationApplierConfigEntity.class);

	}
	
	public ReplicationApplierConfigEntity setReplicationApplierConfig(
			String _database,
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

		ReplicationApplierConfigEntity bodyParam = new ReplicationApplierConfigEntity();
		bodyParam.setEndpoint(endpoint);
		bodyParam.setDatabase(database);
		bodyParam.setUsername(username);
		bodyParam.setPassword(password);
		bodyParam.setMaxConnectRetries(maxConnectRetries);
		bodyParam.setConnectTimeout(connectTimeout);
		bodyParam.setRequestTimeout(requestTimeout);
		bodyParam.setChunkSize(chunkSize);
		bodyParam.setAutoStart(autoStart);
		bodyParam.setAdaptivePolling(adaptivePolling);
		
		return setReplicationApplierConfig(_database, bodyParam);

	}

	public ReplicationApplierConfigEntity setReplicationApplierConfig(
			String database,
			ReplicationApplierConfigEntity param
			) throws ArangoException {

		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/applier-config"),
				null, 
				EntityFactory.toJsonString(param)
				);
		
		return createEntity(res, ReplicationApplierConfigEntity.class);

	}
	
	public ReplicationApplierStateEntity startReplicationApplier(String database, Long from) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/applier-start"),
				new MapBuilder().put("from", from).get(), 
				null
				);
		
		return createEntity(res, ReplicationApplierStateEntity.class);
		
	}

	public ReplicationApplierStateEntity stopReplicationApplier(String database) throws ArangoException {
		
		HttpResponseEntity res = httpManager.doPut(
				createEndpointUrl(baseUrl, database, "/_api/replication/applier-stop"),
				null,
				null
				);
		
		return createEntity(res, ReplicationApplierStateEntity.class);
		
	}

	public ReplicationApplierStateEntity getReplicationApplierState(String database) throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(
				createEndpointUrl(baseUrl, database, "/_api/replication/applier-state"));
		
		return createEntity(res, ReplicationApplierStateEntity.class);

	}

}

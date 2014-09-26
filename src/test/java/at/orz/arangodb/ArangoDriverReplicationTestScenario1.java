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

package at.orz.arangodb;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import at.orz.arangodb.entity.BooleanResultEntity;
import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.ImportResultEntity;
import at.orz.arangodb.entity.ReplicationApplierStateEntity;
import at.orz.arangodb.entity.ReplicationLoggerStateEntity;
import at.orz.arangodb.entity.ReplicationSyncEntity;
import at.orz.arangodb.util.MapBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverReplicationTestScenario1 {

	ArangoConfigure masterConfigure;
	ArangoConfigure slaveConfigure;
	ArangoDriver masterDriver;
	ArangoDriver slaveDriver;
	
	String database = "repl_scenario_test1";
	String collectionName1 = "col1";
	String collectionName2 = "col2";
	
	@Before
	public void before() throws ArangoException {

		masterConfigure = new ArangoConfigure("/arangodb.properties");
		masterConfigure.init();
		masterDriver = new ArangoDriver(masterConfigure);

		slaveConfigure = new ArangoConfigure("/arangodb-slave.properties");
		slaveConfigure.init();
		slaveDriver = new ArangoDriver(slaveConfigure);
		
		// turn off replication logger at master
		masterDriver.stopReplicationLogger();
		masterDriver.stopReplicationApplier();
		
		// turn off replication applier at slave
		slaveDriver.stopReplicationLogger();
		slaveDriver.stopReplicationApplier();
		
	}
	
	@After
	public void after() throws ArangoException {
		masterConfigure.shutdown();
		slaveConfigure.shutdown();
	}
	
	@Test
	public void test_scienario() throws ArangoException, InterruptedException {
		
		// replication: master[db=repl_scenario_test1] -> slave[db=_system]
		
		System.out.println("----------------------------------------");
		
		// create database in master
		try {
			masterDriver.deleteDatabase(database);
		} catch (ArangoException e) {}
		{
			BooleanResultEntity result = masterDriver.createDatabase(database);
			assertThat(result.getResult(), is(true));
		} 
		// configure database
		masterDriver.setDefaultDatabase(database);
		slaveDriver.setDefaultDatabase(null);
		
		try {
			slaveDriver.deleteCollection(collectionName1);
		} catch (ArangoException e) {}
		
		// [Master] add document
		CollectionEntity col1 = masterDriver.createCollection(collectionName1);
		DocumentEntity<Map<String, Object>> doc1 = masterDriver.createDocument(collectionName1, new MapBuilder().put("my-key1", "100").get(), false, false);
		DocumentEntity<Map<String, Object>> doc2 = masterDriver.createDocument(collectionName1, new MapBuilder().put("my-key2", "255").get(), false, false);
		DocumentEntity<Map<String, Object>> doc3 = masterDriver.createDocument(collectionName1, new MapBuilder().put("my-key3", 1234567).get(), false, false);
		
		// [Master] logger property
		masterDriver.setReplicationLoggerConfig(true, null, 1048576L, 0L);
		
		// [Master] turn on replication logger
		masterDriver.startReplicationLogger();
		
		// [Slave] turn off replication applier
		slaveDriver.stopReplicationApplier();

		// [Master] get logger state
		ReplicationLoggerStateEntity state1 = masterDriver.getReplicationLoggerState();
		assertThat(state1.getClients().size(), is(0));

		// [Slave] sync
		ReplicationSyncEntity syncResult = slaveDriver.syncReplication(
				masterConfigure.getEndpoint(), database, "root", null, null);
		System.out.println(syncResult.getLastLogTick());
		
		Thread.sleep(3000L);

		slaveDriver.setReplicationApplierConfig(
				masterConfigure.getEndpoint(), database, "root", null, 
				null, null, null, null, true, true);
		
		// [Slave] turn on replication applier
		slaveDriver.startReplicationApplier(syncResult.getLastLogTick());
		
		// [Master] create 10 document
		for (int i = 0; i < 10; i++) {
			masterDriver.createDocument(collectionName1, new MapBuilder().put("my-key" + i, 1234567).get(), false, false);
		}
		
		// [Master] import 290 document
		LinkedList<Map<String, Object>> values = new LinkedList<Map<String,Object>>();
		for (int i = 10; i < 300; i++) {
			values.add(new MapBuilder().put("my-key" + i, 1234567).get());
		}
		ImportResultEntity importResult = masterDriver.importDocuments(collectionName1, false, values);
		assertThat(importResult.getCreated(), is(290));
		
		// wait
		TimeUnit.SECONDS.sleep(2);
		
		// [Slave] check a replication data
		CollectionEntity entity1 = slaveDriver.getCollectionCount(collectionName1);
		assertThat(entity1.getCount(), is(303L));
		
		
		// ------------------------------------------------------------
		// Delete
		// ------------------------------------------------------------
		
		// [Master] delete document
		DocumentEntity<?> delEntity = masterDriver.deleteDocument(doc1.getDocumentHandle(), null, null);
		assertThat(delEntity.isError(), is(false));
		assertThat(delEntity.getDocumentKey(), is(doc1.getDocumentKey()));
		
		// wait
		TimeUnit.SECONDS.sleep(2);
		
		// [Slave] check a replication data
		CollectionEntity entity2 = slaveDriver.getCollectionCount(collectionName1);
		assertThat(entity2.getCount(), is(302L));
		
		try {
			slaveDriver.getDocument(doc1.getDocumentHandle(), Map.class);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
		}

		
		// ------------------------------------------------------------
		// Replace
		// ------------------------------------------------------------
		// [Master] replace document
		masterDriver.replaceDocument(doc2.getDocumentHandle(), 
				new MapBuilder().put("updatedKey", "あいうえお").get(), null, null, null);
		
		// wait
		TimeUnit.SECONDS.sleep(2);
		
		// [Slave] check a replication data
		CollectionEntity entity3 = slaveDriver.getCollectionCount(collectionName1);
		assertThat(entity3.getCount(), is(302L));
		
		DocumentEntity<Map<String, Object>> doc2a = slaveDriver.getDocument(doc2.getDocumentHandle(), Map.class);
		assertThat(doc2a.getDocumentHandle(), is(doc2a.getDocumentHandle()));
		assertThat(doc2a.getEntity().size(), is(4)); // _id, _rev, _key
		assertThat((String) doc2a.getEntity().get("updatedKey"), is("あいうえお"));

		
		// ------------------------------------------------------------
		// Partial Update
		// ------------------------------------------------------------
		// [Master] update document
		masterDriver.updateDocument(doc2.getDocumentHandle(), 
				new MapBuilder().put("updatedKey2", "ABCDE").get(), null, null, null, null);

		// wait
		TimeUnit.SECONDS.sleep(2);
		
		// [Slave] check a replication data
		CollectionEntity entity4 = slaveDriver.getCollectionCount(collectionName1);
		assertThat(entity4.getCount(), is(302L));
		
		DocumentEntity<Map<String, Object>> doc2b = slaveDriver.getDocument(doc2.getDocumentHandle(), Map.class);
		assertThat(doc2b.getDocumentHandle(), is(doc2a.getDocumentHandle()));
		assertThat(doc2b.getEntity().size(), is(5)); // _id, _rev, _key
		assertThat((String) doc2b.getEntity().get("updatedKey"), is("あいうえお"));
		assertThat((String) doc2b.getEntity().get("updatedKey2"), is("ABCDE"));

		
		// ------------------------------------------------------------
		// Delete Collection
		// ------------------------------------------------------------
		// [Master] delete collection
		masterDriver.deleteCollection(collectionName1);

		// wait
		TimeUnit.SECONDS.sleep(2);
		
		// [Slave] check a replication data
		try {
			masterDriver.getCollection(collectionName1);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(404));
		}

		// ------------------------------------------------------------
		// State
		// ------------------------------------------------------------

		// [Master] logger state
		ReplicationLoggerStateEntity state2 = masterDriver.getReplicationLoggerState();
		assertThat(state2.getState().isRunning(), is(true));
		assertThat(state2.getState().getLastLogTick(), is(not(0L)));
		assertThat(state2.getState().getTotalEvents(), is(307L));
		assertThat(state2.getState().getTime(), is(notNullValue()));

		assertThat(state2.getServerVersion(), is(notNullValue()));
		assertThat(state2.getServerId(), is(masterDriver.getReplicationServerId()));
		
		assertThat(state2.getClients().size(), is(1));
		assertThat(state2.getClients().get(0).getServerId(), is(slaveDriver.getReplicationServerId()));
		assertThat(state2.getClients().get(0).getLastServedTick(), is(0L));
		assertThat(state2.getClients().get(0).getTime(), is(notNullValue()));
		
		System.out.println(state2.getClients().get(0).getTime());
		
		// [Slave] applier state
		ReplicationApplierStateEntity state3 = slaveDriver.getReplicationApplierState();
		assertThat(state3.getStatusCode(), is(200));
		assertThat(state3.getServerVersion(), is(notNullValue()));
		assertThat(state3.getServerId(), is(slaveDriver.getReplicationServerId()));
		assertThat(state3.getEndpoint(), is(masterConfigure.getEndpoint()));
		assertThat(state3.getDatabase(), is(database));
		assertThat(state3.getState().getRunning(), is(true));
		assertThat(state3.getState().getLastAppliedContinuousTick(), is(notNullValue()));
		assertThat(state3.getState().getLastProcessedContinuousTick(), is(notNullValue()));
		assertThat(state3.getState().getLastAvailableContinuousTick(), is(notNullValue()));
		assertThat(state3.getState().getProgress().getTime(), is(notNullValue()));
		assertThat(state3.getState().getProgress().getMessage(), startsWith("fetching master log from offset"));
		assertThat(state3.getState().getProgress().getFailedConnects(), is(0L));
		assertThat(state3.getState().getTotalRequests().longValue(), is(not(0L)));
		assertThat(state3.getState().getTotalFailedConnects().longValue(), is(0L));
		assertThat(state3.getState().getTotalEvents(), is(306L));
		assertThat(state3.getState().getLastError().getErrorNum(), is(0));
		assertThat(state3.getState().getLastError().getErrorMessage(), is(nullValue()));
		assertThat(state3.getState().getLastError().getTime(), is(nullValue()));
		assertThat(state3.getState().getTime(), is(notNullValue()));
		
		
	}
	
}

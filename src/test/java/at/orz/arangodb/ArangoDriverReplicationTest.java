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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import at.orz.arangodb.entity.ReplicationApplierConfigEntity;
import at.orz.arangodb.entity.ReplicationApplierStateEntity;
import at.orz.arangodb.entity.ReplicationDumpHeader;
import at.orz.arangodb.entity.ReplicationDumpRecord;
import at.orz.arangodb.entity.ReplicationInventoryEntity;
import at.orz.arangodb.entity.ReplicationLoggerStateEntity;
import at.orz.arangodb.entity.ReplicationInventoryEntity.Collection;
import at.orz.arangodb.entity.ReplicationLoggerConfigEntity;
import at.orz.arangodb.util.DumpHandler;
import at.orz.arangodb.util.DumpHandlerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class ArangoDriverReplicationTest extends BaseTest {

	public ArangoDriverReplicationTest(ArangoConfigure configure, ArangoDriver driver) {
		super(configure, driver);
	}
	
	
	@Before
	public void before() {
		driver.setDefaultDatabase(null);
	}

	@Test
	public void test_get_inventory() throws ArangoException {
		
		try {
			driver.stopReplicationLogger();
		} catch (Exception e) {}
		
		ReplicationInventoryEntity entity = driver.getReplicationInventory();
		assertThat(entity.getCode(), is(0));
		assertThat(entity.getStatusCode(), is(200));
		
		assertThat(entity.getTick(), is(not(0L)));
		assertThat(entity.getState().isRunning(), is(false));
		assertThat(entity.getCollections().size(), is(not(0)));
		
	}

	@Test
	public void test_get_inventory_includeSystem() throws ArangoException {
		
		ReplicationInventoryEntity entity = driver.getReplicationInventory(true);
		assertThat(entity.getCode(), is(0));
		assertThat(entity.getStatusCode(), is(200));

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		for (Collection col: entity.getCollections()) {
			System.out.println(gson.toJson(col.getParameter()));
			System.out.println(gson.toJson(col.getIndexes()));
		}

	}

	@Test
	public void test_get_inventory_404() throws ArangoException {
		
		driver.setDefaultDatabase("database-404");
		try {
			driver.getReplicationInventory();
			fail();
		} catch(ArangoException e) {
			assertThat(e.getCode(), is(404));
			assertThat(e.getErrorNumber(), is(1228)); // database not found
		}
		
	}
	
	@Test
	public void test_get_dump() throws ArangoException {
		
		String collectionName = "rep_dump_test";
		
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}

		// create 10 document
		for (int i = 0; i < 10; i++) {
			TestComplexEntity01 entity = new TestComplexEntity01("user-" + i, "desc-" + i, 20+i);
			driver.createDocument(collectionName, entity, true, null);
		}
		// truncate
		try {
			driver.truncateCollection(collectionName);
		} catch (ArangoException e) {}
		// create 1 document
		TestComplexEntity01 entity = new TestComplexEntity01("user-99", "desc-99", 99);
		driver.createDocument(collectionName, entity, true, null);
		
		final AtomicInteger upsertCount = new AtomicInteger(0);
		final AtomicInteger deleteCount = new AtomicInteger(0);
		final AtomicBoolean headCall = new AtomicBoolean(false);
		driver.getReplicationDump(collectionName, null, null, null, null, TestComplexEntity01.class, new DumpHandler<TestComplexEntity01>() {
			public boolean head(ReplicationDumpHeader header) {
				headCall.set(true);
				assertThat(header.getCheckmore(), is(not(nullValue())));
				assertThat(header.getLastincluded(), is(not(nullValue())));
				assertThat(header.getLasttick(), is((nullValue())));
				assertThat(header.getActive(), is((nullValue())));
				return true;
			}
			public boolean handle(ReplicationDumpRecord<TestComplexEntity01> entity) {
				switch (entity.getType()) {
				case DOCUMENT_UPSERT:
				case EDGE_UPSERT:
					int x = upsertCount.getAndIncrement();
					assertThat(entity.getTick(), is(not(0L)));
					assertThat(entity.getKey(), is(not(nullValue())));
					assertThat(entity.getRev(), is(not(0L)));
					assertThat(entity.getData(), is(notNullValue()));
					assertThat(entity.getData().getDocumentKey(), is(notNullValue()));
					assertThat(entity.getData().getDocumentRevision(), is(not(0L)));
					assertThat(entity.getData().getDocumentHandle(), is(nullValue()));
					assertThat(entity.getData().getEntity().getAge(), is(not(0)));
					break;
				case DELETION:
					deleteCount.incrementAndGet();
					assertThat(entity.getTick(), is(not(0L)));
					assertThat(entity.getKey(), is(not(nullValue())));
					assertThat(entity.getRev(), is(not(0L)));
					assertThat(entity.getData(), is(nullValue()));
					break;
				}
				return true;
			}

		});
		
		assertThat(headCall.get(), is(true));
		assertThat(upsertCount.get(), is(11));
		assertThat(deleteCount.get(), is(10));
		
	}

	@Test
	public void test_get_dump_noticks() throws ArangoException {
		
		String collectionName = "rep_dump_test";
		
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}

		// create 10 document
		for (int i = 0; i < 10; i++) {
			TestComplexEntity01 entity = new TestComplexEntity01("user-" + i, "desc-" + i, 20+i);
			driver.createDocument(collectionName, entity, true, null);
		}
		// truncate
		try {
			driver.truncateCollection(collectionName);
		} catch (ArangoException e) {}
		// create 1 document
		TestComplexEntity01 entity = new TestComplexEntity01("user-99", "desc-99", 99);
		driver.createDocument(collectionName, entity, true, null);
		
		final AtomicInteger upsertCount = new AtomicInteger(0);
		final AtomicInteger deleteCount = new AtomicInteger(0);
		driver.getReplicationDump(collectionName, null, null, null, false, TestComplexEntity01.class, new DumpHandlerAdapter<TestComplexEntity01>() {
			public boolean handle(ReplicationDumpRecord<TestComplexEntity01> entity) {
				switch (entity.getType()) {
				case DOCUMENT_UPSERT:
				case EDGE_UPSERT:
					int x = upsertCount.getAndIncrement();
					assertThat(entity.getTick(), is((0L)));
					assertThat(entity.getKey(), is(not(nullValue())));
					assertThat(entity.getRev(), is(not(0L)));
					assertThat(entity.getData(), is(notNullValue()));
					assertThat(entity.getData().getDocumentKey(), is(notNullValue()));
					assertThat(entity.getData().getDocumentRevision(), is(not(0L)));
					assertThat(entity.getData().getDocumentHandle(), is(nullValue()));
					assertThat(entity.getData().getEntity().getAge(), is(not(0)));
					break;
				case DELETION:
					deleteCount.incrementAndGet();
					assertThat(entity.getTick(), is((0L)));
					assertThat(entity.getKey(), is(not(nullValue())));
					assertThat(entity.getRev(), is(not(0L)));
					assertThat(entity.getData(), is(nullValue()));
					break;
				}
				return true;
			}

		});
		
		assertThat(upsertCount.get(), is(11));
		assertThat(deleteCount.get(), is(10));
		
	}

	@Test
	public void test_get_dump_handler_control_1() throws ArangoException {
		
		String collectionName = "rep_dump_test";
		
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}

		// create 10 document
		for (int i = 0; i < 10; i++) {
			TestComplexEntity01 entity = new TestComplexEntity01("user-" + i, "desc-" + i, 20+i);
			driver.createDocument(collectionName, entity, true, null);
		}
		// truncate
		try {
			driver.truncateCollection(collectionName);
		} catch (ArangoException e) {}
		// create 1 document
		TestComplexEntity01 entity = new TestComplexEntity01("user-99", "desc-99", 99);
		driver.createDocument(collectionName, entity, true, null);
		
		final AtomicBoolean headCall = new AtomicBoolean(false);
		driver.getReplicationDump(collectionName, null, null, null, null, TestComplexEntity01.class, new DumpHandler<TestComplexEntity01>() {
			public boolean head(ReplicationDumpHeader header) {
				headCall.set(true);
				return false;
			}
			public boolean handle(ReplicationDumpRecord<TestComplexEntity01> entity) {
				fail("");
				return true;
			}
		});
		
		assertThat(headCall.get(), is(true));
		
	}

	@Test
	public void test_get_dump_handler_control_2() throws ArangoException {
		
		String collectionName = "rep_dump_test";
		
		try {
			driver.deleteCollection(collectionName);
		} catch (ArangoException e) {}
		try {
			driver.createCollection(collectionName);
		} catch (ArangoException e) {}

		// create 10 document
		for (int i = 0; i < 10; i++) {
			TestComplexEntity01 entity = new TestComplexEntity01("user-" + i, "desc-" + i, 20+i);
			driver.createDocument(collectionName, entity, true, null);
		}
		// truncate
		try {
			driver.truncateCollection(collectionName);
		} catch (ArangoException e) {}
		// create 1 document
		TestComplexEntity01 entity = new TestComplexEntity01("user-99", "desc-99", 99);
		driver.createDocument(collectionName, entity, true, null);
		
		final AtomicInteger handleCount = new AtomicInteger(0);
		final AtomicBoolean headCall = new AtomicBoolean(false);
		driver.getReplicationDump(collectionName, null, null, null, null, TestComplexEntity01.class, new DumpHandler<TestComplexEntity01>() {
			public boolean head(ReplicationDumpHeader header) {
				headCall.set(true);
				return true;
			}
			public boolean handle(ReplicationDumpRecord<TestComplexEntity01> entity) {
				int cnt = handleCount.incrementAndGet();
				if (cnt == 5) {
					return false;
				}
				return true;
			}

		});
		
		assertThat(headCall.get(), is(true));
		assertThat(handleCount.get(), is(5));
		
	}

	
	// TODO: Dump from-to
	
//	public void test_sync() throws ArangoException {
//		
//		driver.syncReplication(endpoint, database, username, password, restrictType, restrictCollections);
//		
//	}

	@Test
	public void test_server_id() throws ArangoException {
		
		String serverId = driver.getReplicationServerId();
		assertThat(serverId, is(notNullValue()));
		
	}
	
	@Test
	public void test_start_logger() throws ArangoException {
		
		boolean running = driver.startReplicationLogger();
		assertThat(running, is(true));

		boolean running2 = driver.startReplicationLogger();
		assertThat(running2, is(true));

	}

	@Test
	public void test_stop_logger() throws ArangoException {

		boolean running = driver.stopReplicationLogger();
		assertThat(running, is(false));

		boolean running2 = driver.stopReplicationLogger();
		assertThat(running2, is(false));

	}

	@Test
	public void test_logger_config() throws ArangoException {
		
		// set
		ReplicationLoggerConfigEntity config1 = driver.setReplicationLoggerConfig(true, true, 0L, 0L);
		assertThat(config1.isAutoStart(), is(true));
		assertThat(config1.isLogRemoteChanges(), is(true));
		assertThat(config1.getMaxEvents(), is(0L));
		assertThat(config1.getMaxEventsSize(), is(0L));
		
		// get
		ReplicationLoggerConfigEntity config2 = driver.getReplicationLoggerConfig();
		assertThat(config2.isAutoStart(), is(true));
		assertThat(config2.isLogRemoteChanges(), is(true));
		assertThat(config2.getMaxEvents(), is(0L));
		assertThat(config2.getMaxEventsSize(), is(0L));

		// set
		ReplicationLoggerConfigEntity config3 = driver.setReplicationLoggerConfig(false, false, 1048576L, 134217728L);
		assertThat(config3.isAutoStart(), is(false));
		assertThat(config3.isLogRemoteChanges(), is(false));
		assertThat(config3.getMaxEvents(), is(1048576L));
		assertThat(config3.getMaxEventsSize(), is(134217728L));

		// get
		ReplicationLoggerConfigEntity config4 = driver.getReplicationLoggerConfig();
		assertThat(config4.isAutoStart(), is(false));
		assertThat(config4.isLogRemoteChanges(), is(false));
		assertThat(config4.getMaxEvents(), is(1048576L));
		assertThat(config4.getMaxEventsSize(), is(134217728L));

		// fail (500 error)
		try {
			driver.setReplicationLoggerConfig(false, false, 0L, 32768L);
			fail();
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(500));
			assertThat(e.getErrorNumber(), is(1409));
		}

	}

	@Ignore("理由は以下のコメントを見てね")
	@Test
	public void test_set_applier_config_endpoint_null() {

		// endpointはmust指定のはずなのに、1.4.0時点ではエラーにならない。
		// 一度endpointを設定してしまうと、REPLICATION-APPLIER-CONFIG ファイルに保存されてしまい、
		// 以降、修正はできてもリセットはできない。
		// インストール直後のみ再現するケースである。
		
		try {
			driver.setReplicationApplierConfig(
					null, // endpoint is null
					null, null, null, 
					99, 98, 97, 1024, true, true);
			fail("");
		} catch (ArangoException e) {
			assertThat(e.getCode(), is(400));
			assertThat(e.getErrorNumber(), is(1410));
		}
	}
	
	@Test
	public void test_set_get_applier_config() throws ArangoException {

		// master
		ReplicationApplierConfigEntity config = driver.setReplicationApplierConfig(
				configure.getEndpoint(), 
				null, "root", "", 
				99, 98, 97, 1024, true, true);
		
		assertThat(config.getEndpoint(), is(configure.getEndpoint()));
		assertThat(config.getDatabase(), is("_system"));
		assertThat(config.getMaxConnectRetries(), is(99));
		assertThat(config.getConnectTimeout(), is(98));
		assertThat(config.getRequestTimeout(), is(97));
		assertThat(config.getChunkSize(), is(1024));
		assertThat(config.getAutoStart(), is(true));
		assertThat(config.getAdaptivePolling(), is(true));
		assertThat(config.getUsername(), is("root"));
		assertThat(config.getPassword(), is(nullValue()));
	
		config = driver.getReplicationApplierConfig();		
		assertThat(config.getEndpoint(), is(configure.getEndpoint()));
		assertThat(config.getDatabase(), is("_system"));
		assertThat(config.getMaxConnectRetries(), is(99));
		assertThat(config.getConnectTimeout(), is(98));
		assertThat(config.getRequestTimeout(), is(97));
		assertThat(config.getChunkSize(), is(1024));
		assertThat(config.getAutoStart(), is(true));
		assertThat(config.getAdaptivePolling(), is(true));
		assertThat(config.getUsername(), is("root"));
		assertThat(config.getPassword(), is(nullValue()));
		
	}
	
	@Test
	public void test_start_stop_applier() throws ArangoException {
		
		ReplicationApplierStateEntity state1 = driver.startReplicationApplier(null);
		assertThat(state1.getStatusCode(), is(200));
		assertThat(state1.getServerVersion(), is(notNullValue()));
		assertThat(state1.getServerId(), is(notNullValue()));
		assertThat(state1.getEndpoint(), is(notNullValue()));
		assertThat(state1.getDatabase(), is("_system"));
		assertThat(state1.getState().getRunning(), is(true));
		assertThat(state1.getState().getLastAppliedContinuousTick(), is(nullValue()));
		assertThat(state1.getState().getLastProcessedContinuousTick(), is(nullValue()));
		assertThat(state1.getState().getLastAvailableContinuousTick(), is(nullValue()));
		assertThat(state1.getState().getTime(), is(notNullValue()));
		assertThat(state1.getState().getTotalRequests().longValue(), is(not(0L)));
		assertThat(state1.getState().getTotalFailedConnects().longValue(), is(not(0L)));
		assertThat(state1.getState().getTotalEvents(), is(notNullValue()));
		// LastError, Progress -> see Sceinario Test
		
		ReplicationApplierStateEntity state2 = driver.stopReplicationApplier();
		assertThat(state2.getStatusCode(), is(200));
		assertThat(state2.getServerVersion(), is(notNullValue()));
		assertThat(state2.getServerId(), is(notNullValue()));
		assertThat(state2.getEndpoint(), is(notNullValue()));
		assertThat(state2.getDatabase(), is("_system"));
		assertThat(state2.getState().getRunning(), is(false));
		assertThat(state2.getState().getLastAppliedContinuousTick(), is(nullValue()));
		assertThat(state2.getState().getLastProcessedContinuousTick(), is(nullValue()));
		assertThat(state2.getState().getLastAvailableContinuousTick(), is(nullValue()));
		assertThat(state2.getState().getTime(), is(notNullValue()));
		assertThat(state2.getState().getTotalRequests().longValue(), is(not(0L)));
		assertThat(state2.getState().getTotalFailedConnects().longValue(), is(not(0L)));
		assertThat(state2.getState().getTotalEvents(), is(notNullValue()));
		
	}

	@Test
	public void test_logger_state() throws ArangoException {
		
		ReplicationLoggerStateEntity entity = driver.getReplicationLoggerState();
		
		assertThat(entity.getState().isRunning(), is(false));
		assertThat(entity.getState().getLastLogTick(), is(not(0L)));
		assertThat(entity.getState().getTotalEvents(), is(not(0L)));
		assertThat(entity.getState().getTime(), is(notNullValue()));
		
		assertThat(entity.getServerId(), is(notNullValue()));
		assertThat(entity.getServerVersion(), is(notNullValue()));
		assertThat(entity.getClients().size(), is(0)); // see another test-class(scenario1)
		
	}

	@Test
	public void test_applier_state() throws ArangoException {
		
		ReplicationApplierStateEntity state = driver.getReplicationApplierState();
		assertThat(state.getStatusCode(), is(200));
		assertThat(state.getServerVersion(), is(notNullValue()));
		assertThat(state.getServerId(), is(notNullValue()));
		assertThat(state.getEndpoint(), is(notNullValue()));
		assertThat(state.getDatabase(), is("_system"));
		assertThat(state.getState().getRunning(), is(false));
		assertThat(state.getState().getLastAppliedContinuousTick(), is(nullValue()));
		assertThat(state.getState().getLastProcessedContinuousTick(), is(nullValue()));
		assertThat(state.getState().getLastAvailableContinuousTick(), is(nullValue()));
		assertThat(state.getState().getTime(), is(notNullValue()));
		assertThat(state.getState().getTotalRequests().longValue(), is(not(0L)));
		assertThat(state.getState().getTotalFailedConnects().longValue(), is(not(0L)));
		assertThat(state.getState().getTotalEvents(), is(notNullValue()));
		
	}

}

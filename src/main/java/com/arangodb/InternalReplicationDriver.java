package com.arangodb;

import com.arangodb.entity.ReplicationApplierConfigEntity;
import com.arangodb.entity.ReplicationApplierStateEntity;
import com.arangodb.entity.ReplicationInventoryEntity;
import com.arangodb.entity.ReplicationLoggerConfigEntity;
import com.arangodb.entity.ReplicationLoggerStateEntity;
import com.arangodb.entity.ReplicationSyncEntity;
import com.arangodb.entity.RestrictType;
import com.arangodb.impl.BaseDriverInterface;
import com.arangodb.util.DumpHandler;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalReplicationDriver extends BaseDriverInterface {
	ReplicationInventoryEntity getReplicationInventory(String database, Boolean includeSystem) throws ArangoException;

	<T> void getReplicationDump(
		String database,
		String collectionName,
		Long from,
		Long to,
		Integer chunkSize,
		Boolean ticks,
		Class<T> clazz,
		DumpHandler<T> handler) throws ArangoException;

	ReplicationSyncEntity syncReplication(
		String localDatabase,
		String endpoint,
		String database,
		String username,
		String password,
		RestrictType restrictType,
		String... restrictCollections) throws ArangoException;

	String getReplicationServerId() throws ArangoException;

	boolean startReplicationLogger(String database) throws ArangoException;

	boolean stopReplicationLogger(String database) throws ArangoException;

	ReplicationLoggerConfigEntity getReplicationLoggerConfig(String database) throws ArangoException;

	ReplicationLoggerConfigEntity setReplicationLoggerConfig(
		String database,
		Boolean autoStart,
		Boolean logRemoteChanges,
		Long maxEvents,
		Long maxEventsSize) throws ArangoException;

	ReplicationLoggerStateEntity getReplicationLoggerState(String database) throws ArangoException;

	ReplicationApplierConfigEntity getReplicationApplierConfig(String database) throws ArangoException;

	ReplicationApplierConfigEntity setReplicationApplierConfig(
		String localDatabase,
		String endpoint,
		String database,
		String username,
		String password,
		Integer maxConnectRetries,
		Integer connectTimeout,
		Integer requestTimeout,
		Integer chunkSize,
		Boolean autoStart,
		Boolean adaptivePolling) throws ArangoException;

	ReplicationApplierConfigEntity setReplicationApplierConfig(String database, ReplicationApplierConfigEntity param)
			throws ArangoException;

	ReplicationApplierStateEntity startReplicationApplier(String database, Long from) throws ArangoException;

	ReplicationApplierStateEntity stopReplicationApplier(String database) throws ArangoException;

	ReplicationApplierStateEntity getReplicationApplierState(String database) throws ArangoException;
}

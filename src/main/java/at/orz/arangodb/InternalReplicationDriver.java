package at.orz.arangodb;

import at.orz.arangodb.entity.*;
import at.orz.arangodb.impl.BaseDriverInterface;
import at.orz.arangodb.util.DumpHandler;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalReplicationDriver  extends BaseDriverInterface {
  ReplicationInventoryEntity getReplicationInventory(String database, Boolean includeSystem) throws ArangoException;

  <T> void getReplicationDump(
    String database,
    String collectionName,
    Long from, Long to, Integer chunkSize, Boolean ticks,
    Class<T> clazz, DumpHandler<T> handler) throws ArangoException;

  ReplicationSyncEntity syncReplication(
    String _database,
    String endpoint, String database,
    String username, String password,
    RestrictType restrictType, String... restrictCollections
  ) throws ArangoException;

  String getReplicationServerId() throws ArangoException;

  boolean startReplicationLogger(String database) throws ArangoException;

  boolean stopReplicationLogger(String database) throws ArangoException;

  ReplicationLoggerConfigEntity getReplicationLoggerConfig(String database) throws ArangoException;

  ReplicationLoggerConfigEntity setReplicationLoggerConfig(
    String database,
    Boolean autoStart,
    Boolean logRemoteChanges,
    Long maxEvents,
    Long maxEventsSize
  ) throws ArangoException;

  ReplicationLoggerStateEntity getReplicationLoggerState(String database) throws ArangoException;

  ReplicationApplierConfigEntity getReplicationApplierConfig(String database) throws ArangoException;

  ReplicationApplierConfigEntity setReplicationApplierConfig(
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
  ) throws ArangoException;

  ReplicationApplierConfigEntity setReplicationApplierConfig(
    String database,
    ReplicationApplierConfigEntity param
  ) throws ArangoException;

  ReplicationApplierStateEntity startReplicationApplier(String database, Long from) throws ArangoException;

  ReplicationApplierStateEntity stopReplicationApplier(String database) throws ArangoException;

  ReplicationApplierStateEntity getReplicationApplierState(String database) throws ArangoException;
}

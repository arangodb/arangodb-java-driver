package at.orz.arangodb;

import at.orz.arangodb.entity.CollectionEntity;
import at.orz.arangodb.entity.CollectionKeyOption;
import at.orz.arangodb.entity.CollectionType;
import at.orz.arangodb.entity.CollectionsEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalCollectionDriver  extends BaseDriverInterface {
  CollectionEntity createCollection(
    String database,
    String name,
    Boolean waitForSync,
    Boolean doCompact,
    Integer journalSize,
    Boolean isSystem,
    Boolean isVolatile,
    CollectionType type,
    CollectionKeyOption keyOptions
  ) throws ArangoException;

  CollectionEntity getCollection(String database, String name) throws ArangoException;

  CollectionEntity getCollectionRevision(String database, String name) throws ArangoException;

  CollectionEntity getCollectionProperties(String database, String name) throws ArangoException;

  CollectionEntity getCollectionCount(String database, String name) throws ArangoException;

  CollectionEntity getCollectionFigures(String database, String name) throws ArangoException;

  CollectionEntity getCollectionChecksum(String database, String name, Boolean withRevisions, Boolean withData) throws ArangoException;

  CollectionsEntity getCollections(String database, Boolean excludeSystem) throws ArangoException;

  CollectionEntity loadCollection(String database, String name, Boolean count) throws ArangoException;

  CollectionEntity unloadCollection(String database, String name) throws ArangoException;

  CollectionEntity truncateCollection(String database, String name) throws ArangoException;

  CollectionEntity setCollectionProperties(String database, String name, Boolean newWaitForSync, Long journalSize) throws ArangoException;

  CollectionEntity renameCollection(String database, String name, String newName) throws ArangoException;

  CollectionEntity deleteCollection(String database, String name) throws ArangoException;
}

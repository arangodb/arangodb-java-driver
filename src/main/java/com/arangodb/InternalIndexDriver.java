package com.arangodb;

import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalIndexDriver  extends BaseDriverInterface {
  IndexEntity createIndex(String database, String collectionName, IndexType type, boolean unique, boolean sparse, String... fields) throws ArangoException;

  IndexEntity createIndex(String database, String collectionName, IndexType type, boolean unique, String... fields) throws ArangoException;

  IndexEntity createCappedIndex(String database, String collectionName, int size) throws ArangoException;

  IndexEntity createCappedByDocumentSizeIndex(String database, String collectionName, int byteSize) throws ArangoException;

  IndexEntity createFulltextIndex(String database, String collectionName, Integer minLength, String... fields) throws ArangoException;

  IndexEntity deleteIndex(String database, String indexHandle) throws ArangoException;

  IndexEntity getIndex(String database, String indexHandle) throws ArangoException;

  IndexesEntity getIndexes(String database, String collectionName) throws ArangoException;
}

package com.arangodb;

import java.util.Map;

import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalCursorDriver extends BaseDriverInterface {
  CursorEntity<?> validateQuery(String database, String query) throws ArangoException;

  <T> CursorEntity<T> executeQuery(
    String database,
    String query,
    Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount,
    Integer batchSize) throws ArangoException;

  <T> CursorEntity<T> continueQuery(String database, long cursorId, Class<?>... clazz) throws ArangoException;

  DefaultEntity finishQuery(String database, long cursorId) throws ArangoException;

  <T> CursorResultSet<T> executeQueryWithResultSet(
    String database,
    String query,
    Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount,
    Integer batchSize) throws ArangoException;
}

package at.orz.arangodb;

import at.orz.arangodb.entity.CursorEntity;
import at.orz.arangodb.entity.DefaultEntity;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.Map;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalCursorDriver  extends BaseDriverInterface {
  CursorEntity<?> validateQuery(String database, String query) throws ArangoException;

  <T> CursorEntity<T> executeQuery(
    String database,
    String query, Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount, Integer batchSize) throws ArangoException;

  <T> CursorEntity<T> continueQuery(String database, long cursorId, Class<?>... clazz) throws ArangoException;

  DefaultEntity finishQuery(String database, long cursorId) throws ArangoException;

  <T> CursorResultSet<T> executeQueryWithResultSet(
    String database,
    String query, Map<String, Object> bindVars,
    Class<T> clazz,
    Boolean calcCount, Integer batchSize) throws ArangoException;
}

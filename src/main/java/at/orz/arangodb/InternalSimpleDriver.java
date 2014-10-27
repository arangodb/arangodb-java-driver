package at.orz.arangodb;

import at.orz.arangodb.entity.*;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.Map;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalSimpleDriver  extends BaseDriverInterface {
  <T> CursorEntity<T> executeSimpleAll(
    String database,
    String collectionName, int skip, int limit,
    Class<?> clazz) throws ArangoException;

  <T> CursorResultSet<T> executeSimpleAllWithResultSet(
    String database,
    String collectionName, int skip, int limit,
    Class<?> clazz) throws ArangoException;

  <T> CursorEntity<DocumentEntity<T>> executeSimpleAllWithDocument(
    String database,
    String collectionName, int skip, int limit,
    Class<?> clazz) throws ArangoException;

  <T> CursorResultSet<DocumentEntity<T>> executeSimpleAllWithDocumentResultSet(
    String database,
    String collectionName, int skip, int limit,
    Class<?> clazz) throws ArangoException;

  <T> CursorEntity<T> executeSimpleByExample(
    String database,
    String collectionName,
    Map<String, Object> example,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<T> executeSimpleByExampleWithResultSet(
    String database,
    String collectionName, Map<String, Object> example,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorEntity<DocumentEntity<T>> executeSimpleByExampleWithDocument(
    String database,
    String collectionName,
    Map<String, Object> example,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<DocumentEntity<T>> executeSimpleByExampleWithDocumentResultSet(
    String database,
    String collectionName, Map<String, Object> example,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> ScalarExampleEntity<T> executeSimpleFirstExample(
    String database,
    String collectionName,
    Map<String, Object> example,
    Class<?> clazz
  ) throws ArangoException;

  <T> ScalarExampleEntity<T> executeSimpleAny(
    String database,
    String collectionName,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorEntity<T> executeSimpleRange(
    String database,
    String collectionName,
    String attribute,
    Object left, Object right, Boolean closed,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<T> executeSimpleRangeWithResultSet(
    String database,
    String collectionName,
    String attribute,
    Object left, Object right, Boolean closed,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorEntity<DocumentEntity<T>> executeSimpleRangeWithDocument(
    String database,
    String collectionName,
    String attribute,
    Object left, Object right, Boolean closed,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<DocumentEntity<T>> executeSimpleRangeWithDocumentResultSet(
    String database,
    String collectionName,
    String attribute,
    Object left, Object right, Boolean closed,
    int skip, int limit,
    Class<?> clazz
  ) throws ArangoException;

  SimpleByResultEntity executeSimpleRemoveByExample(
    String database,
    String collectionName,
    Map<String, Object> example,
    Boolean waitForSync,
    Integer limit) throws ArangoException;

  SimpleByResultEntity executeSimpleReplaceByExample(
    String database,
    String collectionName,
    Map<String, Object> example,
    Map<String, Object> newValue,
    Boolean waitForSync,
    Integer limit) throws ArangoException;

  SimpleByResultEntity executeSimpleUpdateByExample(
    String database,
    String collectionName,
    Map<String, Object> example,
    Map<String, Object> newValue,
    Boolean keepNull,
    Boolean waitForSync,
    Integer limit) throws ArangoException;

  <T> CursorEntity<T> executeSimpleFulltext(
    String database,
    String collectionName,
    String attribute, String query,
    int skip, int limit,
    String index,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<T> executeSimpleFulltextWithResultSet(
    String database,
    String collectionName,
    String attribute, String query,
    int skip, int limit,
    String index,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorEntity<DocumentEntity<T>> executeSimpleFulltextWithDocument(
    String database,
    String collectionName,
    String attribute, String query,
    int skip, int limit,
    String index,
    Class<?> clazz
  ) throws ArangoException;

  <T> CursorResultSet<DocumentEntity<T>> executeSimpleFulltextWithDocumentResultSet(
    String database,
    String collectionName,
    String attribute, String query,
    int skip, int limit,
    String index,
    Class<?> clazz
  ) throws ArangoException;

  <T> DocumentResultEntity<T> executeSimpleFirst(
    String database,
    String collectionName,
    Integer count,
    Class<?> clazz) throws ArangoException;

  <T> DocumentResultEntity<T> executeSimpleLast(
    String database,
    String collectionName,
    Integer count,
    Class<?> clazz) throws ArangoException;
}

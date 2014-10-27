package at.orz.arangodb;

import at.orz.arangodb.entity.DocumentEntity;
import at.orz.arangodb.entity.Policy;
import at.orz.arangodb.impl.BaseDriverInterface;

import java.util.List;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalDocumentDriver  extends BaseDriverInterface {
  <T> DocumentEntity<T> createDocument(String database, String collectionName, String documentKey, Object value, Boolean createCollection, Boolean waitForSync) throws ArangoException;

  <T> DocumentEntity<T> createDocumentRaw(String database, String collectionName, String documentKey, String rawJsonString, Boolean createCollection, Boolean waitForSync) throws ArangoException;

  <T> DocumentEntity<T> replaceDocument(String database, String documentHandle, Object value, Long rev, Policy policy, Boolean waitForSync) throws ArangoException;

  <T> DocumentEntity<T> updateDocument(String database, String documentHandle, Object value, Long rev, Policy policy, Boolean waitForSync, Boolean keepNull) throws ArangoException;

  List<String> getDocuments(String database, String collectionName, boolean handleConvert) throws ArangoException;

  long checkDocument(String database, String documentHandle) throws ArangoException;

  <T> DocumentEntity<T> getDocument(String database, String documentHandle, Class<?> clazz, Long ifNoneMatchRevision, Long ifMatchRevision) throws ArangoException;

  DocumentEntity<?> deleteDocument(String database, String documentHandle, Long rev, Policy policy) throws ArangoException;
}

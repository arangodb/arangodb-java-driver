package com.arangodb;

import java.util.List;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.impl.BaseDriverInterface;

/**
 * Created by fbartels on 10/27/14.
 */
public interface InternalDocumentDriver extends BaseDriverInterface {
	<T> DocumentEntity<T> createDocument(
		String database,
		String collectionName,
		String documentKey,
		T value,
		Boolean waitForSync) throws ArangoException;

	DocumentEntity<String> createDocumentRaw(
		String database,
		String collectionName,
		String rawJsonString,
		Boolean waitForSync) throws ArangoException;

	<T> DocumentEntity<T> replaceDocument(
		String database,
		String documentHandle,
		T value,
		String rev,
		Boolean waitForSync) throws ArangoException;

	DocumentEntity<String> replaceDocumentRaw(
		String database,
		String documentHandle,
		String rawJsonString,
		String rev,
		Boolean waitForSync) throws ArangoException;

	<T> DocumentEntity<T> updateDocument(
		String database,
		String documentHandle,
		T value,
		String rev,
		Boolean waitForSync,
		Boolean keepNull) throws ArangoException;

	DocumentEntity<String> updateDocumentRaw(
		String database,
		String documentHandle,
		String rawJsonString,
		String rev,
		Boolean waitForSync,
		Boolean keepNull) throws ArangoException;

	List<String> getDocuments(String database, String collectionName) throws ArangoException;

	String checkDocument(String database, String documentHandle) throws ArangoException;

	<T> DocumentEntity<T> getDocument(
		String database,
		String documentHandle,
		Class<T> clazz,
		String ifNoneMatchRevision,
		String ifMatchRevision) throws ArangoException;

	String getDocumentRaw(String database, String documentHandle, String ifNoneMatchRevision, String ifMatchRevision)
			throws ArangoException;

	DocumentEntity<?> deleteDocument(String database, String documentHandle, String rev) throws ArangoException;

	<T> EdgeEntity<T> createEdge(
		String database,
		String collectionName,
		String documentKey,
		T value,
		String fromHandle,
		String toHandle,
		Boolean waitForSync) throws ArangoException;

}

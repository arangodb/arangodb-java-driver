/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb;

import java.util.Collection;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionRevisionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentImportEntity;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentImportOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;

/**
 * Interface for operations on ArangoDB collection level.
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/">Collection API Documentation</a>
 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/">Documents API Documentation</a>
 * @author Mark Vollmary
 */
public interface ArangoCollection {

	/**
	 * The the handler of the database the collection is within
	 * 
	 * @return database handler
	 */
	public ArangoDatabase db();

	/**
	 * The name of the collection
	 * 
	 * @return collection name
	 */
	public String name();

	/**
	 * Creates a new document from the given document, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentCreateEntity<T> insertDocument(T value) throws ArangoDBException;

	/**
	 * Creates a new document from the given document, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options) throws ArangoDBException;

	/**
	 * Creates new documents from the given documents, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(Collection<T> values) throws ArangoDBException;

	/**
	 * Creates new documents from the given documents, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
		Collection<T> values,
		DocumentCreateOptions options) throws ArangoDBException;

	/**
	 * Bulk imports the given values into the collection.
	 * 
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(Collection<?> values) throws ArangoDBException;

	/**
	 * Bulk imports the given values into the collection.
	 * 
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @param options
	 *            Additional options, can be null
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(Collection<?> values, DocumentImportOptions options) throws ArangoDBException;

	/**
	 * Bulk imports the given values into the collection.
	 * 
	 * @param values
	 *            JSON-encoded array of objects that will be stored as documents
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(String values) throws ArangoDBException;

	/**
	 * Bulk imports the given values into the collection.
	 * 
	 * @param values
	 *            JSON-encoded array of objects that will be stored as documents
	 * @param options
	 *            Additional options, can be null
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(String values, DocumentImportOptions options) throws ArangoDBException;

	/**
	 * Retrieves the document with the given {@code key} from the collection.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @return the document identified by the key
	 * @throws ArangoDBException
	 */
	<T> T getDocument(String key, Class<T> type) throws ArangoDBException;

	/**
	 * Retrieves the document with the given {@code key} from the collection.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the document identified by the key
	 * @throws ArangoDBException
	 */
	<T> T getDocument(String key, Class<T> type, DocumentReadOptions options) throws ArangoDBException;

	/**
	 * Retrieves multiple documents with the given {@code _key} from the collection.
	 * 
	 * @param keys
	 *            The keys of the documents
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json)
	 * @return the documents and possible errors
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<T> getDocuments(Collection<String> keys, Class<T> type) throws ArangoDBException;

	/**
	 * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
	 * precondition is violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentUpdateEntity<T> replaceDocument(String key, T value) throws ArangoDBException;

	/**
	 * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
	 * precondition is violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options)
			throws ArangoDBException;

	/**
	 * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
	 * specified by the _key attributes in the documents in values.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(Collection<T> values) throws ArangoDBException;

	/**
	 * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
	 * specified by the _key attributes in the documents in values.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
		Collection<T> values,
		DocumentReplaceOptions options) throws ArangoDBException;

	/**
	 * Partially updates the document identified by document-key. The value must contain a document with the attributes
	 * to patch (the patch document). All attributes from the patch document will be added to the existing document if
	 * they do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentUpdateEntity<T> updateDocument(String key, T value) throws ArangoDBException;

	/**
	 * Partially updates the document identified by document-key. The value must contain a document with the attributes
	 * to patch (the patch document). All attributes from the patch document will be added to the existing document if
	 * they do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentUpdateEntity<T> updateDocument(String key, T value, DocumentUpdateOptions options)
			throws ArangoDBException;

	/**
	 * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
	 * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
	 * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
	 * overwritten in the existing documents if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A list of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(Collection<T> values) throws ArangoDBException;

	/**
	 * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
	 * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
	 * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
	 * overwritten in the existing documents if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A list of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
		Collection<T> values,
		DocumentUpdateOptions options) throws ArangoDBException;

	/**
	 * Deletes the document with the given {@code key} from the collection.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-a-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	DocumentDeleteEntity<Void> deleteDocument(String key) throws ArangoDBException;

	/**
	 * Deletes the document with the given {@code key} from the collection.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-a-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	<T> DocumentDeleteEntity<T> deleteDocument(String key, Class<T> type, DocumentDeleteOptions options)
			throws ArangoDBException;

	/**
	 * Deletes multiple documents from the collection.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-multiple-documents">API
	 *      Documentation</a>
	 * @param values
	 *            The keys of the documents or the documents themselves
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(Collection<?> values) throws ArangoDBException;

	/**
	 * Deletes multiple documents from the collection.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-multiple-documents">API
	 *      Documentation</a>
	 * @param values
	 *            The keys of the documents or the documents themselves
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
		Collection<?> values,
		Class<T> type,
		DocumentDeleteOptions options) throws ArangoDBException;

	/**
	 * Checks if the document exists by reading a single document head
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @return true if the document was found, otherwise false
	 */
	Boolean documentExists(String key);

	/**
	 * Checks if the document exists by reading a single document head
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param options
	 *            Additional options, can be null
	 * @return true if the document was found, otherwise false
	 * @throws ArangoDBException
	 *             only thrown when {@link DocumentExistsOptions#isCatchException()} == false
	 */
	Boolean documentExists(String key, DocumentExistsOptions options) throws ArangoDBException;

	/**
	 * Fetches information about the index with the given {@code id} and returns it.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity getIndex(String id) throws ArangoDBException;

	/**
	 * Deletes the index with the given {@code id} from the collection.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return the id of the index
	 * @throws ArangoDBException
	 */
	String deleteIndex(String id) throws ArangoDBException;

	/**
	 * Creates a hash index for the collection if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Hash.html#create-hash-index">API Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity ensureHashIndex(Iterable<String> fields, HashIndexOptions options) throws ArangoDBException;

	/**
	 * Creates a skip-list index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Skiplist.html#create-skip-list">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity ensureSkiplistIndex(Iterable<String> fields, SkiplistIndexOptions options) throws ArangoDBException;

	/**
	 * Creates a persistent index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Persistent.html#create-a-persistent-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity ensurePersistentIndex(Iterable<String> fields, PersistentIndexOptions options) throws ArangoDBException;

	/**
	 * Creates a geo-spatial index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Geo.html#create-geospatial-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity ensureGeoIndex(Iterable<String> fields, GeoIndexOptions options) throws ArangoDBException;

	/**
	 * Creates a fulltext index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Fulltext.html#create-fulltext-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity ensureFulltextIndex(Iterable<String> fields, FulltextIndexOptions options) throws ArangoDBException;

	/**
	 * Fetches a list of all indexes on this collection.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-all-indexes-of-a-collection">API
	 *      Documentation</a>
	 * @return information about the indexes
	 * @throws ArangoDBException
	 */
	Collection<IndexEntity> getIndexes() throws ArangoDBException;

	/**
	 * Checks whether the collection exists
	 * 
	 * @return true if the collection exists, otherwise false
	 */
	boolean exists() throws ArangoDBException;

	/**
	 * Removes all documents from the collection, but leaves the indexes intact
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#truncate-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity truncate() throws ArangoDBException;

	/**
	 * Counts the documents in a collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-number-of-documents-in-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection, including the number of documents
	 * @throws ArangoDBException
	 */
	CollectionPropertiesEntity count() throws ArangoDBException;

	/**
	 * Creates a collection for this collection's name, then returns collection information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity create() throws ArangoDBException;

	/**
	 * Creates a collection with the given {@code options} for this collection's name, then returns collection
	 * information from the server.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity create(CollectionCreateOptions options) throws ArangoDBException;

	/**
	 * Deletes the collection from the database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	void drop() throws ArangoDBException;

	/**
	 * Deletes the collection from the database.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @param isSystem
	 *            Whether or not the collection to drop is a system collection. This parameter must be set to true in
	 *            order to drop a system collection.
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	void drop(boolean isSystem) throws ArangoDBException;

	/**
	 * Tells the server to load the collection into memory.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#load-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity load() throws ArangoDBException;

	/**
	 * Tells the server to remove the collection from memory. This call does not delete any documents. You can use the
	 * collection afterwards; in which case it will be loaded into memory, again.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#unload-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity unload() throws ArangoDBException;

	/**
	 * Returns information about the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-information-about-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity getInfo() throws ArangoDBException;

	/**
	 * Reads the properties of the specified collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#read-properties-of-a-collection">API
	 *      Documentation</a>
	 * @return properties of the collection
	 * @throws ArangoDBException
	 */
	CollectionPropertiesEntity getProperties() throws ArangoDBException;

	/**
	 * Changes the properties of the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#change-properties-of-a-collection">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return properties of the collection
	 * @throws ArangoDBException
	 */
	CollectionPropertiesEntity changeProperties(CollectionPropertiesOptions options) throws ArangoDBException;

	/**
	 * Renames the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#rename-collection">API
	 *      Documentation</a>
	 * @param newName
	 *            The new name
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity rename(String newName) throws ArangoDBException;

	/**
	 * Retrieve the collections revision
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-collection-revision-id">API
	 *      Documentation</a>
	 * @return information about the collection, including the collections revision
	 * @throws ArangoDBException
	 */
	CollectionRevisionEntity getRevision() throws ArangoDBException;

	/**
	 * Grants or revoke access to the collection for user user. You need permission to the _system database in order to
	 * execute this call.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-collection-access"> API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user
	 * @param permissions
	 *            The permissions the user grant
	 * @throws ArangoDBException
	 */
	void grantAccess(String user, Permissions permissions) throws ArangoDBException;

	/**
	 * Revokes access to the collection for user user. You need permission to the _system database in order to execute
	 * this call.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-collection-access"> API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user
	 * @throws ArangoDBException
	 */
	void revokeAccess(String user) throws ArangoDBException;

	/**
	 * Clear the collection access level, revert back to the default access level.
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/UserManagement/index.html#grant-or-revoke-collection-access"> API
	 *      Documentation</a>
	 * @param user
	 *            The name of the user
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	void resetAccess(String user) throws ArangoDBException;

	/**
	 * Get the collection access level
	 * 
	 * @see <a href= "https://docs.arangodb.com/current/HTTP/UserManagement/#get-the-specific-collection-access-level">
	 *      API Documentation</a>
	 * @param user
	 *            The name of the user
	 * @return permissions of the user
	 * @since ArangoDB 3.2.0
	 * @throws ArangoDBException
	 */
	Permissions getPermissions(String user) throws ArangoDBException;

}

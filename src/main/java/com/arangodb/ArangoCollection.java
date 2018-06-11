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
 * @author Mark Vollmary
 *
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
	<T> DocumentCreateEntity<T> insertDocument(final T value) throws ArangoDBException;

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
	<T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options)
			throws ArangoDBException;

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
	<T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(final Collection<T> values)
			throws ArangoDBException;

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
		final Collection<T> values,
		final DocumentCreateOptions options) throws ArangoDBException;

	/**
	 * Imports documents
	 * 
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(final Collection<?> values) throws ArangoDBException;

	/**
	 * Imports documents
	 * 
	 * @param values
	 *            a list of Objects that will be stored as documents
	 * @param options
	 *            Additional options, can be null
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(final Collection<?> values, final DocumentImportOptions options)
			throws ArangoDBException;

	/**
	 * Imports documents
	 * 
	 * @param values
	 *            JSON-encoded array of objects that will be stored as documents
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(final String values) throws ArangoDBException;

	/**
	 * Imports documents
	 * 
	 * @param values
	 *            JSON-encoded array of objects that will be stored as documents
	 * @param options
	 *            Additional options, can be null
	 * @return information about the import
	 * @throws ArangoDBException
	 */
	DocumentImportEntity importDocuments(final String values, final DocumentImportOptions options)
			throws ArangoDBException;

	/**
	 * Reads a single document
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
	<T> T getDocument(final String key, final Class<T> type) throws ArangoDBException;

	/**
	 * Reads a single document
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
	<T> T getDocument(final String key, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException;

	/**
	 * Reads multiple documents
	 * 
	 * @param keys
	 *            The keys of the documents
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json)
	 * @return the documents and possible errors
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<T> getDocuments(final Collection<String> keys, final Class<T> type)
			throws ArangoDBException;

	/**
	 * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
	 * violated
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
	<T> DocumentUpdateEntity<T> replaceDocument(final String key, final T value) throws ArangoDBException;

	/**
	 * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
	 * violated
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
	<T> DocumentUpdateEntity<T> replaceDocument(final String key, final T value, final DocumentReplaceOptions options)
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
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(final Collection<T> values)
			throws ArangoDBException;

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
		final Collection<T> values,
		final DocumentReplaceOptions options) throws ArangoDBException;

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
	<T> DocumentUpdateEntity<T> updateDocument(final String key, final T value) throws ArangoDBException;

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
	<T> DocumentUpdateEntity<T> updateDocument(final String key, final T value, final DocumentUpdateOptions options)
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
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(final Collection<T> values)
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
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	<T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
		final Collection<T> values,
		final DocumentUpdateOptions options) throws ArangoDBException;

	/**
	 * Removes a document
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
	DocumentDeleteEntity<Void> deleteDocument(final String key) throws ArangoDBException;

	/**
	 * Removes a document
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
	<T> DocumentDeleteEntity<T> deleteDocument(
		final String key,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException;

	/**
	 * Removes multiple document
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
	MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(final Collection<?> values)
			throws ArangoDBException;

	/**
	 * Removes multiple document
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
		final Collection<?> values,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException;

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
	Boolean documentExists(final String key);

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
	Boolean documentExists(final String key, final DocumentExistsOptions options) throws ArangoDBException;

	/**
	 * Returns an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	IndexEntity getIndex(final String id) throws ArangoDBException;

	/**
	 * Deletes an index
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#delete-index">API Documentation</a>
	 * @param id
	 *            The index-handle
	 * @return the id of the index
	 * @throws ArangoDBException
	 */
	String deleteIndex(final String id) throws ArangoDBException;

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
	IndexEntity ensureHashIndex(final Iterable<String> fields, final HashIndexOptions options) throws ArangoDBException;

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
	IndexEntity ensureSkiplistIndex(final Iterable<String> fields, final SkiplistIndexOptions options)
			throws ArangoDBException;

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
	IndexEntity ensurePersistentIndex(final Iterable<String> fields, final PersistentIndexOptions options)
			throws ArangoDBException;

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
	IndexEntity ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options) throws ArangoDBException;

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
	IndexEntity ensureFulltextIndex(final Iterable<String> fields, final FulltextIndexOptions options)
			throws ArangoDBException;

	/**
	 * Returns all indexes of the collection
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
	 * Creates the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity create() throws ArangoDBException;

	/**
	 * Creates the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#create-collection">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity create(final CollectionCreateOptions options) throws ArangoDBException;

	/**
	 * Drops the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	void drop() throws ArangoDBException;

	/**
	 * Drops the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @param isSystem
	 *            Whether or not the collection to drop is a system collection. This parameter must be set to true in
	 *            order to drop a system collection.
	 * @since ArangoDB 3.1.0
	 * @throws ArangoDBException
	 */
	void drop(final boolean isSystem) throws ArangoDBException;

	/**
	 * Loads a collection into memory.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#load-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity load() throws ArangoDBException;

	/**
	 * Removes a collection from memory. This call does not delete any documents. You can use the collection afterwards;
	 * in which case it will be loaded into memory, again.
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
	 * Changes the properties of a collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#change-properties-of-a-collection">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return properties of the collection
	 * @throws ArangoDBException
	 */
	CollectionPropertiesEntity changeProperties(final CollectionPropertiesOptions options) throws ArangoDBException;

	/**
	 * Renames a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#rename-collection">API
	 *      Documentation</a>
	 * @param newName
	 *            The new name
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	CollectionEntity rename(final String newName) throws ArangoDBException;

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
	void grantAccess(final String user, final Permissions permissions) throws ArangoDBException;

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
	void revokeAccess(final String user) throws ArangoDBException;

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
	void resetAccess(final String user) throws ArangoDBException;

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
	Permissions getPermissions(final String user) throws ArangoDBException;

}

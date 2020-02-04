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

import com.arangodb.entity.*;
import com.arangodb.model.*;

import java.util.Collection;

/**
 * Interface for operations on ArangoDB collection level.
 *
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection.html">Collection API Documentation</a>
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection.html">Documents API Documentation</a>
 */
@SuppressWarnings("UnusedReturnValue")
public interface ArangoCollection extends ArangoSerializationAccessor {

    /**
     * The the handler of the database the collection is within
     *
     * @return database handler
     */
    ArangoDatabase db();

    /**
     * The name of the collection
     *
     * @return collection name
     */
    String name();

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value A representation of a single document (POJO, VPackSlice or String for JSON)
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value) throws ArangoDBException;

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options) throws ArangoDBException;

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values A List of documents (POJO, VPackSlice or String for JSON)
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(Collection<T> values) throws ArangoDBException;

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
            Collection<T> values, DocumentCreateOptions options) throws ArangoDBException;

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values a list of Objects that will be stored as documents
     * @return information about the import
     * @throws ArangoDBException
     */
    DocumentImportEntity importDocuments(Collection<?> values) throws ArangoDBException;

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  a list of Objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     * @throws ArangoDBException
     */
    DocumentImportEntity importDocuments(Collection<?> values, DocumentImportOptions options) throws ArangoDBException;

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values JSON-encoded array of objects that will be stored as documents
     * @return information about the import
     * @throws ArangoDBException
     */
    DocumentImportEntity importDocuments(String values) throws ArangoDBException;

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  JSON-encoded array of objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     * @throws ArangoDBException
     */
    DocumentImportEntity importDocuments(String values, DocumentImportOptions options) throws ArangoDBException;

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key  The key of the document
     * @param type The type of the document (POJO class, VPackSlice or String for JSON)
     * @return the document identified by the key
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type) throws ArangoDBException;

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return the document identified by the key
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type, DocumentReadOptions options) throws ArangoDBException;

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys The keys of the documents
     * @param type The type of the documents (POJO class, VPackSlice or String for JSON)
     * @return the documents and possible errors
     * @throws ArangoDBException
     */
    <T> MultiDocumentEntity<T> getDocuments(Collection<String> keys, Class<T> type) throws ArangoDBException;

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys    The keys of the documents
     * @param type    The type of the documents (POJO class, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return the documents and possible errors
     * @throws ArangoDBException
     */
    <T> MultiDocumentEntity<T> getDocuments(Collection<String> keys, Class<T> type, DocumentReadOptions options)
            throws ArangoDBException;

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, VPackSlice or String for JSON)
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value) throws ArangoDBException;

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options)
            throws ArangoDBException;

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values A List of documents (POJO, VPackSlice or String for JSON)
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(Collection<T> values) throws ArangoDBException;

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
            Collection<T> values, DocumentReplaceOptions options) throws ArangoDBException;

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, VPackSlice or String for JSON)
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> updateDocument(String key, T value) throws ArangoDBException;

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> updateDocument(String key, T value, DocumentUpdateOptions options)
            throws ArangoDBException;

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values A list of documents (POJO, VPackSlice or String for JSON)
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(Collection<T> values) throws ArangoDBException;

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values  A list of documents (POJO, VPackSlice or String for JSON)
     * @param options Additional options, can be null
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
            Collection<T> values, DocumentUpdateOptions options) throws ArangoDBException;

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key The key of the document
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    DocumentDeleteEntity<Void> deleteDocument(String key) throws ArangoDBException;

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for JSON). Only necessary if
     *                options.returnOld is set to true, otherwise can be null.
     * @param options Additional options, can be null
     * @return information about the document
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    <T> DocumentDeleteEntity<T> deleteDocument(String key, Class<T> type, DocumentDeleteOptions options)
            throws ArangoDBException;

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values The keys of the documents or the documents themselves
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(Collection<?> values) throws ArangoDBException;

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  The keys of the documents or the documents themselves
     * @param type    The type of the documents (POJO class, VPackSlice or String for JSON). Only necessary if
     *                options.returnOld is set to true, otherwise can be null.
     * @param options Additional options, can be null
     * @return information about the documents
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
            Collection<?> values, Class<T> type, DocumentDeleteOptions options) throws ArangoDBException;

    /**
     * Checks if the document exists by reading a single document head
     *
     * @param key The key of the document
     * @return true if the document was found, otherwise false
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document-header">API
     * Documentation</a>
     */
    Boolean documentExists(String key);

    /**
     * Checks if the document exists by reading a single document head
     *
     * @param key     The key of the document
     * @param options Additional options, can be null
     * @return true if the document was found, otherwise false
     * @throws ArangoDBException only thrown when {@link DocumentExistsOptions#isCatchException()} == false
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document-header">API
     * Documentation</a>
     */
    Boolean documentExists(String key, DocumentExistsOptions options) throws ArangoDBException;

    /**
     * Fetches information about the index with the given {@code id} and returns it.
     *
     * @param id The index-handle
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-index">API Documentation</a>
     */
    IndexEntity getIndex(String id) throws ArangoDBException;

    /**
     * Deletes the index with the given {@code id} from the collection.
     *
     * @param id The index-handle
     * @return the id of the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#delete-index">API Documentation</a>
     */
    String deleteIndex(String id) throws ArangoDBException;

    /**
     * Creates a hash index for the collection if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-hash.html#create-hash-index">API Documentation</a>
     */
    IndexEntity ensureHashIndex(Iterable<String> fields, HashIndexOptions options) throws ArangoDBException;

    /**
     * Creates a skip-list index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-skiplist.html#create-skip-list">API
     * Documentation</a>
     */
    IndexEntity ensureSkiplistIndex(Iterable<String> fields, SkiplistIndexOptions options) throws ArangoDBException;

    /**
     * Creates a persistent index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-persistent.html#create-a-persistent-index">API
     * Documentation</a>
     */
    IndexEntity ensurePersistentIndex(Iterable<String> fields, PersistentIndexOptions options) throws ArangoDBException;

    /**
     * Creates a geo-spatial index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-geo.html#create-geo-spatial-index">API
     * Documentation</a>
     */
    IndexEntity ensureGeoIndex(Iterable<String> fields, GeoIndexOptions options) throws ArangoDBException;

    /**
     * Creates a fulltext index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-fulltext.html#create-fulltext-index">API
     * Documentation</a>
     */
    IndexEntity ensureFulltextIndex(Iterable<String> fields, FulltextIndexOptions options) throws ArangoDBException;

    /**
     * Creates a ttl index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-ttl.html">API
     * Documentation</a>
     */
    IndexEntity ensureTtlIndex(Iterable<String> fields, TtlIndexOptions options) throws ArangoDBException;

    /**
     * Fetches a list of all indexes on this collection.
     *
     * @return information about the indexes
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-all-indexes-of-a-collection">API
     * Documentation</a>
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
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     */
    CollectionEntity truncate() throws ArangoDBException;

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @param options
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionEntity truncate(CollectionTruncateOptions options) throws ArangoDBException;

    /**
     * Counts the documents in a collection
     *
     * @return information about the collection, including the number of documents
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity count() throws ArangoDBException;

    /**
     * Counts the documents in a collection
     *
     * @param options
     * @return information about the collection, including the number of documents
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionPropertiesEntity count(CollectionCountOptions options) throws ArangoDBException;

    /**
     * Creates a collection for this collection's name, then returns collection information from the server.
     *
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity create() throws ArangoDBException;

    /**
     * Creates a collection with the given {@code options} for this collection's name, then returns collection
     * information from the server.
     *
     * @param options Additional options, can be null
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity create(CollectionCreateOptions options) throws ArangoDBException;

    /**
     * Deletes the collection from the database.
     *
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     */
    void drop() throws ArangoDBException;

    /**
     * Deletes the collection from the database.
     *
     * @param isSystem Whether or not the collection to drop is a system collection. This parameter must be set to true in
     *                 order to drop a system collection.
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     * @since ArangoDB 3.1.0
     */
    void drop(boolean isSystem) throws ArangoDBException;

    /**
     * Tells the server to load the collection into memory.
     *
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#load-collection">API
     * Documentation</a>
     */
    CollectionEntity load() throws ArangoDBException;

    /**
     * Tells the server to remove the collection from memory. This call does not delete any documents. You can use the
     * collection afterwards; in which case it will be loaded into memory, again.
     *
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#unload-collection">API
     * Documentation</a>
     */
    CollectionEntity unload() throws ArangoDBException;

    /**
     * Returns information about the collection
     *
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-information-about-a-collection">API
     * Documentation</a>
     */
    CollectionEntity getInfo() throws ArangoDBException;

    /**
     * Reads the properties of the specified collection
     *
     * @return properties of the collection
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#read-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity getProperties() throws ArangoDBException;

    /**
     * Changes the properties of the collection
     *
     * @param options Additional options, can be null
     * @return properties of the collection
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-modifying.html#change-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity changeProperties(CollectionPropertiesOptions options) throws ArangoDBException;

    /**
     * Renames the collection
     *
     * @param newName The new name
     * @return information about the collection
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#rename-collection">API
     * Documentation</a>
     */
    CollectionEntity rename(String newName) throws ArangoDBException;

    /**
     * Returns the responsible shard for the document.
     * Please note that this API is only meaningful and available on a cluster coordinator.
     *
     * @param value A projection of the document containing at least the shard key (_key or a custom attribute) for
     *              which the responsible shard should be determined
     * @return information about the responsible shard
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-responsible-shard-for-a-document">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    ShardEntity getResponsibleShard(final Object value);

    /**
     * Retrieve the collections revision
     *
     * @return information about the collection, including the collections revision
     * @throws ArangoDBException
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-collection-revision-id">API
     * Documentation</a>
     */
    CollectionRevisionEntity getRevision() throws ArangoDBException;

    /**
     * Grants or revoke access to the collection for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    void grantAccess(String user, Permissions permissions) throws ArangoDBException;

    /**
     * Revokes access to the collection for user user. You need permission to the _system database in order to execute
     * this call.
     *
     * @param user The name of the user
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    void revokeAccess(String user) throws ArangoDBException;

    /**
     * Clear the collection access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @throws ArangoDBException
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void resetAccess(String user) throws ArangoDBException;

    /**
     * Get the collection access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @throws ArangoDBException
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#get-the-specific-collection-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user) throws ArangoDBException;

}

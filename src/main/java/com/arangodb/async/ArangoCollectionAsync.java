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

package com.arangodb.async;

import com.arangodb.ArangoDBException;
import com.arangodb.ArangoSerializationAccessor;
import com.arangodb.entity.*;
import com.arangodb.model.*;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for operations on ArangoDB collection level.
 *
 * @author Mark Vollmary
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection.html">Collection API Documentation</a>
 * @see <a href="https://www.arangodb.com/docs/stable/http/collection.html">Documents API Documentation</a>
 */
@SuppressWarnings("unused")
public interface ArangoCollectionAsync extends ArangoSerializationAccessor {

    /**
     * The the handler of the database the collection is within
     *
     * @return database handler
     */
    ArangoDatabaseAsync db();

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
     * @param value A representation of a single document (POJO, VPackSlice or String for Json)
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(final T value);

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(final T value, final DocumentCreateOptions options);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values A List of documents (POJO, VPackSlice or String for Json)
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(final Collection<T> values);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(
            final Collection<T> values,
            final DocumentCreateOptions options);

    /**
     * Imports documents
     *
     * @param values a list of Objects that will be stored as documents
     * @return information about the import
     */
    CompletableFuture<DocumentImportEntity> importDocuments(final Collection<?> values);

    /**
     * Imports documents
     *
     * @param values  a list of Objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     */
    CompletableFuture<DocumentImportEntity> importDocuments(
            final Collection<?> values,
            final DocumentImportOptions options);

    /**
     * Imports documents
     *
     * @param values JSON-encoded array of objects that will be stored as documents
     * @return information about the import
     */
    CompletableFuture<DocumentImportEntity> importDocuments(final String values);

    /**
     * Imports documents
     *
     * @param values  JSON-encoded array of objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     */
    CompletableFuture<DocumentImportEntity> importDocuments(final String values, final DocumentImportOptions options);

    /**
     * Reads a single document
     *
     * @param key  The key of the document
     * @param type The type of the document (POJO class, VPackSlice or String for Json)
     * @return the document identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<T> getDocument(final String key, final Class<T> type) throws ArangoDBException;

    /**
     * Reads a single document
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return the document identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<T> getDocument(final String key, final Class<T> type, final DocumentReadOptions options)
            throws ArangoDBException;

    /**
     * Reads multiple documents
     *
     * @param keys The keys of the documents
     * @param type The type of the documents (POJO class, VPackSlice or String for Json)
     * @return the documents and possible errors
     */
    <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(final Collection<String> keys, final Class<T> type);

    /**
     * Reads multiple documents
     *
     * @param keys    The keys of the documents
     * @param type    The type of the documents (POJO class, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return the documents and possible errors
     */
    <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(
            final Collection<String> keys,
            final Class<T> type,
            DocumentReadOptions options);

    /**
     * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
     * violated
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, VPackSlice or String for Json)
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(final String key, final T value);

    /**
     * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
     * violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(
            final String key,
            final T value,
            final DocumentReplaceOptions options);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values A List of documents (POJO, VPackSlice or String for Json)
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(final Collection<T> values);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(
            final Collection<T> values,
            final DocumentReplaceOptions options);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, VPackSlice or String for Json)
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(final String key, final T value);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(
            final String key,
            final T value,
            final DocumentUpdateOptions options);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values A list of documents (POJO, VPackSlice or String for Json)
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(final Collection<T> values);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values  A list of documents (POJO, VPackSlice or String for Json)
     * @param options Additional options, can be null
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(
            final Collection<T> values,
            final DocumentUpdateOptions options);

    /**
     * Removes a document
     *
     * @param key The key of the document
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    CompletableFuture<DocumentDeleteEntity<Void>> deleteDocument(final String key);

    /**
     * Removes a document
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO class, VPackSlice or String for Json). Only necessary if
     *                options.returnOld is set to true, otherwise can be null.
     * @param options Additional options, can be null
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    <T> CompletableFuture<DocumentDeleteEntity<T>> deleteDocument(
            final String key,
            final Class<T> type,
            final DocumentDeleteOptions options);

    /**
     * Removes multiple document
     *
     * @param values The keys of the documents or the documents themselves
     * @return information about the documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(final Collection<?> values);

    /**
     * Removes multiple document
     *
     * @param values  The keys of the documents or the documents themselves
     * @param type    The type of the documents (POJO class, VPackSlice or String for Json). Only necessary if
     *                options.returnOld is set to true, otherwise can be null.
     * @param options Additional options, can be null
     * @return information about the documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocuments(
            final Collection<?> values,
            final Class<T> type,
            final DocumentDeleteOptions options);

    /**
     * Checks if the document exists by reading a single document head
     *
     * @param key The key of the document
     * @return true if the document was found, otherwise false
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document-header">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> documentExists(final String key);

    /**
     * Checks if the document exists by reading a single document head
     *
     * @param key     The key of the document
     * @param options Additional options, can be null
     * @return true if the document was found, otherwise false
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document-header">API
     * Documentation</a>
     */
    CompletableFuture<Boolean> documentExists(final String key, final DocumentExistsOptions options);

    /**
     * Returns an index
     *
     * @param id The index-handle
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-index">API Documentation</a>
     */
    CompletableFuture<IndexEntity> getIndex(final String id);

    /**
     * Deletes an index
     *
     * @param id The index-handle
     * @return the id of the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#delete-index">API Documentation</a>
     */
    CompletableFuture<String> deleteIndex(final String id);

    /**
     * Creates a hash index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-hash.html#create-hash-index">API Documentation</a>
     */
    CompletableFuture<IndexEntity> ensureHashIndex(final Iterable<String> fields, final HashIndexOptions options);

    /**
     * Creates a skip-list index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-skiplist.html#create-skip-list">API
     * Documentation</a>
     */
    CompletableFuture<IndexEntity> ensureSkiplistIndex(
            final Iterable<String> fields,
            final SkiplistIndexOptions options);

    /**
     * Creates a persistent index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-persistent.html#create-a-persistent-index">API
     * Documentation</a>
     */
    CompletableFuture<IndexEntity> ensurePersistentIndex(
            final Iterable<String> fields,
            final PersistentIndexOptions options);

    /**
     * Creates a geo-spatial index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-geo.html#create-geo-spatial-index">API
     * Documentation</a>
     */
    CompletableFuture<IndexEntity> ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options);

    /**
     * Creates a fulltext index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-fulltext.html#create-fulltext-index">API
     * Documentation</a>
     */
    CompletableFuture<IndexEntity> ensureFulltextIndex(
            final Iterable<String> fields,
            final FulltextIndexOptions options);

    /**
     * Creates a ttl index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-ttl.html">API
     * Documentation</a>
     */
    CompletableFuture<IndexEntity> ensureTtlIndex(Iterable<String> fields, TtlIndexOptions options);

    /**
     * Returns all indexes of the collection
     *
     * @return information about the indexes
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-all-indexes-of-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<Collection<IndexEntity>> getIndexes();

    /**
     * Checks whether the collection exists
     *
     * @return true if the collection exists, otherwise false
     */
    CompletableFuture<Boolean> exists();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> truncate();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> truncate(CollectionTruncateOptions options);

    /**
     * Counts the documents in a collection
     *
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionPropertiesEntity> count();

    /**
     * Counts the documents in a collection
     *
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionPropertiesEntity> count(CollectionCountOptions options);

    /**
     * Creates the collection
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> create();

    /**
     * Creates the collection
     *
     * @param options Additional options, can be null
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> create(final CollectionCreateOptions options);

    /**
     * Drops the collection
     *
     * @return void
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     */
    CompletableFuture<Void> drop();

    /**
     * Drops the collection
     *
     * @param isSystem Whether or not the collection to drop is a system collection. This parameter must be set to true in
     *                 order to drop a system collection.
     * @return void
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     */
    CompletableFuture<Void> drop(final boolean isSystem);

    /**
     * Loads a collection into memory.
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#load-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> load();

    /**
     * Removes a collection from memory. This call does not delete any documents. You can use the collection afterwards;
     * in which case it will be loaded into memory, again.
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#unload-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> unload();

    /**
     * Returns information about the collection
     *
     * @return information about the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-information-about-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> getInfo();

    /**
     * Reads the properties of the specified collection
     *
     * @return properties of the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#read-properties-of-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionPropertiesEntity> getProperties();

    /**
     * Changes the properties of a collection
     *
     * @param options Additional options, can be null
     * @return properties of the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-modifying.html#change-properties-of-a-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionPropertiesEntity> changeProperties(final CollectionPropertiesOptions options);

    /**
     * Renames a collection
     *
     * @param newName The new name
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#rename-collection">API
     * Documentation</a>
     */
    CompletableFuture<CollectionEntity> rename(final String newName);

    /**
     * Returns the responsible shard for the document.
     * Please note that this API is only meaningful and available on a cluster coordinator.
     *
     * @param value A projection of the document containing at least the shard key (_key or a custom attribute) for
     *              which the responsible shard should be determined
     * @return information about the responsible shard
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-responsible-shard-for-a-document">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CompletableFuture<ShardEntity> getResponsibleShard(final Object value);

    /**
     * Retrieve the collections revision
     *
     * @return information about the collection, including the collections revision
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-collection-revision-id">API
     * Documentation</a>
     */
    CompletableFuture<CollectionRevisionEntity> getRevision();

    /**
     * Grants or revoke access to the collection for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @return void
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    CompletableFuture<Void> grantAccess(final String user, final Permissions permissions);

    /**
     * Revokes access to the collection for user user. You need permission to the _system database in order to execute
     * this call.
     *
     * @param user The name of the user
     * @return void
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    CompletableFuture<Void> revokeAccess(final String user);

    /**
     * Clear the collection access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @return void
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Void> resetAccess(final String user);

    /**
     * Get the collection access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#get-the-specific-collection-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    CompletableFuture<Permissions> getPermissions(final String user);
}

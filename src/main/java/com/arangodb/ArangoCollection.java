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
public interface ArangoCollection extends ArangoSerdeAccessor {

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
     * @param value A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    DocumentCreateEntity<Void> insertDocument(Object value);

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options);

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options, Class<T> type);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values A List of documents (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(Collection<?> values);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
            Collection<T> values, DocumentCreateOptions options);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the documents
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#create-document">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
            Collection<T> values, DocumentCreateOptions options, Class<T> type);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values a list of Objects that will be stored as documents
     * @return information about the import
     */
    DocumentImportEntity importDocuments(Collection<?> values);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  a list of Objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     */
    DocumentImportEntity importDocuments(Collection<?> values, DocumentImportOptions options);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values JSON-encoded array of objects that will be stored as documents
     * @return information about the import
     */
    DocumentImportEntity importDocuments(String values);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  JSON-encoded array of objects that will be stored as documents
     * @param options Additional options, can be null
     * @return information about the import
     */
    DocumentImportEntity importDocuments(String values, DocumentImportOptions options);

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key  The key of the document
     * @param type The type of the document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @return the document identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type);

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return the document identified by the key
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type, DocumentReadOptions options);

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys The keys of the documents
     * @param type The type of the documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @return the documents and possible errors
     */
    <T> MultiDocumentEntity<T> getDocuments(Collection<String> keys, Class<T> type);

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys    The keys of the documents
     * @param type    The type of the documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options, can be null
     * @return the documents and possible errors
     */
    <T> MultiDocumentEntity<T> getDocuments(Collection<String> keys, Class<T> type, DocumentReadOptions options);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    DocumentUpdateEntity<Void> replaceDocument(String key, Object value);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options, Class<T> type);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values A List of documents (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(Collection<?> values);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
            Collection<T> values, DocumentReplaceOptions options);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#replace-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
            Collection<T> values, DocumentReplaceOptions options, Class<T> type);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    DocumentUpdateEntity<Void> updateDocument(String key, Object value);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> updateDocument(String key, T value, DocumentUpdateOptions options);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key        The key of the document
     * @param value      A representation of a single document (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options    Additional options
     * @param returnType Type of the returned newDocument and/or oldDocument
     * @return information about the document
     * @see <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> updateDocument(String key, Object value, DocumentUpdateOptions options,
                                               Class<T> returnType);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values A list of documents (POJO, {@link com.arangodb.util.RawJson} or {@link com.arangodb.util.RawBytes})
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(Collection<?> values);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values  A list of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options Additional options
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
            Collection<T> values, DocumentUpdateOptions options);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values     A list of documents (POJO, {@link com.arangodb.util.RawJson} or
     * {@link com.arangodb.util.RawBytes})
     * @param options    Additional options
     * @param returnType Type of the returned newDocument and/or oldDocument
     * @return information about the documents
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#update-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
            Collection<?> values, DocumentUpdateOptions options, Class<T> returnType);

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key The key of the document
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    DocumentDeleteEntity<Void> deleteDocument(String key);

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param options Additional options
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    DocumentDeleteEntity<Void> deleteDocument(String key, DocumentDeleteOptions options);

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param type    Deserialization target type for the returned documents.
     * @param options Additional options
     * @return information about the document
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-a-document">API
     * Documentation</a>
     */
    <T> DocumentDeleteEntity<T> deleteDocument(String key, DocumentDeleteOptions options, Class<T> type);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values The keys of the documents or the documents themselves
     * @return information about the documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(Collection<?> values);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  The keys of the documents or the documents themselves
     * @param options Additional options
     * @return information about the documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
            Collection<?> values, DocumentDeleteOptions options);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  The keys of the documents or the documents themselves
     * @param type    Deserialization target type for the returned documents.
     * @param options Additional options
     * @return information about the documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#removes-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
            Collection<?> values, DocumentDeleteOptions options, Class<T> type);

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
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/document-working-with-documents.html#read-document-header">API
     * Documentation</a>
     */
    Boolean documentExists(String key, DocumentExistsOptions options);

    /**
     * Fetches information about the index with the given {@code id} and returns it.
     *
     * @param id The index-handle
     * @return information about the index
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-index">API Documentation</a>
     */
    IndexEntity getIndex(String id);

    /**
     * Deletes the index with the given {@code id} from the collection.
     *
     * @param id The index-handle
     * @return the id of the index
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/indexes-working-with.html#delete-index">API Documentation</a>
     */
    String deleteIndex(String id);

    /**
     * Creates a hash index for the collection if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/indexes-hash.html#create-hash-index">API Documentation</a>
     * @deprecated use {@link #ensurePersistentIndex(Iterable, PersistentIndexOptions)} instead. Since ArangoDB 3.7 a
     * hash index is an alias for a persistent index.
     */
    @Deprecated
    IndexEntity ensureHashIndex(Iterable<String> fields, HashIndexOptions options);

    /**
     * Creates a skip-list index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-skiplist.html#create-skip-list">API
     * Documentation</a>
     * @deprecated use {@link #ensurePersistentIndex(Iterable, PersistentIndexOptions)} instead. Since ArangoDB 3.7 a
     * skiplist index is an alias for a persistent index.
     */
    @Deprecated
    IndexEntity ensureSkiplistIndex(Iterable<String> fields, SkiplistIndexOptions options);

    /**
     * Creates a persistent index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-persistent.html#create-a-persistent-index">API
     * Documentation</a>
     */
    IndexEntity ensurePersistentIndex(Iterable<String> fields, PersistentIndexOptions options);

    /**
     * Creates a geo-spatial index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-geo.html#create-geo-spatial-index">API
     * Documentation</a>
     */
    IndexEntity ensureGeoIndex(Iterable<String> fields, GeoIndexOptions options);

    /**
     * Creates a fulltext index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-fulltext.html#create-fulltext-index">API
     * Documentation</a>
     */
    IndexEntity ensureFulltextIndex(Iterable<String> fields, FulltextIndexOptions options);

    /**
     * Creates a ttl index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-ttl.html">API
     * Documentation</a>
     */
    IndexEntity ensureTtlIndex(Iterable<String> fields, TtlIndexOptions options);

    /**
     * Creates a ZKD multi-dimensional index for the collection, if it does not already exist.
     * Note that zkd indexes are an experimental feature in ArangoDB 3.9.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://www.arangodb.com/docs/stable/http/indexes-multi-dim.html">API Documentation</a>
     * @since ArangoDB 3.9
     */
    IndexEntity ensureZKDIndex(Iterable<String> fields, ZKDIndexOptions options);

    /**
     * Fetches a list of all indexes on this collection.
     *
     * @return information about the indexes
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/indexes-working-with.html#read-all-indexes-of-a-collection">API
     * Documentation</a>
     */
    Collection<IndexEntity> getIndexes();

    /**
     * Checks whether the collection exists
     *
     * @return true if the collection exists, otherwise false
     */
    boolean exists();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     */
    CollectionEntity truncate();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @param options
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#truncate-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionEntity truncate(CollectionTruncateOptions options);

    /**
     * Counts the documents in a collection
     *
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting
     * .html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity count();

    /**
     * Counts the documents in a collection
     *
     * @param options
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting
     * .html#return-number-of-documents-in-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionPropertiesEntity count(CollectionCountOptions options);

    /**
     * Creates a collection for this collection's name, then returns collection information from the server.
     *
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity create();

    /**
     * Creates a collection with the given {@code options} for this collection's name, then returns collection
     * information from the server.
     *
     * @param options Additional options, can be null
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#create-collection">API
     * Documentation</a>
     */
    CollectionEntity create(CollectionCreateOptions options);

    /**
     * Deletes the collection from the database.
     *
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     */
    void drop();

    /**
     * Deletes the collection from the database.
     *
     * @param isSystem Whether or not the collection to drop is a system collection. This parameter must be set to
     *                 true in
     *                 order to drop a system collection.
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-creating.html#drops-collection">API
     * Documentation</a>
     * @since ArangoDB 3.1.0
     */
    void drop(boolean isSystem);

    /**
     * Returns information about the collection
     *
     * @return information about the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#return-information-about-a-collection">API
     * Documentation</a>
     */
    CollectionEntity getInfo();

    /**
     * Reads the properties of the specified collection
     *
     * @return properties of the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-getting.html#read-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity getProperties();

    /**
     * Changes the properties of the collection
     *
     * @param options Additional options, can be null
     * @return properties of the collection
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/collection-modifying.html#change-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity changeProperties(CollectionPropertiesOptions options);

    /**
     * Renames the collection
     *
     * @param newName The new name
     * @return information about the collection
     * @see <a href="https://www.arangodb.com/docs/stable/http/collection-modifying.html#rename-collection">API
     * Documentation</a>
     */
    CollectionEntity rename(String newName);

    /**
     * Returns the responsible shard for the document.
     * Please note that this API is only meaningful and available on a cluster coordinator.
     *
     * @param value A projection of the document containing at least the shard key (_key or a custom attribute) for
     *              which the responsible shard should be determined
     * @return information about the responsible shard
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-responsible-shard-for-a-document">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    ShardEntity getResponsibleShard(final Object value);

    /**
     * Retrieve the collections revision
     *
     * @return information about the collection, including the collections revision
     * @see
     * <a href="https://www.arangodb.com/docs/stable/http/collection-getting.html#return-collection-revision-id">API
     * Documentation</a>
     */
    CollectionRevisionEntity getRevision();

    /**
     * Grants or revoke access to the collection for user user. You need permission to the _system database in order to
     * execute this call.
     *
     * @param user        The name of the user
     * @param permissions The permissions the user grant
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    void grantAccess(String user, Permissions permissions);

    /**
     * Revokes access to the collection for user user. You need permission to the _system database in order to execute
     * this call.
     *
     * @param user The name of the user
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     */
    void revokeAccess(String user);

    /**
     * Clear the collection access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @see <a href=
     * "https://www.arangodb.com/docs/stable/http/user-management.html#set-the-collection-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void resetAccess(String user);

    /**
     * Get the collection access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see
     * <a href= "https://www.arangodb.com/docs/stable/http/user-management.html#get-the-specific-collection-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user);

}

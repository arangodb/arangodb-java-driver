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
import com.arangodb.util.RawData;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;

/**
 * Interface for operations on ArangoDB collection level.
 *
 * @author Mark Vollmary
 * @author Heiko Kernbach
 * @author Michele Rastelli
 * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/">Collection API Documentation</a>
 */
@ThreadSafe
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
     * @param value A representation of a single document (POJO or {@link com.arangodb.util.RawData}
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-a-document">API
     * Documentation</a>
     */
    DocumentCreateEntity<Void> insertDocument(Object value);

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-a-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options);

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param value   A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-a-document">API
     * Documentation</a>
     */
    <T> DocumentCreateEntity<T> insertDocument(T value, DocumentCreateOptions options, Class<T> type);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values Raw data representing a collection of documents
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(RawData values);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  Raw data representing a collection of documents
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentCreateEntity<RawData>> insertDocuments(
            RawData values, DocumentCreateOptions options);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values A List of documents
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(Iterable<?> values);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(
            Iterable<?> values, DocumentCreateOptions options);

    /**
     * Creates new documents from the given documents, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     *
     * @param values  A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#create-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
            Iterable<? extends T> values, DocumentCreateOptions options, Class<T> type);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @return information about the import
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/import/#import-json-data-as-documents">API
     * Documentation</a>
     */
    DocumentImportEntity importDocuments(Iterable<?> values);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return information about the import
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/import/#import-json-data-as-documents">API
     * Documentation</a>
     */
    DocumentImportEntity importDocuments(Iterable<?> values, DocumentImportOptions options);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values Raw data representing a collection of documents
     * @return information about the import
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/import/#import-json-data-as-documents">API
     * Documentation</a>
     */
    DocumentImportEntity importDocuments(RawData values);

    /**
     * Bulk imports the given values into the collection.
     *
     * @param values  Raw data representing a collection of documents
     * @param options Additional options, can be null
     * @return information about the import
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/import/#import-json-data-as-documents">API
     * Documentation</a>
     */
    DocumentImportEntity importDocuments(RawData values, DocumentImportOptions options);

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key  The key of the document
     * @param type The type of the document (POJO or {@link com.arangodb.util.RawData})
     * @return the document identified by the key
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-a-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type);

    /**
     * Retrieves the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param type    The type of the document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return the document identified by the key
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-a-document">API
     * Documentation</a>
     */
    <T> T getDocument(String key, Class<T> type, DocumentReadOptions options);

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys The keys of the documents
     * @param type The type of the documents (POJO or {@link com.arangodb.util.RawData})
     * @return the documents and possible errors
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<T> getDocuments(Iterable<String> keys, Class<T> type);

    /**
     * Retrieves multiple documents with the given {@code _key} from the collection.
     *
     * @param keys    The keys of the documents
     * @param type    The type of the documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options, can be null
     * @return the documents and possible errors
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<T> getDocuments(Iterable<String> keys, Class<T> type, DocumentReadOptions options);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-a-document">API
     * Documentation</a>
     */
    DocumentUpdateEntity<Void> replaceDocument(String key, Object value);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-a-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options);

    /**
     * Replaces the document with {@code key} with the one in the body, provided there is such a document and no
     * precondition is violated
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-a-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options, Class<T> type);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values Raw data representing a collection of documents
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(RawData values);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  Raw data representing a collection of documents
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<RawData>> replaceDocuments(
            RawData values, DocumentReplaceOptions options);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(Iterable<?> values);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(
            Iterable<?> values, DocumentReplaceOptions options);

    /**
     * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
     * specified by the _key attributes in the documents in values.
     *
     * @param values  A List of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @param type    Deserialization target type for the returned documents.
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#replace-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
            Iterable<? extends T> values, DocumentReplaceOptions options, Class<T> type);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the document
     * @param value A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-a-document">API
     * Documentation</a>
     */
    DocumentUpdateEntity<Void> updateDocument(String key, Object value);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key     The key of the document
     * @param value   A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-a-document">API
     * Documentation</a>
     */
    <T> DocumentUpdateEntity<T> updateDocument(String key, T value, DocumentUpdateOptions options);

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key        The key of the document
     * @param value      A representation of a single document (POJO or {@link com.arangodb.util.RawData})
     * @param options    Additional options
     * @param returnType Type of the returned newDocument and/or oldDocument
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-a-document">API
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
     * @param values Raw data representing a collection of documents
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(RawData values);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values  Raw data representing a collection of documents
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateDocuments(
            RawData values, DocumentUpdateOptions options);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values A list of documents (POJO or {@link com.arangodb.util.RawData})
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(Iterable<?> values);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values  A list of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options Additional options
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(
            Iterable<?> values, DocumentUpdateOptions options);

    /**
     * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
     * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
     * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
     * overwritten in the existing documents if they do exist there.
     *
     * @param values     A list of documents (POJO or {@link com.arangodb.util.RawData})
     * @param options    Additional options
     * @param returnType Type of the returned newDocument and/or oldDocument
     * @return information about the documents
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#update-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
            Iterable<?> values, DocumentUpdateOptions options, Class<T> returnType);

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key The key of the document
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-a-document">API
     * Documentation</a>
     */
    DocumentDeleteEntity<Void> deleteDocument(String key);

    /**
     * Deletes the document with the given {@code key} from the collection.
     *
     * @param key     The key of the document
     * @param options Additional options
     * @return information about the document
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-a-document">API
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
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-a-document">API
     * Documentation</a>
     */
    <T> DocumentDeleteEntity<T> deleteDocument(String key, DocumentDeleteOptions options, Class<T> type);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values Raw data representing the keys of the documents or the documents themselves
     * @return information about the documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(RawData values);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  Raw data representing the keys of the documents or the documents themselves
     * @param options Additional options
     * @return information about the documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<RawData>> deleteDocuments(
            RawData values, DocumentDeleteOptions options);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values The keys of the documents or the documents themselves
     * @return information about the documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(Iterable<?> values);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  The keys of the documents or the documents themselves
     * @param options Additional options
     * @return information about the documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-multiple-documents">API
     * Documentation</a>
     */
    MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(
            Iterable<?> values, DocumentDeleteOptions options);

    /**
     * Deletes multiple documents from the collection.
     *
     * @param values  The keys of the documents or the documents themselves
     * @param type    Deserialization target type for the returned documents.
     * @param options Additional options
     * @return information about the documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#remove-multiple-documents">API
     * Documentation</a>
     */
    <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
            Iterable<?> values, DocumentDeleteOptions options, Class<T> type);

    /**
     * Checks if the document exists by reading a single document head
     *
     * @param key The key of the document
     * @return true if the document was found, otherwise false
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-a-document-header">API
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
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/documents/#get-a-document-header">API
     * Documentation</a>
     */
    Boolean documentExists(String key, DocumentExistsOptions options);

    /**
     * Fetches information about the index with the given {@code id} and returns it.
     * <br/>
     * <b>Note:</b> inverted indexes are not returned by this method. Use
     * {@link ArangoCollection#getInvertedIndex(String)} instead.
     *
     * @param id The index-handle
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#get-an-index">API Documentation</a>
     */
    IndexEntity getIndex(String id);

    /**
     * Fetches information about the inverted index with the given {@code id} and returns it.
     *
     * @param id The index-handle
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#get-an-index">API Documentation</a>
     * @since ArangoDB 3.10
     */
    InvertedIndexEntity getInvertedIndex(String id);

    /**
     * Deletes the index with the given {@code id} from the collection.
     *
     * @param id The index-handle
     * @return the id of the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#delete-an-index">API Documentation</a>
     */
    String deleteIndex(String id);

    /**
     * Creates a persistent index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/persistent/#create-a-persistent-index">API
     * Documentation</a>
     */
    IndexEntity ensurePersistentIndex(Iterable<String> fields, PersistentIndexOptions options);

    /**
     * Creates a geo-spatial index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/geo-spatial/#create-a-geo-spatial-index">API
     * Documentation</a>
     */
    IndexEntity ensureGeoIndex(Iterable<String> fields, GeoIndexOptions options);

    /**
     * Creates a fulltext index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/fulltext/#create-a-full-text-index">API
     * Documentation</a>
     * @deprecated since ArangoDB 3.10, use ArangoSearch or Inverted indexes instead.
     */
    @Deprecated
    IndexEntity ensureFulltextIndex(Iterable<String> fields, FulltextIndexOptions options);

    /**
     * Creates a ttl index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute paths
     * @param options Additional options, can be null
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/ttl/#create-a-ttl-index">API
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
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/multi-dimensional/#create-a-multi-dimensional-index">API Documentation</a>
     * @since ArangoDB 3.9
     * @deprecated since ArangoDB 3.12, use {@link #ensureMDIndex(Iterable, MDIndexOptions)} or
     * {@link #ensureMDPrefixedIndex(Iterable, MDPrefixedIndexOptions)} instead.
     */
    @Deprecated
    IndexEntity ensureZKDIndex(Iterable<String> fields, ZKDIndexOptions options);

    /**
     * Creates a multi-dimensional index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute names used for each dimension
     * @param options Additional options, can be null.
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/multi-dimensional/#create-a-multi-dimensional-index">API Documentation</a>
     * @since ArangoDB 3.12
     */
    IndexEntity ensureMDIndex(Iterable<String> fields, MDIndexOptions options);

    /**
     * Creates a multi-dimensional prefixed index for the collection, if it does not already exist.
     *
     * @param fields  A list of attribute names used for each dimension
     * @param options Additional options, cannot be null.
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/multi-dimensional/#create-a-multi-dimensional-index">API Documentation</a>
     * @since ArangoDB 3.12
     */
    IndexEntity ensureMDPrefixedIndex(Iterable<String> fields, MDPrefixedIndexOptions options);

    /**
     * Creates an inverted index for the collection, if it does not already exist.
     *
     * @param options index creation options
     * @return information about the index
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/inverted/#create-an-inverted-index">API Documentation</a>
     * @since ArangoDB 3.10
     */
    InvertedIndexEntity ensureInvertedIndex(InvertedIndexOptions options);

    /**
     * Creates a vector index for the collection, if it does not already exist.
     *
     * @param fields  A list with exactly one attribute path to specify where the vector embedding is stored in each
     *                document. The vector data needs to be populated before creating the index.
     *                If you want to index another vector embedding attribute, you need to create a separate vector
     *                index.
     * @param options index creation options
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/vector/#create-a-vector-index">API Documentation</a>
     * @return information about the index
     */
    IndexEntity ensureVectorIndex(Iterable<String> fields, VectorIndexOptions options);

    /**
     * Fetches a list of all indexes on this collection.
     * <br/>
     * <b>Note:</b> inverted indexes are not returned by this method. Use
     * {@link ArangoCollection#getInvertedIndexes()} instead.
     *
     * @return information about the indexes
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#list-all-indexes-of-a-collection">API
     * Documentation</a>
     */
    Collection<IndexEntity> getIndexes();

    /**
     * Fetches a list of all inverted indexes on this collection.
     *
     * @return information about the indexes
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/indexes/#list-all-indexes-of-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.10
     */
    Collection<InvertedIndexEntity> getInvertedIndexes();

    /**
     * Checks whether the collection exists
     *
     * @return true if the collection exists, otherwise false
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-collection-information">API
     * Documentation</a>
     */
    boolean exists();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#truncate-a-collection">API
     * Documentation</a>
     */
    CollectionEntity truncate();

    /**
     * Removes all documents from the collection, but leaves the indexes intact
     *
     * @param options
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#truncate-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionEntity truncate(CollectionTruncateOptions options);

    /**
     * Counts the documents in a collection
     *
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-document-count-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity count();

    /**
     * Counts the documents in a collection
     *
     * @param options
     * @return information about the collection, including the number of documents
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-document-count-of-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.5.0
     */
    CollectionPropertiesEntity count(CollectionCountOptions options);

    /**
     * Creates a collection for this collection's name, then returns collection information from the server.
     *
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#create-a-collection">API
     * Documentation</a>
     */
    CollectionEntity create();

    /**
     * Creates a collection with the given {@code options} for this collection's name, then returns collection
     * information from the server.
     *
     * @param options Additional options, can be null
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#create-a-collection">API
     * Documentation</a>
     */
    CollectionEntity create(CollectionCreateOptions options);

    /**
     * Deletes the collection from the database.
     *
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#drop-a-collection">API
     * Documentation</a>
     */
    void drop();

    /**
     * Deletes the collection from the database.
     *
     * @param isSystem Whether or not the collection to drop is a system collection. This parameter must be set to
     *                 true in
     *                 order to drop a system collection.
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#drop-a-collection">API
     * Documentation</a>
     * @since ArangoDB 3.1.0
     */
    void drop(boolean isSystem);

    /**
     * Returns information about the collection
     *
     * @return information about the collection
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-collection-information">API
     * Documentation</a>
     */
    CollectionEntity getInfo();

    /**
     * Reads the properties of the specified collection
     *
     * @return properties of the collection
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity getProperties();

    /**
     * Changes the properties of the collection
     *
     * @param options Additional options, can be null
     * @return properties of the collection
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#change-the-properties-of-a-collection">API
     * Documentation</a>
     */
    CollectionPropertiesEntity changeProperties(CollectionPropertiesOptions options);

    /**
     * Renames the collection
     *
     * @param newName The new name
     * @return information about the collection
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#rename-a-collection">API
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
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-responsible-shard-for-a-document">
     * API Documentation</a>
     * @since ArangoDB 3.5.0
     */
    ShardEntity getResponsibleShard(final Object value);

    /**
     * Retrieve the collections revision
     *
     * @return information about the collection, including the collections revision
     * @see <a href="https://docs.arango.ai/arangodb/stable/develop/http-api/collections/#get-the-collection-revision-id">API
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
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-collection-access-level"> API
     * Documentation</a>
     */
    void grantAccess(String user, Permissions permissions);

    /**
     * Revokes access to the collection for user user. You need permission to the _system database in order to execute
     * this call.
     *
     * @param user The name of the user
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#set-a-users-collection-access-level"> API
     * Documentation</a>
     */
    void revokeAccess(String user);

    /**
     * Clear the collection access level, revert back to the default access level.
     *
     * @param user The name of the user
     * @see <a href=
     * "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#clear-a-users-collection-access-level"> API
     * Documentation</a>
     * @since ArangoDB 3.2.0
     */
    void resetAccess(String user);

    /**
     * Get the collection access level
     *
     * @param user The name of the user
     * @return permissions of the user
     * @see <a href= "https://docs.arango.ai/arangodb/stable/develop/http-api/users/#get-a-users-collection-access-level">
     * API Documentation</a>
     * @since ArangoDB 3.2.0
     */
    Permissions getPermissions(String user);

}

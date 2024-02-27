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
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of {@link ArangoCollection}
 */
@ThreadSafe
public interface ArangoCollectionAsync extends ArangoSerdeAccessor {

    /**
     * @return database async API
     */
    ArangoDatabaseAsync db();

    /**
     * @return collection name
     */
    String name();

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocument(Object)}
     */
    CompletableFuture<DocumentCreateEntity<Void>> insertDocument(Object value);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocument(Object, DocumentCreateOptions)}
     */
    <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(T value, DocumentCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocument(Object, DocumentCreateOptions, Class)}
     */
    <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(T value, DocumentCreateOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocuments(RawData)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(RawData values);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocuments(RawData, DocumentCreateOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<RawData>>> insertDocuments(
            RawData values, DocumentCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocuments(Iterable)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(Iterable<?> values);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocuments(Iterable, DocumentCreateOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(
            Iterable<?> values, DocumentCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#insertDocuments(Iterable, DocumentCreateOptions, Class)}
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(
            Iterable<? extends T> values, DocumentCreateOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#importDocuments(Iterable)}
     */
    CompletableFuture<DocumentImportEntity> importDocuments(Iterable<?> values);

    /**
     * Asynchronous version of {@link ArangoCollection#importDocuments(Iterable, DocumentImportOptions)}
     */
    CompletableFuture<DocumentImportEntity> importDocuments(Iterable<?> values, DocumentImportOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#importDocuments(RawData)}
     */
    CompletableFuture<DocumentImportEntity> importDocuments(RawData values);

    /**
     * Asynchronous version of {@link ArangoCollection#importDocuments(RawData, DocumentImportOptions)}
     */
    CompletableFuture<DocumentImportEntity> importDocuments(RawData values, DocumentImportOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#getDocument(String, Class)}
     */
    <T> CompletableFuture<T> getDocument(String key, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#getDocument(String, Class, DocumentReadOptions)}
     */
    <T> CompletableFuture<T> getDocument(String key, Class<T> type, DocumentReadOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#getDocuments(Iterable, Class)}
     */
    <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(Iterable<String> keys, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#getDocuments(Iterable, Class, DocumentReadOptions)}
     */
    <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(Iterable<String> keys, Class<T> type, DocumentReadOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocument(String, Object)}
     */
    CompletableFuture<DocumentUpdateEntity<Void>> replaceDocument(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocument(String, Object, DocumentReplaceOptions)}
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(String key, T value, DocumentReplaceOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocument(String, Object, DocumentReplaceOptions, Class)}
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(String key, T value, DocumentReplaceOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocuments(RawData)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(RawData values);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocuments(RawData, DocumentReplaceOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> replaceDocuments(
            RawData values, DocumentReplaceOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocuments(Iterable)} )}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(Iterable<?> values);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocuments(Iterable, DocumentReplaceOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(
            Iterable<?> values, DocumentReplaceOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#replaceDocuments(Iterable, DocumentReplaceOptions, Class)}
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(
            Iterable<? extends T> values, DocumentReplaceOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocument(String, Object)}
     */
    CompletableFuture<DocumentUpdateEntity<Void>> updateDocument(String key, Object value);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocument(String, Object, DocumentUpdateOptions)}
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(String key, T value, DocumentUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocument(String, Object, DocumentUpdateOptions, Class)}
     */
    <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(String key, Object value, DocumentUpdateOptions options,
                                               Class<T> returnType);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocuments(RawData)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(RawData values);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocuments(RawData, DocumentUpdateOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> updateDocuments(
            RawData values, DocumentUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocuments(Iterable)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(Iterable<?> values);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocuments(Iterable, DocumentUpdateOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(
            Iterable<?> values, DocumentUpdateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#updateDocuments(Iterable, DocumentUpdateOptions, Class)}
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(
            Iterable<?> values, DocumentUpdateOptions options, Class<T> returnType);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocument(String)}
     */
    CompletableFuture<DocumentDeleteEntity<Void>> deleteDocument(String key);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocument(String, DocumentDeleteOptions)}
     */
    CompletableFuture<DocumentDeleteEntity<Void>> deleteDocument(String key, DocumentDeleteOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocument(String, DocumentDeleteOptions, Class)}
     */
    <T> CompletableFuture<DocumentDeleteEntity<T>> deleteDocument(String key, DocumentDeleteOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocuments(RawData)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(RawData values);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocuments(RawData, DocumentDeleteOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<RawData>>> deleteDocuments(
            RawData values, DocumentDeleteOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocuments(Iterable)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(Iterable<?> values);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocuments(Iterable, DocumentDeleteOptions)}
     */
    CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(
            Iterable<?> values, DocumentDeleteOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteDocuments(Iterable, DocumentDeleteOptions, Class)}
     */
    <T> CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocuments(
            Iterable<?> values, DocumentDeleteOptions options, Class<T> type);

    /**
     * Asynchronous version of {@link ArangoCollection#documentExists(String)}
     */
    CompletableFuture<Boolean> documentExists(String key);

    /**
     * Asynchronous version of {@link ArangoCollection#documentExists(String, DocumentExistsOptions)}
     */
    CompletableFuture<Boolean> documentExists(String key, DocumentExistsOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#getIndex(String)}
     */
    CompletableFuture<IndexEntity> getIndex(String id);

    /**
     * Asynchronous version of {@link ArangoCollection#getInvertedIndex(String)}
     */
    CompletableFuture<InvertedIndexEntity> getInvertedIndex(String id);

    /**
     * Asynchronous version of {@link ArangoCollection#deleteIndex(String)}
     */
    CompletableFuture<String> deleteIndex(String id);

    /**
     * Asynchronous version of {@link ArangoCollection#ensurePersistentIndex(Iterable, PersistentIndexOptions)}
     */
    CompletableFuture<IndexEntity> ensurePersistentIndex(Iterable<String> fields, PersistentIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureGeoIndex(Iterable, GeoIndexOptions)}
     */
    CompletableFuture<IndexEntity> ensureGeoIndex(Iterable<String> fields, GeoIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureFulltextIndex(Iterable, FulltextIndexOptions)}
     */
    @Deprecated
    CompletableFuture<IndexEntity> ensureFulltextIndex(Iterable<String> fields, FulltextIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureTtlIndex(Iterable, TtlIndexOptions)}
     */
    CompletableFuture<IndexEntity> ensureTtlIndex(Iterable<String> fields, TtlIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureZKDIndex(Iterable, ZKDIndexOptions)}
     *
     * @deprecated since ArangoDB 3.12, use {@link #ensureMDIndex(Iterable, MDIndexOptions)} instead.
     */
    @Deprecated
    CompletableFuture<IndexEntity> ensureZKDIndex(Iterable<String> fields, ZKDIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureMDIndex(Iterable, MDIndexOptions)}
     */
    CompletableFuture<IndexEntity> ensureMDIndex(Iterable<String> fields, MDIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#ensureInvertedIndex(InvertedIndexOptions)}
     */
    CompletableFuture<InvertedIndexEntity> ensureInvertedIndex(InvertedIndexOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#getIndexes()}
     */
    CompletableFuture<Collection<IndexEntity>> getIndexes();

    /**
     * Asynchronous version of {@link ArangoCollection#getInvertedIndexes()}
     */
    CompletableFuture<Collection<InvertedIndexEntity>> getInvertedIndexes();

    /**
     * Asynchronous version of {@link ArangoCollection#exists()}
     */
    CompletableFuture<Boolean> exists();

    /**
     * Asynchronous version of {@link ArangoCollection#truncate()}
     */
    CompletableFuture<CollectionEntity> truncate();

    /**
     * Asynchronous version of {@link ArangoCollection#truncate(CollectionTruncateOptions)}
     */
    CompletableFuture<CollectionEntity> truncate(CollectionTruncateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#count()}
     */
    CompletableFuture<CollectionPropertiesEntity> count();

    /**
     * Asynchronous version of {@link ArangoCollection#count(CollectionCountOptions)}
     */
    CompletableFuture<CollectionPropertiesEntity> count(CollectionCountOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#create()}
     */
    CompletableFuture<CollectionEntity> create();

    /**
     * Asynchronous version of {@link ArangoCollection#create(CollectionCreateOptions)}
     */
    CompletableFuture<CollectionEntity> create(CollectionCreateOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#drop()}
     */
    CompletableFuture<Void> drop();

    /**
     * Asynchronous version of {@link ArangoCollection#drop(boolean)}
     */
    CompletableFuture<Void> drop(boolean isSystem);

    /**
     * Asynchronous version of {@link ArangoCollection#getInfo()}
     */
    CompletableFuture<CollectionEntity> getInfo();

    /**
     * Asynchronous version of {@link ArangoCollection#getProperties()}
     */
    CompletableFuture<CollectionPropertiesEntity> getProperties();

    /**
     * Asynchronous version of {@link ArangoCollection#changeProperties(CollectionPropertiesOptions)}
     */
    CompletableFuture<CollectionPropertiesEntity> changeProperties(CollectionPropertiesOptions options);

    /**
     * Asynchronous version of {@link ArangoCollection#rename(String)}
     */
    CompletableFuture<CollectionEntity> rename(String newName);

    /**
     * Asynchronous version of {@link ArangoCollection#getResponsibleShard(Object)}
     */
    CompletableFuture<ShardEntity> getResponsibleShard(final Object value);

    /**
     * Asynchronous version of {@link ArangoCollection#getRevision()}
     */
    CompletableFuture<CollectionRevisionEntity> getRevision();

    /**
     * Asynchronous version of {@link ArangoCollection#grantAccess(String, Permissions)}
     */
    CompletableFuture<Void> grantAccess(String user, Permissions permissions);

    /**
     * Asynchronous version of {@link ArangoCollection#revokeAccess(String)}
     */
    CompletableFuture<Void> revokeAccess(String user);

    /**
     * Asynchronous version of {@link ArangoCollection#resetAccess(String)}
     */
    CompletableFuture<Void> resetAccess(String user);

    /**
     * Asynchronous version of {@link ArangoCollection#getPermissions(String)}
     */
    CompletableFuture<Permissions> getPermissions(String user);

}

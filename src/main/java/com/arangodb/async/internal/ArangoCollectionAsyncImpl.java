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

package com.arangodb.async.internal;

import com.arangodb.async.ArangoCollectionAsync;
import com.arangodb.entity.*;
import com.arangodb.internal.InternalArangoCollection;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.model.*;
import com.arangodb.util.RawData;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoCollectionAsyncImpl
        extends InternalArangoCollection<ArangoDBAsyncImpl, ArangoDatabaseAsyncImpl, ArangoExecutorAsync>
        implements ArangoCollectionAsync {

    ArangoCollectionAsyncImpl(final ArangoDatabaseAsyncImpl db, final String name) {
        super(db, name);
    }

    @Override
    public CompletableFuture<DocumentCreateEntity<Void>> insertDocument(final Object value) {
        return executor.execute(insertDocumentRequest(value, new DocumentCreateOptions()),
                constructParametricType(DocumentCreateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(
            final T value,
            final DocumentCreateOptions options) {
        return insertDocument(value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(T value, DocumentCreateOptions options,
                                                                         Class<T> type) {
        return executor.execute(insertDocumentRequest(value, options),
                constructParametricType(DocumentCreateEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(RawData values) {
        return executor
                .execute(insertDocumentsRequest(values, new DocumentCreateOptions()),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<RawData>>> insertDocuments(RawData values,
                                                                                                 DocumentCreateOptions options) {
        return executor
                .execute(insertDocumentsRequest(values, options),
                        insertDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(
            final Collection<?> values) {
        return executor
                .execute(insertDocumentsRequest(values, new DocumentCreateOptions()),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(
            final Collection<T> values,
            final DocumentCreateOptions options) {
        return insertDocuments(values, options, (Class<T>) getCollectionContentClass(values));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(Collection<T> values,
                                                                                               DocumentCreateOptions options, Class<T> type) {
        return executor
                .execute(insertDocumentsRequest(values, options), insertDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(final Collection<?> values) {
        return importDocuments(values, new DocumentImportOptions());
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(
            final Collection<?> values,
            final DocumentImportOptions options) {
        return executor.execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(RawData values) {
        return executor.execute(importDocumentsRequest(values, new DocumentImportOptions()),
                DocumentImportEntity.class);
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(RawData values, DocumentImportOptions options) {
        return executor.execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public <T> CompletableFuture<T> getDocument(final String key, final Class<T> type) {
        return getDocument(key, type, new DocumentReadOptions());
    }

    @Override
    public <T> CompletableFuture<T> getDocument(
            final String key,
            final Class<T> type,
            final DocumentReadOptions options) {
        DocumentUtil.validateDocumentKey(key);
        return executor.execute(getDocumentRequest(key, options), getDocumentResponseDeserializer(type))
                .exceptionally(ExceptionUtil.catchGetDocumentExceptions());
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(
            final Collection<String> keys,
            final Class<T> type) {
        return getDocuments(keys, type, new DocumentReadOptions());
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(
            final Collection<String> keys,
            final Class<T> type,
            final DocumentReadOptions options) {
        return executor.execute(getDocumentsRequest(keys, options), getDocumentsResponseDeserializer(type, options));
    }

    @Override
    public CompletableFuture<DocumentUpdateEntity<Void>> replaceDocument(final String key, final Object value) {
        final DocumentReplaceOptions options = new DocumentReplaceOptions();
        return executor.execute(replaceDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(
            final String key,
            final T value,
            final DocumentReplaceOptions options) {
        return replaceDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(String key, T value,
                                                                          DocumentReplaceOptions options,
                                                                          Class<T> type) {
        return executor.execute(replaceDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(RawData values) {
        return executor.execute(replaceDocumentsRequest(values, new DocumentReplaceOptions()),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> replaceDocuments(RawData values,
                                                                                                  DocumentReplaceOptions options) {
        return executor.execute(replaceDocumentsRequest(values, options),
                replaceDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(
            final Collection<?> values) {
        return executor.execute(replaceDocumentsRequest(values, new DocumentReplaceOptions()),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(
            final Collection<T> values,
            final DocumentReplaceOptions options) {
        return replaceDocuments(values, options, (Class<T>) getCollectionContentClass(values));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(Collection<T> values,
                                                                                                DocumentReplaceOptions options, Class<T> type) {
        return executor.execute(replaceDocumentsRequest(values, options), replaceDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<DocumentUpdateEntity<Void>> updateDocument(final String key, final Object value) {
        return updateDocument(key, value, new DocumentUpdateOptions(), Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(
            final String key,
            final T value,
            final DocumentUpdateOptions options) {
        return updateDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(
            final String key,
            final Object value,
            final DocumentUpdateOptions options,
            final Class<T> returnType) {
        return executor.execute(updateDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, returnType));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(RawData values) {
        return executor
                .execute(updateDocumentsRequest(values, new DocumentUpdateOptions()),
                        updateDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> updateDocuments(RawData values,
                                                                                                 DocumentUpdateOptions options) {
        return executor
                .execute(updateDocumentsRequest(values, options),
                        updateDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(
            final Collection<?> values) {
        return updateDocuments(values, new DocumentUpdateOptions(), Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(
            final Collection<T> values,
            final DocumentUpdateOptions options) {
        return updateDocuments(values, options, (Class<T>) getCollectionContentClass(values));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(
            final Collection<?> values,
            final DocumentUpdateOptions options,
            final Class<T> returnType) {
        return executor
                .execute(updateDocumentsRequest(values, options), updateDocumentsResponseDeserializer(returnType));
    }

    @Override
    public CompletableFuture<DocumentDeleteEntity<Void>> deleteDocument(final String key) {
        return deleteDocument(key, new DocumentDeleteOptions());
    }

    @Override
    public CompletableFuture<DocumentDeleteEntity<Void>> deleteDocument(String key, DocumentDeleteOptions options) {
        return deleteDocument(key, options, Void.class);
    }

    @Override
    public <T> CompletableFuture<DocumentDeleteEntity<T>> deleteDocument(
            final String key,
            final DocumentDeleteOptions options,
            final Class<T> type) {
        return executor.execute(deleteDocumentRequest(key, options),
                constructParametricType(DocumentDeleteEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(RawData values) {
        return executor.execute(deleteDocumentsRequest(values, new DocumentDeleteOptions()),
                deleteDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<RawData>>> deleteDocuments(RawData values,
                                                                                                 DocumentDeleteOptions options) {
        return executor.execute(deleteDocumentsRequest(values, options),
                deleteDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(
            final Collection<?> values) {
        return deleteDocuments(values, new DocumentDeleteOptions(), Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocuments(Collection<?> values,
                                                                                               DocumentDeleteOptions options) {
        return deleteDocuments(values, options, (Class<T>) getCollectionContentClass(values));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocuments(
            final Collection<?> values,
            final DocumentDeleteOptions options,
            final Class<T> type) {
        return executor.execute(deleteDocumentsRequest(values, options), deleteDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<Boolean> documentExists(final String key) {
        return documentExists(key, new DocumentExistsOptions());
    }

    @Override
    public CompletableFuture<Boolean> documentExists(final String key, final DocumentExistsOptions options) {
        return executor.execute(documentExistsRequest(key, options), response -> response)
                .exceptionally(ExceptionUtil.catchGetDocumentExceptions())
                .thenApply(Objects::nonNull);
    }

    @Override
    public CompletableFuture<IndexEntity> getIndex(final String id) {
        return executor.execute(getIndexRequest(id), IndexEntity.class);
    }

    @Override
    public CompletableFuture<String> deleteIndex(final String id) {
        return executor.execute(deleteIndexRequest(id), deleteIndexResponseDeserializer());
    }

    @Override
    @Deprecated
    public CompletableFuture<IndexEntity> ensureHashIndex(
            final Iterable<String> fields,
            final HashIndexOptions options) {
        return executor.execute(createHashIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    @Deprecated
    public CompletableFuture<IndexEntity> ensureSkiplistIndex(
            final Iterable<String> fields,
            final SkiplistIndexOptions options) {
        return executor.execute(createSkiplistIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensurePersistentIndex(
            final Iterable<String> fields,
            final PersistentIndexOptions options) {
        return executor.execute(createPersistentIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options) {
        return executor.execute(createGeoIndexRequest(fields, options), IndexEntity.class);
    }

    @Deprecated
    @Override
    public CompletableFuture<IndexEntity> ensureFulltextIndex(
            final Iterable<String> fields,
            final FulltextIndexOptions options) {
        return executor.execute(createFulltextIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureTtlIndex(Iterable<String> fields, TtlIndexOptions options) {
        return executor.execute(createTtlIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureZKDIndex(
            final Iterable<String> fields,
            final ZKDIndexOptions options) {
        return executor.execute(createZKDIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<Collection<IndexEntity>> getIndexes() {
        return executor.execute(getIndexesRequest(), getIndexesResponseDeserializer());
    }

    @Override
    public CompletableFuture<Boolean> exists() {
        return getInfo().thenApply(Objects::nonNull).exceptionally(Objects::isNull);
    }

    @Override
    public CompletableFuture<CollectionEntity> truncate() {
        return truncate(null);
    }

    @Override
    public CompletableFuture<CollectionEntity> truncate(CollectionTruncateOptions options) {
        return executor.execute(truncateRequest(options), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> count() {
        return count(null);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> count(CollectionCountOptions options) {
        return executor.execute(countRequest(options), CollectionPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> create() {
        return db().createCollection(name());
    }

    @Override
    public CompletableFuture<CollectionEntity> create(final CollectionCreateOptions options) {
        return db().createCollection(name(), options);
    }

    @Override
    public CompletableFuture<Void> drop() {
        return executor.execute(dropRequest(null), Void.class);
    }

    @Override
    public CompletableFuture<Void> drop(final boolean isSystem) {
        return executor.execute(dropRequest(isSystem), Void.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> getInfo() {
        return executor.execute(getInfoRequest(), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> getProperties() {
        return executor.execute(getPropertiesRequest(), CollectionPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> changeProperties(final CollectionPropertiesOptions options) {
        return executor.execute(changePropertiesRequest(options), CollectionPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> rename(final String newName) {
        return executor.execute(renameRequest(newName), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<ShardEntity> getResponsibleShard(Object value) {
        return executor.execute(responsibleShardRequest(value), ShardEntity.class);
    }

    @Override
    public CompletableFuture<CollectionRevisionEntity> getRevision() {
        return executor.execute(getRevisionRequest(), CollectionRevisionEntity.class);
    }

    @Override
    public CompletableFuture<Void> grantAccess(final String user, final Permissions permissions) {
        return executor.execute(grantAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Void> revokeAccess(final String user) {
        return executor.execute(grantAccessRequest(user, Permissions.NONE), Void.class);
    }

    @Override
    public CompletableFuture<Void> resetAccess(final String user) {
        return executor.execute(resetAccessRequest(user), Void.class);
    }

    @Override
    public CompletableFuture<Permissions> getPermissions(final String user) {
        return executor.execute(getPermissionsRequest(user), getPermissionsResponseDeserialzer());
    }
}

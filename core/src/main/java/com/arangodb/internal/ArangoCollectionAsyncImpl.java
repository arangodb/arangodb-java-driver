/*
 * DISCLAIMER
 *
 * Copyright 2018 ArangoDB GmbH, Cologne, Germany
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

package com.arangodb.internal;

import com.arangodb.ArangoCollectionAsync;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabaseAsync;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import com.arangodb.util.RawData;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.arangodb.internal.ArangoErrors.*;
import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoCollectionAsyncImpl extends InternalArangoCollection implements ArangoCollectionAsync {

    private final ArangoDatabaseAsync db;

    protected ArangoCollectionAsyncImpl(final ArangoDatabaseAsyncImpl db, final String name) {
        super(db, db.name(), name);
        this.db = db;
    }

    @Override
    public ArangoDatabaseAsync db() {
        return db;
    }

    @Override
    public CompletableFuture<DocumentCreateEntity<Void>> insertDocument(final Object value) {
        return executorAsync().execute(() -> insertDocumentRequest(value, new DocumentCreateOptions()),
                constructParametricType(DocumentCreateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(final T value, final DocumentCreateOptions options) {
        return insertDocument(value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentCreateEntity<T>> insertDocument(final T value, final DocumentCreateOptions options,
                                                                         final Class<T> type) {
        return executorAsync().execute(() -> insertDocumentRequest(value, options),
                constructParametricType(DocumentCreateEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(RawData values) {
        return executorAsync()
                .execute(() -> insertDocumentsRequest(values, new DocumentCreateOptions()),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<RawData>>> insertDocuments(RawData values,
                                                                                                 DocumentCreateOptions options) {
        return executorAsync()
                .execute(() -> insertDocumentsRequest(values, options),
                        insertDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(final Iterable<?> values) {
        return insertDocuments(values, new DocumentCreateOptions());
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<Void>>> insertDocuments(
            final Iterable<?> values, final DocumentCreateOptions options) {
        return executorAsync()
                .execute(() -> insertDocumentsRequest(values, options),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocuments(Iterable<? extends T> values,
                                                                                               DocumentCreateOptions options,
                                                                                               Class<T> type) {
        return executorAsync()
                .execute(() -> insertDocumentsRequest(values, options), insertDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(final Iterable<?> values) {
        return importDocuments(values, new DocumentImportOptions());
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(final Iterable<?> values, final DocumentImportOptions options) {
        return executorAsync().execute(() -> importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(RawData values) {
        return importDocuments(values, new DocumentImportOptions());
    }

    @Override
    public CompletableFuture<DocumentImportEntity> importDocuments(RawData values, DocumentImportOptions options) {
        return executorAsync().execute(() -> importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public <T> CompletableFuture<T> getDocument(final String key, final Class<T> type) {
        return getDocument(key, type, new DocumentReadOptions());
    }

    @Override
    public <T> CompletableFuture<T> getDocument(final String key, final Class<T> type, final DocumentReadOptions options) {
        return executorAsync().execute(() -> getDocumentRequest(key, options), getDocumentResponseDeserializer(type))
                .exceptionally(err -> {
                    Throwable e = err instanceof CompletionException ? err.getCause() : err;
                    if (e instanceof ArangoDBException) {
                        ArangoDBException aEx = (ArangoDBException) e;
                        if (matches(aEx, 304)
                                || matches(aEx, 404, ERROR_ARANGO_DOCUMENT_NOT_FOUND)
                                || matches(aEx, 412, ERROR_ARANGO_CONFLICT)
                        ) {
                            return null;
                        }
                    }
                    throw ArangoDBException.of(e);
                });
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(final Iterable<String> keys, final Class<T> type) {
        return getDocuments(keys, type, new DocumentReadOptions());
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<T>> getDocuments(
            final Iterable<String> keys, final Class<T> type, final DocumentReadOptions options) {
        return executorAsync().execute(() -> getDocumentsRequest(keys, options), getDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<DocumentUpdateEntity<Void>> replaceDocument(final String key, final Object value) {
        return executorAsync().execute(() -> replaceDocumentRequest(key, value, new DocumentReplaceOptions()),
                constructParametricType(DocumentUpdateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(
            final String key, final T value, final DocumentReplaceOptions options) {
        return replaceDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentUpdateEntity<T>> replaceDocument(String key, T value, DocumentReplaceOptions options,
                                                                          Class<T> type) {
        return executorAsync().execute(() -> replaceDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(RawData values) {
        return executorAsync().execute(() -> replaceDocumentsRequest(values, new DocumentReplaceOptions()),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> replaceDocuments(RawData values,
                                                                                                  DocumentReplaceOptions options) {
        return executorAsync().execute(() -> replaceDocumentsRequest(values, options),
                replaceDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(final Iterable<?> values) {
        return replaceDocuments(values, new DocumentReplaceOptions());
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> replaceDocuments(
            final Iterable<?> values, final DocumentReplaceOptions options) {
        return executorAsync().execute(() -> replaceDocumentsRequest(values, options),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocuments(Iterable<? extends T> values,
                                                                                                DocumentReplaceOptions options,
                                                                                                Class<T> type) {
        return executorAsync().execute(() -> replaceDocumentsRequest(values, options), replaceDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<DocumentUpdateEntity<Void>> updateDocument(final String key, final Object value) {
        return updateDocument(key, value, new DocumentUpdateOptions(), Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(
            final String key, final T value, final DocumentUpdateOptions options) {
        return updateDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> CompletableFuture<DocumentUpdateEntity<T>> updateDocument(
            final String key, final Object value, final DocumentUpdateOptions options, final Class<T> returnType) {
        return executorAsync().execute(() -> updateDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, returnType));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(RawData values) {
        return executorAsync()
                .execute(() -> updateDocumentsRequest(values, new DocumentUpdateOptions()),
                        updateDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<RawData>>> updateDocuments(RawData values,
                                                                                                 DocumentUpdateOptions options) {
        return executorAsync()
                .execute(() -> updateDocumentsRequest(values, options),
                        updateDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(final Iterable<?> values) {
        return updateDocuments(values, new DocumentUpdateOptions());
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<Void>>> updateDocuments(
            final Iterable<?> values, final DocumentUpdateOptions options) {
        return updateDocuments(values, options, Void.class);
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocuments(
            final Iterable<?> values, final DocumentUpdateOptions options, final Class<T> returnType) {
        return executorAsync()
                .execute(() -> updateDocumentsRequest(values, options), updateDocumentsResponseDeserializer(returnType));
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
            final String key, final DocumentDeleteOptions options, final Class<T> type) {
        return executorAsync().execute(() -> deleteDocumentRequest(key, options),
                constructParametricType(DocumentDeleteEntity.class, type));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(RawData values) {
        return executorAsync().execute(() -> deleteDocumentsRequest(values, new DocumentDeleteOptions()),
                deleteDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<RawData>>> deleteDocuments(RawData values,
                                                                                                 DocumentDeleteOptions options) {
        return executorAsync().execute(() -> deleteDocumentsRequest(values, options),
                deleteDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(final Iterable<?> values) {
        return deleteDocuments(values, new DocumentDeleteOptions());
    }

    @Override
    public CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<Void>>> deleteDocuments(
            final Iterable<?> values, final DocumentDeleteOptions options) {
        return deleteDocuments(values, options, Void.class);
    }

    @Override
    public <T> CompletableFuture<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocuments(
            final Iterable<?> values, final DocumentDeleteOptions options, final Class<T> type) {
        return executorAsync().execute(() -> deleteDocumentsRequest(values, options), deleteDocumentsResponseDeserializer(type));
    }

    @Override
    public CompletableFuture<Boolean> documentExists(final String key) {
        return documentExists(key, new DocumentExistsOptions());
    }

    @Override
    public CompletableFuture<Boolean> documentExists(final String key, final DocumentExistsOptions options) {
        return executorAsync().execute(() -> documentExistsRequest(key, options), Void.class)
                .thenApply(it -> true)
                .exceptionally(err -> {
                    Throwable e = err instanceof CompletionException ? err.getCause() : err;
                    if (e instanceof ArangoDBException) {
                        ArangoDBException aEx = (ArangoDBException) e;
                        if (matches(aEx, 304)
                                || matches(aEx, 404)
                                || matches(aEx, 412)
                        ) {
                            return false;
                        }
                    }
                    throw ArangoDBException.of(e);
                });
    }

    @Override
    public CompletableFuture<IndexEntity> getIndex(final String id) {
        return executorAsync().execute(() -> getIndexRequest(id), IndexEntity.class);
    }

    @Override
    public CompletableFuture<InvertedIndexEntity> getInvertedIndex(String id) {
        return executorAsync().execute(() -> getIndexRequest(id), InvertedIndexEntity.class);
    }

    @Override
    public CompletableFuture<String> deleteIndex(final String id) {
        return executorAsync().execute(() -> deleteIndexRequest(id), deleteIndexResponseDeserializer());
    }

    @Override
    public CompletableFuture<IndexEntity> ensurePersistentIndex(final Iterable<String> fields, final PersistentIndexOptions options) {
        return executorAsync().execute(() -> createPersistentIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<InvertedIndexEntity> ensureInvertedIndex(final InvertedIndexOptions options) {
        return executorAsync().execute(() -> createInvertedIndexRequest(options), InvertedIndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureVectorIndex(Iterable<String> fields, VectorIndexOptions options) {
        return executorAsync().execute(() -> createVectorIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options) {
        return executorAsync().execute(() -> createGeoIndexRequest(fields, options), IndexEntity.class);
    }

    @Deprecated
    @Override
    public CompletableFuture<IndexEntity> ensureFulltextIndex(final Iterable<String> fields, final FulltextIndexOptions options) {
        return executorAsync().execute(() -> createFulltextIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureTtlIndex(final Iterable<String> fields, final TtlIndexOptions options) {
        return executorAsync().execute(() -> createTtlIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureZKDIndex(final Iterable<String> fields, final ZKDIndexOptions options) {
        return executorAsync().execute(() -> createZKDIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureMDIndex(final Iterable<String> fields, final MDIndexOptions options) {
        return executorAsync().execute(() -> createMDIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<IndexEntity> ensureMDPrefixedIndex(final Iterable<String> fields, final MDPrefixedIndexOptions options) {
        return executorAsync().execute(() -> createMDIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public CompletableFuture<Collection<IndexEntity>> getIndexes() {
        return executorAsync().execute(this::getIndexesRequest, getIndexesResponseDeserializer());
    }

    @Override
    public CompletableFuture<Collection<InvertedIndexEntity>> getInvertedIndexes() {
        return executorAsync().execute(this::getIndexesRequest, getInvertedIndexesResponseDeserializer());
    }

    @Override
    public CompletableFuture<Boolean> exists() {
        return getInfo()
                .thenApply(Objects::nonNull)
                .exceptionally(err -> {
                    Throwable e = err instanceof CompletionException ? err.getCause() : err;
                    if (e instanceof ArangoDBException) {
                        ArangoDBException aEx = (ArangoDBException) e;
                        if (matches(aEx, 404, ERROR_ARANGO_DATA_SOURCE_NOT_FOUND)) {
                            return false;
                        }
                    }
                    throw ArangoDBException.of(e);
                });
    }

    @Override
    public CompletableFuture<CollectionEntity> truncate() {
        return truncate(null);
    }

    @Override
    public CompletableFuture<CollectionEntity> truncate(CollectionTruncateOptions options) {
        return executorAsync().execute(() -> truncateRequest(options), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> count() {
        return count(null);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> count(CollectionCountOptions options) {
        return executorAsync().execute(() -> countRequest(options), CollectionPropertiesEntity.class);
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
        return executorAsync().execute(() -> dropRequest(null), Void.class);
    }

    @Override
    public CompletableFuture<Void> drop(final boolean isSystem) {
        return executorAsync().execute(() -> dropRequest(isSystem), Void.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> getInfo() {
        return executorAsync().execute(this::getInfoRequest, CollectionEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> getProperties() {
        return executorAsync().execute(this::getPropertiesRequest, CollectionPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<CollectionPropertiesEntity> changeProperties(final CollectionPropertiesOptions options) {
        return executorAsync().execute(() -> changePropertiesRequest(options), CollectionPropertiesEntity.class);
    }

    @Override
    public CompletableFuture<CollectionEntity> rename(final String newName) {
        return executorAsync().execute(() -> renameRequest(newName), CollectionEntity.class);
    }

    @Override
    public CompletableFuture<ShardEntity> getResponsibleShard(final Object value) {
        return executorAsync().execute(() -> responsibleShardRequest(value), ShardEntity.class);
    }

    @Override
    public CompletableFuture<CollectionRevisionEntity> getRevision() {
        return executorAsync().execute(this::getRevisionRequest, CollectionRevisionEntity.class);
    }

    @Override
    public CompletableFuture<Void> grantAccess(final String user, final Permissions permissions) {
        return executorAsync().execute(() -> grantAccessRequest(user, permissions), Void.class);
    }

    @Override
    public CompletableFuture<Void> revokeAccess(final String user) {
        return executorAsync().execute(() -> grantAccessRequest(user, Permissions.NONE), Void.class);
    }

    @Override
    public CompletableFuture<Void> resetAccess(final String user) {
        return executorAsync().execute(() -> resetAccessRequest(user), Void.class);
    }

    @Override
    public CompletableFuture<Permissions> getPermissions(final String user) {
        return executorAsync().execute(() -> getPermissionsRequest(user), getPermissionsResponseDeserialzer());
    }

}

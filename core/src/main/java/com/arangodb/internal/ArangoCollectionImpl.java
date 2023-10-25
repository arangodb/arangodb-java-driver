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

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.*;
import com.arangodb.model.*;
import com.arangodb.util.RawData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.arangodb.internal.serde.SerdeUtils.constructParametricType;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public class ArangoCollectionImpl extends InternalArangoCollection implements ArangoCollection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArangoCollectionImpl.class);

    private final ArangoDatabase db;

    protected ArangoCollectionImpl(final ArangoDatabaseImpl db, final String name) {
        super(db, db.name(), name);
        this.db = db;
    }

    @Override
    public ArangoDatabase db() {
        return db;
    }

    @Override
    public DocumentCreateEntity<Void> insertDocument(final Object value) {
        return executorSync().execute(insertDocumentRequest(value, new DocumentCreateOptions()),
                constructParametricType(DocumentCreateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options) {
        return insertDocument(value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options,
                                                      final Class<T> type) {
        return executorSync().execute(insertDocumentRequest(value, options),
                constructParametricType(DocumentCreateEntity.class, type));
    }

    @Override
    public MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(RawData values) {
        return executorSync()
                .execute(insertDocumentsRequest(values, new DocumentCreateOptions()),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultiDocumentEntity<DocumentCreateEntity<RawData>> insertDocuments(RawData values,
                                                                              DocumentCreateOptions options) {
        return executorSync()
                .execute(insertDocumentsRequest(values, options),
                        insertDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(final Iterable<?> values) {
        return insertDocuments(values, new DocumentCreateOptions());
    }

    @Override
    public MultiDocumentEntity<DocumentCreateEntity<Void>> insertDocuments(
            final Iterable<?> values, final DocumentCreateOptions options) {
        return executorSync()
                .execute(insertDocumentsRequest(values, options),
                        insertDocumentsResponseDeserializer(Void.class));
    }

    @Override
    public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(Iterable<? extends T> values,
                                                                            DocumentCreateOptions options,
                                                                            Class<T> type) {
        return executorSync()
                .execute(insertDocumentsRequest(values, options), insertDocumentsResponseDeserializer(type));
    }

    @Override
    public DocumentImportEntity importDocuments(final Iterable<?> values) {
        return importDocuments(values, new DocumentImportOptions());
    }

    @Override
    public DocumentImportEntity importDocuments(final Iterable<?> values, final DocumentImportOptions options) {
        return executorSync().execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public DocumentImportEntity importDocuments(RawData values) {
        return importDocuments(values, new DocumentImportOptions());
    }

    @Override
    public DocumentImportEntity importDocuments(RawData values, DocumentImportOptions options) {
        return executorSync().execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
    }

    @Override
    public <T> T getDocument(final String key, final Class<T> type) {
        return getDocument(key, type, new DocumentReadOptions());
    }

    @Override
    public <T> T getDocument(final String key, final Class<T> type, final DocumentReadOptions options) {
        try {
            return executorSync().execute(getDocumentRequest(key, options), getDocumentResponseDeserializer(type));
        } catch (final ArangoDBException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            }

            // handle Response: 404, Error: 1655 - transaction not found
            if (e.getErrorNum() != null && e.getErrorNum() == 1655) {
                throw e;
            }

            if ((e.getResponseCode() != null && (e.getResponseCode() == 404 || e.getResponseCode() == 304
                    || e.getResponseCode() == 412))) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public <T> MultiDocumentEntity<T> getDocuments(final Iterable<String> keys, final Class<T> type) {
        return getDocuments(keys, type, new DocumentReadOptions());
    }

    @Override
    public <T> MultiDocumentEntity<T> getDocuments(
            final Iterable<String> keys, final Class<T> type, final DocumentReadOptions options) {
        return executorSync().execute(getDocumentsRequest(keys, options), getDocumentsResponseDeserializer(type));
    }

    @Override
    public DocumentUpdateEntity<Void> replaceDocument(final String key, final Object value) {
        return executorSync().execute(replaceDocumentRequest(key, value, new DocumentReplaceOptions()),
                constructParametricType(DocumentUpdateEntity.class, Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DocumentUpdateEntity<T> replaceDocument(
            final String key, final T value, final DocumentReplaceOptions options) {
        return replaceDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> DocumentUpdateEntity<T> replaceDocument(String key, T value, DocumentReplaceOptions options,
                                                       Class<T> type) {
        return executorSync().execute(replaceDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, type));
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(RawData values) {
        return executorSync().execute(replaceDocumentsRequest(values, new DocumentReplaceOptions()),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultiDocumentEntity<DocumentUpdateEntity<RawData>> replaceDocuments(RawData values,
                                                                               DocumentReplaceOptions options) {
        return executorSync().execute(replaceDocumentsRequest(values, options),
                replaceDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(final Iterable<?> values) {
        return replaceDocuments(values, new DocumentReplaceOptions());
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> replaceDocuments(
            final Iterable<?> values, final DocumentReplaceOptions options) {
        return executorSync().execute(replaceDocumentsRequest(values, options),
                replaceDocumentsResponseDeserializer(Void.class));
    }

    @Override
    public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(Iterable<? extends T> values,
                                                                             DocumentReplaceOptions options,
                                                                             Class<T> type) {
        return executorSync().execute(replaceDocumentsRequest(values, options), replaceDocumentsResponseDeserializer(type));
    }

    @Override
    public DocumentUpdateEntity<Void> updateDocument(final String key, final Object value) {
        return updateDocument(key, value, new DocumentUpdateOptions(), Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DocumentUpdateEntity<T> updateDocument(
            final String key, final T value, final DocumentUpdateOptions options) {
        return updateDocument(key, value, options, (Class<T>) value.getClass());
    }

    @Override
    public <T> DocumentUpdateEntity<T> updateDocument(
            final String key, final Object value, final DocumentUpdateOptions options, final Class<T> returnType) {
        return executorSync().execute(updateDocumentRequest(key, value, options),
                constructParametricType(DocumentUpdateEntity.class, returnType));
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(RawData values) {
        return executorSync()
                .execute(updateDocumentsRequest(values, new DocumentUpdateOptions()),
                        updateDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultiDocumentEntity<DocumentUpdateEntity<RawData>> updateDocuments(RawData values,
                                                                              DocumentUpdateOptions options) {
        return executorSync()
                .execute(updateDocumentsRequest(values, options),
                        updateDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(final Iterable<?> values) {
        return updateDocuments(values, new DocumentUpdateOptions());
    }

    @Override
    public MultiDocumentEntity<DocumentUpdateEntity<Void>> updateDocuments(
            final Iterable<?> values, final DocumentUpdateOptions options) {
        return updateDocuments(values, options, Void.class);
    }

    @Override
    public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
            final Iterable<?> values, final DocumentUpdateOptions options, final Class<T> returnType) {
        return executorSync()
                .execute(updateDocumentsRequest(values, options), updateDocumentsResponseDeserializer(returnType));
    }

    @Override
    public DocumentDeleteEntity<Void> deleteDocument(final String key) {
        return deleteDocument(key, new DocumentDeleteOptions());
    }

    @Override
    public DocumentDeleteEntity<Void> deleteDocument(String key, DocumentDeleteOptions options) {
        return deleteDocument(key, options, Void.class);
    }

    @Override
    public <T> DocumentDeleteEntity<T> deleteDocument(
            final String key, final DocumentDeleteOptions options, final Class<T> type) {
        return executorSync().execute(deleteDocumentRequest(key, options),
                constructParametricType(DocumentDeleteEntity.class, type));
    }

    @Override
    public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(RawData values) {
        return executorSync().execute(deleteDocumentsRequest(values, new DocumentDeleteOptions()),
                deleteDocumentsResponseDeserializer(Void.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultiDocumentEntity<DocumentDeleteEntity<RawData>> deleteDocuments(RawData values,
                                                                              DocumentDeleteOptions options) {
        return executorSync().execute(deleteDocumentsRequest(values, options),
                deleteDocumentsResponseDeserializer((Class<RawData>) values.getClass()));
    }

    @Override
    public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(final Iterable<?> values) {
        return deleteDocuments(values, new DocumentDeleteOptions());
    }

    @Override
    public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(
            final Iterable<?> values, final DocumentDeleteOptions options) {
        return deleteDocuments(values, options, Void.class);
    }

    @Override
    public <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
            final Iterable<?> values, final DocumentDeleteOptions options, final Class<T> type) {
        return executorSync().execute(deleteDocumentsRequest(values, options), deleteDocumentsResponseDeserializer(type));
    }

    @Override
    public Boolean documentExists(final String key) {
        return documentExists(key, new DocumentExistsOptions());
    }

    @Override
    public Boolean documentExists(final String key, final DocumentExistsOptions options) {
        try {
            executorSync().execute(documentExistsRequest(key, options), Void.class);
            return true;
        } catch (final ArangoDBException e) {

            // handle Response: 404, Error: 1655 - transaction not found
            if (e.getErrorNum() != null && e.getErrorNum() == 1655) {
                throw e;
            }

            if ((e.getResponseCode() != null &&
                    (e.getResponseCode() == 404 || e.getResponseCode() == 304 || e.getResponseCode() == 412))) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public IndexEntity getIndex(final String id) {
        return executorSync().execute(getIndexRequest(id), IndexEntity.class);
    }

    @Override
    public InvertedIndexEntity getInvertedIndex(String id) {
        return executorSync().execute(getIndexRequest(id), InvertedIndexEntity.class);
    }

    @Override
    public String deleteIndex(final String id) {
        return executorSync().execute(deleteIndexRequest(id), deleteIndexResponseDeserializer());
    }

    @Override
    public IndexEntity ensurePersistentIndex(final Iterable<String> fields, final PersistentIndexOptions options) {
        return executorSync().execute(createPersistentIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public InvertedIndexEntity ensureInvertedIndex(final InvertedIndexOptions options) {
        return executorSync().execute(createInvertedIndexRequest(options), InvertedIndexEntity.class);
    }

    @Override
    public IndexEntity ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options) {
        return executorSync().execute(createGeoIndexRequest(fields, options), IndexEntity.class);
    }

    @Deprecated
    @Override
    public IndexEntity ensureFulltextIndex(final Iterable<String> fields, final FulltextIndexOptions options) {
        return executorSync().execute(createFulltextIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public IndexEntity ensureTtlIndex(final Iterable<String> fields, final TtlIndexOptions options) {
        return executorSync().execute(createTtlIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public IndexEntity ensureZKDIndex(final Iterable<String> fields, final ZKDIndexOptions options) {
        return executorSync().execute(createZKDIndexRequest(fields, options), IndexEntity.class);
    }

    @Override
    public Collection<IndexEntity> getIndexes() {
        return executorSync().execute(getIndexesRequest(), getIndexesResponseDeserializer());
    }

    @Override
    public Collection<InvertedIndexEntity> getInvertedIndexes() {
        return executorSync().execute(getIndexesRequest(), getInvertedIndexesResponseDeserializer());
    }

    @Override
    public boolean exists() {
        try {
            getInfo();
            return true;
        } catch (final ArangoDBException e) {
            if (ArangoErrors.ERROR_ARANGO_DATA_SOURCE_NOT_FOUND.equals(e.getErrorNum())) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public CollectionEntity truncate() {
        return truncate(null);
    }

    @Override
    public CollectionEntity truncate(CollectionTruncateOptions options) {
        return executorSync().execute(truncateRequest(options), CollectionEntity.class);
    }

    @Override
    public CollectionPropertiesEntity count() {
        return count(null);
    }

    @Override
    public CollectionPropertiesEntity count(CollectionCountOptions options) {
        return executorSync().execute(countRequest(options), CollectionPropertiesEntity.class);
    }

    @Override
    public CollectionEntity create() {
        return db().createCollection(name());
    }

    @Override
    public CollectionEntity create(final CollectionCreateOptions options) {
        return db().createCollection(name(), options);
    }

    @Override
    public void drop() {
        executorSync().execute(dropRequest(null), Void.class);
    }

    @Override
    public void drop(final boolean isSystem) {
        executorSync().execute(dropRequest(isSystem), Void.class);
    }

    @Override
    public CollectionEntity getInfo() {
        return executorSync().execute(getInfoRequest(), CollectionEntity.class);
    }

    @Override
    public CollectionPropertiesEntity getProperties() {
        return executorSync().execute(getPropertiesRequest(), CollectionPropertiesEntity.class);
    }

    @Override
    public CollectionPropertiesEntity changeProperties(final CollectionPropertiesOptions options) {
        return executorSync().execute(changePropertiesRequest(options), CollectionPropertiesEntity.class);
    }

    @Override
    public CollectionEntity rename(final String newName) {
        return executorSync().execute(renameRequest(newName), CollectionEntity.class);
    }

    @Override
    public ShardEntity getResponsibleShard(final Object value) {
        return executorSync().execute(responsibleShardRequest(value), ShardEntity.class);
    }

    @Override
    public CollectionRevisionEntity getRevision() {
        return executorSync().execute(getRevisionRequest(), CollectionRevisionEntity.class);
    }

    @Override
    public void grantAccess(final String user, final Permissions permissions) {
        executorSync().execute(grantAccessRequest(user, permissions), Void.class);
    }

    @Override
    public void revokeAccess(final String user) {
        executorSync().execute(grantAccessRequest(user, Permissions.NONE), Void.class);
    }

    @Override
    public void resetAccess(final String user) {
        executorSync().execute(resetAccessRequest(user), Void.class);
    }

    @Override
    public Permissions getPermissions(final String user) {
        return executorSync().execute(getPermissionsRequest(user), getPermissionsResponseDeserialzer());
    }

}

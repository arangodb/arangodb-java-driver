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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDBException;
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
import com.arangodb.internal.velocystream.internal.ConnectionSync;
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
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoCollectionImpl
		extends InternalArangoCollection<ArangoDBImpl, ArangoDatabaseImpl, ArangoExecutorSync, Response, ConnectionSync>
		implements ArangoCollection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoCollection.class);

	protected ArangoCollectionImpl(final ArangoDatabaseImpl db, final String name) {
		super(db, name);
	}

	@Override
	public <T> DocumentCreateEntity<T> insertDocument(final T value) throws ArangoDBException {
		return executor.execute(insertDocumentRequest(value, new DocumentCreateOptions()),
			insertDocumentResponseDeserializer(value));
	}

	@Override
	public <T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options)
			throws ArangoDBException {
		return executor.execute(insertDocumentRequest(value, options), insertDocumentResponseDeserializer(value));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentCreateOptions params = new DocumentCreateOptions();
		return executor.execute(insertDocumentsRequest(values, params),
			insertDocumentsResponseDeserializer(values, params));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
		final Collection<T> values,
		final DocumentCreateOptions options) throws ArangoDBException {
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		return executor.execute(insertDocumentsRequest(values, params),
			insertDocumentsResponseDeserializer(values, params));
	}

	@Override
	public DocumentImportEntity importDocuments(final Collection<?> values) throws ArangoDBException {
		return importDocuments(values, new DocumentImportOptions());
	}

	@Override
	public DocumentImportEntity importDocuments(final Collection<?> values, final DocumentImportOptions options)
			throws ArangoDBException {
		return executor.execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
	}

	@Override
	public DocumentImportEntity importDocuments(final String values) throws ArangoDBException {
		return executor.execute(importDocumentsRequest(values, new DocumentImportOptions()),
			DocumentImportEntity.class);
	}

	@Override
	public DocumentImportEntity importDocuments(final String values, final DocumentImportOptions options)
			throws ArangoDBException {
		return executor.execute(importDocumentsRequest(values, options), DocumentImportEntity.class);
	}

	@Override
	public <T> T getDocument(final String key, final Class<T> type) throws ArangoDBException {
		return getDocument(key, type, new DocumentReadOptions());
	}

	@Override
	public <T> T getDocument(final String key, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		executor.validateDocumentKey(key);
		try {
			return executor.execute(getDocumentRequest(key, options), type);
		} catch (final ArangoDBException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
			if ((e.getResponseCode() != null && (e.getResponseCode().intValue() == 404
					|| e.getResponseCode().intValue() == 304 || e.getResponseCode().intValue() == 412))
					&& (options == null || options.isCatchException())) {
				return null;
			}
			throw e;
		}
	}

	@Override
	public <T> MultiDocumentEntity<T> getDocuments(final Collection<String> keys, final Class<T> type)
			throws ArangoDBException {
		final DocumentReadOptions options = new DocumentReadOptions();
		return executor.execute(getDocumentsRequest(keys, options), getDocumentsResponseDeserializer(type, options));
	}

	@Override
	public <T> DocumentUpdateEntity<T> replaceDocument(final String key, final T value) throws ArangoDBException {
		return executor.execute(replaceDocumentRequest(key, value, new DocumentReplaceOptions()),
			replaceDocumentResponseDeserializer(value));
	}

	@Override
	public <T> DocumentUpdateEntity<T> replaceDocument(
		final String key,
		final T value,
		final DocumentReplaceOptions options) throws ArangoDBException {
		return executor.execute(replaceDocumentRequest(key, value, options),
			replaceDocumentResponseDeserializer(value));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentReplaceOptions params = new DocumentReplaceOptions();
		return executor.execute(replaceDocumentsRequest(values, params),
			replaceDocumentsResponseDeserializer(values, params));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
		final Collection<T> values,
		final DocumentReplaceOptions options) throws ArangoDBException {
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		return executor.execute(replaceDocumentsRequest(values, params),
			replaceDocumentsResponseDeserializer(values, params));
	}

	@Override
	public <T> DocumentUpdateEntity<T> updateDocument(final String key, final T value) throws ArangoDBException {
		return executor.execute(updateDocumentRequest(key, value, new DocumentUpdateOptions()),
			updateDocumentResponseDeserializer(value));
	}

	@Override
	public <T> DocumentUpdateEntity<T> updateDocument(
		final String key,
		final T value,
		final DocumentUpdateOptions options) throws ArangoDBException {
		return executor.execute(updateDocumentRequest(key, value, options), updateDocumentResponseDeserializer(value));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentUpdateOptions params = new DocumentUpdateOptions();
		return executor.execute(updateDocumentsRequest(values, params),
			updateDocumentsResponseDeserializer(values, params));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
		final Collection<T> values,
		final DocumentUpdateOptions options) throws ArangoDBException {
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		return executor.execute(updateDocumentsRequest(values, params),
			updateDocumentsResponseDeserializer(values, params));
	}

	@Override
	public DocumentDeleteEntity<Void> deleteDocument(final String key) throws ArangoDBException {
		return executor.execute(deleteDocumentRequest(key, new DocumentDeleteOptions()),
			deleteDocumentResponseDeserializer(Void.class));
	}

	@Override
	public <T> DocumentDeleteEntity<T> deleteDocument(
		final String key,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executor.execute(deleteDocumentRequest(key, options), deleteDocumentResponseDeserializer(type));
	}

	@Override
	public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(final Collection<?> values)
			throws ArangoDBException {
		return executor.execute(deleteDocumentsRequest(values, new DocumentDeleteOptions()),
			deleteDocumentsResponseDeserializer(Void.class));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
		final Collection<?> values,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executor.execute(deleteDocumentsRequest(values, options), deleteDocumentsResponseDeserializer(type));
	}

	@Override
	public Boolean documentExists(final String key) {
		return documentExists(key, new DocumentExistsOptions());
	}

	@Override
	public Boolean documentExists(final String key, final DocumentExistsOptions options) throws ArangoDBException {
		try {
			executor.execute(documentExistsRequest(key, options), VPackSlice.class);
			return true;
		} catch (final ArangoDBException e) {
			if ((e.getResponseCode() != null && (e.getResponseCode().intValue() == 404
					|| e.getResponseCode().intValue() == 304 || e.getResponseCode().intValue() == 412))
					&& (options == null || options.isCatchException())) {
				return false;
			}
			throw e;
		}
	}

	@Override
	public IndexEntity getIndex(final String id) throws ArangoDBException {
		return executor.execute(getIndexRequest(id), IndexEntity.class);
	}

	@Override
	public String deleteIndex(final String id) throws ArangoDBException {
		return executor.execute(deleteIndexRequest(id), deleteIndexResponseDeserializer());
	}

	@Override
	public IndexEntity ensureHashIndex(final Iterable<String> fields, final HashIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createHashIndexRequest(fields, options), IndexEntity.class);
	}

	@Override
	public IndexEntity ensureSkiplistIndex(final Iterable<String> fields, final SkiplistIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createSkiplistIndexRequest(fields, options), IndexEntity.class);
	}

	@Override
	public IndexEntity ensurePersistentIndex(final Iterable<String> fields, final PersistentIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createPersistentIndexRequest(fields, options), IndexEntity.class);
	}

	@Override
	public IndexEntity ensureGeoIndex(final Iterable<String> fields, final GeoIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createGeoIndexRequest(fields, options), IndexEntity.class);
	}

	@Override
	public IndexEntity ensureFulltextIndex(final Iterable<String> fields, final FulltextIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createFulltextIndexRequest(fields, options), IndexEntity.class);
	}

	@Override
	public Collection<IndexEntity> getIndexes() throws ArangoDBException {
		return executor.execute(getIndexesRequest(), getIndexesResponseDeserializer());
	}

	@Override
	public boolean exists() throws ArangoDBException {
		try {
			getInfo();
			return true;
		} catch (final ArangoDBException e) {
			return false;
		}
	}

	@Override
	public CollectionEntity truncate() throws ArangoDBException {
		return executor.execute(truncateRequest(), CollectionEntity.class);
	}

	@Override
	public CollectionPropertiesEntity count() throws ArangoDBException {
		return executor.execute(countRequest(), CollectionPropertiesEntity.class);
	}

	@Override
	public CollectionEntity create() throws ArangoDBException {
		return db().createCollection(name());
	}

	@Override
	public CollectionEntity create(final CollectionCreateOptions options) throws ArangoDBException {
		return db().createCollection(name(), options);
	}

	@Override
	public void drop() throws ArangoDBException {
		executor.execute(dropRequest(null), Void.class);
	}

	@Override
	public void drop(final boolean isSystem) throws ArangoDBException {
		executor.execute(dropRequest(isSystem), Void.class);
	}

	@Override
	public CollectionEntity load() throws ArangoDBException {
		return executor.execute(loadRequest(), CollectionEntity.class);
	}

	@Override
	public CollectionEntity unload() throws ArangoDBException {
		return executor.execute(unloadRequest(), CollectionEntity.class);
	}

	@Override
	public CollectionEntity getInfo() throws ArangoDBException {
		return executor.execute(getInfoRequest(), CollectionEntity.class);
	}

	@Override
	public CollectionPropertiesEntity getProperties() throws ArangoDBException {
		return executor.execute(getPropertiesRequest(), CollectionPropertiesEntity.class);
	}

	@Override
	public CollectionPropertiesEntity changeProperties(final CollectionPropertiesOptions options)
			throws ArangoDBException {
		return executor.execute(changePropertiesRequest(options), CollectionPropertiesEntity.class);
	}

	@Override
	public CollectionEntity rename(final String newName) throws ArangoDBException {
		return executor.execute(renameRequest(newName), CollectionEntity.class);
	}

	@Override
	public CollectionRevisionEntity getRevision() throws ArangoDBException {
		return executor.execute(getRevisionRequest(), CollectionRevisionEntity.class);
	}

	@Override
	public void grantAccess(final String user, final Permissions permissions) throws ArangoDBException {
		executor.execute(grantAccessRequest(user, permissions), Void.class);
	}

	@Override
	public void revokeAccess(final String user) throws ArangoDBException {
		executor.execute(grantAccessRequest(user, Permissions.NONE), Void.class);
	}

	@Override
	public void resetAccess(final String user) throws ArangoDBException {
		executor.execute(resetAccessRequest(user), Void.class);
	}

	@Override
	public Permissions getPermissions(final String user) throws ArangoDBException {
		return executor.execute(getPermissionsRequest(user), getPermissionsResponseDeserialzer());
	}

}

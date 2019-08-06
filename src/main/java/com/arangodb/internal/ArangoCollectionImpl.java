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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.model.*;
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
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark Vollmary
 *
 */
public class ArangoCollectionImpl extends InternalArangoCollection<ArangoDBImpl, ArangoDatabaseImpl, ArangoExecutorSync>
		implements ArangoCollection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoCollection.class);

	protected ArangoCollectionImpl(final ArangoDatabaseImpl db, final String name) {
		super(db, name);
	}

	@Override
	public <T> DocumentCreateEntity<T> insertDocument(final T value) throws ArangoDBException {
		return insertDocument(value, new DocumentCreateOptions());
	}

	@Override
	public <T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options)
			throws ArangoDBException {
		return executor.execute(insertDocumentRequest(value, options),
			insertDocumentResponseDeserializer(value, options));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(final Collection<T> values)
			throws ArangoDBException {
				return insertDocuments(values, new DocumentCreateOptions());
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
	public Collection<DocumentImportEntity> importDocuments(Collection<?> values, DocumentImportOptions options,
															int batchSize, int numThreads) throws ArangoDBException {

		List<? extends List<?>> batches = ListUtils.partition(new ArrayList<>(values), batchSize);
		LOGGER.info("Partitioned [{}] values into [{}] batches of at most [{}] in size.",
				values.size(), batches.size(), batchSize);

		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		LOGGER.info("Created fixed thread pool of [{}] threads.", numThreads);

		LOGGER.info("Importing documents with: {}", options.toJSONString());

		long startTimeMillis = System.currentTimeMillis();
		LOGGER.info("Started importing batches into collection: [{}].", this.name);

		int batchNumber = 1;
		List<CompletableFuture<DocumentImportEntity>> completableFutureList = new ArrayList<>();
		for (List<?> batch : batches) {
			LOGGER.info("Started/queued import of batch: [{}/{}] of size: [{}].", batchNumber, batches.size(), batch.size());
			CompletableFuture<DocumentImportEntity> completableFuture = CompletableFuture.supplyAsync(() -> {
				DocumentImportEntity documentImportEntity = importDocuments(batch, options);
				return documentImportEntity;
			}, executorService);
			completableFutureList.add(completableFuture);
			batchNumber++;
		}

		int combinedCreated = 0;
		int combinedErrors = 0;
		int combinedUpdated = 0;
		int combinedIgnored = 0;
		int combinedEmpty = 0;
		Collection<String> combinedDetails = new ArrayList<>();

		int completedBatchNumber = 1;
		List<DocumentImportEntity> documentImportEntityList = new ArrayList<>();
		for (CompletableFuture<DocumentImportEntity> completableFuture : completableFutureList) {
			DocumentImportEntity documentImportEntity = null;
			try {
				documentImportEntity = completableFuture.get();
			} catch (InterruptedException | ExecutionException e) {
				executorService.shutdown();
				throw new ArangoDBException(e);
			}
			documentImportEntityList.add(documentImportEntity);
			LOGGER.info("Completed import of batch: [{}/{}]. {}", completedBatchNumber, batches.size(),
					documentImportEntity.toJSONString());

			combinedCreated += documentImportEntity.getCreated();
			combinedErrors += documentImportEntity.getErrors();
			combinedUpdated += documentImportEntity.getUpdated();
			combinedIgnored += documentImportEntity.getIgnored();
			combinedEmpty += documentImportEntity.getEmpty();
			combinedDetails.addAll(documentImportEntity.getDetails());

			completedBatchNumber++;
		}

		DocumentImportEntity combinedDocumentImportEntity = new DocumentImportEntity();
		combinedDocumentImportEntity.setCreated(combinedCreated);
		combinedDocumentImportEntity.setErrors(combinedErrors);
		combinedDocumentImportEntity.setUpdated(combinedUpdated);
		combinedDocumentImportEntity.setIgnored(combinedIgnored);
		combinedDocumentImportEntity.setEmpty(combinedEmpty);
		//combinedDocumentImportEntity.setDetails(combinedDetails);

		LOGGER.info("Finished importing batches into collection: [{}]. {}", this.name,
				combinedDocumentImportEntity.toJSONString());

		long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
		long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTimeMillis, TimeUnit.MILLISECONDS);
		int numDocumentsImported = combinedDocumentImportEntity.getCreated() + combinedDocumentImportEntity.getUpdated();
		if( elapsedTimeSeconds > 0) {
			String performance = String.format("%.2f", (float) numDocumentsImported / elapsedTimeSeconds);
			LOGGER.info("Total number of documents imported (created + updated): [{}]  Time taken: [{}] seconds" +
					"  Performance: [{}] documents/second.", numDocumentsImported, elapsedTimeSeconds, performance);
		} else {
			String performance = String.format("%.2f", (float) numDocumentsImported / elapsedTimeMillis);
			LOGGER.info("Total number of documents imported (created + updated): [{}]  Time taken: [{}] milliseconds" +
					"  Performance: [{}] documents/millisecond.", numDocumentsImported, elapsedTimeMillis, performance);
		}

		executorService.shutdown();
		LOGGER.info("Shutdown fixed thread pool of [{}] threads.", numThreads);

		// It would be nice to return a combinedDocumentImportEntity, but since details reflect the position of
		// documents within each batch, such as when reporting errors as shown below it doesn't seem like a good idea to
		// return a combinedDocumentImportEntity unless the positions can be adjusted to reflect the positions in the
		// original Collection<?> values.
		// [at position 9: creating document failed with error 'unique constraint violated', offending document: {"_key":"9"}]
		// [at position 9: creating document failed with error 'unique constraint violated', offending document: {"_key":"19"}]
		return documentImportEntityList;
	}

	@Override
	public DocumentImportEntity importDocuments(final String values) throws ArangoDBException {
		return importDocuments(values, new DocumentImportOptions());
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
		DocumentUtil.validateDocumentKey(key);
		try {
			return executor.execute(getDocumentRequest(key, options), type);
		} catch (final ArangoDBException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
			if ((e.getResponseCode() != null && (e.getResponseCode() == 404
					|| e.getResponseCode() == 304 || e.getResponseCode() == 412))
					&& (options == null || options.isCatchException())) {
				return null;
			}
			throw e;
		}
	}

	@Override
	public <T> MultiDocumentEntity<T> getDocuments(final Collection<String> keys, final Class<T> type)
			throws ArangoDBException {
		return getDocuments(keys, type, new DocumentReadOptions());
	}

	@Override
	public <T> MultiDocumentEntity<T> getDocuments(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentReadOptions options) throws ArangoDBException {
		return executor.execute(getDocumentsRequest(keys, options), getDocumentsResponseDeserializer(type, options));
	}

	@Override
	public <T> DocumentUpdateEntity<T> replaceDocument(final String key, final T value) throws ArangoDBException {
		return replaceDocument(key, value, new DocumentReplaceOptions());
	}

	@Override
	public <T> DocumentUpdateEntity<T> replaceDocument(
		final String key,
		final T value,
		final DocumentReplaceOptions options) throws ArangoDBException {
		return executor.execute(replaceDocumentRequest(key, value, options),
			replaceDocumentResponseDeserializer(value, options));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(final Collection<T> values)
			throws ArangoDBException {
		return replaceDocuments(values, new DocumentReplaceOptions());
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
		return updateDocument(key, value, new DocumentUpdateOptions());
	}

	@Override
	public <T> DocumentUpdateEntity<T> updateDocument(
		final String key,
		final T value,
		final DocumentUpdateOptions options) throws ArangoDBException {
		return executor.execute(updateDocumentRequest(key, value, options),
			updateDocumentResponseDeserializer(value, options));
	}

	@Override
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(final Collection<T> values)
			throws ArangoDBException {
				return updateDocuments(values, new DocumentUpdateOptions());
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
			if ((e.getResponseCode() != null && (e.getResponseCode() == 404
					|| e.getResponseCode() == 304 || e.getResponseCode() == 412))
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
	public IndexEntity ensureTtlIndex(final Iterable<String> fields, final TtlIndexOptions options)
			throws ArangoDBException {
		return executor.execute(createTtlIndexRequest(fields, options), IndexEntity.class);
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
			if (ArangoErrors.ERROR_ARANGO_DATA_SOURCE_NOT_FOUND.equals(e.getErrorNum())) {
				return false;
			}
			throw e;
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
	public synchronized CollectionEntity rename(final String newName) throws ArangoDBException {
		final CollectionEntity result = executor.execute(renameRequest(newName), CollectionEntity.class);
		name = result.getName();
		return result;
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

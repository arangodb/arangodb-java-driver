package com.arangodb.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.CollectionPropertiesResult;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.DocumentDeleteResult;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentUpdateResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollection extends Executeable {

	private final DB db;
	private final String name;

	protected DBCollection(final DB db, final String name) {
		super(db.communication(), db.vpack(), db.vpackNull(), db.vpackParser(), db.documentCache(),
				db.collectionCache());
		this.db = db;
		this.name = name;
	}

	protected DB db() {
		return db;
	}

	protected String name() {
		return name;
	}

	private String createDocumentHandle(final String key) {
		validateDocumentKey(key);
		return createPath(name, key);
	}

	public <T> DocumentCreateResult<T> insert(final T value, final DocumentCreate.Options options)
			throws ArangoDBException {
		return unwrap(insertAsync(value, options));
	}

	public <T> CompletableFuture<DocumentCreateResult<T>> insertAsync(
		final T value,
		final DocumentCreate.Options options) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentCreate params = (options != null ? options : new DocumentCreate.Options()).build();
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(value));
		return execute(request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentCreateResult<T> doc = deserialize(body, DocumentCreateResult.class);
			final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
			if (newDoc.isObject()) {
				doc.setNew(deserialize(newDoc, value.getClass()));
			}
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.ID, doc.getId());
			values.put(DocumentField.Type.KEY, doc.getKey());
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		});
	}

	public <T> Collection<DocumentCreateResult<T>> insert(
		final Collection<T> values,
		final DocumentCreate.Options options) throws ArangoDBException {
		return unwrap(insertAsync(values, options));
	}

	@SuppressWarnings("unchecked")
	public <T> CompletableFuture<Collection<DocumentCreateResult<T>>> insertAsync(
		final Collection<T> values,
		final DocumentCreate.Options options) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentCreate params = (options != null ? options : new DocumentCreate.Options()).build();
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(values));
		return execute(request, response -> {
			Class<T> type = null;
			if (params.getReturnNew() != null && params.getReturnNew()) {
				final Optional<T> first = values.stream().findFirst();
				if (first.isPresent()) {
					type = (Class<T>) first.get().getClass();
				}
			}
			final Collection<DocumentCreateResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.iterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentCreateResult<T> doc = deserialize(next, DocumentCreateResult.class);
				final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew(deserialize(newDoc, type));
				}
				docs.add(doc);
			}
			return docs;
		});
	}

	public <T> T read(final String key, final Class<T> type, final DocumentRead.Options options)
			throws ArangoDBException {
		return unwrap(readAsync(key, type, options));
	}

	public <T> CompletableFuture<T> readAsync(
		final String key,
		final Class<T> type,
		final DocumentRead.Options options) {
		final Request request = new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentRead params = (options != null ? options : new DocumentRead.Options()).build();
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(type, request);
	}

	public <T> DocumentUpdateResult<T> replace(final String key, final T value, final DocumentReplace.Options options)
			throws ArangoDBException {
		return unwrap(replaceAsync(key, value, options));
	}

	public <T> CompletableFuture<DocumentUpdateResult<T>> replaceAsync(
		final String key,
		final T value,
		final DocumentReplace.Options options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentReplace params = (options != null ? options : new DocumentReplace.Options()).build();
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return execute(request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentUpdateResult<T> doc = deserialize(body, DocumentUpdateResult.class);
			final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
			if (newDoc.isObject()) {
				doc.setNew(deserialize(newDoc, value.getClass()));
			}
			final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
			if (oldDoc.isObject()) {
				doc.setOld(deserialize(oldDoc, value.getClass()));
			}
			final Map<DocumentField.Type, String> values = new HashMap<>();
			values.put(DocumentField.Type.REV, doc.getRev());
			documentCache.setValues(value, values);
			return doc;
		});
	}

	public <T> DocumentUpdateResult<T> update(final String key, final T value, final DocumentUpdate.Options options)
			throws ArangoDBException {
		return unwrap(updateAsync(key, value, options));
	}

	public <T> CompletableFuture<DocumentUpdateResult<T>> updateAsync(
		final String key,
		final T value,
		final DocumentUpdate.Options options) {
		final Request request = new Request(db.name(), RequestType.PATCH,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentUpdate params = (options != null ? options : new DocumentUpdate.Options()).build();
		final Boolean keepNull = params.getKeepNull();
		request.putParameter(ArangoDBConstants.KEEP_NULL, keepNull);
		request.putParameter(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return execute(request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentUpdateResult<T> doc = deserialize(body, DocumentUpdateResult.class);
			final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
			if (newDoc.isObject()) {
				doc.setNew(deserialize(newDoc, value.getClass()));
			}
			final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
			if (oldDoc.isObject()) {
				doc.setOld(deserialize(oldDoc, value.getClass()));
			}
			return doc;
		});
	}

	public <T> DocumentDeleteResult<T> delete(
		final String key,
		final Class<T> type,
		final DocumentDelete.Options options) throws ArangoDBException {
		return unwrap(deleteAsync(key, type, options));
	}

	public <T> CompletableFuture<DocumentDeleteResult<T>> deleteAsync(
		final String key,
		final Class<T> type,
		final DocumentDelete.Options options) {
		final Request request = new Request(db.name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentDelete params = (options != null ? options : new DocumentDelete.Options()).build();
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentDeleteResult<T> doc = deserialize(body, DocumentDeleteResult.class);
			final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
			if (oldDoc.isObject()) {
				doc.setOld(deserialize(oldDoc, type));
			}
			return doc;
		});
	}

	public <T> Collection<DocumentDeleteResult<T>> delete(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentDelete.Options options) throws ArangoDBException {
		return unwrap(deleteAsync(keys, type, options));
	}

	public <T> CompletableFuture<Collection<DocumentDeleteResult<T>>> deleteAsync(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentDelete.Options options) {
		final Request request = new Request(db.name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentDelete params = (options != null ? options : new DocumentDelete.Options()).build();
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.setBody(serialize(keys));
		return execute(request, response -> {
			final Collection<DocumentDeleteResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.iterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentDeleteResult<T> doc = deserialize(next, DocumentDeleteResult.class);
				final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld(deserialize(oldDoc, type));
				}
				docs.add(doc);
			}
			return docs;
		});
	}

	public Boolean documentExists(final String key, final DocumentExists.Options options) throws ArangoDBException {
		return unwrap(documentExistsAsync(key, options));
	}

	public CompletableFuture<Boolean> documentExistsAsync(final String key, final DocumentExists.Options options) {
		final Request request = new Request(db.name(), RequestType.HEAD,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentExists params = (options != null ? options : new DocumentExists.Options()).build();
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		final CompletableFuture<Boolean> result = new CompletableFuture<>();
		communication.execute(request).whenComplete((response, ex) -> {
			if (response != null) {
				result.complete(true);
			} else if (ex != null) {
				result.complete(false);
			} else {
				result.cancel(true);
			}
		});
		return result;
	}

	public IndexResult createHashIndex(final Collection<String> fields, final HashIndex.Options options)
			throws ArangoDBException {
		return unwrap(createHashIndexAsync(fields, options));
	}

	public CompletableFuture<IndexResult> createHashIndexAsync(
		final Collection<String> fields,
		final HashIndex.Options options) {
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize((options != null ? options : new HashIndex.Options()).build(fields)));
		return execute(IndexResult.class, request);
	}

	public IndexResult createSkiplistIndex(final Collection<String> fields, final SkiplistIndex.Options options)
			throws ArangoDBException {
		return unwrap(createSkiplistIndexAsync(fields, options));
	}

	public CompletableFuture<IndexResult> createSkiplistIndexAsync(
		final Collection<String> fields,
		final SkiplistIndex.Options options) {
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize((options != null ? options : new SkiplistIndex.Options()).build(fields)));
		return execute(IndexResult.class, request);
	}

	public IndexResult createPersistentIndex(final Collection<String> fields, final PersistentIndex.Options options)
			throws ArangoDBException {
		return unwrap(createPersistentIndexAsync(fields, options));
	}

	public CompletableFuture<IndexResult> createPersistentIndexAsync(
		final Collection<String> fields,
		final PersistentIndex.Options options) {
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize((options != null ? options : new PersistentIndex.Options()).build(fields)));
		return execute(IndexResult.class, request);
	}

	public IndexResult createGeoIndex(final Collection<String> fields, final GeoIndex.Options options)
			throws ArangoDBException {
		return unwrap(createGeoIndexAsync(fields, options));
	}

	public CompletableFuture<IndexResult> createGeoIndexAsync(
		final Collection<String> fields,
		final GeoIndex.Options options) {
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize((options != null ? options : new GeoIndex.Options()).build(fields)));
		return execute(IndexResult.class, request);
	}

	public Collection<IndexResult> readIndexes() throws ArangoDBException {
		return unwrap(readIndexesAsync());
	}

	public CompletableFuture<Collection<IndexResult>> readIndexesAsync() {
		final Request request = new Request(db.name(), RequestType.GET, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		return execute(request,
			response -> deserialize(response.getBody().get().get("indexes"), new Type<Collection<IndexResult>>() {
			}.getType()));
	}

	public CollectionResult truncate() throws ArangoDBException {
		return unwrap(truncateAsync());
	}

	public CompletableFuture<CollectionResult> truncateAsync() {
		return execute(CollectionResult.class, new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, "truncate")));
	}

	public CollectionPropertiesResult count() throws ArangoDBException {
		return unwrap(countAsync());
	}

	public CompletableFuture<CollectionPropertiesResult> countAsync() {
		return execute(CollectionPropertiesResult.class,
			new Request(db.name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_COLLECTION, name, "count")));
	}

	public void drop() throws ArangoDBException {
		unwrap(dropAsync());
	}

	public CompletableFuture<Void> dropAsync() {
		validateCollectionName(name);
		return execute(Void.class,
			new Request(db.name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_COLLECTION, name)));
	}
}

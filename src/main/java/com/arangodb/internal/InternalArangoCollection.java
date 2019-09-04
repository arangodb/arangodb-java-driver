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

package com.arangodb.internal;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.util.ArangoSerializationFactory.Serializer;
import com.arangodb.internal.util.DocumentUtil;
import com.arangodb.internal.util.RequestUtils;
import com.arangodb.model.*;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;

import java.util.*;

/**
 * @author Mark Vollmary
 * @author Michele Rastelli
 */
public abstract class InternalArangoCollection<A extends InternalArangoDB<E>, D extends InternalArangoDatabase<A, E>, E extends ArangoExecutor>
		extends ArangoExecuteable<E> {

	private static final String COLLECTION = "collection";

	protected static final String PATH_API_COLLECTION = "/_api/collection";
	private static final String PATH_API_DOCUMENT = "/_api/document";
	private static final String PATH_API_INDEX = "/_api/index";
	private static final String PATH_API_IMPORT = "/_api/import";
	private static final String PATH_API_USER = "/_api/user";

	private static final String MERGE_OBJECTS = "mergeObjects";
	private static final String IGNORE_REVS = "ignoreRevs";
	private static final String RETURN_NEW = "returnNew";
	private static final String NEW = "new";
	private static final String RETURN_OLD = "returnOld";
	private static final String OVERWRITE = "overwrite";
	private static final String OLD = "old";
	private static final String SILENT = "silent";

	private static final String TRANSACTION_ID = "x-arango-trx-id";

	private final D db;
	protected volatile String name;

	protected InternalArangoCollection(final D db, final String name) {
		super(db.executor, db.util, db.context);
		this.db = db;
		this.name = name;
	}

	public D db() {
		return db;
	}

	public String name() {
		return name;
	}

	protected <T> Request insertDocumentRequest(final T value, final DocumentCreateOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_DOCUMENT, name);
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.putQueryParam(OVERWRITE, params.getOverwrite());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentCreateEntity<T>> insertDocumentResponseDeserializer(
			final T value, final DocumentCreateOptions options) {
        return response -> {
            final VPackSlice body = response.getBody();
            final DocumentCreateEntity<T> doc = util().deserialize(body, DocumentCreateEntity.class);
            final VPackSlice newDoc = body.get(NEW);
            if (newDoc.isObject()) {
                doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, value.getClass()));
            }
            final VPackSlice oldDoc = body.get(OLD);
            if (oldDoc.isObject()) {
                doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, value.getClass()));
            }
            if (options == null || Boolean.TRUE != options.getSilent()) {
                final Map<DocumentField.Type, String> values = new HashMap<>();
                values.put(DocumentField.Type.ID, doc.getId());
                values.put(DocumentField.Type.KEY, doc.getKey());
                values.put(DocumentField.Type.REV, doc.getRev());
                executor.documentCache().setValues(value, values);
            }
            return doc;
        };
	}

	protected <T> Request insertDocumentsRequest(final Collection<T> values, final DocumentCreateOptions params) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_DOCUMENT, name);
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.putQueryParam(OVERWRITE, params.getOverwrite());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.setBody(util(Serializer.CUSTOM)
				.serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocumentsResponseDeserializer(
			final Collection<T> values, final DocumentCreateOptions params) {
        return response -> {
            Class<T> type = null;
            if (Boolean.TRUE == params.getReturnNew()) {
                if (!values.isEmpty()) {
                    type = (Class<T>) values.iterator().next().getClass();
                }
            }
            final MultiDocumentEntity<DocumentCreateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentCreateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final VPackSlice body = response.getBody();
            if (body.isArray()) {
                for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext(); ) {
                    final VPackSlice next = iterator.next();
                    if (next.get(ArangoResponseField.ERROR).isTrue()) {
                        final ErrorEntity error = util().deserialize(next, ErrorEntity.class);
                        errors.add(error);
                        documentsAndErrors.add(error);
                    } else {
                        final DocumentCreateEntity<T> doc = util().deserialize(next, DocumentCreateEntity.class);
                        final VPackSlice newDoc = next.get(NEW);
                        if (newDoc.isObject()) {
                            doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, type));
                        }
                        final VPackSlice oldDoc = next.get(OLD);
                        if (oldDoc.isObject()) {
                            doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, type));
                        }
                        docs.add(doc);
                        documentsAndErrors.add(doc);
                    }
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
	}

	protected Request importDocumentsRequest(final String values, final DocumentImportOptions options) {
		return importDocumentsRequest(options).putQueryParam("type", ImportType.auto).setBody(util().serialize(values));
	}

	protected Request importDocumentsRequest(final Collection<?> values, final DocumentImportOptions options) {
		return importDocumentsRequest(options).putQueryParam("type", ImportType.list).setBody(util(Serializer.CUSTOM)
				.serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
	}

	protected Request importDocumentsRequest(final DocumentImportOptions options) {
		final DocumentImportOptions params = options != null ? options : new DocumentImportOptions();
		return request(db.name(), RequestType.POST, PATH_API_IMPORT).putQueryParam(COLLECTION, name)
				.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync())
				.putQueryParam("fromPrefix", params.getFromPrefix()).putQueryParam("toPrefix", params.getToPrefix())
				.putQueryParam(OVERWRITE, params.getOverwrite()).putQueryParam("onDuplicate", params.getOnDuplicate())
				.putQueryParam("complete", params.getComplete()).putQueryParam("details", params.getDetails());
	}

	protected Request getDocumentRequest(final String key, final DocumentReadOptions options) {
		final Request request = request(db.name(), RequestType.GET, PATH_API_DOCUMENT,
				DocumentUtil.createDocumentHandle(name, key));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		if (params.getAllowDirtyRead() == Boolean.TRUE) {
			RequestUtils.allowDirtyRead(request);
		}
		return request;
	}

	protected Request getDocumentsRequest(final Collection<String> keys, final DocumentReadOptions options) {
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		final Request request = request(db.name(), RequestType.PUT, PATH_API_DOCUMENT, name)
				.putQueryParam("onlyget", true)
				.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch())
				.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch()).setBody(util().serialize(keys))
				.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		if (params.getAllowDirtyRead() == Boolean.TRUE) {
			RequestUtils.allowDirtyRead(request);
		}
		return request;
	}

	protected <T> ResponseDeserializer<MultiDocumentEntity<T>> getDocumentsResponseDeserializer(
			final Class<T> type, final DocumentReadOptions options) {
        return response -> {
            final MultiDocumentEntity<T> multiDocument = new MultiDocumentEntity<>();
            final Collection<T> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final VPackSlice body = response.getBody();
            for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext(); ) {
                final VPackSlice next = iterator.next();
                if (next.get(ArangoResponseField.ERROR).isTrue()) {
                    final ErrorEntity error = util().deserialize(next, ErrorEntity.class);
                    errors.add(error);
                    documentsAndErrors.add(error);
                } else {
                    final T doc = util(Serializer.CUSTOM).deserialize(next, type);
                    docs.add(doc);
                    documentsAndErrors.add(doc);
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
	}

	protected <T> Request replaceDocumentRequest(
			final String key, final T value, final DocumentReplaceOptions options) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_DOCUMENT,
				DocumentUtil.createDocumentHandle(name, key));
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentUpdateEntity<T>> replaceDocumentResponseDeserializer(
			final T value, final DocumentReplaceOptions options) {
        return response -> {
            final VPackSlice body = response.getBody();
            final DocumentUpdateEntity<T> doc = util().deserialize(body, DocumentUpdateEntity.class);
            final VPackSlice newDoc = body.get(NEW);
            if (newDoc.isObject()) {
                doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, value.getClass()));
            }
            final VPackSlice oldDoc = body.get(OLD);
            if (oldDoc.isObject()) {
                doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, value.getClass()));
            }
            if (options == null || Boolean.TRUE != options.getSilent()) {
                final Map<DocumentField.Type, String> values = new HashMap<>();
                values.put(DocumentField.Type.REV, doc.getRev());
                executor.documentCache().setValues(value, values);
            }
            return doc;
        };
	}

	protected <T> Request replaceDocumentsRequest(final Collection<T> values, final DocumentReplaceOptions params) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_DOCUMENT, name);
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.setBody(util(Serializer.CUSTOM)
				.serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocumentsResponseDeserializer(
			final Collection<T> values, final DocumentReplaceOptions params) {
        return response -> {
            Class<T> type = null;
            if (Boolean.TRUE == params.getReturnNew() || Boolean.TRUE == params.getReturnOld()) {
                if (!values.isEmpty()) {
                    type = (Class<T>) values.iterator().next().getClass();
                }
            }
            final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final VPackSlice body = response.getBody();
            if (body.isArray()) {
                for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext(); ) {
                    final VPackSlice next = iterator.next();
                    if (next.get(ArangoResponseField.ERROR).isTrue()) {
                        final ErrorEntity error = util().deserialize(next, ErrorEntity.class);
                        errors.add(error);
                        documentsAndErrors.add(error);
                    } else {
                        final DocumentUpdateEntity<T> doc = util().deserialize(next, DocumentUpdateEntity.class);
                        final VPackSlice newDoc = next.get(NEW);
                        if (newDoc.isObject()) {
                            doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, type));
                        }
                        final VPackSlice oldDoc = next.get(OLD);
                        if (oldDoc.isObject()) {
                            doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, type));
                        }
                        docs.add(doc);
                        documentsAndErrors.add(doc);
                    }
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
	}

	protected <T> Request updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
		final Request request = request(db.name(), RequestType.PATCH, PATH_API_DOCUMENT,
				DocumentUtil.createDocumentHandle(name, key));
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.setBody(util(Serializer.CUSTOM).serialize(value, new ArangoSerializer.Options()
				.serializeNullValues(params.getSerializeNull() == null || params.getSerializeNull())));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentUpdateEntity<T>> updateDocumentResponseDeserializer(
			final T value, final DocumentUpdateOptions options) {
        return response -> {
            final VPackSlice body = response.getBody();
            final DocumentUpdateEntity<T> doc = util().deserialize(body, DocumentUpdateEntity.class);
            final VPackSlice newDoc = body.get(NEW);
            if (newDoc.isObject()) {
                doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, value.getClass()));
            }
            final VPackSlice oldDoc = body.get(OLD);
            if (oldDoc.isObject()) {
                doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, value.getClass()));
            }
            if (options == null || Boolean.TRUE != options.getSilent()) {
                final Map<DocumentField.Type, String> values = new HashMap<>();
                values.put(DocumentField.Type.REV, doc.getRev());
                executor.documentCache().setValues(value, values);
            }
            return doc;
        };
	}

	protected <T> Request updateDocumentsRequest(final Collection<T> values, final DocumentUpdateOptions params) {
		final Request request = request(db.name(), RequestType.PATCH, PATH_API_DOCUMENT, name);
		final Boolean keepNull = params.getKeepNull();
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.KEEP_NULL, keepNull);
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(RETURN_NEW, params.getReturnNew());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.setBody(util(Serializer.CUSTOM).serialize(values, new ArangoSerializer.Options()
				.serializeNullValues(params.getSerializeNull() == null || params.getSerializeNull())
				.stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocumentsResponseDeserializer(
			final Collection<T> values, final DocumentUpdateOptions params) {
        return response -> {
            Class<T> type = null;
            if (Boolean.TRUE == params.getReturnNew() || Boolean.TRUE == params.getReturnOld()) {
                if (!values.isEmpty()) {
                    type = (Class<T>) values.iterator().next().getClass();
                }
            }
            final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final VPackSlice body = response.getBody();
            if (body.isArray()) {
                for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext(); ) {
                    final VPackSlice next = iterator.next();
                    if (next.get(ArangoResponseField.ERROR).isTrue()) {
                        final ErrorEntity error = util().deserialize(next, ErrorEntity.class);
                        errors.add(error);
                        documentsAndErrors.add(error);
                    } else {
                        final DocumentUpdateEntity<T> doc = util().deserialize(next, DocumentUpdateEntity.class);
                        final VPackSlice newDoc = next.get(NEW);
                        if (newDoc.isObject()) {
                            doc.setNew(util(Serializer.CUSTOM).deserialize(newDoc, type));
                        }
                        final VPackSlice oldDoc = next.get(OLD);
                        if (oldDoc.isObject()) {
                            doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, type));
                        }
                        docs.add(doc);
                        documentsAndErrors.add(doc);
                    }
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
	}

	protected Request deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
		final Request request = request(db.name(), RequestType.DELETE, PATH_API_DOCUMENT,
				DocumentUtil.createDocumentHandle(name, key));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		return request;
	}

	protected <T> ResponseDeserializer<DocumentDeleteEntity<T>> deleteDocumentResponseDeserializer(
			final Class<T> type) {
        return response -> {
            final VPackSlice body = response.getBody();
            final DocumentDeleteEntity<T> doc = util().deserialize(body, DocumentDeleteEntity.class);
            final VPackSlice oldDoc = body.get(OLD);
            if (oldDoc.isObject()) {
                doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, type));
            }
            return doc;
        };
	}

	protected <T> Request deleteDocumentsRequest(final Collection<T> keys, final DocumentDeleteOptions options) {
		final Request request = request(db.name(), RequestType.DELETE, PATH_API_DOCUMENT, name);
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putQueryParam(ArangoRequestParam.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(RETURN_OLD, params.getReturnOld());
		request.putQueryParam(SILENT, params.getSilent());
		request.setBody(util().serialize(keys));
		return request;
	}

	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocumentsResponseDeserializer(
			final Class<T> type) {
        return response -> {
            final MultiDocumentEntity<DocumentDeleteEntity<T>> multiDocument = new MultiDocumentEntity<>();
            final Collection<DocumentDeleteEntity<T>> docs = new ArrayList<>();
            final Collection<ErrorEntity> errors = new ArrayList<>();
            final Collection<Object> documentsAndErrors = new ArrayList<>();
            final VPackSlice body = response.getBody();
            if (body.isArray()) {
                for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext(); ) {
                    final VPackSlice next = iterator.next();
                    if (next.get(ArangoResponseField.ERROR).isTrue()) {
                        final ErrorEntity error = util().deserialize(next, ErrorEntity.class);
                        errors.add(error);
                        documentsAndErrors.add(error);
                    } else {
                        final DocumentDeleteEntity<T> doc = util().deserialize(next, DocumentDeleteEntity.class);
                        final VPackSlice oldDoc = next.get(OLD);
                        if (oldDoc.isObject()) {
                            doc.setOld(util(Serializer.CUSTOM).deserialize(oldDoc, type));
                        }
                        docs.add(doc);
                        documentsAndErrors.add(doc);
                    }
                }
            }
            multiDocument.setDocuments(docs);
            multiDocument.setErrors(errors);
            multiDocument.setDocumentsAndErrors(documentsAndErrors);
            return multiDocument;
        };
	}

	protected Request documentExistsRequest(final String key, final DocumentExistsOptions options) {
		final Request request = request(db.name(), RequestType.HEAD, PATH_API_DOCUMENT,
				DocumentUtil.createDocumentHandle(name, key));
		final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		request.putHeaderParam(ArangoRequestParam.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(ArangoRequestParam.IF_NONE_MATCH, params.getIfNoneMatch());
		return request;
	}

	protected Request getIndexRequest(final String id) {
		return request(db.name(), RequestType.GET, PATH_API_INDEX, createIndexId(id));
	}

	protected Request deleteIndexRequest(final String id) {
		return request(db.name(), RequestType.DELETE, PATH_API_INDEX, createIndexId(id));
	}

	protected ResponseDeserializer<String> deleteIndexResponseDeserializer() {
        return response -> response.getBody().get("id").getAsString();
	}

	private String createIndexId(final String id) {
		final String index;
		if (id.matches(DocumentUtil.REGEX_ID)) {
			index = id;
		} else if (id.matches(DocumentUtil.REGEX_KEY)) {
			index = name + "/" + id;
		} else {
			throw new ArangoDBException(String.format("index id %s is not valid.", id));
		}
		return index;
	}

	protected Request createHashIndexRequest(final Iterable<String> fields, final HashIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(
				util().serialize(OptionsBuilder.build(options != null ? options : new HashIndexOptions(), fields)));
		return request;
	}

	protected Request createSkiplistIndexRequest(final Iterable<String> fields, final SkiplistIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(
				util().serialize(OptionsBuilder.build(options != null ? options : new SkiplistIndexOptions(), fields)));
		return request;
	}

	protected Request createPersistentIndexRequest(
			final Iterable<String> fields, final PersistentIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(util().serialize(
				OptionsBuilder.build(options != null ? options : new PersistentIndexOptions(), fields)));
		return request;
	}

	protected Request createGeoIndexRequest(final Iterable<String> fields, final GeoIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(
				util().serialize(OptionsBuilder.build(options != null ? options : new GeoIndexOptions(), fields)));
		return request;
	}

	protected Request createFulltextIndexRequest(final Iterable<String> fields, final FulltextIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(
				util().serialize(OptionsBuilder.build(options != null ? options : new FulltextIndexOptions(), fields)));
		return request;
	}

	protected Request createTtlIndexRequest(final Iterable<String> fields, final TtlIndexOptions options) {
		final Request request = request(db.name(), RequestType.POST, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		request.setBody(
				util().serialize(OptionsBuilder.build(options != null ? options : new TtlIndexOptions(), fields)));
		return request;
	}

	protected Request getIndexesRequest() {
		final Request request = request(db.name(), RequestType.GET, PATH_API_INDEX);
		request.putQueryParam(COLLECTION, name);
		return request;
	}

	protected ResponseDeserializer<Collection<IndexEntity>> getIndexesResponseDeserializer() {
        return response -> util().deserialize(response.getBody().get("indexes"), new Type<Collection<IndexEntity>>() {
        }.getType());
	}

	protected Request truncateRequest(final CollectionTruncateOptions options) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "truncate");
		final CollectionTruncateOptions params = (options != null ? options : new CollectionTruncateOptions());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		return request;
	}

	protected Request countRequest(final CollectionCountOptions options) {
		final Request request = request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "count");
		final CollectionCountOptions params = (options != null ? options : new CollectionCountOptions());
		request.putHeaderParam(TRANSACTION_ID, params.getStreamTransactionId());
		return request;
	}

	protected Request dropRequest(final Boolean isSystem) {
		return request(db.name(), RequestType.DELETE, PATH_API_COLLECTION, name).putQueryParam("isSystem", isSystem);
	}

	protected Request loadRequest() {
		return request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "load");
	}

	protected Request unloadRequest() {
		return request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "unload");
	}

	protected Request getInfoRequest() {
		return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name);
	}

	protected Request getPropertiesRequest() {
		return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "properties");
	}

	protected Request changePropertiesRequest(final CollectionPropertiesOptions options) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "properties");
		request.setBody(util().serialize(options != null ? options : new CollectionPropertiesOptions()));
		return request;
	}

	protected Request renameRequest(final String newName) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "rename");
		request.setBody(util().serialize(OptionsBuilder.build(new CollectionRenameOptions(), newName)));
		return request;
	}

	protected <T> Request responsibleShardRequest(final T value) {
		final Request request = request(db.name(), RequestType.PUT, PATH_API_COLLECTION, name, "responsibleShard");
		request.setBody(util(Serializer.CUSTOM).serialize(value));
		return request;
	}

	protected Request getRevisionRequest() {
		return request(db.name(), RequestType.GET, PATH_API_COLLECTION, name, "revision");
	}

	protected Request grantAccessRequest(final String user, final Permissions permissions) {
		return request(ArangoRequestParam.SYSTEM, RequestType.PUT, PATH_API_USER, user, ArangoRequestParam.DATABASE,
				db.name(), name).setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
	}

	protected Request resetAccessRequest(final String user) {
		return request(ArangoRequestParam.SYSTEM, RequestType.DELETE, PATH_API_USER, user, ArangoRequestParam.DATABASE,
				db.name(), name);
	}

	protected Request getPermissionsRequest(final String user) {
		return request(ArangoRequestParam.SYSTEM, RequestType.GET, PATH_API_USER, user, ArangoRequestParam.DATABASE,
				db.name(), name);
	}

	protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
        return response -> {
            final VPackSlice body = response.getBody();
            if (body != null) {
                final VPackSlice result = body.get(ArangoResponseField.RESULT);
                if (!result.isNone()) {
                    return util().deserialize(result, Permissions.class);
                }
            }
            return null;
        };
	}

}

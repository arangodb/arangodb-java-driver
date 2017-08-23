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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.entity.Permissions;
import com.arangodb.internal.ArangoExecutor.ResponseDeserializer;
import com.arangodb.internal.velocystream.internal.Connection;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.CollectionRenameOptions;
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
import com.arangodb.model.ImportType;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.arangodb.model.UserAccessOptions;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark Vollmary
 *
 */
public class InternalArangoCollection<A extends InternalArangoDB<E, R, C>, D extends InternalArangoDatabase<A, E, R, C>, E extends ArangoExecutor, R, C extends Connection>
		extends ArangoExecuteable<E, R, C> {

	private final D db;
	private final String name;

	public InternalArangoCollection(final D db, final String name) {
		super(db.executor(), db.util());
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
		final Request request = new Request(db.name(), RequestType.POST,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(util().serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentCreateEntity<T>> insertDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentCreateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentCreateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentCreateEntity<T> doc = util().deserialize(body, DocumentCreateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) util().deserialize(newDoc, value.getClass()));
				}
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.ID, doc.getId());
				values.put(DocumentField.Type.KEY, doc.getKey());
				values.put(DocumentField.Type.REV, doc.getRev());
				executor.documentCache().setValues(value, values);
				return doc;
			}
		};
	}

	protected <T> Request insertDocumentsRequest(final Collection<T> values, final DocumentCreateOptions params) {
		final Request request = new Request(db.name(), RequestType.POST,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(
			util().serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentCreateOptions params) {
		return new ResponseDeserializer<MultiDocumentEntity<DocumentCreateEntity<T>>>() {
			@Override
			public MultiDocumentEntity<DocumentCreateEntity<T>> deserialize(final Response response)
					throws VPackException {
				Class<T> type = null;
				if (params.getReturnNew() != null && params.getReturnNew()) {
					if (!values.isEmpty()) {
						type = (Class<T>) values.iterator().next().getClass();
					}
				}
				final MultiDocumentEntity<DocumentCreateEntity<T>> multiDocument = new MultiDocumentEntity<DocumentCreateEntity<T>>();
				final Collection<DocumentCreateEntity<T>> docs = new ArrayList<DocumentCreateEntity<T>>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final Collection<Object> documentsAndErrors = new ArrayList<Object>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						final ErrorEntity error = (ErrorEntity) util().deserialize(next, ErrorEntity.class);
						errors.add(error);
						documentsAndErrors.add(error);
					} else {
						final DocumentCreateEntity<T> doc = util().deserialize(next, DocumentCreateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) util().deserialize(newDoc, type));
						}
						docs.add(doc);
						documentsAndErrors.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				multiDocument.setDocumentsAndErrors(documentsAndErrors);
				return multiDocument;
			}
		};
	}

	protected Request importDocumentsRequest(final String values, final DocumentImportOptions options) {
		return importDocumentsRequest(options).putQueryParam(ArangoDBConstants.TYPE, ImportType.auto)
				.setBody(util().serialize(values));
	}

	protected Request importDocumentsRequest(final Collection<?> values, final DocumentImportOptions options) {
		return importDocumentsRequest(options).putQueryParam(ArangoDBConstants.TYPE, ImportType.list).setBody(
			util().serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
	}

	protected Request importDocumentsRequest(final DocumentImportOptions options) {
		final DocumentImportOptions params = options != null ? options : new DocumentImportOptions();
		return new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_IMPORT)
				.putQueryParam(ArangoDBConstants.COLLECTION, name)
				.putQueryParam(ArangoDBConstants.FROM_PREFIX, params.getFromPrefix())
				.putQueryParam(ArangoDBConstants.TO_PREFIX, params.getToPrefix())
				.putQueryParam(ArangoDBConstants.OVERWRITE, params.getOverwrite())
				.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync())
				.putQueryParam(ArangoDBConstants.ON_DUPLICATE, params.getOnDuplicate())
				.putQueryParam(ArangoDBConstants.COMPLETE, params.getComplete())
				.putQueryParam(ArangoDBConstants.DETAILS, params.getDetails());
	}

	protected Request getDocumentRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, executor.createDocumentHandle(name, key)));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	protected Request getDocumentsRequest(final Collection<String> keys, final DocumentReadOptions options) {
		return new Request(db.name(), RequestType.PUT, executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name))
				.putQueryParam("onlyget", true)
				.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, options.getIfNoneMatch())
				.putHeaderParam(ArangoDBConstants.IF_MATCH, options.getIfMatch()).setBody(util().serialize(keys));
	}

	protected <T> ResponseDeserializer<MultiDocumentEntity<T>> getDocumentsResponseDeserializer(
		final Class<T> type,
		final DocumentReadOptions options) {
		return new ResponseDeserializer<MultiDocumentEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public MultiDocumentEntity<T> deserialize(final Response response) throws VPackException {
				final MultiDocumentEntity<T> multiDocument = new MultiDocumentEntity<T>();
				final Collection<T> docs = new ArrayList<T>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final Collection<Object> documentsAndErrors = new ArrayList<Object>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						final ErrorEntity error = (ErrorEntity) util().deserialize(next, ErrorEntity.class);
						errors.add(error);
						documentsAndErrors.add(error);
					} else {
						final T doc = (T) util().deserialize(next, type);
						docs.add(doc);
						documentsAndErrors.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				multiDocument.setDocumentsAndErrors(documentsAndErrors);
				return multiDocument;
			}
		};
	}

	protected <T> Request replaceDocumentRequest(
		final String key,
		final T value,
		final DocumentReplaceOptions options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, executor.createDocumentHandle(name, key)));
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(util().serialize(value));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentUpdateEntity<T>> replaceDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentUpdateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentUpdateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentUpdateEntity<T> doc = util().deserialize(body, DocumentUpdateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) util().deserialize(newDoc, value.getClass()));
				}
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) util().deserialize(oldDoc, value.getClass()));
				}
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				executor.documentCache().setValues(value, values);
				return doc;
			}
		};
	}

	protected <T> Request replaceDocumentsRequest(final Collection<T> values, final DocumentReplaceOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(
			util().serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentReplaceOptions params) {
		return new ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>>() {
			@Override
			public MultiDocumentEntity<DocumentUpdateEntity<T>> deserialize(final Response response)
					throws VPackException {
				Class<T> type = null;
				if ((params.getReturnNew() != null && params.getReturnNew())
						|| (params.getReturnOld() != null && params.getReturnOld())) {
					if (!values.isEmpty()) {
						type = (Class<T>) values.iterator().next().getClass();
					}
				}
				final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<DocumentUpdateEntity<T>>();
				final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<DocumentUpdateEntity<T>>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final Collection<Object> documentsAndErrors = new ArrayList<Object>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						final ErrorEntity error = (ErrorEntity) util().deserialize(next, ErrorEntity.class);
						errors.add(error);
						documentsAndErrors.add(error);
					} else {
						final DocumentUpdateEntity<T> doc = util().deserialize(next, DocumentUpdateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) util().deserialize(newDoc, type));
						}
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) util().deserialize(oldDoc, type));
						}
						docs.add(doc);
						documentsAndErrors.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				multiDocument.setDocumentsAndErrors(documentsAndErrors);
				return multiDocument;
			}
		};
	}

	protected <T> Request updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, executor.createDocumentHandle(name, key)));
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(util().serialize(value, new ArangoSerializer.Options()
				.serializeNullValues(params.getSerializeNull() == null || params.getSerializeNull())));
		return request;
	}

	protected <T> ResponseDeserializer<DocumentUpdateEntity<T>> updateDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentUpdateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentUpdateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentUpdateEntity<T> doc = util().deserialize(body, DocumentUpdateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) util().deserialize(newDoc, value.getClass()));
				}
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) util().deserialize(oldDoc, value.getClass()));
				}
				return doc;
			}
		};
	}

	protected <T> Request updateDocumentsRequest(final Collection<T> values, final DocumentUpdateOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final Boolean keepNull = params.getKeepNull();
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, keepNull);
		request.putQueryParam(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(
			util().serialize(values, new ArangoSerializer.Options().serializeNullValues(false).stringAsJson(true)));
		return request;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentUpdateOptions params) {
		return new ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>>() {
			@Override
			public MultiDocumentEntity<DocumentUpdateEntity<T>> deserialize(final Response response)
					throws VPackException {
				Class<T> type = null;
				if ((params.getReturnNew() != null && params.getReturnNew())
						|| (params.getReturnOld() != null && params.getReturnOld())) {
					if (!values.isEmpty()) {
						type = (Class<T>) values.iterator().next().getClass();
					}
				}
				final MultiDocumentEntity<DocumentUpdateEntity<T>> multiDocument = new MultiDocumentEntity<DocumentUpdateEntity<T>>();
				final Collection<DocumentUpdateEntity<T>> docs = new ArrayList<DocumentUpdateEntity<T>>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final Collection<Object> documentsAndErrors = new ArrayList<Object>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						final ErrorEntity error = (ErrorEntity) util().deserialize(next, ErrorEntity.class);
						errors.add(error);
						documentsAndErrors.add(error);
					} else {
						final DocumentUpdateEntity<T> doc = util().deserialize(next, DocumentUpdateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) util().deserialize(newDoc, type));
						}
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) util().deserialize(oldDoc, type));
						}
						docs.add(doc);
						documentsAndErrors.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				multiDocument.setDocumentsAndErrors(documentsAndErrors);
				return multiDocument;
			}
		};
	}

	protected Request deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, executor.createDocumentHandle(name, key)));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	protected <T> ResponseDeserializer<DocumentDeleteEntity<T>> deleteDocumentResponseDeserializer(
		final Class<T> type) {
		return new ResponseDeserializer<DocumentDeleteEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentDeleteEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentDeleteEntity<T> doc = util().deserialize(body, DocumentDeleteEntity.class);
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) util().deserialize(oldDoc, type));
				}
				return doc;
			}
		};
	}

	protected <T> Request deleteDocumentsRequest(final Collection<T> keys, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.setBody(util().serialize(keys));
		return request;
	}

	protected <T> ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocumentsResponseDeserializer(
		final Class<T> type) {
		return new ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>>() {
			@SuppressWarnings("unchecked")
			@Override
			public MultiDocumentEntity<DocumentDeleteEntity<T>> deserialize(final Response response)
					throws VPackException {
				final MultiDocumentEntity<DocumentDeleteEntity<T>> multiDocument = new MultiDocumentEntity<DocumentDeleteEntity<T>>();
				final Collection<DocumentDeleteEntity<T>> docs = new ArrayList<DocumentDeleteEntity<T>>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final Collection<Object> documentsAndErrors = new ArrayList<Object>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						final ErrorEntity error = (ErrorEntity) util().deserialize(next, ErrorEntity.class);
						errors.add(error);
						documentsAndErrors.add(error);
					} else {
						final DocumentDeleteEntity<T> doc = util().deserialize(next, DocumentDeleteEntity.class);
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) util().deserialize(oldDoc, type));
						}
						docs.add(doc);
						documentsAndErrors.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				multiDocument.setDocumentsAndErrors(documentsAndErrors);
				return multiDocument;
			}
		};
	}

	protected Request documentExistsRequest(final String key, final DocumentExistsOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.HEAD,
				executor.createPath(ArangoDBConstants.PATH_API_DOCUMENT, executor.createDocumentHandle(name, key)));
		final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		return request;
	}

	protected Request getIndexRequest(final String id) {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_INDEX, createIndexId(id)));
	}

	protected Request deleteIndexRequest(final String id) {
		return new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_INDEX, createIndexId(id)));
	}

	protected ResponseDeserializer<String> deleteIndexResponseDeserializer() {
		return new ResponseDeserializer<String>() {
			@Override
			public String deserialize(final Response response) throws VPackException {
				return response.getBody().get(ArangoDBConstants.ID).getAsString();
			}
		};
	}

	private String createIndexId(final String id) {
		final String index;
		if (id.matches(ArangoExecutor.REGEX_ID)) {
			index = id;
		} else if (id.matches(ArangoExecutor.REGEX_KEY)) {
			index = name + "/" + id;
		} else {
			throw new ArangoDBException(String.format("index id %s is not valid.", id));
		}
		return index;
	}

	protected Request createHashIndexRequest(final Iterable<String> fields, final HashIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new HashIndexOptions(), fields)));
		return request;
	}

	protected Request createSkiplistIndexRequest(final Iterable<String> fields, final SkiplistIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new SkiplistIndexOptions(), fields)));
		return request;
	}

	protected Request createPersistentIndexRequest(
		final Iterable<String> fields,
		final PersistentIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new PersistentIndexOptions(), fields)));
		return request;
	}

	protected Request createGeoIndexRequest(final Iterable<String> fields, final GeoIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new GeoIndexOptions(), fields)));
		return request;
	}

	protected Request createFulltextIndexRequest(final Iterable<String> fields, final FulltextIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			util().serialize(OptionsBuilder.build(options != null ? options : new FulltextIndexOptions(), fields)));
		return request;
	}

	protected Request getIndexesRequest() {
		final Request request;
		request = new Request(db.name(), RequestType.GET, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		return request;
	}

	protected ResponseDeserializer<Collection<IndexEntity>> getIndexesResponseDeserializer() {
		return new ResponseDeserializer<Collection<IndexEntity>>() {
			@Override
			public Collection<IndexEntity> deserialize(final Response response) throws VPackException {
				return util().deserialize(response.getBody().get(ArangoDBConstants.INDEXES),
					new Type<Collection<IndexEntity>>() {
					}.getType());
			}
		};
	}

	protected Request truncateRequest() {
		return new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.TRUNCATE));
	}

	protected Request countRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.COUNT));
	}

	protected Request dropRequest(final Boolean isSystem) {
		return new Request(db.name(), RequestType.DELETE,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name))
						.putQueryParam(ArangoDBConstants.IS_SYSTEM, isSystem);
	}

	protected Request loadRequest() {
		return new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.LOAD));
	}

	protected Request unloadRequest() {
		return new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.UNLOAD));
	}

	protected Request getInfoRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name));
	}

	protected Request getPropertiesRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.PROPERTIES));
	}

	protected Request changePropertiesRequest(final CollectionPropertiesOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.PROPERTIES));
		request.setBody(util().serialize(options != null ? options : new CollectionPropertiesOptions()));
		return request;
	}

	protected Request renameRequest(final String newName) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.RENAME));
		request.setBody(util().serialize(OptionsBuilder.build(new CollectionRenameOptions(), newName)));
		return request;
	}

	protected Request getRevisionRequest() {
		return new Request(db.name(), RequestType.GET,
				executor.createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.REVISION));
	}

	protected Request grantAccessRequest(final String user, final Permissions permissions) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.PUT,
				executor.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, db.name(), name))
						.setBody(util().serialize(OptionsBuilder.build(new UserAccessOptions(), permissions)));
	}

	protected Request resetAccessRequest(final String user) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.DELETE, executor
				.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, db.name(), name));
	}

	protected Request getPermissionsRequest(final String user) {
		return new Request(ArangoDBConstants.SYSTEM, RequestType.GET, executor
				.createPath(ArangoDBConstants.PATH_API_USER, user, ArangoDBConstants.DATABASE, db.name(), name));
	}

	protected ResponseDeserializer<Permissions> getPermissionsResponseDeserialzer() {
		return new ResponseDeserializer<Permissions>() {
			@Override
			public Permissions deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				if (body != null) {
					final VPackSlice result = body.get(ArangoDBConstants.RESULT);
					if (!result.isNone()) {
						return util().deserialize(result, Permissions.class);
					}
				}
				return null;
			}
		};
	}

}

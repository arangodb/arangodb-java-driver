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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionPropertiesEntity;
import com.arangodb.entity.CollectionRevisionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.DocumentDeleteEntity;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentUpdateEntity;
import com.arangodb.entity.ErrorEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.model.CollectionPropertiesOptions;
import com.arangodb.model.CollectionRenameOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentDeleteOptions;
import com.arangodb.model.DocumentExistsOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentReplaceOptions;
import com.arangodb.model.DocumentUpdateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.OptionsBuilder;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.arangodb.velocypack.Type;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Request;
import com.arangodb.velocystream.RequestType;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoCollection extends ArangoExecuteable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoCollection.class);

	private final ArangoDatabase db;
	private final String name;

	protected ArangoCollection(final ArangoDatabase db, final String name) {
		super(db.communication(), db.vpack(), db.vpackNull(), db.vpackParser(), db.documentCache(),
				db.collectionCache());
		this.db = db;
		this.name = name;
	}

	protected ArangoDatabase db() {
		return db;
	}

	protected String name() {
		return name;
	}

	private String createDocumentHandle(final String key) {
		validateDocumentKey(key);
		return createPath(name, key);
	}

	/**
	 * Creates a new document from the given document, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentCreateEntity<T> insertDocument(final T value) throws ArangoDBException {
		return executeSync(insertDocumentRequest(value, new DocumentCreateOptions()),
			insertDocumentResponseDeserializer(value));
	}

	/**
	 * Creates a new document from the given document, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentCreateEntity<T> insertDocument(final T value, final DocumentCreateOptions options)
			throws ArangoDBException {
		return executeSync(insertDocumentRequest(value, options), insertDocumentResponseDeserializer(value));
	}

	private <T> Request insertDocumentRequest(final T value, final DocumentCreateOptions options) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<DocumentCreateEntity<T>> insertDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentCreateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentCreateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentCreateEntity<T> doc = ArangoCollection.this.deserialize(body, DocumentCreateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) ArangoCollection.this.deserialize(newDoc, value.getClass()));
				}
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.ID, doc.getId());
				values.put(DocumentField.Type.KEY, doc.getKey());
				values.put(DocumentField.Type.REV, doc.getRev());
				documentCache.setValues(value, values);
				return doc;
			}
		};
	}

	/**
	 * Creates new documents from the given documents, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentCreateOptions params = new DocumentCreateOptions();
		return executeSync(insertDocumentsRequest(values, params), insertDocumentsResponseDeserializer(values, params));
	}

	/**
	 * Creates new documents from the given documents, unless there is already a document with the _key given. If no
	 * _key is given, a new unique _key is generated automatically.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentCreateEntity<T>> insertDocuments(
		final Collection<T> values,
		final DocumentCreateOptions options) throws ArangoDBException {
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		return executeSync(insertDocumentsRequest(values, params), insertDocumentsResponseDeserializer(values, params));
	}

	private <T> Request insertDocumentsRequest(final Collection<T> values, final DocumentCreateOptions params) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(values));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<MultiDocumentEntity<DocumentCreateEntity<T>>> insertDocumentsResponseDeserializer(
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
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						errors.add((ErrorEntity) ArangoCollection.this.deserialize(next, ErrorEntity.class));
					} else {
						final DocumentCreateEntity<T> doc = ArangoCollection.this.deserialize(next,
							DocumentCreateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) ArangoCollection.this.deserialize(newDoc, type));
						}
						docs.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				return multiDocument;
			}
		};
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @return the document identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getDocument(final String key, final Class<T> type) {
		validateDocumentKey(key);
		try {
			return executeSync(getDocumentRequest(key, new DocumentReadOptions()), type);
		} catch (final ArangoDBException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
			return null;
		}
	}

	/**
	 * Reads a single document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return the document identified by the key
	 * @throws ArangoDBException
	 */
	public <T> T getDocument(final String key, final Class<T> type, final DocumentReadOptions options)
			throws ArangoDBException {
		validateDocumentKey(key);
		try {
			return executeSync(getDocumentRequest(key, options), type);
		} catch (final ArangoDBException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(e.getMessage(), e);
			}
			return null;
		}
	}

	private Request getDocumentRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	/**
	 * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentUpdateEntity<T> replaceDocument(final String key, final T value) throws ArangoDBException {
		return executeSync(replaceDocumentRequest(key, value, new DocumentReplaceOptions()),
			replaceDocumentResponseDeserializer(value));
	}

	/**
	 * Replaces the document with key with the one in the body, provided there is such a document and no precondition is
	 * violated
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentUpdateEntity<T> replaceDocument(
		final String key,
		final T value,
		final DocumentReplaceOptions options) throws ArangoDBException {
		return executeSync(replaceDocumentRequest(key, value, options), replaceDocumentResponseDeserializer(value));
	}

	private <T> Request replaceDocumentRequest(final String key, final T value, final DocumentReplaceOptions options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<DocumentUpdateEntity<T>> replaceDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentUpdateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentUpdateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentUpdateEntity<T> doc = ArangoCollection.this.deserialize(body, DocumentUpdateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) ArangoCollection.this.deserialize(newDoc, value.getClass()));
				}
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, value.getClass()));
				}
				final Map<DocumentField.Type, String> values = new HashMap<DocumentField.Type, String>();
				values.put(DocumentField.Type.REV, doc.getRev());
				documentCache.setValues(value, values);
				return doc;
			}
		};
	}

	/**
	 * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
	 * specified by the _key attributes in the documents in values.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentReplaceOptions params = new DocumentReplaceOptions();
		return executeSync(replaceDocumentsRequest(values, params),
			replaceDocumentsResponseDeserializer(values, params));
	}

	/**
	 * Replaces multiple documents in the specified collection with the ones in the values, the replaced documents are
	 * specified by the _key attributes in the documents in values.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A List of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> replaceDocuments(
		final Collection<T> values,
		final DocumentReplaceOptions options) throws ArangoDBException {
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		return executeSync(replaceDocumentsRequest(values, params),
			replaceDocumentsResponseDeserializer(values, params));
	}

	private <T> Request replaceDocumentsRequest(final Collection<T> values, final DocumentReplaceOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(values));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> replaceDocumentsResponseDeserializer(
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
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						errors.add((ErrorEntity) ArangoCollection.this.deserialize(next, ErrorEntity.class));
					} else {
						final DocumentUpdateEntity<T> doc = ArangoCollection.this.deserialize(next,
							DocumentUpdateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) ArangoCollection.this.deserialize(newDoc, type));
						}
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, type));
						}
						docs.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				return multiDocument;
			}
		};
	}

	/**
	 * Partially updates the document identified by document-key. The value must contain a document with the attributes
	 * to patch (the patch document). All attributes from the patch document will be added to the existing document if
	 * they do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentUpdateEntity<T> updateDocument(final String key, final T value) throws ArangoDBException {
		return executeSync(updateDocumentRequest(key, value, new DocumentUpdateOptions()),
			updateDocumentResponseDeserializer(value));
	}

	/**
	 * Partially updates the document identified by document-key. The value must contain a document with the attributes
	 * to patch (the patch document). All attributes from the patch document will be added to the existing document if
	 * they do not yet exist, and overwritten in the existing document if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param value
	 *            A representation of a single document (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentUpdateEntity<T> updateDocument(
		final String key,
		final T value,
		final DocumentUpdateOptions options) throws ArangoDBException {
		return executeSync(updateDocumentRequest(key, value, options), updateDocumentResponseDeserializer(value));
	}

	private <T> Request updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putQueryParam(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return request;
	}

	private <T> ResponseDeserializer<DocumentUpdateEntity<T>> updateDocumentResponseDeserializer(final T value) {
		return new ResponseDeserializer<DocumentUpdateEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentUpdateEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentUpdateEntity<T> doc = ArangoCollection.this.deserialize(body, DocumentUpdateEntity.class);
				final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew((T) ArangoCollection.this.deserialize(newDoc, value.getClass()));
				}
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, value.getClass()));
				}
				return doc;
			}
		};
	}

	/**
	 * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
	 * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
	 * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
	 * overwritten in the existing documents if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A list of documents (POJO, VPackSlice or String for Json)
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(final Collection<T> values)
			throws ArangoDBException {
		final DocumentUpdateOptions params = new DocumentUpdateOptions();
		return executeSync(updateDocumentsRequest(values, params), updateDocumentsResponseDeserializer(values, params));
	}

	/**
	 * Partially updates documents, the documents to update are specified by the _key attributes in the objects on
	 * values. Vales must contain a list of document updates with the attributes to patch (the patch documents). All
	 * attributes from the patch documents will be added to the existing documents if they do not yet exist, and
	 * overwritten in the existing documents if they do exist there.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#update-documents">API
	 *      Documentation</a>
	 * @param values
	 *            A list of documents (POJO, VPackSlice or String for Json)
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentUpdateEntity<T>> updateDocuments(
		final Collection<T> values,
		final DocumentUpdateOptions options) throws ArangoDBException {
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		return executeSync(updateDocumentsRequest(values, params), updateDocumentsResponseDeserializer(values, params));
	}

	private <T> Request updateDocumentsRequest(final Collection<T> values, final DocumentUpdateOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final Boolean keepNull = params.getKeepNull();
		request.putQueryParam(ArangoDBConstants.KEEP_NULL, keepNull);
		request.putQueryParam(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putQueryParam(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(values, true));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<MultiDocumentEntity<DocumentUpdateEntity<T>>> updateDocumentsResponseDeserializer(
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
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						errors.add((ErrorEntity) ArangoCollection.this.deserialize(next, ErrorEntity.class));
					} else {
						final DocumentUpdateEntity<T> doc = ArangoCollection.this.deserialize(next,
							DocumentUpdateEntity.class);
						final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
						if (newDoc.isObject()) {
							doc.setNew((T) ArangoCollection.this.deserialize(newDoc, type));
						}
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, type));
						}
						docs.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				return multiDocument;
			}
		};
	}

	/**
	 * Removes a document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-a-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public DocumentDeleteEntity<Void> deleteDocument(final String key) throws ArangoDBException {
		return executeSync(deleteDocumentRequest(key, new DocumentDeleteOptions()),
			deleteDocumentResponseDeserializer(Void.class));
	}

	/**
	 * Removes a document
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-a-document">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param type
	 *            The type of the document (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the document
	 * @throws ArangoDBException
	 */
	public <T> DocumentDeleteEntity<T> deleteDocument(
		final String key,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executeSync(deleteDocumentRequest(key, options), deleteDocumentResponseDeserializer(type));
	}

	private Request deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<DocumentDeleteEntity<T>> deleteDocumentResponseDeserializer(final Class<T> type) {
		return new ResponseDeserializer<DocumentDeleteEntity<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public DocumentDeleteEntity<T> deserialize(final Response response) throws VPackException {
				final VPackSlice body = response.getBody();
				final DocumentDeleteEntity<T> doc = ArangoCollection.this.deserialize(body, DocumentDeleteEntity.class);
				final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, type));
				}
				return doc;
			}
		};
	}

	/**
	 * Removes multiple document
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-multiple-documents">API
	 *      Documentation</a>
	 * @param keys
	 *            The keys of the documents
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public MultiDocumentEntity<DocumentDeleteEntity<Void>> deleteDocuments(final Collection<String> keys)
			throws ArangoDBException {
		return executeSync(deleteDocumentsRequest(keys, new DocumentDeleteOptions()),
			deleteDocumentsResponseDeserializer(Void.class));
	}

	/**
	 * Removes multiple document
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-multiple-documents">API
	 *      Documentation</a>
	 * @param keys
	 *            The keys of the documents
	 * @param type
	 *            The type of the documents (POJO class, VPackSlice or String for Json). Only necessary if
	 *            options.returnOld is set to true, otherwise can be null.
	 * @param options
	 *            Additional options, can be null
	 * @return information about the documents
	 * @throws ArangoDBException
	 */
	public <T> MultiDocumentEntity<DocumentDeleteEntity<T>> deleteDocuments(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executeSync(deleteDocumentsRequest(keys, options), deleteDocumentsResponseDeserializer(type));
	}

	private Request deleteDocumentsRequest(final Collection<String> keys, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putQueryParam(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putQueryParam(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.setBody(serialize(keys));
		return request;
	}

	private <T> ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>> deleteDocumentsResponseDeserializer(
		final Class<T> type) {
		return new ResponseDeserializer<MultiDocumentEntity<DocumentDeleteEntity<T>>>() {
			@SuppressWarnings("unchecked")
			@Override
			public MultiDocumentEntity<DocumentDeleteEntity<T>> deserialize(final Response response)
					throws VPackException {
				final MultiDocumentEntity<DocumentDeleteEntity<T>> multiDocument = new MultiDocumentEntity<DocumentDeleteEntity<T>>();
				final Collection<DocumentDeleteEntity<T>> docs = new ArrayList<DocumentDeleteEntity<T>>();
				final Collection<ErrorEntity> errors = new ArrayList<ErrorEntity>();
				final VPackSlice body = response.getBody();
				for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
					final VPackSlice next = iterator.next();
					if (next.get(ArangoDBConstants.ERROR).isTrue()) {
						errors.add((ErrorEntity) ArangoCollection.this.deserialize(next, ErrorEntity.class));
					} else {
						final DocumentDeleteEntity<T> doc = ArangoCollection.this.deserialize(next,
							DocumentDeleteEntity.class);
						final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
						if (oldDoc.isObject()) {
							doc.setOld((T) ArangoCollection.this.deserialize(oldDoc, type));
						}
						docs.add(doc);
					}
				}
				multiDocument.setDocuments(docs);
				multiDocument.setErrors(errors);
				return multiDocument;
			}
		};
	}

	/**
	 * Checks if the document exists by reading a single document head
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @return true if the document was found, otherwise false
	 */
	public Boolean documentExists(final String key) {
		return documentExists(key, new DocumentExistsOptions());
	}

	/**
	 * Checks if the document exists by reading a single document head
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
	 *      Documentation</a>
	 * @param key
	 *            The key of the document
	 * @param options
	 *            Additional options, can be null
	 * @return true if the document was found, otherwise false
	 */
	public Boolean documentExists(final String key, final DocumentExistsOptions options) {
		try {
			communication.executeSync(documentExistsRequest(key, options));
			return true;
		} catch (final ArangoDBException e) {
			return false;
		}
	}

	private Request documentExistsRequest(final String key, final DocumentExistsOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.HEAD,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
		request.putHeaderParam(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.putHeaderParam(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		return request;
	}

	/**
	 * Creates a hash index for the collection if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Hash.html#create-hash-index">API Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity createHashIndex(final Collection<String> fields, final HashIndexOptions options)
			throws ArangoDBException {
		return executeSync(createHashIndexRequest(fields, options), IndexEntity.class);
	}

	private Request createHashIndexRequest(final Collection<String> fields, final HashIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize(OptionsBuilder.build(options != null ? options : new HashIndexOptions(), fields)));
		return request;
	}

	/**
	 * Creates a skip-list index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Skiplist.html#create-skip-list">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity createSkiplistIndex(final Collection<String> fields, final SkiplistIndexOptions options)
			throws ArangoDBException {
		return executeSync(createSkiplistIndexRequest(fields, options), IndexEntity.class);
	}

	private Request createSkiplistIndexRequest(final Collection<String> fields, final SkiplistIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new SkiplistIndexOptions(), fields)));
		return request;
	}

	/**
	 * Creates a persistent index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Persistent.html#create-a-persistent-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity createPersistentIndex(final Collection<String> fields, final PersistentIndexOptions options)
			throws ArangoDBException {
		return executeSync(createPersistentIndexRequest(fields, options), IndexEntity.class);
	}

	private Request createPersistentIndexRequest(
		final Collection<String> fields,
		final PersistentIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new PersistentIndexOptions(), fields)));
		return request;
	}

	/**
	 * Creates a geo-spatial index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Geo.html#create-geospatial-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity createGeoIndex(final Collection<String> fields, final GeoIndexOptions options)
			throws ArangoDBException {
		return executeSync(createGeoIndexRequest(fields, options), IndexEntity.class);
	}

	private Request createGeoIndexRequest(final Collection<String> fields, final GeoIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(serialize(OptionsBuilder.build(options != null ? options : new GeoIndexOptions(), fields)));
		return request;
	}

	/**
	 * Creates a fulltext index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Fulltext.html#create-fulltext-index">API
	 *      Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 * @throws ArangoDBException
	 */
	public IndexEntity createFulltextIndex(final Collection<String> fields, final FulltextIndexOptions options)
			throws ArangoDBException {
		return executeSync(createFulltextIndexRequest(fields, options), IndexEntity.class);
	}

	private Request createFulltextIndexRequest(final Collection<String> fields, final FulltextIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		request.setBody(
			serialize(OptionsBuilder.build(options != null ? options : new FulltextIndexOptions(), fields)));
		return request;
	}

	/**
	 * Returns all indexes of the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-all-indexes-of-a-collection">API
	 *      Documentation</a>
	 * @return information about the indexes
	 * @throws ArangoDBException
	 */
	public Collection<IndexEntity> getIndexes() throws ArangoDBException {
		return executeSync(getIndexesRequest(), getIndexesResponseDeserializer());
	}

	private Request getIndexesRequest() {
		final Request request;
		request = new Request(db.name(), RequestType.GET, ArangoDBConstants.PATH_API_INDEX);
		request.putQueryParam(ArangoDBConstants.COLLECTION, name);
		return request;
	}

	private ResponseDeserializer<Collection<IndexEntity>> getIndexesResponseDeserializer() {
		return new ResponseDeserializer<Collection<IndexEntity>>() {
			@Override
			public Collection<IndexEntity> deserialize(final Response response) throws VPackException {
				return ArangoCollection.this.deserialize(response.getBody().get(ArangoDBConstants.INDEXES),
					new Type<Collection<IndexEntity>>() {
					}.getType());
			}
		};
	}

	/**
	 * Removes all documents from the collection, but leaves the indexes intact
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#truncate-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity truncate() throws ArangoDBException {
		return executeSync(truncateRequest(), CollectionEntity.class);
	}

	private Request truncateRequest() {
		return new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.TRUNCATE));
	}

	/**
	 * Counts the documents in a collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-number-of-documents-in-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection, including the number of documents
	 * @throws ArangoDBException
	 */
	public CollectionPropertiesEntity count() throws ArangoDBException {
		return executeSync(countRequest(), CollectionPropertiesEntity.class);
	}

	private Request countRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.COUNT));
	}

	/**
	 * Drops the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @throws ArangoDBException
	 */
	public void drop() throws ArangoDBException {
		executeSync(dropRequest(), Void.class);
	}

	private Request dropRequest() {
		return new Request(db.name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_COLLECTION, name));
	}

	/**
	 * Loads a collection into memory.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#load-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity load() throws ArangoDBException {
		return executeSync(loadRequest(), CollectionEntity.class);
	}

	private Request loadRequest() {
		return new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.LOAD));
	}

	/**
	 * Removes a collection from memory. This call does not delete any documents. You can use the collection afterwards;
	 * in which case it will be loaded into memory, again.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#unload-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity unload() throws ArangoDBException {
		return executeSync(unloadRequest(), CollectionEntity.class);
	}

	private Request unloadRequest() {
		return new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.UNLOAD));
	}

	/**
	 * Returns information about the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-information-about-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity getInfo() throws ArangoDBException {
		return executeSync(getInfoRequest(), CollectionEntity.class);
	}

	private Request getInfoRequest() {
		return new Request(db.name(), RequestType.GET, createPath(ArangoDBConstants.PATH_API_COLLECTION, name));
	}

	/**
	 * Reads the properties of the specified collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#read-properties-of-a-collection">API
	 *      Documentation</a>
	 * @return properties of the collection
	 * @throws ArangoDBException
	 */
	public CollectionPropertiesEntity getProperties() throws ArangoDBException {
		return executeSync(getPropertiesRequest(), CollectionPropertiesEntity.class);
	}

	private Request getPropertiesRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.PROPERTIES));
	}

	/**
	 * Changes the properties of a collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#change-properties-of-a-collection">API
	 *      Documentation</a>
	 * @param options
	 *            Additional options, can be null
	 * @return properties of the collection
	 * @throws ArangoDBException
	 */
	public CollectionPropertiesEntity changeProperties(final CollectionPropertiesOptions options)
			throws ArangoDBException {
		return executeSync(changePropertiesRequest(options), CollectionPropertiesEntity.class);
	}

	private Request changePropertiesRequest(final CollectionPropertiesOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.PROPERTIES));
		request.setBody(serialize(options != null ? options : new CollectionPropertiesOptions()));
		return request;
	}

	/**
	 * Renames a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#rename-collection">API
	 *      Documentation</a>
	 * @param newName
	 *            The new name
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionEntity rename(final String newName) throws ArangoDBException {
		return executeSync(renameRequest(newName), CollectionEntity.class);
	}

	private Request renameRequest(final String newName) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.RENAME));
		request.setBody(serialize(OptionsBuilder.build(new CollectionRenameOptions(), newName)));
		return request;
	}

	/**
	 * Retrieve the collections revision
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-collection-revision-id">API
	 *      Documentation</a>
	 * @return information about the collection, including the collections revision
	 * @throws ArangoDBException
	 */
	public CollectionRevisionEntity getRevision() throws ArangoDBException {
		return executeSync(getRevisionRequest(), CollectionRevisionEntity.class);
	}

	private Request getRevisionRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.REVISION));
	}

}

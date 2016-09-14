package com.arangodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.arangodb.entity.CollectionPropertiesResult;
import com.arangodb.entity.CollectionResult;
import com.arangodb.entity.CollectionRevisionResult;
import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.DocumentDeleteResult;
import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentUpdateResult;
import com.arangodb.entity.IndexResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.Response;
import com.arangodb.internal.net.velocystream.RequestType;
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

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoCollection extends ArangoExecuteable {

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
	public <T> DocumentCreateResult<T> insertDocument(final T value) throws ArangoDBException {
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
	public <T> DocumentCreateResult<T> insertDocument(final T value, final DocumentCreateOptions options)
			throws ArangoDBException {
		return executeSync(insertDocumentRequest(value, options), insertDocumentResponseDeserializer(value));
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
	 */
	public <T> CompletableFuture<DocumentCreateResult<T>> insertDocumentAsync(final T value) {
		return executeAsync(insertDocumentRequest(value, new DocumentCreateOptions()),
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
	 */
	public <T> CompletableFuture<DocumentCreateResult<T>> insertDocumentAsync(
		final T value,
		final DocumentCreateOptions options) {
		return executeAsync(insertDocumentRequest(value, options), insertDocumentResponseDeserializer(value));
	}

	private <T> Request insertDocumentRequest(final T value, final DocumentCreateOptions options) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<DocumentCreateResult<T>> insertDocumentResponseDeserializer(final T value) {
		return response -> {
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
	public <T> Collection<DocumentCreateResult<T>> insertDocuments(final Collection<T> values)
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
	public <T> Collection<DocumentCreateResult<T>> insertDocuments(
		final Collection<T> values,
		final DocumentCreateOptions options) throws ArangoDBException {
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
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
	 * @return information about the documents
	 */
	public <T> CompletableFuture<Collection<DocumentCreateResult<T>>> insertDocumentsAsync(final Collection<T> values) {
		final DocumentCreateOptions params = new DocumentCreateOptions();
		return executeAsync(insertDocumentsRequest(values, params),
			insertDocumentsResponseDeserializer(values, params));
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
	 */
	public <T> CompletableFuture<Collection<DocumentCreateResult<T>>> insertDocumentsAsync(
		final Collection<T> values,
		final DocumentCreateOptions options) {
		final DocumentCreateOptions params = (options != null ? options : new DocumentCreateOptions());
		return executeAsync(insertDocumentsRequest(values, params),
			insertDocumentsResponseDeserializer(values, params));
	}

	private <T> Request insertDocumentsRequest(final Collection<T> values, final DocumentCreateOptions params) {
		final Request request = new Request(db.name(), RequestType.POST,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(values));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<Collection<DocumentCreateResult<T>>> insertDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentCreateOptions params) {
		return response -> {
			Class<T> type = null;
			if (params.getReturnNew() != null && params.getReturnNew()) {
				final Optional<T> first = values.stream().findFirst();
				if (first.isPresent()) {
					type = (Class<T>) first.get().getClass();
				}
			}
			final Collection<DocumentCreateResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentCreateResult<T> doc = deserialize(next, DocumentCreateResult.class);
				final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew(deserialize(newDoc, type));
				}
				docs.add(doc);
			}
			return docs;
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
	public <T> T getDocument(final String key, final Class<T> type) throws ArangoDBException {
		return executeSync(getDocumentRequest(key, new DocumentReadOptions()), type);
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
		return executeSync(getDocumentRequest(key, options), type);
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
	 */
	public <T> CompletableFuture<T> getDocumentAsync(final String key, final Class<T> type) {
		return executeAsync(getDocumentRequest(key, new DocumentReadOptions()), type);
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
	 */
	public <T> CompletableFuture<T> getDocumentAsync(
		final String key,
		final Class<T> type,
		final DocumentReadOptions options) {
		return executeAsync(getDocumentRequest(key, options), type);
	}

	private Request getDocumentRequest(final String key, final DocumentReadOptions options) {
		final Request request = new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentReadOptions params = (options != null ? options : new DocumentReadOptions());
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
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
	public <T> DocumentUpdateResult<T> replaceDocument(final String key, final T value) throws ArangoDBException {
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
	public <T> DocumentUpdateResult<T> replaceDocument(
		final String key,
		final T value,
		final DocumentReplaceOptions options) throws ArangoDBException {
		return executeSync(replaceDocumentRequest(key, value, options), replaceDocumentResponseDeserializer(value));
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
	 */
	public <T> CompletableFuture<DocumentUpdateResult<T>> replaceDocumentAsync(final String key, final T value) {
		return executeAsync(replaceDocumentRequest(key, value, new DocumentReplaceOptions()),
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
	 */
	public <T> CompletableFuture<DocumentUpdateResult<T>> replaceDocumentAsync(
		final String key,
		final T value,
		final DocumentReplaceOptions options) {
		return executeAsync(replaceDocumentRequest(key, value, options), replaceDocumentResponseDeserializer(value));
	}

	private <T> Request replaceDocumentRequest(final String key, final T value, final DocumentReplaceOptions options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value));
		return request;
	}

	private <T> ResponseDeserializer<DocumentUpdateResult<T>> replaceDocumentResponseDeserializer(final T value) {
		return response -> {
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
	public <T> Collection<DocumentUpdateResult<T>> replaceDocuments(final Collection<T> values)
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
	public <T> Collection<DocumentUpdateResult<T>> replaceDocuments(
		final Collection<T> values,
		final DocumentReplaceOptions options) throws ArangoDBException {
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
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
	 * @return information about the documents
	 */
	public <T> CompletableFuture<Collection<DocumentUpdateResult<T>>> replaceDocumentsAsync(
		final Collection<T> values) {
		final DocumentReplaceOptions params = new DocumentReplaceOptions();
		return executeAsync(replaceDocumentsRequest(values, params),
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
	 */
	public <T> CompletableFuture<Collection<DocumentUpdateResult<T>>> replaceDocumentsAsync(
		final Collection<T> values,
		final DocumentReplaceOptions options) {
		final DocumentReplaceOptions params = (options != null ? options : new DocumentReplaceOptions());
		return executeAsync(replaceDocumentsRequest(values, params),
			replaceDocumentsResponseDeserializer(values, params));
	}

	private <T> Request replaceDocumentsRequest(final Collection<T> values, final DocumentReplaceOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PUT, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(values));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<Collection<DocumentUpdateResult<T>>> replaceDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentReplaceOptions params) {
		return response -> {
			Class<T> type = null;
			if ((params.getReturnNew() != null && params.getReturnNew())
					|| (params.getReturnOld() != null && params.getReturnOld())) {
				final Optional<T> first = values.stream().findFirst();
				if (first.isPresent()) {
					type = (Class<T>) first.get().getClass();
				}
			}
			final Collection<DocumentUpdateResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentUpdateResult<T> doc = deserialize(next, DocumentUpdateResult.class);
				final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew(deserialize(newDoc, type));
				}
				final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld(deserialize(oldDoc, type));
				}
				docs.add(doc);
			}
			return docs;
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
	public <T> DocumentUpdateResult<T> updateDocument(final String key, final T value) throws ArangoDBException {
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
	public <T> DocumentUpdateResult<T> updateDocument(
		final String key,
		final T value,
		final DocumentUpdateOptions options) throws ArangoDBException {
		return executeSync(updateDocumentRequest(key, value, options), updateDocumentResponseDeserializer(value));
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
	 */
	public <T> CompletableFuture<DocumentUpdateResult<T>> updateDocumentAsync(final String key, final T value) {
		return executeAsync(updateDocumentRequest(key, value, new DocumentUpdateOptions()),
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
	 */
	public <T> CompletableFuture<DocumentUpdateResult<T>> updateDocumentAsync(
		final String key,
		final T value,
		final DocumentUpdateOptions options) {
		return executeAsync(updateDocumentRequest(key, value, options), updateDocumentResponseDeserializer(value));
	}

	private <T> Request updateDocumentRequest(final String key, final T value, final DocumentUpdateOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		request.putParameter(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		request.putParameter(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(value, true));
		return request;
	}

	private <T> ResponseDeserializer<DocumentUpdateResult<T>> updateDocumentResponseDeserializer(final T value) {
		return response -> {
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
	public <T> Collection<DocumentUpdateResult<T>> updateDocuments(final Collection<T> values)
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
	public <T> Collection<DocumentUpdateResult<T>> updateDocuments(
		final Collection<T> values,
		final DocumentUpdateOptions options) throws ArangoDBException {
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
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
	 * @return information about the documents
	 */
	public <T> CompletableFuture<Collection<DocumentUpdateResult<T>>> updateDocumentsAsync(final Collection<T> values) {
		final DocumentUpdateOptions params = new DocumentUpdateOptions();
		return executeAsync(updateDocumentsRequest(values, params),
			updateDocumentsResponseDeserializer(values, params));
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
	 */
	public <T> CompletableFuture<Collection<DocumentUpdateResult<T>>> updateDocumentsAsync(
		final Collection<T> values,
		final DocumentUpdateOptions options) {
		final DocumentUpdateOptions params = (options != null ? options : new DocumentUpdateOptions());
		return executeAsync(updateDocumentsRequest(values, params),
			updateDocumentsResponseDeserializer(values, params));
	}

	private <T> Request updateDocumentsRequest(final Collection<T> values, final DocumentUpdateOptions params) {
		final Request request;
		request = new Request(db.name(), RequestType.PATCH, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final Boolean keepNull = params.getKeepNull();
		request.putParameter(ArangoDBConstants.KEEP_NULL, keepNull);
		request.putParameter(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.putParameter(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.setBody(serialize(values, true));
		return request;
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseDeserializer<Collection<DocumentUpdateResult<T>>> updateDocumentsResponseDeserializer(
		final Collection<T> values,
		final DocumentUpdateOptions params) {
		return response -> {
			Class<T> type = null;
			if ((params.getReturnNew() != null && params.getReturnNew())
					|| (params.getReturnOld() != null && params.getReturnOld())) {
				final Optional<T> first = values.stream().findFirst();
				if (first.isPresent()) {
					type = (Class<T>) first.get().getClass();
				}
			}
			final Collection<DocumentUpdateResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentUpdateResult<T> doc = deserialize(next, DocumentUpdateResult.class);
				final VPackSlice newDoc = next.get(ArangoDBConstants.NEW);
				if (newDoc.isObject()) {
					doc.setNew(deserialize(newDoc, type));
				}
				final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld(deserialize(oldDoc, type));
				}
				docs.add(doc);
			}
			return docs;
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
	public DocumentDeleteResult<Void> deleteDocument(final String key) throws ArangoDBException {
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
	public <T> DocumentDeleteResult<T> deleteDocument(
		final String key,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executeSync(deleteDocumentRequest(key, options), deleteDocumentResponseDeserializer(type));
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
	 * @return information about the document
	 */
	public CompletableFuture<DocumentDeleteResult<Void>> deleteDocumentAsync(final String key) {
		return executeAsync(deleteDocumentRequest(key, new DocumentDeleteOptions()),
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
	 */
	public <T> CompletableFuture<DocumentDeleteResult<T>> deleteDocumentAsync(
		final String key,
		final Class<T> type,
		final DocumentDeleteOptions options) {
		return executeAsync(deleteDocumentRequest(key, options), deleteDocumentResponseDeserializer(type));
	}

	private Request deleteDocumentRequest(final String key, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return request;
	}

	private <T> ResponseDeserializer<DocumentDeleteResult<T>> deleteDocumentResponseDeserializer(final Class<T> type) {
		return response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentDeleteResult<T> doc = deserialize(body, DocumentDeleteResult.class);
			final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
			if (oldDoc.isObject()) {
				doc.setOld(deserialize(oldDoc, type));
			}
			return doc;
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
	public Collection<DocumentDeleteResult<Void>> deleteDocuments(final Collection<String> keys)
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
	public <T> Collection<DocumentDeleteResult<T>> deleteDocuments(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentDeleteOptions options) throws ArangoDBException {
		return executeSync(deleteDocumentsRequest(keys, options), deleteDocumentsResponseDeserializer(type));
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
	 */
	public CompletableFuture<Collection<DocumentDeleteResult<Void>>> deleteDocumentsAsync(
		final Collection<String> keys) {
		return executeAsync(deleteDocumentsRequest(keys, new DocumentDeleteOptions()),
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
	 */
	public <T> CompletableFuture<Collection<DocumentDeleteResult<T>>> deleteDocumentsAsync(
		final Collection<String> keys,
		final Class<T> type,
		final DocumentDeleteOptions options) {
		return executeAsync(deleteDocumentsRequest(keys, options), deleteDocumentsResponseDeserializer(type));
	}

	private Request deleteDocumentsRequest(final Collection<String> keys, final DocumentDeleteOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.DELETE, createPath(ArangoDBConstants.PATH_API_DOCUMENT, name));
		final DocumentDeleteOptions params = (options != null ? options : new DocumentDeleteOptions());
		request.putParameter(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.putParameter(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.setBody(serialize(keys));
		return request;
	}

	private <T> ResponseDeserializer<Collection<DocumentDeleteResult<T>>> deleteDocumentsResponseDeserializer(
		final Class<T> type) {
		return response -> {
			final Collection<DocumentDeleteResult<T>> docs = new ArrayList<>();
			final VPackSlice body = response.getBody().get();
			for (final Iterator<VPackSlice> iterator = body.arrayIterator(); iterator.hasNext();) {
				final VPackSlice next = iterator.next();
				final DocumentDeleteResult<T> doc = deserialize(next, DocumentDeleteResult.class);
				final VPackSlice oldDoc = next.get(ArangoDBConstants.OLD);
				if (oldDoc.isObject()) {
					doc.setOld(deserialize(oldDoc, type));
				}
				docs.add(doc);
			}
			return docs;
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
	 * @throws ArangoDBException
	 */
	public Boolean documentExists(final String key) throws ArangoDBException {
		return unwrap(documentExistsAsync(key, new DocumentExistsOptions()));
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
	 * @throws ArangoDBException
	 */
	public Boolean documentExists(final String key, final DocumentExistsOptions options) throws ArangoDBException {
		return unwrap(documentExistsAsync(key, options));
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
	public CompletableFuture<Boolean> documentExistsAsync(final String key) {
		final CompletableFuture<Boolean> result = new CompletableFuture<>();
		communication.executeAsync(documentExistsRequest(key, new DocumentExistsOptions()))
				.whenComplete(documentExistsResponseConsumer(result));
		return result;
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
	public CompletableFuture<Boolean> documentExistsAsync(final String key, final DocumentExistsOptions options) {
		final CompletableFuture<Boolean> result = new CompletableFuture<>();
		communication.executeAsync(documentExistsRequest(key, options))
				.whenComplete(documentExistsResponseConsumer(result));
		return result;
	}

	private Request documentExistsRequest(final String key, final DocumentExistsOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.HEAD,
				createPath(ArangoDBConstants.PATH_API_DOCUMENT, createDocumentHandle(key)));
		final DocumentExistsOptions params = (options != null ? options : new DocumentExistsOptions());
		request.putMeta(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		request.putMeta(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		return request;
	}

	private BiConsumer<? super Response, ? super Throwable> documentExistsResponseConsumer(
		final CompletableFuture<Boolean> result) {
		return (response, ex) -> {
			if (response != null) {
				result.complete(true);
			} else if (ex != null) {
				result.complete(false);
			} else {
				result.cancel(true);
			}
		};
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
	public IndexResult createHashIndex(final Collection<String> fields, final HashIndexOptions options)
			throws ArangoDBException {
		return executeSync(createHashIndexRequest(fields, options), IndexResult.class);
	}

	/**
	 * Creates a hash index for the collection, if it does not already exist.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Indexes/Hash.html#create-hash-index">API Documentation</a>
	 * @param fields
	 *            A list of attribute paths
	 * @param options
	 *            Additional options, can be null
	 * @return information about the index
	 */
	public CompletableFuture<IndexResult> createHashIndexAsync(
		final Collection<String> fields,
		final HashIndexOptions options) {
		return executeAsync(createHashIndexRequest(fields, options), IndexResult.class);
	}

	private Request createHashIndexRequest(final Collection<String> fields, final HashIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
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
	public IndexResult createSkiplistIndex(final Collection<String> fields, final SkiplistIndexOptions options)
			throws ArangoDBException {
		return executeSync(createSkiplistIndexRequest(fields, options), IndexResult.class);
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
	 */
	public CompletableFuture<IndexResult> createSkiplistIndexAsync(
		final Collection<String> fields,
		final SkiplistIndexOptions options) {
		return executeAsync(createSkiplistIndexRequest(fields, options), IndexResult.class);
	}

	private Request createSkiplistIndexRequest(final Collection<String> fields, final SkiplistIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
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
	public IndexResult createPersistentIndex(final Collection<String> fields, final PersistentIndexOptions options)
			throws ArangoDBException {
		return executeSync(createPersistentIndexRequest(fields, options), IndexResult.class);
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
	 */
	public CompletableFuture<IndexResult> createPersistentIndexAsync(
		final Collection<String> fields,
		final PersistentIndexOptions options) {
		return executeAsync(createPersistentIndexRequest(fields, options), IndexResult.class);
	}

	private Request createPersistentIndexRequest(
		final Collection<String> fields,
		final PersistentIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
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
	public IndexResult createGeoIndex(final Collection<String> fields, final GeoIndexOptions options)
			throws ArangoDBException {
		return executeSync(createGeoIndexRequest(fields, options), IndexResult.class);
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
	 */
	public CompletableFuture<IndexResult> createGeoIndexAsync(
		final Collection<String> fields,
		final GeoIndexOptions options) {
		return executeAsync(createGeoIndexRequest(fields, options), IndexResult.class);
	}

	private Request createGeoIndexRequest(final Collection<String> fields, final GeoIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
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
	public IndexResult createFulltextIndex(final Collection<String> fields, final FulltextIndexOptions options)
			throws ArangoDBException {
		return executeSync(createFulltextIndexRequest(fields, options), IndexResult.class);
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
	 */
	public CompletableFuture<IndexResult> createFulltextIndexAsync(
		final Collection<String> fields,
		final FulltextIndexOptions options) {
		return executeAsync(createFulltextIndexRequest(fields, options), IndexResult.class);
	}

	private Request createFulltextIndexRequest(final Collection<String> fields, final FulltextIndexOptions options) {
		final Request request;
		request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
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
	public Collection<IndexResult> getIndexes() throws ArangoDBException {
		return executeSync(getIndexesRequest(), getIndexesResponseDeserializer());
	}

	/**
	 * Returns all indexes of the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Indexes/WorkingWith.html#read-all-indexes-of-a-collection">API
	 *      Documentation</a>
	 * @return information about the indexes
	 */
	public CompletableFuture<Collection<IndexResult>> getIndexesAsync() {
		return executeAsync(getIndexesRequest(), getIndexesResponseDeserializer());
	}

	private Request getIndexesRequest() {
		final Request request;
		request = new Request(db.name(), RequestType.GET, ArangoDBConstants.PATH_API_INDEX);
		request.putParameter(ArangoDBConstants.COLLECTION, name);
		return request;
	}

	private ResponseDeserializer<Collection<IndexResult>> getIndexesResponseDeserializer() {
		return response -> deserialize(response.getBody().get().get(ArangoDBConstants.INDEXES),
			new Type<Collection<IndexResult>>() {
			}.getType());
	}

	/**
	 * Removes all documents from the collection, but leaves the indexes intact
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#truncate-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 * @throws ArangoDBException
	 */
	public CollectionResult truncate() throws ArangoDBException {
		return executeSync(truncateRequest(), CollectionResult.class);
	}

	/**
	 * Removes all documents from the collection, but leaves the indexes intact
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#truncate-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionResult> truncateAsync() {
		return executeAsync(truncateRequest(), CollectionResult.class);
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
	public CollectionPropertiesResult count() throws ArangoDBException {
		return executeSync(countRequest(), CollectionPropertiesResult.class);
	}

	/**
	 * Counts the documents in a collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-number-of-documents-in-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection, including the number of documents
	 */
	public CompletableFuture<CollectionPropertiesResult> countAsync() {
		return executeAsync(countRequest(), CollectionPropertiesResult.class);
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

	/**
	 * Drops the collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Creating.html#drops-collection">API
	 *      Documentation</a>
	 * @return void
	 */
	public CompletableFuture<Void> dropAsync() {
		return executeAsync(dropRequest(), Void.class);
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
	public CollectionResult load() throws ArangoDBException {
		return executeSync(loadRequest(), CollectionResult.class);
	}

	/**
	 * Loads a collection into memory.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#load-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionResult> loadAsync() {
		return executeAsync(loadRequest(), CollectionResult.class);
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
	public CollectionResult unload() throws ArangoDBException {
		return executeSync(unloadRequest(), CollectionResult.class);
	}

	/**
	 * Removes a collection from memory. This call does not delete any documents. You can use the collection afterwards;
	 * in which case it will be loaded into memory, again.
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#unload-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionResult> unloadAsync() {
		return executeAsync(unloadRequest(), CollectionResult.class);
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
	public CollectionResult getInfo() throws ArangoDBException {
		return executeSync(getInfoRequest(), CollectionResult.class);
	}

	/**
	 * Returns information about the collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-information-about-a-collection">API
	 *      Documentation</a>
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionResult> getInfoAsync() {
		return executeAsync(getInfoRequest(), CollectionResult.class);
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
	public CollectionPropertiesResult getProperties() throws ArangoDBException {
		return executeSync(getPropertiesRequest(), CollectionPropertiesResult.class);
	}

	/**
	 * Reads the properties of the specified collection
	 * 
	 * @see <a href=
	 *      "https://docs.arangodb.com/current/HTTP/Collection/Getting.html#read-properties-of-a-collection">API
	 *      Documentation</a>
	 * @return properties of the collection
	 */
	public CompletableFuture<CollectionPropertiesResult> getPropertiesAsync() {
		return executeAsync(getPropertiesRequest(), CollectionPropertiesResult.class);
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
	public CollectionPropertiesResult changeProperties(final CollectionPropertiesOptions options)
			throws ArangoDBException {
		return executeSync(changePropertiesRequest(options), CollectionPropertiesResult.class);
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
	 */
	public CompletableFuture<CollectionPropertiesResult> changePropertiesAsync(
		final CollectionPropertiesOptions options) {
		return executeAsync(changePropertiesRequest(options), CollectionPropertiesResult.class);
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
	public CollectionResult rename(final String newName) throws ArangoDBException {
		return executeSync(renameRequest(newName), CollectionResult.class);
	}

	/**
	 * Renames a collection
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Modifying.html#rename-collection">API
	 *      Documentation</a>
	 * @param newName
	 *            The new name
	 * @return information about the collection
	 */
	public CompletableFuture<CollectionResult> renameAsync(final String newName) {
		return executeAsync(renameRequest(newName), CollectionResult.class);
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
	public CollectionRevisionResult getRevision() throws ArangoDBException {
		return executeSync(getRevisionRequest(), CollectionRevisionResult.class);
	}

	/**
	 * Retrieve the collections revision
	 * 
	 * @see <a href="https://docs.arangodb.com/current/HTTP/Collection/Getting.html#return-collection-revision-id">API
	 *      Documentation</a>
	 * @return information about the collection, including the collections revision
	 */
	public CompletableFuture<CollectionRevisionResult> getRevisionAsync() {
		return executeAsync(getRevisionRequest(), CollectionRevisionResult.class);
	}

	private Request getRevisionRequest() {
		return new Request(db.name(), RequestType.GET,
				createPath(ArangoDBConstants.PATH_API_COLLECTION, name, ArangoDBConstants.REVISION));
	}

}

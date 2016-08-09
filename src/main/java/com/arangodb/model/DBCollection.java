package com.arangodb.model;

import java.util.Map;

import com.arangodb.entity.DocumentCreateResult;
import com.arangodb.entity.DocumentDeleteResult;
import com.arangodb.entity.DocumentUpdateResult;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DBCollection extends ExecuteBase {

	private final DB db;
	private final String name;

	protected DBCollection(final DB db, final String name) {
		super(db);
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
		return String.format("%s/%s", name, key);
	}

	public <T> Executeable<DocumentCreateResult<T>> createDocument(
		final T value,
		final DocumentCreate.Options options) {
		// TODO set key, id, rev in value
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_DOCUMENT + name);
		final DocumentCreate params = (options != null ? options : new DocumentCreate.Options()).build();
		final Map<String, Object> parameter = request.getParameter();
		parameter.put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		parameter.put(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		request.setBody(serialize(value));
		return execute(DocumentCreateResult.class, request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentCreateResult<T> doc = deserialize(body, DocumentCreateResult.class);
			final VPackSlice newDoc = body.get(ArangoDBConstants.NEW);
			if (newDoc.isObject()) {
				doc.setNew(deserialize(newDoc, value.getClass()));
			}
			return doc;
		});
	}

	public <T> Executeable<T> readDocument(final String key, final Class<T> type, final DocumentRead.Options options) {
		final Request request = new Request(db.name(), RequestType.GET,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentRead params = (options != null ? options : new DocumentRead.Options()).build();
		request.getMeta().put(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(type, request);
	}

	public <T> Executeable<DocumentUpdateResult<T>> replaceDocument(
		final String key,
		final T value,
		final DocumentReplace.Options options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentReplace params = (options != null ? options : new DocumentReplace.Options()).build();
		final Map<String, Object> parameter = request.getParameter();
		parameter.put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		parameter.put(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		parameter.put(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		parameter.put(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(DocumentUpdateResult.class, request, response -> {
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

	public <T> Executeable<DocumentUpdateResult<T>> updateDocument(
		final String key,
		final T value,
		final DocumentUpdate.Options options) {
		final Request request = new Request(db.name(), RequestType.PATCH,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentUpdate params = (options != null ? options : new DocumentUpdate.Options()).build();
		final Map<String, Object> parameter = request.getParameter();
		parameter.put(ArangoDBConstants.KEEP_NULL, params.getKeepNull());
		parameter.put(ArangoDBConstants.MERGE_OBJECTS, params.getMergeObjects());
		parameter.put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		parameter.put(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		parameter.put(ArangoDBConstants.RETURN_NEW, params.getReturnNew());
		parameter.put(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(DocumentUpdateResult.class, request, response -> {
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

	public <T> Executeable<DocumentDeleteResult<T>> deleteDocument(
		final String key,
		final Class<T> type,
		final DocumentDelete.Options options) {
		final Request request = new Request(db.name(), RequestType.DELETE,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentDelete params = (options != null ? options : new DocumentDelete.Options()).build();
		final Map<String, Object> parameter = request.getParameter();
		parameter.put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		parameter.put(ArangoDBConstants.RETURN_OLD, params.getReturnOld());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(Void.class, request, response -> {
			final VPackSlice body = response.getBody().get();
			final DocumentDeleteResult<T> doc = deserialize(body, DocumentDeleteResult.class);
			final VPackSlice oldDoc = body.get(ArangoDBConstants.OLD);
			if (oldDoc.isObject()) {
				doc.setOld(deserialize(oldDoc, type));
			}
			return doc;
		});
	}

}

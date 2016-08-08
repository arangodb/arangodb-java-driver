package com.arangodb.model;

import java.util.Map;

import com.arangodb.entity.DocumentEntity;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.velocystream.RequestType;

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

	public <T> Executeable<DocumentEntity> createDocument(final T value, final DocumentCreate.Options options) {
		// TODO set key, id, rev in value
		final Request request = new Request(db.name(), RequestType.POST, ArangoDBConstants.PATH_API_DOCUMENT + name);
		request.getParameter().put(ArangoDBConstants.WAIT_FOR_SYNC,
			(options != null ? options : new DocumentCreate.Options()).build().getWaitForSync());
		request.setBody(serialize(value));
		return execute(DocumentEntity.class, request);
	}

	public <T> Executeable<T> readDocument(final String key, final Class<T> type, final DocumentRead.Options options) {
		final Request request = new Request(db.name(), RequestType.GET,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentRead params = (options != null ? options : new DocumentRead.Options()).build();
		request.getMeta().put(ArangoDBConstants.IF_NONE_MATCH, params.getIfNoneMatch());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		return execute(type, request);
	}

	public <T> Executeable<DocumentEntity> replaceDocument(
		final String key,
		final T value,
		final DocumentReplace.Options options) {
		final Request request = new Request(db.name(), RequestType.PUT,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentReplace params = (options != null ? options : new DocumentReplace.Options()).build();
		final Map<String, Object> parameter = request.getParameter();
		parameter.put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		parameter.put(ArangoDBConstants.IGNORE_REVS, params.getIgnoreRevs());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		// TODO returnOld , returnNew
		return execute(DocumentEntity.class, request);
	}

	public <T> Executeable<DocumentEntity> updateDocument(
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
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		// TODO returnOld , returnNew
		return execute(DocumentEntity.class, request);
	}

	public Executeable<Void> deleteDocument(final String key, final DocumentDelete.Options options) {
		final Request request = new Request(db.name(), RequestType.DELETE,
				ArangoDBConstants.PATH_API_DOCUMENT + createDocumentHandle(key));
		final DocumentDelete params = (options != null ? options : new DocumentDelete.Options()).build();
		request.getParameter().put(ArangoDBConstants.WAIT_FOR_SYNC, params.getWaitForSync());
		request.getMeta().put(ArangoDBConstants.IF_MATCH, params.getIfMatch());
		// TODO returnOld
		return execute(Void.class, request);
	}

}

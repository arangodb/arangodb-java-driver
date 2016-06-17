/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.impl;

import java.util.List;
import java.util.Map;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EdgeEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.EdgeUtils;
import com.arangodb.util.MapBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalDocumentDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalDocumentDriver {

	private static final String WAIT_FOR_SYNC = "waitForSync";

	InternalDocumentDriverImpl(final ArangoConfigure configure, final HttpManager httpManager) {
		super(configure, httpManager);
	}

	private <T> DocumentEntity<T> internalCreateDocument(
		final String database,
		final String collectionName,
		final String documentKey,
		final T value,
		final Boolean waitForSync,
		final boolean raw) throws ArangoException {

		validateCollectionName(collectionName);

		String body;
		if (raw) {
			body = value.toString();
		} else if (documentKey != null) {
			final JsonElement elem = EntityFactory.toJsonElement(value, false);
			if (elem.isJsonObject()) {
				elem.getAsJsonObject().addProperty(BaseDocument.KEY, documentKey);
			}
			body = EntityFactory.toJsonString(elem);
		} else {
			body = EntityFactory.toJsonString(value);
		}

		final HttpResponseEntity res = httpManager.doPost(createDocumentEndpointUrl(database),
			new MapBuilder().put(COLLECTION, collectionName).put(WAIT_FOR_SYNC, waitForSync).get(), body);

		@SuppressWarnings("unchecked")
		final DocumentEntity<T> result = createEntity(res, DocumentEntity.class);

		annotationHandler.updateDocumentAttributes(value, result.getDocumentRevision(), result.getDocumentHandle(),
			result.getDocumentKey());

		result.setEntity(value);
		return result;
	}

	@Override
	public <T> DocumentEntity<T> createDocument(
		final String database,
		final String collectionName,
		final String documentKey,
		final T value,
		final Boolean waitForSync) throws ArangoException {
		return internalCreateDocument(database, collectionName, documentKey, value, waitForSync, false);
	}

	@Override
	public DocumentEntity<String> createDocumentRaw(
		final String database,
		final String collectionName,
		final String rawJsonObjectString,
		final Boolean waitForSync) throws ArangoException {
		return internalCreateDocument(database, collectionName, null, rawJsonObjectString, waitForSync, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DocumentEntity<T> replaceDocument(
		final String database,
		final String documentHandle,
		final T value,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {

		validateDocumentHandle(documentHandle);

		final HttpResponseEntity res = httpManager.doPut(createDocumentEndpointUrl(database, documentHandle),
			createRevisionCheckHeader(rev), new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(),
			EntityFactory.toJsonString(value));

		final DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentRev(value, result.getDocumentRevision());
		result.setEntity(value);
		return result;
	}

	@Override
	public DocumentEntity<String> replaceDocumentRaw(
		final String database,
		final String documentHandle,
		final String rawJsonString,
		final Long rev,
		final Boolean waitForSync) throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doPut(createDocumentEndpointUrl(database, documentHandle),
			createRevisionCheckHeader(rev), new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), rawJsonString);

		@SuppressWarnings("unchecked")
		final DocumentEntity<String> result = createEntity(res, DocumentEntity.class);
		result.setEntity(rawJsonString);
		return result;
	}

	@Override
	public <T> DocumentEntity<T> updateDocument(
		final String database,
		final String documentHandle,
		final T value,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doPatch(createDocumentEndpointUrl(database, documentHandle),
			createRevisionCheckHeader(rev),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).put("keepNull", keepNull).get(),
			EntityFactory.toJsonString(value, keepNull != null && !keepNull));

		@SuppressWarnings("unchecked")
		final DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentAttributes(value, result.getDocumentRevision(), result.getDocumentHandle(),
			result.getDocumentKey());
		result.setEntity(value);
		return result;
	}

	@Override
	public DocumentEntity<String> updateDocumentRaw(
		final String database,
		final String documentHandle,
		final String rawJsonString,
		final Long rev,
		final Boolean waitForSync,
		final Boolean keepNull) throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doPatch(createDocumentEndpointUrl(database, documentHandle),
			createRevisionCheckHeader(rev),
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).put("keepNull", keepNull).get(), rawJsonString);

		@SuppressWarnings("unchecked")
		final DocumentEntity<String> result = createEntity(res, DocumentEntity.class);
		result.setEntity(rawJsonString);
		return result;
	}

	@Override
	public List<String> getDocuments(final String database, final String collectionName) throws ArangoException {
		final HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/all-keys"), null,
			EntityFactory.toJsonString(new MapBuilder().put(COLLECTION, collectionName).put("type", "id").get()));

		@SuppressWarnings("unchecked")
		final CursorEntity<String> tmp = createEntity(res, CursorEntity.class, String.class);

		return tmp.getResults();
	}

	@Override
	public long checkDocument(final String database, final String documentHandle) throws ArangoException {
		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doHead(createDocumentEndpointUrl(database, documentHandle), null);

		final DefaultEntity entity = createEntity(res, DefaultEntity.class);
		return entity.getEtag();

	}

	@Override
	public <T> DocumentEntity<T> getDocument(
		final String database,
		final String documentHandle,
		final Class<T> clazz,
		final Long ifNoneMatchRevision,
		final Long ifMatchRevision) throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doGet(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision).get(),
			null);
		@SuppressWarnings("unchecked")
		DocumentEntity<T> entity = createEntity(res, DocumentEntity.class, clazz);
		if (entity == null) {
			entity = new DocumentEntity<T>();
		}
		return entity;
	}

	@Override
	public String getDocumentRaw(
		final String database,
		final String documentHandle,
		final Long ifNoneMatchRevision,
		final Long ifMatchRevision) throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doGet(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision).get(),
			null);

		if (res.getStatusCode() >= 400) {
			final BaseDocument entity = new BaseDocument();
			entity.setError(true);
			entity.setCode(res.getStatusCode());
			entity.setStatusCode(res.getStatusCode());
			entity.setErrorNumber(res.getStatusCode());
			entity.setErrorMessage(res.getText());
			throw new ArangoException(entity);
		}

		return res.getText();
	}

	@Override
	public DocumentEntity<?> deleteDocument(final String database, final String documentHandle, final Long rev)
			throws ArangoException {

		validateDocumentHandle(documentHandle);
		final HttpResponseEntity res = httpManager.doDelete(createDocumentEndpointUrl(database, documentHandle),
			createRevisionCheckHeader(rev), null);
		return createEntity(res, DocumentEntity.class);
	}

	private Map<String, Object> createRevisionCheckHeader(final Long rev) {
		Map<String, Object> header = null;
		if (rev != null) {
			final MapBuilder mapBuilder = new MapBuilder().put("If-Match", rev);
			header = mapBuilder.get();
		}
		return header;
	}

	@Override
	public <T> EdgeEntity<T> createEdge(
		String database,
		String collectionName,
		String documentKey,
		T value,
		String fromHandle,
		String toHandle,
		Boolean waitForSync) throws ArangoException {

		validateCollectionName(collectionName);

		JsonObject obj = EdgeUtils.valueToEdgeJsonObject(documentKey, fromHandle, toHandle, value);

		final HttpResponseEntity res = httpManager.doPost(createDocumentEndpointUrl(database),
			new MapBuilder().put(COLLECTION, collectionName).put(WAIT_FOR_SYNC, waitForSync).get(),
			EntityFactory.toJsonString(obj));

		@SuppressWarnings("unchecked")
		final EdgeEntity<T> entity = createEntity(res, EdgeEntity.class);

		if (value != null) {
			entity.setEntity(value);
			annotationHandler.updateDocumentAttributes(value, entity.getDocumentRevision(), entity.getDocumentHandle(),
				entity.getDocumentKey());
		}

		entity.setFromVertexHandle(fromHandle);
		entity.setToVertexHandle(toHandle);
		return entity;
	}
}

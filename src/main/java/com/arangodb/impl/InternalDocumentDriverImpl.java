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
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CursorEntity;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.Policy;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.MapBuilder;
import com.google.gson.JsonElement;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalDocumentDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalDocumentDriver {

	private static final String WAIT_FOR_SYNC = "waitForSync";

	private static final String POLICY = "policy";

	private static final String API_DOCUMENT_PREFIX = "/_api/document/";

	private static final Pattern pattern = Pattern.compile("^/_db/.*/_api/document/(.*)$");

	InternalDocumentDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	private <T> DocumentEntity<T> internalCreateDocument(
		String database,
		String collectionName,
		String documentKey,
		T value,
		Boolean createCollection,
		Boolean waitForSync,
		boolean raw) throws ArangoException {

		validateCollectionName(collectionName);

		String body;
		if (raw) {
			body = value.toString();
		} else if (documentKey != null) {
			JsonElement elem = EntityFactory.toJsonElement(value, false);
			if (elem.isJsonObject()) {
				elem.getAsJsonObject().addProperty(BaseDocument.KEY, documentKey);
			}
			body = EntityFactory.toJsonString(elem);
		} else {
			body = EntityFactory.toJsonString(value);
		}

		HttpResponseEntity res = httpManager.doPost(createDocumentEndpointUrl(database),
			new MapBuilder().put("collection", collectionName).put("createCollection", createCollection)
					.put(WAIT_FOR_SYNC, waitForSync).get(),
			body);

		@SuppressWarnings("unchecked")
		DocumentEntity<T> result = createEntity(res, DocumentEntity.class);

		annotationHandler.updateDocumentAttributes(value, result.getDocumentRevision(), result.getDocumentHandle(),
			result.getDocumentKey());

		result.setEntity(value);
		return result;
	}

	@Override
	public <T> DocumentEntity<T> createDocument(
		String database,
		String collectionName,
		String documentKey,
		T value,
		Boolean createCollection,
		Boolean waitForSync) throws ArangoException {
		return internalCreateDocument(database, collectionName, documentKey, value, createCollection, waitForSync,
			false);
	}

	@Override
	public DocumentEntity<String> createDocumentRaw(
		String database,
		String collectionName,
		String rawJsonObjectString,
		Boolean createCollection,
		Boolean waitForSync) throws ArangoException {
		return internalCreateDocument(database, collectionName, null, rawJsonObjectString, createCollection,
			waitForSync, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> DocumentEntity<T> replaceDocument(
		String database,
		String documentHandle,
		T value,
		Long rev,
		Policy policy,
		Boolean waitForSync) throws ArangoException {

		validateDocumentHandle(documentHandle);

		Map<String, Object> header = null;
		if (rev != null) {
			MapBuilder mapBuilder = new MapBuilder().put("If-Match", rev);
			header = mapBuilder.get();
		}

		HttpResponseEntity res = httpManager.doPut(createDocumentEndpointUrl(database, documentHandle), header,
			new MapBuilder().put(WAIT_FOR_SYNC, waitForSync).get(), EntityFactory.toJsonString(value));

		DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentRev(value, result.getDocumentRevision());
		result.setEntity(value);
		return result;
	}

	@Override
	public DocumentEntity<String> replaceDocumentRaw(
		String database,
		String documentHandle,
		String rawJsonString,
		Long rev,
		Policy policy,
		Boolean waitForSync) throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doPut(
			createDocumentEndpointUrl(database, documentHandle), new MapBuilder().put("rev", rev)
					.put(POLICY, policy == null ? null : policy.name()).put(WAIT_FOR_SYNC, waitForSync).get(),
			rawJsonString);

		@SuppressWarnings("unchecked")
		DocumentEntity<String> result = createEntity(res, DocumentEntity.class);
		result.setEntity(rawJsonString);
		return result;
	}

	@Override
	public <T> DocumentEntity<T> updateDocument(
		String database,
		String documentHandle,
		T value,
		Long rev,
		Policy policy,
		Boolean waitForSync,
		Boolean keepNull) throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doPatch(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("rev", rev).put(POLICY, policy == null ? null : policy.name())
					.put(WAIT_FOR_SYNC, waitForSync).put("keepNull", keepNull).get(),
			EntityFactory.toJsonString(value, keepNull != null && !keepNull));

		@SuppressWarnings("unchecked")
		DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentAttributes(value, result.getDocumentRevision(), result.getDocumentHandle(),
			result.getDocumentKey());
		result.setEntity(value);
		return result;
	}

	@Override
	public DocumentEntity<String> updateDocumentRaw(
		String database,
		String documentHandle,
		String rawJsonString,
		Long rev,
		Policy policy,
		Boolean waitForSync,
		Boolean keepNull) throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doPatch(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("rev", rev).put(POLICY, policy == null ? null : policy.name())
					.put(WAIT_FOR_SYNC, waitForSync).put("keepNull", keepNull).get(),
			rawJsonString);

		@SuppressWarnings("unchecked")
		DocumentEntity<String> result = createEntity(res, DocumentEntity.class);
		result.setEntity(rawJsonString);
		return result;
	}

	@Override
	public List<String> getDocuments(String database, String collectionName) throws ArangoException {
		HttpResponseEntity res = httpManager.doPut(createEndpointUrl(database, "/_api/simple/all-keys"), null,
			EntityFactory.toJsonString(new MapBuilder().put("collection", collectionName).put("type", "id").get()));

		@SuppressWarnings("unchecked")
		CursorEntity<String> tmp = createEntity(res, CursorEntity.class, String.class);

		return tmp.getResults();
	}

	private void updateDocumentHandles(List<String> documents) {
		ListIterator<String> lit = documents.listIterator();
		while (lit.hasNext()) {
			String d = lit.next();
			if (d.startsWith(API_DOCUMENT_PREFIX)) {
				lit.set(d.substring(API_DOCUMENT_PREFIX.length()));
			} else {
				Matcher matcher = pattern.matcher(d);
				if (matcher.find()) {
					lit.set(matcher.group(1));
				}
			}
		}
	}

	@Override
	public long checkDocument(String database, String documentHandle) throws ArangoException {
		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doHead(createDocumentEndpointUrl(database, documentHandle), null);

		DefaultEntity entity = createEntity(res, DefaultEntity.class);
		return entity.getEtag();

	}

	@Override
	public <T> DocumentEntity<T> getDocument(
		String database,
		String documentHandle,
		Class<T> clazz,
		Long ifNoneMatchRevision,
		Long ifMatchRevision) throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doGet(createDocumentEndpointUrl(database, documentHandle),
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
	public String getDocumentRaw(String database, String documentHandle, Long ifNoneMatchRevision, Long ifMatchRevision)
			throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doGet(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("If-None-Match", ifNoneMatchRevision, true).put("If-Match", ifMatchRevision).get(),
			null);

		if (res.getStatusCode() >= 400) {
			BaseDocument entity = new BaseDocument();
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
	public DocumentEntity<?> deleteDocument(String database, String documentHandle, Long rev, Policy policy)
			throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doDelete(createDocumentEndpointUrl(database, documentHandle),
			new MapBuilder().put("rev", rev).put(POLICY, policy == null ? null : policy.name().toLowerCase(Locale.US))
					.get());

		return createEntity(res, DocumentEntity.class);
	}

}

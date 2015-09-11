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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DefaultEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.DocumentsEntity;
import com.arangodb.entity.EntityFactory;
import com.arangodb.entity.Policy;
import com.arangodb.http.HttpManager;
import com.arangodb.http.HttpResponseEntity;
import com.arangodb.util.CollectionUtils;
import com.arangodb.util.MapBuilder;
import com.google.gson.JsonElement;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class InternalDocumentDriverImpl extends BaseArangoDriverImpl implements com.arangodb.InternalDocumentDriver {

	InternalDocumentDriverImpl(ArangoConfigure configure, HttpManager httpManager) {
		super(configure, httpManager);
	}

	private <T> DocumentEntity<T> _createDocument(
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

		HttpResponseEntity res = httpManager.doPost(createEndpointUrl(database, "/_api/document"),
			new MapBuilder().put("collection", collectionName).put("createCollection", createCollection)
					.put("waitForSync", waitForSync).get(),
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
		return _createDocument(database, collectionName, documentKey, value, createCollection, waitForSync, false);
	}

	@Override
	public DocumentEntity<String> createDocumentRaw(
		String database,
		String collectionName,
		String documentKey,
		String rawJsonString,
		Boolean createCollection,
		Boolean waitForSync) throws ArangoException {
		return _createDocument(database, collectionName, documentKey, rawJsonString, createCollection, waitForSync,
			true);
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
		HttpResponseEntity res = httpManager.doPut(
			createEndpointUrl(database, "/_api/document", documentHandle), new MapBuilder().put("rev", rev)
					.put("policy", policy == null ? null : policy.name()).put("waitForSync", waitForSync).get(),
			EntityFactory.toJsonString(value));

		DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentRev(value, result.getDocumentRevision());
		result.setEntity(value);
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
		HttpResponseEntity res = httpManager.doPatch(createEndpointUrl(database, "/_api/document", documentHandle),
			new MapBuilder().put("rev", rev).put("policy", policy == null ? null : policy.name())
					.put("waitForSync", waitForSync).put("keepNull", keepNull).get(),
			EntityFactory.toJsonString(value, keepNull != null && !keepNull));

		@SuppressWarnings("unchecked")
		DocumentEntity<T> result = createEntity(res, DocumentEntity.class);
		annotationHandler.updateDocumentAttributes(value, result.getDocumentRevision(), result.getDocumentHandle(),
			result.getDocumentKey());
		result.setEntity(value);
		return result;
	}

	private static final String API_DOCUMENT_PREFIX = "/_api/document/";
	private static final Pattern pattern = Pattern.compile("^/_db/.*/_api/document/(.*)$");

	@Override
	public List<String> getDocuments(String database, String collectionName, boolean handleConvert)
			throws ArangoException {

		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/document"),
			new MapBuilder("collection", collectionName).get());

		DocumentsEntity entity = createEntity(res, DocumentsEntity.class);
		List<String> documents = CollectionUtils.safety(entity.getDocuments());

		if (handleConvert && !documents.isEmpty()) {
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
		return documents;
	}

	@Override
	public long checkDocument(String database, String documentHandle) throws ArangoException {
		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doHead(createEndpointUrl(database, "/_api/document", documentHandle),
			null);

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
		HttpResponseEntity res = httpManager.doGet(createEndpointUrl(database, "/_api/document", documentHandle),
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
	public DocumentEntity<?> deleteDocument(String database, String documentHandle, Long rev, Policy policy)
			throws ArangoException {

		validateDocumentHandle(documentHandle);
		HttpResponseEntity res = httpManager.doDelete(createEndpointUrl(database, "/_api/document", documentHandle),
			new MapBuilder().put("rev", rev).put("policy", policy == null ? null : policy.name().toLowerCase(Locale.US))
					.get());

		try {
			DocumentEntity<?> entity = createEntity(res, DocumentEntity.class);
			return entity;
		} catch (ArangoException e) {
			throw e;
		}

	}

}

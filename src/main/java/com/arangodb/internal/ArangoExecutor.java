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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.velocystream.Communication;
import com.arangodb.internal.velocystream.Connection;
import com.arangodb.util.ArangoUtil;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ArangoExecutor<R, C extends Connection> {

	public static interface ResponseDeserializer<T> {
		T deserialize(Response response) throws VPackException;
	}

	private static final String REGEX_DOCUMENT_KEY = "[^/]+";
	private static final String REGEX_DOCUMENT_ID = "[^/]+/[^/]+";

	private final Communication<R, C> communication;
	private final DocumentCache documentCache;
	private final CollectionCache collectionCache;
	private final ArangoUtil util;

	protected ArangoExecutor(final Communication<R, C> communication, final VPack vpacker, final VPack vpackerNull,
		final VPackParser vpackParser, final DocumentCache documentCache, final CollectionCache collectionCache) {
		super();
		this.communication = communication;
		this.documentCache = documentCache;
		this.collectionCache = collectionCache;
		util = new ArangoUtil(vpacker, vpackerNull, vpackParser);
	}

	public Communication<R, C> communication() {
		return communication;
	}

	public DocumentCache documentCache() {
		return documentCache;
	}

	protected CollectionCache collectionCache() {
		return collectionCache;
	}

	protected ArangoUtil util() {
		return util;
	}

	protected String createPath(final String... params) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				sb.append("/");
			}
			try {
				final String param;
				if (params[i].contains("/") || params[i].contains(" ")) {
					param = params[i];
				} else {
					param = URLEncoder.encode(params[i], "UTF-8");
				}
				sb.append(param);
			} catch (final UnsupportedEncodingException e) {
				throw new ArangoDBException(e);
			}
		}
		return sb.toString();
	}

	public void validateDocumentKey(final String key) throws ArangoDBException {
		validateName("document key", REGEX_DOCUMENT_KEY, key);
	}

	public void validateDocumentId(final String id) throws ArangoDBException {
		validateName("document id", REGEX_DOCUMENT_ID, id);
	}

	protected void validateName(final String type, final String regex, final CharSequence name)
			throws ArangoDBException {
		if (!Pattern.matches(regex, name)) {
			throw new ArangoDBException(String.format("%s %s is not valid.", type, name));
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T createResult(final Type type, final Response response) {
		return (T) ((type != Void.class && response.getBody() != null) ? deserialize(response.getBody(), type) : null);
	}

	protected <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		return util.deserialize(vpack, type);
	}

	protected VPackSlice serialize(final Object entity) throws ArangoDBException {
		return util.serialize(entity);
	}

	protected VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		return util.serialize(entity, serializeNullValues);
	}

	protected VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		return util.serialize(entity, type);
	}

	protected VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		return util.serialize(entity, type, serializeNullValues);
	}

	protected VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		return util.serialize(entity, type, additionalFields);
	}

}

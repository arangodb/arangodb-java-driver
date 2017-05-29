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
import com.arangodb.util.ArangoSerializer;
import com.arangodb.util.ArangoSerialization;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocystream.Response;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ArangoExecutor<R, C extends Connection> {

	private static final String SLASH = "/";

	public static interface ResponseDeserializer<T> {
		T deserialize(Response response) throws VPackException;
	}

	protected static final String REGEX_KEY = "[^/]+";
	protected static final String REGEX_ID = "[^/]+/[^/]+";

	private final Communication<R, C> communication;
	private final DocumentCache documentCache;
	private final CollectionCache collectionCache;
	private final ArangoSerialization util;

	protected ArangoExecutor(final Communication<R, C> communication, final ArangoSerialization util,
		final DocumentCache documentCache, final CollectionCache collectionCache) {
		super();
		this.communication = communication;
		this.documentCache = documentCache;
		this.collectionCache = collectionCache;
		this.util = util;
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

	protected ArangoSerialization util() {
		return util;
	}

	protected String createPath(final String... params) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				sb.append(SLASH);
			}
			try {
				final String param;
				if (params[i].contains(SLASH)) {
					param = createPath(params[i].split(SLASH));
				} else {
					param = encode(params[i]);
				}
				sb.append(param);
			} catch (final UnsupportedEncodingException e) {
				throw new ArangoDBException(e);
			}
		}
		return sb.toString();
	}

	private String encode(final String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!")
				.replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
	}

	public void validateIndexId(final String id) {
		validateName("index id", REGEX_ID, id);
	}

	public void validateDocumentKey(final String key) throws ArangoDBException {
		validateName("document key", REGEX_KEY, key);
	}

	public void validateDocumentId(final String id) throws ArangoDBException {
		validateName("document id", REGEX_ID, id);
	}

	public String createDocumentHandle(final String collection, final String key) {
		validateDocumentKey(key);
		return new StringBuffer().append(collection).append(SLASH).append(key).toString();
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

	@Deprecated
	protected <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		return util.deserialize(vpack, type);
	}

	@Deprecated
	protected VPackSlice serialize(final Object entity) throws ArangoDBException {
		return util.serialize(entity);
	}

	@Deprecated
	protected VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		return util.serialize(entity, new ArangoSerializer.Options().serializeNullValues(serializeNullValues));
	}

	@Deprecated
	protected VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		return util.serialize(entity, new ArangoSerializer.Options().type(type));
	}

	@Deprecated
	protected VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		return util.serialize(entity,
			new ArangoSerializer.Options().type(type).serializeNullValues(serializeNullValues));
	}

	@Deprecated
	protected VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		return util.serialize(entity, new ArangoSerializer.Options().type(type).additionalFields(additionalFields));
	}

}

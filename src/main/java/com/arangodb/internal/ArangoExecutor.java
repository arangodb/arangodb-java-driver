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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.velocystream.Communication;
import com.arangodb.internal.velocystream.Connection;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;
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

	protected final Communication<R, C> communication;
	protected final VPack vpacker;
	protected final VPack vpackerNull;
	protected final VPackParser vpackParser;
	protected final DocumentCache documentCache;
	protected final CollectionCache collectionCache;

	protected ArangoExecutor(final Communication<R, C> communication, final VPack vpacker, final VPack vpackerNull,
		final VPackParser vpackParser, final DocumentCache documentCache, final CollectionCache collectionCache) {
		super();
		this.communication = communication;
		this.vpacker = vpacker;
		this.vpackerNull = vpackerNull;
		this.vpackParser = vpackParser;
		this.documentCache = documentCache;
		this.collectionCache = collectionCache;
	}

	public Communication<R, C> communication() {
		return communication;
	}

	protected VPack vpack() {
		return vpacker;
	}

	protected VPack vpackNull() {
		return vpackerNull;
	}

	protected VPackParser vpackParser() {
		return vpackParser;
	}

	public DocumentCache documentCache() {
		return documentCache;
	}

	protected CollectionCache collectionCache() {
		return collectionCache;
	}

	protected String createPath(final String... params) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				sb.append("/");
			}
			sb.append(params[i]);
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
	protected <T> T createResult(
		final VPack vpack,
		final VPackParser vpackParser,
		final Type type,
		final Response response) {
		return (T) ((type != Void.class && response.getBody() != null) ? deserialize(response.getBody(), type) : null);
	}

	@SuppressWarnings("unchecked")
	protected <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		try {
			final T doc;
			if (type == String.class && !vpack.isString()) {
				doc = (T) vpackParser.toJson(vpack);
			} else {
				doc = vpacker.deserialize(vpack, type);
			}
			return doc;
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity) throws ArangoDBException {
		try {
			final VPackSlice vpack;
			if (String.class.isAssignableFrom(entity.getClass())) {
				vpack = vpackParser.fromJson((String) entity);
			} else {
				vpack = vpacker.serialize(entity);
			}
			return vpack;
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		try {
			final VPackSlice vpack;
			if (String.class.isAssignableFrom(entity.getClass())) {
				vpack = vpackParser.fromJson((String) entity, serializeNullValues);
			} else {
				final VPack vp = serializeNullValues ? vpackerNull : vpacker;
				vpack = vp.serialize(entity);
			}
			return vpack;
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		try {
			final VPack vp = serializeNullValues ? vpackerNull : vpacker;
			return vp.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type, additionalFields);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

	protected <T> T unwrap(final Future<T> future) throws ArangoDBException {
		try {
			return future.get();
		} catch (final Exception e) {
			final Throwable cause = e.getCause();
			if (cause != null && ArangoDBException.class.isAssignableFrom(cause.getClass())) {
				throw ArangoDBException.class.cast(cause);
			}
			throw new ArangoDBException(e);
		}
	}
}

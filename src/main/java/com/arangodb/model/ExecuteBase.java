package com.arangodb.model;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.model.Executeable.ResponseDeserializer;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ExecuteBase {

	private static final String REGEX_DB_NAME = ArangoDBConstants.SYSTEM + "|[a-zA-Z][\\w-]*";
	private static final String REGEX_COLLECTION_NAME = "[a-zA-Z_][\\w-]*";
	private static final String REGEX_DOCUMENT_KEY = "[^/]+";

	protected final Communication communication;
	protected final VPack vpacker;
	protected final VPack vpackerNull;
	protected final VPackParser vpackParser;
	protected final DocumentCache documentCache;
	protected final CollectionCache collectionCache;

	protected ExecuteBase(final Communication communication, final VPack vpacker, final VPack vpackerNull,
		final VPackParser vpackParser, final DocumentCache documentCache, final CollectionCache collectionCache) {
		super();
		this.communication = communication;
		this.vpacker = vpacker;
		this.vpackerNull = vpackerNull;
		this.vpackParser = vpackParser;
		this.documentCache = documentCache;
		this.collectionCache = collectionCache;
	}

	protected Communication communication() {
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

	protected DocumentCache documentCache() {
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

	protected void validateDBName(final String name) throws ArangoDBException {
		validateName(REGEX_DB_NAME, name);
	}

	protected void validateCollectionName(final String name) throws ArangoDBException {
		validateName(REGEX_COLLECTION_NAME, name);
	}

	protected void validateDocumentKey(final String key) throws ArangoDBException {
		validateName(REGEX_DOCUMENT_KEY, key);
	}

	protected void validateName(final String regex, final CharSequence name) throws ArangoDBException {
		if (!Pattern.matches(regex, name)) {
			throw new ArangoDBException(String.format("Name %s is not valid.", name));
		}
	}

	protected <T> Executeable<T> execute(final Type type, final Request request) {
		return new Executeable<>(communication, vpacker, vpackParser, type, request);
	}

	protected <T> Executeable<T> execute(final Request request, final ResponseDeserializer<T> responseDeserializer) {
		return new Executeable<>(communication, request, responseDeserializer);
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

}

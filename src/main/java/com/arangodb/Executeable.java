package com.arangodb;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.arangodb.internal.ArangoDBConstants;
import com.arangodb.internal.CollectionCache;
import com.arangodb.internal.DocumentCache;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.internal.net.Response;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class Executeable {

	public static interface ResponseDeserializer<T> {
		T deserialize(Response response) throws VPackException;
	}

	private static final String REGEX_DB_NAME = ArangoDBConstants.SYSTEM + "|[a-zA-Z][\\w-]*";
	private static final String REGEX_COLLECTION_NAME = "[0-9]+|[a-zA-Z_][\\w-]*";
	private static final String REGEX_DOCUMENT_KEY = "[^/]+";

	protected final Communication communication;
	protected final VPack vpacker;
	protected final VPack vpackerNull;
	protected final VPackParser vpackParser;
	protected final DocumentCache documentCache;
	protected final CollectionCache collectionCache;

	protected Executeable(final Communication communication, final VPack vpacker, final VPack vpackerNull,
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

	protected <T> CompletableFuture<T> execute(final Type type, final Request request) {
		return execute(request, (response) -> {
			return createResult(vpacker, vpackParser, type, response);
		});
	}

	protected <T> CompletableFuture<T> execute(
		final Request request,
		final ResponseDeserializer<T> responseDeserializer) {
		final CompletableFuture<T> result = new CompletableFuture<>();
		communication.execute(request).whenComplete((response, ex) -> {
			if (response != null) {
				try {
					result.complete(responseDeserializer.deserialize(response));
				} catch (final VPackException | ArangoDBException e) {
					result.completeExceptionally(e);
				}
			} else if (ex != null) {
				result.completeExceptionally(ex);
			} else {
				result.cancel(true);
			}
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T createResult(
		final VPack vpack,
		final VPackParser vpackParser,
		final Type type,
		final Response response) {
		T value = null;
		if (type != Void.class) {
			if (response.getBody().isPresent()) {
				try {
					final VPackSlice body = response.getBody().get();
					if (type == String.class && !body.isString()) {
						value = (T) vpackParser.toJson(body);
					} else {
						value = vpack.deserialize(body, type);
					}
				} catch (final VPackException e) {
					throw new ArangoDBException(e);
				}
			}
		}
		return value;
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
		} catch (InterruptedException | ExecutionException | CancellationException e) {
			final Throwable cause = e.getCause();
			if (cause != null && ArangoDBException.class.isAssignableFrom(cause.getClass())) {
				throw ArangoDBException.class.cast(cause);
			}
			throw new ArangoDBException(e);
		}
	}
}

package com.arangodb.model;

import java.lang.reflect.Type;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.internal.net.Communication;
import com.arangodb.internal.net.Request;
import com.arangodb.model.Executeable.ResponseDeserializer;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public abstract class ExecuteBase {

	protected final Communication communication;
	protected final VPack vpacker;

	protected ExecuteBase(final DBCollection dbCollection) {
		this(dbCollection.db());
	}

	protected ExecuteBase(final DB db) {
		this(db.communication(), db.vpack());
	}

	protected ExecuteBase(final Communication communication, final VPack vpacker) {
		super();
		this.communication = communication;
		this.vpacker = vpacker;
	}

	protected <T> Executeable<T> execute(final Type type, final Request request) {
		return new Executeable<>(communication, vpacker, type, request);
	}

	protected <T> Executeable<T> execute(
		final Type type,
		final Request request,
		final ResponseDeserializer<T> responseDeserializer) {
		return new Executeable<>(communication, vpacker, type, request, responseDeserializer);
	}

	protected <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		try {
			return vpacker.deserialize(vpack, type);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity) throws ArangoDBException {
		try {
			return vpacker.serialize(entity);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		try {
			return vpacker.serialize(entity, additionalFields);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

	protected VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type);
		} catch (final VPackParserException e) {
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

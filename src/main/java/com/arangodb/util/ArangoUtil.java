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

package com.arangodb.util;

import java.lang.reflect.Type;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 * 
 */
public class ArangoUtil {

	private final VPack vpacker;
	private final VPack vpackerNull;
	private final VPackParser vpackParser;

	public ArangoUtil(final VPack vpacker, final VPack vpackerNull, final VPackParser vpackParser) {
		super();
		this.vpacker = vpacker;
		this.vpackerNull = vpackerNull;
		this.vpackParser = vpackParser;
	}

	/**
	 * Deserialze a given VelocPack to an instance of a given type
	 * 
	 * @param vpack
	 *            The VelocyPack to deserialize
	 * @param type
	 *            The target type to deserialize to. Use String for raw Json.
	 * @return The deserialized VelocyPack
	 * @throws ArangoDBException
	 */
	@SuppressWarnings("unchecked")
	public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
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

	/**
	 * Serialize a given Object to VelocyPack
	 * 
	 * @param entity
	 *            The Object to serialize. If it is from type String, it will be handled as a Json.
	 * @return The serialized VelocyPack
	 * @throws ArangoDBException
	 */
	public VPackSlice serialize(final Object entity) throws ArangoDBException {
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

	/**
	 * Serialize a given Object to VelocyPack
	 * 
	 * @param entity
	 *            The Object to serialize. If it is from type String, it will be handled as a Json.
	 * @param serializeNullValues
	 *            Whether or not null values should be excluded from serialization.
	 * @return the serialized VelocyPack
	 * @throws ArangoDBException
	 */
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
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

	/**
	 * Serialize a given Object to VelocyPack. This method is for serialization of types with generic parameter like
	 * Collection, List, Map.
	 * 
	 * @param entity
	 *            The Object to serialize
	 * @param type
	 *            The source type of the Object.
	 * @return the serialized VelocyPack
	 * @throws ArangoDBException
	 */
	public VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	/**
	 * Serialize a given Object to VelocyPack. This method is for serialization of types with generic parameter like
	 * Collection, List, Map.
	 * 
	 * @param entity
	 *            The Object to serialize
	 * @param type
	 *            The source type of the Object.
	 * @param serializeNullValues
	 *            Whether or not null values should be excluded from serialization.
	 * @return the serialized VelocyPack
	 * @throws ArangoDBException
	 */
	public VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		try {
			final VPack vp = serializeNullValues ? vpackerNull : vpacker;
			return vp.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	/**
	 * Serialize a given Object to VelocyPack. This method is for serialization of types with generic parameter like
	 * Collection, List, Map.
	 * 
	 * @param entity
	 *            The Object to serialize
	 * @param type
	 *            The source type of the Object.
	 * @param serializeNullValues
	 *            Whether or not null values should be excluded from serialization.
	 * @param additionalFields
	 *            Additional Key/Value pairs to include in the created VelocyPack
	 * @return the serialized VelocyPack
	 * @throws ArangoDBException
	 */
	public VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type, additionalFields);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}

}

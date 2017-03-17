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
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface ArangoSerializer {

	/**
	 * Serialize a given Object to VelocyPack
	 * 
	 * @param entity
	 *            The Object to serialize. If it is from type String, it will be handled as a Json.
	 * @return The serialized VelocyPack
	 * @throws ArangoDBException
	 */
	VPackSlice serialize(final Object entity) throws ArangoDBException;

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
	VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException;

	/**
	 * Serialize a given Object to VelocyPack. If the Object is from type Iterable<String> the String will be
	 * interpreted as Json
	 * 
	 * @param entity
	 *            The Object to serialize. If it is from type String, it will be handled as a Json.
	 * @param serializeNullValues
	 *            Whether or not null values should be excluded from serialization.
	 * @param stringAsJson
	 * @return the serialized VelocyPack
	 * @throws ArangoDBException
	 */
	VPackSlice serialize(final Object entity, final boolean serializeNullValues, final boolean stringAsJson)
			throws ArangoDBException;

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
	VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException;

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
	VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException;

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
	VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException;

}

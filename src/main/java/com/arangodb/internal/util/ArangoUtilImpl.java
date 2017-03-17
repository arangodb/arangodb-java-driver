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

package com.arangodb.internal.util;

import java.lang.reflect.Type;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.util.ArangoUtil;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoUtilImpl implements ArangoUtil {

	private final ArangoSerializer serializer;
	private final ArangoDeserializer deserializer;

	public ArangoUtilImpl(final ArangoSerializer serializer, final ArangoDeserializer deserializer) {
		super();
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	@Override
	public VPackSlice serialize(final Object entity) throws ArangoDBException {
		return serializer.serialize(entity);
	}

	@Override
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		return serializer.serialize(entity, serializeNullValues);
	}

	@Override
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues, final boolean stringAsJson)
			throws ArangoDBException {
		return serializer.serialize(entity, serializeNullValues, stringAsJson);
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		return serializer.serialize(entity, type);
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		return serializer.serialize(entity, type, serializeNullValues);
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		return serializer.serialize(entity, type, additionalFields);
	}

	@Override
	public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		return deserializer.deserialize(vpack, type);
	}

}

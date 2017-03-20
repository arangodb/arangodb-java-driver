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

package com.arangodb.jackson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.jackson.dataformat.velocypack.VPackMapper;
import com.arangodb.util.ArangoDeserializer;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPackSlice;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VelocyJack implements ArangoSerializer, ArangoDeserializer {

	private final VPackMapper mapper;

	public VelocyJack() {
		super();
		mapper = new VPackMapper();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(final VPackSlice vpack, final Type type) throws ArangoDBException {
		try {
			return mapper.readValue(vpack.getBuffer(), (Class<T>) type);
		} catch (final IOException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public VPackSlice serialize(final Object entity) throws ArangoDBException {
		try {
			return new VPackSlice(mapper.writeValueAsBytes(entity));
		} catch (final JsonProcessingException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		return null;
	}

	@Override
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues, final boolean stringAsJson)
			throws ArangoDBException {
		return null;
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		return null;
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		return null;
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		return null;
	}

}

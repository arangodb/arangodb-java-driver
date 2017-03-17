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
import java.util.Iterator;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.util.ArangoSerializer;
import com.arangodb.velocypack.VPack;
import com.arangodb.velocypack.VPackParser;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoSerializerImpl implements ArangoSerializer {

	private final VPack vpacker;
	private final VPack vpackerNull;
	private final VPackParser vpackParser;

	public ArangoSerializerImpl(final VPack vpacker, final VPack vpackerNull, final VPackParser vpackParser) {
		super();
		this.vpacker = vpacker;
		this.vpackerNull = vpackerNull;
		this.vpackParser = vpackParser;
	}

	@Override
	public VPackSlice serialize(final Object entity) throws ArangoDBException {
		return serialize(entity, false);
	}

	@Override
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues) throws ArangoDBException {
		return serialize(entity, serializeNullValues, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public VPackSlice serialize(final Object entity, final boolean serializeNullValues, final boolean stringAsJson)
			throws ArangoDBException {
		try {
			final VPackSlice vpack;
			final Class<? extends Object> type = entity.getClass();
			if (String.class.isAssignableFrom(type)) {
				vpack = vpackParser.fromJson((String) entity, serializeNullValues);
			} else if (stringAsJson && Iterable.class.isAssignableFrom(type)) {
				final Iterator<?> iterator = Iterable.class.cast(entity).iterator();
				if (iterator.hasNext() && String.class.isAssignableFrom(iterator.next().getClass())) {
					vpack = vpackParser.fromJson((Iterable<String>) entity, serializeNullValues);
				} else {
					final VPack vp = serializeNullValues ? vpackerNull : vpacker;
					vpack = vp.serialize(entity);
				}
			} else {
				final VPack vp = serializeNullValues ? vpackerNull : vpacker;
				vpack = vp.serialize(entity);
			}
			return vpack;
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type) throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final boolean serializeNullValues)
			throws ArangoDBException {
		try {
			final VPack vp = serializeNullValues ? vpackerNull : vpacker;
			return vp.serialize(entity, type);
		} catch (final VPackException e) {
			throw new ArangoDBException(e);
		}
	}

	@Override
	public VPackSlice serialize(final Object entity, final Type type, final Map<String, Object> additionalFields)
			throws ArangoDBException {
		try {
			return vpacker.serialize(entity, type, additionalFields);
		} catch (final VPackParserException e) {
			throw new ArangoDBException(e);
		}
	}
}

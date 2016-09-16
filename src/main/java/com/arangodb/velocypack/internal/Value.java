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

package com.arangodb.velocypack.internal;

import java.math.BigInteger;

import com.arangodb.velocypack.ValueType;
import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Value {

	private final Object value;

	private final ValueType type;
	private final Class<?> clazz;
	private final boolean unindexed;

	private Value(final Object value, final ValueType type, final Class<?> clazz) {
		this(value, type, clazz, false);
	}

	private Value(final Object value, final ValueType type, final Class<?> clazz, final boolean unindexed) {
		super();
		this.value = value;
		this.type = type;
		this.clazz = clazz;
		this.unindexed = unindexed;
	}

	public Value(final ValueType type) {
		this(type, false);
	}

	public Value(final ValueType type, final boolean unindexed) throws VPackValueTypeException {
		this(null, type, null, unindexed);
		if (type != ValueType.ARRAY && type != ValueType.OBJECT && type != ValueType.NULL) {
			throw new VPackValueTypeException(ValueType.ARRAY, ValueType.OBJECT, ValueType.NULL);
		}
	}

	public Value(final Long value, final ValueType type) throws VPackValueTypeException {
		this(value, type, Long.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public Value(final BigInteger value, final ValueType type) throws VPackValueTypeException {
		this(value, type, BigInteger.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public ValueType getType() {
		return type;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public boolean isUnindexed() {
		return unindexed;
	}

	public Number getNumber() {
		return (Number) value;
	}

	public Long getLong() {
		return (Long) value;
	}

	public BigInteger getBigInteger() {
		return (BigInteger) value;
	}

}

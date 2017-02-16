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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.internal.util.DateUtil;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackSerializers {

	private VPackSerializers() {
		super();
	}

	public static final VPackSerializer<String> STRING = new VPackSerializer<String>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final String value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Boolean> BOOLEAN = new VPackSerializer<Boolean>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Boolean value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Integer> INTEGER = new VPackSerializer<Integer>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Integer value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Long> LONG = new VPackSerializer<Long>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Long value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Short> SHORT = new VPackSerializer<Short>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Short value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Double> DOUBLE = new VPackSerializer<Double>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Double value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Float> FLOAT = new VPackSerializer<Float>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Float value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<BigInteger> BIG_INTEGER = new VPackSerializer<BigInteger>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final BigInteger value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<BigDecimal> BIG_DECIMAL = new VPackSerializer<BigDecimal>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final BigDecimal value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Number> NUMBER = new VPackSerializer<Number>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Number value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, Double.class.cast(value));
		}
	};
	public static final VPackSerializer<Character> CHARACTER = new VPackSerializer<Character>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Character value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<Date> DATE = new VPackSerializer<Date>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Date value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, DateUtil.format(value));
		}
	};
	public static final VPackSerializer<java.sql.Date> SQL_DATE = new VPackSerializer<java.sql.Date>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final java.sql.Date value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, DateUtil.format(value));
		}
	};
	public static final VPackSerializer<java.sql.Timestamp> SQL_TIMESTAMP = new VPackSerializer<Timestamp>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Timestamp value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, DateUtil.format(value));
		}
	};
	public static final VPackSerializer<VPackSlice> VPACK = new VPackSerializer<VPackSlice>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final VPackSlice value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value);
		}
	};
	public static final VPackSerializer<UUID> UUID = new VPackSerializer<UUID>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final java.util.UUID value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, value.toString());
		}
	};
	public static final VPackSerializer<byte[]> BYTE_ARRAY = new VPackSerializer<byte[]>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final byte[] value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, DatatypeConverter.printBase64Binary(value));
		}
	};
	public static final VPackSerializer<Byte> BYTE = new VPackSerializer<Byte>() {
		@Override
		public void serialize(
				final VPackBuilder builder,
				final String attribute,
				final Byte value,
				final VPackSerializationContext context) {
			builder.add(attribute, value);
		}
	};
}

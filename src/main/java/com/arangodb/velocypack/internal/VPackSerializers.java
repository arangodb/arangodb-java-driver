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
import java.util.Date;

import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackSerializers {

	private VPackSerializers() {
		super();
	}

	public static VPackSerializer<String> STRING = (builder, attribute, value, context) -> builder.add(attribute,
		value);
	public static VPackSerializer<Boolean> BOOLEAN = (builder, attribute, value, context) -> builder.add(attribute,
		value);
	public static VPackSerializer<Integer> INTEGER = (builder, attribute, value, context) -> builder.add(attribute,
		value);
	public static VPackSerializer<Long> LONG = (builder, attribute, value, context) -> builder.add(attribute, value);
	public static VPackSerializer<Short> SHORT = (builder, attribute, value, context) -> builder.add(attribute, value);
	public static VPackSerializer<Double> DOUBLE = (builder, attribute, value, context) -> builder.add(attribute,
		value);
	public static VPackSerializer<Float> FLOAT = (builder, attribute, value, context) -> builder.add(attribute, value);
	public static VPackSerializer<BigInteger> BIG_INTEGER = (builder, attribute, value, context) -> builder
			.add(attribute, value);
	public static VPackSerializer<BigDecimal> BIG_DECIMAL = (builder, attribute, value, context) -> builder
			.add(attribute, value);
	public static VPackSerializer<Number> NUMBER = (builder, attribute, value, context) -> builder.add(attribute,
		Double.class.cast(value));
	public static VPackSerializer<Character> CHARACTER = (builder, attribute, value, context) -> builder.add(attribute,
		value);
	public static VPackSerializer<Date> DATE = (builder, attribute, value, context) -> builder.add(attribute, value);
	public static VPackSerializer<java.sql.Date> SQL_DATE = (builder, attribute, value, context) -> builder
			.add(attribute, value);
	public static VPackSerializer<java.sql.Timestamp> SQL_TIMESTAMP = (builder, attribute, value, context) -> builder
			.add(attribute, value);
	public static VPackSerializer<VPackSlice> VPACK = (builder, attribute, value, context) -> builder.add(attribute,
		value);
}

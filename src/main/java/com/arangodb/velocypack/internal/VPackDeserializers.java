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
import java.time.Instant;
import java.util.Date;

import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackDeserializers {

	private VPackDeserializers() {
		super();
	}

	public static final VPackDeserializer<String> STRING = (parent, vpack, context) -> vpack.getAsString();
	public static final VPackDeserializer<Boolean> BOOLEAN = (parent, vpack, context) -> vpack.getAsBoolean();
	public static final VPackDeserializer<Integer> INTEGER = (parent, vpack, context) -> vpack.getAsInt();
	public static final VPackDeserializer<Long> LONG = (parent, vpack, context) -> vpack.getAsLong();
	public static final VPackDeserializer<Short> SHORT = (parent, vpack, context) -> vpack.getAsShort();
	public static final VPackDeserializer<Double> DOUBLE = (parent, vpack, context) -> vpack.getAsDouble();
	public static final VPackDeserializer<Float> FLOAT = (parent, vpack, context) -> vpack.getAsFloat();
	public static final VPackDeserializer<BigInteger> BIG_INTEGER = (parent, vpack, context) -> vpack.getAsBigInteger();
	public static final VPackDeserializer<BigDecimal> BIG_DECIMAL = (parent, vpack, context) -> vpack.getAsBigDecimal();
	public static final VPackDeserializer<Number> NUMBER = (parent, vpack, context) -> vpack.getAsNumber();
	public static final VPackDeserializer<Character> CHARACTER = (parent, vpack, context) -> vpack.getAsChar();
	public static final VPackDeserializer<Date> DATE = (parent, vpack, context) -> vpack.getAsDate();
	public static final VPackDeserializer<java.sql.Date> SQL_DATE = (parent, vpack, context) -> vpack.getAsSQLDate();
	public static final VPackDeserializer<java.sql.Timestamp> SQL_TIMESTAMP = (parent, vpack, context) -> vpack
			.getAsSQLTimestamp();
	public static final VPackDeserializer<Instant> INSTANT = (parent, vpack, context) -> vpack.getAsInstant();
	public static final VPackDeserializer<VPackSlice> VPACK = (parent, vpack, context) -> vpack;

}

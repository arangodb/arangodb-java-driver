package com.arangodb.velocypack.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arangodb.velocypack.VPackDeserializer;

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

}

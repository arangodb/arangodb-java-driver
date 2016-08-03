package com.arangodb.velocypack.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.arangodb.velocypack.VPackBuilder;
import com.arangodb.velocypack.VPackSerializationContext;
import com.arangodb.velocypack.VPackSerializer;
import com.arangodb.velocypack.Value;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackSerializers {

	private VPackSerializers() {
		super();
	}

	public static VPackSerializer<String> STRING = new VPackSerializer<String>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final String value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(String.class.cast(value)));
		}
	};
	public static VPackSerializer<Boolean> BOOLEAN = new VPackSerializer<Boolean>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Boolean value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Boolean.class.cast(value)));
		}
	};
	public static VPackSerializer<Integer> INTEGER = new VPackSerializer<Integer>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Integer value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Integer.class.cast(value)));
		}
	};
	public static VPackSerializer<Long> LONG = new VPackSerializer<Long>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Long value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Long.class.cast(value)));
		}
	};
	public static VPackSerializer<Short> SHORT = new VPackSerializer<Short>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Short value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Short.class.cast(value)));
		}
	};
	public static VPackSerializer<Double> DOUBLE = new VPackSerializer<Double>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Double value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Double.class.cast(value)));
		}
	};
	public static VPackSerializer<Float> FLOAT = new VPackSerializer<Float>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Float value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Float.class.cast(value)));
		}
	};
	public static VPackSerializer<BigInteger> BIG_INTEGER = new VPackSerializer<BigInteger>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final BigInteger value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(BigInteger.class.cast(value)));
		}
	};
	public static VPackSerializer<BigDecimal> BIG_DECIMAL = new VPackSerializer<BigDecimal>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final BigDecimal value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(BigDecimal.class.cast(value)));
		}
	};
	public static VPackSerializer<Number> NUMBER = new VPackSerializer<Number>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Number value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Double.class.cast(value)));
		}
	};
	public static VPackSerializer<Character> CHARACTER = new VPackSerializer<Character>() {
		@Override
		public void serialize(
			final VPackBuilder builder,
			final String attribute,
			final Character value,
			final VPackSerializationContext context) throws VPackException {
			builder.add(attribute, new Value(Character.class.cast(value)));
		}
	};
}

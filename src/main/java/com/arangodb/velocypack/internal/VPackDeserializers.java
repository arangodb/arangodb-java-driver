package com.arangodb.velocypack.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.arangodb.velocypack.VPackDeserializationContext;
import com.arangodb.velocypack.VPackDeserializer;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackDeserializers {

	private VPackDeserializers() {
		super();
	}

	public static final VPackDeserializer<String> STRING = new VPackDeserializer<String>() {
		@Override
		public String deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsString();
		}
	};
	public static final VPackDeserializer<Boolean> BOOLEAN = new VPackDeserializer<Boolean>() {
		@Override
		public Boolean deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsBoolean();
		}
	};
	public static final VPackDeserializer<Integer> INTEGER = new VPackDeserializer<Integer>() {
		@Override
		public Integer deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsInt();
		}
	};
	public static final VPackDeserializer<Long> LONG = new VPackDeserializer<Long>() {
		@Override
		public Long deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsLong();
		}
	};
	public static final VPackDeserializer<Short> SHORT = new VPackDeserializer<Short>() {
		@Override
		public Short deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsShort();
		}
	};
	public static final VPackDeserializer<Double> DOUBLE = new VPackDeserializer<Double>() {
		@Override
		public Double deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsDouble();
		}
	};
	public static final VPackDeserializer<Float> FLOAT = new VPackDeserializer<Float>() {
		@Override
		public Float deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsFloat();
		}
	};
	public static final VPackDeserializer<BigInteger> BIG_INTEGER = new VPackDeserializer<BigInteger>() {
		@Override
		public BigInteger deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsBigInteger();
		}
	};
	public static final VPackDeserializer<BigDecimal> BIG_DECIMAL = new VPackDeserializer<BigDecimal>() {
		@Override
		public BigDecimal deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsBigDecimal();
		}
	};
	public static final VPackDeserializer<Number> NUMBER = new VPackDeserializer<Number>() {
		@Override
		public Number deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsNumber();
		}
	};
	public static final VPackDeserializer<Character> CHARACTER = new VPackDeserializer<Character>() {
		@Override
		public Character deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
				throws VPackException {
			return vpack.getAsChar();
		}
	};

}

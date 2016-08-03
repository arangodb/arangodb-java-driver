package com.arangodb.velocypack.internal;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.arangodb.velocypack.VPackKeyMapAdapter;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackKeyMapAdapters {

	private VPackKeyMapAdapters() {
		super();
	}

	public static final VPackKeyMapAdapter<Enum<?>> createEnumAdapter(final Class<?> type) {
		return new VPackKeyMapAdapter<Enum<?>>() {
			@Override
			public String serialize(final Enum<?> key) {
				return key.name();
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Enum<?> deserialize(final String key) {
				final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
				return Enum.valueOf(enumType, key);
			}
		};
	}

	public static final VPackKeyMapAdapter<String> STRING = new VPackKeyMapAdapter<String>() {
		@Override
		public String serialize(final String key) {
			return key;
		}

		@Override
		public String deserialize(final String key) {
			return key;
		}
	};
	public static final VPackKeyMapAdapter<Boolean> BOOLEAN = new VPackKeyMapAdapter<Boolean>() {
		@Override
		public String serialize(final Boolean key) {
			return key.toString();
		}

		@Override
		public Boolean deserialize(final String key) {
			return Boolean.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Integer> INTEGER = new VPackKeyMapAdapter<Integer>() {
		@Override
		public String serialize(final Integer key) {
			return key.toString();
		}

		@Override
		public Integer deserialize(final String key) {
			return Integer.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Long> LONG = new VPackKeyMapAdapter<Long>() {
		@Override
		public String serialize(final Long key) {
			return key.toString();
		}

		@Override
		public Long deserialize(final String key) {
			return Long.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Short> SHORT = new VPackKeyMapAdapter<Short>() {
		@Override
		public String serialize(final Short key) {
			return key.toString();
		}

		@Override
		public Short deserialize(final String key) {
			return Short.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Double> DOUBLE = new VPackKeyMapAdapter<Double>() {
		@Override
		public String serialize(final Double key) {
			return key.toString();
		}

		@Override
		public Double deserialize(final String key) {
			return Double.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Float> FLOAT = new VPackKeyMapAdapter<Float>() {
		@Override
		public String serialize(final Float key) {
			return key.toString();
		}

		@Override
		public Float deserialize(final String key) {
			return Float.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<BigInteger> BIG_INTEGER = new VPackKeyMapAdapter<BigInteger>() {
		@Override
		public String serialize(final BigInteger key) {
			return key.toString();
		}

		@Override
		public BigInteger deserialize(final String key) {
			return new BigInteger(key);
		}
	};
	public static final VPackKeyMapAdapter<BigDecimal> BIG_DECIMAL = new VPackKeyMapAdapter<BigDecimal>() {
		@Override
		public String serialize(final BigDecimal key) {
			return key.toString();
		}

		@Override
		public BigDecimal deserialize(final String key) {
			return new BigDecimal(key);
		}
	};
	public static final VPackKeyMapAdapter<Number> NUMBER = new VPackKeyMapAdapter<Number>() {
		@Override
		public String serialize(final Number key) {
			return key.toString();
		}

		@Override
		public Number deserialize(final String key) {
			return Double.valueOf(key);
		}
	};
	public static final VPackKeyMapAdapter<Character> CHARACTER = new VPackKeyMapAdapter<Character>() {
		@Override
		public String serialize(final Character key) {
			return key.toString();
		}

		@Override
		public Character deserialize(final String key) {
			return key.charAt(0);
		}
	};
}

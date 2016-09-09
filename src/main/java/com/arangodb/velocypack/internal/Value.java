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

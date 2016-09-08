package com.arangodb.velocypack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Value {

	private Object value;

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

	/**
	 * creates a Value with the specified type Array or Object
	 * 
	 * @throws VPackValueTypeException
	 */
	public Value(final ValueType type) {
		this(type, false);
	}

	/**
	 * creates a Value with the specified type Array or Object or Null
	 * 
	 * @throws VPackValueTypeException
	 */
	public Value(final ValueType type, final boolean unindexed) throws VPackValueTypeException {
		this(null, type, null, unindexed);
		if (type != ValueType.ARRAY && type != ValueType.OBJECT && type != ValueType.NULL) {
			throw new VPackValueTypeException(ValueType.ARRAY, ValueType.OBJECT, ValueType.NULL);
		}
	}

	public Value(final Boolean value) {
		this(value, checkNull(value, ValueType.BOOL), Boolean.class);
	}

	public Value(final Long value) {
		this(value, checkSmallInt(value, ValueType.INT), Long.class);
	}

	public Value(final Long value, final ValueType type) throws VPackValueTypeException {
		this(value, checkSmallInt(value, type), Long.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public Value(final Integer value) {
		this(value, checkSmallInt(value, ValueType.INT), Integer.class);
	}

	public Value(final Integer value, final ValueType type) throws VPackValueTypeException {
		this(value, checkSmallInt(value, type), Integer.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public Value(final Short value) {
		this(value, checkSmallInt(value, ValueType.INT), Short.class);
	}

	public Value(final BigInteger value) {
		this(value, checkSmallInt(value, ValueType.INT), BigInteger.class);
	}

	public Value(final BigInteger value, final ValueType type) throws VPackValueTypeException {
		this(value, checkSmallInt(value, type), BigInteger.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
	}

	public Value(final Double value) {
		this(value, checkNull(value, ValueType.DOUBLE), Double.class);
	}

	public Value(final Float value) {
		this(value, checkNull(value, ValueType.DOUBLE), Float.class);
	}

	public Value(final BigDecimal value) {
		this(value, checkNull(value, ValueType.DOUBLE), BigDecimal.class);
	}

	public Value(final String value) {
		this(value, checkNull(value, ValueType.STRING), String.class);
	}

	public Value(final Character value) {
		this(value, checkNull(value, ValueType.STRING), Character.class);
	}

	public Value(final Date value) {
		this(value, checkNull(value, ValueType.UTC_DATE), Date.class);
	}

	public Value(final byte[] value) {
		this(value, checkNull(value, ValueType.BINARY), null);
	}

	public Value(final VPackSlice value) {
		this(value, checkNull(value, ValueType.VPACK), null);
		this.value = value.getValue();
	}

	private static ValueType checkSmallInt(final Number value, final ValueType type) {
		return value != null ? value.longValue() <= 9 && value.longValue() >= -6 ? ValueType.SMALLINT : type
				: ValueType.NULL;
	}

	private static ValueType checkNull(final Object obj, final ValueType type) {
		return obj != null ? type : ValueType.NULL;
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

	public Boolean getBoolean() {
		return (Boolean) value;
	}

	public Double getDouble() {
		return (Double) value;
	}

	public Number getNumber() {
		return (Number) value;
	}

	public Long getLong() {
		return (Long) value;
	}

	public Integer getInteger() {
		return (Integer) value;
	}

	public Float getFloat() {
		return (Float) value;
	}

	public Short getShort() {
		return (Short) value;
	}

	public BigInteger getBigInteger() {
		return (BigInteger) value;
	}

	public BigDecimal getBigDecimal() {
		return (BigDecimal) value;
	}

	public String getString() {
		return (String) value;
	}

	public Character getCharacter() {
		return (Character) value;
	}

	public Date getDate() {
		return (Date) value;
	}

	public byte[] getBinary() {
		return (byte[]) value;
	}

}

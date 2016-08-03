package com.arangodb.velocypack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.arangodb.velocypack.exception.VPackValueTypeException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class Value {

	private Boolean b;
	private Double d;
	private Long l;
	private Integer i;
	private Float f;
	private Short sh;
	private BigInteger bi;
	private BigDecimal bd;
	private String s;
	private Character c;
	private Date date;
	private byte[] blob;

	private final ValueType type;
	private final Class<?> clazz;
	private final boolean unindexed;

	private Value(final ValueType type, final Class<?> clazz) {
		this(type, clazz, false);
	}

	private Value(final ValueType type, final Class<?> clazz, final boolean unindexed) {
		super();
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
		this(type, null, unindexed);
		if (type != ValueType.ARRAY && type != ValueType.OBJECT && type != ValueType.NULL) {
			throw new VPackValueTypeException(ValueType.ARRAY, ValueType.OBJECT, ValueType.NULL);
		}
	}

	public Value(final Boolean value) {
		this(checkNull(value, ValueType.BOOL), Boolean.class);
		b = value;
	}

	public Value(final Long value) {
		this(checkSmallInt(value, ValueType.INT), Long.class);
		l = value;
	}

	public Value(final Long value, final ValueType type) throws VPackValueTypeException {
		this(checkSmallInt(value, type), Long.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
		l = value;
	}

	public Value(final Integer value) {
		this(checkSmallInt(value, ValueType.INT), Integer.class);
		i = value;
	}

	public Value(final Integer value, final ValueType type) throws VPackValueTypeException {
		this(checkSmallInt(value, type), Integer.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
		i = value;
	}

	public Value(final Short value) {
		this(checkSmallInt(value, ValueType.INT), Short.class);
		sh = value;
	}

	public Value(final BigInteger value) {
		this(checkSmallInt(value, ValueType.INT), BigInteger.class);
		bi = value;
	}

	public Value(final BigInteger value, final ValueType type) throws VPackValueTypeException {
		this(checkSmallInt(value, type), BigInteger.class);
		if (type != ValueType.INT && type != ValueType.UINT && type != ValueType.SMALLINT) {
			throw new VPackValueTypeException(ValueType.INT, ValueType.UINT, ValueType.SMALLINT);
		}
		bi = value;
	}

	public Value(final Double value) {
		this(checkNull(value, ValueType.DOUBLE), Double.class);
		d = value;
	}

	public Value(final Float value) {
		this(checkNull(value, ValueType.DOUBLE), Float.class);
		f = value;
	}

	public Value(final BigDecimal value) {
		this(checkNull(value, ValueType.DOUBLE), BigDecimal.class);
		bd = value;
	}

	public Value(final String value) {
		this(checkNull(value, ValueType.STRING), String.class);
		s = value;
	}

	public Value(final Character value) {
		this(checkNull(value, ValueType.STRING), Character.class);
		c = value;
	}

	public Value(final Date value) {
		this(checkNull(value, ValueType.UTC_DATE), Date.class);
		date = value;
	}

	public Value(final byte[] value) {
		this(checkNull(value, ValueType.BINARY), null);
		blob = value;
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
		return b;
	}

	public Double getDouble() {
		return d;
	}

	public Long getLong() {
		return l;
	}

	public Integer getInteger() {
		return i;
	}

	public Float getFloat() {
		return f;
	}

	public Short getShort() {
		return sh;
	}

	public BigInteger getBigInteger() {
		return bi;
	}

	public BigDecimal getBigDecimal() {
		return bd;
	}

	public String getString() {
		return s;
	}

	public Character getCharacter() {
		return c;
	}

	public Date getDate() {
		return date;
	}

	public byte[] getBinary() {
		return blob;
	}

}

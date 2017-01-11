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

package com.arangodb.velocypack;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arangodb.velocypack.exception.VPackBuilderException;
import com.arangodb.velocypack.exception.VPackBuilderKeyAlreadyWrittenException;
import com.arangodb.velocypack.exception.VPackBuilderNeedOpenCompoundException;
import com.arangodb.velocypack.exception.VPackBuilderNeedOpenObjectException;
import com.arangodb.velocypack.exception.VPackBuilderNumberOutOfRangeException;
import com.arangodb.velocypack.exception.VPackBuilderUnexpectedValueException;
import com.arangodb.velocypack.exception.VPackKeyTypeException;
import com.arangodb.velocypack.exception.VPackNeedAttributeTranslatorException;
import com.arangodb.velocypack.exception.VPackValueTypeException;
import com.arangodb.velocypack.internal.DefaultVPackBuilderOptions;
import com.arangodb.velocypack.internal.Value;
import com.arangodb.velocypack.internal.util.NumberUtil;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackBuilder {

	private static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;
	private static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
	private static final int DOUBLE_BYTES = Double.SIZE / Byte.SIZE;

	public static interface BuilderOptions {
		boolean isBuildUnindexedArrays();

		void setBuildUnindexedArrays(boolean buildUnindexedArrays);

		boolean isBuildUnindexedObjects();

		void setBuildUnindexedObjects(boolean buildUnindexedObjects);
	}

	public static interface Appender<T> {
		void append(VPackBuilder builder, T value) throws VPackBuilderException;
	}

	private static final Appender<Value> VALUE = new Appender<Value>() {
		@Override
		public void append(final VPackBuilder builder, final Value value) throws VPackBuilderException {
			builder.set(value);
		}
	};
	private static final Appender<ValueType> VALUE_TYPE = new Appender<ValueType>() {
		@Override
		public void append(final VPackBuilder builder, final ValueType value) throws VPackBuilderException {
			switch (value) {
			case NULL:
				builder.appendNull();
				break;
			case ARRAY:
				builder.addArray(false);
				break;
			case OBJECT:
				builder.addObject(false);
				break;
			default:
				throw new VPackValueTypeException(ValueType.ARRAY, ValueType.OBJECT, ValueType.NULL);
			}
		}
	};
	private static final Appender<Boolean> BOOLEAN = new Appender<Boolean>() {
		@Override
		public void append(final VPackBuilder builder, final Boolean value) throws VPackBuilderException {
			builder.appendBoolean(value);
		}
	};
	private static final Appender<Double> DOUBLE = new Appender<Double>() {
		@Override
		public void append(final VPackBuilder builder, final Double value) throws VPackBuilderException {
			builder.appendDouble(value);
		}
	};
	private static final Appender<Float> FLOAT = new Appender<Float>() {
		@Override
		public void append(final VPackBuilder builder, final Float value) throws VPackBuilderException {
			builder.appendDouble(value);
		}
	};
	private static final Appender<BigDecimal> BIG_DECIMAL = new Appender<BigDecimal>() {
		@Override
		public void append(final VPackBuilder builder, final BigDecimal value) throws VPackBuilderException {
			builder.appendDouble(value.doubleValue());
		}
	};
	private static final Appender<Long> LONG = new Appender<Long>() {
		@Override
		public void append(final VPackBuilder builder, final Long value) throws VPackBuilderException {
			if (value <= 9 && value >= -6) {
				builder.appendSmallInt(value);
			} else {
				builder.add((byte) 0x27);
				builder.append(value, LONG_BYTES);
			}
		}
	};
	private static final Appender<Integer> INTEGER = new Appender<Integer>() {
		@Override
		public void append(final VPackBuilder builder, final Integer value) throws VPackBuilderException {
			if (value <= 9 && value >= -6) {
				builder.appendSmallInt(value);
			} else {
				builder.add((byte) 0x23);
				builder.append(value, INTEGER_BYTES);
			}
		}
	};
	private static final Appender<Short> SHORT = new Appender<Short>() {
		@Override
		public void append(final VPackBuilder builder, final Short value) throws VPackBuilderException {
			if (value <= 9 && value >= -6) {
				builder.appendSmallInt(value);
			} else {
				builder.add((byte) 0x23);
				builder.append(value, INTEGER_BYTES);
			}
		}
	};
	private static final Appender<BigInteger> BIG_INTEGER = new Appender<BigInteger>() {
		@Override
		public void append(final VPackBuilder builder, final BigInteger value) throws VPackBuilderException {
			if (value.longValue() <= 9 && value.longValue() >= -6) {
				builder.appendSmallInt(value.longValue());
			} else {
				builder.add((byte) 0x27);
				builder.append(value, LONG_BYTES);
			}
		}
	};
	private static final Appender<Date> DATE = new Appender<Date>() {
		@Override
		public void append(final VPackBuilder builder, final Date value) throws VPackBuilderException {
			builder.appendDate(value);
		}
	};
	private static final Appender<java.sql.Date> SQL_DATE = new Appender<java.sql.Date>() {
		@Override
		public void append(final VPackBuilder builder, final java.sql.Date value) throws VPackBuilderException {
			builder.appendSQLDate(value);
		}
	};
	private static final Appender<Timestamp> SQL_TIMESTAMP = new Appender<Timestamp>() {
		@Override
		public void append(final VPackBuilder builder, final Timestamp value) throws VPackBuilderException {
			builder.appendSQLTimestamp(value);
		}
	};
	private static final Appender<String> STRING = new Appender<String>() {
		@Override
		public void append(final VPackBuilder builder, final String value) throws VPackBuilderException {
			builder.appendString(value);
		}
	};
	private static final Appender<Character> CHARACTER = new Appender<Character>() {
		@Override
		public void append(final VPackBuilder builder, final Character value) throws VPackBuilderException {
			builder.appendString(String.valueOf(value));
		}
	};
	private static final Appender<byte[]> BYTE_ARRAY = new Appender<byte[]>() {
		@Override
		public void append(final VPackBuilder builder, final byte[] value) throws VPackBuilderException {
			builder.appendBinary(value);
		}
	};
	private static final Appender<VPackSlice> VPACK = new Appender<VPackSlice>() {
		@Override
		public void append(final VPackBuilder builder, final VPackSlice value) throws VPackBuilderException {
			builder.appendVPack(value);
		}
	};

	private byte[] buffer; // Here we collect the result
	private int size;
	private final List<Integer> stack; // Start positions of open
										// objects/arrays
	private final Map<Integer, List<Integer>> index; // Indices for starts
														// of
														// subindex
	private boolean keyWritten; // indicates that in the current object the key
								// has been written but the value not yet
	private final BuilderOptions options;

	public VPackBuilder() {
		this(new DefaultVPackBuilderOptions());
	}

	public VPackBuilder(final BuilderOptions options) {
		super();
		this.options = options;
		size = 0;
		buffer = new byte[10];
		stack = new ArrayList<Integer>();
		index = new HashMap<Integer, List<Integer>>();
	}

	public BuilderOptions getOptions() {
		return options;
	}

	private void add(final byte b) {
		ensureCapacity(size + 1);
		buffer[size++] = b;
	}

	private void addUnchecked(final byte b) {
		buffer[size++] = b;
	}

	private void remove(final int index) {
		final int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(buffer, index + 1, buffer, index, numMoved);
		}
		buffer[--size] = 0;
	}

	private void ensureCapacity(final int minCapacity) {
		final int oldCapacity = buffer.length;
		if (minCapacity > oldCapacity) {
			final byte[] oldData = buffer;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			buffer = Arrays.copyOf(oldData, newCapacity);
		}
	}

	public VPackBuilder add(final ValueType value) throws VPackBuilderException {
		return addInternal(VALUE_TYPE, value);
	}

	public VPackBuilder add(final ValueType value, final boolean unindexed) throws VPackBuilderException {
		return addInternal(VALUE, new Value(value, unindexed));
	}

	public VPackBuilder add(final Boolean value) throws VPackBuilderException {
		return addInternal(BOOLEAN, value);
	}

	public VPackBuilder add(final Double value) throws VPackBuilderException {
		return addInternal(DOUBLE, value);
	}

	public VPackBuilder add(final Float value) throws VPackBuilderException {
		return addInternal(FLOAT, value);
	}

	public VPackBuilder add(final BigDecimal value) throws VPackBuilderException {
		return addInternal(BIG_DECIMAL, value);
	}

	public VPackBuilder add(final Long value) throws VPackBuilderException {
		return addInternal(LONG, value);
	}

	public VPackBuilder add(final Long value, final ValueType type) throws VPackBuilderException {
		return addInternal(VALUE, new Value(value, type));
	}

	public VPackBuilder add(final Integer value) throws VPackBuilderException {
		return addInternal(INTEGER, value);
	}

	public VPackBuilder add(final Short value) throws VPackBuilderException {
		return addInternal(SHORT, value);
	}

	public VPackBuilder add(final BigInteger value) throws VPackBuilderException {
		return addInternal(BIG_INTEGER, value);
	}

	public VPackBuilder add(final BigInteger value, final ValueType type) throws VPackBuilderException {
		return addInternal(VALUE, new Value(value, type));
	}

	public VPackBuilder add(final Date value) throws VPackBuilderException {
		return addInternal(DATE, value);
	}

	public VPackBuilder add(final java.sql.Date value) throws VPackBuilderException {
		return addInternal(SQL_DATE, value);
	}

	public VPackBuilder add(final java.sql.Timestamp value) throws VPackBuilderException {
		return addInternal(SQL_TIMESTAMP, value);
	}

	public VPackBuilder add(final String value) throws VPackBuilderException {
		return addInternal(STRING, value);
	}

	public VPackBuilder add(final Character value) throws VPackBuilderException {
		return addInternal(CHARACTER, value);
	}

	public VPackBuilder add(final byte[] value) throws VPackBuilderException {
		return addInternal(BYTE_ARRAY, value);
	}

	public VPackBuilder add(final VPackSlice value) throws VPackBuilderException {
		return addInternal(VPACK, value);
	}

	public VPackBuilder add(final String attribute, final ValueType value) throws VPackBuilderException {
		return addInternal(attribute, VALUE_TYPE, value);
	}

	public VPackBuilder add(final String attribute, final ValueType value, final boolean unindexed)
			throws VPackBuilderException {
		return addInternal(attribute, VALUE, new Value(value, unindexed));
	}

	public VPackBuilder add(final String attribute, final Boolean value) throws VPackBuilderException {
		return addInternal(attribute, BOOLEAN, value);
	}

	public VPackBuilder add(final String attribute, final Double value) throws VPackBuilderException {
		return addInternal(attribute, DOUBLE, value);
	}

	public VPackBuilder add(final String attribute, final Float value) throws VPackBuilderException {
		return addInternal(attribute, FLOAT, value);
	}

	public VPackBuilder add(final String attribute, final BigDecimal value) throws VPackBuilderException {
		return addInternal(attribute, BIG_DECIMAL, value);
	}

	public VPackBuilder add(final String attribute, final Long value) throws VPackBuilderException {
		return addInternal(attribute, LONG, value);
	}

	public VPackBuilder add(final String attribute, final Long value, final ValueType type)
			throws VPackBuilderException {
		return addInternal(attribute, VALUE, new Value(value, type));
	}

	public VPackBuilder add(final String attribute, final Integer value) throws VPackBuilderException {
		return addInternal(attribute, INTEGER, value);
	}

	public VPackBuilder add(final String attribute, final Short value) throws VPackBuilderException {
		return addInternal(attribute, SHORT, value);
	}

	public VPackBuilder add(final String attribute, final BigInteger value) throws VPackBuilderException {
		return addInternal(attribute, BIG_INTEGER, value);
	}

	public VPackBuilder add(final String attribute, final BigInteger value, final ValueType type)
			throws VPackBuilderException {
		return addInternal(attribute, VALUE, new Value(value, type));
	}

	public VPackBuilder add(final String attribute, final String value) throws VPackBuilderException {
		return addInternal(attribute, STRING, value);
	}

	public VPackBuilder add(final String attribute, final Character value) throws VPackBuilderException {
		return addInternal(attribute, CHARACTER, value);
	}

	public VPackBuilder add(final String attribute, final Date value) throws VPackBuilderException {
		return addInternal(attribute, DATE, value);
	}

	public VPackBuilder add(final String attribute, final java.sql.Date value) throws VPackBuilderException {
		return addInternal(attribute, SQL_DATE, value);
	}

	public VPackBuilder add(final String attribute, final java.sql.Timestamp value) throws VPackBuilderException {
		return addInternal(attribute, SQL_TIMESTAMP, value);
	}

	public VPackBuilder add(final String attribute, final byte[] value) throws VPackBuilderException {
		return addInternal(attribute, BYTE_ARRAY, value);
	}

	public VPackBuilder add(final String attribute, final VPackSlice value) throws VPackBuilderException {
		return addInternal(attribute, VPACK, value);
	}

	private <T> VPackBuilder addInternal(final Appender<T> appender, final T value) throws VPackBuilderException {
		boolean haveReported = false;
		if (!stack.isEmpty() && !keyWritten) {
			reportAdd();
			haveReported = true;
		}
		try {
			if (value == null) {
				appendNull();
			} else {
				appender.append(this, value);
			}
		} catch (final VPackBuilderException e) {
			// clean up in case of an exception
			if (haveReported) {
				cleanupAdd();
			}
			throw e;
		}
		return this;
	}

	private <T> VPackBuilder addInternal(final String attribute, final Appender<T> appender, final T value)
			throws VPackBuilderException {
		if (attribute != null) {
			boolean haveReported = false;
			if (!stack.isEmpty()) {
				final byte head = head();
				if (head != 0x0b && head != 0x14) {
					throw new VPackBuilderNeedOpenObjectException();
				}
				if (keyWritten) {
					throw new VPackBuilderKeyAlreadyWrittenException();
				}
				reportAdd();
				haveReported = true;
			}
			try {
				if (VPackSlice.attributeTranslator != null) {
					final VPackSlice translate = VPackSlice.attributeTranslator.translate(attribute);
					if (translate != null) {
						final byte[] trValue = translate.getRawVPack();
						ensureCapacity(size + trValue.length);
						for (int i = 0; i < trValue.length; i++) {
							addUnchecked(trValue[i]);
						}
						keyWritten = true;
						if (value == null) {
							appendNull();
						} else {
							appender.append(this, value);
						}
						return this;
					}
					// otherwise fall through to regular behavior
				}
				STRING.append(this, attribute);
				keyWritten = true;
				if (value == null) {
					appendNull();
				} else {
					appender.append(this, value);
				}
			} catch (final VPackBuilderException e) {
				// clean up in case of an exception
				if (haveReported) {
					cleanupAdd();
				}
				throw e;
			} finally {
				keyWritten = false;
			}
		} else {
			addInternal(appender, value);
		}
		return this;
	}

	private void set(final Value item) throws VPackBuilderException {
		final Class<?> clazz = item.getClazz();
		switch (item.getType()) {
		case NULL:
			appendNull();
			break;
		case ARRAY:
			addArray(item.isUnindexed());
			break;
		case OBJECT:
			addObject(item.isUnindexed());
			break;
		case SMALLINT:
			final long vSmallInt = item.getNumber().longValue();
			if (vSmallInt < -6 || vSmallInt > 9) {
				throw new VPackBuilderNumberOutOfRangeException(ValueType.SMALLINT);
			}
			appendSmallInt(vSmallInt);
			break;
		case INT:
			final int length;
			if (clazz == Long.class || clazz == BigInteger.class) {
				add((byte) 0x27);
				length = LONG_BYTES;
			} else {
				throw new VPackBuilderUnexpectedValueException(ValueType.INT, Long.class, Integer.class,
						BigInteger.class, Short.class);
			}
			append(item.getNumber().longValue(), length);
			break;
		case UINT:
			final BigInteger vUInt;
			if (clazz == Long.class) {
				vUInt = BigInteger.valueOf(item.getLong());
			} else if (clazz == BigInteger.class) {
				vUInt = item.getBigInteger();
			} else {
				throw new VPackBuilderUnexpectedValueException(ValueType.UINT, Long.class, Integer.class,
						BigInteger.class);
			}
			if (-1 == vUInt.compareTo(BigInteger.ZERO)) {
				throw new VPackBuilderUnexpectedValueException(ValueType.UINT, "non-negative", Long.class,
						Integer.class, BigInteger.class);
			}
			appendUInt(vUInt);
			break;
		default:
			break;
		}
	}

	private void appendNull() {
		add((byte) 0x18);
	}

	private void appendBoolean(final boolean value) {
		if (value) {
			add((byte) 0x1a);
		} else {
			add((byte) 0x19);
		}
	}

	private void appendDouble(final double value) {
		add((byte) 0x1b);
		append(value);
	}

	private void append(final double value) {
		append(Double.doubleToRawLongBits(value), DOUBLE_BYTES);
	}

	private void appendSmallInt(final long value) {
		if (value >= 0) {
			add((byte) (value + 0x30));
		} else {
			add((byte) (value + 0x40));
		}
	}

	private void appendUInt(final BigInteger value) {
		add((byte) 0x2f);
		append(value, LONG_BYTES);
	}

	private void append(final long value, final int length) {
		ensureCapacity(size + length);
		for (int i = length - 1; i >= 0; i--) {
			addUnchecked((byte) (value >> (length - i - 1 << 3)));
		}
	}

	private void append(final BigInteger value, final int length) {
		ensureCapacity(size + length);
		for (int i = length - 1; i >= 0; i--) {
			addUnchecked(value.shiftRight(length - i - 1 << 3).byteValue());
		}
	}

	private void appendDate(final Date value) {
		add((byte) 0x1c);
		append(value.getTime(), LONG_BYTES);
	}

	private void appendSQLDate(final java.sql.Date value) {
		add((byte) 0x1c);
		append(value.getTime(), LONG_BYTES);
	}

	private void appendSQLTimestamp(final Timestamp value) {
		add((byte) 0x1c);
		append(value.getTime(), LONG_BYTES);
	}

	private void appendString(final String value) throws VPackBuilderException {
		try {
			final byte[] bytes = value.getBytes("UTF-8");
			final int length = bytes.length;
			if (length <= 126) {
				// short string
				add((byte) (0x40 + length));
			} else {
				// long string
				add((byte) 0xbf);
				appendLength(length);
			}
			appendString(bytes);
		} catch (final UnsupportedEncodingException e) {
			throw new VPackBuilderException(e);
		}
	}

	private void appendString(final byte[] bytes) {
		ensureCapacity(size + bytes.length);
		System.arraycopy(bytes, 0, buffer, size, bytes.length);
		size += bytes.length;
	}

	private void appendBinary(final byte[] value) {
		add((byte) 0xc3);
		append(value.length, INTEGER_BYTES);
		ensureCapacity(size + value.length);
		System.arraycopy(value, 0, buffer, size, value.length);
		size += value.length;
	}

	private void appendVPack(final VPackSlice value) {
		final byte[] vpack = value.getRawVPack();
		ensureCapacity(size + vpack.length);
		System.arraycopy(vpack, 0, buffer, size, vpack.length);
		size += vpack.length;
	}

	private void addArray(final boolean unindexed) {
		addCompoundValue((byte) (unindexed ? 0x13 : 0x06));
	}

	private void addObject(final boolean unindexed) {
		addCompoundValue((byte) (unindexed ? 0x14 : 0x0b));
	}

	private void addCompoundValue(final byte head) {
		// an Array or Object is started:
		stack.add(size);
		index.put(stack.size() - 1, new ArrayList<Integer>());
		add(head);
		// Will be filled later with bytelength and nr subs
		size += 8;
		ensureCapacity(size);
	}

	private void appendLength(final long length) {
		append(length, LONG_BYTES);
	}

	private void reportAdd() {
		final Collection<Integer> depth = index.get(stack.size() - 1);
		depth.add(size - stack.get(stack.size() - 1));
	}

	private void cleanupAdd() {
		final List<Integer> depth = index.get(stack.size() - 1);
		depth.remove(depth.size() - 1);
	}

	public VPackBuilder close() throws VPackBuilderException {
		try {
			return close(true);
		} catch (final VPackKeyTypeException e) {
			throw new VPackBuilderException(e);
		} catch (final VPackNeedAttributeTranslatorException e) {
			throw new VPackBuilderException(e);
		}
	}

	protected VPackBuilder close(final boolean sort)
			throws VPackBuilderNeedOpenCompoundException, VPackKeyTypeException, VPackNeedAttributeTranslatorException {
		if (isClosed()) {
			throw new VPackBuilderNeedOpenCompoundException();
		}
		final byte head = head();
		final boolean isArray = head == 0x06 || head == 0x13;
		final List<Integer> in = index.get(stack.size() - 1);
		final int tos = stack.get(stack.size() - 1);
		if (in.isEmpty()) {
			return closeEmptyArrayOrObject(tos, isArray);
		}
		if (head == 0x13 || head == 0x14 || (head == 0x06 && options.isBuildUnindexedArrays())
				|| head == 0x0b && (options.isBuildUnindexedObjects() || in.size() == 1)) {
			if (closeCompactArrayOrObject(tos, isArray, in)) {
				return this;
			}
			// This might fall through, if closeCompactArrayOrObject gave up!
		}
		if (isArray) {
			return closeArray(tos, in);
		}
		// fix head byte in case a compact Array / Object was originally
		// requested
		buffer[tos] = (byte) 0x0b;

		// First determine byte length and its format:
		final int offsetSize;
		// can be 1, 2, 4 or 8 for the byte width of the offsets,
		// the byte length and the number of subvalues:
		if (size - tos + in.size() - 6 <= 0xff) {
			// We have so far used _pos - tos bytes, including the reserved 8
			// bytes for byte length and number of subvalues. In the 1-byte
			// number
			// case we would win back 6 bytes but would need one byte per
			// subvalue
			// for the index table
			offsetSize = 1;
		} else if ((size - tos) + 2 * in.size() <= 0xffff) {
			offsetSize = 2;
		} else if (((size - tos) / 2) + 4 * in.size() / 2 <= Integer.MAX_VALUE/* 0xffffffffu */) {
			offsetSize = 4;
		} else {
			offsetSize = 8;
		}
		// Maybe we need to move down data
		if (offsetSize == 1) {
			final int targetPos = 3;
			if ((size - 1) > (tos + 9)) {
				for (int i = tos + targetPos; i < tos + 9; i++) {
					remove(tos + targetPos);
				}
			}
			final int diff = 9 - targetPos;
			final int n = in.size();
			for (int i = 0; i < n; i++) {
				in.set(i, in.get(i) - diff);
			}
		}
		// One could move down things in the offsetSize == 2 case as well,
		// since we only need 4 bytes in the beginning. However, saving these
		// 4 bytes has been sacrificed on the Altar of Performance.

		// Now build the table:
		if (sort && in.size() >= 2) {
			// Object
			sortObjectIndex(tos, in);
		}
		// final int tableBase = size;
		for (int i = 0; i < in.size(); i++) {
			long x = in.get(i);
			ensureCapacity(size + offsetSize);
			for (int j = 0; j < offsetSize; j++) {
				addUnchecked(/* tableBase + offsetSize * i + j, */ (byte) (x & 0xff));
				x >>= 8;
			}
		}
		// Finally fix the byte width in the type byte:
		if (offsetSize > 1) {
			if (offsetSize == 2) {
				buffer[tos] = (byte) (buffer[tos] + 1);
			} else if (offsetSize == 4) {
				buffer[tos] = (byte) (buffer[tos] + 2);
			} else { // offsetSize == 8
				buffer[tos] = (byte) (buffer[tos] + 3);
				appendLength(in.size());
			}
		}
		// Fix the byte length in the beginning
		long x = size - tos;
		for (int i = 1; i <= offsetSize; i++) {
			buffer[tos + i] = (byte) (x & 0xff);
			x >>= 8;
		}
		// set the number of items in the beginning
		if (offsetSize < 8) {
			x = in.size();
			for (int i = offsetSize + 1; i <= 2 * offsetSize; i++) {
				buffer[tos + i] = (byte) (x & 0xff);
				x >>= 8;
			}
		}
		stack.remove(stack.size() - 1);
		return this;
	}

	private VPackBuilder closeEmptyArrayOrObject(final int tos, final boolean isArray) {
		// empty Array or Object
		buffer[tos] = (byte) (isArray ? 0x01 : 0x0a);
		// no bytelength and number subvalues needed
		for (int i = 1; i <= 8; i++) {
			remove(tos + 1);
		}
		stack.remove(stack.size() - 1);
		return this;
	}

	private boolean closeCompactArrayOrObject(final int tos, final boolean isArray, final List<Integer> in) {
		// use the compact Array / Object format
		final long nLen = NumberUtil.getVariableValueLength(in.size());
		long byteSize = size - (tos + 8) + nLen;
		long bLen = NumberUtil.getVariableValueLength(byteSize);
		byteSize += bLen;
		if (NumberUtil.getVariableValueLength(byteSize) != bLen) {
			byteSize += 1;
			bLen += 1;
		}
		if (bLen < 9) {
			// can only use compact notation if total byte length is at most
			// 8 bytes long
			buffer[tos] = (byte) (isArray ? 0x13 : 0x14);
			final int targetPos = (int) (1 + bLen);
			if (size - 1 > (tos + 9)) {
				for (int i = tos + targetPos; i < tos + 9; i++) {
					remove(tos + targetPos);
				}
			}
			// store byte length
			storeVariableValueLength(tos, byteSize, false);
			// need additional memory for storing the number of values
			if (nLen > 8 - bLen) {
				ensureCapacity((int) (size + nLen));
			}
			// store number of values
			storeVariableValueLength((int) (tos + byteSize), in.size(), true);
			size += nLen;
			stack.remove(stack.size() - 1);
			return true;
		}
		return false;
	}

	private void storeVariableValueLength(final int offset, final long value, final boolean reverse) {
		int i = offset;
		long val = value;
		if (reverse) {
			while (val >= 0x80) {
				buffer[--i] = (byte) ((byte) (val & 0x7f) | (byte) 0x80);
				val >>= 7;
			}
			buffer[--i] = (byte) (val & 0x7f);
		} else {
			while (val >= 0x80) {
				buffer[++i] = (byte) ((byte) (val & 0x7f) | (byte) 0x80);
				val >>= 7;
			}
			buffer[++i] = (byte) (val & 0x7f);
		}
	}

	private VPackBuilder closeArray(final int tos, final List<Integer> in) {
		// fix head byte in case a compact Array was originally
		// requested
		buffer[tos] = (byte) 0x06;

		boolean needIndexTable = true;
		boolean needNrSubs = true;
		final int n = in.size();
		if (n == 1) {
			needIndexTable = false;
			needNrSubs = false;
		} else if ((size - tos) - in.get(0) == n * (in.get(1) - in.get(0))) {
			// In this case it could be that all entries have the same length
			// and we do not need an offset table at all:
			boolean noTable = true;
			final int subLen = in.get(1) - in.get(0);
			if ((size - tos) - in.get(n - 1) != subLen) {
				noTable = false;
			} else {
				for (int i = 1; i < n - 1; i++) {
					if (in.get(i + 1) - in.get(i) != subLen) {
						noTable = false;
						break;
					}
				}
			}
			if (noTable) {
				needIndexTable = false;
				needNrSubs = false;
			}
		}

		// First determine byte length and its format:
		final int offsetSize;
		// can be 1, 2, 4 or 8 for the byte width of the offsets,
		// the byte length and the number of subvalues:
		if ((size - 1 - tos) + (needIndexTable ? n : 0) - (needNrSubs ? 6 : 7) <= 0xff) {
			// We have so far used _pos - tos bytes, including the reserved 8
			// bytes for byte length and number of subvalues. In the 1-byte
			// number
			// case we would win back 6 bytes but would need one byte per
			// subvalue
			// for the index table
			offsetSize = 1;
		} else if ((size - 1 - tos) + (needIndexTable ? 2 * n : 0) <= 0xffff) {
			offsetSize = 2;
		} else if (((size - 1 - tos) / 2) + ((needIndexTable ? 4 * n : 0) / 2) <= Integer.MAX_VALUE/* 0xffffffffu */) {
			offsetSize = 4;
		} else {
			offsetSize = 8;
		}
		// Maybe we need to move down data
		if (offsetSize == 1) {
			int targetPos = 3;
			if (!needIndexTable) {
				targetPos = 2;
			}
			if ((size - 1) > (tos + 9)) {
				for (int i = tos + targetPos; i < tos + 9; i++) {
					remove(tos + targetPos);
				}
			}
			final int diff = 9 - targetPos;
			if (needIndexTable) {
				for (int i = 0; i < n; i++) {
					in.set(i, in.get(i) - diff);
				}
			} // Note: if !needIndexTable the index is now wrong!
		}
		// One could move down things in the offsetSize == 2 case as well,
		// since we only need 4 bytes in the beginning. However, saving these
		// 4 bytes has been sacrificed on the Altar of Performance.

		// Now build the table:
		if (needIndexTable) {
			// final int tableBase = size;
			for (int i = 0; i < n; i++) {
				long x = in.get(i);
				ensureCapacity(size + offsetSize);
				for (int j = 0; j < offsetSize; j++) {
					addUnchecked(/* tableBase + offsetSize * i + j, */ (byte) (x & 0xff));
					x >>= 8;
				}
			}
		} else { // no index table
			buffer[tos] = (byte) 0x02;
		}
		// Finally fix the byte width in the type byte:
		if (offsetSize > 1) {
			if (offsetSize == 2) {
				buffer[tos] = (byte) (buffer[tos] + 1);
			} else if (offsetSize == 4) {
				buffer[tos] = (byte) (buffer[tos] + 2);
			} else { // offsetSize == 8
				buffer[tos] = (byte) (buffer[tos] + 3);
				if (needNrSubs) {
					appendLength(n);
				}
			}
		}
		// Fix the byte length in the beginning
		long x = size - tos;
		for (int i = 1; i <= offsetSize; i++) {
			buffer[tos + i] = (byte) (x & 0xff);
			x >>= 8;
		}
		// set the number of items in the beginning
		if (offsetSize < 8 && needNrSubs) {
			x = n;
			for (int i = offsetSize + 1; i <= 2 * offsetSize; i++) {
				buffer[tos + i] = (byte) (x & 0xff);
				x >>= 8;
			}
		}
		stack.remove(stack.size() - 1);
		return this;
	}

	private static class SortEntry {
		private final VPackSlice slice;
		private final int offset;

		public SortEntry(final VPackSlice slice, final int offset) {
			super();
			this.slice = slice;
			this.offset = offset;
		}
	}

	private void sortObjectIndex(final int start, final List<Integer> offsets)
			throws VPackKeyTypeException, VPackNeedAttributeTranslatorException {
		final List<VPackBuilder.SortEntry> attributes = new ArrayList<VPackBuilder.SortEntry>();
		for (final Integer offset : offsets) {
			attributes.add(new SortEntry(new VPackSlice(buffer, start + offset).makeKey(), offset));
		}
		final Comparator<SortEntry> comparator = new Comparator<SortEntry>() {
			@Override
			public int compare(final SortEntry o1, final SortEntry o2) {
				return o1.slice.getAsString().compareTo(o2.slice.getAsString());
			}
		};
		Collections.sort(attributes, comparator);
		offsets.clear();
		for (final SortEntry sortEntry : attributes) {
			offsets.add(sortEntry.offset);
		}
	}

	public static int compareTo(
		final byte[] b1,
		final int b1Index,
		final int b1Length,
		final byte[] b2,
		final int b2Index,
		final int b2Length) {
		final int commonLength = Math.min(b1Length, b2Length);
		for (int i = 0; i < commonLength; i++) {
			final byte byte1 = b1[b1Index + i];
			final byte byte2 = b2[b2Index + i];
			if (byte1 != byte2) {
				return (byte1 < byte2) ? -1 : 1;
			}
		}
		if (b1Length != b2Length) {
			return (b1Length < b2Length) ? -2 : 2;
		}
		return 0;
	}

	private boolean isClosed() {
		return stack.isEmpty();
	}

	private byte head() {
		final Integer in = stack.get(stack.size() - 1);
		return buffer[in];
	}

	public VPackSlice slice() {
		return new VPackSlice(buffer);
	}

	public int getVpackSize() {
		return size;
	}

}

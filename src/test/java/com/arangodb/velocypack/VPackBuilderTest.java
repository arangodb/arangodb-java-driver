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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Date;

import org.junit.Test;

import com.arangodb.ArangoDB;
import com.arangodb.velocypack.exception.VPackBuilderNeedOpenCompoundException;
import com.arangodb.velocypack.exception.VPackBuilderNumberOutOfRangeException;
import com.arangodb.velocypack.exception.VPackBuilderUnexpectedValueException;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackBuilderTest {

	@Test
	public void empty() {
		final VPackSlice slice = new VPackBuilder().slice();
		assertThat(slice.isNone(), is(true));
	}

	@Test
	public void addNull() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.NULL);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isNull(), is(true));
	}

	@Test
	public void addBooleanTrue() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(true);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isBoolean(), is(true));
		assertThat(slice.getAsBoolean(), is(true));
	}

	@Test
	public void addBooleanFalse() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(false);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isBoolean(), is(true));
		assertThat(slice.getAsBoolean(), is(false));
	}

	@Test
	public void addBooleanNull() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final Boolean b = null;
		builder.add(b);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isNull(), is(true));
	}

	@Test
	public void addDouble() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final double value = Double.MAX_VALUE;
		builder.add(value);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isDouble(), is(true));
		assertThat(slice.getAsDouble(), is(value));
	}

	@Test
	public void addIntegerAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = -6;
		builder.add(value);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsInt(), is(value));
	}

	@Test
	public void addIntegerAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = 9;
		builder.add(value);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsInt(), is(value));
	}

	@Test
	public void addLongAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = -6;
		builder.add(value, ValueType.SMALLINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsLong(), is(value));
	}

	@Test
	public void addLongAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = 9;
		builder.add(value, ValueType.SMALLINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsLong(), is(value));
	}

	@Test(expected = VPackBuilderNumberOutOfRangeException.class)
	public void addLongAsSmallIntOutofRange() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(value, ValueType.SMALLINT);
	}

	@Test
	public void addBigIntegerAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(-6);
		builder.add(value, ValueType.SMALLINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsBigInteger(), is(value));
	}

	@Test
	public void addBigIntegerAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(9);
		builder.add(value, ValueType.SMALLINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isSmallInt(), is(true));
		assertThat(slice.getAsBigInteger(), is(value));
	}

	@Test(expected = VPackBuilderNumberOutOfRangeException.class)
	public void addBigIntegerAsSmallIntOutofRange() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(value, ValueType.SMALLINT);
	}

	@Test
	public void addIntegerAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = Integer.MAX_VALUE;
		builder.add(value);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isInt(), is(true));
		assertThat(slice.getAsInt(), is(value));
	}

	@Test
	public void addLongAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(value, ValueType.INT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isInt(), is(true));
		assertThat(slice.getAsLong(), is(value));
	}

	@Test
	public void addBigIntegerAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(value, ValueType.INT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isInt(), is(true));
		assertThat(slice.getAsBigInteger(), is(value));
	}

	@Test
	public void addLongAsUInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(value, ValueType.UINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isUInt(), is(true));
		assertThat(slice.getAsLong(), is(value));
	}

	@Test
	public void addBigIntegerAsUInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(value, ValueType.UINT);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isUInt(), is(true));
		assertThat(slice.getAsBigInteger(), is(value));
	}

	@Test(expected = VPackBuilderUnexpectedValueException.class)
	public void addLongAsUIntNegative() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = -10;
		builder.add(value, ValueType.UINT);
	}

	@Test(expected = VPackBuilderUnexpectedValueException.class)
	public void addBigIntegerAsUIntNegative() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(-10);
		builder.add(value, ValueType.UINT);
	}

	@Test
	public void addDate() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final Date date = new Date();
		builder.add(date);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isDate(), is(true));
		assertThat(slice.getAsDate(), is(date));
	}

	@Test
	public void addSqlDate() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final java.sql.Date date = new java.sql.Date(new Date().getTime());
		builder.add(date);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isDate(), is(true));
		assertThat(slice.getAsSQLDate(), is(date));
	}

	@Test
	public void addSqlTimestamp() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final java.sql.Timestamp timestamp = new java.sql.Timestamp(new Date().getTime());
		builder.add(timestamp);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isDate(), is(true));
		assertThat(slice.getAsSQLTimestamp(), is(timestamp));
	}

	@Test
	public void addStringShort() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final String s = "Hallo Welt!";
		builder.add(s);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isString(), is(true));
		assertThat(slice.getAsString(), is(s));
	}

	@Test
	public void addStringLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final String s = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.";
		builder.add(s);

		final VPackSlice slice = builder.slice();
		assertThat(slice.isString(), is(true));
		assertThat(slice.getAsString(), is(s));
	}

	@Test
	public void emptyArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(0));
		try {
			slice.get(0);
			fail();
		} catch (final IndexOutOfBoundsException e) {

		}
	}

	@Test
	public void compactArray() throws VPackException {
		final long[] expected = { 1, 16 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY, true);
		for (final long l : expected) {
			builder.add(l);
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(2));
		for (int i = 0; i < expected.length; i++) {
			final VPackSlice at = slice.get(i);
			assertThat(at.isInteger(), is(true));
			assertThat(at.getAsLong(), is(expected[i]));
		}
	}

	@Test
	public void arrayItemsSameLength() throws VPackException {
		VPackSlice sliceNotSame;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(ValueType.ARRAY);
			builder.add("aa");
			builder.add("a");
			builder.close();
			sliceNotSame = builder.slice();
		}
		VPackSlice sliceSame;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(ValueType.ARRAY);
			builder.add("aa");
			builder.add("aa");
			builder.close();
			sliceSame = builder.slice();
		}
		assertThat(sliceSame.getByteSize() < sliceNotSame.getByteSize(), is(true));
	}

	@Test
	public void unindexedArray() throws VPackException {
		final long[] expected = { 1, 16 };
		final VPackBuilder builder = new VPackBuilder();
		builder.getOptions().setBuildUnindexedArrays(true);
		builder.add(ValueType.ARRAY, false);
		for (final long l : expected) {
			builder.add(l);
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(2));
		for (int i = 0; i < expected.length; i++) {
			final VPackSlice at = slice.get(i);
			assertThat(at.isInteger(), is(true));
			assertThat(at.getAsLong(), is(expected[i]));
		}
	}

	@Test
	public void indexedArray() throws VPackException {
		final long[] values = { 1, 2, 3 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (final long l : values) {
			builder.add(l);
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(3));
	}

	@Test
	public void indexedArray2ByteLength() throws VPackException {
		final int valueCount = 100;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (long i = 0; i < valueCount; i++) {
			builder.add(
				i + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.");
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.head(), is((byte) 0x07));
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(valueCount));
	}

	@Test
	public void indexedArray2ByteLengthNoIndexTable() throws VPackException {
		final int valueCount = 100;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (long i = 0; i < valueCount; i++) {
			builder.add(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.");
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.head(), is((byte) 0x03));
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(valueCount));
	}

	@Test
	public void indexedArray4ByteLength() throws VPackException {
		final int valueCount = 200;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (long i = 0; i < valueCount; i++) {
			builder.add(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.");
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.head(), is((byte) 0x04));
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(valueCount));
	}

	@Test
	public void indexedArray4ByteLengthNoIndexTable() throws VPackException {
		final int valueCount = 200;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (long i = 0; i < valueCount; i++) {
			builder.add(
				i + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.");
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.head(), is((byte) 0x08));
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(valueCount));
	}

	@Test
	public void arrayInArray() throws VPackException {
		final long[][] values = { { 1, 2, 3 }, { 1, 2, 3 } };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (final long[] ls : values) {
			builder.add(ValueType.ARRAY);
			for (final long l : ls) {
				builder.add(l);
			}
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(values.length));
		for (int i = 0; i < values.length; i++) {
			final VPackSlice ls = slice.get(i);
			assertThat(ls.isArray(), is(true));
			assertThat(ls.getLength(), is(values[i].length));
			for (int j = 0; j < values[i].length; j++) {
				final VPackSlice l = ls.get(j);
				assertThat(l.isInteger(), is(true));
				assertThat(l.getAsLong(), is(values[i][j]));
			}
		}
	}

	@Test
	public void arrayInArrayInArray() throws VPackException {
		final long[][][] values = { { { 1, 2, 3 } } };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (final long[][] lss : values) {
			builder.add(ValueType.ARRAY);
			for (final long[] ls : lss) {
				builder.add(ValueType.ARRAY);
				for (final long l : ls) {
					builder.add(l);
				}
				builder.close();
			}
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isArray(), is(true));
		assertThat(slice.getLength(), is(values.length));
		for (int i = 0; i < values.length; i++) {
			final VPackSlice lls = slice.get(i);
			assertThat(lls.isArray(), is(true));
			assertThat(lls.getLength(), is(values[i].length));
			for (int j = 0; j < values[i].length; j++) {
				final VPackSlice ls = lls.get(i);
				assertThat(ls.isArray(), is(true));
				assertThat(ls.getLength(), is(values[i][j].length));
				for (int k = 0; k < values[i][j].length; k++) {
					final VPackSlice l = ls.get(k);
					assertThat(l.isInteger(), is(true));
					assertThat(l.getAsLong(), is(values[i][j][k]));
				}
			}

		}
	}

	@Test
	public void emptyObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(0));
		final VPackSlice a = slice.get("a");
		assertThat(a.isNone(), is(true));
		try {
			slice.keyAt(0);
			fail();
		} catch (final IndexOutOfBoundsException e) {

		}
		try {
			slice.valueAt(0);
			fail();
		} catch (final IndexOutOfBoundsException e) {

		}
	}

	@Test
	public void compactObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT, true);
		builder.add("a", 12);
		builder.add("b", true);
		builder.add("c", "xyz");
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(3));
		assertThat(slice.get("a").getAsLong(), is(12L));
		assertThat(slice.get("b").getAsBoolean(), is(true));
		assertThat(slice.get("c").getAsString(), is("xyz"));
	}

	@Test
	public void unindexedObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.getOptions().setBuildUnindexedObjects(true);
		builder.add(ValueType.OBJECT, false);
		builder.add("a", 12);
		builder.add("b", true);
		builder.add("c", "xyz");
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(3));
		assertThat(slice.get("a").getAsLong(), is(12L));
		assertThat(slice.get("b").getAsBoolean(), is(true));
		assertThat(slice.get("c").getAsString(), is("xyz"));
	}

	@Test
	public void indexedObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", 12);
		builder.add("b", true);
		builder.add("c", "xyz");
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(3));
		assertThat(slice.get("a").getAsLong(), is(12L));
		assertThat(slice.get("b").getAsBoolean(), is(true));
		assertThat(slice.get("c").getAsString(), is("xyz"));
	}

	@Test
	public void objectInObject() throws VPackException {
		// {"a":{"a1":1,"a2":2},"b":{"b1":1,"b2":1}}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		{
			builder.add("a", ValueType.OBJECT);
			builder.add("a1", 1);
			builder.add("a2", 2);
			builder.close();
		}
		{
			builder.add("b", ValueType.OBJECT);
			builder.add("b1", 1);
			builder.add("b2", 2);
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(2));
		{
			final VPackSlice a = slice.get("a");
			assertThat(a.isObject(), is(true));
			assertThat(a.getLength(), is(2));
			assertThat(a.get("a1").getAsLong(), is(1L));
			assertThat(a.get("a2").getAsLong(), is(2L));
		}
		{
			final VPackSlice b = slice.get("b");
			assertThat(b.isObject(), is(true));
			assertThat(b.getLength(), is(2));
			assertThat(b.get("b1").getAsLong(), is(1L));
			assertThat(b.get("b2").getAsLong(), is(2L));
		}
	}

	@Test
	public void objectInObjectInObject() throws VPackException {
		// {"a":{"b":{"c":{"d":true}}}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.OBJECT);
		builder.add("b", ValueType.OBJECT);
		builder.add("c", ValueType.OBJECT);
		builder.add("d", true);
		builder.close();
		builder.close();
		builder.close();
		builder.close();

		final VPackSlice slice = builder.slice();
		assertThat(slice.isObject(), is(true));
		assertThat(slice.getLength(), is(1));
		final VPackSlice a = slice.get("a");
		assertThat(a.isObject(), is(true));
		assertThat(a.getLength(), is(1));
		final VPackSlice b = a.get("b");
		assertThat(b.isObject(), is(true));
		assertThat(b.getLength(), is(1));
		final VPackSlice c = b.get("c");
		assertThat(c.isObject(), is(true));
		assertThat(c.getLength(), is(1));
		final VPackSlice d = c.get("d");
		assertThat(d.isBoolean(), is(true));
		assertThat(d.isTrue(), is(true));
	}

	@Test
	public void objectAttributeNotFound() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "a");
		builder.close();
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.isObject(), is(true));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isNone(), is(true));
	}

	@Test
	public void object1ByteOffset() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		final int size = 5;
		for (int i = 0; i < size; i++) {
			builder.add(String.valueOf(i), ValueType.OBJECT);
			for (int j = 0; j < size; j++) {
				builder.add(String.valueOf(j), "test");
			}
			builder.close();
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(size));
		for (int i = 0; i < size; i++) {
			final VPackSlice attr = vpack.get(String.valueOf(i));
			assertThat(attr.isObject(), is(true));
			for (int j = 0; j < size; j++) {
				final VPackSlice childAttr = attr.get(String.valueOf(j));
				assertThat(childAttr.isString(), is(true));
			}
		}
	}

	@Test
	public void object2ByteOffset() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		final int size = 10;
		for (int i = 0; i < size; i++) {
			builder.add(String.valueOf(i), ValueType.OBJECT);
			for (int j = 0; j < size; j++) {
				builder.add(String.valueOf(j), "test");
			}
			builder.close();
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(size));
		for (int i = 0; i < size; i++) {
			final VPackSlice attr = vpack.get(String.valueOf(i));
			assertThat(attr.isObject(), is(true));
			for (int j = 0; j < size; j++) {
				final VPackSlice childAttr = attr.get(String.valueOf(j));
				assertThat(childAttr.isString(), is(true));
			}
		}
	}

	@Test
	public void sortObjectAttr() throws VPackException {
		final int min = 0;
		final int max = 9;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		for (int i = max; i >= min; i--) {
			builder.add(String.valueOf(i), "test");
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(max - min + 1));
		for (int i = min, j = 0; i <= max; i++, j++) {
			assertThat(vpack.keyAt(j).getAsString(), is(String.valueOf(i)));
		}
	}

	@Test
	public void sortObjectAttr2() throws VPackException {
		final String[] keys = { "a", "b", "c", "d", "e", "f", "g", "h" };
		final String[] keysUnsorted = { "b", "d", "c", "e", "g", "f", "h", "a" };
		assertThat(keysUnsorted.length, is(keys.length));
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		for (int i = 0; i < keysUnsorted.length; i++) {
			builder.add(String.valueOf(keysUnsorted[i]), "test");
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(keys.length));
		for (int i = 0; i < keys.length; i++) {
			assertThat(vpack.keyAt(i).getAsString(), is(String.valueOf(keys[i])));
		}
	}

	@Test
	public void attributeAdapterDefaults() throws VPackException {
		final VPackSlice vpackWithAttrAdapter;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(ValueType.OBJECT);
			builder.add("_key", "a");
			builder.close();
			vpackWithAttrAdapter = builder.slice();
			assertThat(vpackWithAttrAdapter.isObject(), is(true));
		}
		final VPackSlice vpackWithoutAttrAdapter;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(ValueType.OBJECT);
			builder.add("_kay", "a");
			builder.close();
			vpackWithoutAttrAdapter = builder.slice();
			assertThat(vpackWithoutAttrAdapter.isObject(), is(true));
		}
		assertThat(vpackWithAttrAdapter.getByteSize() < vpackWithoutAttrAdapter.getByteSize(), is(true));
	}

	@Test(expected = VPackBuilderNeedOpenCompoundException.class)
	public void closeClosed() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.close();
		builder.close();
	}

	@Test
	public void addBinary() throws VPackException {
		final byte[] expected = new byte[] { 49, 50, 51, 52, 53, 54, 55, 56, 57 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(expected);
		final VPackSlice slice = builder.slice();

		assertThat(slice.isBinary(), is(true));
		assertThat(slice.getBinaryLength(), is(expected.length));
		assertThat(slice.getAsBinary(), is(expected));
		assertThat(slice.getByteSize(), is(1 + 4 + expected.length));
	}

	@Test
	public void addVPack() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("s", new VPackBuilder().add("test").slice());
		builder.close();
		final VPackSlice slice = builder.slice();
		assertThat(slice, is(notNullValue()));
		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("s").isString(), is(true));
		assertThat(slice.get("s").getAsString(), is("test"));
		assertThat(slice.size(), is(1));
	}

	@Test
	public void addVPackObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		{
			final VPackBuilder builder2 = new VPackBuilder();
			builder2.add(ValueType.OBJECT);
			builder2.add("s", "test");
			builder2.close();
			builder.add("o", builder2.slice());
		}
		builder.close();
		final VPackSlice slice = builder.slice();
		assertThat(slice, is(notNullValue()));
		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("o").isObject(), is(true));
		assertThat(slice.get("o").get("s").isString(), is(true));
		assertThat(slice.get("o").get("s").getAsString(), is("test"));
		assertThat(slice.size(), is(1));
		assertThat(slice.get("o").size(), is(1));
	}

	@Test
	public void addVPackObjectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		for (int i = 0; i < 10; i++) {
			final VPackBuilder builder2 = new VPackBuilder();
			builder2.add(ValueType.OBJECT);
			builder2.add("s", "test");
			builder2.close();
			builder.add(builder2.slice());
		}
		builder.close();
		final VPackSlice slice = builder.slice();
		assertThat(slice, is(notNullValue()));
		assertThat(slice.isArray(), is(true));
		assertThat(slice.size(), is(10));
		for (int i = 0; i < 10; i++) {
			assertThat(slice.get(i).isObject(), is(true));
			assertThat(slice.get(i).get("s").isString(), is(true));
			assertThat(slice.get(i).get("s").getAsString(), is("test"));
			assertThat(slice.get(i).size(), is(1));
		}
	}

	@Test
	public void nonASCII() {
		final String s = "·ÃÂ";
		final VPackSlice vpack = new VPackBuilder().add(s).slice();
		assertThat(vpack.isString(), is(true));
		assertThat(vpack.getAsString(), is(s));
	}

	@Test
	public void addLong() {
		final long value = 12345678901L;
		final VPackBuilder builder = new VPackBuilder().add(value);
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.getAsLong(), is(value));
	}

	@Test
	public void addBitInteger() {
		final BigInteger value = new BigInteger("12345678901");
		final VPackBuilder builder = new VPackBuilder().add(value);
		final VPackSlice vpack = builder.slice();
		assertThat(vpack.getAsBigInteger(), is(value));
	}

	@Test
	public void objectWithByteSize256() {
		final StringBuilder aa = new StringBuilder();
		final int stringLength = 256 - 25;
		for (int i = 0; i < stringLength; ++i) {
			aa.append("a");
		}
		final String foo = "foo";
		final String bar1 = "bar1";
		final String bar2 = "bar2";
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add(foo, ValueType.OBJECT).add(bar2, "")
				.add(bar1, aa.toString()).close().close().slice();

		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get(foo).isObject(), is(true));
		assertThat(vpack.get(foo).get(bar1).isString(), is(true));
		assertThat(vpack.get(foo).get(bar1).getLength(), is(stringLength));
		assertThat(vpack.get(foo).get(bar2).isString(), is(true));
		assertThat(vpack.get(foo).get(bar2).getLength(), is(0));
	}

	@Test
	public void objectWithByteSizeOver65536() {
		final StringBuilder aa = new StringBuilder();
		final int stringLength = 65536 - 25 - 8;
		for (int i = 0; i < stringLength; ++i) {
			aa.append("a");
		}
		final String foo = "foo";
		final String bar1 = "bar1";
		final String bar2 = "bar2";
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add(foo, ValueType.OBJECT).add(bar2, "")
				.add(bar1, aa.toString()).close().close().slice();

		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get(foo).isObject(), is(true));
		assertThat(vpack.get(foo).get(bar1).isString(), is(true));
		assertThat(vpack.get(foo).get(bar1).getLength(), is(stringLength));
		assertThat(vpack.get(foo).get(bar2).isString(), is(true));
		assertThat(vpack.get(foo).get(bar2).getLength(), is(0));
	}

	@Test
	public void bytelength() {
		final String name1 = "{\"name1\":\"job_04_detail_1\",\"seven__\":\"123456789\",\"_key\":\"191d936d-1eb9-4094-9c1c-9e0ba1d01867\",\"lang\":\"it\",\"value\":\"[CTO]\\n Ha supervisionato e gestito il reparto di R&D per il software, 1234567 formulando una visione di lungo periodo con la Direzione dell'Azienda.\"}";
		final String name = "{\"name\":\"job_04_detail_1\",\"seven__\":\"123456789\",\"_key\":\"191d936d-1eb9-4094-9c1c-9e0ba1d01867\",\"lang\":\"it\",\"value\":\"[CTO]\\n Ha supervisionato e gestito il reparto di R&D per il software, 1234567 formulando una visione di lungo periodo con la Direzione dell'Azienda.\"}";

		final ArangoDB arangoDB = new ArangoDB.Builder().build();
		{
			final VPackSlice vpack = arangoDB.util().serialize(name1);
			assertThat(vpack.isObject(), is(true));
			assertThat(vpack.get("name1").isString(), is(true));
			assertThat(vpack.get("name1").getAsString(), is("job_04_detail_1"));

		}
		{
			final VPackSlice vpack = arangoDB.util().serialize(name);
			assertThat(vpack.isObject(), is(true));
			assertThat(vpack.get("name").isString(), is(true));
			assertThat(vpack.get("name").getAsString(), is("job_04_detail_1"));
		}
	}

}

package com.arangodb.velocypack;

import java.math.BigInteger;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.arangodb.velocypack.exception.VPackBuilderNeedOpenCompoundException;
import com.arangodb.velocypack.exception.VPackBuilderNumberOutOfRangeException;
import com.arangodb.velocypack.exception.VPackBuilderUnexpectedValueException;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackBuilderTest {

	@Test
	public void empty() {
		final VPackSlice slice = new VPackBuilder().slice();
		Assert.assertTrue(slice.isNone());
	}

	@Test
	public void addNull() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.NULL));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isNull());
	}

	@Test
	public void addBooleanTrue() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(true));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isBoolean());
		Assert.assertTrue(slice.getAsBoolean());
	}

	@Test
	public void addBooleanFalse() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(false));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isBoolean());
		Assert.assertFalse(slice.getAsBoolean());
	}

	@Test
	public void addBooleanNull() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final Boolean b = null;
		builder.add(new Value(b));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isNull());
	}

	@Test
	public void addDouble() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final double value = Double.MAX_VALUE;
		builder.add(new Value(value));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isDouble());
		Assert.assertEquals(value, slice.getAsDouble(), 0);
	}

	@Test
	public void addIntegerAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = -6;
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsInt());
	}

	@Test
	public void addIntegerAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = 9;
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsInt());
	}

	@Test(expected = VPackBuilderNumberOutOfRangeException.class)
	public void addIntegerAsSmallIntOutofRange() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = Integer.MAX_VALUE;
		builder.add(new Value(value, ValueType.SMALLINT));
	}

	@Test
	public void addLongAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = -6;
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsLong());
	}

	@Test
	public void addLongAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = 9;
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsLong());
	}

	@Test(expected = VPackBuilderNumberOutOfRangeException.class)
	public void addLongAsSmallIntOutofRange() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(new Value(value, ValueType.SMALLINT));
	}

	@Test
	public void addBigIntegerAsSmallIntMin() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(-6);
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsBigInteger());
	}

	@Test
	public void addBigIntegerAsSmallIntMax() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(9);
		builder.add(new Value(value, ValueType.SMALLINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isSmallInt());
		Assert.assertEquals(value, slice.getAsBigInteger());
	}

	@Test(expected = VPackBuilderNumberOutOfRangeException.class)
	public void addBigIntegerAsSmallIntOutofRange() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(new Value(value, ValueType.SMALLINT));
	}

	@Test
	public void addIntegerAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final int value = Integer.MAX_VALUE;
		builder.add(new Value(value, ValueType.INT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isInt());
		Assert.assertEquals(value, slice.getAsInt());
	}

	@Test
	public void addLongAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(new Value(value, ValueType.INT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isInt());
		Assert.assertEquals(value, slice.getAsLong());
	}

	@Test
	public void addBigIntegerAsInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(new Value(value, ValueType.INT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isInt());
		Assert.assertEquals(value, slice.getAsBigInteger());
	}

	@Test
	public void addLongAsUInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = Long.MAX_VALUE;
		builder.add(new Value(value, ValueType.UINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isUInt());
		Assert.assertEquals(value, slice.getAsLong());
	}

	@Test
	public void addBigIntegerAsUInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(Long.MAX_VALUE);
		builder.add(new Value(value, ValueType.UINT));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isUInt());
		Assert.assertEquals(value, slice.getAsBigInteger());
	}

	@Test(expected = VPackBuilderUnexpectedValueException.class)
	public void addLongAsUIntNegative() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final long value = -10;
		builder.add(new Value(value, ValueType.UINT));
	}

	@Test(expected = VPackBuilderUnexpectedValueException.class)
	public void addBigIntegerAsUIntNegative() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final BigInteger value = BigInteger.valueOf(-10);
		builder.add(new Value(value, ValueType.UINT));
	}

	@Test
	public void addUTCDate() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final Date date = new Date();
		builder.add(new Value(date));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isDate());
		Assert.assertEquals(date, slice.getAsDate());
	}

	@Test
	public void addStringShort() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final String s = "Hallo Welt!";
		builder.add(new Value(s));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isString());
		Assert.assertEquals(s, slice.getAsString());
	}

	@Test
	public void addStringLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final String s = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus.";
		builder.add(new Value(s));

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isString());
		Assert.assertEquals(s, slice.getAsString());
	}

	@Test
	public void emptyArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(0, slice.getLength());
		try {
			slice.get(0);
			Assert.fail();
		} catch (final IndexOutOfBoundsException e) {

		}
	}

	@Test
	public void compactArray() throws VPackException {
		final long[] expected = { 1, 16 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY, true));
		for (final long l : expected) {
			builder.add(new Value(l));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(2, slice.getLength());
		for (int i = 0; i < expected.length; i++) {
			final VPackSlice at = slice.get(i);
			Assert.assertTrue(at.isInteger());
			Assert.assertEquals(expected[i], at.getAsLong());
		}
	}

	@Test
	public void arrayItemsSameLength() throws VPackException {
		VPackSlice sliceNotSame;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(new Value(ValueType.ARRAY));
			builder.add(new Value("aa"));
			builder.add(new Value("a"));
			builder.close();
			sliceNotSame = builder.slice();
		}
		VPackSlice sliceSame;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(new Value(ValueType.ARRAY));
			builder.add(new Value("aa"));
			builder.add(new Value("aa"));
			builder.close();
			sliceSame = builder.slice();
		}
		Assert.assertTrue(sliceSame.getByteSize() < sliceNotSame.getByteSize());
	}

	@Test
	public void unindexedArray() throws VPackException {
		final long[] expected = { 1, 16 };
		final VPackBuilder builder = new VPackBuilder();
		builder.getOptions().setBuildUnindexedArrays(true);
		builder.add(new Value(ValueType.ARRAY, false));
		for (final long l : expected) {
			builder.add(new Value(l));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(2, slice.getLength());
		for (int i = 0; i < expected.length; i++) {
			final VPackSlice at = slice.get(i);
			Assert.assertTrue(at.isInteger());
			Assert.assertEquals(expected[i], at.getAsLong());
		}
	}

	@Test
	public void indexedArray() throws VPackException {
		final long[] values = { 1, 2, 3 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (final long l : values) {
			builder.add(new Value(l));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(3, slice.getLength());
	}

	@Test
	public void indexedArray2ByteLength() throws VPackException {
		final long valueCount = 100;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (long i = 0; i < valueCount; i++) {
			builder.add(new Value(i
					+ "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus."));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertEquals(0x07, slice.head());
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(valueCount, slice.getLength());
	}

	@Test
	public void indexedArray2ByteLengthNoIndexTable() throws VPackException {
		final long valueCount = 100;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (long i = 0; i < valueCount; i++) {
			builder.add(new Value(
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus."));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertEquals(0x03, slice.head());
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(valueCount, slice.getLength());
	}

	@Test
	public void indexedArray4ByteLength() throws VPackException {
		final long valueCount = 200;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (long i = 0; i < valueCount; i++) {
			builder.add(new Value(
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus."));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertEquals(0x04, slice.head());
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(valueCount, slice.getLength());
	}

	@Test
	public void indexedArray4ByteLengthNoIndexTable() throws VPackException {
		final long valueCount = 200;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (long i = 0; i < valueCount; i++) {
			builder.add(new Value(i
					+ "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus."));
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertEquals(0x08, slice.head());
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(valueCount, slice.getLength());
	}

	@Test
	public void arrayInArray() throws VPackException {
		final long[][] values = { { 1, 2, 3 }, { 1, 2, 3 } };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (final long[] ls : values) {
			builder.add(new Value(ValueType.ARRAY));
			for (final long l : ls) {
				builder.add(new Value(l));
			}
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(values.length, slice.getLength());
		for (int i = 0; i < values.length; i++) {
			final VPackSlice ls = slice.get(i);
			Assert.assertTrue(ls.isArray());
			Assert.assertEquals(values[i].length, ls.getLength());
			for (int j = 0; j < values[i].length; j++) {
				final VPackSlice l = ls.get(j);
				Assert.assertTrue(l.isInteger());
				Assert.assertEquals(values[i][j], l.getAsLong());
			}
		}
	}

	@Test
	public void arrayInArrayInArray() throws VPackException {
		final long[][][] values = { { { 1, 2, 3 } } };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		for (final long[][] lss : values) {
			builder.add(new Value(ValueType.ARRAY));
			for (final long[] ls : lss) {
				builder.add(new Value(ValueType.ARRAY));
				for (final long l : ls) {
					builder.add(new Value(l));
				}
				builder.close();
			}
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isArray());
		Assert.assertEquals(values.length, slice.getLength());
		for (int i = 0; i < values.length; i++) {
			final VPackSlice lls = slice.get(i);
			Assert.assertTrue(lls.isArray());
			Assert.assertEquals(values[i].length, lls.getLength());
			for (int j = 0; j < values[i].length; j++) {
				final VPackSlice ls = lls.get(i);
				Assert.assertTrue(ls.isArray());
				Assert.assertEquals(values[i][j].length, ls.getLength());
				for (int k = 0; k < values[i][j].length; k++) {
					final VPackSlice l = ls.get(k);
					Assert.assertTrue(l.isInteger());
					Assert.assertEquals(values[i][j][k], l.getAsLong());
				}
			}

		}
	}

	@Test
	public void emptyObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(0, slice.getLength());
		final VPackSlice a = slice.get("a");
		Assert.assertTrue(a.isNone());
		try {
			slice.keyAt(0);
			Assert.fail();
		} catch (final IndexOutOfBoundsException e) {

		}
		try {
			slice.valueAt(0);
			Assert.fail();
		} catch (final IndexOutOfBoundsException e) {

		}
	}

	@Test
	public void compactObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT, true));
		builder.add("a", new Value(12));
		builder.add("b", new Value(true));
		builder.add("c", new Value("xyz"));
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(3, slice.getLength());
		Assert.assertEquals(12, slice.get("a").getAsLong());
		Assert.assertEquals(true, slice.get("b").getAsBoolean());
		Assert.assertEquals("xyz", slice.get("c").getAsString());
	}

	@Test
	public void unindexedObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.getOptions().setBuildUnindexedObjects(true);
		builder.add(new Value(ValueType.OBJECT, false));
		builder.add("a", new Value(12));
		builder.add("b", new Value(true));
		builder.add("c", new Value("xyz"));
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(3, slice.getLength());
		Assert.assertEquals(12, slice.get("a").getAsLong());
		Assert.assertEquals(true, slice.get("b").getAsBoolean());
		Assert.assertEquals("xyz", slice.get("c").getAsString());
	}

	@Test
	public void indexedObject() throws VPackException {
		// {"a": 12, "b": true, "c": "xyz"}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(12));
		builder.add("b", new Value(true));
		builder.add("c", new Value("xyz"));
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(3, slice.getLength());
		Assert.assertEquals(12, slice.get("a").getAsLong());
		Assert.assertEquals(true, slice.get("b").getAsBoolean());
		Assert.assertEquals("xyz", slice.get("c").getAsString());
	}

	@Test
	public void objectInObject() throws VPackException {
		// {"a":{"a1":1,"a2":2},"b":{"b1":1,"b2":1}}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		{
			builder.add("a", new Value(ValueType.OBJECT));
			builder.add("a1", new Value(1));
			builder.add("a2", new Value(2));
			builder.close();
		}
		{
			builder.add("b", new Value(ValueType.OBJECT));
			builder.add("b1", new Value(1));
			builder.add("b2", new Value(2));
			builder.close();
		}
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(2, slice.getLength());
		{
			final VPackSlice a = slice.get("a");
			Assert.assertTrue(a.isObject());
			Assert.assertEquals(2, a.getLength());
			Assert.assertEquals(1, a.get("a1").getAsLong());
			Assert.assertEquals(2, a.get("a2").getAsLong());
		}
		{
			final VPackSlice b = slice.get("b");
			Assert.assertTrue(b.isObject());
			Assert.assertEquals(2, b.getLength());
			Assert.assertEquals(1, b.get("b1").getAsLong());
			Assert.assertEquals(2, b.get("b2").getAsLong());
		}
	}

	@Test
	public void objectInObjectInObject() throws VPackException {
		// {"a":{"b":{"c":{"d":true}}}
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.OBJECT));
		builder.add("b", new Value(ValueType.OBJECT));
		builder.add("c", new Value(ValueType.OBJECT));
		builder.add("d", new Value(true));
		builder.close();
		builder.close();
		builder.close();
		builder.close();

		final VPackSlice slice = builder.slice();
		Assert.assertTrue(slice.isObject());
		Assert.assertEquals(1, slice.getLength());
		final VPackSlice a = slice.get("a");
		Assert.assertTrue(a.isObject());
		Assert.assertEquals(1, a.getLength());
		final VPackSlice b = a.get("b");
		Assert.assertTrue(b.isObject());
		Assert.assertEquals(1, b.getLength());
		final VPackSlice c = b.get("c");
		Assert.assertTrue(c.isObject());
		Assert.assertEquals(1, c.getLength());
		final VPackSlice d = c.get("d");
		Assert.assertTrue(d.isBoolean());
		Assert.assertTrue(d.isTrue());
	}

	@Test
	public void objectAttributeNotFound() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("a"));
		builder.close();
		final VPackSlice vpack = builder.slice();
		Assert.assertTrue(vpack.isObject());
		final VPackSlice b = vpack.get("b");
		Assert.assertTrue(b.isNone());
	}

	@Test
	public void object1ByteOffset() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		final int size = 5;
		for (int i = 0; i < size; i++) {
			builder.add(String.valueOf(i), new Value(ValueType.OBJECT));
			for (int j = 0; j < size; j++) {
				builder.add(String.valueOf(j), new Value("test"));
			}
			builder.close();
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(size, vpack.getLength());
		for (int i = 0; i < size; i++) {
			final VPackSlice attr = vpack.get(String.valueOf(i));
			Assert.assertTrue(attr.isObject());
			for (int j = 0; j < size; j++) {
				final VPackSlice childAttr = attr.get(String.valueOf(j));
				Assert.assertTrue(childAttr.isString());
			}
		}
	}

	@Test
	public void object2ByteOffset() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		final int size = 10;
		for (int i = 0; i < size; i++) {
			builder.add(String.valueOf(i), new Value(ValueType.OBJECT));
			for (int j = 0; j < size; j++) {
				builder.add(String.valueOf(j), new Value("test"));
			}
			builder.close();
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(size, vpack.getLength());
		for (int i = 0; i < size; i++) {
			final VPackSlice attr = vpack.get(String.valueOf(i));
			Assert.assertTrue(attr.isObject());
			for (int j = 0; j < size; j++) {
				final VPackSlice childAttr = attr.get(String.valueOf(j));
				Assert.assertTrue(childAttr.isString());
			}
		}
	}

	@Test
	public void sortObjectAttr() throws VPackException {
		final int min = 0;
		final int max = 9;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		for (int i = max; i >= min; i--) {
			builder.add(String.valueOf(i), new Value("test"));
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(max - min + 1, vpack.getLength());
		for (int i = min, j = 0; i <= max; i++, j++) {
			Assert.assertEquals(String.valueOf(i), vpack.keyAt(j).getAsString());
		}
	}

	@Test
	public void sortObjectAttr2() throws VPackException {
		final String[] keys = { "a", "b", "c", "d", "e", "f", "g", "h" };
		final String[] keysUnsorted = { "b", "d", "c", "e", "g", "f", "h", "a" };
		Assert.assertEquals(keys.length, keysUnsorted.length);
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		for (int i = 0; i < keysUnsorted.length; i++) {
			builder.add(String.valueOf(keysUnsorted[i]), new Value("test"));
		}
		builder.close();
		final VPackSlice vpack = builder.slice();
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(keys.length, vpack.getLength());
		for (int i = 0; i < keys.length; i++) {
			Assert.assertEquals(String.valueOf(keys[i]), vpack.keyAt(i).getAsString());
		}
	}

	@Test
	public void attributeAdapterDefaults() throws VPackException {
		final VPackSlice vpackWithAttrAdapter;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(new Value(ValueType.OBJECT));
			builder.add("_key", new Value("a"));
			builder.close();
			vpackWithAttrAdapter = builder.slice();
			Assert.assertTrue(vpackWithAttrAdapter.isObject());
		}
		final VPackSlice vpackWithoutAttrAdapter;
		{
			final VPackBuilder builder = new VPackBuilder();
			builder.add(new Value(ValueType.OBJECT));
			builder.add("_kay", new Value("a"));
			builder.close();
			vpackWithoutAttrAdapter = builder.slice();
			Assert.assertTrue(vpackWithoutAttrAdapter.isObject());
		}
		Assert.assertTrue(vpackWithAttrAdapter.getByteSize() < vpackWithoutAttrAdapter.getByteSize());
	}

	@Test(expected = VPackBuilderNeedOpenCompoundException.class)
	public void closeClosed() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.close();
		builder.close();
	}

	@Test
	public void addBinary() throws VPackException {
		final byte[] expected = new byte[] { 49, 50, 51, 52, 53, 54, 55, 56, 57 };
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(expected));
		final VPackSlice slice = builder.slice();

		Assert.assertTrue(slice.isBinary());
		Assert.assertEquals(expected.length, slice.getBinaryLength());
		Assert.assertArrayEquals(expected, slice.getAsBinary());
		Assert.assertEquals(1 + 4 + expected.length, slice.getByteSize());
	}
}

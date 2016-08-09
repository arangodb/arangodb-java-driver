package com.arangodb.velocypack;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackSerializeDeserializeTest {

	@Test
	public void defaultOptions() {
		Assert.assertNotNull(new VPack().getOptions());
	}

	protected static class TestEntityBoolean {
		private boolean a = true;
		private boolean b = false;
		private Boolean c = Boolean.TRUE;
		private Boolean d = Boolean.FALSE;

		public boolean isA() {
			return a;
		}

		public void setA(final boolean a) {
			this.a = a;
		}

		public boolean isB() {
			return b;
		}

		public void setB(final boolean b) {
			this.b = b;
		}

		public Boolean getC() {
			return c;
		}

		public void setC(final Boolean c) {
			this.c = c;
		}

		public Boolean getD() {
			return d;
		}

		public void setD(final Boolean d) {
			this.d = d;
		}
	}

	@Test
	public void fromBoolean() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityBoolean());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice a = vpack.get("a");
			Assert.assertTrue(a.isBoolean());
			Assert.assertTrue(a.getAsBoolean());
		}
		{
			final VPackSlice b = vpack.get("b");
			Assert.assertTrue(b.isBoolean());
			Assert.assertFalse(b.getAsBoolean());
		}
		{
			final VPackSlice c = vpack.get("c");
			Assert.assertTrue(c.isBoolean());
			Assert.assertTrue(c.getAsBoolean());
		}
		{
			final VPackSlice d = vpack.get("d");
			Assert.assertTrue(d.isBoolean());
			Assert.assertFalse(d.getAsBoolean());
		}
	}

	@Test
	public void toBoolean() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("a", new Value(false));
			builder.add("b", new Value(true));
			builder.add("c", new Value(Boolean.FALSE));
			builder.add("d", new Value(Boolean.TRUE));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityBoolean entity = new VPack().deserialize(vpack, TestEntityBoolean.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(false, entity.a);
		Assert.assertEquals(true, entity.b);
		Assert.assertEquals(Boolean.FALSE, entity.c);
		Assert.assertEquals(Boolean.TRUE, entity.d);
	}

	protected static class TestEntityString {
		private String s = "test";
		private Character c1 = 't';
		private char c2 = 't';

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}

		public Character getC1() {
			return c1;
		}

		public void setC1(final Character c1) {
			this.c1 = c1;
		}

		public char getC2() {
			return c2;
		}

		public void setC2(final char c2) {
			this.c2 = c2;
		}
	}

	@Test
	public void fromStrings() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityString());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice s = vpack.get("s");
			Assert.assertTrue(s.isString());
			Assert.assertEquals("test", s.getAsString());
		}
		{
			final VPackSlice c1 = vpack.get("c1");
			Assert.assertTrue(c1.isString());
			Assert.assertEquals('t', c1.getAsChar());
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			Assert.assertTrue(c2.isString());
			Assert.assertEquals('t', c2.getAsChar());
		}
	}

	@Test
	public void toStrings() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("s", new Value("abc"));
			builder.add("c1", new Value('d'));
			builder.add("c2", new Value('d'));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityString entity = new VPack().deserialize(vpack, TestEntityString.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals("abc", entity.s);
		Assert.assertEquals(new Character('d'), entity.c1);
		Assert.assertEquals(new Character('d'), entity.c1);
	}

	protected static class TestEntityInteger {
		private int i1 = 1;
		private Integer i2 = 1;

		public int getI1() {
			return i1;
		}

		public void setI1(final int i1) {
			this.i1 = i1;
		}

		public Integer getI2() {
			return i2;
		}

		public void setI2(final Integer i2) {
			this.i2 = i2;
		}
	}

	@Test
	public void fromInteger() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityInteger());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice i1 = vpack.get("i1");
			Assert.assertTrue(i1.isInteger());
			Assert.assertEquals(1, i1.getAsInt());
		}
		{
			final VPackSlice i2 = vpack.get("i2");
			Assert.assertTrue(i2.isInteger());
			Assert.assertEquals(1, i2.getAsInt());
		}
	}

	@Test
	public void toInteger() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("i1", new Value(2));
			builder.add("i2", new Value(3));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityInteger entity = new VPack().deserialize(vpack, TestEntityInteger.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.i1);
		Assert.assertEquals(new Integer(3), entity.i2);
	}

	protected static class TestEntityLong {
		private long l1 = 1;
		private Long l2 = 1L;

		public long getL1() {
			return l1;
		}

		public void setL1(final long l1) {
			this.l1 = l1;
		}

		public Long getL2() {
			return l2;
		}

		public void setL2(final Long l2) {
			this.l2 = l2;
		}
	}

	@Test
	public void fromLong() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityLong());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice l1 = vpack.get("l1");
			Assert.assertTrue(l1.isInteger());
			Assert.assertEquals(1, l1.getAsLong());
		}
		{
			final VPackSlice l2 = vpack.get("l2");
			Assert.assertTrue(l2.isInteger());
			Assert.assertEquals(1, l2.getAsLong());
		}
	}

	@Test
	public void toLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("l1", new Value(2));
			builder.add("l2", new Value(3));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack().deserialize(vpack, TestEntityLong.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.l1);
		Assert.assertEquals(new Long(3), entity.l2);
	}

	protected static class TestEntityFloat {
		private float f1 = 1;
		private Float f2 = 1F;

		public float getF1() {
			return f1;
		}

		public void setF1(final float f1) {
			this.f1 = f1;
		}

		public Float getF2() {
			return f2;
		}

		public void setF2(final Float f2) {
			this.f2 = f2;
		}
	}

	@Test
	public void fromFloat() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityFloat());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice f1 = vpack.get("f1");
			Assert.assertTrue(f1.isDouble());
			Assert.assertEquals(1.0, f1.getAsFloat(), 0.);
		}
		{
			final VPackSlice f2 = vpack.get("f2");
			Assert.assertTrue(f2.isDouble());
			Assert.assertEquals(1.0, f2.getAsFloat(), 0.);
		}
	}

	@Test
	public void toFloat() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("f1", new Value(2F));
			builder.add("f2", new Value(3F));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityFloat entity = new VPack().deserialize(vpack, TestEntityFloat.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.f1, 0.);
		Assert.assertEquals(new Float(3), entity.f2);
	}

	protected static class TestEntityShort {
		private short s1 = 1;
		private Short s2 = 1;

		public short getS1() {
			return s1;
		}

		public void setS1(final short s1) {
			this.s1 = s1;
		}

		public Short getS2() {
			return s2;
		}

		public void setS2(final Short s2) {
			this.s2 = s2;
		}
	}

	@Test
	public void fromShort() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityShort());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice s1 = vpack.get("s1");
			Assert.assertTrue(s1.isInteger());
			Assert.assertEquals(1, s1.getAsShort());
		}
		{
			final VPackSlice s2 = vpack.get("s2");
			Assert.assertTrue(s2.isInteger());
			Assert.assertEquals(1, s2.getAsShort());
		}
	}

	@Test
	public void toShort() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("s1", new Value(2));
			builder.add("s2", new Value(3));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityShort entity = new VPack().deserialize(vpack, TestEntityShort.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.s1);
		Assert.assertEquals(new Short((short) 3), entity.s2);
	}

	protected static class TestEntityDouble {
		private Double d1 = 1.5;
		private double d2 = 1.5;

		public Double getD1() {
			return d1;
		}

		public void setD1(final Double d1) {
			this.d1 = d1;
		}

		public double getD2() {
			return d2;
		}

		public void setD2(final double d2) {
			this.d2 = d2;
		}
	}

	@Test
	public void fromDouble() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityDouble());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice d1 = vpack.get("d1");
			Assert.assertTrue(d1.isDouble());
			Assert.assertEquals(1.5, d1.getAsDouble(), 0.);
		}
		{
			final VPackSlice d2 = vpack.get("d2");
			Assert.assertTrue(d2.isDouble());
			Assert.assertEquals(1.5, d2.getAsDouble(), 0.);
		}
	}

	@Test
	public void toDouble() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("d1", new Value(2.25));
			builder.add("d2", new Value(3.75));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityDouble entity = new VPack().deserialize(vpack, TestEntityDouble.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2.25, entity.d1, 0.);
		Assert.assertEquals(3.75, entity.d2, 0.);
	}

	protected static class TestEntityBigNumber {
		private BigInteger bi = BigInteger.valueOf(1L);
		private BigDecimal bd = BigDecimal.valueOf(1.5);

		public BigInteger getBi() {
			return bi;
		}

		public void setBi(final BigInteger bi) {
			this.bi = bi;
		}

		public BigDecimal getBd() {
			return bd;
		}

		public void setBd(final BigDecimal bd) {
			this.bd = bd;
		}
	}

	@Test
	public void fromBigNumbers() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityBigNumber());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice bi = vpack.get("bi");
			Assert.assertTrue(bi.isInteger());
			Assert.assertEquals(BigInteger.valueOf(1L), bi.getAsBigInteger());
		}
		{
			final VPackSlice bd = vpack.get("bd");
			Assert.assertTrue(bd.isDouble());
			Assert.assertEquals(BigDecimal.valueOf(1.5), bd.getAsBigDecimal());
		}
	}

	@Test
	public void toBigNumbers() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("bi", new Value(BigInteger.valueOf(2)));
			builder.add("bd", new Value(BigDecimal.valueOf(3.75)));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityBigNumber entity = new VPack().deserialize(vpack, TestEntityBigNumber.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(BigInteger.valueOf(2), entity.bi);
		Assert.assertEquals(BigDecimal.valueOf(3.75), entity.bd);
	}

	protected static class TestEntityArray {
		private String[] a1 = { "a", "b", "cd" };
		private int[] a2 = { 1, 2, 3, 4, 5 };
		private boolean[] a3 = { true, true, false };
		private TestEnum[] a4 = TestEnum.values();

		public String[] getA1() {
			return a1;
		}

		public void setA1(final String[] a1) {
			this.a1 = a1;
		}

		public int[] getA2() {
			return a2;
		}

		public void setA2(final int[] a2) {
			this.a2 = a2;
		}

		public boolean[] getA3() {
			return a3;
		}

		public void setA3(final boolean[] a3) {
			this.a3 = a3;
		}

		public TestEnum[] getA4() {
			return a4;
		}

		public void setA4(final TestEnum[] a4) {
			this.a4 = a4;
		}

	}

	@Test
	public void fromArray() throws VPackException {
		final TestEntityArray entity = new TestEntityArray();
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice a1 = vpack.get("a1");
			Assert.assertTrue(a1.isArray());
			Assert.assertEquals(entity.a1.length, a1.getLength());
			for (int i = 0; i < a1.getLength(); i++) {
				Assert.assertEquals(entity.a1[i], a1.get(i).getAsString());
			}
		}
		{
			final VPackSlice a2 = vpack.get("a2");
			Assert.assertTrue(a2.isArray());
			Assert.assertEquals(entity.a2.length, a2.getLength());
			for (int i = 0; i < a2.getLength(); i++) {
				Assert.assertEquals(entity.a2[i], a2.get(i).getAsInt());
			}
		}
		{
			final VPackSlice a3 = vpack.get("a3");
			Assert.assertTrue(a3.isArray());
			Assert.assertEquals(entity.a3.length, a3.getLength());
			for (int i = 0; i < a3.getLength(); i++) {
				Assert.assertEquals(entity.a3[i], a3.get(i).getAsBoolean());
			}
		}
		{
			final VPackSlice a4 = vpack.get("a4");
			Assert.assertTrue(a4.isArray());
			Assert.assertEquals(entity.a4.length, a4.getLength());
			for (int i = 0; i < a4.getLength(); i++) {
				Assert.assertEquals(entity.a4[i], TestEnum.valueOf(a4.get(i).getAsString()));
			}
		}
	}

	@Test
	public void toArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("a1", new Value(ValueType.ARRAY));
				builder.add(new Value("a"));
				builder.add(new Value("b"));
				builder.add(new Value("c"));
				builder.close();
			}
			{
				builder.add("a2", new Value(ValueType.ARRAY));
				builder.add(new Value(1));
				builder.add(new Value(2));
				builder.add(new Value(3));
				builder.add(new Value(4));
				builder.close();
			}
			{
				builder.add("a3", new Value(ValueType.ARRAY));
				builder.add(new Value(false));
				builder.add(new Value(true));
				builder.close();
			}
			{
				builder.add("a4", new Value(ValueType.ARRAY));
				builder.add(new Value(TestEnum.A.name()));
				builder.add(new Value(TestEnum.B.name()));
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArray entity = new VPack().deserialize(vpack, TestEntityArray.class);
		Assert.assertNotNull(entity);
		{
			Assert.assertEquals(3, entity.a1.length);
			Assert.assertEquals("a", entity.a1[0]);
			Assert.assertEquals("b", entity.a1[1]);
			Assert.assertEquals("c", entity.a1[2]);
		}
		{
			Assert.assertEquals(4, entity.a2.length);
			Assert.assertEquals(1, entity.a2[0]);
			Assert.assertEquals(2, entity.a2[1]);
			Assert.assertEquals(3, entity.a2[2]);
			Assert.assertEquals(4, entity.a2[3]);
		}
		{
			Assert.assertEquals(2, entity.a3.length);
			Assert.assertEquals(false, entity.a3[0]);
			Assert.assertEquals(true, entity.a3[1]);
		}
		{
			Assert.assertEquals(2, entity.a4.length);
			Assert.assertEquals(TestEnum.A, entity.a4[0]);
			Assert.assertEquals(TestEnum.B, entity.a4[1]);
		}
	}

	protected enum TestEnum {
		A, B, C
	}

	protected static class TestEntityEnum {
		private TestEnum e1 = TestEnum.A;

		public TestEnum getE1() {
			return e1;
		}

		public void setE1(final TestEnum e1) {
			this.e1 = e1;
		}
	}

	@Test
	public void fromEnum() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityEnum());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice e1 = vpack.get("e1");
			Assert.assertTrue(e1.isString());
			Assert.assertEquals(TestEnum.A, TestEnum.valueOf(e1.getAsString()));
		}
	}

	@Test
	public void toEnum() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("e1", new Value(TestEnum.B.name()));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityEnum entity = new VPack().deserialize(vpack, TestEntityEnum.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(TestEnum.B, entity.e1);
	}

	protected static class TestEntityObject {
		private TestEntityLong o1 = new TestEntityLong();
		private TestEntityArray o2 = new TestEntityArray();

		public TestEntityLong getO1() {
			return o1;
		}

		public void setO1(final TestEntityLong o1) {
			this.o1 = o1;
		}

		public TestEntityArray getO2() {
			return o2;
		}

		public void setO2(final TestEntityArray o2) {
			this.o2 = o2;
		}
	}

	@Test
	public void fromObject() throws VPackException {
		final TestEntityObject entity = new TestEntityObject();
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice o1 = vpack.get("o1");
			Assert.assertTrue(o1.isObject());
			{
				final VPackSlice l1 = o1.get("l1");
				Assert.assertTrue(l1.isInteger());
				Assert.assertEquals(1, l1.getAsLong());
			}
			{
				final VPackSlice l2 = o1.get("l2");
				Assert.assertTrue(l2.isInteger());
				Assert.assertEquals(1, l2.getAsLong());
			}
		}
		{
			final VPackSlice o2 = vpack.get("o2");
			Assert.assertTrue(o2.isObject());
			{
				final VPackSlice a1 = o2.get("a1");
				Assert.assertTrue(a1.isArray());
				Assert.assertEquals(entity.o2.a1.length, a1.getLength());
				for (int i = 0; i < a1.getLength(); i++) {
					Assert.assertEquals(entity.o2.a1[i], a1.get(i).getAsString());
				}
			}
			{
				final VPackSlice a2 = o2.get("a2");
				Assert.assertTrue(a2.isArray());
				Assert.assertEquals(entity.o2.a2.length, a2.getLength());
				for (int i = 0; i < a2.getLength(); i++) {
					Assert.assertEquals(entity.o2.a2[i], a2.get(i).getAsInt());
				}
			}
			{
				final VPackSlice a3 = o2.get("a3");
				Assert.assertTrue(a3.isArray());
				Assert.assertEquals(entity.o2.a3.length, a3.getLength());
				for (int i = 0; i < a3.getLength(); i++) {
					Assert.assertEquals(entity.o2.a3[i], a3.get(i).getAsBoolean());
				}
			}
			{
				final VPackSlice a4 = o2.get("a4");
				Assert.assertTrue(a4.isArray());
				Assert.assertEquals(entity.o2.a4.length, a4.getLength());
				for (int i = 0; i < a4.getLength(); i++) {
					Assert.assertEquals(entity.o2.a4[i], TestEnum.valueOf(a4.get(i).getAsString()));
				}
			}
		}
	}

	@Test
	public void toObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("o1", new Value(ValueType.OBJECT));
				builder.add("l1", new Value(5L));
				builder.add("l2", new Value(5L));
				builder.close();
			}
			{
				builder.add("o2", new Value(ValueType.OBJECT));
				{
					builder.add("a1", new Value(ValueType.ARRAY));
					builder.add(new Value("a"));
					builder.add(new Value("b"));
					builder.add(new Value("c"));
					builder.close();
				}
				{
					builder.add("a2", new Value(ValueType.ARRAY));
					builder.add(new Value(1));
					builder.add(new Value(2));
					builder.add(new Value(3));
					builder.add(new Value(4));
					builder.close();
				}
				{
					builder.add("a3", new Value(ValueType.ARRAY));
					builder.add(new Value(false));
					builder.add(new Value(true));
					builder.close();
				}
				{
					builder.add("a4", new Value(ValueType.ARRAY));
					builder.add(new Value(TestEnum.A.name()));
					builder.add(new Value(TestEnum.B.name()));
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityObject entity = new VPack().deserialize(vpack, TestEntityObject.class);
		Assert.assertNotNull(entity);
		{
			Assert.assertEquals(5, entity.o1.l1);
			Assert.assertEquals(new Long(5), entity.o1.l2);
		}
		{
			Assert.assertEquals(3, entity.o2.a1.length);
			Assert.assertEquals("a", entity.o2.a1[0]);
			Assert.assertEquals("b", entity.o2.a1[1]);
			Assert.assertEquals("c", entity.o2.a1[2]);
		}
		{
			Assert.assertEquals(4, entity.o2.a2.length);
			Assert.assertEquals(1, entity.o2.a2[0]);
			Assert.assertEquals(2, entity.o2.a2[1]);
			Assert.assertEquals(3, entity.o2.a2[2]);
			Assert.assertEquals(4, entity.o2.a2[3]);
		}
		{
			Assert.assertEquals(2, entity.o2.a3.length);
			Assert.assertEquals(false, entity.o2.a3[0]);
			Assert.assertEquals(true, entity.o2.a3[1]);
		}
		{
			Assert.assertEquals(2, entity.o2.a4.length);
			Assert.assertEquals(TestEnum.A, entity.o2.a4[0]);
			Assert.assertEquals(TestEnum.B, entity.o2.a4[1]);
		}
	}

	protected static class TestEntityArrayInArray {
		private long[][] a1;

		public long[][] getA1() {
			return a1;
		}

		public void setA1(final long[][] a1) {
			this.a1 = a1;
		}
	}

	@Test
	public void fromArrayInArray() throws VPackException {
		final TestEntityArrayInArray entity = new TestEntityArrayInArray();
		entity.a1 = new long[][] { { 1, 2, 3 }, { 4, 5, 6 } };
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice a1 = vpack.get("a1");
			Assert.assertTrue(a1.isArray());
			Assert.assertEquals(entity.a1.length, a1.getLength());
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				Assert.assertTrue(at.isArray());
				Assert.assertEquals(entity.a1[i].length, at.getLength());
				for (int j = 0; j < at.getLength(); j++) {
					final VPackSlice atat = at.get(j);
					Assert.assertTrue(atat.isInteger());
					Assert.assertEquals(entity.a1[i][j], atat.getAsLong());
				}
			}
		}
	}

	@Test
	public void toArrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("a1", new Value(ValueType.ARRAY));
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(1));
					builder.add(new Value(2));
					builder.add(new Value(3));
					builder.close();
				}
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(4));
					builder.add(new Value(5));
					builder.add(new Value(6));
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArrayInArray entity = new VPack().deserialize(vpack, TestEntityArrayInArray.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.a1.length);
		{
			Assert.assertEquals(3, entity.a1[0].length);
			Assert.assertEquals(1, entity.a1[0][0]);
			Assert.assertEquals(2, entity.a1[0][1]);
			Assert.assertEquals(3, entity.a1[0][2]);
		}
		{
			Assert.assertEquals(3, entity.a1[1].length);
			Assert.assertEquals(4, entity.a1[1][0]);
			Assert.assertEquals(5, entity.a1[1][1]);
			Assert.assertEquals(6, entity.a1[1][2]);
		}
	}

	protected static class TestEntityArrayInArrayInArray {
		private double[][][] a1;

		public double[][][] getA1() {
			return a1;
		}

		public void setA1(final double[][][] a1) {
			this.a1 = a1;
		}

	}

	@Test
	public void fromArrayInArrayInArray() throws VPackException {
		final TestEntityArrayInArrayInArray entity = new TestEntityArrayInArrayInArray();
		entity.setA1(new double[][][] { { { 1.5, 2.25 }, { 10.5, 20.25 } }, { { 100.5 }, { 200.25 } } });
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice a1 = vpack.get("a1");
			Assert.assertTrue(a1.isArray());
			Assert.assertEquals(entity.a1.length, a1.getLength());
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				Assert.assertTrue(at.isArray());
				Assert.assertEquals(entity.a1[i].length, at.getLength());
				for (int j = 0; j < at.getLength(); j++) {
					final VPackSlice atat = at.get(j);
					Assert.assertTrue(atat.isArray());
					Assert.assertEquals(entity.a1[i][j].length, atat.getLength());
					for (int k = 0; k < atat.getLength(); k++) {
						final VPackSlice atatat = atat.get(k);
						Assert.assertTrue(atatat.isDouble());
						Assert.assertEquals(entity.a1[i][j][k], atatat.getAsDouble(), 0.);
					}
				}
			}
		}
	}

	@Test
	public void toArrayInArrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("a1", new Value(ValueType.ARRAY));
				builder.add(new Value(ValueType.ARRAY));
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(1.5));
					builder.add(new Value(2.5));
					builder.add(new Value(3.5));
					builder.close();
				}
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(4.5));
					builder.add(new Value(5.5));
					builder.add(new Value(6.5));
					builder.close();
				}
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(7.5));
					builder.add(new Value(8.5));
					builder.add(new Value(9.5));
					builder.close();
				}
				builder.close();
				builder.add(new Value(ValueType.ARRAY));
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(1.5));
					builder.add(new Value(2.5));
					builder.add(new Value(3.5));
					builder.close();
				}
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(4.5));
					builder.add(new Value(5.5));
					builder.add(new Value(6.5));
					builder.close();
				}
				{
					builder.add(new Value(ValueType.ARRAY));
					builder.add(new Value(7.5));
					builder.add(new Value(8.5));
					builder.add(new Value(9.5));
					builder.close();
				}
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArrayInArrayInArray entity = new VPack().deserialize(vpack,
			TestEntityArrayInArrayInArray.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.a1.length);
		{
			Assert.assertEquals(3, entity.a1[0].length);
			Assert.assertEquals(3, entity.a1[0][0].length);
			Assert.assertEquals(1.5, entity.a1[0][0][0], 0.);
			Assert.assertEquals(2.5, entity.a1[0][0][1], 0.);
			Assert.assertEquals(3.5, entity.a1[0][0][2], 0.);
			Assert.assertEquals(3, entity.a1[0][1].length);
			Assert.assertEquals(4.5, entity.a1[0][1][0], 0.);
			Assert.assertEquals(5.5, entity.a1[0][1][1], 0.);
			Assert.assertEquals(6.5, entity.a1[0][1][2], 0.);
			Assert.assertEquals(3, entity.a1[0][2].length);
			Assert.assertEquals(7.5, entity.a1[0][2][0], 0.);
			Assert.assertEquals(8.5, entity.a1[0][2][1], 0.);
			Assert.assertEquals(9.5, entity.a1[0][2][2], 0.);
		}
		{
			Assert.assertEquals(3, entity.a1[1].length);
			Assert.assertEquals(3, entity.a1[1][0].length);
			Assert.assertEquals(1.5, entity.a1[1][0][0], 0.);
			Assert.assertEquals(2.5, entity.a1[1][0][1], 0.);
			Assert.assertEquals(3.5, entity.a1[1][0][2], 0.);
			Assert.assertEquals(3, entity.a1[1][1].length);
			Assert.assertEquals(4.5, entity.a1[1][1][0], 0.);
			Assert.assertEquals(5.5, entity.a1[1][1][1], 0.);
			Assert.assertEquals(6.5, entity.a1[1][1][2], 0.);
			Assert.assertEquals(3, entity.a1[1][2].length);
			Assert.assertEquals(7.5, entity.a1[1][2][0], 0.);
			Assert.assertEquals(8.5, entity.a1[1][2][1], 0.);
			Assert.assertEquals(9.5, entity.a1[1][2][2], 0.);
		}
	}

	protected static class TestEntityObjectInArray {
		private TestEntityString[] a1;

		public TestEntityString[] getA1() {
			return a1;
		}

		public void setA1(final TestEntityString[] a1) {
			this.a1 = a1;
		}
	}

	@Test
	public void fromObjectInArray() throws VPackException {
		final TestEntityObjectInArray entity = new TestEntityObjectInArray();
		{
			final TestEntityString[] a1 = new TestEntityString[2];
			final TestEntityString s = new TestEntityString();
			s.setS("abc");
			a1[0] = s;
			a1[1] = s;
			entity.setA1(a1);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice a1 = vpack.get("a1");
			Assert.assertTrue(a1.isArray());
			Assert.assertEquals(2, a1.getLength());
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				Assert.assertTrue(at.isObject());
				final VPackSlice s = at.get("s");
				Assert.assertTrue(s.isString());
				Assert.assertEquals("abc", s.getAsString());
			}
		}
	}

	@Test
	public void toObjectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("a1", new Value(ValueType.ARRAY));
			{
				builder.add(new Value(ValueType.OBJECT));
				builder.add("s", new Value("abc"));
				builder.close();
			}
			builder.close();
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityObjectInArray entity = new VPack().deserialize(vpack, TestEntityObjectInArray.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.a1);
		Assert.assertEquals(1, entity.a1.length);
		final TestEntityString st = entity.a1[0];
		Assert.assertNotNull(st);
		Assert.assertEquals("abc", st.s);
	}

	protected static class TestEntityA {
		private String a = "a";

		public String getA() {
			return a;
		}

		public void setA(final String a) {
			this.a = a;
		}
	}

	protected static class TestEntityB extends TestEntityA {
		private String b = "b";

		public String getB() {
			return b;
		}

		public void setB(final String b) {
			this.b = b;
		}
	}

	@Test
	public void fromInheritance() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityB());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(2, vpack.getLength());
		{
			final VPackSlice a = vpack.get("a");
			Assert.assertTrue(a.isString());
			Assert.assertEquals("a", a.getAsString());
		}
		{
			final VPackSlice b = vpack.get("b");
			Assert.assertTrue(b.isString());
			Assert.assertEquals("b", b.getAsString());
		}
	}

	@Test
	public void toInheritance() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("a", new Value("test"));
			builder.add("b", new Value("test"));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		{
			final TestEntityA entity = new VPack().deserialize(vpack, TestEntityA.class);
			Assert.assertNotNull(entity);
			Assert.assertEquals("test", entity.getA());
		}
		{
			final TestEntityB entity = new VPack().deserialize(vpack, TestEntityB.class);
			Assert.assertNotNull(entity);
			Assert.assertEquals("test", entity.getA());
			Assert.assertEquals("test", entity.getB());
		}
	}

	protected static class TestEntityC {
		private TestEntityD d;

		public TestEntityD getD() {
			return d;
		}

		public void setD(final TestEntityD d) {
			this.d = d;
		}
	}

	protected static interface TestEntityD {
		String getD();

		void setD(String d);
	}

	protected static class TestEntityDImpl implements TestEntityD {
		private String d = "d";

		@Override
		public String getD() {
			return d;
		}

		@Override
		public void setD(final String d) {
			this.d = d;
		}
	}

	@Test
	public void fromInterface() throws VPackException {
		final TestEntityC entity = new TestEntityC();
		entity.setD(new TestEntityDImpl());
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice d = vpack.get("d");
			Assert.assertTrue(d.isObject());
			final VPackSlice dd = d.get("d");
			Assert.assertTrue(dd.isString());
			Assert.assertEquals("d", dd.getAsString());
		}
	}

	@Test
	public void toInterface() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("d", new Value(ValueType.OBJECT));
			builder.add("d", new Value("test"));
			builder.close();
			builder.close();
		}
		final VPackSlice slice = builder.slice();
		final VPack vPack = new VPack();
		vPack.regitserInstanceCreator(TestEntityD.class, new VPackInstanceCreator<TestEntityD>() {
			@Override
			public TestEntityD createInstance() {
				return new TestEntityDImpl();
			}
		});
		final TestEntityC entity = vPack.deserialize(slice, TestEntityC.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.d);
		Assert.assertEquals("test", entity.d.getD());
	}

	protected static class TestEntityCollection {
		private Collection<String> c1 = new LinkedList<>();
		private List<String> c2 = new ArrayList<>();
		private ArrayList<String> c3 = new ArrayList<>();
		private Set<String> c4 = new LinkedHashSet<>();
		private HashSet<String> c5 = new HashSet<>();

		public TestEntityCollection() {
			super();
		}

		public Collection<String> getC1() {
			return c1;
		}

		public void setC1(final Collection<String> c1) {
			this.c1 = c1;
		}

		public List<String> getC2() {
			return c2;
		}

		public void setC2(final List<String> c2) {
			this.c2 = c2;
		}

		public ArrayList<String> getC3() {
			return c3;
		}

		public void setC3(final ArrayList<String> c3) {
			this.c3 = c3;
		}

		public Set<String> getC4() {
			return c4;
		}

		public void setC4(final Set<String> c4) {
			this.c4 = c4;
		}

		public HashSet<String> getC5() {
			return c5;
		}

		public void setC5(final HashSet<String> c5) {
			this.c5 = c5;
		}
	}

	@Test
	public void fromCollection() throws VPackException {
		final TestEntityCollection entity = new TestEntityCollection();
		{
			entity.c1.add("test");
			entity.c2.add("test");
			entity.c3.add("test");
			entity.c4.add("test");
			entity.c5.add("test");
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice c1 = vpack.get("c1");
			Assert.assertTrue(c1.isArray());
			Assert.assertEquals(1, c1.getLength());
			Assert.assertEquals("test", c1.get(0).getAsString());
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			Assert.assertTrue(c2.isArray());
			Assert.assertEquals(1, c2.getLength());
			Assert.assertEquals("test", c2.get(0).getAsString());
		}
		{
			final VPackSlice c3 = vpack.get("c3");
			Assert.assertTrue(c3.isArray());
			Assert.assertEquals(1, c3.getLength());
			Assert.assertEquals("test", c3.get(0).getAsString());
		}
		{
			final VPackSlice c4 = vpack.get("c4");
			Assert.assertTrue(c4.isArray());
			Assert.assertEquals(1, c4.getLength());
			Assert.assertEquals("test", c4.get(0).getAsString());
		}
		{
			final VPackSlice c5 = vpack.get("c5");
			Assert.assertTrue(c5.isArray());
			Assert.assertEquals(1, c5.getLength());
			Assert.assertEquals("test", c5.get(0).getAsString());
		}
	}

	@Test
	public void toCollection() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("c1", new Value(ValueType.ARRAY));
				builder.add(new Value("test1"));
				builder.add(new Value("test2"));
				builder.close();
			}
			{
				builder.add("c2", new Value(ValueType.ARRAY));
				builder.add(new Value("test1"));
				builder.add(new Value("test2"));
				builder.close();
			}
			{
				builder.add("c3", new Value(ValueType.ARRAY));
				builder.add(new Value("test1"));
				builder.add(new Value("test2"));
				builder.close();
			}
			{
				builder.add("c4", new Value(ValueType.ARRAY));
				builder.add(new Value("test1"));
				builder.add(new Value("test2"));
				builder.close();
			}
			{
				builder.add("c5", new Value(ValueType.ARRAY));
				builder.add(new Value("test1"));
				builder.add(new Value("test2"));
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityCollection entity = new VPack().deserialize(vpack, TestEntityCollection.class);
		Assert.assertNotNull(entity);
		{
			checkCollection(entity.c1);
			checkCollection(entity.c2);
			checkCollection(entity.c3);
			checkCollection(entity.c4);
			checkCollection(entity.c5);
		}
	}

	private void checkCollection(final Collection<String> col) {
		Assert.assertNotNull(col);
		Assert.assertEquals(2, col.size());
		for (final Iterator<String> iterator = col.iterator(); iterator.hasNext();) {
			final String next = iterator.next();
			Assert.assertTrue("test1".equals(next) || "test2".equals(next));
		}
	}

	protected static class TestEntityCollectionWithObjects {
		private Collection<TestEntityString> c1;
		private Set<TestEntityArray> c2;

		public Collection<TestEntityString> getC1() {
			return c1;
		}

		public void setC1(final Collection<TestEntityString> c1) {
			this.c1 = c1;
		}

		public Set<TestEntityArray> getC2() {
			return c2;
		}

		public void setC2(final Set<TestEntityArray> c2) {
			this.c2 = c2;
		}
	}

	@Test
	public void fromCollectionWithObjects() throws VPackException {
		final TestEntityCollectionWithObjects entity = new TestEntityCollectionWithObjects();
		{
			final Collection<TestEntityString> c1 = new ArrayList<>();
			c1.add(new TestEntityString());
			c1.add(new TestEntityString());
			entity.setC1(c1);
			final Set<TestEntityArray> c2 = new HashSet<>();
			c2.add(new TestEntityArray());
			entity.setC2(c2);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice c1 = vpack.get("c1");
			Assert.assertTrue(c1.isArray());
			Assert.assertEquals(2, c1.getLength());
			Assert.assertTrue(c1.get(0).isObject());
			Assert.assertTrue(c1.get(1).isObject());
			{
				final VPackSlice s = c1.get(0).get("s");
				Assert.assertTrue(s.isString());
				Assert.assertEquals("test", s.getAsString());
			}
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			Assert.assertTrue(c2.isArray());
			Assert.assertEquals(1, c2.getLength());
			Assert.assertTrue(c2.get(0).isObject());
			{
				final VPackSlice a2 = c2.get(0).get("a2");
				Assert.assertTrue(a2.isArray());
				Assert.assertEquals(5, a2.getLength());
				for (int i = 0; i < a2.getLength(); i++) {
					final VPackSlice at = a2.get(i);
					Assert.assertTrue(at.isInteger());
					Assert.assertEquals(i + 1, at.getAsInt());
				}
			}
		}
	}

	@Test
	public void toCollectionWithObjects() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("c1", new Value(ValueType.ARRAY));
				builder.add(new Value(ValueType.OBJECT));
				builder.add("s", new Value("abc"));
				builder.close();
				builder.close();
			}
			{
				builder.add("c2", new Value(ValueType.ARRAY));
				builder.add(new Value(ValueType.OBJECT));
				builder.add("a2", new Value(ValueType.ARRAY));
				for (int i = 0; i < 10; i++) {
					builder.add(new Value(i));
				}
				builder.close();
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityCollectionWithObjects entity = new VPack().deserialize(vpack,
			TestEntityCollectionWithObjects.class);
		Assert.assertNotNull(entity);
		{
			Assert.assertNotNull(entity.c1);
			Assert.assertEquals(1, entity.c1.size());
			Assert.assertEquals("abc", entity.c1.iterator().next().s);
		}
		{
			Assert.assertNotNull(entity.c2);
			Assert.assertEquals(1, entity.c2.size());
			final int[] array = entity.c2.iterator().next().a2;
			for (int i = 0; i < array.length; i++) {
				Assert.assertEquals(i, array[i]);
			}
		}
	}

	protected static class TestEntityMap {
		private Map<String, String> m1;
		private HashMap<Integer, String> m2;
		private Map<String, TestEntityString> m3;

		public Map<String, String> getM1() {
			return m1;
		}

		public void setM1(final Map<String, String> m1) {
			this.m1 = m1;
		}

		public HashMap<Integer, String> getM2() {
			return m2;
		}

		public void setM2(final HashMap<Integer, String> m2) {
			this.m2 = m2;
		}

		public Map<String, TestEntityString> getM3() {
			return m3;
		}

		public void setM3(final Map<String, TestEntityString> m3) {
			this.m3 = m3;
		}
	}

	@Test
	public void fromMap() throws VPackException {
		final TestEntityMap entity = new TestEntityMap();
		{
			final Map<String, String> m1 = new LinkedHashMap<>();
			m1.put("a", "b");
			m1.put("c", "d");
			entity.setM1(m1);
			final HashMap<Integer, String> m2 = new HashMap<>();
			m2.put(1, "a");
			m2.put(2, "b");
			entity.setM2(m2);
			final Map<String, TestEntityString> m3 = new HashMap<>();
			final TestEntityString s = new TestEntityString();
			s.setS("abc");
			m3.put("a", s);
			entity.setM3(m3);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice m1 = vpack.get("m1");
			Assert.assertTrue(m1.isObject());
			Assert.assertEquals(2, m1.getLength());
			{
				final VPackSlice a = m1.get("a");
				Assert.assertTrue(a.isString());
				Assert.assertEquals("b", a.getAsString());
			}
			{
				final VPackSlice c = m1.get("c");
				Assert.assertTrue(c.isString());
				Assert.assertEquals("d", c.getAsString());
			}
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			Assert.assertTrue(m2.isObject());
			Assert.assertEquals(2, m2.getLength());
			{
				final VPackSlice one = m2.get("1");
				Assert.assertTrue(one.isString());
				Assert.assertEquals("a", one.getAsString());
			}
			{
				final VPackSlice two = m2.get("2");
				Assert.assertTrue(two.isString());
				Assert.assertEquals("b", two.getAsString());
			}
		}
		{
			final VPackSlice m3 = vpack.get("m3");
			Assert.assertTrue(m3.isObject());
			Assert.assertEquals(1, m3.getLength());
			final VPackSlice a = m3.get("a");
			Assert.assertTrue(a.isObject());
			final VPackSlice s = a.get("s");
			Assert.assertTrue(s.isString());
			Assert.assertEquals("abc", s.getAsString());
		}
	}

	@Test
	public void toMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			{
				builder.add("m1", new Value(ValueType.OBJECT));
				builder.add("a", new Value("a"));
				builder.add("b", new Value("b"));
				builder.close();
			}
			{
				builder.add("m2", new Value(ValueType.OBJECT));
				builder.add("1", new Value("a"));
				builder.add("-1", new Value("a"));
				builder.close();
			}
			{
				builder.add("m3", new Value(ValueType.OBJECT));
				builder.add("a", new Value(ValueType.OBJECT));
				builder.add("s", new Value("abc"));
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityMap entity = new VPack().deserialize(vpack, TestEntityMap.class);
		Assert.assertNotNull(entity);
		{
			Assert.assertNotNull(entity.m1);
			Assert.assertEquals(2, entity.m1.size());
			final String a = entity.m1.get("a");
			Assert.assertNotNull(a);
			Assert.assertEquals("a", a);
			final String b = entity.m1.get("b");
			Assert.assertNotNull(b);
			Assert.assertEquals("b", b);
		}
		{
			Assert.assertNotNull(entity.m2);
			Assert.assertEquals(2, entity.m2.size());
			final String one = entity.m2.get(1);
			Assert.assertNotNull(one);
			Assert.assertEquals("a", one);
			final String oneNegative = entity.m2.get(-1);
			Assert.assertNotNull(oneNegative);
			Assert.assertEquals("a", oneNegative);
		}
		{
			Assert.assertNotNull(entity.m3);
			Assert.assertEquals(1, entity.m3.size());
			final TestEntityString a = entity.m3.get("a");
			Assert.assertNotNull(a);
			Assert.assertEquals("abc", a.s);
		}
	}

	protected static class TestEntityMapStringableKey {
		private Map<Boolean, String> m1;
		private Map<Integer, String> m2;
		private Map<Long, String> m3;
		private Map<Float, String> m4;
		private Map<Short, String> m5;
		private Map<Double, String> m6;
		private Map<Number, String> m7;
		private Map<BigInteger, String> m8;
		private Map<BigDecimal, String> m9;
		private Map<Character, String> m10;
		private Map<TestEnum, String> m11;

		public Map<Boolean, String> getM1() {
			return m1;
		}

		public void setM1(final Map<Boolean, String> m1) {
			this.m1 = m1;
		}

		public Map<Integer, String> getM2() {
			return m2;
		}

		public void setM2(final Map<Integer, String> m2) {
			this.m2 = m2;
		}

		public Map<Long, String> getM3() {
			return m3;
		}

		public void setM3(final Map<Long, String> m3) {
			this.m3 = m3;
		}

		public Map<Float, String> getM4() {
			return m4;
		}

		public void setM4(final Map<Float, String> m4) {
			this.m4 = m4;
		}

		public Map<Short, String> getM5() {
			return m5;
		}

		public void setM5(final Map<Short, String> m5) {
			this.m5 = m5;
		}

		public Map<Double, String> getM6() {
			return m6;
		}

		public void setM6(final Map<Double, String> m6) {
			this.m6 = m6;
		}

		public Map<Number, String> getM7() {
			return m7;
		}

		public void setM7(final Map<Number, String> m7) {
			this.m7 = m7;
		}

		public Map<BigInteger, String> getM8() {
			return m8;
		}

		public void setM8(final Map<BigInteger, String> m8) {
			this.m8 = m8;
		}

		public Map<BigDecimal, String> getM9() {
			return m9;
		}

		public void setM9(final Map<BigDecimal, String> m9) {
			this.m9 = m9;
		}

		public Map<Character, String> getM10() {
			return m10;
		}

		public void setM10(final Map<Character, String> m10) {
			this.m10 = m10;
		}

		public Map<TestEnum, String> getM11() {
			return m11;
		}

		public void setM11(final Map<TestEnum, String> m11) {
			this.m11 = m11;
		}

	}

	@Test
	public void fromMapStringableKey() throws VPackException {
		final TestEntityMapStringableKey entity = new TestEntityMapStringableKey();
		final String value = "test";
		{
			final Map<Boolean, String> m1 = new HashMap<>();
			m1.put(true, value);
			m1.put(false, value);
			entity.setM1(m1);
		}
		{
			final Map<Integer, String> m2 = new HashMap<>();
			m2.put(1, value);
			m2.put(2, value);
			entity.setM2(m2);
		}
		{
			final Map<Long, String> m3 = new HashMap<>();
			m3.put(1L, value);
			m3.put(2L, value);
			entity.setM3(m3);
		}
		{
			final Map<Float, String> m4 = new HashMap<>();
			m4.put(1.5F, value);
			m4.put(2.25F, value);
			entity.setM4(m4);
		}
		{
			final Map<Short, String> m5 = new HashMap<>();
			m5.put(new Short("1"), value);
			m5.put(new Short("2"), value);
			entity.setM5(m5);
		}
		{
			final Map<Double, String> m6 = new HashMap<>();
			m6.put(1.5, value);
			m6.put(2.25, value);
			entity.setM6(m6);
		}
		{
			final Map<Number, String> m7 = new HashMap<>();
			m7.put(1.5, value);
			m7.put(1L, value);
			entity.setM7(m7);
		}
		{
			final Map<BigInteger, String> m8 = new HashMap<>();
			m8.put(new BigInteger("1"), value);
			m8.put(new BigInteger("2"), value);
			entity.setM8(m8);
		}
		{
			final Map<BigDecimal, String> m9 = new HashMap<>();
			m9.put(new BigDecimal("1.5"), value);
			m9.put(new BigDecimal("2.25"), value);
			entity.setM9(m9);
		}
		{
			final Map<Character, String> m10 = new HashMap<>();
			m10.put('1', value);
			m10.put('a', value);
			entity.setM10(m10);
		}
		{
			final Map<TestEnum, String> m11 = new HashMap<>();
			m11.put(TestEnum.A, value);
			m11.put(TestEnum.B, value);
			entity.setM11(m11);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice m1 = vpack.get("m1");
			Assert.assertTrue(m1.isObject());
			Assert.assertEquals(2, m1.getLength());
			checkMapAttribute(m1.get("true"));
			checkMapAttribute(m1.get("false"));
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			Assert.assertTrue(m2.isObject());
			Assert.assertEquals(2, m2.getLength());
			checkMapAttribute(m2.get("1"));
			checkMapAttribute(m2.get("2"));
		}
		{
			final VPackSlice m3 = vpack.get("m3");
			Assert.assertTrue(m3.isObject());
			Assert.assertEquals(2, m3.getLength());
			checkMapAttribute(m3.get("1"));
			checkMapAttribute(m3.get("2"));
		}
		{
			final VPackSlice m4 = vpack.get("m4");
			Assert.assertTrue(m4.isObject());
			Assert.assertEquals(2, m4.getLength());
			checkMapAttribute(m4.get("1.5"));
			checkMapAttribute(m4.get("2.25"));
		}
		{
			final VPackSlice m5 = vpack.get("m5");
			Assert.assertTrue(m5.isObject());
			Assert.assertEquals(2, m5.getLength());
			checkMapAttribute(m5.get("1"));
			checkMapAttribute(m5.get("2"));
		}
		{
			final VPackSlice m6 = vpack.get("m6");
			Assert.assertTrue(m6.isObject());
			Assert.assertEquals(2, m6.getLength());
			checkMapAttribute(m6.get("1.5"));
			checkMapAttribute(m6.get("2.25"));
		}
		{
			final VPackSlice m7 = vpack.get("m7");
			Assert.assertTrue(m7.isObject());
			Assert.assertEquals(2, m7.getLength());
			checkMapAttribute(m7.get("1.5"));
			checkMapAttribute(m7.get("1"));
		}
		{
			final VPackSlice m8 = vpack.get("m8");
			Assert.assertTrue(m8.isObject());
			Assert.assertEquals(2, m8.getLength());
			checkMapAttribute(m8.get("1"));
			checkMapAttribute(m8.get("2"));
		}
		{
			final VPackSlice m9 = vpack.get("m9");
			Assert.assertTrue(m9.isObject());
			Assert.assertEquals(2, m9.getLength());
			checkMapAttribute(m9.get("1.5"));
			checkMapAttribute(m9.get("2.25"));
		}
		{
			final VPackSlice m10 = vpack.get("m10");
			Assert.assertTrue(m10.isObject());
			Assert.assertEquals(2, m10.getLength());
			checkMapAttribute(m10.get("1"));
			checkMapAttribute(m10.get("a"));
		}
		{
			final VPackSlice m11 = vpack.get("m11");
			Assert.assertTrue(m11.isObject());
			Assert.assertEquals(2, m11.getLength());
			checkMapAttribute(m11.get(TestEnum.A.name()));
			checkMapAttribute(m11.get(TestEnum.B.name()));
		}
	}

	@Test
	public void toMapSringableKey() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		{
			builder.add("m1", new Value(ValueType.OBJECT));
			builder.add("true", new Value("test"));
			builder.add("false", new Value("test"));
			builder.close();
		}
		{
			builder.add("m2", new Value(ValueType.OBJECT));
			builder.add("1", new Value("test"));
			builder.add("2", new Value("test"));
			builder.close();
		}
		{
			builder.add("m3", new Value(ValueType.OBJECT));
			builder.add("1", new Value("test"));
			builder.add("2", new Value("test"));
			builder.close();
		}
		{
			builder.add("m4", new Value(ValueType.OBJECT));
			builder.add("1.5", new Value("test"));
			builder.add("2.25", new Value("test"));
			builder.close();
		}
		{
			builder.add("m5", new Value(ValueType.OBJECT));
			builder.add("1", new Value("test"));
			builder.add("2", new Value("test"));
			builder.close();
		}
		{
			builder.add("m6", new Value(ValueType.OBJECT));
			builder.add("1.5", new Value("test"));
			builder.add("2.25", new Value("test"));
			builder.close();
		}
		{
			builder.add("m7", new Value(ValueType.OBJECT));
			builder.add("1.5", new Value("test"));
			builder.add("1", new Value("test"));
			builder.close();
		}
		{
			builder.add("m8", new Value(ValueType.OBJECT));
			builder.add("1", new Value("test"));
			builder.add("2", new Value("test"));
			builder.close();
		}
		{
			builder.add("m9", new Value(ValueType.OBJECT));
			builder.add("1.5", new Value("test"));
			builder.add("2.25", new Value("test"));
			builder.close();
		}
		{
			builder.add("m10", new Value(ValueType.OBJECT));
			builder.add("1", new Value("test"));
			builder.add("a", new Value("test"));
			builder.close();
		}
		{
			builder.add("m11", new Value(ValueType.OBJECT));
			builder.add(TestEnum.A.name(), new Value("test"));
			builder.add(TestEnum.B.name(), new Value("test"));
			builder.close();
		}
		builder.close();
		final TestEntityMapStringableKey entity = new VPack().deserialize(builder.slice(),
			TestEntityMapStringableKey.class);
		{
			Assert.assertEquals(2, entity.m1.size());
			checkMapAttribute(entity.m1.get(true));
			checkMapAttribute(entity.m1.get(false));
		}
		{
			Assert.assertEquals(2, entity.m2.size());
			checkMapAttribute(entity.m2.get(1));
			checkMapAttribute(entity.m2.get(2));
		}
		{
			Assert.assertEquals(2, entity.m3.size());
			checkMapAttribute(entity.m3.get(1L));
			checkMapAttribute(entity.m3.get(2L));
		}
		{
			Assert.assertEquals(2, entity.m4.size());
			checkMapAttribute(entity.m4.get(1.5F));
			checkMapAttribute(entity.m4.get(2.25F));
		}
		{
			Assert.assertEquals(2, entity.m5.size());
			checkMapAttribute(entity.m5.get(new Short("1")));
			checkMapAttribute(entity.m5.get(new Short("2")));
		}
		{
			Assert.assertEquals(2, entity.m6.size());
			checkMapAttribute(entity.m6.get(1.5));
			checkMapAttribute(entity.m6.get(2.25));
		}
		{
			Assert.assertEquals(2, entity.m7.size());
			checkMapAttribute(entity.m7.get(new Double(1.5)));
			checkMapAttribute(entity.m7.get(new Double(1L)));
		}
		{
			Assert.assertEquals(2, entity.m8.size());
			checkMapAttribute(entity.m8.get(new BigInteger("1")));
			checkMapAttribute(entity.m8.get(new BigInteger("2")));
		}
		{
			Assert.assertEquals(2, entity.m9.size());
			checkMapAttribute(entity.m9.get(new BigDecimal("1.5")));
			checkMapAttribute(entity.m9.get(new BigDecimal("2.25")));
		}
		{
			Assert.assertEquals(2, entity.m10.size());
			checkMapAttribute(entity.m10.get('1'));
			checkMapAttribute(entity.m10.get('a'));
		}
		{
			Assert.assertEquals(2, entity.m11.size());
			checkMapAttribute(entity.m11.get(TestEnum.A));
			checkMapAttribute(entity.m11.get(TestEnum.B));
		}
	}

	private void checkMapAttribute(final VPackSlice attr) {
		Assert.assertTrue(attr.isString());
		Assert.assertEquals("test", attr.getAsString());
	}

	private void checkMapAttribute(final String attr) {
		Assert.assertEquals("test", attr);
	}

	protected static class TestEntityMapWithObjectKey {
		private Map<TestEntityLong, TestEntityCollection> m1;
		private Map<TestEntityLong, String> m2;

		public Map<TestEntityLong, TestEntityCollection> getM1() {
			return m1;
		}

		public void setM1(final Map<TestEntityLong, TestEntityCollection> m1) {
			this.m1 = m1;
		}

		public Map<TestEntityLong, String> getM2() {
			return m2;
		}

		public void setM2(final Map<TestEntityLong, String> m2) {
			this.m2 = m2;
		}
	}

	@Test
	public void fromMapWithObjectKey() throws VPackException {
		final TestEntityMapWithObjectKey entity = new TestEntityMapWithObjectKey();
		{
			final Map<TestEntityLong, TestEntityCollection> m1 = new HashMap<>();
			m1.put(new TestEntityLong(), new TestEntityCollection());
			m1.put(new TestEntityLong(), new TestEntityCollection());
			entity.setM1(m1);
		}
		{
			final Map<TestEntityLong, String> m2 = new HashMap<>();
			m2.put(new TestEntityLong(), "test");
			m2.put(new TestEntityLong(), "test");
			m2.put(new TestEntityLong(), "test");
			entity.setM2(m2);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice m1 = vpack.get("m1");
			Assert.assertTrue(m1.isArray());
			Assert.assertEquals(2, m1.getLength());
			for (int i = 0; i < m1.getLength(); i++) {
				final VPackSlice at = m1.get(i);
				Assert.assertTrue(at.isObject());
				Assert.assertEquals(2, at.getLength());
				{
					final VPackSlice key = at.get("key");
					Assert.assertTrue(key.isObject());
					final VPackSlice l1 = key.get("l1");
					Assert.assertTrue(l1.isInteger());
					Assert.assertEquals(1, l1.getAsLong());
				}
				{
					final VPackSlice value = at.get("value");
					Assert.assertTrue(value.isObject());
					final VPackSlice c1 = value.get("c1");
					Assert.assertTrue(c1.isArray());
				}
			}
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			Assert.assertTrue(m2.isArray());
			Assert.assertEquals(3, m2.getLength());
			for (int i = 0; i < m2.getLength(); i++) {
				final VPackSlice at = m2.get(i);
				Assert.assertTrue(at.isObject());
				Assert.assertEquals(2, at.getLength());
				{
					final VPackSlice key = at.get("key");
					Assert.assertTrue(key.isObject());
					final VPackSlice l1 = key.get("l1");
					Assert.assertTrue(l1.isInteger());
					Assert.assertEquals(1, l1.getAsLong());
				}
				{
					final VPackSlice value = at.get("value");
					Assert.assertTrue(value.isString());
					Assert.assertEquals("test", value.getAsString());
				}
			}
		}
	}

	@Test
	public void toMapWithObjectKey() throws VPackException {
		final int size = 2;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		{
			builder.add("m1", new Value(ValueType.ARRAY));
			for (int i = 0; i < size; i++) {
				builder.add(new Value(ValueType.OBJECT));
				{
					builder.add("key", new Value(ValueType.OBJECT));
					builder.add("l1", new Value(5L));
					builder.close();
				}
				{
					builder.add("value", new Value(ValueType.OBJECT));
					builder.add("c1", new Value(ValueType.ARRAY));
					builder.add(new Value("test"));
					builder.close();
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		{
			builder.add("m2", new Value(ValueType.ARRAY));
			for (int i = 0; i < size; i++) {
				builder.add(new Value(ValueType.OBJECT));
				{
					builder.add("key", new Value(ValueType.OBJECT));
					builder.add("l1", new Value(5L));
					builder.close();
				}
				{
					builder.add("value", new Value("test"));
				}
				builder.close();
			}
			builder.close();
		}
		builder.close();
		final TestEntityMapWithObjectKey entity = new VPack().deserialize(builder.slice(),
			TestEntityMapWithObjectKey.class);
		Assert.assertNotNull(entity);
		{
			Assert.assertNotNull(entity.m1);
			Assert.assertEquals(size, entity.m1.size());
			for (final Entry<TestEntityLong, TestEntityCollection> entry : entity.m1.entrySet()) {
				Assert.assertEquals(5L, entry.getKey().l1);
				Assert.assertEquals(1, entry.getValue().c1.size());
				Assert.assertEquals("test", entry.getValue().c1.iterator().next());
			}
		}
		{
			Assert.assertNotNull(entity.m2);
			Assert.assertEquals(2, entity.m2.size());
			for (final Entry<TestEntityLong, String> entry : entity.m2.entrySet()) {
				Assert.assertEquals(5L, entry.getKey().l1);
				Assert.assertEquals("test", entry.getValue());
			}
		}
	}

	protected static class TestEntityEmpty {

	}

	@Test
	public void fromEmptyObject() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityEmpty());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(0, vpack.getLength());
	}

	@Test
	public void toEmptyObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.close();
		final TestEntityEmpty entity = new VPack().deserialize(builder.slice(), TestEntityEmpty.class);
		Assert.assertNotNull(entity);
	}

	protected static class TestEntityEmptyMap {
		private Map<String, Object> m;

		public Map<String, Object> getM() {
			return m;
		}

		public void setM(final Map<String, Object> m) {
			this.m = m;
		}
	}

	@Test
	public void fromEmptyMap() throws VPackException {
		final TestEntityEmptyMap entity = new TestEntityEmptyMap();
		entity.setM(new HashMap<String, Object>());
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(1, vpack.getLength());
		final VPackSlice m = vpack.get("m");
		Assert.assertTrue(m.isObject());
		Assert.assertEquals(0, m.getLength());
	}

	@Test
	public void toEmptyMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("m", new Value(ValueType.OBJECT));
		builder.close();
		builder.close();
		final TestEntityEmptyMap entity = new VPack().deserialize(builder.slice(), TestEntityEmptyMap.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.m);
		Assert.assertEquals(0, entity.m.size());
	}

	protected static class TestEntityBaseAttributes {
		private String _key = "test1";
		private String _rev = "test2";
		private String _id = "test3";
		private String _from = "test4";
		private String _to = "test5";

		public String get_key() {
			return _key;
		}

		public void set_key(final String _key) {
			this._key = _key;
		}

		public String get_rev() {
			return _rev;
		}

		public void set_rev(final String _rev) {
			this._rev = _rev;
		}

		public String get_id() {
			return _id;
		}

		public void set_id(final String _id) {
			this._id = _id;
		}

		public String get_from() {
			return _from;
		}

		public void set_from(final String _from) {
			this._from = _from;
		}

		public String get_to() {
			return _to;
		}

		public void set_to(final String _to) {
			this._to = _to;
		}

	}

	@Test
	public void fromObjectWithAttributeAdapter() throws VPackException {
		final VPackSlice vpack = new VPack().serialize(new TestEntityBaseAttributes());
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(5, vpack.getLength());
		{
			final VPackSlice key = vpack.get("_key");
			Assert.assertTrue(key.isString());
			Assert.assertEquals("test1", key.getAsString());
		}
		{
			final VPackSlice rev = vpack.get("_rev");
			Assert.assertTrue(rev.isString());
			Assert.assertEquals("test2", rev.getAsString());
		}
		{
			final VPackSlice id = vpack.get("_id");
			Assert.assertTrue(id.isString());
			Assert.assertEquals("test3", id.getAsString());
		}
		{
			final VPackSlice from = vpack.get("_from");
			Assert.assertTrue(from.isString());
			Assert.assertEquals("test4", from.getAsString());
		}
		{
			final VPackSlice to = vpack.get("_to");
			Assert.assertTrue(to.isString());
			Assert.assertEquals("test5", to.getAsString());
		}
	}

	@Test
	public void toObjectWithAttributeAdapter() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("_key", new Value("a"));
			builder.add("_rev", new Value("b"));
			builder.add("_id", new Value("c"));
			builder.add("_from", new Value("d"));
			builder.add("_to", new Value("e"));
			builder.close();
		}
		final TestEntityBaseAttributes entity = new VPack().deserialize(builder.slice(),
			TestEntityBaseAttributes.class);
		Assert.assertNotNull(entity);
		Assert.assertEquals("a", entity._key);
		Assert.assertEquals("b", entity._rev);
		Assert.assertEquals("c", entity._id);
		Assert.assertEquals("d", entity._from);
		Assert.assertEquals("e", entity._to);
	}

	@Test
	public void fromMapWithAttributeAdapter() throws VPackException {
		final TestEntityMap entity = new TestEntityMap();
		{
			final Map<String, String> m1 = new HashMap<>();
			m1.put("_key", "test1");
			m1.put("_rev", "test2");
			m1.put("_id", "test3");
			m1.put("_from", "test4");
			m1.put("_to", "test5");
			entity.setM1(m1);
		}
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertTrue(vpack.isObject());
		final VPackSlice m1 = vpack.get("m1");
		Assert.assertTrue(m1.isObject());
		Assert.assertEquals(5, m1.getLength());
		{
			final VPackSlice key = m1.get("_key");
			Assert.assertTrue(key.isString());
			Assert.assertEquals("test1", key.getAsString());
		}
		{
			final VPackSlice rev = m1.get("_rev");
			Assert.assertTrue(rev.isString());
			Assert.assertEquals("test2", rev.getAsString());
		}
		{
			final VPackSlice id = m1.get("_id");
			Assert.assertTrue(id.isString());
			Assert.assertEquals("test3", id.getAsString());
		}
		{
			final VPackSlice from = m1.get("_from");
			Assert.assertTrue(from.isString());
			Assert.assertEquals("test4", from.getAsString());
		}
		{
			final VPackSlice to = m1.get("_to");
			Assert.assertTrue(to.isString());
			Assert.assertEquals("test5", to.getAsString());
		}
	}

	@Test
	public void toMapWithAttributeAdapter() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(new Value(ValueType.OBJECT));
			builder.add("m1", new Value(ValueType.OBJECT));
			builder.add("_key", new Value("a"));
			builder.add("_rev", new Value("b"));
			builder.add("_id", new Value("c"));
			builder.add("_from", new Value("d"));
			builder.add("_to", new Value("e"));
			builder.close();
			builder.close();
		}
		final TestEntityMap entity = new VPack().deserialize(builder.slice(), TestEntityMap.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.m1);
		Assert.assertEquals(5, entity.m1.size());
	}

	@Test
	public void customSerializer() throws VPackException {
		final String value = "abc";
		final VPack vp = new VPack();
		vp.registerSerializer(TestEntityString.class, new VPackSerializer<TestEntityString>() {
			@Override
			public void serialize(
				final VPackBuilder builder,
				final String attribute,
				final TestEntityString entity,
				final VPackSerializationContext context) throws VPackException {
				builder.add(attribute, new Value(ValueType.OBJECT));
				builder.add("not-s", new Value(entity.s));
				builder.close();
			}
		});
		final TestEntityString entity = new TestEntityString();
		entity.setS(value);
		final VPackSlice vpack = vp.serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice s = vpack.get("not-s");
			Assert.assertTrue(s.isString());
			Assert.assertEquals(value, s.getAsString());
		}
	}

	@Test
	public void customDeserializer() throws VPackException {
		final String value = "abc";
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("not-s", new Value(value));
		builder.close();
		final VPack vp = new VPack();
		vp.registerDeserializer(TestEntityString.class, new VPackDeserializer<TestEntityString>() {
			@Override
			public TestEntityString deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
					throws VPackException {
				final TestEntityString entity = new TestEntityString();
				final VPackSlice nots = vpack.get("not-s");
				if (nots.isString()) {
					entity.s = nots.getAsString();
				}
				return entity;
			}
		});
		final TestEntityString entity = vp.deserialize(builder.slice(), TestEntityString.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.s);
		Assert.assertEquals(value, entity.s);
	}

	@Test
	public void customSerializerWithContext() throws VPackException {
		final VPack vp = new VPack();
		vp.registerSerializer(TestEntityObject.class, new VPackSerializer<TestEntityObject>() {
			@Override
			public void serialize(
				final VPackBuilder builder,
				final String attribute,
				final TestEntityObject entity,
				final VPackSerializationContext context) throws VPackException {
				builder.add(attribute, new Value(ValueType.OBJECT));
				context.serialize(builder, "test", entity.o1);
				builder.close();
			}
		});
		final TestEntityObject entity = new TestEntityObject();
		final VPackSlice vpack = vp.serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		{
			final VPackSlice test = vpack.get("test");
			Assert.assertTrue(test.isObject());
			final VPackSlice l1 = test.get("l1");
			Assert.assertTrue(l1.isInteger());
			Assert.assertEquals(1, l1.getAsInt());
		}
	}

	@Test
	public void customDeserializerWithContext() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("test", new Value(ValueType.OBJECT));
		builder.add("l1", new Value(5));
		builder.close();
		builder.close();
		final VPack vp = new VPack();
		vp.registerDeserializer(TestEntityObject.class, new VPackDeserializer<TestEntityObject>() {
			@Override
			public TestEntityObject deserialize(final VPackSlice vpack, final VPackDeserializationContext context)
					throws VPackException {
				final TestEntityObject entity = new TestEntityObject();
				entity.o1 = context.deserialize(vpack.get("test"), TestEntityLong.class);
				return entity;
			}
		});
		final TestEntityObject entity = vp.deserialize(builder.slice(), TestEntityObject.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.o1);
		Assert.assertEquals(5, entity.o1.l1);
	}

	protected static class TestEntitySerializeAnnotation {

		@SerializedName("abc")
		private String test = "test";

		public String getTest() {
			return test;
		}

		public void setTest(final String test) {
			this.test = test;
		}

	}

	@Test
	public void fromSerializedName() throws VPackException {
		final TestEntitySerializeAnnotation entity = new TestEntitySerializeAnnotation();
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(1, vpack.getLength());
		final VPackSlice abc = vpack.get("abc");
		Assert.assertTrue(abc.isString());
		Assert.assertEquals("test", abc.getAsString());
	}

	@Test
	public void toSerializedName() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("abc", new Value("test2"));
		builder.close();
		final TestEntitySerializeAnnotation entity = new VPack().deserialize(builder.slice(),
			TestEntitySerializeAnnotation.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.test);
		Assert.assertEquals("test2", entity.test);
	}

	protected static class TestEntityExpose {
		private String a;
		@Expose
		private String b;
		@Expose(serialize = true, deserialize = true)
		private String c;
		@Expose(serialize = true, deserialize = false)
		private String d;
		@Expose(serialize = false, deserialize = true)
		private String e;
		@Expose(serialize = false, deserialize = false)
		private String f;

		public String getA() {
			return a;
		}

		public void setA(final String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(final String b) {
			this.b = b;
		}

		public String getC() {
			return c;
		}

		public void setC(final String c) {
			this.c = c;
		}

		public String getD() {
			return d;
		}

		public void setD(final String d) {
			this.d = d;
		}

		public String getE() {
			return e;
		}

		public void setE(final String e) {
			this.e = e;
		}

		public String getF() {
			return f;
		}

		public void setF(final String f) {
			this.f = f;
		}
	}

	@Test
	public void fromExpose() throws VPackException {
		final TestEntityExpose entity = new TestEntityExpose();
		entity.a = entity.b = entity.c = entity.d = entity.e = entity.f = "test";
		final VPackSlice vpack = new VPack().serialize(entity);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(4, vpack.getLength());
		{
			final VPackSlice a = vpack.get("a");
			Assert.assertTrue(a.isString());
			Assert.assertEquals("test", a.getAsString());
		}
		{
			final VPackSlice b = vpack.get("b");
			Assert.assertTrue(b.isString());
			Assert.assertEquals("test", b.getAsString());
		}
		{
			final VPackSlice c = vpack.get("c");
			Assert.assertTrue(c.isString());
			Assert.assertEquals("test", c.getAsString());
		}
		{
			final VPackSlice d = vpack.get("d");
			Assert.assertTrue(d.isString());
			Assert.assertEquals("test", d.getAsString());
		}
	}

	@Test
	public void toExpose() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.add("b", new Value("test"));
		builder.add("c", new Value("test"));
		builder.add("d", new Value("test"));
		builder.add("e", new Value("test"));
		builder.add("f", new Value("test"));
		builder.close();
		final TestEntityExpose entity = new VPack().deserialize(builder.slice(), TestEntityExpose.class);
		Assert.assertNotNull(entity);
		Assert.assertNotNull(entity.a);
		Assert.assertEquals("test", entity.a);
		Assert.assertNotNull(entity.b);
		Assert.assertEquals("test", entity.b);
		Assert.assertNotNull(entity.c);
		Assert.assertEquals("test", entity.c);
		Assert.assertNotNull(entity.e);
		Assert.assertEquals("test", entity.e);
		Assert.assertNull(entity.d);
		Assert.assertNull(entity.f);
	}

	@Test
	public void directFromCollection() throws VPackException {
		final Collection<TestEntityString> list = new ArrayList<>();
		list.add(new TestEntityString());
		list.add(new TestEntityString());

		final VPackSlice vpack = new VPack().serialize(list, new Type<Collection<TestEntityString>>() {
		}.getType());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isArray());
		Assert.assertEquals(list.size(), vpack.getLength());
		for (int i = 0; i < list.size(); i++) {
			final VPackSlice entry = vpack.get(i);
			Assert.assertTrue(entry.isObject());
			Assert.assertEquals(3, entry.getLength());
			final VPackSlice s = entry.get("s");
			Assert.assertTrue(s.isString());
			Assert.assertEquals("test", s.getAsString());
		}
	}

	@Test
	public void directToCollection() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.OBJECT));
		builder.add("s", new Value("abc"));
		builder.close();
		builder.close();
		final List<TestEntityString> list = new VPack().deserialize(builder.slice(),
			new Type<List<TestEntityString>>() {
			}.getType());
		Assert.assertNotNull(list);
		Assert.assertEquals(1, list.size());
		final TestEntityString entry = list.get(0);
		Assert.assertEquals("abc", entry.s);
	}

	@Test
	public void directFromStringMap() throws VPackException {
		final Map<String, TestEntityString> map = new HashMap<>();
		map.put("a", new TestEntityString());
		map.put("b", new TestEntityString());

		final VPackSlice vpack = new VPack().serialize(map, new Type<Map<String, TestEntityString>>() {
		}.getType());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(2, vpack.getLength());
		final VPackSlice a = vpack.get("a");
		checkStringEntity(a);
	}

	@Test
	public void directToStringMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.OBJECT));
		builder.add("s", new Value("abc"));
		builder.close();
		builder.close();
		final Map<String, TestEntityString> map = new VPack().deserialize(builder.slice(),
			new Type<Map<String, TestEntityString>>() {
			}.getType());
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		final TestEntityString a = map.get("a");
		Assert.assertNotNull(a);
		Assert.assertEquals("abc", a.s);
	}

	@Test
	public void directFromObjectMap() throws VPackException {
		final Map<TestEntityString, TestEntityString> map = new HashMap<>();
		map.put(new TestEntityString(), new TestEntityString());
		map.put(new TestEntityString(), new TestEntityString());

		final VPackSlice vpack = new VPack().serialize(map, new Type<Map<TestEntityString, TestEntityString>>() {
		}.getType());
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isArray());
		Assert.assertEquals(map.size(), vpack.getLength());
		for (int i = 0; i < map.size(); i++) {
			final VPackSlice entry = vpack.get(i);
			final VPackSlice key = entry.get("key");
			checkStringEntity(key);
			final VPackSlice value = entry.get("value");
			checkStringEntity(value);
		}
	}

	private void checkStringEntity(final VPackSlice vpack) throws VPackException {
		final TestEntityString expected = new TestEntityString();
		Assert.assertTrue(vpack.isObject());
		Assert.assertEquals(3, vpack.getLength());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isString());
		Assert.assertEquals(expected.s, s.getAsString());
		final VPackSlice c1 = vpack.get("c1");
		Assert.assertTrue(c1.isString());
		Assert.assertEquals(expected.c1, new Character(c1.getAsChar()));
		final VPackSlice c2 = vpack.get("c2");
		Assert.assertTrue(c2.isString());
		Assert.assertEquals(expected.c2, c2.getAsChar());
	}

	@Test
	public void directToObjectMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.OBJECT));
		builder.add("key", new Value(ValueType.OBJECT));
		builder.add("s", new Value("abc"));
		builder.close();
		builder.add("value", new Value(ValueType.OBJECT));
		builder.add("s", new Value("abc"));
		builder.close();
		builder.close();
		builder.close();
		final Map<TestEntityString, TestEntityString> map = new VPack().deserialize(builder.slice(),
			new Type<Map<TestEntityString, TestEntityString>>() {
			}.getType());
		Assert.assertNotNull(map);
		Assert.assertEquals(1, map.size());
		for (final Entry<TestEntityString, TestEntityString> entry : map.entrySet()) {
			Assert.assertEquals("abc", entry.getKey().s);
			Assert.assertEquals("abc", entry.getValue().s);
		}
	}

	@Test
	public void dontSerializeNullValues() throws VPackException {
		final VPack serializer = new VPack();
		serializer.getOptions().setSerializeNullValues(false);
		final TestEntityString entity = new TestEntityString();
		entity.setS(null);
		final VPackSlice vpack = serializer.serialize(entity);
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isObject());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isNone());
	}

	@Test
	public void serializeNullValue() throws VPackException {
		final VPack serializer = new VPack();
		serializer.getOptions().setSerializeNullValues(true);
		final TestEntityString entity = new TestEntityString();
		entity.setS(null);
		final VPackSlice vpack = serializer.serialize(entity);
		Assert.assertNotNull(vpack);
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isNull());
	}

	@Test
	public void toNullValue() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("s", new Value(ValueType.NULL));
		builder.close();
		final TestEntityString entity = new VPack().deserialize(builder.slice(), TestEntityString.class);
		Assert.assertNotNull(entity);
		Assert.assertNull(entity.s);
		Assert.assertNotNull(entity.c1);
		Assert.assertNotNull(entity.c2);
	}

	@Test
	public void additionalFields() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<>();
		additionalFields.put("a", "test");
		final VPackSlice vpack = new VPack().serialize(entity, additionalFields);
		Assert.assertNotNull(vpack);
		Assert.assertEquals(4, vpack.getLength());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isString());
		Assert.assertEquals("test", s.getAsString());
		final VPackSlice a = vpack.get("a");
		Assert.assertTrue(a.isString());
		Assert.assertEquals("test", a.getAsString());
	}

	@Test
	public void additionalDuplicatedFields() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<>();
		additionalFields.put("s", "test1");
		final VPackSlice vpack = new VPack().serialize(entity, additionalFields);
		Assert.assertNotNull(vpack);
		Assert.assertEquals(3, vpack.getLength());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isString());
		Assert.assertEquals("test", s.getAsString());
	}

	@Test
	public void additionalNullFieldsExcludeNull() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<>();
		additionalFields.put("a", null);
		final VPackSlice vpack = new VPack().serialize(entity, additionalFields);
		Assert.assertNotNull(vpack);
		Assert.assertEquals(3, vpack.getLength());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isString());
		Assert.assertEquals("test", s.getAsString());
	}

	@Test
	public void additionalNullFieldsIncludeNull() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<>();
		additionalFields.put("a", null);
		final VPack serializer = new VPack();
		serializer.getOptions().setSerializeNullValues(true);
		final VPackSlice vpack = serializer.serialize(entity, additionalFields);
		Assert.assertNotNull(vpack);
		Assert.assertEquals(4, vpack.getLength());
		final VPackSlice s = vpack.get("s");
		Assert.assertTrue(s.isString());
		Assert.assertEquals("test", s.getAsString());
		final VPackSlice a = vpack.get("a");
		Assert.assertTrue(a.isNull());
	}

	@Test
	public void toSimpleString() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value("test"));
		final String s = new VPack().deserialize(builder.slice(), String.class);
		Assert.assertNotNull(s);
		Assert.assertEquals("test", s);
	}

	@Test
	public void fromSimpleString() throws VPackException {
		final VPackSlice vpack = new VPack().serialize("test");
		Assert.assertNotNull(vpack);
		Assert.assertTrue(vpack.isString());
		Assert.assertEquals("test", vpack.getAsString());
	}

	protected static class TestEntityTyped<T> {
		private T e;
	}

	@Test
	public void fromStringTypedEntity() throws VPackException {
		final TestEntityTyped<String> entity = new TestEntityTyped<>();
		entity.e = "test";
		final VPackSlice vpack = new VPack().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isString(), is(true));
		assertThat(e.getAsString(), is("test"));
	}

	@Test
	public void fromObjectTypedEntity() throws VPackException {
		final TestEntityTyped<TestEntityString> entity = new TestEntityTyped<>();
		entity.e = new TestEntityString();
		entity.e.s = "test2";
		final VPackSlice vpack = new VPack().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isObject(), is(true));
		final VPackSlice s = e.get("s");
		assertThat(s, is(notNullValue()));
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test2"));
	}

	@Test
	public void fromTypedTypedEntity() throws VPackException {
		final TestEntityTyped<TestEntityTyped<String>> entity = new TestEntityTyped<>();
		entity.e = new TestEntityTyped<>();
		entity.e.e = "test";
		final VPackSlice vpack = new VPack().serialize(entity,
			new Type<TestEntityTyped<TestEntityTyped<String>>>() {
			}.getType());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isObject(), is(true));
		final VPackSlice e2 = e.get("e");
		assertThat(e2, is(notNullValue()));
		assertThat(e2.isString(), is(true));
		assertThat(e2.getAsString(), is("test"));
	}
}

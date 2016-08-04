package com.arangodb.velocypack;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackParserTest {

	@Test
	public void object1Field() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\"}", json);
	}

	@Test
	public void object2Fields() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.add("b", new Value(true));
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\",\"b\":true}", json);
	}

	@Test
	public void objectStringField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.add("b", new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\",\"b\":\"test\"}", json);
	}

	@Test
	public void objectBooleanField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(true));
		builder.add("b", new Value(false));
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":true,\"b\":false}", json);
	}

	@Test
	public void objectNumberField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(5));
		builder.add("b", new Value(5.5));
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":5,\"b\":5.5}", json);
	}

	@Test
	public void objectDateField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(new Date(946681200000L)));// 2000-01-01
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"Jan 1, 2000 12:00:00 AM\"}", json);
	}

	@Test
	public void arrayInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.ARRAY));
		builder.add(new Value(1));
		builder.add(new Value(2));
		builder.add(new Value(3));
		builder.close();
		builder.add("b", new Value(ValueType.ARRAY));
		builder.add(new Value("a"));
		builder.add(new Value("b"));
		builder.add(new Value("c"));
		builder.close();
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":[1,2,3],\"b\":[\"a\",\"b\",\"c\"]}", json);
	}

	@Test
	public void objectInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.OBJECT));
		builder.add("aa", new Value("test"));
		builder.add("ab", new Value(true));
		builder.close();
		builder.add("b", new Value(ValueType.OBJECT));
		builder.add("ba", new Value("test"));
		builder.add("bb", new Value(5.5));
		builder.close();
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("{\"a\":{\"aa\":\"test\",\"ab\":true},\"b\":{\"ba\":\"test\",\"bb\":5.5}}", json);
	}

	@Test
	public void objectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("[{\"a\":\"test\"},{\"a\":\"test\"}]", json);
	}

	@Test
	public void arrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(1));
		builder.add(new Value(2));
		builder.add(new Value(3));
		builder.close();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value("a"));
		builder.add(new Value("b"));
		builder.add(new Value("c"));
		builder.close();
		builder.close();
		final String json = VPackParser.toJson(builder.slice());
		Assert.assertEquals("[[1,2,3],[\"a\",\"b\",\"c\"]]", json);
	}

	@Test
	public void excludeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.NULL));
		final String b = null;
		builder.add("b", new Value(b));
		builder.add("c", new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice(), false);
		Assert.assertEquals("{\"c\":\"test\"}", json);
	}

	@Test
	public void includeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.NULL));
		final String b = null;
		builder.add("b", new Value(b));
		builder.add("c", new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice(), true);
		Assert.assertEquals("{\"a\":null,\"b\":null,\"c\":\"test\"}", json);
	}

	@Test
	public void excludeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.NULL));
		final String s = null;
		builder.add(new Value(s));
		builder.add(new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice(), false);
		Assert.assertEquals("[\"test\"]", json);
	}

	@Test
	public void includeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.NULL));
		final String s = null;
		builder.add(new Value(s));
		builder.add(new Value("test"));
		builder.close();
		final String json = VPackParser.toJson(builder.slice(), true);
		Assert.assertEquals("[null,null,\"test\"]", json);
	}
}

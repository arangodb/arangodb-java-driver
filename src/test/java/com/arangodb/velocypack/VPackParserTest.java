package com.arangodb.velocypack;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackParserTest {

	@Test
	public void toJsonObject1Field() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\"}", json);
	}

	@Test
	public void toJsonObject2Fields() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.add("b", new Value(true));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\",\"b\":true}", json);
	}

	@Test
	public void toJsonObjectStringField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.add("b", new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":\"test\",\"b\":\"test\"}", json);
	}

	@Test
	public void toJsonObjectBooleanField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(true));
		builder.add("b", new Value(false));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":true,\"b\":false}", json);
	}

	@Test
	public void toJsonObjectNumberField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(5));
		builder.add("b", new Value(5.5));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":5,\"b\":5.5}", json);
	}

	@Test
	public void toJsonArrayInObject() throws VPackException {
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
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":[1,2,3],\"b\":[\"a\",\"b\",\"c\"]}", json);
	}

	@Test
	public void toJsonObjectInObject() throws VPackException {
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
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("{\"a\":{\"aa\":\"test\",\"ab\":true},\"b\":{\"ba\":\"test\",\"bb\":5.5}}", json);
	}

	@Test
	public void toJsonObjectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("test"));
		builder.close();
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("[{\"a\":\"test\"},{\"a\":\"test\"}]", json);
	}

	@Test
	public void toJsonArrayInArray() throws VPackException {
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
		final String json = new VPackParser().toJson(builder.slice());
		Assert.assertEquals("[[1,2,3],[\"a\",\"b\",\"c\"]]", json);
	}

	@Test
	public void toJsonExcludeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.NULL));
		final String b = null;
		builder.add("b", new Value(b));
		builder.add("c", new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), false);
		Assert.assertEquals("{\"c\":\"test\"}", json);
	}

	@Test
	public void toJsonIncludeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value(ValueType.NULL));
		final String b = null;
		builder.add("b", new Value(b));
		builder.add("c", new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), true);
		Assert.assertEquals("{\"a\":null,\"b\":null,\"c\":\"test\"}", json);
	}

	@Test
	public void toJsonExcludeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.NULL));
		final String s = null;
		builder.add(new Value(s));
		builder.add(new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), false);
		Assert.assertEquals("[\"test\"]", json);
	}

	@Test
	public void toJsonIncludeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.ARRAY));
		builder.add(new Value(ValueType.NULL));
		final String s = null;
		builder.add(new Value(s));
		builder.add(new Value("test"));
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), true);
		Assert.assertEquals("[null,null,\"test\"]", json);
	}

	@Test
	public void fromJsonObject1Field() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":\"test\"}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("test"));
	}

	@Test
	public void fromJsonObject2Fields() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":\"test\",\"b\":true}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("test"));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isBoolean(), is(true));
		assertThat(b.getAsBoolean(), is(true));
	}

	@Test
	public void fromJsonObjectStringField() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":\"test1\",\"b\":\"test2\"}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("test1"));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isString(), is(true));
		assertThat(b.getAsString(), is("test2"));
	}

	@Test
	public void fromJsonObjectBooleanField() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":true,\"b\":false}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isBoolean(), is(true));
		assertThat(a.getAsBoolean(), is(true));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isBoolean(), is(true));
		assertThat(b.getAsBoolean(), is(false));
	}

	@Test
	public void fromJsonObjectNumberField() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":5,\"b\":5.5}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isInteger(), is(true));
		assertThat(a.getAsInt(), is(5));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isDouble(), is(true));
		assertThat(b.getAsDouble(), is(5.5));
	}

	@Test
	public void fromJsonArrayInObject() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":[1,2,3],\"b\":[\"a\",\"b\",\"c\"]}");
		assertThat(vpack.isObject(), is(true));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isArray(), is(true));
		assertThat(a.size(), is(3));
		assertThat(a.get(0).isInteger(), is(true));
		assertThat(a.get(0).getAsInt(), is(1));
		assertThat(a.get(1).isInteger(), is(true));
		assertThat(a.get(1).getAsInt(), is(2));
		assertThat(a.get(2).isInteger(), is(true));
		assertThat(a.get(2).getAsInt(), is(3));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isArray(), is(true));
		assertThat(b.size(), is(3));
		assertThat(b.get(0).isString(), is(true));
		assertThat(b.get(0).getAsString(), is("a"));
		assertThat(b.get(1).isString(), is(true));
		assertThat(b.get(1).getAsString(), is("b"));
		assertThat(b.get(2).isString(), is(true));
		assertThat(b.get(2).getAsString(), is("c"));
	}

	@Test
	public void fromJsonObjectInObject() throws VPackException {
		final VPackSlice vpack = new VPackParser()
				.fromJson("{\"a\":{\"aa\":\"test\",\"ab\":true},\"b\":{\"ba\":\"test\",\"bb\":5.5}}");
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(2));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isObject(), is(true));
		assertThat(a.size(), is(2));
		final VPackSlice aa = a.get("aa");
		assertThat(aa.isString(), is(true));
		assertThat(aa.getAsString(), is("test"));
		final VPackSlice ab = a.get("ab");
		assertThat(ab.isBoolean(), is(true));
		assertThat(ab.getAsBoolean(), is(true));
		final VPackSlice b = vpack.get("b");
		assertThat(b.isObject(), is(true));
		assertThat(b.size(), is(2));
		final VPackSlice ba = b.get("ba");
		assertThat(ba.isString(), is(true));
		assertThat(ba.getAsString(), is("test"));
		final VPackSlice bb = b.get("bb");
		assertThat(bb.isDouble(), is(true));
		assertThat(bb.getAsDouble(), is(5.5));
	}

	@Test
	public void fromJsonObjectInArray() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("[{\"a\":\"test\"},{\"a\":\"test\"}]");
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.size(), is(2));
		final VPackSlice z = vpack.get(0);
		assertThat(z.isObject(), is(true));
		assertThat(z.size(), is(1));
		final VPackSlice za = z.get("a");
		assertThat(za.isString(), is(true));
		assertThat(za.getAsString(), is("test"));
		final VPackSlice o = vpack.get(1);
		assertThat(o.isObject(), is(true));
		assertThat(o.size(), is(1));
		final VPackSlice oa = o.get("a");
		assertThat(oa.isString(), is(true));
		assertThat(oa.getAsString(), is("test"));
	}

	@Test
	public void fromJsonArrayInArray() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("[[1,2,3],[\"a\",\"b\",\"c\"]]");
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.size(), is(2));
		final VPackSlice z = vpack.get(0);
		assertThat(z.isArray(), is(true));
		assertThat(z.size(), is(3));
		assertThat(z.get(0).isInteger(), is(true));
		assertThat(z.get(0).getAsInt(), is(1));
		assertThat(z.get(1).isInteger(), is(true));
		assertThat(z.get(1).getAsInt(), is(2));
		assertThat(z.get(2).isInteger(), is(true));
		assertThat(z.get(2).getAsInt(), is(3));
		final VPackSlice o = vpack.get(1);
		assertThat(o.isArray(), is(true));
		assertThat(o.size(), is(3));
		assertThat(o.get(0).isString(), is(true));
		assertThat(o.get(0).getAsString(), is("a"));
		assertThat(o.get(1).isString(), is(true));
		assertThat(o.get(1).getAsString(), is("b"));
		assertThat(o.get(2).isString(), is(true));
		assertThat(o.get(2).getAsString(), is("c"));
	}

	@Test
	public void fromJsonExcludeNullValueInObject() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":null,\"b\":null,\"c\":\"test\"}", false);
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(1));
		assertThat(vpack.get("a").isNone(), is(true));
		assertThat(vpack.get("b").isNone(), is(true));
		assertThat(vpack.get("c").isString(), is(true));
		assertThat(vpack.get("c").getAsString(), is("test"));
	}

	@Test
	public void fromJsonIncludeNullValueInObject() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("{\"a\":null,\"b\":null,\"c\":\"test\"}", true);
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(3));
		assertThat(vpack.get("a").isNull(), is(true));
		assertThat(vpack.get("b").isNull(), is(true));
		assertThat(vpack.get("c").isString(), is(true));
		assertThat(vpack.get("c").getAsString(), is("test"));
	}

	@Test
	public void fromJsonExcludeNullValueInArray() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("[null,null,\"test\"]", false);
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.size(), is(1));
		assertThat(vpack.get(0).isString(), is(true));
		assertThat(vpack.get(0).getAsString(), is("test"));
	}

	@Test
	public void fromJsonIncludeNullValueInArray() throws VPackException {
		final VPackSlice vpack = new VPackParser().fromJson("[null,null,\"test\"]", true);
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.size(), is(3));
		assertThat(vpack.get(0).isNull(), is(true));
		assertThat(vpack.get(1).isNull(), is(true));
		assertThat(vpack.get(2).isString(), is(true));
		assertThat(vpack.get(2).getAsString(), is("test"));
	}

	@Test
	public void customDeserializer() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("a"));
		builder.add("b", new Value("b"));
		builder.close();
		final String json = new VPackParser()
				.registerDeserializer(ValueType.STRING, (parent, attribute, vpack, jsonBuffer) -> {
					jsonBuffer.append(JSONValue.toJSONString(vpack.getAsString() + "1"));
				}).toJson(builder.slice());
		assertThat(json, is("{\"a\":\"a1\",\"b\":\"b1\"}"));
	}

	@Test
	public void customDeserializerByName() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(new Value(ValueType.OBJECT));
		builder.add("a", new Value("a"));
		builder.add("b", new Value("b"));
		builder.close();
		final String json = new VPackParser()
				.registerDeserializer("a", ValueType.STRING, (parent, attribute, vpack, jsonBuffer) -> {
					jsonBuffer.append(JSONValue.toJSONString(vpack.getAsString() + "1"));
				}).toJson(builder.slice());
		assertThat(json, is("{\"a\":\"a1\",\"b\":\"b\"}"));
	}

}

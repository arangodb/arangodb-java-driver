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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.json.simple.JSONValue;
import org.junit.Test;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackParserTest {

	@Test
	public void toJsonObject1Field() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":\"test\"}"));
	}

	@Test
	public void toJsonObject2Fields() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.add("b", true);
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":\"test\",\"b\":true}"));
	}

	@Test
	public void toJsonObjectStringField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.add("b", "test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":\"test\",\"b\":\"test\"}"));
	}

	@Test
	public void toJsonObjectBooleanField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", true);
		builder.add("b", false);
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":true,\"b\":false}"));
	}

	@Test
	public void toJsonObjectNumberField() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", 5);
		builder.add("b", 5.5);
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":5,\"b\":5.5}"));
	}

	@Test
	public void toJsonArrayInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.ARRAY);
		builder.add(1);
		builder.add(2);
		builder.add(3);
		builder.close();
		builder.add("b", ValueType.ARRAY);
		builder.add("a");
		builder.add("b");
		builder.add("c");
		builder.close();
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":[1,2,3],\"b\":[\"a\",\"b\",\"c\"]}"));
	}

	@Test
	public void toJsonObjectInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.OBJECT);
		builder.add("aa", "test");
		builder.add("ab", true);
		builder.close();
		builder.add("b", ValueType.OBJECT);
		builder.add("ba", "test");
		builder.add("bb", 5.5);
		builder.close();
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("{\"a\":{\"aa\":\"test\",\"ab\":true},\"b\":{\"ba\":\"test\",\"bb\":5.5}}"));
	}

	@Test
	public void toJsonObjectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.close();
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.close();
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("[{\"a\":\"test\"},{\"a\":\"test\"}]"));
	}

	@Test
	public void toJsonArrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.ARRAY);
		builder.add(1);
		builder.add(2);
		builder.add(3);
		builder.close();
		builder.add(ValueType.ARRAY);
		builder.add("a");
		builder.add("b");
		builder.add("c");
		builder.close();
		builder.close();
		final String json = new VPackParser().toJson(builder.slice());
		assertThat(json, is("[[1,2,3],[\"a\",\"b\",\"c\"]]"));
	}

	@Test
	public void toJsonExcludeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.NULL);
		final String b = null;
		builder.add("b", b);
		builder.add("c", "test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), false);
		assertThat(json, is("{\"c\":\"test\"}"));
	}

	@Test
	public void toJsonIncludeNullValueInObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.NULL);
		final String b = null;
		builder.add("b", b);
		builder.add("c", "test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), true);
		assertThat(json, is("{\"a\":null,\"b\":null,\"c\":\"test\"}"));
	}

	@Test
	public void toJsonExcludeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.NULL);
		final String s = null;
		builder.add(s);
		builder.add("test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), false);
		assertThat(json, is("[\"test\"]"));
	}

	@Test
	public void toJsonIncludeNullValueInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.NULL);
		final String s = null;
		builder.add(s);
		builder.add("test");
		builder.close();
		final String json = new VPackParser().toJson(builder.slice(), true);
		assertThat(json, is("[null,null,\"test\"]"));
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
		builder.add(ValueType.OBJECT);
		builder.add("a", "a");
		builder.add("b", "b");
		builder.close();
		final VPackJsonDeserializer deserializer = new VPackJsonDeserializer() {
			@Override
			public void deserialize(
				final VPackSlice parent,
				final String attribute,
				final VPackSlice vpack,
				final StringBuilder json) throws VPackException {
				json.append(JSONValue.toJSONString(vpack.getAsString() + "1"));
			}
		};
		final String json = new VPackParser().registerDeserializer(ValueType.STRING, deserializer)
				.toJson(builder.slice());
		assertThat(json, is("{\"a\":\"a1\",\"b\":\"b1\"}"));
	}

	@Test
	public void customDeserializerByName() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "a");
		builder.add("b", "b");
		builder.close();
		final String json = new VPackParser().registerDeserializer("a", ValueType.STRING, new VPackJsonDeserializer() {
			@Override
			public void deserialize(
				final VPackSlice parent,
				final String attribute,
				final VPackSlice vpack,
				final StringBuilder json) throws VPackException {
				json.append(JSONValue.toJSONString(vpack.getAsString() + "1"));
			}
		}).toJson(builder.slice());
		assertThat(json, is("{\"a\":\"a1\",\"b\":\"b\"}"));
	}

	@Test
	public void customSerializer() throws VPackException {
		final VPackSlice vpack = new VPackParser().registerSerializer(String.class, new VPackJsonSerializer<String>() {
			@Override
			public void serialize(final VPackBuilder builder, final String attribute, final String value)
					throws VPackException {
				builder.add(attribute, value + "1");
			}
		}).fromJson("{\"a\":\"a\"}");
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get("a").isString(), is(true));
		assertThat(vpack.get("a").getAsString(), is("a1"));
	}

	@Test
	public void customSerializerByName() {
		final String json = "{\"a\":\"a\",\"b\":\"b\"}";
		final VPackSlice vpack = new VPackParser()
				.registerSerializer("a", String.class, new VPackJsonSerializer<String>() {
					@Override
					public void serialize(final VPackBuilder builder, final String attribute, final String value)
							throws VPackException {
						builder.add(attribute, value + "1");
					}
				}).fromJson(json);
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get("a").isString(), is(true));
		assertThat(vpack.get("a").getAsString(), is("a1"));
		assertThat(vpack.get("b").isString(), is(true));
		assertThat(vpack.get("b").getAsString(), is("b"));
	}

	@Test
	public void dateToJson() {
		final VPackSlice vpack = new VPackBuilder().add(new Date(1478766992059L)).slice();
		final VPackParser parser = new VPackParser();
		assertThat(parser.toJson(vpack), containsString("2016-11-10T"));
		assertThat(parser.toJson(vpack), containsString(":36:32.059Z"));
	}

	@Test
	public void bytelength() {
		final String name1 = "{\"name1\":\"job_04_detail_1\",\"seven__\":\"123456789\",\"_key\":\"191d936d-1eb9-4094-9c1c-9e0ba1d01867\",\"lang\":\"it\",\"value\":\"[CTO]\\n Ha supervisionato e gestito il reparto di R&D per il software, 1234567 formulando una visione di lungo periodo con la Direzione dell'Azienda.\"}";
		final String name = "{\"name\":\"job_04_detail_1\",\"seven__\":\"123456789\",\"_key\":\"191d936d-1eb9-4094-9c1c-9e0ba1d01867\",\"lang\":\"it\",\"value\":\"[CTO]\\n Ha supervisionato e gestito il reparto di R&D per il software, 1234567 formulando una visione di lungo periodo con la Direzione dell'Azienda.\"}";

		final VPack vpacker = new VPack.Builder().build();
		{
			final VPackSlice vpack = vpacker.serialize(name1);
			assertThat(vpack.isObject(), is(true));
			assertThat(vpack.get("name1").isString(), is(true));
			assertThat(vpack.get("name1").getAsString(), is("job_04_detail_1"));

		}
		{
			final VPackSlice vpack = vpacker.serialize(name);
			assertThat(vpack.isObject(), is(true));
			assertThat(vpack.get("name").isString(), is(true));
			assertThat(vpack.get("name").getAsString(), is("job_04_detail_1"));
		}
	}

}

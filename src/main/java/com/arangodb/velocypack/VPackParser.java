package com.arangodb.velocypack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.arangodb.velocypack.exception.VPackBuilderException;
import com.arangodb.velocypack.exception.VPackBuilderNeedOpenCompoundException;
import com.arangodb.velocypack.exception.VPackException;
import com.arangodb.velocypack.exception.VPackKeyTypeException;
import com.arangodb.velocypack.exception.VPackNeedAttributeTranslatorException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackParser {

	private static final char OBJECT_OPEN = '{';
	private static final char OBJECT_CLOSE = '}';
	private static final char ARRAY_OPEN = '[';
	private static final char ARRAY_CLOSE = ']';
	private static final char FIELD = ':';
	private static final char SEPARATOR = ',';
	private static final String NULL = "null";
	private final Map<ValueType, VPackJsonDeserializer> deserializers;
	private final Map<String, Map<ValueType, VPackJsonDeserializer>> deserializersByName;

	public VPackParser() {
		super();
		deserializers = new HashMap<>();
		deserializersByName = new HashMap<>();
	}

	public String toJson(final VPackSlice vpack) throws VPackException {
		return toJson(vpack, false);
	}

	public String toJson(final VPackSlice vpack, final boolean includeNullValues) throws VPackException {
		final StringBuilder json = new StringBuilder();
		parse(null, null, vpack, json, includeNullValues);
		return json.toString();
	}

	public VPackParser registerDeserializer(
		final String attribute,
		final ValueType type,
		final VPackJsonDeserializer deserializer) {
		Map<ValueType, VPackJsonDeserializer> byName = deserializersByName.get(attribute);
		if (byName == null) {
			byName = new HashMap<>();
			deserializersByName.put(attribute, byName);
		}
		byName.put(type, deserializer);
		return this;
	}

	public VPackParser registerDeserializer(final ValueType type, final VPackJsonDeserializer deserializer) {
		deserializers.put(type, deserializer);
		return this;
	}

	private VPackJsonDeserializer getDeserializer(final String attribute, final ValueType type) {
		VPackJsonDeserializer deserializer = null;
		final Map<ValueType, VPackJsonDeserializer> byName = deserializersByName.get(attribute);
		if (byName != null) {
			deserializer = byName.get(type);
		}
		if (deserializer == null) {
			deserializer = deserializers.get(type);
		}
		return deserializer;
	}

	private void parse(
		final VPackSlice parent,
		final VPackSlice attribute,
		final VPackSlice value,
		final StringBuilder json,
		final boolean includeNullValues) throws VPackException {

		VPackJsonDeserializer deserializer = null;
		if (attribute != null) {
			final String name = attribute.makeKey().getAsString();
			appendField(name, json);
			deserializer = getDeserializer(name, value.type());
		}
		if (deserializer != null) {
			deserializer.deserialize(parent, attribute, value, json);
		} else {
			if (value.isObject()) {
				parseObject(value, json, includeNullValues);
			} else if (value.isArray()) {
				parseArray(value, json, includeNullValues);
			} else if (value.isBoolean()) {
				json.append(value.getAsBoolean());
			} else if (value.isString()) {
				json.append(JSONValue.toJSONString(value.getAsString()));
			} else if (value.isNumber()) {
				json.append(value.getAsNumber());
			} else if (value.isNull()) {
				json.append(NULL);
			} else {
				json.append(NULL);
			}
		}
	}

	private static void appendField(final String attribute, final StringBuilder json) {
		json.append(JSONValue.toJSONString(attribute));
		json.append(FIELD);
	}

	private void parseObject(final VPackSlice value, final StringBuilder json, final boolean includeNullValues)
			throws VPackException {
		json.append(OBJECT_OPEN);
		int added = 0;
		for (final Iterator<VPackSlice> iterator = value.iterator(); iterator.hasNext();) {
			final VPackSlice nextAttr = iterator.next();
			final VPackSlice nextValue = new VPackSlice(nextAttr.getVpack(),
					nextAttr.getStart() + nextAttr.getByteSize());
			if (!nextValue.isNull() || includeNullValues) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(value, nextAttr, nextValue, json, includeNullValues);
			}
		}
		json.append(OBJECT_CLOSE);
	}

	private void parseArray(final VPackSlice value, final StringBuilder json, final boolean includeNullValues)
			throws VPackException {
		json.append(ARRAY_OPEN);
		int added = 0;
		for (int i = 0; i < value.getLength(); i++) {
			final VPackSlice valueAt = value.get(i);
			if (!valueAt.isNull() || includeNullValues) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(value, null, valueAt, json, includeNullValues);
			}
		}
		json.append(ARRAY_CLOSE);
	}

	public VPackSlice fromJson(final String json) throws VPackException {
		return fromJson(json, false);
	}

	public VPackSlice fromJson(final String json, final boolean includeNullValues) throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		final JSONParser parser = new JSONParser();
		final ContentHandler contentHandler = new VPackContentHandler(builder, includeNullValues);
		try {
			parser.parse(json, contentHandler);
		} catch (final ParseException e) {
			throw new VPackBuilderException(e);
		}
		return builder.slice();
	}

	private static class VPackContentHandler implements ContentHandler {

		private final VPackBuilder builder;
		private String attribute;
		private final boolean includeNullValues;

		public VPackContentHandler(final VPackBuilder builder, final boolean includeNullValues) {
			this.builder = builder;
			this.includeNullValues = includeNullValues;
			attribute = null;
		}

		private void add(final Value value) throws ParseException {
			try {
				builder.add(attribute, value);
				attribute = null;
			} catch (final VPackBuilderException e) {
				throw new ParseException(ParseException.ERROR_UNEXPECTED_EXCEPTION);
			}
		}

		private void close() throws ParseException {
			try {
				builder.close();
			} catch (VPackBuilderNeedOpenCompoundException | VPackKeyTypeException
					| VPackNeedAttributeTranslatorException e) {
				throw new ParseException(ParseException.ERROR_UNEXPECTED_EXCEPTION);
			}
		}

		@Override
		public void startJSON() throws ParseException, IOException {
		}

		@Override
		public void endJSON() throws ParseException, IOException {
		}

		@Override
		public boolean startObject() throws ParseException, IOException {
			add(new Value(ValueType.OBJECT));
			return true;
		}

		@Override
		public boolean endObject() throws ParseException, IOException {
			close();
			return true;
		}

		@Override
		public boolean startObjectEntry(final String key) throws ParseException, IOException {
			attribute = key;
			return true;
		}

		@Override
		public boolean endObjectEntry() throws ParseException, IOException {
			return true;
		}

		@Override
		public boolean startArray() throws ParseException, IOException {
			add(new Value(ValueType.ARRAY));
			return true;
		}

		@Override
		public boolean endArray() throws ParseException, IOException {
			close();
			return true;
		}

		@Override
		public boolean primitive(final Object value) throws ParseException, IOException {
			if (value == null) {
				if (includeNullValues) {
					add(new Value(ValueType.NULL));
				}
			} else if (String.class.isAssignableFrom(value.getClass())) {
				add(new Value(String.class.cast(value)));
			} else if (Boolean.class.isAssignableFrom(value.getClass())) {
				add(new Value(Boolean.class.cast(value)));
			} else if (Double.class.isAssignableFrom(value.getClass())) {
				add(new Value(Double.class.cast(value)));
			} else if (Number.class.isAssignableFrom(value.getClass())) {
				add(new Value(Long.class.cast(value)));
			}
			return true;
		}

	}

}
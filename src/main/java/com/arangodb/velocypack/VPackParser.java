package com.arangodb.velocypack;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackParser {

	private static final String OBJECT_OPEN = "{";
	private static final String OBJECT_CLOSE = "}";
	private static final String ARRAY_OPEN = "[";
	private static final String ARRAY_CLOSE = "]";
	private static final String STRING = "\"";
	private static final String FIELD = ":";
	private static final String SEPARATOR = ",";
	private static final String NULL = "null";
	private static final DateFormat DATEFORMAT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT,
		Locale.US);

	public static String toJson(final VPackSlice vpack) {
		return toJson(vpack, false);
	}

	public static String toJson(final VPackSlice vpack, final boolean includeNullValue) {
		final StringBuilder json = new StringBuilder();
		parse(null, vpack, json, includeNullValue);
		return json.toString();
	}

	private static void parse(
		final VPackSlice attribute,
		final VPackSlice value,
		final StringBuilder json,
		final boolean includeNullValue) {
		if (attribute != null && attribute.isString()) {
			appendField(attribute, json);
		}
		if (value.isObject()) {
			parseObject(value, json, includeNullValue);
		} else if (value.isArray()) {
			parseArray(value, json, includeNullValue);
		} else if (value.isBoolean()) {
			json.append(value.getAsBoolean());
		} else if (value.isString()) {
			json.append(STRING);
			json.append(value.getAsString());
			json.append(STRING);
		} else if (value.isNumber()) {
			json.append(value.getAsNumber());
		} else if (value.isDate()) {
			json.append(STRING);
			json.append(DATEFORMAT.format(value.getAsDate()));
			json.append(STRING);
		} else if (value.isNull()) {
			json.append(NULL);
		}
	}

	private static void appendField(final VPackSlice attribute, final StringBuilder json) {
		json.append(STRING);
		json.append(attribute.getAsString());
		json.append(STRING);
		json.append(FIELD);
	}

	private static void parseObject(final VPackSlice value, final StringBuilder json, final boolean includeNullValue) {
		json.append(OBJECT_OPEN);
		int added = 0;
		for (final Iterator<VPackSlice> iterator = value.iterator(); iterator.hasNext();) {
			final VPackSlice nextAttr = iterator.next();
			final VPackSlice nextValue = new VPackSlice(nextAttr.getVpack(),
					nextAttr.getStart() + nextAttr.getByteSize());
			if (!nextValue.isNull() || includeNullValue) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(nextAttr, nextValue, json, includeNullValue);
			}
		}
		json.append(OBJECT_CLOSE);
	}

	private static void parseArray(final VPackSlice value, final StringBuilder json, final boolean includeNullValue) {
		json.append(ARRAY_OPEN);
		int added = 0;
		for (int i = 0; i < value.getLength(); i++) {
			final VPackSlice valueAt = value.get(i);
			if (!valueAt.isNull() || includeNullValue) {
				if (added++ > 0) {
					json.append(SEPARATOR);
				}
				parse(null, valueAt, json, includeNullValue);
			}
		}
		json.append(ARRAY_CLOSE);
	}

}

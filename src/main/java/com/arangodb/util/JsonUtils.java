/*
 * Copyright (C) 2012 tamtam180
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arangodb.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author tamtam180 - kirscheless at gmail.com
 *
 */
public class JsonUtils {

	private JsonUtils() {
		// this is a helper class
	}

	public static int[] toArray(JsonArray array) {
		int len = array.size();
		int[] iarray = new int[len];
		for (int i = 0; i < len; i++) {
			iarray[i] = array.get(i).getAsInt();
		}
		return iarray;
	}

	public static double[] toDoubleArray(JsonArray array) {
		int len = array.size();
		double[] darray = new double[len];
		for (int i = 0; i < len; i++) {
			darray[i] = toDouble(array.get(i));
		}
		return darray;
	}

	public static double toDouble(JsonElement elem) {
		if (elem != null && !elem.isJsonNull()) {
			JsonPrimitive primitive = elem.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				return primitive.getAsDouble();
			} else if (primitive.isString()) {
				if ("INF".equals(primitive.getAsString())) {
					return Double.POSITIVE_INFINITY;
				} else if ("NaN".equals(primitive.getAsString())) {
					return Double.NaN;
				}
			}
		}
		return Double.NaN;
	}

	public static Object convertNullToMap(OptionsInterface o) {
		if (o == null) {
			return new MapBuilder().get();
		}
		return o.toMap();
	}

	public static Object convertNullToMap(Object o) {
		if (o == null) {
			return new MapBuilder().get();
		}
		return o;
	}

	public static String convertBaseDocumentToJson(JsonObject jsonObject) {
		JsonObject result = jsonObject.getAsJsonObject("properties");

		JsonElement keyObject = jsonObject.get("_key");
		if ((null != keyObject) && (JsonNull.class != keyObject.getClass())) {
			result.add("_key", jsonObject.get("_key"));
		}
		JsonElement handleObject = jsonObject.get("_id");
		if ((null != handleObject) && (JsonNull.class != handleObject.getClass())) {
			result.add("_id", jsonObject.get("_id"));
		}
		// JsonElement revisionObject = jsonObject.get("documentRevision");
		// result.add("_rev", revisionObject);

		return result.toString();
	}

}

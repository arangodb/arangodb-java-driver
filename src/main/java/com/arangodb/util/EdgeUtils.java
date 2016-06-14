package com.arangodb.util;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.EntityFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class EdgeUtils {

	private EdgeUtils() {
		super();
	}

	public static <T> JsonObject valueToEdgeJsonObject(
		final String key,
		final String fromHandle,
		final String toHandle,
		final T value) {
		JsonObject obj;
		if (value == null) {
			obj = new JsonObject();
		} else {
			final JsonElement elem = EntityFactory.toJsonElement(value, false);
			if (elem.isJsonObject()) {
				obj = elem.getAsJsonObject();
			} else {
				throw new IllegalArgumentException("value need object type(not support array, primitive, etc..).");
			}
		}
		if (key != null) {
			obj.addProperty(BaseDocument.KEY, key);
		}
		if (fromHandle != null) {
			obj.addProperty(BaseDocument.FROM, fromHandle);
		}
		if (toHandle != null) {
			obj.addProperty(BaseDocument.TO, toHandle);
		}
		return obj;
	}
}

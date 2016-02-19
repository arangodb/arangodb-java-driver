package com.arangodb.util;

import java.util.List;
import java.util.Map;

public abstract class AbstractOptions {

	protected void putAttribute(Map<String, Object> object, String key, Object value) {
		if (value != null) {
			object.put(key, value);
		}
	}

	protected void putAttributeToLower(Map<String, Object> object, String key, Object value) {
		if (value != null) {
			object.put(key, value.toString().toLowerCase());
		}
	}

	protected void putAttribute(MapBuilder object, String key, Object value) {
		if (value != null) {
			object.put(key, value);
		}
	}

	protected void putAttributeToLower(MapBuilder object, String key, Object value) {
		if (value != null) {
			object.put(key, value.toString().toLowerCase());
		}
	}

	protected void putAttributeCollection(MapBuilder object, String key, List<?> value) {
		if (CollectionUtils.isNotEmpty(value)) {
			object.put(key, value);
		}
	}
}

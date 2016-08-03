package com.arangodb.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class MapBuilder {

	private final boolean ignoreValue;
	private final LinkedHashMap<String, Object> map;

	public MapBuilder() {
		this(true);
	}

	public MapBuilder(final boolean ignoreValue) {
		this.ignoreValue = ignoreValue;
		map = new LinkedHashMap<>();
	}

	public MapBuilder put(final String key, final Object value) {
		return put(key, value, false);
	}

	public MapBuilder put(final String key, final Object value, final boolean toString) {
		if (!this.ignoreValue || (key != null && value != null)) {
			map.put(key, toString ? value.toString() : value);
		}
		return this;
	}

	public Map<String, Object> get() {
		return map;
	}
}

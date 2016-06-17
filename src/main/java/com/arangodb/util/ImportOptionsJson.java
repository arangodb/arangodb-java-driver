package com.arangodb.util;

import java.util.Map;

public class ImportOptionsJson extends ImportOptions {

	private String fromPrefix;
	private String toPrefix;

	/**
	 * An optional prefix for the values in _from attributes. If specified, the
	 * value is automatically prepended to each _from input value. This allows
	 * specifying just the keys for _from.
	 * 
	 * @return prefix for the values in _from attributes
	 */
	public String getFromPrefix() {
		return fromPrefix;
	}

	/**
	 * An optional prefix for the values in _from attributes. If specified, the
	 * value is automatically prepended to each _from input value. This allows
	 * specifying just the keys for _from.
	 * 
	 * @param fromPrefix
	 * @return this ImportOptionsJson object
	 */
	public ImportOptionsJson setFromPrefix(String fromPrefix) {
		this.fromPrefix = fromPrefix;
		return this;
	}

	/**
	 * An optional prefix for the values in _to attributes. If specified, the
	 * value is automatically prepended to each _to input value. This allows
	 * specifying just the keys for _to.
	 * 
	 * @return prefix for the values in _to attributes
	 */
	public String getToPrefix() {
		return toPrefix;
	}

	/**
	 * An optional prefix for the values in _to attributes. If specified, the
	 * value is automatically prepended to each _to input value. This allows
	 * specifying just the keys for _to.
	 * 
	 * @param toPrefix
	 * @return this ImportOptionsJson object
	 */
	public ImportOptionsJson setToPrefix(String toPrefix) {
		this.toPrefix = toPrefix;
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();

		if (fromPrefix != null) {
			map.put("fromPrefix", fromPrefix);
		}

		if (toPrefix != null) {
			map.put("toPrefix", toPrefix);
		}

		return map;
	}

}

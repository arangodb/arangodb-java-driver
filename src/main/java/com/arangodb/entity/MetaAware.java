package com.arangodb.entity;

import java.util.Map;

/**
 * @author Mark Vollmary
 *
 */
public interface MetaAware {

	Map<String, String> getMeta();

	void setMeta(final Map<String, String> meta);

}

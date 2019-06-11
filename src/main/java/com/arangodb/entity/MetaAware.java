package com.arangodb.entity;

import java.util.Map;

/**
 * @author Mark Vollmary
 *
 */
public interface MetaAware {

	public Map<String, String> getMeta();

	public void setMeta(final Map<String, String> meta);

}

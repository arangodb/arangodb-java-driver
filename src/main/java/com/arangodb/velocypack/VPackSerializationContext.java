package com.arangodb.velocypack;

import java.util.Map;

import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackSerializationContext {

	void serialize(final VPackBuilder builder, final String attribute, final Object entity) throws VPackParserException;

	void serialize(final VPackBuilder builder, final String attribute, final Map<?, ?> entity, final Class<?> keyType)
			throws VPackParserException;

}

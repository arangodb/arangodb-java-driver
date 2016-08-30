package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackSerializer<T> {

	void serialize(VPackBuilder builder, String attribute, T value, VPackSerializationContext context)
			throws VPackException;

}

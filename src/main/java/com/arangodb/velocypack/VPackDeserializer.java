package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackDeserializer<T> {

	T deserialize(VPackSlice parent, VPackSlice vpack, VPackDeserializationContext context) throws VPackException;

}

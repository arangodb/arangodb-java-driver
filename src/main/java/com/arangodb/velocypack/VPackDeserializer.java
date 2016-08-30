package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackDeserializer<T> {

	T deserialize(VPackSlice parent, VPackSlice vpack, VPackDeserializationContext context) throws VPackException;

}

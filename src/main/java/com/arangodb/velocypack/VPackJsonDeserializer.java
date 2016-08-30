package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public interface VPackJsonDeserializer {

	void deserialize(VPackSlice parent, String attribute, VPackSlice vpack, StringBuilder json) throws VPackException;

}

package com.arangodb.velocypack;

import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackDeserializationContext {

	<T> T deserialize(final VPackSlice vpack, final Class<T> type) throws VPackParserException;

}

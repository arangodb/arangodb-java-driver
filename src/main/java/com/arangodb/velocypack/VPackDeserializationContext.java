package com.arangodb.velocypack;

import java.util.Collection;
import java.util.Map;

import com.arangodb.velocypack.exception.VPackParserException;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public interface VPackDeserializationContext {

	<T> T deserialize(final VPackSlice vpack, final Class<T> type) throws VPackParserException;

	<T extends Collection<C>, C> T deserialize(final VPackSlice vpack, final Class<T> type, final Class<C> contentType)
			throws VPackParserException;

	<T extends Map<K, C>, K, C> T deserialize(
		final VPackSlice vpack,
		final Class<T> type,
		final Class<K> keyType,
		final Class<C> contentType) throws VPackParserException;

}

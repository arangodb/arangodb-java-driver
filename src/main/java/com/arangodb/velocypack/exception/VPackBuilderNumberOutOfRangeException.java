package com.arangodb.velocypack.exception;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackBuilderNumberOutOfRangeException extends VPackBuilderException {

	public VPackBuilderNumberOutOfRangeException(final ValueType type) {
		super(String.format("Number out of range of %s.%s", type.getClass().getSimpleName(), type.name()));
	}

}

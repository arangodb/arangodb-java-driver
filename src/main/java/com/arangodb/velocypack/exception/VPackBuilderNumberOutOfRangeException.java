package com.arangodb.velocypack.exception;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackBuilderNumberOutOfRangeException extends VPackBuilderException {

	private static final long serialVersionUID = 7173727199390076286L;

	public VPackBuilderNumberOutOfRangeException(final ValueType type) {
		super(String.format("Number out of range of %s.%s", type.getClass().getSimpleName(), type.name()));
	}

}

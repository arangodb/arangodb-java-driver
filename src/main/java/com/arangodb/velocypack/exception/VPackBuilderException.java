package com.arangodb.velocypack.exception;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public class VPackBuilderException extends VPackException {

	public VPackBuilderException() {
		super();
	}

	public VPackBuilderException(final String message) {
		super(message);
	}

	public VPackBuilderException(final Throwable cause) {
		super(cause);
	}

}

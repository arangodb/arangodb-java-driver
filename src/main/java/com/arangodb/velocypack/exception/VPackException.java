package com.arangodb.velocypack.exception;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public abstract class VPackException extends Exception {

	protected VPackException() {
		super();
	}

	protected VPackException(final String message) {
		super(message);
	}

	protected VPackException(final Throwable cause) {
		super(cause);
	}

}

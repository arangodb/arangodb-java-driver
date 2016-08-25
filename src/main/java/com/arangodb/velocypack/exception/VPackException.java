package com.arangodb.velocypack.exception;

/**
 * @author Mark - mark@arangodb.com
 *
 */
public abstract class VPackException extends Exception {

	private static final long serialVersionUID = 3547943271830879415L;

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

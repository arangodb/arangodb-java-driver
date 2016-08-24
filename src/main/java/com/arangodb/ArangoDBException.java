package com.arangodb;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBException extends RuntimeException {

	private static final long serialVersionUID = 6165638002614173801L;

	public ArangoDBException(final String message) {
		super(message);
	}

	public ArangoDBException(final Throwable cause) {
		super(cause);
	}

}

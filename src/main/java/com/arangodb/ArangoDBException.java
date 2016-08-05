package com.arangodb;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoDBException extends RuntimeException {

	public ArangoDBException(final Exception e) {
		super(e);
	}

}

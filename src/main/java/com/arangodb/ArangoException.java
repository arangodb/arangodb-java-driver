package com.arangodb;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ArangoException extends Exception {

	public ArangoException(final Exception e) {
		super(e);
	}

}

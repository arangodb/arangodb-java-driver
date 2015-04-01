package com.arangodb;

/**
 * @author mrbatista
 */
public class NonUniqueResultException extends RuntimeException {

	/**
	 * Constructs a NonUniqueResultException.
	 *
	 * @param resultCount
	 *            The number of actual results.
	 */
	public NonUniqueResultException(int resultCount) {
		super("Query did not return a unique result: " + resultCount);
	}

}

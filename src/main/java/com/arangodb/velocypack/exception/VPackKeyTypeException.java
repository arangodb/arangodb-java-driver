package com.arangodb.velocypack.exception;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackKeyTypeException extends VPackException {

	private static final long serialVersionUID = 1328826711387001473L;

	public VPackKeyTypeException(final String message) {
		super(message);
	}

}

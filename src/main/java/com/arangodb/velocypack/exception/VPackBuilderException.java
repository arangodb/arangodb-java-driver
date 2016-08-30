package com.arangodb.velocypack.exception;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackBuilderException extends VPackException {

	private static final long serialVersionUID = -8439245715363257017L;

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

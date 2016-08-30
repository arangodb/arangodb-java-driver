package com.arangodb.velocypack.exception;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackBuilderUnexpectedValueException extends VPackBuilderException {

	private static final long serialVersionUID = -7365305871886897353L;

	public VPackBuilderUnexpectedValueException(final ValueType type, final Class<?>... classes) {
		super(createMessage(type, null, classes));
	}

	public VPackBuilderUnexpectedValueException(final ValueType type, final String specify, final Class<?>... classes) {
		super(createMessage(type, specify, classes));
	}

	private static String createMessage(final ValueType type, final String specify, final Class<?>... classes) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Must give ");
		if (specify != null) {
			sb.append(specify);
			sb.append(" ");
		}
		for (int i = 0; i < classes.length; i++) {
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(classes[i].getSimpleName());
		}
		sb.append(" for ");
		sb.append(type.getClass().getSimpleName());
		sb.append(".");
		sb.append(type.name());
		return sb.toString();
	}

}

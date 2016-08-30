package com.arangodb.velocypack.exception;

import com.arangodb.velocypack.ValueType;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VPackValueTypeException extends IllegalStateException {

	private static final long serialVersionUID = 8128171173539033177L;

	public VPackValueTypeException(final ValueType... types) {
		super(createMessage(types));
	}

	private static String createMessage(final ValueType... types) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Expecting type ");
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(types[i].name());
		}
		return sb.toString();
	}

}

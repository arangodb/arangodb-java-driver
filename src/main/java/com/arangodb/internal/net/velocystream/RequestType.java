package com.arangodb.internal.net.velocystream;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public enum RequestType {

	GET(1), POST(2), PUT(3), DELETE(4), PATCH(5);

	private final int type;

	private RequestType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static RequestType fromType(final int type) {
		for (final RequestType rType : RequestType.values()) {
			if (rType.type == type) {
				return rType;
			}
		}
		return null;
	}
}

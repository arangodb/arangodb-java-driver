package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public enum CollectionType {

	DOCUMENT(2), EDGES(3);

	private final int type;

	private CollectionType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static CollectionType fromType(final int type) {
		for (final CollectionType cType : CollectionType.values()) {
			if (cType.type == type) {
				return cType;
			}
		}
		return null;
	}
}

package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public enum CollectionStatus {

	NEW_BORN_COLLECTION(1), UNLOADED(2), LOADED(3), IN_THE_PROCESS_OF_BEING_UNLOADED(4), DELETED(5);

	private final int status;

	private CollectionStatus(final int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public static CollectionStatus fromStatus(final int status) {
		for (final CollectionStatus cStatus : CollectionStatus.values()) {
			if (cStatus.status == status) {
				return cStatus;
			}
		}
		return null;
	}

}

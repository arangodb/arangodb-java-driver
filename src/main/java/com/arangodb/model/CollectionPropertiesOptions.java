package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionPropertiesOptions {

	private Boolean waitForSync;
	private Long journalSize;

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public CollectionPropertiesOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Long getJournalSize() {
		return journalSize;
	}

	public CollectionPropertiesOptions journalSize(final Long journalSize) {
		this.journalSize = journalSize;
		return this;
	}

}

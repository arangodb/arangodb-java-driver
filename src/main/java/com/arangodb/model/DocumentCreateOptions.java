package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentCreateOptions {

	private Boolean waitForSync;
	private Boolean returnNew;

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public DocumentCreateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getReturnNew() {
		return returnNew;
	}

	public DocumentCreateOptions returnNew(final Boolean returnNew) {
		this.returnNew = returnNew;
		return this;
	}

}

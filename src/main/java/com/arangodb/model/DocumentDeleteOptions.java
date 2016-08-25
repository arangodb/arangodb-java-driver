package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDeleteOptions {

	private Boolean waitForSync;
	private String ifMatch;
	private Boolean returnOld;

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public DocumentDeleteOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public DocumentDeleteOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	public DocumentDeleteOptions returnOld(final Boolean returnOld) {
		this.returnOld = returnOld;
		return this;
	}

}

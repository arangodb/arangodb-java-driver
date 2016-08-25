package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentReplaceOptions {

	private Boolean waitForSync;
	private Boolean ignoreRevs;
	private String ifMatch;
	private Boolean returnNew;
	private Boolean returnOld;

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public DocumentReplaceOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getIgnoreRevs() {
		return ignoreRevs;
	}

	public DocumentReplaceOptions ignoreRevs(final Boolean ignoreRevs) {
		this.ignoreRevs = ignoreRevs;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public DocumentReplaceOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

	public Boolean getReturnNew() {
		return returnNew;
	}

	public DocumentReplaceOptions returnNew(final Boolean returnNew) {
		this.returnNew = returnNew;
		return this;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	public DocumentReplaceOptions returnOld(final Boolean returnOld) {
		this.returnOld = returnOld;
		return this;
	}

}

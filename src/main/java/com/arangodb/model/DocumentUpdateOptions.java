package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentUpdateOptions {

	private Boolean keepNull;
	private Boolean mergeObjects;
	private Boolean waitForSync;
	private Boolean ignoreRevs;
	private String ifMatch;
	private Boolean returnNew;
	private Boolean returnOld;

	public Boolean getKeepNull() {
		return keepNull;
	}

	public DocumentUpdateOptions keepNull(final Boolean keepNull) {
		this.keepNull = keepNull;
		return this;
	}

	public Boolean getMergeObjects() {
		return mergeObjects;
	}

	public DocumentUpdateOptions mergeObjects(final Boolean mergeObjects) {
		this.mergeObjects = mergeObjects;
		return this;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public DocumentUpdateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getIgnoreRevs() {
		return ignoreRevs;
	}

	public DocumentUpdateOptions ignoreRevs(final Boolean ignoreRevs) {
		this.ignoreRevs = ignoreRevs;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public DocumentUpdateOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

	public Boolean getReturnNew() {
		return returnNew;
	}

	public DocumentUpdateOptions returnNew(final Boolean returnNew) {
		this.returnNew = returnNew;
		return this;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	public DocumentUpdateOptions returnOld(final Boolean returnOld) {
		this.returnOld = returnOld;
		return this;
	}

}

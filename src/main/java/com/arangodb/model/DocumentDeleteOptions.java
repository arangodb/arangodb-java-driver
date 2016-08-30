package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#removes-a-document">API
 *      Documentation</a>
 */
public class DocumentDeleteOptions {

	private Boolean waitForSync;
	private String ifMatch;
	private Boolean returnOld;

	public DocumentDeleteOptions() {
		super();
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * @param waitForSync
	 *            Wait until deletion operation has been synced to disk.
	 * @return options
	 */
	public DocumentDeleteOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	/**
	 * @param ifMatch
	 *            remove a document based on a target revision
	 * @return options
	 */
	public DocumentDeleteOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	/**
	 * @param returnOld
	 *            Return additionally the complete previous revision of the changed document under the attribute old in
	 *            the result.
	 * @return options
	 */
	public DocumentDeleteOptions returnOld(final Boolean returnOld) {
		this.returnOld = returnOld;
		return this;
	}

}

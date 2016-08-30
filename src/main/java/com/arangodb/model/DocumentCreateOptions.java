package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#create-document">API
 *      Documentation</a>
 */
public class DocumentCreateOptions {

	private Boolean waitForSync;
	private Boolean returnNew;

	public DocumentCreateOptions() {
		super();
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * @param waitForSync
	 *            Wait until document has been synced to disk.
	 * @return options
	 */
	public DocumentCreateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getReturnNew() {
		return returnNew;
	}

	/**
	 * @param returnNew
	 *            Return additionally the complete new document under the attribute new in the result.
	 * @return options
	 */
	public DocumentCreateOptions returnNew(final Boolean returnNew) {
		this.returnNew = returnNew;
		return this;
	}

}

package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#replace-document">API
 *      Documentation</a>
 */
public class DocumentReplaceOptions {

	private Boolean waitForSync;
	private Boolean ignoreRevs;
	private String ifMatch;
	private Boolean returnNew;
	private Boolean returnOld;

	public DocumentReplaceOptions() {
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
	public DocumentReplaceOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

	public Boolean getIgnoreRevs() {
		return ignoreRevs;
	}

	/**
	 * @param ignoreRevs
	 *            By default, or if this is set to true, the _rev attributes in the given document is ignored. If this
	 *            is set to false, then the _rev attribute given in the body document is taken as a precondition. The
	 *            document is only replaced if the current revision is the one specified.
	 * @return options
	 */
	public DocumentReplaceOptions ignoreRevs(final Boolean ignoreRevs) {
		this.ignoreRevs = ignoreRevs;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	/**
	 * @param ifMatch
	 *            replace a document based on target revision
	 * @return options
	 */
	public DocumentReplaceOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
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
	public DocumentReplaceOptions returnNew(final Boolean returnNew) {
		this.returnNew = returnNew;
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
	public DocumentReplaceOptions returnOld(final Boolean returnOld) {
		this.returnOld = returnOld;
		return this;
	}

}

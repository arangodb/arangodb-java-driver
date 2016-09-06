package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VertexUpdateOptions {

	private Boolean keepNull;
	private Boolean waitForSync;
	private String ifMatch;

	public VertexUpdateOptions() {
		super();
	}

	public Boolean getKeepNull() {
		return keepNull;
	}

	/**
	 * @param keepNull
	 *            If the intention is to delete existing attributes with the patch command, the URL query parameter
	 *            keepNull can be used with a value of false. This will modify the behavior of the patch command to
	 *            remove any attributes from the existing document that are contained in the patch document with an
	 *            attribute value of null.
	 * @return options
	 */
	public VertexUpdateOptions keepNull(final Boolean keepNull) {
		this.keepNull = keepNull;
		return this;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	/**
	 * @param waitForSync
	 *            Wait until document has been synced to disk.
	 * @return options
	 */
	public VertexUpdateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
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
	public VertexUpdateOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}
}

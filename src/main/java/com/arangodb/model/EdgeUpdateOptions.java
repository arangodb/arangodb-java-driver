package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#modify-an-edge">API Documentation</a>
 */
public class EdgeUpdateOptions {

	private Boolean keepNull;
	private Boolean waitForSync;
	private String ifMatch;

	public EdgeUpdateOptions() {
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
	public EdgeUpdateOptions keepNull(final Boolean keepNull) {
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
	public EdgeUpdateOptions waitForSync(final Boolean waitForSync) {
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
	public EdgeUpdateOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}
}

package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#remove-a-vertex">API Documentation</a>
 */
public class VertexDeleteOptions {

	private Boolean waitForSync;
	private String ifMatch;

	public VertexDeleteOptions() {
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
	public VertexDeleteOptions waitForSync(final Boolean waitForSync) {
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
	public VertexDeleteOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}
}

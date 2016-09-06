package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Vertices.html#create-a-vertex">API Documentation</a>
 */
public class VertexCreateOptions {

	private Boolean waitForSync;

	public VertexCreateOptions() {
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
	public VertexCreateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

}

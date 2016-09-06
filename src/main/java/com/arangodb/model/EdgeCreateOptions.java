package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Gharial/Edges.html#create-an-edge">API Documentation</a>
 */
public class EdgeCreateOptions {

	private Boolean waitForSync;

	public EdgeCreateOptions() {
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
	public EdgeCreateOptions waitForSync(final Boolean waitForSync) {
		this.waitForSync = waitForSync;
		return this;
	}

}

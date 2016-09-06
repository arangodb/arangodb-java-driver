package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class VertexReplaceOptions {
	private Boolean waitForSync;
	private String ifMatch;

	public VertexReplaceOptions() {
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
	public VertexReplaceOptions waitForSync(final Boolean waitForSync) {
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
	public VertexReplaceOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}
}

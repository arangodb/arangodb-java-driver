package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDelete {

	private final Boolean waitForSync;
	private final String ifMatch;

	private DocumentDelete(final Boolean waitForSync, final String ifMatch) {
		super();
		this.waitForSync = waitForSync;
		this.ifMatch = ifMatch;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public static class Options {

		private Boolean waitForSync;
		private String ifMatch;

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		public Options ifMatch(final String ifMatch) {
			this.ifMatch = ifMatch;
			return this;
		}

		protected DocumentDelete build() {
			return new DocumentDelete(waitForSync, ifMatch);
		}
	}

}

package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentReplace {

	private final Boolean waitForSync;
	private final Boolean ignoreRevs;
	private final String ifMatch;

	private DocumentReplace(final Boolean waitForSync, final Boolean ignoreRevs, final String ifMatch) {
		super();
		this.waitForSync = waitForSync;
		this.ignoreRevs = ignoreRevs;
		this.ifMatch = ifMatch;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public Boolean getIgnoreRevs() {
		return ignoreRevs;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public static class Options {

		private Boolean waitForSync;
		private Boolean ignoreRevs;
		private String ifMatch;

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		public Options ignoreRevs(final Boolean ignoreRevs) {
			this.ignoreRevs = ignoreRevs;
			return this;
		}

		public Options ifMatch(final String ifMatch) {
			this.ifMatch = ifMatch;
			return this;
		}

		protected DocumentReplace build() {
			return new DocumentReplace(waitForSync, ignoreRevs, ifMatch);
		}

	}

}

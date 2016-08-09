package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentReplace {

	private final Boolean waitForSync;
	private final Boolean ignoreRevs;
	private final String ifMatch;
	private final Boolean returnNew;
	private final Boolean returnOld;

	private DocumentReplace(final Boolean waitForSync, final Boolean ignoreRevs, final String ifMatch,
		final Boolean returnNew, final Boolean returnOld) {
		super();
		this.waitForSync = waitForSync;
		this.ignoreRevs = ignoreRevs;
		this.ifMatch = ifMatch;
		this.returnNew = returnNew;
		this.returnOld = returnOld;
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

	public Boolean getReturnNew() {
		return returnNew;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	public static class Options {

		private Boolean waitForSync;
		private Boolean ignoreRevs;
		private String ifMatch;
		private Boolean returnNew;
		private Boolean returnOld;

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

		public Options returnNew(final Boolean returnNew) {
			this.returnNew = returnNew;
			return this;
		}

		public Options returnOld(final Boolean returnOld) {
			this.returnOld = returnOld;
			return this;
		}

		protected DocumentReplace build() {
			return new DocumentReplace(waitForSync, ignoreRevs, ifMatch, returnNew, returnOld);
		}

	}

}

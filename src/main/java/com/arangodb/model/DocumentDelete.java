package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentDelete {

	private final Boolean waitForSync;
	private final String ifMatch;
	private final Boolean returnOld;

	private DocumentDelete(final Boolean waitForSync, final String ifMatch, final Boolean returnOld) {
		super();
		this.waitForSync = waitForSync;
		this.ifMatch = ifMatch;
		this.returnOld = returnOld;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public Boolean getReturnOld() {
		return returnOld;
	}

	public static class Options {

		private Boolean waitForSync;
		private String ifMatch;
		private Boolean returnOld;

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		public Options ifMatch(final String ifMatch) {
			this.ifMatch = ifMatch;
			return this;
		}

		public Options returnOld(final Boolean returnOld) {
			this.returnOld = returnOld;
			return this;
		}

		protected DocumentDelete build() {
			return new DocumentDelete(waitForSync, ifMatch, returnOld);
		}
	}

}

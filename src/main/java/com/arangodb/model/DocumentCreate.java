package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentCreate {

	private final Boolean waitForSync;
	private final Boolean returnNew;

	private DocumentCreate(final Boolean waitForSync, final Boolean returnNew) {
		super();
		this.waitForSync = waitForSync;
		this.returnNew = returnNew;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public Boolean getReturnNew() {
		return returnNew;
	}

	public static class Options {

		private Boolean waitForSync;
		private Boolean returnNew;

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		public Options returnNew(final Boolean returnNew) {
			this.returnNew = returnNew;
			return this;
		}

		protected DocumentCreate build() {
			return new DocumentCreate(waitForSync, returnNew);
		}
	}

}

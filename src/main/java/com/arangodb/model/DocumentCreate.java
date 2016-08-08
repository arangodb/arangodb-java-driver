package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentCreate {

	private final Boolean waitForSync;

	private DocumentCreate(final Boolean waitForSync) {
		super();
		this.waitForSync = waitForSync;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public static class Options {

		private Boolean waitForSync;

		public Options waitForSync(final Boolean waitForSync) {
			this.waitForSync = waitForSync;
			return this;
		}

		protected DocumentCreate build() {
			return new DocumentCreate(waitForSync);
		}
	}

}

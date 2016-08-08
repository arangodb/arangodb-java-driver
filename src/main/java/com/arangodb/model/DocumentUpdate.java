package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentUpdate {

	private final Boolean keepNull;
	private final Boolean mergeObjects;
	private final Boolean waitForSync;
	private final Boolean ignoreRevs;
	private final String ifMatch;

	private DocumentUpdate(final Boolean keepNull, final Boolean mergeObjects, final Boolean waitForSync,
		final Boolean ignoreRevs, final String ifMatch) {
		super();
		this.keepNull = keepNull;
		this.mergeObjects = mergeObjects;
		this.waitForSync = waitForSync;
		this.ignoreRevs = ignoreRevs;
		this.ifMatch = ifMatch;
	}

	public Boolean getKeepNull() {
		return keepNull;
	}

	public Boolean getMergeObjects() {
		return mergeObjects;
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

		private Boolean keepNull;
		private Boolean mergeObjects;
		private Boolean waitForSync;
		private Boolean ignoreRevs;
		private String ifMatch;

		public Options keepNull(final Boolean keepNull) {
			this.keepNull = keepNull;
			return this;
		}

		public Options mergeObjects(final Boolean mergeObjects) {
			this.mergeObjects = mergeObjects;
			return this;
		}

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

		protected DocumentUpdate build() {
			return new DocumentUpdate(keepNull, mergeObjects, waitForSync, ignoreRevs, ifMatch);
		}
	}

}

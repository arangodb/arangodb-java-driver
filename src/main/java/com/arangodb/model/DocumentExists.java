package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentExists {

	private final String ifNoneMatch;
	private final String ifMatch;

	private DocumentExists(final String ifNoneMatch, final String ifMatch) {
		super();
		this.ifNoneMatch = ifNoneMatch;
		this.ifMatch = ifMatch;
	}

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public static class Options {
		private String ifNoneMatch;
		private String ifMatch;

		public Options ifNoneMatch(final String ifNoneMatch) {
			this.ifNoneMatch = ifNoneMatch;
			return this;
		}

		public Options ifMatch(final String ifMatch) {
			this.ifMatch = ifMatch;
			return this;
		}

		protected DocumentExists build() {
			return new DocumentExists(ifNoneMatch, ifMatch);
		}
	}

}

package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href="https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document">API
 *      Documentation</a>
 */
public class DocumentReadOptions {

	private String ifNoneMatch;
	private String ifMatch;

	public DocumentReadOptions() {
		super();
	}

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	/**
	 * @param ifNoneMatch
	 *            document revision must not contain If-None-Match
	 * @return options
	 */
	public DocumentReadOptions ifNoneMatch(final String ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	/**
	 * @param ifMatch
	 *            document revision must contain If-Match
	 * @return options
	 */
	public DocumentReadOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

}

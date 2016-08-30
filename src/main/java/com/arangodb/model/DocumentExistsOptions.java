package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 * 
 * @see <a href= "https://docs.arangodb.com/current/HTTP/Document/WorkingWithDocuments.html#read-document-header">API
 *      Documentation</a>
 */
public class DocumentExistsOptions {

	private String ifNoneMatch;
	private String ifMatch;

	public DocumentExistsOptions() {
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
	public DocumentExistsOptions ifNoneMatch(final String ifNoneMatch) {
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
	public DocumentExistsOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

}

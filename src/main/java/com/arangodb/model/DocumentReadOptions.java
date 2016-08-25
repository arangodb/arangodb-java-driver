package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentReadOptions {

	private String ifNoneMatch;
	private String ifMatch;

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	public DocumentReadOptions ifNoneMatch(final String ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public DocumentReadOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

}

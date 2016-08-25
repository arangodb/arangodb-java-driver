package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentExistsOptions {

	private String ifNoneMatch;
	private String ifMatch;

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	public DocumentExistsOptions ifNoneMatch(final String ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
		return this;
	}

	public String getIfMatch() {
		return ifMatch;
	}

	public DocumentExistsOptions ifMatch(final String ifMatch) {
		this.ifMatch = ifMatch;
		return this;
	}

}

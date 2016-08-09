package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentResult {

	@DocumentKey
	private String key;
	@DocumentId
	private String id;
	@DocumentRev
	private String rev;

	public DocumentResult() {
		super();
	}

	public String getKey() {
		return key;
	}

	public String getId() {
		return id;
	}

	public String getRev() {
		return rev;
	}

}

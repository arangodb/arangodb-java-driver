package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentEntity {

	@DocumentKey
	private String key;
	@DocumentId
	private String id;
	@DocumentRev
	private String rev;

	public DocumentEntity() {
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

package com.arangodb.entity;

import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class DocumentResult {

	@DocumentField(Type.KEY)
	private String key;
	@DocumentField(Type.ID)
	private String id;
	@DocumentField(Type.REV)
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

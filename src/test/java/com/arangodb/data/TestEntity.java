package com.arangodb.data;

import com.arangodb.entity.DocumentField;
import com.arangodb.entity.DocumentField.Type;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class TestEntity {

	@DocumentField(Type.ID)
	private String id;
	@DocumentField(Type.KEY)
	private String key;
	@DocumentField(Type.REV)
	private String rev;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getRev() {
		return rev;
	}

	public void setRev(final String rev) {
		this.rev = rev;
	}

}

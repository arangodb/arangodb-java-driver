package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class KeyOptions {

	private Boolean allowUserKeys;
	private KeyType type;
	private Integer increment;
	private Integer offset;

	public KeyOptions() {
		super();
	}

	public KeyOptions(final Boolean allowUserKeys, final KeyType type, final Integer increment, final Integer offset) {
		super();
		this.allowUserKeys = allowUserKeys;
		this.type = type;
		this.increment = increment;
		this.offset = offset;
	}

	public Boolean getAllowUserKeys() {
		return allowUserKeys;
	}

	public void setAllowUserKeys(final Boolean allowUserKeys) {
		this.allowUserKeys = allowUserKeys;
	}

	public KeyType getType() {
		return type;
	}

	public void setType(final KeyType type) {
		this.type = type;
	}

	public Integer getIncrement() {
		return increment;
	}

	public void setIncrement(final Integer increment) {
		this.increment = increment;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(final Integer offset) {
		this.offset = offset;
	}

}

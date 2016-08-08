package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionEntity {

	private String name;
	private Boolean waitForSync;
	private Boolean isVolatile;
	private Boolean isSystem;
	private CollectionStatus status;
	private CollectionType type;

	public CollectionEntity() {
		super();
	}

	public String getName() {
		return name;
	}

	public Boolean getWaitForSync() {
		return waitForSync;
	}

	public Boolean getIsVolatile() {
		return isVolatile;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public CollectionStatus getStatus() {
		return status;
	}

	public CollectionType getType() {
		return type;
	}

}

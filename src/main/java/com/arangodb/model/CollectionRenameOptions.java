package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class CollectionRenameOptions {

	private String name;

	public CollectionRenameOptions() {
		super();
	}

	public String getName() {
		return name;
	}

	protected CollectionRenameOptions name(final String name) {
		this.name = name;
		return this;
	}

}

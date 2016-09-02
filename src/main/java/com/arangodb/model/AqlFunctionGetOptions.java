package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlFunctionGetOptions {

	private String namespace;

	public AqlFunctionGetOptions() {
		super();
	}

	public String getNamespace() {
		return namespace;
	}

	public AqlFunctionGetOptions namespace(final String namespace) {
		this.namespace = namespace;
		return this;
	}

}

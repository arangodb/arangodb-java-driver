package com.arangodb.model;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlFunctionCreateOptions {

	private String name;
	private String code;
	private Boolean isDeterministic;

	public AqlFunctionCreateOptions() {
		super();
	}

	protected AqlFunctionCreateOptions name(final String name) {
		this.name = name;
		return this;
	}

	protected String getName() {
		return name;
	}

	protected AqlFunctionCreateOptions code(final String code) {
		this.code = code;
		return this;
	}

	protected String getCode() {
		return code;
	}

	public AqlFunctionCreateOptions isDeterministic(final Boolean isDeterministic) {
		this.isDeterministic = isDeterministic;
		return this;
	}

	public Boolean getIsDeterministic() {
		return isDeterministic;
	}

}

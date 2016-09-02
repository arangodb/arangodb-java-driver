package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class AqlFunctionResult {

	private String name;
	private String code;

	public AqlFunctionResult() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

}

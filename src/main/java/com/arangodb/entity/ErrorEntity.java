package com.arangodb.entity;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class ErrorEntity {

	private boolean error;
	private String errorMessage;
	private int code;
	private int errorNum;

	public ErrorEntity() {
		super();
	}

	public boolean isError() {
		return error;
	}

	public void setError(final boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getCode() {
		return code;
	}

	public void setCode(final int code) {
		this.code = code;
	}

	public int getErrorNum() {
		return errorNum;
	}

	public void setErrorNum(final int errorNum) {
		this.errorNum = errorNum;
	}

}

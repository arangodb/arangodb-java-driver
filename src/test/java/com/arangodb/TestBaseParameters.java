package com.arangodb;

public class TestBaseParameters {

	private boolean error;
	private int code;
	private int errorNum;
	private String errorMessage;
	private int etag;

	public TestBaseParameters(
			boolean error,
			int code,
			int errorNum,
			String errorMessage,
			int etag) {
		this.error = error;
		this.code = code;
		this.errorNum = errorNum;
		this.errorMessage = errorMessage;
		this.etag = etag;
	}
	
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public int getErrorNum() {
		return errorNum;
	}
	public void setErrorNum(int errorNum) {
		this.errorNum = errorNum;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public int getEtag() {
		return etag;
	}
	public void setEtag(int etag) {
		this.etag = etag;
	}

}

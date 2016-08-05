package com.arangodb.internal.net;

import java.util.Optional;

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Response {

	private int version = 1;
	private int type = 2;
	private int responseCode;
	@Expose(deserialize = false)
	private Optional<VPackSlice> body = Optional.empty();

	public Response(final int responseCode) {
		super();
		this.responseCode = responseCode;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public int getType() {
		return type;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(final int responseCode) {
		this.responseCode = responseCode;
	}

	public Optional<VPackSlice> getBody() {
		return body;
	}

	public void setBody(final VPackSlice body) {
		this.body = Optional.ofNullable(body);
	}

}

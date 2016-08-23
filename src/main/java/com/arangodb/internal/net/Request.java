package com.arangodb.internal.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.arangodb.internal.net.velocystream.RequestType;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class Request {

	private int version = 1;
	private int type = 1;
	private String database;
	private RequestType requestType;
	private String request;
	private Map<String, String> parameter;
	private Map<String, String> meta;
	@Expose(serialize = false)
	private Optional<VPackSlice> body;

	public Request(final String database, final RequestType requestType, final String path) {
		super();
		this.database = database;
		this.requestType = requestType;
		this.request = path;
		body = Optional.empty();
		parameter = new HashMap<>();
		meta = new HashMap<>();
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

	public String getDatabase() {
		return database;
	}

	public void setDatabase(final String database) {
		this.database = database;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(final RequestType requestType) {
		this.requestType = requestType;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(final String request) {
		this.request = request;
	}

	private Map<String, String> getParameter() {
		if (parameter == null) {
			parameter = new HashMap<>();
		}
		return parameter;
	}

	public void putParameter(final String key, final Object value) {
		if (value != null) {
			getParameter().put(key, value.toString());
		}
	}

	private Map<String, String> getMeta() {
		if (meta == null) {
			meta = new HashMap<>();
		}
		return meta;
	}

	public void putMeta(final String key, final String value) {
		if (value != null) {
			getMeta().put(key, value);
		}
	}

	public Optional<VPackSlice> getBody() {
		return body;
	}

	public void setBody(final VPackSlice body) {
		this.body = Optional.ofNullable(body);
	}

}

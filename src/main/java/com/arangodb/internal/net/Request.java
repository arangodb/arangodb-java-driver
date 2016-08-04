package com.arangodb.internal.net;

import java.util.Map;

/**
 * @author Mark - mark at arangodb.com
 * @param <T>
 *
 */
public class Request {

	public enum RequestType {
		GET(1), POST(2), PUT(3), DELETE(4), PATCH(5);
		private final int n;

		private RequestType(final int n) {
			this.n = n;

		}
	}

	private int version;
	private int type;
	private String database;
	private RequestType requestType;
	private String request;
	private Map<String, Object> parameter;
	private Map<String, String> meta;

}

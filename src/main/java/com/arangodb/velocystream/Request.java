/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.velocystream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.annotations.Expose;

/**
 * @author Mark - mark at arangodb.com
 *
 */
public class Request {

	private int version = 1;
	private int type = 1;
	private final String database;
	private final RequestType requestType;
	private final String request;
	private Map<String, String> queryParam;
	private Map<String, String> headerParam;
	@Expose(serialize = false)
	private Optional<VPackSlice> body;

	public Request(final String database, final RequestType requestType, final String path) {
		super();
		this.database = database;
		this.requestType = requestType;
		this.request = path;
		body = Optional.empty();
		queryParam = new HashMap<>();
		headerParam = new HashMap<>();
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

	public RequestType getRequestType() {
		return requestType;
	}

	public String getRequest() {
		return request;
	}

	public Map<String, String> getQueryParam() {
		if (queryParam == null) {
			queryParam = new HashMap<>();
		}
		return queryParam;
	}

	public void putQueryParam(final String key, final Object value) {
		if (value != null) {
			getQueryParam().put(key, value.toString());
		}
	}

	public Map<String, String> getHeaderParam() {
		if (headerParam == null) {
			headerParam = new HashMap<>();
		}
		return headerParam;
	}

	public void putHeaderParam(final String key, final String value) {
		if (value != null) {
			getHeaderParam().put(key, value);
		}
	}

	public Optional<VPackSlice> getBody() {
		return body;
	}

	public void setBody(final VPackSlice body) {
		this.body = Optional.ofNullable(body);
	}

}

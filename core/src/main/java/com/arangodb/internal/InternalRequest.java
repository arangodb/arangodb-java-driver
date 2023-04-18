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

package com.arangodb.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class InternalRequest {

    private final String dbName;
    private final RequestType requestType;
    private final String path;
    private final Map<String, String> queryParam;
    private final Map<String, String> headerParam;
    private int version = 1;
    private int type = 1;
    private byte[] body;

    public InternalRequest(final String dbName, final RequestType requestType, final String path) {
        super();
        this.dbName = dbName;
        this.requestType = requestType;
        this.path = path;
        body = null;
        queryParam = new HashMap<>();
        headerParam = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public InternalRequest setVersion(final int version) {
        this.version = version;
        return this;
    }

    public int getType() {
        return type;
    }

    public InternalRequest setType(final int type) {
        this.type = type;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParam() {
        return queryParam;
    }

    public InternalRequest putQueryParam(final String key, final Object value) {
        if (value != null) {
            queryParam.put(key, value.toString());
        }
        return this;
    }

    public InternalRequest putQueryParams(final Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> it : params.entrySet()) {
                putQueryParam(it.getKey(), it.getValue());
            }
        }
        return this;
    }

    public Map<String, String> getHeaderParam() {
        return Collections.unmodifiableMap(headerParam);
    }

    public boolean containsHeaderParam(final String key) {
        return headerParam.containsKey(key.toLowerCase(Locale.ROOT));
    }

    public InternalRequest putHeaderParam(final String key, final String value) {
        if (value != null) {
            headerParam.put(key.toLowerCase(Locale.ROOT), value);
        }
        return this;
    }

    public InternalRequest putHeaderParams(final Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> it : params.entrySet()) {
                putHeaderParam(it.getKey(), it.getValue());
            }
        }
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public InternalRequest setBody(final byte[] body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "requestType=" + requestType +
                ", database='" + dbName + '\'' +
                ", url='" + path + '\'' +
                ", parameters=" + queryParam +
                ", headers=" + headerParam +
                '}';
    }

}

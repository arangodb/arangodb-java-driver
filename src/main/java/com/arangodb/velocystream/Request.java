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

import com.arangodb.DbName;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vollmary
 */
public class Request {

    private int version = 1;
    private int type = 1;
    private final DbName dbName;
    private final RequestType requestType;
    private final String request;
    private final Map<String, String> queryParam;
    private final Map<String, String> headerParam;
    @Expose(serialize = false)
    private VPackSlice body;

    /**
     * @deprecated Use {@link #Request(DbName, RequestType, String)} instead
     */
    @Deprecated
    public Request(final String database, final RequestType requestType, final String path) {
        this(DbName.of(database), requestType, path);
    }

    public Request(final DbName dbName, final RequestType requestType, final String path) {
        super();
        this.dbName = dbName;
        this.requestType = requestType;
        this.request = path;
        body = null;
        queryParam = new HashMap<>();
        headerParam = new HashMap<>();
    }

    public int getVersion() {
        return version;
    }

    public Request setVersion(final int version) {
        this.version = version;
        return this;
    }

    public int getType() {
        return type;
    }

    public Request setType(final int type) {
        this.type = type;
        return this;
    }

    /**
     * @deprecated Use {@link #getDbName()} instead
     */
    @Deprecated
    public String getDatabase() {
        return getDbName().get();
    }

    public DbName getDbName() {
        return dbName;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, String> getQueryParam() {
        return queryParam;
    }

    public Request putQueryParam(final String key, final Object value) {
        if (value != null) {
            queryParam.put(key, value.toString());
        }
        return this;
    }

    public Map<String, String> getHeaderParam() {
        return headerParam;
    }

    public Request putHeaderParam(final String key, final String value) {
        if (value != null) {
            headerParam.put(key, value);
        }
        return this;
    }

    public VPackSlice getBody() {
        return body;
    }

    public Request setBody(final VPackSlice body) {
        this.body = body;
        return this;
    }

}
